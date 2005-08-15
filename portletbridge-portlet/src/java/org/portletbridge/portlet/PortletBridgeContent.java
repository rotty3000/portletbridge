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

/**
 * @author jmccrindle
 */
public class PortletBridgeContent {
    
    private URI url = null;
    private String content = null;
    private String method;

    public PortletBridgeContent(URI url, String method, String content) {
        super();
        this.url = url;
        this.method = method;
        this.content = content;
    }

    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public URI getUrl() {
        return url;
    }
    public void setUrl(URI url) {
        this.url = url;
    }
}