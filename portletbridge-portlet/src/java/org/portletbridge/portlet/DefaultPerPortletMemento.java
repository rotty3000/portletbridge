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
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.portlet.PortletPreferences;
import javax.portlet.RenderRequest;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.NTCredentials;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.portletbridge.PortletBridgeException;
import org.portletbridge.ResourceException;

/**
 * Default implementation of the per portlet memento. This stored information
 * about the current state of the portlet that can be retrieved by the 
 * PortletBridgePortlet.
 * 
 * @author JMcCrindle
 */
public class DefaultPerPortletMemento implements PerPortletMemento, Serializable {

    /**
     * default serial version id 
     */
    private static final long serialVersionUID = 7117499680906225653L;

    private URI initUrl;

    private SerializeableHttpState state = new SerializeableHttpState();

    private String proxyHost;

    private int proxyPort;

    private Pattern scope = Pattern.compile(".*");

    private Map bridgeContent = new HashMap();

    private final BridgeAuthenticator bridgeAuthenticator;

	private final InitUrlFactory initUrlFactory;

    /**
     *  
     */
    public DefaultPerPortletMemento(BridgeAuthenticator bridgeAuthenticator, InitUrlFactory initUrlFactory) {
        this.bridgeAuthenticator = bridgeAuthenticator;
		this.initUrlFactory = initUrlFactory;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.portletbridge.portlet.PerPortletMemento#getHttpState()
     */
    public SerializeableHttpState getHttpState() {
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
    public void setPreferences(RenderRequest request)
            throws ResourceException {
        PortletPreferences preferences = request.getPreferences();
        
        if(initUrlFactory != null) {
        		this.initUrl = initUrlFactory.getInitUrl(request);
        } else {
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
        }

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

        if(bridgeAuthenticator != null) {
            Credentials credentials = bridgeAuthenticator.getCredentials(request);
            if(credentials != null) {
                state.setCredentials(AuthScope.ANY, credentials);
            } else {
                state.clearCredentials();
            }
        }
        
        proxyHost = preferences.getValue("proxyHost", System.getProperty("http.proxyHost"));
        String proxyPortPreference = preferences.getValue("proxyPort", System.getProperty("http.proxyPort"));
        if(proxyPortPreference != null) {
            String trimmed = proxyPortPreference.trim();
            if(trimmed.length() > 0) {
                try {
                    proxyPort = Integer.parseInt(proxyPortPreference);
                } catch(NumberFormatException e) {
                    // noop for now
                    proxyPort = 80;
                }
            } else {
                proxyPort = 80;
            }
        } else {
            proxyPort = 80;
        }

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

    public void enqueueContent(String bridgeRequestId, PortletBridgeContent content) {
        synchronized(bridgeContent) {
            bridgeContent.clear();
            bridgeContent.put(bridgeRequestId, content);
        }
    }

    public PortletBridgeContent dequeueContent(String bridgeRequestId) {
        synchronized(bridgeContent) {
            PortletBridgeContent portletBridgeContent = (PortletBridgeContent) bridgeContent.get(bridgeRequestId);
            bridgeContent.clear();
            return portletBridgeContent;
        }
    }

}