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
package org.portletbridge.web;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NTCredentials;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.Serializer;
import org.apache.xml.serialize.SerializerFactory;
import org.portletbridge.rewriter.FullUrlRewriter;
import org.portletbridge.rewriter.RegExStyleSheetRewriter;
import org.portletbridge.xsl.LinkRewriterXmlFilter;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * @author JMcCrindle
 */
public class ProxyHttpServlet extends HttpServlet {

    /**
     * default serial version id 
     */
    private static final long serialVersionUID = 7594239856795649323L;

    private static final org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory
            .getLog(ProxyHttpServlet.class);

    private HttpClient client = null;

    private String httpStateSessionKey = ProxyHttpServlet.class + ".httpStateSessionKey";

    private XMLReader parser = null;

    private String configProxyEnabled = null;

    private String configProxyHost = null;

    private String configProxyPort = null;

    private String configProxyAuthentication = null;

    private String configProxyAuthenticationUsername = null;

    private String configProxyAuthenticationPassword = null;

    private String configProxyAuthenticationHost = null;

    private String configProxyAuthenticationDomain = null;

    private String configConnectionTimeout = null;

    private String configConnectionMaxTotal = null;

    private String configConnectionMaxPerHost = null;

    private static Set BLOCK_REQUEST_HEADERS = new HashSet();

    private static Set BLOCK_RESPONSE_HEADERS = new HashSet();

