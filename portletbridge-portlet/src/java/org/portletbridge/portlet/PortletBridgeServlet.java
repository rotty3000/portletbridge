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

import java.io.IOException;
import java.net.URI;
import java.text.MessageFormat;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpStatus;
import org.portletbridge.ResourceException;

/**
 * @author jmccrindle
 * @author rickard
 */
public class PortletBridgeServlet extends HttpServlet {

    public static final ResourceBundle resourceBundle = PropertyResourceBundle
            .getBundle("org.portletbridge.portlet.PortletBridgePortlet");

    private PortletBridgeService portletBridgeService = new DefaultPortletBridgeService();

    private HttpClientTemplate httpClientTemplate = new DefaultHttpClientTemplate();

    private String mementoSessionKey;

    /**
     * Initialise the servlet. Will throw a servlet exception if the
     * proxyBrowserSessionKey is not set.
     * 
     * @see javax.servlet.GenericServlet#init()
     */
    public void init() throws ServletException {

        // get proxyBrowserSessionKey
        mementoSessionKey = this.getServletConfig().getInitParameter(
                "mementoSessionKey");
        if (mementoSessionKey == null) {
            throw new ServletException(resourceBundle
                    .getString("error.mementoSessionKey"));
        }
    }

    /**
     * url pattern should be: http://host:port/context/servlet/id
     */
    protected void doGet(HttpServletRequest request,
            final HttpServletResponse response) throws ServletException,
            IOException {
        // get the id
        final String id = portletBridgeService.getIdFromRequestUri(request
                .getRequestURI());
        // look up the data associated with that id from the session
        HttpSession session = request.getSession();
        if (session == null) {
            throw new ServletException(resourceBundle
                    .getString("error.nosession"));
        }
        PortletBridgeMemento memento = (PortletBridgeMemento) session
                .getAttribute(mementoSessionKey);
        if (memento == null) {
            throw new ServletException(resourceBundle
                    .getString("error.nomemento"));
        }
        final BridgeRequest bridgeRequest = memento.getBridgeRequest(id);
        if (bridgeRequest == null) {
            throw new ServletException(resourceBundle
                    .getString("error.nobridgerequest"));
        }
        final PerPortletMemento perPortletMemento = memento
                .getPerPortletMemento(bridgeRequest.getPortletId());
        if (perPortletMemento == null) {
            throw new ServletException(resourceBundle
                    .getString("error.noperportletmemento"));
        }

        // go and fetch the data from the backend as appropriate
        // what i'm thinking here is:
        //  - do i want to expose the httpclient directly at this point instead
        //    of using the service
        //    but it makes testing more difficult...
        //  - i could abstract is a little but still expose httpclient classes
        //    like "method" etc....
        //    maybe that's better
        //  - the momento should have the httpstate in it
        URI url = bridgeRequest.getUrl();

        try {
            httpClientTemplate.doGet(url, perPortletMemento,
                    new HttpClientCallback() {
                        public Object doInHttpClient(URI url, int statusCode,
                                HttpMethodBase method)
                                throws ResourceException, Throwable {
                            if (statusCode == HttpStatus.SC_OK) {
                                // if it's text/html then store it and redirect
                                // back to the
                                // portlet
                                // render view (portletUrl)
                                Header responseHeader = method
                                        .getResponseHeader("Content-Type");
                                if (responseHeader != null
                                        && responseHeader.getValue()
                                                .startsWith("text/html")) {
                                    String content = ResourceUtil.getString(
                                            method.getResponseBodyAsStream(),
                                            method.getResponseCharSet());
                                    // TODO: think about cleaning this up if we don't get back to the render
                                    bridgeRequest
                                            .enqueueContent(new PortletBridgeContent(
                                                    url, "get", content));
                                    // redirect
                                    // TODO: worry about this... adding the id at the end
                                    response.sendRedirect(bridgeRequest
                                            .getPageUrl()
                                            + "&id=" + id);
                                } else {
                                    // if it's anything else then stream it
                                    // back... consider
                                    // stylesheets and
                                    // javascript
                                    // TODO: javascript and css rewriting
                                    response.setContentType(method.getResponseHeader("Content-Type").toExternalForm());
                                    ResourceUtil.copy(method
                                            .getResponseBodyAsStream(),
                                            response.getOutputStream(), 4096);
                                }
                            } else {
                                // if there is a problem with the status code
                                // then return that
                                // error
                                // back
                                response.sendError(statusCode);
                            }
                            return null;
                        }
                    });
        } catch (ResourceException resourceException) {
            String format = MessageFormat.format(resourceBundle
                    .getString(resourceException.getMessage()),
                    resourceException.getArgs());
            throw new ServletException(format, resourceException);
        }

    }

    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {

    }

    public void setPortletBridgeService(
            PortletBridgeService portletBridgeService) {
        this.portletBridgeService = portletBridgeService;
    }
    public void setHttpClientTemplate(HttpClientTemplate httpClientTemplate) {
        this.httpClientTemplate = httpClientTemplate;
    }
}