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

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.portletbridge.ResourceException;

/**
 * Default implementation of the httpclient template. Makes a call
 * to the url specifies and then passes the result to the callback.
 * 
 * @author JMcCrindle
 */
public class DefaultHttpClientTemplate implements HttpClientTemplate {
    
    private HttpClient httpClient = null;

    /**
     * 
     */
    public DefaultHttpClientTemplate() {
        httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());
    }
    
    public Object service(HttpMethodBase method, HttpClientState state, HttpClientCallback callback) throws ResourceException {
        try {
            HostConfiguration hostConfiguration = new HostConfiguration();
            
            if(state.getProxyHost() != null && state.getProxyHost().trim().length() > 0) {
                hostConfiguration.setProxy(state.getProxyHost(), state.getProxyPort());
            }
            hostConfiguration.setHost(method.getURI());
            int statusCode = httpClient.executeMethod(hostConfiguration, method, state.getHttpState());
            return callback.doInHttpClient(statusCode, method);
        } catch (ResourceException e) {
            throw e;
        } catch (Throwable e) {
            throw new ResourceException("error.httpclient", e.getMessage(), e);
        } finally {
            method.releaseConnection();
        }
    }

}
