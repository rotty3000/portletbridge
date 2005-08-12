/*
 * Copyright 2001-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.portletbridge.portlet.old;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.portlet.PortletURL;
import javax.portlet.RenderResponse;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.portletbridge.PortletBridgeException;
import org.portletbridge.UrlRewriter;
import org.portletbridge.UrlUtils;

/**
 * Rewrites urls so that they go back through the portletbridge
 * 
 * @author jmccrindle
 */
public class PortletUrlRewriter implements UrlRewriter {

    private final RenderResponse response;

    private final URI currentUrl;
    
    private final boolean encode;

    /**
     * Create a new PortletURLRewriter. A new portlet url rewriter
     * should be created every time there is a new render.
     * 
     * @param currentUrl the current url of the downstream site
     * @param encode whether to encode the urls added as a parameter
     * @param response the current render response
     * @throws PortletBridgeException if there was a problem turning
     *         the currentUrl parameter into a url
     */
    public PortletUrlRewriter(String currentUrl, boolean encode, RenderResponse response) throws PortletBridgeException {
        this.response = response;
        this.encode = encode;
        try {
            this.currentUrl = new URI(currentUrl, false);
        } catch (URIException e) {
            throw new PortletBridgeException("error.bad.url", currentUrl, e);
        }
    }

    /**
     * Rewrites urls so that they come back through the processAction
     * for the portlet associated with the RenderResponse set in the 
     * constructor.
     * 
     * @param url a url to rewrite
     * @return the rewritten url
     */
    public String rewrite(String url) throws PortletBridgeException {
        PortletURL portletURL = response.createActionURL();
        try {
            // first turn it into an absolute url
            URI uri = new URI(url, false);
            if(uri.isAbsoluteURI()) {
                if(!"http".equalsIgnoreCase(uri.getScheme()) && !"https".equalsIgnoreCase(uri.getScheme())) {
                    return url;
                }
            } else {
                if (uri.isRelPath()) {
                    String path = currentUrl.getPath() != null ? UrlUtils.getPath(currentUrl.getURI()) : currentUrl.getURI();
                    url = UrlUtils.safeAppend(path, url);
                } else if(uri.isAbsPath()) {
                    if(uri.getHost() != null) {
                        String protocol = currentUrl.getScheme();
                        url = protocol + ':' + url;
                    } else {
                        String protocolHostPort = currentUrl.getScheme() + "://" + 
                            currentUrl.getHost() + 
                            (currentUrl.getPort() != -1 ? 
                                 (":" + currentUrl.getPort()) : "");
                        url = UrlUtils.safeAppend(protocolHostPort, url);
                    }
                }
            }
            try {
                portletURL.setParameter("actionurl", (encode ? URLEncoder.encode(url, "UTF-8") : url));
            } catch (UnsupportedEncodingException e) {
                // what kind of world do we live in where UTF-8 is unsupported?
                throw new RuntimeException(e);
            }
            return portletURL.toString();
        } catch (URIException e1) {
            throw new PortletBridgeException("error.uri.exception", e1.getMessage(), e1);
        }
    }
}