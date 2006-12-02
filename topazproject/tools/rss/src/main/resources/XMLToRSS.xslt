<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:param name="urlPrefix"></xsl:param>

  <xsl:output method="xml" indent="yes" encoding="UTF-8"/>

  <xsl:template match="/">
    <rss version="2.0">
      <channel>
        <xsl:apply-templates select="articles/article"/>
      </channel>
    </rss>
  </xsl:template>

  <xsl:template match="article">
    <item>
      <title><xsl:value-of select="title"/></title>
      <link><xsl:value-of select="$urlPrefix"/><xsl:value-of select="uri"/></link>
      <pubDate><xsl:value-of select="date"/></pubDate>
      <xsl:apply-templates select="authors/author"/>
    </item>
  </xsl:template>

  <xsl:template match="author">
    <author><xsl:value-of select="."/></author>
  </xsl:template>

</xsl:stylesheet>
