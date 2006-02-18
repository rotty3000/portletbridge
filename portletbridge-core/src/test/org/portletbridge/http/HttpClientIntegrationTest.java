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
package org.portletbridge.http;

import java.io.IOException;
import java.io.Serializable;

import junit.framework.TestCase;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;

import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;

/**
 * @author JMcCrindle
 */
public class HttpClientIntegrationTest extends TestCase {

    private static final class MyHttpState extends HttpState implements Serializable {
    		private static final long serialVersionUID = 1404178053957985083L; 
    	}
    
    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    public void testHttpClient() throws Exception {
        final HttpClient client = new HttpClient(new MultiThreadedHttpConnectionManager());
        PooledExecutor executor = new PooledExecutor();
        for(int i = 0; i < 100; i++) {
            executor.execute(new Runnable() {
                public void run() {
                    GetMethod getMethod = new GetMethod("http://localhost:8080/helloworld/helloworld.jsp");
                    try {
                        HttpState state = new MyHttpState();
                        client.executeMethod(client.getHostConfiguration(), getMethod, state);
                    } catch (HttpException e) {
                        throw new RuntimeException(e);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } finally {
                        getMethod.releaseConnection();
                    }
                }
            });
        }
        executor.shutdownAfterProcessingCurrentlyQueuedTasks();
        executor.awaitTerminationAfterShutdown();
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Constructor for HttpClientIntegrationTest.
     * @param name
     */
    public HttpClientIntegrationTest(String name) {
        super(name);
    }

}
