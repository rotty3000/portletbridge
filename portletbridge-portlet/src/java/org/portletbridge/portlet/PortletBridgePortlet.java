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
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.Portlet;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamSource;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.Serializer;
import org.apache.xml.serialize.SerializerFactory;
import org.portletbridge.ResourceException;
import org.portletbridge.xsl.XslFilter;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * The PortletBridge portlet is designed to call downstream sites over HTTP
 * and rewrite the content so that it can be displayed in a browser.
 * 
 * @author jmccrindle
 */
public class PortletBridgePortlet implements Portlet {

    private IdGenerator idGenerator = new DefaultIdGenerator();

    private String instanceId = null;

    private PortletConfig config;

    private String proxyBrowserSessionKey;

    private String parserClassName;

    private String servletName;

    private XMLReader parser;

    private ProxyTransport proxyTransport = new CommonsProxyTransport();

    private SynchronizedBoolean firstTimeThrough = new SynchronizedBoolean(true);

    private Templates templates;
    
    private boolean encodeUrls = false;

    private String currentStylesheet;

    private String portletBrowserInstanceSessionKey;

    public void setProxyTransport(ProxyTransport proxyBrowserFactory) {
        this.proxyTransport = proxyBrowserFactory;
    }

    /**
     * Default Constructor. Called by the portlet container.
     */
    public PortletBridgePortlet() {
        super();
    }

    /**
     * Initialise the portlet. Will through a portlet exception if any of 
     * the configuration is missing.
     * 
     * Errors can be internationalized using the 
     * org.portletbridge.portlet.PortletBridgePortlet resource bundle.
     * 
     * @see javax.portlet.Portlet#init(javax.portlet.PortletConfig)
     */
    public void init(PortletConfig config) throws PortletException {

        ResourceBundle resourceBundle = 
            PropertyResourceBundle.getBundle("org.portletbridge.portlet.PortletBridgePortlet");
        
        this.config = config;
        this.instanceId = idGenerator.nextId();

        // get proxyBrowserSessionKey
        proxyBrowserSessionKey = this.config
                .getInitParameter("proxyBrowserSessionKey");
        if (proxyBrowserSessionKey == null) {
            throw new PortletException(resourceBundle
                    .getString("error.proxyBrowserSessionKey"));
        }
        // get encodeUrls
        String encodeUrlsInitParam = this.config
                .getInitParameter("encodeUrls");
        if (encodeUrlsInitParam != null) {
            encodeUrls = encodeUrlsInitParam.trim().equalsIgnoreCase("true");
        }
        // get the servlet name
        servletName = this.config.getInitParameter("servletName");
        if (servletName == null) {
            throw new PortletException(resourceBundle
                    .getString("error.servletName"));
        }
        // get parserClassName
        parserClassName = this.config.getInitParameter("parserClassName");
        if (parserClassName == null) {
            throw new PortletException(resourceBundle
                    .getString("error.parserClassName"));
        }
        // setup parser
        try {
            parser = XMLReaderFactory.createXMLReader(parserClassName);
        } catch (SAXNotRecognizedException e) {
            throw new PortletException(e);
        } catch (SAXNotSupportedException e) {
            throw new PortletException(e);
        } catch (SAXException e) {
            throw new PortletException(e);
        }

        portletBrowserInstanceSessionKey = proxyBrowserSessionKey + "."
                + instanceId;

    }

    /**
     * Called when any link is clicked on the portlet bridge portlet.
     * Will use a POST method downstream if a "__method" is present and
     * has a value of "post". Will only post parameters if the enctype of the
     * post is application/x-www-form-urlencoded.
     * 
     * @see javax.portlet.Portlet#processAction(javax.portlet.ActionRequest,
     *      javax.portlet.ActionResponse)
     */
    public void processAction(ActionRequest request, ActionResponse response)
            throws PortletException, IOException {
        ResourceBundle resourceBundle = config.getResourceBundle(request
                .getLocale());

        try {

            PortletSession session = request.getPortletSession();
            PortletPreferences preferences = request.getPreferences();

            ProxyBrowser browser = (ProxyBrowser) session
                    .getAttribute(portletBrowserInstanceSessionKey,
                            PortletSession.APPLICATION_SCOPE);

            HttpResult result = null;

            if (browser == null) {
                throw new ResourceException("error.no.browser");
            } else {
                String url = request.getParameter("actionurl");
                if (url == null || url.trim().length() == 0) {
                    throw new ResourceException("error.missing.url");
                }
                config.getPortletContext().log(url);
                browser.setPreferences(preferences);
                // TODO: handle file uploads
                String method = request.getParameter("__method");
                if ("post".equalsIgnoreCase(method)) {
                    String contentType = request.getContentType();
                    if ("application/x-www-form-urlencoded"
                            .equalsIgnoreCase(contentType)) {
                        proxyTransport.post(browser, url, request
                                .getParameterMap());
                    } else {
                        throw new PortletException(
                                "can't handle contentType of " + contentType
                                        + " yet");
                    }
                } else {
                    result = proxyTransport.get(browser, url);
                }
                // TODO: handle not text/html results
            }

        } catch (ResourceException resourceException) {
            String format = MessageFormat.format(resourceBundle
                    .getString(resourceException.getMessage()),
                    resourceException.getArgs());
            throw new PortletException(format, resourceException.getCause());
        }
    }

