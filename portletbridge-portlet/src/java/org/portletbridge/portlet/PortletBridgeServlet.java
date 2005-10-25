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
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.Set;

import javax.portlet.RenderResponse;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.portletbridge.ResourceException;

/**
 * @author jmccrindle
 * @author rickard
 */
public class PortletBridgeServlet extends HttpServlet {

    /**
     * default serial version id
     */
    private static final long serialVersionUID = 7841139248662925798L;

    public static final ResourceBundle resourceBundle = PropertyResourceBundle
            .getBundle("org.portletbridge.portlet.PortletBridgePortlet");

    private static final org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory
            .getLog(PortletBridgeServlet.class);

    private PortletBridgeService portletBridgeService = new DefaultPortletBridgeService();

    private HttpClientTemplate httpClientTemplate = new DefaultHttpClientTemplate();

    private String mementoSessionKey;

    private BridgeFunctionsFactory bridgeFunctionsFactory;

    private Set ignoreRequestHeaders;

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

        log.debug("init(): mementoSessionKey=" + mementoSessionKey);

        if (mementoSessionKey == null) {
            throw new ServletException(resourceBundle
                    .getString("error.mementoSessionKey"));
        }

        // TODO: blow up if these aren't set.
        String cssRegex = this.getServletConfig().getInitParameter("cssRegex");
        String javascriptRegex = this.getServletConfig().getInitParameter(
                "jsRegex");

        ContentRewriter javascriptRewriter = new RegexContentRewriter(
                javascriptRegex);
        ContentRewriter cssRewriter = new RegexContentRewriter(cssRegex);

        bridgeFunctionsFactory = new BridgeFunctionsFactory(DefaultIdGenerator
                .getInstance(), javascriptRewriter, cssRewriter);

