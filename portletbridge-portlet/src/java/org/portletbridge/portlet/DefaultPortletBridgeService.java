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


/**
 * @author jmccrindle
 */
public class DefaultPortletBridgeService implements PortletBridgeService {

    /**
     * Default Constructor
     */
    public DefaultPortletBridgeService() {
        super();
    }

    /* (non-Javadoc)
     * @see org.portletbridge.portlet.PortletBridgeService#getIdFromRequestUri(java.lang.String)
     */
    public String getIdFromRequestUri(String requestUri) {
        int lastIndexOfSlash = requestUri.lastIndexOf('/');
        int secondLastIndexOfSlash = requestUri.lastIndexOf('/', lastIndexOfSlash);
        if(lastIndexOfSlash >= 0 && secondLastIndexOfSlash >= 0) {
            return requestUri.substring(secondLastIndexOfSlash + 1, lastIndexOfSlash - 1);
        }
        return null;
    }

}