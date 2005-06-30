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

import java.util.LinkedList;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import EDU.oswego.cs.dl.util.concurrent.Callable;

/**
 * @author JMcCrindle
 */
public class QueuingContentHandler implements ContentHandler {

    private LinkedList events = null;
    private ContentHandler contentHandler = null;
    private boolean queuing = false;
    private Object lock = new Object();
    
    public QueuingContentHandler(ContentHandler contentHandler) {
        this.contentHandler = contentHandler;
    }

    public void enqueue(Callable callable) throws SAXException {
        synchronized(lock) {
            if(queuing) {
                events.addLast(callable);
            } else {
                run(callable);
            }
        }
    }
    
    public void run(Callable callable) throws SAXException {
        synchronized(lock) {
            try {
                callable.call();
            } catch (SAXException e) {
                throw e;
            } catch (Exception e) {
                throw new SAXException(e);
            }
        }
    }
    
    public void stopQueuing() throws SAXException {
        synchronized(lock) {
            while(events.size() > 0) {
                Callable callable = (Callable) events.removeFirst();
                run(callable);
            }
            queuing = false;
        }
    }
    
    public void startQueuing() throws SAXException {
        synchronized(lock) {
            queuing = true;
        }
    }
    
    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#startDocument()
     */
    public void startDocument() throws SAXException {
        synchronized(lock) {
            events = new LinkedList();
        }
        enqueue(new Callable() {
            public Object call() throws Exception {
                contentHandler.startDocument();
                return null;
            }
        });
    }

    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#endDocument()
     */
    public void endDocument() throws SAXException {
        enqueue(new Callable() {
            public Object call() throws Exception {
                contentHandler.endDocument();
                return null;
            }
        });
        synchronized(lock) {
            if(queuing) {
                stopQueuing();
                startQueuing();
            }
        }
    }

    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    public void characters(char[] ch, int start, final int length) throws SAXException {
        final char[] copy = new char[length];
        System.arraycopy(ch, start, copy, 0, length);
        enqueue(new Callable() {
            public Object call() throws Exception {
                contentHandler.characters(copy, 0, length);
                return null;
            }
        });
    }

    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#ignorableWhitespace(char[], int, int)
     */
    public void ignorableWhitespace(char[] ch, int start, final int length) throws SAXException {
        final char[] copy = new char[length];
        System.arraycopy(ch, start, copy, 0, length);
        enqueue(new Callable() {
            public Object call() throws Exception {
                contentHandler.ignorableWhitespace(copy, 0, length);
                return null;
            }
        });
    }

    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#endPrefixMapping(java.lang.String)
     */
    public void endPrefixMapping(final String prefix) throws SAXException {
        enqueue(new Callable() {
            public Object call() throws Exception {
                contentHandler.endPrefixMapping(prefix);
                return null;
            }
        });
    }

    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#skippedEntity(java.lang.String)
     */
    public void skippedEntity(final String name) throws SAXException {
        enqueue(new Callable() {
            public Object call() throws Exception {
                contentHandler.skippedEntity(name);
                return null;
            }
        });
    }

    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#setDocumentLocator(org.xml.sax.Locator)
     */
    public void setDocumentLocator(Locator locator) {
        contentHandler.setDocumentLocator(locator);
    }

    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#processingInstruction(java.lang.String, java.lang.String)
     */
    public void processingInstruction(final String target, final String data) throws SAXException {
        enqueue(new Callable() {
            public Object call() throws Exception {
                contentHandler.processingInstruction(target, data);
                return null;
            }
        });
    }

    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#startPrefixMapping(java.lang.String, java.lang.String)
     */
    public void startPrefixMapping(final String prefix, final String uri) throws SAXException {
        enqueue(new Callable() {
            public Object call() throws Exception {
                contentHandler.startPrefixMapping(prefix, uri);
                return null;
            }
        });
    }

    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
     */
    public void endElement(final String namespaceURI, final String localName, final String qName) throws SAXException {
        enqueue(new Callable() {
            public Object call() throws Exception {
                contentHandler.endElement(namespaceURI, localName, qName);
                return null;
            }
        });
    }

    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    public void startElement(final String namespaceURI, final String localName, final String qName, final Attributes atts) throws SAXException {
        enqueue(new Callable() {
            public Object call() throws Exception {
                contentHandler.startElement(namespaceURI, localName, qName, atts);
                return null;
            }
        });
    }

    public ContentHandler getContentHandler() {
        return contentHandler;
    }
}
