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

import java.io.IOException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author jmccrindle
 */
public class LinkServlet extends HttpServlet {

    public static final ResourceBundle resourceBundle = 
        PropertyResourceBundle.getBundle("org.portletbridge.portlet.PortletBridgePortlet");

    private ResourceTransport resourceTransport = new CommonsProxyTransport();
    private String proxyBrowserSessionKey;

    /**
     * Default Constructor, called by the container 
     */
    public LinkServlet() {

    }
    
    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

//        try {
//
//            PortletSession session = request.getPortletSession();
//            PortletPreferences preferences = request.getPreferences();
//
//            PortletBridgeMemento browser = (PortletBridgeMemento) session
//                    .getAttribute(portletBrowserInstanceSessionKey,
//                            PortletSession.APPLICATION_SCOPE);
//
//            HttpResult result = null;
//
//            if (browser == null) {
//                throw new ResourceException("error.no.browser");
//            } else {
//                String url = request.getParameter("actionurl");
//                if (url == null || url.trim().length() == 0) {
//                    throw new ResourceException("error.missing.url");
//                }
//                config.getPortletContext().log(url);
//                browser.setPreferences(preferences);
//                // TODO: handle file uploads
//                String method = request.getParameter("__method");
//                if ("post".equalsIgnoreCase(method)) {
//                    String contentType = request.getContentType();
//                    if ("application/x-www-form-urlencoded"
//                            .equalsIgnoreCase(contentType)) {
//                        proxyTransport.post(browser, url, request
//                                .getParameterMap());
//                    } else {
//                        throw new PortletException(
//                                "can't handle contentType of " + contentType
//                                        + " yet");
//                    }
//                } else {
//                    result = proxyTransport.get(browser, url);
//                }
//                // TODO: handle not text/html results
//            }
//
//        } catch (ResourceException resourceException) {
//            String format = MessageFormat.format(resourceBundle
//                    .getString(resourceException.getMessage()),
//                    resourceException.getArgs());
//            throw new PortletException(format, resourceException.getCause());
//        }
    }
    
    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // TODO Auto-generated method stub
        super.doPost(request, response);
    }

}
