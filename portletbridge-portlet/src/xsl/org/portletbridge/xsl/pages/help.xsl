<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:java="http://xml.apache.org/xslt/java"
    exclude-result-prefixes="java">

  <xsl:output 
      method="html" 
      version="1.0" 
      indent="yes"
      omit-xml-declaration="yes"/>

  <xsl:param name="portlet"/>
  
  <xsl:template match="/">
  
  See <a href="http://www.portletbridge.org">Portlet Bridge</a> for more information.
  
  </xsl:template>
   
</xsl:stylesheet>
