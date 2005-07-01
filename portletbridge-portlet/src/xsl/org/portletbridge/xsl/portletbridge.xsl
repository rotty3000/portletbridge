<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:java="http://xml.apache.org/xslt/java"
    exclude-result-prefixes="java">

<xsl:output method="xml" version="1.0" indent="yes"/>

  <xsl:param name="portletrewriter"/>
  <xsl:param name="servletrewriter"/>

  <xsl:param name="request"/>
  <xsl:param name="response"/>

  <!-- make sure '<' is not printed as '&lt;' -->

  <!-- copy input to output -->
  <xsl:template match='*|@*'>
    <xsl:copy>
      <xsl:apply-templates select='node()|@*'/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match='/'>
      <xsl:apply-templates select='/HTML/HEAD/TITLE'/>
      <xsl:apply-templates select='/HTML/BODY'/>
  </xsl:template>
  
  <xsl:template match='/HTML/HEAD/TITLE'>
      <xsl:value-of select="java:setTitle($response, text())"/>
  </xsl:template>

  <xsl:template match='/HTML/BODY'>
      <xsl:apply-templates select='node()|@*'/>
  </xsl:template>
  
  <!-- rewrite <a href> -->
  <xsl:template match="@href">
    <xsl:attribute name="href"><xsl:value-of select="java:rewrite($servletrewriter,.)"/></xsl:attribute>
  </xsl:template>

  <xsl:template match="A[@href]">
    <xsl:copy>
      <xsl:attribute name="href"><xsl:value-of select="java:rewrite($portletrewriter,@href)"/></xsl:attribute>
      <xsl:apply-templates select="node()|@*[name(.)!='href']"/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="@src">
      <xsl:attribute name="src"><xsl:value-of select="java:rewrite($servletrewriter,.)"/></xsl:attribute>
  </xsl:template>

  <xsl:template match="@action">
    <xsl:attribute name="action"><xsl:value-of select="java:rewrite($portletrewriter,.)"/></xsl:attribute>
  </xsl:template>

  <xsl:template match="FORM">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:attribute name="method">post</xsl:attribute>
      <input type="hidden" name="__method" value="post"/>
      <xsl:apply-templates select="node()"/>
    </xsl:copy>
  </xsl:template>
  
</xsl:stylesheet>