    /**
     * Renders the current page from the downstream site. The current
     * page is stored in the portlet session in Application Scope. Uses
     * an xsl stylesheet to transform the incoming HTML which should
     * rewrite all the urls to go through the processAction method of
     * this portlet.
     * 
     * @see javax.portlet.Portlet#render(javax.portlet.RenderRequest,
     *      javax.portlet.RenderResponse)
     */
    public void render(RenderRequest request, RenderResponse response)
            throws PortletException, IOException {

        ResourceBundle resourceBundle = config.getResourceBundle(request
                .getLocale());

        response.setContentType("text/html");

        try {

            PortletSession session = request.getPortletSession();
            PortletPreferences preferences = request.getPreferences();

            synchronized (firstTimeThrough) {
                // is this the first time that we've been called?
                // DONE: what happens when this preference is changed...
                String stylesheet = preferences.getValue("stylesheet", null);
                if (firstTimeThrough.get()
                        || (stylesheet != null && !stylesheet
                                .equals(currentStylesheet))) {
                    templates = getTemplates(stylesheet);
                    // set first time through to be false
                    firstTimeThrough.set(false);
                    currentStylesheet = stylesheet;
                }
            }

            ProxyBrowser browser = (ProxyBrowser) session.getAttribute(
                    portletBrowserInstanceSessionKey,
                    PortletSession.APPLICATION_SCOPE);

            HttpResult result = null;

            if (browser == null) {
                browser = proxyTransport.createProxyBrowser();
                String url = preferences.getValue("initUrl", null);
                if (url == null || url.trim().length() == 0) {
                    throw new ResourceException("error.initUrl");
                }
                browser.setPreferences(preferences);
                result = proxyTransport.get(browser, preferences.getValue(
                        "initUrl", null));
                session.setAttribute(portletBrowserInstanceSessionKey, browser,
                        PortletSession.APPLICATION_SCOPE);
            } else {
                browser.setPreferences(preferences);
                result = browser.getCurrentPage();
            }

            transform(request, response, result);

        } catch (ResourceException resourceException) {
            String format = MessageFormat.format(resourceBundle
                    .getString(resourceException.getMessage()),
                    resourceException.getArgs());
            response.getWriter().println(format);
            config.getPortletContext().log(format, resourceException);
        }
    }

    /**
     * Transforms the HTML from a downstream site using a configured
     * XSL stylesheet.
     * 
     * @param request the render request
     * @param response the render response
     * @param in the http result from calling the downstream site.
     * @throws ResourceException if there was a problem doing the 
     *         transform (e.g. if the stylesheet throws an error).
     */
    protected void transform(RenderRequest request, RenderResponse response,
            HttpResult in) throws ResourceException {
        try {
            SerializerFactory factory = SerializerFactory
                    .getSerializerFactory("html");
            OutputFormat outputFormat = new OutputFormat();
            outputFormat.setPreserveSpace(true);
            outputFormat.setOmitDocumentType(true);
            outputFormat.setOmitXMLDeclaration(true);
            Serializer writer = factory.makeSerializer(outputFormat);
            PrintWriter responseWriter = response.getWriter();
            writer.setOutputCharStream(responseWriter);
            XslFilter filter = new XslFilter(templates);
            Map context = new HashMap();
            context.put("request", request);
            context.put("response", response);
            context.put("portletrewriter", new PortletUrlRewriter(in.getUrl(), encodeUrls, 
                    response));
            context.put("servletrewriter", new ServletUrlRewriter(request.getContextPath(), 
                    servletName, 
                    instanceId, 
                    in.getUrl()));
            filter.setContext(context);
            filter.setParent(parser);
            filter.setContentHandler(writer.asContentHandler());
            InputSource inputSource = new InputSource(in.getContents());
            filter.parse(inputSource);
        } catch (TransformerConfigurationException e) {
            throw new ResourceException("error.transformer", e
                    .getLocalizedMessage(), e);
        } catch (SAXException e) {
            throw new ResourceException("error.filter.sax", e
                    .getLocalizedMessage(), e);
        } catch (IOException e) {
            throw new ResourceException("error.filter.io", e
                    .getLocalizedMessage(), e);
        }
    }

    /**
     * Creates compiled templates for a particular stylesheet
     * for performance.
     * 
     * @param stylesheet the stylesheet to compile
     * @return @throws
     *         ResourceException if the stylesheet could not be found.
     * @throws TransformerFactoryConfigurationError if there was
     *         a problem finding a suitable transformer factory.
     */
    protected Templates getTemplates(String stylesheet)
            throws ResourceException, TransformerFactoryConfigurationError {
        if (stylesheet == null) {
            throw new ResourceException("error.stylesheet");
        }
        Templates result = null;
        TransformerFactory factory = TransformerFactory.newInstance();
        try {
            InputStream resourceAsStream = null;
            if (stylesheet.startsWith("classpath:")) {
                String substring = stylesheet.substring(10);
                resourceAsStream = this.getClass().getResourceAsStream(
                        substring);
            } else {
                URL url = new URL(stylesheet);
                resourceAsStream = url.openStream();
            }
            if (resourceAsStream == null) {
                throw new ResourceException("error.stylesheet.notfound",
                        stylesheet);
            }
            result = factory.newTemplates(new StreamSource(resourceAsStream));
        } catch (TransformerConfigurationException e) {
            throw new ResourceException("error.transformer", e.getMessage(), e);
        } catch (MalformedURLException e) {
            throw new ResourceException("error.stylesheet.url", e.getMessage(), e);
        } catch (IOException e) {
            throw new ResourceException("error.stylesheet.url", e.getMessage(), e);
        }
        return result;
    }

    /**
     * @see javax.portlet.Portlet#destroy()
     */
    public void destroy() {

    }

}