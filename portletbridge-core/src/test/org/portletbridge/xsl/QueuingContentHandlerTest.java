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

import junit.framework.TestCase;

import org.apache.xerces.impl.xs.opti.DefaultDocument;
import org.apache.xml.utils.DOMBuilder;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

import EDU.oswego.cs.dl.util.concurrent.Callable;

/**
 * @author JMcCrindle
 */
public class QueuingContentHandlerTest extends TestCase {

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    public void testQueue() throws Exception {
        final QueuingContentHandler handler = new QueuingContentHandler(new DefaultHandler());
        handler.startQueuing();
        handler.startDocument();
        handler.startElement("", "a", "a", new AttributesImpl());
        handler.startElement("", "a", "a", new AttributesImpl());
        handler.enqueue(new Callable() {
            public Object call() throws Exception {
                handler.getContentHandler().startElement("", "b", "b", new AttributesImpl());
                return null;
            }
        });
        handler.stopQueuing();
        handler.endElement("", "a", "a");
        handler.endElement("", "a", "a");
        handler.endDocument();
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Constructor for QueuingContentHandlerTest.
     * @param arg0
     */
    public QueuingContentHandlerTest(String arg0) {
        super(arg0);
    }

}
