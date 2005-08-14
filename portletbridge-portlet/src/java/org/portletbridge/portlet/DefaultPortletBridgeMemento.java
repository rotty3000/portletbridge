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
import java.util.HashMap;
import java.util.Map;

import javax.portlet.PortletURL;
import javax.portlet.RenderResponse;

/**
 * @author JMcCrindle
 */
public class DefaultPortletBridgeMemento implements PortletBridgeMemento {

    private Map requests = new HashMap();
    private Map mementos = new HashMap();
    private IdGenerator idGenerator = new DefaultIdGenerator();
    
    public DefaultPortletBridgeMemento() {
        super();
    }

    /* (non-Javadoc)
     * @see org.portletbridge.portlet.PortletBridgeMemento#getBridgeRequest(java.lang.String)
     */
    public BridgeRequest getBridgeRequest(String id) {
        return (BridgeRequest) requests.get(id);
    }

    /* (non-Javadoc)
     * @see org.portletbridge.portlet.PortletBridgeMemento#getPerPortletMemento(java.lang.String)
     */
    public PerPortletMemento getPerPortletMemento(String portletId) {
        PerPortletMemento memento = (PerPortletMemento) mementos.get(portletId);
        if(memento == null) {
            memento = new DefaultPerPortletMemento();
        }
        return memento;
    }

    public BridgeRequest createBridgeRequest(RenderResponse response, URI url) {
        String id = idGenerator.nextId();
        PortletURL pageUrl = response.createRenderURL();
        pageUrl.setParameter("id", id);
        DefaultBridgeRequest bridgeRequest = new DefaultBridgeRequest(id, pageUrl.toString(), response.getNamespace(), url);
        requests.put(id, bridgeRequest);
        return bridgeRequest;
    }

}
