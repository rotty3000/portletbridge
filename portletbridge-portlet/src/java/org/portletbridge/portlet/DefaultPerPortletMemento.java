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
import java.net.URISyntaxException;
import java.util.regex.Pattern;

import javax.portlet.PortletPreferences;

import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.NTCredentials;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.portletbridge.PortletBridgeException;
import org.portletbridge.ResourceException;

/**
 * @author JMcCrindle
 */
public class DefaultPerPortletMemento implements PerPortletMemento {

    private URI initUrl;

    private HttpState state = new HttpState();

    private String proxyHost;

    private int proxyPort;

    private Pattern scope = Pattern.compile(".*");

    /**
     *  
     */
    public DefaultPerPortletMemento() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.portletbridge.portlet.PerPortletMemento#getHttpState()
     */
    public HttpState getHttpState() {
        return state;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.portletbridge.portlet.PerPortletMemento#getProxyHost()
     */
    public String getProxyHost() {
        return proxyHost;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.portletbridge.portlet.PerPortletMemento#getProxyPort()
     */
    public int getProxyPort() {
        return proxyPort;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.portletbridge.portlet.PerPortletMemento#setPreferences(javax.portlet.PortletPreferences)
     */
    public void setPreferences(PortletPreferences preferences)
            throws ResourceException {
        String initUrlPreference = preferences.getValue("initUrl", null);
        if (initUrlPreference == null || initUrlPreference.trim().length() == 0) {
            throw new ResourceException("error.initurl",
                    "preference not defined");
        }
        try {
            this.initUrl = new URI(initUrlPreference);
        } catch (URISyntaxException e) {
            throw new ResourceException("error.initurl", e.getMessage(), e);
        }
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

        String configProxyAuthentication = preferences.getValue(
                "proxyAuthentication", "none");

        String configProxyAuthenticationUsername = preferences.getValue(
                "proxyAuthenticationUsername", null);
        String configProxyAuthenticationPassword = preferences.getValue(
                "proxyAuthenticationPassword", null);
        String configProxyAuthenticationHost = preferences.getValue(
                "proxyAuthenticationHost", null);
        String configProxyAuthenticationDomain = preferences.getValue(
                "proxyAuthenticationDomain", null);

        if (configProxyAuthentication != null
                && configProxyAuthentication.trim().length() > 0) {
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
            throw new PortletBridgeException("error.configProxyEnabled");
        }

        if (configProxyAuthentication != null
                && configAuthentication.trim().length() > 0) {
            if ("ntlm".equalsIgnoreCase(configProxyAuthentication)) {
                state.setCredentials(AuthScope.ANY, new NTCredentials(
                        configAuthenticationUsername,
                        configAuthenticationPassword, configAuthenticationHost,
                        configAuthenticationDomain));
            } else if ("basic".equalsIgnoreCase(configProxyAuthentication)) {
                state.setProxyCredentials(AuthScope.ANY,
                        new UsernamePasswordCredentials(
                                configProxyAuthenticationUsername,
                                configProxyAuthenticationPassword));
            } else if ("none".equalsIgnoreCase(configAuthentication)) {
                state.clearProxyCredentials();
            } else {
                throw new PortletBridgeException("error.configAuthentication");
            }
        }

        proxyHost = preferences.getValue("proxyHost", null);
        proxyPort = Integer.parseInt(preferences.getValue("proxyPort", "80"));

        String scopePreference = preferences.getValue("scope", null);
        if(scopePreference != null) {
            if(!scope.pattern().equals(scopePreference)) {
                scope = Pattern.compile(scopePreference);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.portletbridge.portlet.PerPortletMemento#getScope()
     */
    public Pattern getScope() {
        return scope;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.portletbridge.portlet.PerPortletMemento#getInitUrl()
     */
    public URI getInitUrl() {
        return initUrl;
    }

}