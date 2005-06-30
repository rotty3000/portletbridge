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

import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import junit.framework.TestCase;

import org.ccil.cowan.tagsoup.Parser;
import org.portletbridge.rewriter.DefaultUrlRewriter;
import org.xml.sax.InputSource;

/**
 * @author JMcCrindle
 */
public class TransformerTest extends TestCase {

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    public void testTransform() throws Exception {
        try {
            TransformerFactory tfactory = TransformerFactory.newInstance();

            Parser parser = new Parser();
            parser.setFeature(Parser.namespacesFeature, false);

            // Does this factory support SAX features?
            if (tfactory.getFeature(SAXSource.FEATURE))
            {
                // If so, we can safely cast.
                SAXTransformerFactory stfactory = 
                    ((SAXTransformerFactory) tfactory);
          
                // A TransformerHandler is a ContentHandler that will listen for
                // SAX events, and transform them to the result.
                // Set up a hanlder for each style sheet. 

                StreamSource streamSource = new StreamSource(new File("./src/xsl/org/portletbridge/xsl/linkrewriter.xsl"));
                Templates templates = stfactory.newTemplates(streamSource);

                Transformer transformer = templates.newTransformer();
                
                transformer.setParameter("rewriter", new DefaultUrlRewriter("/test/test.do", "/mytest/"));

                transformer.transform(new SAXSource(parser,
                        new InputSource(new FileReader(new File(
                        "./src/test/org/portletbridge/xsl/linkrewritertest.xml")))), new StreamResult(new OutputStreamWriter(System.out)));
                
            }
            else
            {
                System.out.println(
                    "Can't do exampleContentHandlerToContentHandler because tfactory is not a SAXTransformerFactory");
            }

        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) throws Exception {
        TransformerTest test = new TransformerTest("LinkRewriterTest");
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
    public TransformerTest(String arg0) {
        super(arg0);
    }

}
