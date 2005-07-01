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

import java.io.IOException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.WindowState;

import junit.framework.TestCase;

import org.portletbridge.mock.MockPortletConfig;
import org.portletbridge.mock.MockPortletPreferences;
import org.portletbridge.mock.MockPortletSession;
import org.portletbridge.mock.MockProxyTransport;
import org.portletbridge.mock.MockRenderRequest;
import org.portletbridge.mock.MockRenderResponse;

/**
 * @author jmccrindle
 */
public class PortletBridgePortletTest extends TestCase {

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Constructor for PortletBridgePortletTest.
     * @param name
     */
    public PortletBridgePortletTest(String name) {
        super(name);
    }

    public void testInit() throws PortletException {
        PortletBridgePortlet portlet = new PortletBridgePortlet();
        ResourceBundle bundle = PropertyResourceBundle.getBundle("org.portletbridge.portlet.PortletBridgePortlet");
        MockPortletConfig mockPortletConfig = new MockPortletConfig();
        mockPortletConfig.setupResourceBundle(bundle);
        mockPortletConfig.setupInitParam("proxyBrowserSessionKey", "proxyBrowserSessionKey");
        mockPortletConfig.setupInitParam("parserClassName", "org.cyberneko.html.parsers.SAXParser");
        mockPortletConfig.setupInitParam("servletName", "pbhs");
        portlet.init(mockPortletConfig);
    }

    public void testProcessAction() {
    }

    public void testRenderViewNormal() throws IOException, PortletException {
        ResourceBundle bundle = PropertyResourceBundle.getBundle("org.portletbridge.portlet.PortletBridgePortlet");
        PortletBridgePortlet portlet = new PortletBridgePortlet();
        MockPortletConfig mockPortletConfig = new MockPortletConfig();
        mockPortletConfig.setupInitParam("proxyBrowserSessionKey", "proxyBrowserSessionKey");
        mockPortletConfig.setupInitParam("methodRequestKey", "methodRequestKey");
        mockPortletConfig.setupInitParam("parserClassName", "org.cyberneko.html.parsers.SAXParser");
        mockPortletConfig.setupInitParam("servletName", "pbhs");
        mockPortletConfig.setupResourceBundle(bundle);
        portlet.init(mockPortletConfig);
        MockRenderRequest mockRenderRequest = new MockRenderRequest();
        mockRenderRequest.setupWindowState(WindowState.NORMAL);
        mockRenderRequest.setupPortletMode(PortletMode.VIEW);
        mockRenderRequest.setupPortletSession(new MockPortletSession());
        MockPortletPreferences mockPortletPreferences = new MockPortletPreferences();
        mockPortletPreferences.setValue("initUrl", "http://asksid:8080/");
        mockPortletPreferences.setValue("stylesheet", "classpath:/org/portletbridge/xsl/portletbridge.xsl");
        mockRenderRequest.setupPortletPreferences(mockPortletPreferences);
        MockProxyTransport proxyTransport = new MockProxyTransport();
        proxyTransport.setupContents("<html><head><title>atitle</title></head><body>test</body></html>");
        portlet.setProxyTransport(proxyTransport);
        MockRenderResponse mockRenderResponse = new MockRenderResponse();
        portlet.render(mockRenderRequest, mockRenderResponse);
        assertEquals("text/html", mockRenderResponse.getContentType());
        assertEquals("test", mockRenderResponse.retrieveContent());
        assertEquals("atitle", mockRenderResponse.retrieveTitle());
    }

    public void testCreateState() {
    }

}
