package org.portletbridge.portlet;

import java.net.URI;

import junit.framework.TestCase;

public class BridgeFunctionsTest extends TestCase {
    public void testRewriteQuestionMark() throws Exception {
        String link = "?test";
        URI currentUrl = new URI("http://www.test.com");
        URI url = null;
        if(link.startsWith("?")) {
            url = URI.create(currentUrl.toString() + link);
        }
        assertEquals("http://www.test.com?test", url.toString());
    }
}
