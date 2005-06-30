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
package org.portletbridge.xsl;

import org.portletbridge.UrlUtils;

import junit.framework.TestCase;

/**
 * @author JMcCrindle
 */
public class UrlUtilsTest extends TestCase {

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    public void testGetHostPort() throws Exception {
        assertEquals("http://www.bluesnews.com", UrlUtils.getProtocolHostPort("http://www.bluesnews.com"));
        assertEquals("http://www.bluesnews.com", UrlUtils.getProtocolHostPort("http://www.bluesnews.com/"));
        assertEquals("http://www.bluesnews.com", UrlUtils.getProtocolHostPort("http://www.bluesnews.com/test"));
        assertEquals("http://www.bluesnews.com", UrlUtils.getProtocolHostPort("http://www.bluesnews.com/test/blah.html"));
        assertEquals("http://www.bluesnews.com", UrlUtils.getProtocolHostPort("http://www.bluesnews.com/test/test.html?blah"));
        assertEquals("http://www.bluesnews.com:80", UrlUtils.getProtocolHostPort("http://www.bluesnews.com:80"));
        assertEquals("http://www.bluesnews.com:80", UrlUtils.getProtocolHostPort("http://www.bluesnews.com:80/blah.html"));
        assertNull(UrlUtils.getProtocolHostPort("/blah.html"));
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Constructor for UrlUtilsTest.
     * @param arg0
     */
    public UrlUtilsTest(String arg0) {
        super(arg0);
    }

}