    static {
        BLOCK_REQUEST_HEADERS.add("cookie");
        BLOCK_REQUEST_HEADERS.add("accept-encoding");
        BLOCK_REQUEST_HEADERS.add("connection");
        BLOCK_REQUEST_HEADERS.add("keep-alive");
        BLOCK_RESPONSE_HEADERS.add("connection");
        BLOCK_RESPONSE_HEADERS.add("proxy-connection");
        BLOCK_RESPONSE_HEADERS.add("content-length");
        BLOCK_RESPONSE_HEADERS.add("location");
        BLOCK_RESPONSE_HEADERS.add("set-cookie");
        BLOCK_RESPONSE_HEADERS.add("content-encoding");
        BLOCK_RESPONSE_HEADERS.add("transfer-encoding");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.GenericServlet#init()
     */
    public void init() throws ServletException {
        try {
            initProperties();
        } catch (IllegalAccessException e1) {
            throw new ServletException(e1);
        } catch (InvocationTargetException e1) {
            throw new ServletException(e1);
        }
        MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
        HttpConnectionManagerParams httpConnectionManagerParams = new HttpConnectionManagerParams();
        if (configConnectionTimeout != null) {
            httpConnectionManagerParams.setConnectionTimeout(Integer
                    .parseInt(configConnectionTimeout));
        }
        if (configConnectionMaxTotal != null) {
            httpConnectionManagerParams.setMaxTotalConnections(Integer
                    .parseInt(configConnectionMaxTotal));
        }
        connectionManager.setParams(httpConnectionManagerParams);
        client = new HttpClient(connectionManager);
        if (configConnectionMaxPerHost != null) {
            httpConnectionManagerParams.setMaxConnectionsPerHost(client
                    .getHostConfiguration(), Integer
                    .parseInt(configConnectionMaxPerHost));
        }
        if (configProxyEnabled != null
                && Boolean.valueOf(configProxyEnabled).booleanValue()) {
            client.getHostConfiguration().setProxy(configProxyHost,
                    Integer.parseInt(configProxyPort));
        }
        try {
            parser = XMLReaderFactory
                    .createXMLReader("org.cyberneko.html.parsers.SAXParser");
            parser.setFeature(
                    "http://cyberneko.org/html/features/balance-tags", true);
        } catch (SAXNotRecognizedException e) {
            throw new ServletException(e);
        } catch (SAXNotSupportedException e) {
            throw new ServletException(e);
        } catch (SAXException e) {
            throw new ServletException(e);
        }
    }

    /**
     * noop
     * 
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    protected void initProperties() throws IllegalAccessException,
            InvocationTargetException {
        BeanMap map = new BeanMap(this);
        String servletName = getServletConfig().getServletName();
        log.debug(servletName);
        for (Iterator iter = map.keyIterator(); iter.hasNext();) {
            String property = (String) iter.next();
            String initParameter = getServletContext().getInitParameter(
                    servletName + "." + property);
            if (initParameter != null) {
                log.debug(property + "=" + initParameter);
                BeanUtils.setProperty(this, property, initParameter);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        String queryString = request.getQueryString();
        // TODO: worry about the context path being 0 length
        // TODO: code duplication
        String url = request.getRequestURI().substring(
                request.getContextPath().length() + 1)
                + (queryString != null ? '?' + queryString : "");
        log.debug(url);
        GetMethod getMethod = new GetMethod(url);
        process(request, response, getMethod);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        String queryString = request.getQueryString();
        String url = request.getRequestURI().substring(
                request.getContextPath().length() + 1)
                + (queryString != null ? '?' + queryString : "");
        log.debug(url);
        PostMethod postMethod = new PostMethod(url);
        process(request, response, postMethod);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServlet#doHead(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    protected void doHead(HttpServletRequest arg0, HttpServletResponse arg1)
            throws ServletException, IOException {
        throw new ServletException("HEAD Unsupported");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    protected void process(HttpServletRequest request,
            HttpServletResponse response, HttpMethodBase method)
            throws ServletException, IOException {
        
        long startTime = System.currentTimeMillis();

        String methodUrl = method
            .getURI().toString();

        try {
            HttpSession session = request.getSession(true);
            int statusCode = 0;
            FullUrlRewriter fullUrlRewriter = new FullUrlRewriter(methodUrl, request.getRequestURI());
            RegExStyleSheetRewriter styleSheetRewriter = new RegExStyleSheetRewriter(
                    fullUrlRewriter);

            method.setFollowRedirects(false);
            Enumeration headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = (String) headerNames.nextElement();
                if (!BLOCK_REQUEST_HEADERS.contains(headerName.toLowerCase())) {
                    String headerValue = request.getHeader(headerName);
                    log.debug("RequestHeader:" + headerName + "=" + headerValue);
                    method.setRequestHeader(headerName, headerValue);
                }
            }

            HttpState state = null;
            
            synchronized (session) {
                state = (HttpState) session
                        .getAttribute(httpStateSessionKey);
                if (state == null) {
                    state = createState();
                    session.setAttribute(httpStateSessionKey, state);
                }
            }

            statusCode = client.executeMethod(
                    client.getHostConfiguration(), method, state);
            response.setStatus(statusCode);

            Header[] responseHeaders = method.getResponseHeaders();
            for (int i = 0; i < responseHeaders.length; i++) {
                Header header = responseHeaders[i];
                String headerName = header.getName();
                if (!BLOCK_RESPONSE_HEADERS.contains(headerName
                        .toLowerCase())) {
                    String headerValue = header.getValue();
                    log.debug("ResponseHeader:" + headerName + "="
                            + headerValue);
                    response.setHeader(headerName, headerValue);
                }
            }

            if ((int) Math.floor(statusCode / 100) == 3) {
                Header responseHeader = method.getResponseHeader("Location");
                if (responseHeader != null) {
                    response.setHeader("Location", fullUrlRewriter
                            .rewrite(responseHeader.getValue()));
                }
            } else {
                Header responseHeader = method
                        .getResponseHeader("Content-Type");
                String contentType = responseHeader.getValue();
                if (contentType.indexOf("text/html") != -1) {
                    PrintWriter responseWriter = response.getWriter();
                    InputStream responseBodyAsStream = method
                            .getResponseBodyAsStream();
                    SerializerFactory factory = SerializerFactory
                            .getSerializerFactory("html");
                    Serializer writer = factory
                            .makeSerializer(new OutputFormat());
                    writer.setOutputCharStream(responseWriter);
                    LinkRewriterXmlFilter filter = new LinkRewriterXmlFilter();
                    filter.setParent(parser);
                    filter.setContentHandler(writer.asContentHandler());
                    log.debug(request.getRequestURI());
                    filter.setUrlRewriter(fullUrlRewriter);
                    filter.setStyleSheetRewriter(styleSheetRewriter);

                    try {
                        InputSource inputSource = new InputSource(
                                responseBodyAsStream);
                        filter.parse(inputSource);
                    } catch (SAXException e) {
                        throw new ServletException(e);
                    } catch (IOException e) {
                        throw new ServletException(e);
                    } finally {
                        responseWriter.flush();
                        responseBodyAsStream.close();
                    }
                } else if (contentType.indexOf("text/css") != -1) {
                    String responseBodyAsString = method
                            .getResponseBodyAsString();
                    String rewrittenCss = styleSheetRewriter
                            .rewrite(responseBodyAsString);
                    response.getWriter().write(rewrittenCss);
                } else {
                    BufferedOutputStream outputStream = new BufferedOutputStream(response
                            .getOutputStream());
                    InputStream responseBodyAsStream = method
                            .getResponseBodyAsStream();
                    BufferedInputStream in = new BufferedInputStream(
                            responseBodyAsStream);
                    byte[] b = new byte[4096];
                    int i = -1;
                    while ((i = in.read(b)) != -1) {
                        outputStream.write(b, 0, i);
                    }
                    outputStream.flush();
                    responseBodyAsStream.close();
                }
            }
        } catch (Throwable t) {
            log.warn(t, t);
        } finally {
            method.releaseConnection();
        }
        
        log.info(methodUrl + " finished in " + (System.currentTimeMillis() - startTime) + "ms");
    }

    /**
     * @return
     * @throws ServletException
     */
    protected HttpState createState() throws ServletException {
        HttpState state;
        state = new HttpState();
        if (configProxyEnabled != null
                && Boolean.valueOf(configProxyEnabled)
                        .booleanValue()) {
            if (configProxyAuthentication != null) {
                if ("ntlm"
                        .equalsIgnoreCase(configProxyAuthentication)) {
                    state
                            .setProxyCredentials(
                                    AuthScope.ANY,
                                    new NTCredentials(
                                            configProxyAuthenticationUsername,
                                            configProxyAuthenticationPassword,
                                            configProxyAuthenticationHost,
                                            configProxyAuthenticationDomain));
                } else if ("basic"
                        .equalsIgnoreCase(configProxyAuthentication) || "digest"
                            .equalsIgnoreCase(configProxyAuthentication)) {
                    state
                            .setProxyCredentials(
                                    AuthScope.ANY,
                                    new UsernamePasswordCredentials(
                                            configProxyAuthenticationUsername,
                                            configProxyAuthenticationPassword));
                } else {
                    throw new ServletException(
                            "configProxyAuthentication can only be either 'basic' or 'ntlm'");
                }
            } else {
                throw new ServletException(
                        "If configProxyEnabled is try, configProxyAuthentication must be set");
            }
        }
        return state;
    }

    public void setConfigConnectionMaxPerHost(String configConnectionMaxPerHost) {
        this.configConnectionMaxPerHost = configConnectionMaxPerHost;
    }

    public void setConfigConnectionMaxTotal(String configConnectionMaxTotal) {
        this.configConnectionMaxTotal = configConnectionMaxTotal;
    }

    public void setConfigConnectionTimeout(String configConnectionTimeout) {
        this.configConnectionTimeout = configConnectionTimeout;
    }

    public void setConfigProxyAuthentication(String configProxyAuthentication) {
        this.configProxyAuthentication = configProxyAuthentication;
    }

    public void setConfigProxyAuthenticationHost(
            String configProxyAuthenticationHost) {
        this.configProxyAuthenticationHost = configProxyAuthenticationHost;
    }

    public void setConfigProxyAuthenticationPassword(
            String configProxyAuthenticationPassword) {
        this.configProxyAuthenticationPassword = configProxyAuthenticationPassword;
    }

    public void setConfigProxyAuthenticationUsername(
            String configProxyAuthenticationUsername) {
        this.configProxyAuthenticationUsername = configProxyAuthenticationUsername;
    }

    public void setConfigProxyEnabled(String configProxyEnabled) {
        this.configProxyEnabled = configProxyEnabled;
    }

    public void setConfigProxyHost(String configProxyHost) {
        this.configProxyHost = configProxyHost;
    }

    public void setConfigProxyPort(String configProxyPort) {
        this.configProxyPort = configProxyPort;
    }

    public void setHttpStateSessionKey(String httpStateSessionKey) {
        this.httpStateSessionKey = httpStateSessionKey;
    }

    public String getConfigProxyAuthenticationDomain() {
        return configProxyAuthenticationDomain;
    }

    public void setConfigProxyAuthenticationDomain(
            String configProxyAuthenticationDomain) {
        this.configProxyAuthenticationDomain = configProxyAuthenticationDomain;
    }

    public String getConfigConnectionMaxPerHost() {
        return configConnectionMaxPerHost;
    }

    public String getConfigConnectionMaxTotal() {
        return configConnectionMaxTotal;
    }

    public String getConfigConnectionTimeout() {
        return configConnectionTimeout;
    }

    public String getConfigProxyAuthentication() {
        return configProxyAuthentication;
    }

    public String getConfigProxyAuthenticationHost() {
        return configProxyAuthenticationHost;
    }

    public String getConfigProxyAuthenticationPassword() {
        return configProxyAuthenticationPassword;
    }

    public String getConfigProxyAuthenticationUsername() {
        return configProxyAuthenticationUsername;
    }

    public String getConfigProxyEnabled() {
        return configProxyEnabled;
    }

    public String getConfigProxyHost() {
        return configProxyHost;
    }

    public String getConfigProxyPort() {
        return configProxyPort;
    }

    public String getHttpStateSessionKey() {
        return httpStateSessionKey;
    }
}