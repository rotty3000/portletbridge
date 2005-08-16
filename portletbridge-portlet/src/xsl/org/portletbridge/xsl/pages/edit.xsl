<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:java="http://xml.apache.org/xslt/java"
    exclude-result-prefixes="java">

  <xsl:output method="xml" version="1.0" indent="yes"/>

  <xsl:param name="portlet"/>
  
  <xsl:template match="/">

  <form>
    <xsl:attribute name="action"><xsl:value-of select="java:actionUrl($portlet)"/></xsl:attribute>
    <table>
    <tr>
        <td>Initial URL</td>
	  	<td>
	  		<input type="text" name="initUrl">
	  		<xsl:attribute name="value"><xsl:value-of select="java:preference($portlet, 'initUrl', '')"/></xsl:attribute>
	  		</input>
	  	</td>
  	</tr>
    <tr>
        <td>Scope (Regex)</td>
	  	<td>
	  		<input type="text" name="scope">
	  		<xsl:attribute name="value"><xsl:value-of select="java:preference($portlet, 'scope', '.*')"/></xsl:attribute>
	  		</input>
	  	</td>
  	</tr>
  	<tr><td colspan="2" align="right"><input type="submit"/></td></tr>
  	</table>
  </form>  
  
  </xsl:template>

</xsl:stylesheet>
