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
package org.portletbridge.mock;

import java.net.URI;
import java.util.regex.Pattern;

import javax.portlet.PortletPreferences;

import org.apache.commons.httpclient.HttpState;
import org.portletbridge.ResourceException;
import org.portletbridge.portlet.PerPortletMemento;

/**
 * @author JMcCrindle
 */
public class MockPerPortletMemento implements PerPortletMemento {

    /**
     * 
     */
    public MockPerPortletMemento() {
        super();
    }

    /* (non-Javadoc)
     * @see org.portletbridge.portlet.PerPortletMemento#setPreferences(javax.portlet.PortletPreferences)
     */
    public void setPreferences(PortletPreferences preferences)
            throws ResourceException {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.portletbridge.portlet.PerPortletMemento#getScope()
     */
    public Pattern getScope() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.portletbridge.portlet.PerPortletMemento#getInitUrl()
     */
    public URI getInitUrl() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.portletbridge.portlet.HttpClientState#getHttpState()
     */
    public HttpState getHttpState() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.portletbridge.portlet.HttpClientState#getProxyHost()
     */
    public String getProxyHost() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.portletbridge.portlet.HttpClientState#getProxyPort()
     */
    public int getProxyPort() {
        // TODO Auto-generated method stub
        return 0;
    }

}
