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
public class DefaultUrlRewriter implements UrlRewriter {
    
    private String currentUrl;
    private String currentProtocolHostPort;
    private String newBaseUrl;

    /**
     * @param baseUrl
     */
    public DefaultUrlRewriter(String currentUrl, String newBaseUrl) {
        this.currentUrl = currentUrl;
        this.newBaseUrl = newBaseUrl;
        this.currentProtocolHostPort = UrlUtils.getProtocolHostPort(currentUrl);
    }

    /* (non-Javadoc)
     * @see org.portletbridge.xsl.LinkRewriter#rewrite(java.lang.String)
     */
    public String rewrite(String url) {
        if(UrlUtils.isRelativeHttp(url)) {
            return url;
        } else if(UrlUtils.isAbsoluteHttp(url)) {
            return UrlUtils.safeAppend(newBaseUrl, url);
        } else if(UrlUtils.isFullHttp(url)) {
            if(currentProtocolHostPort != null) {
                if(url.startsWith(currentProtocolHostPort)) {
                    return UrlUtils.safeAppend(newBaseUrl, url.substring(currentProtocolHostPort.length()));
                } else {
                    return url;
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
