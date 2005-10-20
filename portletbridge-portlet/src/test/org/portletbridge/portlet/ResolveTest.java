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

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

}
