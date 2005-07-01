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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.portletbridge.PortletBridgeException;


/**
 * Implementation of the ProxyTransport and ResourceTransport interfaces
 * using the apache HttpClient.
 * @author jmccrindle
 */
public class CommonsProxyTransport implements ProxyTransport, ResourceTransport {

    private HttpClient client = null;
    
    /**
     * Default constructor. Creates a multithreaded HttpClient with default 
     * settings.
     */
    public CommonsProxyTransport() {
        MultiThreadedHttpConnectionManager connectionManager = 
            new MultiThreadedHttpConnectionManager();
        client = new HttpClient(connectionManager);
    }

    /**
     * @see org.portletbridge.portlet.ProxyTransport#createProxyBrowser()
     */
    public ProxyBrowser createProxyBrowser() {
        return new CommonsProxyBrowser();
    }

    /**
     * @see org.portletbridge.portlet.ProxyTransport#get(org.portletbridge.portlet.ProxyBrowser, java.lang.String)
     */
    public HttpResult get(ProxyBrowser proxyBrowser, final String url) throws PortletBridgeException {
        CommonsProxyBrowser browser = (CommonsProxyBrowser) proxyBrowser;
        GetMethod method = new GetMethod(url);
        try {
            HostConfiguration hostConfiguration = new HostConfiguration();
            
            if(browser.getProxyHost() != null && browser.getProxyHost().trim().length() > 0) {
                hostConfiguration.setProxy(browser.getProxyHost(), browser.getProxyPort());
            }
            hostConfiguration.setHost(method.getURI());
            client.executeMethod(hostConfiguration, method, browser.getState());
            final byte[] responseBody = method.getResponseBody();
            final String contentType = method.getResponseHeader("Content-Type").getValue();
            DefaultHttpResult result = new DefaultHttpResult(url, contentType, responseBody);
            if(contentType.indexOf("text/html") != -1) {
                synchronized(browser) {
                    browser.setCurrentPage(result);
                }
            }
            return result;
        } catch (HttpException e) {
            throw new PortletBridgeException("error.http.exception", e.getMessage(), e);
        } catch (IOException e) {
            throw new PortletBridgeException("error.io.exception", e.getMessage(), e);
        } finally {
            method.releaseConnection();
        }
    }

    /**
     * @see org.portletbridge.portlet.ProxyTransport#post(org.portletbridge.portlet.ProxyBrowser, java.lang.String, java.util.Map)
     */
    public HttpResult post(ProxyBrowser proxyBrowser, String url, Map parameters) throws PortletBridgeException {
        CommonsProxyBrowser browser = (CommonsProxyBrowser) proxyBrowser;
        PostMethod method = new PostMethod(url);
        for (Iterator paramIterator = parameters.entrySet().iterator(); paramIterator.hasNext();) {
            Map.Entry entry = (Map.Entry) paramIterator.next();
            String[] values = (String[]) entry.getValue();
            for (int i = 0; i < values.length; i++) {
                String value = values[i];
                method.addParameter((String) entry.getKey(), value);
            }
        }
        try {
            HostConfiguration hostConfiguration = new HostConfiguration();
            
            if(browser.getProxyHost() != null && browser.getProxyHost().trim().length() > 0) {
                hostConfiguration.setProxy(browser.getProxyHost(), browser.getProxyPort());
            }
            hostConfiguration.setHost(method.getURI());
            client.executeMethod(hostConfiguration, method, browser.getState());
            final byte[] responseBody = method.getResponseBody();
            final String contentType = method.getResponseHeader("Content-Type").getValue();
            DefaultHttpResult result = new DefaultHttpResult(url, contentType, responseBody);
            if(contentType.indexOf("text/html") != -1) {
                synchronized(browser) {
                    browser.setCurrentPage(result);
                }
            }
            return result;
        } catch (HttpException e) {
            throw new PortletBridgeException("error.http.exception", e.getMessage(), e);
        } catch (IOException e) {
            throw new PortletBridgeException("error.io.exception", e.getMessage(), e);
        } finally {
            method.releaseConnection();
        }
    }

    /**
     * Note, the HttpResult inputstream must be opened and closed.
     * @see org.portletbridge.portlet.ResourceTransport#getResource(org.portletbridge.portlet.ProxyBrowser, java.lang.String)
     */
    public HttpResult getResource(ProxyBrowser proxyBrowser, final String url) throws PortletBridgeException {
        CommonsProxyBrowser browser = (CommonsProxyBrowser) proxyBrowser;
        final GetMethod method = new GetMethod(url);
        try {
            HostConfiguration hostConfiguration = new HostConfiguration();
            
            if(browser.getProxyHost() != null && browser.getProxyHost().trim().length() > 0) {
                hostConfiguration.setProxy(browser.getProxyHost(), browser.getProxyPort());
            }
            hostConfiguration.setHost(method.getURI());
            client.executeMethod(hostConfiguration, method, browser.getState());
            final byte[] responseBody = method.getResponseBody();
            final String contentType = method.getResponseHeader("Content-Type").getValue();
            HttpResult result = new HttpResult() {
                public String getContentType() {
                    return contentType;
                }
                public InputStream getContents() throws IOException {
                    return new FilterInputStream(method.getResponseBodyAsStream()) {
                        public void close() throws IOException {
                            method.releaseConnection();
                        }
                    };
                }
                public String getUrl() {
                    return url;
                }
            };
            return result;
        } catch (HttpException e) {
            throw new PortletBridgeException("error.http.exception", e.getMessage(), e);
        } catch (IOException e) {
            throw new PortletBridgeException("error.io.exception", e.getMessage(), e);
        }
    }

    /**
     * @param client set a different client for this transport to use
     */
    public void setClient(HttpClient client) {
        this.client = client;
    }
}
