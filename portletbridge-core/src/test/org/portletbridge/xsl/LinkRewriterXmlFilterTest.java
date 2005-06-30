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
import java.io.OutputStreamWriter;

import junit.framework.TestCase;

import org.ccil.cowan.tagsoup.XMLWriter;
import org.portletbridge.rewriter.FullUrlRewriter;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * @author JMcCrindle
 */
public class LinkRewriterXmlFilterTest extends TestCase {

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    public void testTransform() throws Exception {
//        XMLReader parser = XMLReaderFactory
//            .createXMLReader("org.ccil.cowan.tagsoup.Parser");
        XMLReader parser =
               XMLReaderFactory.createXMLReader("org.cyberneko.html.parsers.SAXParser");
        parser.setFeature("http://cyberneko.org/html/features/scanner/notify-builtin-refs", false);
        //parser.setFeature("http://xml.org/sax/features/namespaces", false);

      
            // A TransformerHandler is a ContentHandler that will listen for
            // SAX events, and transform them to the result.
            // Set up a hanlder for each style sheet. 

            long startTime = System.currentTimeMillis();
            
            int max = 1;
            for(int i = 0; i < max; i++) {

                //XMLWriter writer = new XMLWriter(new StringWriter());
                XMLWriter writer = new XMLWriter(new OutputStreamWriter(System.out));
//                ContentHandler x;
//                SerializerToHTML writer = new SerializerToHTML();
//                writer.setWriter(new OutputStreamWriter(System.out));

                LinkRewriterXmlFilter filter = new LinkRewriterXmlFilter();
                filter.setParent(parser);
                filter.setContentHandler(writer);
                filter.setUrlRewriter(new FullUrlRewriter("http://localhost:80/test/test.do", "/mytest/http://localhost:80/"));

                // parser.setContentHandler(handler1);

                // Parse the source XML, and send the parse events to the
                // TransformerHandler.
                filter.parse(new InputSource(
                        new FileReader(new File(
                        "./src/test/org/portletbridge/xsl/linkrewritertest.xml"))));

            }
            
            long finishTime = System.currentTimeMillis();
            System.out.println("Total Time: " + (finishTime - startTime) + "ms");
            System.out.println("Average Time: " + ((finishTime - startTime) / max));
    }
    
    public static void main(String[] args) throws Exception {
        LinkRewriterXmlFilterTest test = new LinkRewriterXmlFilterTest("LinkRewriterTest");
        test.setUp();
        test.testTransform();
        test.tearDown();
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
    public LinkRewriterXmlFilterTest(String arg0) {
        super(arg0);
    }

}
