<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:java="http://xml.apache.org/xslt/java"
    exclude-result-prefixes="java">

<xsl:output method="xml" version="1.0" indent="yes"/>

  <xsl:param name="test"/>
  <xsl:variable name="newtest" select="java:java.util.Date.new(1000000000)"/>

  <xsl:template match="/">
  	<xsl:apply-templates select="/html/body"/>
  </xsl:template>
  
  <xsl:template match="/html/body">
    <html>
    <head><title><xsl:value-of select="java:toString($test)"/></title></head>
  	<xsl:copy>
	  	<xsl:copy-of select="*|@*"/>
	</xsl:copy>
	</html>
  </xsl:template>

</xsl:stylesheet>
