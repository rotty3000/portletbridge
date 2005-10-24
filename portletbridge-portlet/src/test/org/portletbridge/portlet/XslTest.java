package org.portletbridge.portlet;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import junit.framework.TestCase;

public class XslTest extends TestCase {
    public void testXsl() throws TransformerException {
        TransformerFactory tfactory = TransformerFactory.newInstance();
        Transformer transformer = tfactory.newTransformer(new StreamSource(new StringReader("" +
                "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">" +
                "   <xsl:template match=\"//@src\">" +
                "   <src><xsl:value-of select=\".\"/></src>" +
                "   </xsl:template>" +
                "</xsl:stylesheet>")));
        StringWriter writer = new StringWriter();
        transformer.transform(new StreamSource(new StringReader("<HTML>" +
                "<HEAD></HEAD>" +
                "<BODY>" +
                "<FORM>" +
                "<INPUT type=\"image\" src=\"blah\"/>" +
                "<INPUT type=\"text\" src=\"blah\"/>" +
                "</FORM>" +
                "</BODY>" +
                "</HTML>")), new StreamResult(writer));
        writer.flush();
        System.out.println(writer.getBuffer().toString());
        
    }
}
