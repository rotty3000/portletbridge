<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns:portlet="java:org.portletbridge.portlet.PortletFunctions"
   extension-element-prefixes="portlet">

   <xsl:output method="html"
      encoding="UTF-8"
      indent="yes"
      standalone="no"
      omit-xml-declaration="yes"/>

   <xsl:param name="portlet"/>

   <!-- Fetch some info from head, and all of body -->
   <xsl:template match="/HTML">
      <xsl:apply-templates select="HEAD"/>
      <xsl:apply-templates select="BODY"/>
   </xsl:template>

   <xsl:template match="/HTML/HEAD">
      <xsl:apply-templates select="STYLE"/>
      <xsl:apply-templates select="LINK[@rel='stylesheet']"/>
      <xsl:apply-templates select="SCRIPT"/>
   </xsl:template>

   <xsl:template match="/HTML/BODY">
      <xsl:apply-templates select="node()"/>
   </xsl:template>

   <!-- Rewrite links -->
   <xsl:template match="A/@href">
      <xsl:attribute name="href">
         <xsl:value-of select="portlet:link($portlet, .)"/>
      </xsl:attribute>
   </xsl:template>

   <xsl:template match="A/@onclick">
      <xsl:attribute name="onclick">
         <xsl:value-of select="portlet:script($portlet, .)"/>
      </xsl:attribute>
   </xsl:template>

   <!-- Rewrite image references -->
   <xsl:template match="IMG/@src">
      <xsl:attribute name="src">
         <xsl:value-of select="portlet:link($portlet, .)"/>
      </xsl:attribute>
   </xsl:template>

   <!-- Rewrite imagemap references -->
   <xsl:template match="MAP/AREA/@href">
      <xsl:attribute name="href">
         <xsl:value-of select="portlet:link($portlet, .)"/>
      </xsl:attribute>
   </xsl:template>

   <!-- Copy style tags from head -->
   <xsl:template match="/HTML/HEAD/STYLE">
      <xsl:copy>
         <xsl:apply-templates select="@*"/>

         <xsl:value-of select="portlet:style($portlet, .)"/>
      </xsl:copy>
   </xsl:template>

   <!-- Copy script tags from head -->
   <xsl:template match="/HTML/HEAD/SCRIPT[@src]">
      <xsl:copy>
         <xsl:apply-templates select="@*"/>
         <xsl:attribute name="src">
            <xsl:value-of select="portlet:link($portlet, @src)"/>
         </xsl:attribute>
      </xsl:copy>
   </xsl:template>

   <xsl:template match="/HTML/HEAD/SCRIPT/text()">
      <xsl:value-of select="portlet:script($portlet, .)"/>
   </xsl:template>

   <!-- Convert link tags in head to style tags -->
   <xsl:template match="/HTML/HEAD/LINK">
      <style type="text/css">@import "
         <xsl:value-of select="portlet:link($portlet, @href)"/>";
      </style>
   </xsl:template>

   <xsl:template match="FORM/@action">
      <xsl:attribute name="action">
         <xsl:value-of select="portlet:link($portlet, .)"/>
      </xsl:attribute>
   </xsl:template>

   <!-- Identity template -->
   <xsl:template match="@*|*">
      <xsl:copy>
         <xsl:apply-templates select="@*|node()"/>
      </xsl:copy>
   </xsl:template>
</xsl:stylesheet>
