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
package org.portletbridge.portlet.old;

import org.portletbridge.portlet.old.ServletUrlRewriter;

import junit.framework.TestCase;

/**
 * @author jmccrindle
 */
public class ServletUrlRewriterTest extends TestCase {

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

    public void testRewrite() throws Exception {
        ServletUrlRewriter urlRewriter = new ServletUrlRewriter("/portletbridge-portlet", 
                "pbhs", 
                "0", 
                "http://slashdot.org/");
        
        assertEquals("/portletbridge-portlet/pbhs/0/http://images.slashdot.org/title.gif", 
                urlRewriter.rewrite("//images.slashdot.org/title.gif"));
    }
    
    public void testRewriteNoSlash() throws Exception {
        ServletUrlRewriter urlRewriter = new ServletUrlRewriter("/portletbridge-portlet", 
                "pbhs", 
                "0", 
                "http://slashdot.org");
        
        assertEquals("/portletbridge-portlet/pbhs/0/http://slashdot.org/title.gif", 
                urlRewriter.rewrite("title.gif"));
    }
    
    /**
     * Constructor for ServletUrlRewriterTest.
     * @param name
     */
    public ServletUrlRewriterTest(String name) {
        super(name);
    }

}
