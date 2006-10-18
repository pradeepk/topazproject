<xsl:stylesheet version="2.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:my="my:ingest.pmc#"
    exclude-result-prefixes="my">

  <!-- This stylesheet must be xsl:include'd - it will not run standalone! -->

  <!--
     - Validate a PMC document. There is no result from these templates; instead, a fatal
     - message is generated if a problem is found.
     -
     - Checks currently performed:
     -  * main article xml must exist
     -  * all links in the article must be absolute, and if a doi and that has the article doi
     -    as a prefix then it must point to an entry in the zip
     -  * all entries in the zip must be pointed to by links in the article (modulo
     -    representations)
     -  * if the zip-format is 'AP', then all entries must have the same prefix with the
     -    article being named prefix.xml; otherwise the entries must be DOI's
    -->

  <!-- Main entry point for validation. This invokes the individual checks. -->
  <xsl:template name="validate-pmc" as="empty-sequence()">
    <xsl:call-template name="validate-zip"/>

    <xsl:call-template name="validate-links"/>

    <xsl:call-template name="validate-entries"/>
  </xsl:template>

  <!-- validate the structure of the zip:
     - 1. Need an article
     - 2. In AP format, secondary entries must share prefix with article
     -->
  <xsl:template name="validate-zip" as="empty-sequence()">
    <xsl:if test="not($article-entry)">
      <xsl:message>No article entry found in zip file</xsl:message>
    </xsl:if>

    <xsl:if test="$zip-fmt = 'AP'">
      <xsl:variable name="prefix" as="xs:string"
          select="my:get-root(my:basename($article-entry/@name))"/>

      <xsl:for-each select="$file-entries/@name">
        <xsl:if test="not(starts-with(my:basename(.), $prefix))">
          <xsl:message>Zip entry '<xsl:value-of select="."/>' does not have same prefix as article ('<xsl:value-of select="$prefix"/>')</xsl:message>
        </xsl:if>
      </xsl:for-each>
    </xsl:if>
  </xsl:template>

  <!-- validate xlink:href links. This involves:
     -   * The URL must be absolute
     -   * if the URL uses the doi scheme, and the current article doi is a prefix of the doi,
     -     then make sure the thing it points to is part of the ingest (i.e. in the zip)
     -->
  <xsl:template name="validate-links" as="empty-sequence()">
    <xsl:for-each select="$fixed-article//@xlink:href">
      <xsl:choose>
        <xsl:when test="my:uri-is-absolute(.)">
          <xsl:if test="my:is-internal-link(.)">
            <xsl:call-template name="check-presence">
              <xsl:with-param name="doi" select="my:uri-to-doi(.)"/>
            </xsl:call-template>
          </xsl:if>
        </xsl:when>

        <xsl:otherwise>
          <xsl:message>link '<xsl:value-of select="."/>' is not absolute</xsl:message>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:for-each>
  </xsl:template>

  <!-- check that an object for the given doi exists in the zip. -->
  <xsl:template name="check-presence" as="empty-sequence()">
    <xsl:param name="doi" as="xs:string"/>

    <xsl:if test="not($file-entries[my:fname-to-doi(@name) = $doi])">
      <xsl:message>No entry found in zip file for doi '<xsl:value-of select="$doi"/>'</xsl:message>
    </xsl:if>
  </xsl:template>

  <!-- Check that all entries in the zip are referenced (no orphans). -->
  <xsl:template name="validate-entries" as="empty-sequence()">
    <xsl:variable name="refs" as="xs:string*"
        select="for $uri in $fixed-article//@xlink:href[my:is-internal-link(.)] return my:uri-to-doi($uri)"/>

    <xsl:for-each select="$file-entries[not(. is $article-entry)]">
      <xsl:variable name="edoi" as="xs:string" select="my:fname-to-doi(@name)"/>
      <xsl:if test="$edoi != $article-doi and not($edoi = $refs)">
        <xsl:message>Found unreferenced entry in zip file: '<xsl:value-of select="@name"/>'</xsl:message>
      </xsl:if>
    </xsl:for-each>
  </xsl:template>


  <!-- Helper funtions -->

  <!-- check if the link is (supposed to be) a link to something in the zip -->
  <xsl:function name="my:is-internal-link" as="xs:boolean">
    <xsl:param name="href" as="xs:string"/>
    <xsl:sequence
        select="starts-with(my:urldecode($href), my:urldecode(my:doi-to-uri($article-doi)))"/>
  </xsl:function>
</xsl:stylesheet>