        // TODO: blow up if these aren't set.
        ignoreRequestHeaders = new HashSet(Arrays.asList(getInitParameter(
                "ignoreRequestHeaders").split(",")));

    }

    /**
     * url pattern should be: http://host:port/context/servlet/id
     */
    protected void doGet(final HttpServletRequest request,
            final HttpServletResponse response) throws ServletException,
            IOException {
        // get the id
        final String id = portletBridgeService.getIdFromRequestUri(request
                .getContextPath(), request.getRequestURI());
        // look up the data associated with that id from the session
        HttpSession session = request.getSession();
        if (session == null) {
            throw new ServletException(resourceBundle
                    .getString("error.nosession")
                    + ", URL=" + request.getRequestURI());
        }
        final PortletBridgeMemento memento = (PortletBridgeMemento) session
                .getAttribute(mementoSessionKey);
        if (memento == null) {
            throw new ServletException(resourceBundle
                    .getString("error.nomemento")
                    + ", URL=" + request.getRequestURI());
        }
        final BridgeRequest bridgeRequest = memento.getBridgeRequest(id);
        if (bridgeRequest == null) {
            throw new ServletException(resourceBundle
                    .getString("error.nobridgerequest")
                    + ", URL=" + request.getRequestURI());
        }
        final PerPortletMemento perPortletMemento = memento
                .getPerPortletMemento(bridgeRequest.getPortletId());
        if (perPortletMemento == null) {
            throw new ServletException(resourceBundle
                    .getString("error.noperportletmemento")
                    + ", URL=" + request.getRequestURI());
        }

        // go and fetch the data from the backend as appropriate
        URI url = bridgeRequest.getUrl();

        if (request.getQueryString() != null
                && request.getQueryString().trim().length() > 0) {
            try {
                // TODO: may have to change encoding
                url = new URI(url.toString() + '?' + request.getQueryString());
            } catch (URISyntaxException e) {
                throw new ServletException(e.getMessage() + ", doGet(): URL="
                        + url + ", id=" + id + ", request URI="
                        + request.getRequestURI(), e);
            }
        }

        log.debug("doGet(): URL=" + url + ", id=" + id + ", request URI="
                + request.getRequestURI());

        fetch(request, response, bridgeRequest, memento, perPortletMemento, url);

    }

    /**
     * @param response
     * @param bridgeRequest
     * @param perPortletMemento
     * @param url
     * @throws ServletException
     */
    protected void fetch(final HttpServletRequest request,
            final HttpServletResponse response,
            final BridgeRequest bridgeRequest,
            final PortletBridgeMemento memento,
            final PerPortletMemento perPortletMemento, final URI url)
            throws ServletException {
        try {
            GetMethod getMethod = new GetMethod(url.toString());
            // TODO: suspect to send the same request headers after a redirect?
            copyRequestHeaders(request, getMethod);
            httpClientTemplate.service(getMethod, perPortletMemento,
                    new HttpClientCallback() {
                        public Object doInHttpClient(int statusCode,
                                HttpMethodBase method)
                                throws ResourceException, Throwable {
                            if (statusCode == HttpStatus.SC_OK) {
                                // if it's text/html then store it and redirect
                                // back to the portlet render view (portletUrl)
                                Header responseHeader = method
                                        .getResponseHeader("Content-Type");
                                if (responseHeader != null
                                        && responseHeader.getValue()
                                                .startsWith("text/html")) {
                                    String content = ResourceUtil.getString(
                                            method.getResponseBodyAsStream(),
                                            method.getResponseCharSet());
                                    // TODO: think about cleaning this up if we
                                    // don't get back to the render
                                    perPortletMemento.enqueueContent(
                                            bridgeRequest.getId(),
                                            new PortletBridgeContent(url,
                                                    "get", content));
                                    // redirect
                                    // TODO: worry about this... adding the id
                                    // at the end
                                    response.sendRedirect(bridgeRequest
                                            .getPageUrl());
                                } else if (responseHeader != null
                                        && responseHeader.getValue()
                                                .startsWith("text/javascript")) {
                                    // rewrite external javascript
                                    String content = ResourceUtil.getString(
                                            method.getResponseBodyAsStream(),
                                            method.getResponseCharSet());
                                    BridgeFunctions bridge = bridgeFunctionsFactory
                                            .createBridgeFunctions(
                                                    memento,
                                                    perPortletMemento,
                                                    getServletName(),
                                                    url,
                                                    new PseudoRenderRequest(request.getContextPath()),
                                                    new PseudoRenderResponse(
                                                            bridgeRequest
                                                                    .getPortletId(),
                                                            bridgeRequest
                                                                    .getPageUrl(),
                                                            bridgeRequest
                                                                    .getId()));
                                    response.setContentType("text/javascript");
                                    PrintWriter writer = response.getWriter();
                                    writer.write(bridge.script(content));
                                    writer.flush();
                                } else if (responseHeader != null
                                        && responseHeader.getValue()
                                                .startsWith("text/css")) {
                                    // rewrite external css
                                    String content = ResourceUtil.getString(
                                            method.getResponseBodyAsStream(),
                                            method.getResponseCharSet());
                                    BridgeFunctions bridge = bridgeFunctionsFactory
                                            .createBridgeFunctions(
                                                    memento,
                                                    perPortletMemento,
                                                    getServletName(),
                                                    url,
                                                    new PseudoRenderRequest(request.getContextPath()),
                                                    new PseudoRenderResponse(
                                                            bridgeRequest
                                                                    .getPortletId(),
                                                            bridgeRequest
                                                                    .getPageUrl(),
                                                            bridgeRequest
                                                                    .getId()));
                                    response.setContentType("text/css");
                                    PrintWriter writer = response.getWriter();
                                    writer.write(bridge.style(content));
                                    writer.flush();
                                } else {
                                    // if it's anything else then stream it
                                    // back... consider stylesheets and
                                    // javascript
                                    // TODO: javascript and css rewriting
                                    Header header = method
                                            .getResponseHeader("Content-Type");
                                    response
                                            .setContentType(((null == header
                                                    .getName() ? "" : header
                                                    .getName())
                                                    + ": " + (null == header
                                                    .getValue() ? "" : header
                                                    .getValue())));

                                    log.trace("fetch(): returning URL=" + url
                                            + ", as stream, content type="
                                            + header);
                                    ResourceUtil.copy(method
                                            .getResponseBodyAsStream(),
                                            response.getOutputStream(), 4096);
                                }
                            } else {
                                // if there is a problem with the status code
                                // then return that error back
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

    protected void doPost(final HttpServletRequest request,
            final HttpServletResponse response) throws ServletException,
            IOException {
        // get the id
        final String id = portletBridgeService.getIdFromRequestUri(request
                .getContextPath(), request.getRequestURI());
        // look up the data associated with that id from the session
        HttpSession session = request.getSession();
        if (session == null) {
            throw new ServletException(resourceBundle
                    .getString("error.nosession"));
        }
        final PortletBridgeMemento memento = (PortletBridgeMemento) session
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
        final URI url = bridgeRequest.getUrl();

        log.debug("doPost(): URL=" + url);

        try {
            PostMethod postMethod = new PostMethod(url.toString());
            copyRequestHeaders(request, postMethod);
            postMethod.setRequestEntity(new InputStreamRequestEntity(request
                    .getInputStream()));
            httpClientTemplate.service(postMethod, perPortletMemento,
                    new HttpClientCallback() {
                        public Object doInHttpClient(int statusCode,
                                HttpMethodBase method)
                                throws ResourceException, Throwable {
                            if (statusCode == HttpStatus.SC_OK) {
                                // if it's text/html then store it and redirect
                                // back to the portlet render view (portletUrl)
                                Header responseHeader = method
                                        .getResponseHeader("Content-Type");
                                if (responseHeader != null
                                        && responseHeader.getValue()
                                                .startsWith("text/html")) {
                                    String content = ResourceUtil.getString(
                                            method.getResponseBodyAsStream(),
                                            method.getResponseCharSet());
                                    // TODO: think about cleaning this up if we
                                    // don't get back to the render
                                    perPortletMemento.enqueueContent(
                                            bridgeRequest.getId(),
                                            new PortletBridgeContent(url,
                                                    "post", content));
                                    // redirect
                                    // TODO: worry about this... adding the id
                                    // at the end

                                    log
                                            .debug("doPost(): doing response.sendRedirect to URL="
                                                    + bridgeRequest
                                                            .getPageUrl());

                                    response.sendRedirect(bridgeRequest
                                            .getPageUrl());
                                } else {
                                    // if it's anything else then stream it
                                    // back... consider stylesheets and
                                    // javascript
                                    // TODO: javascript and css rewriting
                                    response.setContentType(method
                                            .getResponseHeader("Content-Type")
                                            .toExternalForm());
                                    ResourceUtil.copy(method
                                            .getResponseBodyAsStream(),
                                            response.getOutputStream(), 4096);
                                }
                            } else if (statusCode == HttpStatus.SC_MOVED_TEMPORARILY) {
                                fetch(request, response, bridgeRequest,
                                        memento, perPortletMemento, url);
                            } else {
                                // if there is a problem with the status code
                                // then return that error back
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

    public void setPortletBridgeService(
            PortletBridgeService portletBridgeService) {
        this.portletBridgeService = portletBridgeService;
    }

    public void setHttpClientTemplate(HttpClientTemplate httpClientTemplate) {
        this.httpClientTemplate = httpClientTemplate;
    }

    protected void copyRequestHeaders(HttpServletRequest request,
            HttpMethodBase method) {

        Enumeration properties = request.getHeaderNames();
        while (properties.hasMoreElements()) {
            String propertyName = (String) properties.nextElement();
            if (!ignoreRequestHeaders.contains(propertyName.toLowerCase())) {
                Enumeration values = request.getHeaders(propertyName);
                while (values.hasMoreElements()) {
                    String property = (String) values.nextElement();
                    method.setRequestHeader(propertyName, property);
                }
            }
        }

        // Conditional cookie transfer
        try {
            if (method.getURI().getHost().equals(request.getHeader("host"))) {
                String cookie = request.getHeader("cookie");
                if (cookie != null)
                    method.setRequestHeader("cookie", cookie);
            }
        } catch (URIException e) {
            log.warn(e, e);
        }

    }

}
