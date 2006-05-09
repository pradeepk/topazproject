<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 xmlns="http://www.w3.org/1999/xhtml">

 <xsl:param name="contextPath"/>

 <xsl:template match="welcome">
  <html xml:lang="en" lang="en">
   <head>
    <title>Welcome to Fedora Access Demo!</title>
    <!-- 
    NOTE (SM): this meta tag reflects the *output* of the pipeline and not
    the encoding of this file. I agree it's sort of an hack and it should
    be the XHTML serializer to add the meta tag to the response, but, for
    now, this fixes encoding problems in those user-agents that don't parse
    the <?xml?> processing instruction to understand the encoding of the
    stream 
    --> 
    <meta http-equiv="Content-Type" content="text/xhtml; charset=UTF-8"/>
    <link href="{$contextPath}/styles/main.css" type="text/css" rel="stylesheet"/>
    <link href="favicon.ico" rel="SHORTCUT ICON" />
   </head>
   <body>
    <h1>Welcome to Fedora Access Demo!</h1>

    <table border="0" cellspacing="2" cellpadding="2" align="center" width="100%">
     <tr>
      <td nowrap="nowrap" align="right">
       Orthogonal views:
       <a href="?cocoon-view=content">Content</a>
       &#160;
       <a href="?cocoon-view=pretty-content">Pretty content</a>
       &#160;
       <a href="?cocoon-view=links">Links</a>
      </td>
     </tr>
    </table>

    <xsl:apply-templates/> 

    <p class="copyright">
     Copyright © 2006 <a href="http://www.plos.org/">PLoS Public Library
      Of Science</a>. All rights reserved.
    </p>
    <p class="block">
     <a href="http://cocoon.apache.org/"><img src="{$contextPath}/images/powered.gif" alt="Powered by Apache Cocoon"/></a>
    </p>
   </body>
  </html>
 </xsl:template>

 <xsl:template match="message">
  <p class="block"><xsl:apply-templates/></p>
 </xsl:template>

 <xsl:template match="assumptions">
  <h2>Assumptions</h2>
  <ul><xsl:apply-templates/></ul>
 </xsl:template>

 <xsl:template match="assumption">
  <li><xsl:apply-templates/></li>
 </xsl:template>

 <xsl:template match="links">
  <h2>Links</h2>
  <ul><xsl:apply-templates/></ul>
 </xsl:template>

 <xsl:template match="link">
  <li><a href="{@href}"><xsl:apply-templates/></a></li>
 </xsl:template>

</xsl:stylesheet>
