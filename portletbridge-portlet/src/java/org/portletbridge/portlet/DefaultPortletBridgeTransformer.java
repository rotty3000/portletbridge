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
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.portlet.PortletPreferences;
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
import org.xml.sax.XMLReader;

/**
 * @author JMcCrindle
 */
public class DefaultPortletBridgeTransformer implements
        PortletBridgeTransformer {

    private Map templatesCache = Collections.synchronizedMap(new HashMap());
    private XMLReader parser;
    private String servletName;

    /**
     * 
     */
    public DefaultPortletBridgeTransformer(XMLReader parser, String servletName) {
        this.parser = parser;
        this.servletName = servletName;
    }

    /**
     * Transforms the HTML from a downstream site using a configured XSL
     * stylesheet.
     * 
     * @param request
     *            the render request
     * @param response
     *            the render response
     * @param in
     *            the http result from calling the downstream site.
     * @throws ResourceException
     *             if there was a problem doing the transform (e.g. if the
     *             stylesheet throws an error).
     */
    public void transform(PortletBridgeMemento memento, PerPortletMemento perPortletMemento, URI currentUrl,
            RenderRequest request, RenderResponse response,
            Reader in) throws ResourceException {
        try {
            PortletPreferences preferences = request.getPreferences();
            String stylesheet = preferences.getValue("stylesheet", null);
            Templates templates = (Templates) templatesCache.get(stylesheet);
            if (templates == null) {
                templates = getTemplates(stylesheet);
            }
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
            context.put("portlet", new PortletFunctions(memento, perPortletMemento, servletName,
                    currentUrl, request, response));
            filter.setContext(context);
            filter.setParent(parser);
            filter.setContentHandler(writer.asContentHandler());
            InputSource inputSource = new InputSource(in);
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
     * Creates compiled templates for a particular stylesheet for performance.
     * 
     * @param stylesheet
     *            the stylesheet to compile
     * @return @throws
     *         ResourceException if the stylesheet could not be found.
     * @throws TransformerFactoryConfigurationError
     *             if there was a problem finding a suitable transformer
     *             factory.
     */
    protected Templates getTemplates(String stylesheet)
            throws ResourceException, TransformerFactoryConfigurationError {
        if (stylesheet == null) {
            throw new ResourceException("error.stylesheet");
        }
        Templates result = null;
        TransformerFactory factory = TransformerFactory.newInstance();
        try {
            URL resourceUrl = null;
            if (stylesheet.startsWith("classpath:")) {
                String substring = stylesheet.substring(10);
                resourceUrl= this.getClass().getResource(
                        substring);
            } else {
                resourceUrl = new URL(stylesheet);
            }
            if (resourceUrl == null) {
                throw new ResourceException("error.stylesheet.notfound",
                        stylesheet);
            }
            result = factory.newTemplates(new StreamSource(resourceUrl.toExternalForm()));
        } catch (TransformerConfigurationException e) {
            throw new ResourceException("error.transformer", e.getMessage(), e);
        } catch (MalformedURLException e) {
            throw new ResourceException("error.stylesheet.url", e.getMessage(),
                    e);
        }
        return result;
    }

}
