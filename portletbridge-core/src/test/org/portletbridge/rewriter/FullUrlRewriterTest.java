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
package org.portletbridge.rewriter;

import junit.framework.TestCase;

/**
 * @author JMcCrindle
 */
public class FullUrlRewriterTest extends TestCase {

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
     * Constructor for FullUrlRewriterTest.
     * @param name
     */
    public FullUrlRewriterTest(String name) {
        super(name);
    }

    public void testRewriteSlashdot() {
        FullUrlRewriter urlRewriter = new FullUrlRewriter("http://www.slashdot.org", "/portletbridge/http://www.slashdot.org");
        assertEquals("/portletbridge/http://www.slashdot.org/images/test.gif", urlRewriter.rewrite("/images/test.gif"));
        assertEquals("/portletbridge/http://www.slashdot.org/images/images/test.gif", urlRewriter.rewrite("/images/images/test.gif"));
        assertEquals("images/test.gif", urlRewriter.rewrite("images/test.gif"));
        assertEquals("/portletbridge/http://images.slashdot.org/topics/topicspace.gif", urlRewriter.rewrite("//images.slashdot.org/topics/topicspace.gif"));
    }

    public void testRewriteJakarta() {
        FullUrlRewriter urlRewriter = new FullUrlRewriter("http://jakarta.apache.org", "/portletbridge/http://jakarta.apache.org");
        assertEquals("./index.html", urlRewriter.rewrite("./index.html"));
        assertEquals("/portletbridge/http://jakarta.apache.org/", urlRewriter.rewrite("http://jakarta.apache.org"));
        assertEquals("/portletbridge/http://slashdot.org/", urlRewriter.rewrite("http://slashdot.org"));
    }

}
