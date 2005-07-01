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

import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.NTCredentials;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.portletbridge.PortletBridgeException;

/**
 * Implementation of the ProxyBrowser for HttpClient (or the Commons
 * ProxyTransport). 
 * 
 * @author jmccrindle
 */
public class CommonsProxyBrowser implements ProxyBrowser {

    private SerializeableHttpState state = new SerializeableHttpState();
    private HttpResult result;
    private String proxyHost;
    private int proxyPort;
    
    /**
     * Default Constructor
     */
    public CommonsProxyBrowser() {

    }

    /**
     * @return the current page that the browser is on
     * @see org.portletbridge.portlet.ProxyBrowser#getCurrentPage()
     */
    public HttpResult getCurrentPage() {
        return result;
    }
    
    /**
     * @param result the current page that the browser is on. should not be null.
     */
    public void setCurrentPage(HttpResult result) {
        this.result = result;
    }

    /**
     * Set the preferences for this browser. The complete set of preferences is:
     * 
     * authentication
     * authenticationUsername
     * authenticationPassword
     * authenticationHost
     * authenticationDomain
     * proxyAuthentication
     * proxyAuthenticationUsername
     * proxyAuthenticationPassword
     * proxyAuthenticationHost
     * proxyAuthenticationDomain
     * proxyHost
     * proxyPort
     * 
     * @see org.portletbridge.portlet.ProxyBrowser#setPreferences(javax.portlet.PortletPreferences)
     */
    public synchronized void setPreferences(PortletPreferences preferences) throws PortletBridgeException {
        
        // TODO: this could be done only on a change instead of every time.

        String configAuthentication = preferences.getValue("authentication","none");
        String configAuthenticationUsername = preferences.getValue("authenticationUsername", null);
        String configAuthenticationPassword = preferences.getValue("authenticationPassword", null);
        String configAuthenticationHost = preferences.getValue("authenticationHost",null);
        String configAuthenticationDomain = preferences.getValue("authenticationDomain", null);

        String configProxyAuthentication = preferences.getValue("proxyAuthentication","none");
        
        String configProxyAuthenticationUsername = preferences
                .getValue(
                        "proxyAuthenticationUsername",
                        null);
        String configProxyAuthenticationPassword = preferences
                .getValue(
                        "proxyAuthenticationPassword",
                        null); 
        String configProxyAuthenticationHost = preferences
                .getValue(
                        "proxyAuthenticationHost",
                        null);
        String configProxyAuthenticationDomain = preferences.getValue(
                "proxyAuthenticationDomain", null);
                
        if (configProxyAuthentication != null && configProxyAuthentication.trim().length() > 0) {
            if ("ntlm".equalsIgnoreCase(configProxyAuthentication)) {
                state.setProxyCredentials(AuthScope.ANY, new NTCredentials(
                        configProxyAuthenticationUsername,
                        configProxyAuthenticationPassword,
                        configProxyAuthenticationHost,
                        configProxyAuthenticationDomain));
            } else if ("basic".equalsIgnoreCase(configProxyAuthentication)) {
                state.setProxyCredentials(AuthScope.ANY,
                        new UsernamePasswordCredentials(
                                configProxyAuthenticationUsername,
                                configProxyAuthenticationPassword));
            } else if ("none".equalsIgnoreCase(configProxyAuthentication)) {
                state.clearProxyCredentials();
            } else {
                throw new PortletBridgeException(
                        "error.configProxyAuthentication");
            }
        } else {
            throw new PortletBridgeException(
                    "error.configProxyEnabled");
        }

        if (configProxyAuthentication != null && configAuthentication.trim().length() > 0) {
            if ("ntlm".equalsIgnoreCase(configProxyAuthentication)) {
                state.setCredentials(AuthScope.ANY, new NTCredentials(
                        configAuthenticationUsername,
                        configAuthenticationPassword,
                        configAuthenticationHost,
                        configAuthenticationDomain));
            } else if ("basic".equalsIgnoreCase(configProxyAuthentication)) {
                state.setProxyCredentials(AuthScope.ANY,
                        new UsernamePasswordCredentials(
                                configProxyAuthenticationUsername,
                                configProxyAuthenticationPassword));
            } else if ("none".equalsIgnoreCase(configAuthentication)) {
                state.clearProxyCredentials();
            } else {
                throw new PortletBridgeException(
                        "error.configAuthentication");
            }
        }
        
        proxyHost = preferences.getValue("proxyHost", null);
        proxyPort = Integer.parseInt(preferences.getValue("proxyPort", "80"));

    }

    /**
     * @return the current httpclient state of the browser (cookies etc.)
     */
    public HttpState getState() {
        return state;
    }
    
    /**
     * @return the proxy host for this browser. null indicates that no
     *  proxy is required
     */
    public String getProxyHost() {
        return proxyHost;
    }
    
    /**
     * @return the proxy port for this browser
     */
    public int getProxyPort() {
        return proxyPort;
    }
}
