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
package org.portletbridge.mock;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

import javax.portlet.PortletPreferences;

import org.portletbridge.PortletBridgeException;
import org.portletbridge.portlet.CommonsProxyBrowser;
import org.portletbridge.portlet.HttpResult;
import org.portletbridge.portlet.ProxyBrowser;
import org.portletbridge.portlet.ProxyTransport;

/**
 * @author jmccrindle
 */
public class MockProxyTransport implements ProxyTransport {

    private String contents;

    /**
     * 
     */
    public MockProxyTransport() {
        super();
    }

    /* (non-Javadoc)
     * @see org.portletbridge.portlet.ProxyTransport#createProxyBrowser()
     */
    public ProxyBrowser createProxyBrowser() {
        return new CommonsProxyBrowser();
    }

    /* (non-Javadoc)
     * @see org.portletbridge.portlet.ProxyTransport#get(org.portletbridge.portlet.ProxyBrowser, java.lang.String)
     */
    public HttpResult get(ProxyBrowser proxyBrowser, final String url) throws PortletBridgeException {
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(contents.getBytes());
        return new HttpResult() {
            public String getContentType() {
                return "text/html";
            }

            public InputStream getContents() {
                return inputStream;
            }

            public String getUrl() {
                return url;
            }
        };
    }

    /* (non-Javadoc)
     * @see org.portletbridge.portlet.ProxyTransport#post(org.portletbridge.portlet.ProxyBrowser, java.lang.String, java.util.Map)
     */
    public HttpResult post(ProxyBrowser proxyBrowser, String url, Map parameters) throws PortletBridgeException {
        // TODO Auto-generated method stub
        return null;
    }
    
    public void setupContents(String contents) {
        this.contents = contents;
    }

    /* (non-Javadoc)
     * @see org.portletbridge.portlet.ProxyTransport#setPreferences(javax.portlet.PortletPreferences)
     */
    public void setPreferences(PortletPreferences preferences) {
        // TODO Auto-generated method stub
        
    }

}
