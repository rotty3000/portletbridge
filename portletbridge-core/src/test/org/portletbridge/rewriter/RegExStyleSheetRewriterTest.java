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

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.portletbridge.PortletBridgeException;
import org.portletbridge.UrlRewriter;

/**
 * @author JMcCrindle
 */
public class RegExStyleSheetRewriterTest extends TestCase {

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
     * Constructor for RegExStyleSheetRewriterTest.
     * @param name
     */
    public RegExStyleSheetRewriterTest(String name) {
        super(name);
    }

    public void testRewrite() throws PortletBridgeException {
        
        final List urls = new ArrayList();
        
        RegExStyleSheetRewriter rewriter = new RegExStyleSheetRewriter(new UrlRewriter() {
            public String rewrite(String url) {
                urls.add(url);
                System.out.println(url);
                return "" + urls.size();
            }
        });
        
        String rewritten = rewriter.rewrite("@import url(\"blah.css\")\n" +
                "body {\n" +
                "background-color: #FFF;\n" +
                "background-image: url(/Design/graphics/Reg_default/one.png);\n" +
                "background-image: url(\"/Design/graphics/Reg_default/two.png\");\n" +
                "background-repeat: repeat-y;\n" +
                "}");
        
        assertEquals(3, urls.size());
        
        System.out.println(rewritten);
        
    }

    public void testRewriteImports() throws PortletBridgeException {
        
        final List urls = new ArrayList();
        
        RegExStyleSheetRewriter rewriter = new RegExStyleSheetRewriter(new UrlRewriter() {
            public String rewrite(String url) {
                urls.add(url);
                System.out.println("url");
                return "" + urls.size();
            }
        });
        
        StringBuffer rewritten = rewriter.rewriteImports(new StringBuffer("@import 'blah.css'\n" +
                "body {\n" +
                "background-color: #FFF;\n" +
                "background-image: url(/Design/graphics/Reg_default/red_left_edge.png);\n" +
                "background-repeat: repeat-y;\n" +
                "}"));
        
        assertEquals(1, urls.size());
        
        System.out.println(rewritten);
        
    }

}
