<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:param name="urlPrefix"></xsl:param>

  <xsl:output method="xml" indent="yes" encoding="UTF-8"/>

  <xsl:template match="/">
    <rss version="2.0">
      <title>PLoS ONE Alerts</title>
      <link>http://www.plosone.org/</link>
      <image>http://www.ploseone.org/images/pone_favicon.ico</image>
      <description>PLoS ONE Journal</description>
      <channel>
        <xsl:apply-templates select="articles/article"/>
      </channel>
    </rss>
  </xsl:template>

  <xsl:template match="article">
    <item>
      <title><xsl:value-of select="title"/></title>
      <pubDate>
        <xsl:value-of select="format-date(date, '[FNn,*-3], [D01] [MNn,*-3] [Y0001] 00:00:00 GMT')"/>
      </pubDate>
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
