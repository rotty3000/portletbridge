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
package org.portletbridge.rewriter;

import org.portletbridge.UrlRewriter;
import org.portletbridge.UrlUtils;

/**
 * @author JMcCrindle
 */
public class FullUrlRewriter implements UrlRewriter {
    
    private String currentUrl;
    private String currentProtocolHostPort;
    private String newBaseUrl;
    private String newBasePath;
    private String newBaseContext;

    /**
     * @param baseUrl
     */
    public FullUrlRewriter(String currentUrl, String newBaseUrl) {
        this.currentUrl = currentUrl;
        this.newBaseUrl = newBaseUrl;
        this.newBaseContext = getBaseContext(newBaseUrl);
        this.newBasePath = newBaseContext + '/' + UrlUtils.getProtocolHostPort(newBaseUrl.substring(newBaseContext.length() + 1));
        this.currentProtocolHostPort = UrlUtils.getProtocolHostPort(currentUrl);
    }

    /**
     * @param newBaseUrl2
     * @return
     */
    protected String getBaseContext(String newBaseUrl) {
        int indexOfColon = newBaseUrl.indexOf(':');
        if(indexOfColon != -1) {
            int lastIndexOfContext = newBaseUrl.lastIndexOf('/', indexOfColon);
            if(lastIndexOfContext == -1) {
                throw new RuntimeException("BaseUrl must have full url in it e.g. " +
                        "/new/http://asdfadsaf:80 but is currently " + newBaseUrl);
            }
            return newBaseUrl.substring(0, lastIndexOfContext);
        } else {
            throw new RuntimeException("BaseUrl must have full url in it e.g. " +
                    "/new/http://asdfadsaf:80 but is currently " + newBaseUrl);
        }
    }

    /* (non-Javadoc)
     * @see org.portletbridge.xsl.LinkRewriter#rewrite(java.lang.String)
     */
    public String rewrite(String url) {
        if(UrlUtils.isRelativeHttp(url)) {
            return url;
        } else if(UrlUtils.isAbsoluteWithHostHttp(url)) {
            String protocol = UrlUtils.getProtocol(currentUrl);
            return newBaseContext + '/' + protocol + ':' + url;
        } else if(UrlUtils.isAbsoluteHttp(url)) {
            return UrlUtils.safeAppend(newBasePath, url);
        } else if(UrlUtils.isFullHttp(url)) {
            if(currentProtocolHostPort != null) {
                if(url.startsWith(currentProtocolHostPort)) {
                    if(url.length() == currentProtocolHostPort.length()) {
                        return newBasePath + '/';
                    } else {
                        return UrlUtils.safeAppend(newBasePath, url.substring(currentProtocolHostPort.length()));
                    }
                } else {
                    String protocolHostPort = UrlUtils.getProtocolHostPort(url);
                    if(protocolHostPort.length() == url.length()) {
                        return newBaseContext + '/' + url + '/';
                    } else {
                        return newBaseContext + '/' + url;
                    }
                }
            } else {
                return url;
            }
        } else {
            // it's not http
            return url;
        }
    }
    
}
