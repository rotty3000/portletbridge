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

import org.portletbridge.PortletBridgeException;
import org.portletbridge.mock.MockRenderResponse;

import junit.framework.TestCase;

/**
 * @author jmccrindle
 */
public class PortletUrlRewriterTest extends TestCase {

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
     * Constructor for PortletUrlRewriterTest.
     * @param name
     */
    public PortletUrlRewriterTest(String name) {
        super(name);
    }

    public void testRewrite() throws PortletBridgeException {
        PortletUrlRewriter rewriter = new PortletUrlRewriter("http://asksid:8080/", true, new MockRenderResponse());
        assertEquals("mock?actionurl=http%3A%2F%2Fasksid%3A8080%2FWiki.jsp%3Fpage%3DPortletBridge", rewriter.rewrite("Wiki.jsp?page=PortletBridge"));
    }

    public void testRewriteNoSlash() throws PortletBridgeException {
        // PortletUrlRewriter rewriter = new PortletUrlRewriter("http://asksid:8080", true, new MockRenderResponse());
        // assertEquals("mock?actionurl=http%3A%2F%2Fasksid%3A8080%2FWiki.jsp%3Fpage%3DPortletBridge", rewriter.rewrite("Wiki.jsp?page=PortletBridge"));
    }

    public void testRewriteDotDot() throws PortletBridgeException {
        PortletUrlRewriter rewriter = new PortletUrlRewriter("http://asksid:8080/test/", true, new MockRenderResponse());
        assertEquals("mock?actionurl=http%3A%2F%2Fasksid%3A8080%2Ftest%2F..%2FWiki.jsp%3Fpage%3DPortletBridge", rewriter.rewrite("../Wiki.jsp?page=PortletBridge"));
    }

    public void testRewriteNonHttp() throws PortletBridgeException {
        PortletUrlRewriter rewriter = new PortletUrlRewriter("http://asksid:8080/", true, new MockRenderResponse());
        assertEquals("mailto:bob@bob.com", rewriter.rewrite("mailto:bob@bob.com"));
    }

}
