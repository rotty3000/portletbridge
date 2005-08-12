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
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.text.MessageFormat;
import java.util.Locale;
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

import org.apache.commons.httpclient.HttpMethodBase;
import org.portletbridge.ResourceException;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * @author JMcCrindle
 */
public class PortletBridgePortlet implements Portlet {

    private PortletConfig config = null;

    private String mementoSessionKey = null;

    private HttpClientTemplate httpClientTemplate = new DefaultHttpClientTemplate();
    
    private PortletBridgeTransformer transformer = null;

    public PortletBridgePortlet() {
        super();
    }

    public void init(PortletConfig config) throws PortletException {
        this.config = config;
        ResourceBundle resourceBundle = config.getResourceBundle(Locale.getDefault());
        mementoSessionKey = this.config.getInitParameter("mementoSessionKey");
        if (mementoSessionKey == null) {
            throw new PortletException(resourceBundle
                    .getString("error.mementoSessionKey"));
        }
        // get the servlet name
        String servletName = this.config.getInitParameter("servletName");
        if (servletName == null) {
            throw new PortletException(resourceBundle
                    .getString("error.servletName"));
        }
        // get parserClassName
        String parserClassName = this.config.getInitParameter("parserClassName");
        if (parserClassName == null) {
            throw new PortletException(resourceBundle
                    .getString("error.parserClassName"));
        }
        // setup parser
        try {
            XMLReader parser = XMLReaderFactory.createXMLReader(parserClassName);
            transformer = new DefaultPortletBridgeTransformer(parser, servletName);
        } catch (SAXNotRecognizedException e) {
            throw new PortletException(e);
        } catch (SAXNotSupportedException e) {
            throw new PortletException(e);
        } catch (SAXException e) {
            throw new PortletException(e);
        }
        
        
    }

    public void processAction(ActionRequest request, ActionResponse response)
            throws PortletException, IOException {
        // noop
    }

    public void render(final RenderRequest request,
            final RenderResponse response) throws PortletException, IOException {
        // TODO Auto-generated method stub
        ResourceBundle resourceBundle = config.getResourceBundle(request
                .getLocale());
        
        response.setContentType("text/html");
        
        try {
            PortletSession session = request.getPortletSession();
            PortletPreferences preferences = request.getPreferences();
            String portletId = response.getNamespace();
            PortletBridgeMemento tempMemento = (PortletBridgeMemento) session
                    .getAttribute(mementoSessionKey,
                            PortletSession.APPLICATION_SCOPE);
            if (tempMemento == null) {
                tempMemento = new DefaultPortletBridgeMemento();
                session.setAttribute(mementoSessionKey, tempMemento);
            }
            final PortletBridgeMemento memento = tempMemento;
            final PerPortletMemento perPortletMemento = memento
                    .getPerPortletMemento(portletId);
            perPortletMemento.setPreferences(preferences);
            String urlId = request.getParameter("id");

            URI currentUrl = null;
            Reader in = null;

            if (urlId == null) {
                // this is the default start page for the portlet so go and
                // fetch it
                httpClientTemplate.doGet(perPortletMemento.getInitUrl(), perPortletMemento,
                        new HttpClientCallback() {
                            public Object doInHttpClient(URI url, int statusCode,
                                    HttpMethodBase method) throws Throwable {
                                transformer.transform(memento, perPortletMemento, url, 
                                        request, response,
                                        new InputStreamReader(method
                                                .getResponseBodyAsStream(),
                                                method.getResponseCharSet()));
                                return null;
                            }
                        });
            } else {
                // render or rerender
                BridgeRequest bridgeRequest = memento
                        .getBridgeRequest(urlId);
                if (bridgeRequest == null) {
                    // TODO: throw exception
                }
                PortletBridgeContent content = bridgeRequest.dequeueContent();
                if (content == null) {
                    // we're rerending
                    httpClientTemplate.doGet(bridgeRequest.getUrl(),
                            perPortletMemento, new HttpClientCallback() {
                                public Object doInHttpClient(URI url, int statusCode,
                                        HttpMethodBase method) throws Throwable {
                                    transformer.transform(
                                            memento,
                                            perPortletMemento, 
                                            url,
                                            request,
                                            response,
                                            new InputStreamReader(method
                                                    .getResponseBodyAsStream(),
                                                    method.getResponseCharSet()));
                                    return null;
                                }
                            });
                } else {
                    // we have content, transform that
                    transformer.transform(memento, perPortletMemento, bridgeRequest.getUrl(), request,
                            response, new StringReader(content.getContent()));
                }
            }

        } catch (ResourceException resourceException) {
            String format = MessageFormat.format(resourceBundle
                    .getString(resourceException.getMessage()),
                    resourceException.getArgs());
            throw new PortletException(format, resourceException.getCause());
        }
    }


    public void destroy() {
    }

    public void setHttpClientTemplate(HttpClientTemplate httpClientTemplate) {
        this.httpClientTemplate = httpClientTemplate;
    }
}
