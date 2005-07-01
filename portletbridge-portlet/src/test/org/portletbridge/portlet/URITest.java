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

import junit.framework.TestCase;

import org.apache.commons.httpclient.URI;

/**
 * @author jmccrindle
 */
public class URITest extends TestCase {
    public void testURI() throws Exception {
        URI uri = new URI("//slashdot.org/", false);
        assertTrue(!uri.isAbsoluteURI());
        assertTrue(uri.isAbsPath());
    }
    
    public void testHostPort() throws Exception {
        URI uri = new URI("http://www.blah.com/test.gif", false);
        assertEquals("/", uri.getAboveHierPath());
        assertEquals(-1, uri.getPort());
    }

    public void testGetPath() throws Exception {
        URI uri = new URI("http://asksid:8080", false);
        assertEquals(null, uri.getPath());
    }
}
