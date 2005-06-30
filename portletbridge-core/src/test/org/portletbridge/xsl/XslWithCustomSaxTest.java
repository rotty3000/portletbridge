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

import java.io.File;
import java.io.FileReader;

import javax.xml.transform.Templates;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import junit.framework.TestCase;

import org.ccil.cowan.tagsoup.Parser;
import org.portletbridge.rewriter.DefaultUrlRewriter;
import org.xml.sax.InputSource;

/**
 * @author JMcCrindle
 */
public class XslWithCustomSaxTest extends TestCase {

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    public void testTransform() throws Exception {
        Parser parser = new Parser();
        parser.setFeature(Parser.namespacesFeature, false);
        
        TransformerFactory factory = TransformerFactory.newInstance();
        Templates templates = factory.newTemplates(new StreamSource(new FileReader(new File("./src/test/org/portletbridge/xsl/test.xsl"))));

        XslFilter xslFilter = new XslFilter(templates);

        LinkRewriterXmlFilter filter = new LinkRewriterXmlFilter();
        filter.setParent(parser);
        filter.setContentHandler(xslFilter);
        filter.setUrlRewriter(new DefaultUrlRewriter("/test/test.do", "/mytest/"));

        // parser.setContentHandler(handler1);

        // Parse the source XML, and send the parse events to the
        // TransformerHandler.
        filter.parse(new InputSource(
                new FileReader(new File(
                "./src/test/org/portletbridge/xsl/test.xml"))));
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Constructor for XslTest.
     * @param arg0
     */
    public XslWithCustomSaxTest(String arg0) {
        super(arg0);
    }

}
