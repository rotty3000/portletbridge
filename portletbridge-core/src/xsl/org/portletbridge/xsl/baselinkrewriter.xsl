<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:java="http://xml.apache.org/xslt/java"
    exclude-result-prefixes="java">

<xsl:output method="xml" version="1.0" indent="yes"/>

  <xsl:variable name="baseurl" select="java:org.portletbridge.UrlUtils.getPath(/html/head/base/@href)"/>

  <!-- copy input to output -->
  <xsl:template match='*|@*'>
    <xsl:copy>
      <xsl:apply-templates select='node()|@*'/>
    </xsl:copy>
  </xsl:template>
  
  <!-- rewrite <a href> -->
  <xsl:template match="a[@href]|link[@href]|area[@href]">
    <xsl:copy>
      <xsl:attribute name="href"><xsl:value-of select="java:org.portletbridge.UrlUtils.rewriteRelative($baseurl,@href)"/></xsl:attribute>
      <xsl:apply-templates select="node()|@*[name(.)!='href']"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="*[@src]">
    <xsl:copy>
      <xsl:attribute name="src"><xsl:value-of select="java:org.portletbridge.UrlUtils.rewriteRelative($baseurl,@src)"/></xsl:attribute>
      <xsl:apply-templates select="node()|@*[name(.)!='src']"/>
    </xsl:copy>
  </xsl:template>

  <!-- rewrite <a href> -->
  <xsl:template match="base">
  </xsl:template>

</xsl:stylesheet>
