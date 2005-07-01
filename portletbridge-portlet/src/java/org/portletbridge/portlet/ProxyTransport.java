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
package org.portletbridge.portlet;

import java.util.Map;

import org.portletbridge.PortletBridgeException;

/**
 * The transport mechanism to make calls to downstream sites.
 * @author jmccrindle
 */
public interface ProxyTransport {
    /**
     * @return create a proxy browser that will work with this transport.
     */
    ProxyBrowser createProxyBrowser();
    
    /**
     * Get data from a downstream site and update the proxyBrowser currentPage
     * if the result is text/html
     * 
     * @param proxyBrowser a proxybrowser, must have been created with 
     *          a compatible "createProxyBrowser".
     * @param url the url to get
     * @return an httpresult with the response returned from the url
     * @throws PortletBridgeException if there was a problem calling the downstream site.
     */
    HttpResult get(ProxyBrowser proxyBrowser, String url) throws PortletBridgeException;

    /**
     * Post data to a downstream site and update the proxyBrowser currentPage
     * if the result is text/html
     * 
     * @param proxyBrowser a proxybrowser, must have been created with 
     *          a compatible "createProxyBrowser".
     * @param url the url to get
     * @param parameters the post parameters to use
     * @return an httpresult with the response returned from the url
     * @throws PortletBridgeException if there was a problem calling the downstream site.
     */
    HttpResult post(ProxyBrowser proxyBrowser, String url, Map parameters) throws PortletBridgeException;
}
