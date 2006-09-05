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
    -->

  <!-- Main entry point for validation. This invokes the individual checks. -->
  <xsl:template name="validate-pmc" as="empty-sequence()">
    <xsl:call-template name="validate-zip"/>

    <xsl:call-template name="validate-links"/>

    <xsl:call-template name="validate-entries"/>
  </xsl:template>

  <!-- validate the structure of the zip.
     -->
  <xsl:template name="validate-zip" as="empty-sequence()">
    <xsl:if test="not($pmc-entry)">
      <xsl:message>No 'pmc.xml' found in zip file</xsl:message>
    </xsl:if>
  </xsl:template>

  <!-- validate xlink:href links. This involves:
     -   * if the URL is absolute, uses the doi scheme, and the current article doi is a
     -     prefix of the doi, then make sure the thing it points to is part of the ingest
     -     (i.e. in the zip)
     -   * if the URL is relative, make sure the thing it points to is part of the ingest
     -     (i.e. in the zip)
     -->
  <xsl:template name="validate-links" as="empty-sequence()">
    <xsl:for-each select="$article//@xlink:href">
      <xsl:variable name="dec-uri" as="xs:string" select="my:urldecode(.)"/>
      <xsl:choose>
        <xsl:when test="my:uri-is-absolute($dec-uri)">
          <xsl:if test="starts-with($dec-uri, concat('info:doi/', $doi))">
            <xsl:call-template name="check-presence">
              <xsl:with-param name="uri" select="$dec-uri"/>
            </xsl:call-template>
          </xsl:if>
          <xsl:if test="starts-with($dec-uri, concat('doi:', $doi))">
            <xsl:call-template name="check-presence">
              <xsl:with-param name="uri"
                  select="concat('info:doi/', substring-after($dec-uri, ':'))"/>
            </xsl:call-template>
          </xsl:if>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="check-presence">
            <xsl:with-param name="uri"     select="my:resolve-relative-doi($doi, $dec-uri)"/>
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:for-each>
  </xsl:template>

  <!-- check that an object for the given uri exists in the zip. The uri must be of the
     - form info:doi/<doi>
     -->
  <xsl:template name="check-presence" as="empty-sequence()">
    <xsl:param name="uri" as="xs:string"/>
    <xsl:variable name="doi" as="xs:string" select="substring-after($uri, '/')"/>

    <xsl:if test="not($file-entries[my:fname-to-doi(@name) = $doi])">
      <xsl:message>No entry found in zip file for doi <xsl:value-of select="$doi"/></xsl:message>
    </xsl:if>
  </xsl:template>

  <!-- Check that all entries in the zip are referenced (no orphans).
     -->
  <xsl:template name="validate-entries" as="empty-sequence()">
    <xsl:variable name="refs" as="xs:string*"
        select="my:hrefs-to-doi($article//@xlink:href, $doi)"/>

    <xsl:for-each select="$file-entries[. != $pmc-entry]">
      <xsl:variable name="edoi" as="xs:string" select="my:fname-to-doi(@name)"/>
      <xsl:if test="$edoi != $doi and not($edoi = $refs)">
        <xsl:message>Found unreferenced entry in zip file: '<xsl:value-of select="@name"/>'</xsl:message>
      </xsl:if>
    </xsl:for-each>
  </xsl:template>


  <!-- Helper funtions -->

  <!-- Check if the URI is absolute -->
  <xsl:function name="my:uri-is-absolute" as="xs:boolean">
    <xsl:param name="uri" as="xs:string"/>
    <xsl:copy-of select="matches($uri, '^[^:/?#]+:')"/>
  </xsl:function>

  <!-- Attempt to resolve a relative DOI to a full URI. The rules for this are not
     - defined anywhere, so this is an ad-hoc method.
     -->
  <xsl:function name="my:resolve-relative-doi" as="xs:string">
    <xsl:param name="base" as="xs:string"/>
    <xsl:param name="rdoi" as="xs:string"/>
    <xsl:variable name="abs-doi" as="xs:string"
      select="if (matches($rdoi, '^10\.[^./]+/')) then $rdoi
              else concat(substring-before($base, '/'), '/', $rdoi)"/>
    <xsl:value-of select="concat('info:doi/', $abs-doi)"/>
  </xsl:function>

  <!-- remove any extension from the filename -->
  <xsl:function name="my:hrefs-to-doi" as="xs:string*">
    <xsl:param name="hrefs" as="xs:string*"/>
    <xsl:param name="base"  as="xs:string"/>
    <xsl:for-each select="$hrefs">
      <xsl:value-of
        select="if (starts-with(., 'doi:')) then substring-after(., ':')
                else if (starts-with(., 'info:doi/')) then substring-after(., '/')
                else my:resolve-relative-doi($base, .)"/>
    </xsl:for-each>
  </xsl:function>
</xsl:stylesheet>
