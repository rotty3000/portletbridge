package org.portletbridge.portlet;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.StringWriter;
import java.net.URL;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import junit.framework.TestCase;

import org.cyberneko.html.parsers.SAXParser;
import org.portletbridge.mock.MockBridgeFunctions;
import org.xml.sax.InputSource;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

public class PortletBridgeXslTest extends TestCase {
    public void testXsl() throws TransformerException, SAXNotRecognizedException, SAXNotSupportedException, FileNotFoundException {
        TransformerFactory tfactory = TransformerFactory.newInstance();
        URL resource = this.getClass().getResource("/org/portletbridge/xsl/default.xsl");
        Transformer transformer = tfactory.newTransformer(new StreamSource(resource.toExternalForm()));
        StringWriter writer = new StringWriter();
        SAXParser parser = new SAXParser();
        transformer.setParameter("bridge", new MockBridgeFunctions());
        FileReader inputReader = new FileReader("./src/test/org/portletbridge/portlet/slashdot.html");
        assertNotNull(inputReader);
        transformer.transform(new SAXSource(parser, new InputSource(inputReader)), new StreamResult(writer));
        writer.flush();
        System.out.println(writer.getBuffer().toString());
        
    }
}
