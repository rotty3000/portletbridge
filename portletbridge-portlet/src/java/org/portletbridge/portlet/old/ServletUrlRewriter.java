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

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.portletbridge.PortletBridgeException;
import org.portletbridge.UrlRewriter;
import org.portletbridge.UrlUtils;

/**
 * Rewrites resource urls so that go back through the PortletBridgeServlet
 * 
 * @author jmccrindle
 */
public class ServletUrlRewriter implements UrlRewriter {

    private final URI currentUrl;

    private String id;

    private final String contextPath;

    private final String servletName;

    /**
     * Create a new ServletUrlRewriter to rewrite urls that go through the 
     * servlet.
     * 
     * @param contextPath the context path of the current portlet (and servlet)
     * @param servletName the name of the servlet
     * @param id the id that the proxyBrowser is stored under in the session
     * @param currentUrl the currentUrl of the downstream site.
     */
    public ServletUrlRewriter(String contextPath, String servletName, String id, String currentUrl)  throws PortletBridgeException {
        this.contextPath = contextPath;
        this.servletName = servletName;
        this.id = id;
        try {
            this.currentUrl = new URI(currentUrl, false);
        } catch (URIException e) {
            throw new PortletBridgeException("error.bad.url", currentUrl, e);
        }
    }

    /**
     * should change url to: /contextpath/servlet/id/url
     */
    public String rewrite(String url) throws PortletBridgeException {
        String result = url;
        try {
            // first turn it into an absolute url
            URI uri = new URI(url, false);
            if(uri.isAbsoluteURI()) {
                // noop
            } else {
                if (uri.isRelPath()) {
                    String path = currentUrl.getPath() != null ? UrlUtils.getPath(currentUrl.getURI()) : currentUrl.getURI();
                    result = UrlUtils.safeAppend(path, url);
                } else if(uri.isAbsPath()) {
                    if(uri.getHost() != null) {
                        String protocol = currentUrl.getScheme();
                        result = protocol + ':' + url;
                    } else {
                        String protocolHostPort = currentUrl.getScheme() + "://" + 
                        currentUrl.getHost() + 
                        (currentUrl.getPort() != -1 ? 
                             (":" + currentUrl.getPort()) : "");
                        result = UrlUtils.safeAppend(protocolHostPort, url);
                    }
                }
            }
            return contextPath + '/' + servletName + '/' + id + '/' + result;
        } catch (URIException e1) {
            throw new PortletBridgeException("error.uri.exception", e1.getMessage(), e1);
        }
    }
}