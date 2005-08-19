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

import java.io.Serializable;
import java.net.URI;

/**
 * @author jmccrindle
 */
public class DefaultBridgeRequest implements Serializable, BridgeRequest {
    
    private static final long serialVersionUID = -2897995240044433094L;
    
    private String id = null;
    private String portletId = null;
    private String pageUrl = null;
    private URI url = null;
    private Object contentLock = new Object();
    private PortletBridgeContent content = null;
    
    public DefaultBridgeRequest() {
        
    }
    
    public DefaultBridgeRequest(String id, String portletId, String pageUrl, URI url) {
        super();
        this.id = id;
        this.portletId = portletId;
        this.pageUrl = pageUrl;
        this.url = url;
    }

    public String getPageUrl() {
        return pageUrl;
    }
    public void setPageUrl(String pageUrl) {
        this.pageUrl = pageUrl;
    }
    public String getPortletId() {
        return portletId;
    }
    public void setPortletId(String portletId) {
        this.portletId = portletId;
    }
    public URI getUrl() {
        return url;
    }
    public void setUrl(URI url) {
        this.url = url;
    }

    public String getId() {
        return id;
    }

    protected boolean equals(Object o1, Object o2) {
        return (o1 != null && o1.equals(o2)) || (o2 == null && o1 == null);
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        BridgeRequest bridgeRequest = (BridgeRequest) obj;
        if(bridgeRequest == null) return false;
        return equals(pageUrl, bridgeRequest.getPageUrl()) &&
            equals(url, bridgeRequest.getUrl()) && equals(portletId, bridgeRequest.getPortletId());
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return (pageUrl != null ? pageUrl.hashCode() / 3 : 0) + 
            (url != null ? url.hashCode() / 3 : 0) + 
            (portletId != null ? portletId.hashCode() / 3 : 0);
    }

}
