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

import javax.portlet.PortletPreferences;
import javax.portlet.RenderRequest;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.NTCredentials;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.portletbridge.PortletBridgeException;
import org.portletbridge.ResourceException;

public class DefaultBridgeAuthenticator implements BridgeAuthenticator {

    public Credentials getCredentials(RenderRequest request)
            throws ResourceException {
        PortletPreferences preferences = request.getPreferences();
        String configAuthentication = preferences.getValue("authentication",
                "none");
        String configAuthenticationUsername = preferences.getValue(
                "authenticationUsername", null);
        String configAuthenticationPassword = preferences.getValue(
                "authenticationPassword", null);
        String configAuthenticationHost = preferences.getValue(
                "authenticationHost", null);
        String configAuthenticationDomain = preferences.getValue(
                "authenticationDomain", null);
        if (configAuthentication != null
                && configAuthentication.trim().length() > 0) {
            if ("ntlm".equalsIgnoreCase(configAuthentication)) {
                return new NTCredentials(configAuthenticationUsername,
                        configAuthenticationPassword, configAuthenticationHost,
                        configAuthenticationDomain);
            } else if ("basic".equalsIgnoreCase(configAuthentication)) {
                return
                        new UsernamePasswordCredentials(
                                configAuthenticationUsername,
                                configAuthenticationPassword);
            } else if ("none".equalsIgnoreCase(configAuthentication)) {
                return null;
            } else {
                throw new PortletBridgeException("error.configAuthentication");
            }
        }
        return null;
    }
}
