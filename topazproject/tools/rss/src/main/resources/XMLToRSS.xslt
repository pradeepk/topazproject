<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:param name="urlPrefix"></xsl:param>

  <xsl:output method="xml" indent="yes" encoding="UTF-8"/>

  <xsl:template match="/">
    <rss version="2.0">
      <title>PLoS ONE RSS</title>
      <link>http://www.plosone.org/rss/</link>
      <description>RSS channel to monitor for updates</description>
      <channel>
        <xsl:apply-templates select="articles/article"/>
      </channel>
    </rss>
  </xsl:template>

  <xsl:template match="article">
    <item>
      <title><xsl:value-of select="title"/></title>
      <pubDate><xsl:value-of select="date"/></pubDate>
      <link><xsl:value-of select="$urlPrefix"/><xsl:value-of select="uri"/></link>
      <description><xsl:value-of select="description"/></description>
      <xsl:apply-templates select="authors/author"/>
      <guid><xsl:value-of select="uri"/></guid>
    </item>
  </xsl:template>

  <xsl:template match="author">
    <author><xsl:value-of select="."/></author>
  </xsl:template>

</xsl:stylesheet>
