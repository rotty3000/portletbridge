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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.httpclient.URI;
import org.portletbridge.PortletBridgeException;
import org.portletbridge.ResourceException;

/**
 * Gets images / resources etc. for the PortletBridgePortlet.
 * 
 * @author jmccrindle
 */
public class PortletBridgetServlet extends HttpServlet {

    // TODO: consider making this an init param instead
    public static final ResourceBundle resourceBundle = 
        PropertyResourceBundle.getBundle("org.portletbridge.portlet.PortletBridgePortlet");
    public static final String NULL_DELIM = new String(new byte[] {0});
    
    private ResourceTransport resourceTransport = new CommonsProxyTransport();
    private String proxyBrowserSessionKey;

    /**
     * Initialise the servlet. Will throw a servlet exception if the 
     * proxyBrowserSessionKey is not set.
     * 
     * @see javax.servlet.GenericServlet#init()
     */
    public void init() throws ServletException {

        // get proxyBrowserSessionKey
        proxyBrowserSessionKey = this.getServletConfig()
                .getInitParameter("proxyBrowserSessionKey");
        if (proxyBrowserSessionKey == null) {
            throw new ServletException(resourceBundle
                    .getString("error.proxyBrowserSessionKey"));
        }
    }
    
    /**
     * Gets the resource from the downstream site and streams it through to the
     * browser. It expects urls of the form:
     * 
     * http://host:port/context/servlet/id/url or /context/servlet/id/url or
     * http://host:port/servlet/id/url or /servlet/id/url
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String requestURI = request.getRequestURI();
            URI uri = new URI(requestURI, false);
            String path = uri.getPath();
            int length = request.getContextPath().length();
            StringTokenizer st = new StringTokenizer(length > 0 ? path
                    .substring(length) : path, "/");
            String servlet = st.hasMoreElements() ? st.nextToken() : null;
            String id = st.hasMoreElements() ? st.nextToken() : null;
            // hack to get the rest of the string
            String url = st.hasMoreElements() ? st.nextToken(NULL_DELIM) : null;
            HttpSession session = request.getSession();
            if (session == null) {
                throw new PortletBridgeException("error.no.session");
            }
            if (id == null || url == null) {
                throw new PortletBridgeException("error.servlet.bad.url", requestURI);
            }
            url = url.substring(1);
            String portletBrowserInstanceSessionKey = proxyBrowserSessionKey
                    + "." + id;
            ProxyBrowser browser = (ProxyBrowser) session
                    .getAttribute(portletBrowserInstanceSessionKey);
            if (browser == null) {
                throw new PortletBridgeException("error.no.browser");
            }

            HttpResult result = resourceTransport.getResource(browser, url);

            BufferedInputStream in = new BufferedInputStream(
                    result.getContents());
            try {
                response.setContentType(result.getContentType());
                BufferedOutputStream outputStream = new BufferedOutputStream(response
                        .getOutputStream());
                byte[] b = new byte[4096];
                int i = -1;
                while ((i = in.read(b)) != -1) {
                    outputStream.write(b, 0, i);
                }
                outputStream.flush();
            } finally {
                in.close();
            }
            
        } catch (ResourceException e) {
            String format = MessageFormat.format(resourceBundle
                    .getString(e.getMessage()),
                    e.getArgs());
            throw new ServletException(format);
        }
    }
}
