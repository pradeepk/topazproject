<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="text" encoding="UTF-8" media-type="text/plain" indent="no" />
  <xsl:strip-space elements="*" />

  <xsl:template match="/">
PLEASE DO NOT REPLY DIRECTLY TO THIS E-MAIL
For assistance with this alert, email webmaster@plos.org


New articles in PLoS-One

<xsl:for-each select="articles/article">
Published <xsl:value-of select="date"/>:
      
<xsl:value-of select="title"/> --
<xsl:value-of select="description"/>
    by <xsl:for-each select="authors/author"><xsl:value-of select="text()"/> </xsl:for-each>
----
</xsl:for-each>

</xsl:template>
</xsl:stylesheet>
