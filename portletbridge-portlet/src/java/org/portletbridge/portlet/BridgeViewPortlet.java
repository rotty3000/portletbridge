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
import java.io.StringReader;
import java.net.URI;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.commons.httpclient.HttpMethodBase;
import org.portletbridge.ResourceException;

/**
 * @author JMcCrindle
 */
public class BridgeViewPortlet extends GenericPortlet {

    private String mementoSessionKey = null;

    private HttpClientTemplate httpClientTemplate = null;

    private BridgeTransformer transformer = null;

    public void processAction(ActionRequest request, ActionResponse response)
            throws PortletException, IOException {
        // noop
    }

    public void doView(final RenderRequest request,
            final RenderResponse response) throws PortletException, IOException {

        ResourceBundle resourceBundle = getPortletConfig().getResourceBundle(request
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
                session.setAttribute(mementoSessionKey, tempMemento, PortletSession.APPLICATION_SCOPE);
            }
            final PortletBridgeMemento memento = tempMemento;
            final PerPortletMemento perPortletMemento = memento
                    .getPerPortletMemento(portletId);
            perPortletMemento.setPreferences(preferences);
            String urlId = request.getParameter("id");

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
        super.destroy();
    }

    public void setHttpClientTemplate(HttpClientTemplate httpClientTemplate) {
        this.httpClientTemplate = httpClientTemplate;
    }

    public void setTransformer(BridgeTransformer transformer) {
        this.transformer = transformer;
    }
    public void setMementoSessionKey(String mementoSessionKey) {
        this.mementoSessionKey = mementoSessionKey;
    }
}
