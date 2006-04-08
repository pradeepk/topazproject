<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns="http://www.w3.org/1999/xhtml">

  <xsl:param name="contextPath"/>

  <xsl:template match="fedoraRepository">
    <html xml:lang="en" lang="en">
      <head>
        <title><xsl:value-of select="repositoryName"/></title>
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
        <h1><xsl:value-of select="repositoryName"/></h1>

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

        <table border="1" cellspacing="2" cellpadding="2" align="center">
          <xsl:apply-templates/> 
        </table>

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

  <xsl:template match="repositoryPID | repositoryOAI-identifier">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="repositoryName">
    <tr><td>Repository Name</td><td><xsl:apply-templates/></td></tr>
  </xsl:template>

  <xsl:template match="repositoryBaseURL">
    <tr><td>Repository Base URL</td><td><xsl:apply-templates/></td></tr>
  </xsl:template>

  <xsl:template match="repositoryVersion">
    <tr><td>Repository Version</td><td><xsl:apply-templates/></td></tr>
  </xsl:template>

  <!-- and so on and so forth. just getting lazy about typing up all. so here is
  a catch-all for the rest -->
  <xsl:template match="*">
    <tr><td><xsl:value-of select="name(.)"/></td><td><xsl:apply-templates/></td></tr>
  </xsl:template>

</xsl:stylesheet>
