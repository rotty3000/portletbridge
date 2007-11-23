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

import java.net.URI;

import junit.framework.TestCase;

public class ResolveTest extends TestCase {

    public ResolveTest(String arg0) {
        super(arg0);
    }
    
    public void testResolve() throws Throwable {
        URI uri = new URI("http://www.nscorp.com/");
        URI resolvedUri = uri.resolve("/nscorp/application?pageid=Employees&category=Employees&title=Employee+Resource+Center");
        assertNotNull(resolvedUri);
    }
    
    public void testYourResolve() throws Throwable {
        URI uri = new URI("http://emu.atldc.nscorp.com:8011/EOLStoreWeb/init.do");
        URI resolvedUri = uri.resolve("getItemList.do?searchResults=A");
        assertNotNull(resolvedUri);
        assertEquals("http://emu.atldc.nscorp.com:8011/EOLStoreWeb/getItemList.do?searchResults=A", resolvedUri.toString());
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

}
