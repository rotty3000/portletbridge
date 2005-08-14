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

import java.net.URI;

import javax.portlet.PortletURL;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

/**
 * @author JMcCrindle
 */
public class PortletFunctions {

    private final URI currentUrl;

    private final RenderRequest request;

    private final RenderResponse response;

    private final String servletName;

    private final PortletBridgeMemento memento;
    
    private final URI[] scope;

    private final PerPortletMemento perPortletMemento;

    public PortletFunctions(PortletBridgeMemento memento,
            PerPortletMemento perPortletMemento, String servletName,
            URI currentUrl, RenderRequest request, RenderResponse response) {
        this.memento = memento;
        this.perPortletMemento = perPortletMemento;
        this.servletName = servletName;
        this.currentUrl = currentUrl;
        this.request = request;
        this.response = response;
        this.scope = perPortletMemento.getScope();
    }

    public String link(String link) {
        if (link.startsWith("javascript:")) {
            return script(link);
        } else if (link.equals("#")) {
            return link;
        } else {
            return rewrite(link, true);
        }
    }

    private String rewrite(String link, boolean checkScope) {
        URI url = currentUrl.resolve(link.trim());
        if (url.getScheme().equals("http") || url.getScheme().equals("https")) {
            if (!checkScope || shouldRewrite(url)) {
                BridgeRequest bridgeRequest = memento.createBridgeRequest(response, url);
                String name = url.getPath();
                int lastIndex = name.lastIndexOf('/');
                if (lastIndex != -1) {
                    name = name.substring(lastIndex + 1);
                    if (name.equals("") && lastIndex > 0)
                        name = url.getPath().substring(
                                url.getPath().lastIndexOf('/', lastIndex - 1));

                }
                if (name.startsWith("/"))
                    name = name.substring(1);
                name = '/' + servletName + '/' + bridgeRequest.getId() + "/" + name;
                return name;
            } else {
                return url.toString();
            }
        } else {
            return link;
        }
    }

    private boolean shouldRewrite(URI uri) {
        for (int i = 0; i < scope.length; i++) {
            URI proxyUri = scope[i];
            if (uri.getScheme().equals(proxyUri.getScheme())
                    && uri.getHost().equals(proxyUri.getHost())
                    && uri.getPath().startsWith(proxyUri.getPath()))
                return true;
        }

        return false;
    }

    public String style(String link) {
        return link;
    }

    public String script(String link) {
        return link;
    }

    public URI getCurrentUrl() {
        return currentUrl;
    }

    public PortletBridgeMemento getMemento() {
        return memento;
    }

    public PerPortletMemento getPerPortletMemento() {
        return perPortletMemento;
    }

    public RenderRequest getRequest() {
        return request;
    }

    public RenderResponse getResponse() {
        return response;
    }

    public URI[] getScope() {
        return scope;
    }

    public String getServletName() {
        return servletName;
    }

}