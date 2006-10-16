<xsl:stylesheet version="2.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:my="my:ingest.pmc#"
    exclude-result-prefixes="my">

  <!--
    - Convert a ZipInfo (zip.dtd) to an ObjectList (fedora.dtd). This contains the main 
    - object generation logic for ingest.
    -
    - This converter handles zip's according to TOPAZ's specs and zip's from AP, both of
    - which use PMC 2.0 for the main article description.
    -->

  <xsl:include href="validate_pmc.xslt"/>

  <xsl:output method="xml" omit-xml-declaration="yes" indent="yes"/>

  <xsl:param name="is_update"       select="false()" as="xs:boolean"/>

  <xsl:variable name="file-entries" select="/ZipInfo/ZipEntry[not(@isDirectory)]"
      as="element(ZipEntry)*"/>
  <xsl:variable name="pmc-entry"    select="my:find-pmc-xml(/ZipInfo)"
      as="element(ZipEntry)"/>
  <xsl:variable name="article"      select="document($pmc-entry/@name, .)/article"
      as="element(article)"/>
  <xsl:variable name="meta"         select="$article/front/article-meta"
      as="element(article-meta)"/>
  <xsl:variable name="doi"          select="$meta/article-id[@pub-id-type = 'doi']"
      as="xs:string"/>
  <xsl:variable name="zip-fmt"
      select="if (my:basename($pmc-entry/@name) = 'pmc.xml') then 'TPZ' else 'AP'"
      as="xs:string"/>

  <xsl:variable name="sec-dois"
      select="distinct-values(my:fname-to-doi($file-entries[my:is-secondary(@name)]/@name))"
      as="xs:string*"/>
  <xsl:variable name="sec-obj-refs" select="for $doi in $sec-dois return my:links-for-doi($doi)[1]"
      as="element()*"/>

  <!-- top-level template - do some checks, and then run the production templates -->
  <xsl:template match="/">
    <xsl:call-template name="validate-pmc"/>

    <xsl:apply-templates/>
  </xsl:template>

  <!-- generate the ObjectList -->
  <xsl:template match="/ZipInfo">
    <ObjectList logMessage="Ingest of article '{$meta/title-group/article-title}'"
                articleId="{$doi}">
      <xsl:call-template name="main-entry"/>

      <xsl:for-each-group select="$file-entries[my:is-secondary(@name)]"
                          group-by="my:fname-to-doi(@name)">
        <xsl:apply-templates select="." mode="sec"/>
      </xsl:for-each-group>

      <xsl:for-each 
          select="$meta/article-categories/subj-group[@subj-group-type = 'Discipline']/subject">
        <xsl:call-template name="cat-aux"/>
      </xsl:for-each>
    </ObjectList>
  </xsl:template>

  <!-- templates for the main (pmc) entry -->

  <!-- generate the object and rdf for the article -->
  <xsl:template name="main-entry">
    <xsl:variable name="rdf" as="element()*">
      <xsl:call-template name="main-rdf"/>
    </xsl:variable>

    <Object pid="{my:doi-to-pid($doi)}" cModel="PlosArticle">
      <DC xmlns:dc="http://purl.org/dc/elements/1.1/">
        <xsl:sequence select="my:filter-dc($rdf, true())"/>
      </DC>
      <RELS-EXT xmlns:topaz="http://rdf.topazproject.org/RDF/"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:dc_terms="http://purl.org/dc/terms/">
        <xsl:sequence select="my:filter-dt(my:filter-dc($rdf, false()))"/>
      </RELS-EXT>
      <xsl:call-template name="main-ds"/>
    </Object>

    <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
             xmlns:topaz="http://rdf.topazproject.org/RDF/"
             xmlns:dc="http://purl.org/dc/elements/1.1/"
             xmlns:dc_terms="http://purl.org/dc/terms/">
      <rdf:Description rdf:about="{my:doi-to-uri($doi)}">
        <xsl:sequence select="$rdf"/>
      </rdf:Description>
    </rdf:RDF>
  </xsl:template>

  <!-- generate the rdf statements for the article -->
  <xsl:template name="main-rdf" xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:dc_terms="http://purl.org/dc/terms/"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:topaz="http://rdf.topazproject.org/RDF/">
    <rdf:type rdf:resource="http://rdf.topazproject.org/RDF/Article"/>

    <dc:identifier><xsl:value-of select="concat('info:doi/', $doi)"/></dc:identifier>
    <dc:title><xsl:value-of select="$meta/title-group/article-title"/></dc:title>
    <dc:type rdf:resource="http://purl.org/dc/dcmitype/Text"/>
    <dc:format>text/xml</dc:format>
    <dc:language>en</dc:language>
    <xsl:if test="$meta/pub-date">
      <dc:date rdf:datatype="http://www.w3.org/2001/XMLSchema#date"><xsl:value-of select="my:format-date(my:select-date($meta/pub-date))"/></dc:date>
      <dc_terms:issued rdf:datatype="http://www.w3.org/2001/XMLSchema#date"><xsl:value-of select="my:format-date(my:select-date($meta/pub-date))"/></dc_terms:issued>
      <dc_terms:available rdf:datatype="http://www.w3.org/2001/XMLSchema#date"><xsl:value-of select="my:format-date(my:select-date($meta/pub-date))"/></dc_terms:available>
    </xsl:if>
    <xsl:if test="$meta/history/date[@date-type = 'received']">
      <dc_terms:dateSubmitted rdf:datatype="http://www.w3.org/2001/XMLSchema#date"><xsl:value-of select="my:format-date($meta/history/date[@date-type = 'received'])"/></dc_terms:dateSubmitted>
    </xsl:if>
    <xsl:if test="$meta/history/date[@date-type = 'accepted']">
      <dc_terms:dateAccepted rdf:datatype="http://www.w3.org/2001/XMLSchema#date"><xsl:value-of select="my:format-date($meta/history/date[@date-type = 'accepted'])"/></dc_terms:dateAccepted>
    </xsl:if>
    <xsl:for-each select="$meta/contrib-group/contrib[@contrib-type = 'author']">
      <dc:creator><xsl:value-of select="my:format-name(.)"/></dc:creator>
    </xsl:for-each>
    <xsl:for-each select="$meta/contrib-group/contrib[@contrib-type = 'contributor']">
      <dc:contributor><xsl:value-of select="my:format-name(.)"/></dc:contributor>
    </xsl:for-each>
    <xsl:for-each
        select="$meta/article-categories/subj-group[@subj-group-type = 'Discipline']/subject">
      <dc:subject><xsl:value-of select="."/></dc:subject>
    </xsl:for-each>
    <xsl:if test="$meta/abstract">
      <dc:description rdf:parseType="Literal"><xsl:copy-of select="my:select-abstract($meta/abstract)/node()"/></dc:description>
    </xsl:if>
    <xsl:if test="$article/front/journal-meta/publisher">
      <dc:publisher><xsl:value-of select="$article/front/journal-meta/publisher/publisher-name"/></dc:publisher>
    </xsl:if>
    <xsl:if test="$meta/copyright-statement">
      <dc:rights><xsl:value-of select="normalize-space($meta/copyright-statement)"/></dc:rights>
    </xsl:if>

    <xsl:for-each select="$sec-dois">
      <dc_terms:hasPart rdf:resource="{my:doi-to-uri(.)}"/>
    </xsl:for-each>

    <xsl:for-each 
        select="$meta/article-categories/subj-group[@subj-group-type = 'Discipline']/subject">
      <topaz:hasCategory
        rdf:resource="{my:doi-to-uri(my:doi-to-aux($doi, 'category', position()))}"/>
    </xsl:for-each>

    <xsl:if test="$sec-obj-refs">
      <topaz:nextObject rdf:resource="{my:doi-to-uri(my:link-to-doi($sec-obj-refs[1]/@xlink:href))}"/>
    </xsl:if>

    <xsl:apply-templates select="$file-entries[my:is-main(@name)]" mode="ds-rdf"/>
  </xsl:template>

  <!-- generate the object's datastream definitions for the article -->
  <xsl:template name="main-ds">
    <xsl:apply-templates select="$file-entries[my:is-main(@name)]" mode="ds"/>
  </xsl:template>

  <!-- generate the auxiliary object definitions (objects not directly present in the pmc) -->
  <xsl:template name="cat-aux">
    <xsl:variable name="rdf" as="element()*">
      <xsl:call-template name="cat-aux-rdf"/>
    </xsl:variable>

    <xsl:variable name="cat-pid" as="xs:string"
        select="my:doi-to-pid(my:doi-to-aux($doi, 'category', position()))"/>

    <Object pid="{$cat-pid}" cModel="PlosCategory">
      <DC xmlns:dc="http://purl.org/dc/elements/1.1/">
        <xsl:sequence select="my:filter-dc($rdf, true())"/>
      </DC>
      <RELS-EXT xmlns:topaz="http://rdf.topazproject.org/RDF/"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:dc_terms="http://purl.org/dc/terms/">
        <xsl:sequence select="my:filter-dt(my:filter-dc($rdf, false()))"/>
      </RELS-EXT>
    </Object>

    <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
             xmlns:topaz="http://rdf.topazproject.org/RDF/"
             xmlns:dc="http://purl.org/dc/elements/1.1/"
             xmlns:dc_terms="http://purl.org/dc/terms/">
      <rdf:Description rdf:about="{my:pid-to-uri($cat-pid)}">
        <xsl:sequence select="$rdf"/>
      </rdf:Description>
    </rdf:RDF>
  </xsl:template>

  <!-- generate the rdf statements for an auxiliary object -->
  <xsl:template name="cat-aux-rdf" xmlns:topaz="http://rdf.topazproject.org/RDF/">
    <xsl:variable name="main-cat" as="xs:string"
        select="if (contains(., '/')) then substring-before(., '/') else ."/>
    <xsl:variable name="sub-cat" as="xs:string" select="substring-after(., '/')"/>

    <topaz:mainCategory><xsl:value-of select="$main-cat"/></topaz:mainCategory>
    <xsl:if test="$sub-cat">
      <topaz:subCategory><xsl:value-of select="$sub-cat"/></topaz:subCategory>
    </xsl:if>
  </xsl:template>

  <!-- templates for all secondary entries -->

  <!-- generate the object and rdf for a secondary object -->
  <xsl:template match="ZipEntry" mode="sec">
    <xsl:variable name="sdoi" select="my:fname-to-doi(@name)"/>
    <xsl:variable name="rdf" as="element()*">
      <xsl:call-template name="sec-rdf"/>
    </xsl:variable>


    <Object pid="{my:doi-to-pid($sdoi)}" cModel="PlosArticleSecObj">
      <DC xmlns:dc="http://purl.org/dc/elements/1.1/">
        <xsl:sequence select="my:filter-dc($rdf, true())"/>
      </DC>
      <RELS-EXT xmlns:topaz="http://rdf.topazproject.org/RDF/"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:dc_terms="http://purl.org/dc/terms/">
        <xsl:sequence select="my:filter-dt(my:filter-dc($rdf, false()))"/>
      </RELS-EXT>
      <xsl:call-template name="sec-ds"/>
    </Object>

    <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
             xmlns:topaz="http://rdf.topazproject.org/RDF/"
             xmlns:dc="http://purl.org/dc/elements/1.1/"
             xmlns:dc_terms="http://purl.org/dc/terms/">
      <rdf:Description rdf:about="{my:doi-to-uri($sdoi)}">
        <xsl:sequence select="$rdf"/>
      </rdf:Description>
    </rdf:RDF>
  </xsl:template>

  <!-- generate the rdf statements for the secondary object -->
  <xsl:template name="sec-rdf" xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:dc_terms="http://purl.org/dc/terms/"
                xmlns:topaz="http://rdf.topazproject.org/RDF/">
    <xsl:variable name="sdoi" select="my:fname-to-doi(@name)"/>

    <dc:identifier><xsl:value-of select="concat('info:doi/', $sdoi)"/></dc:identifier>
    <xsl:if test="$meta/pub-date">
      <dc:date rdf:datatype="http://www.w3.org/2001/XMLSchema#date"><xsl:value-of select="my:format-date(my:select-date($meta/pub-date))"/></dc:date>
    </xsl:if>
    <xsl:for-each select="$meta/contrib-group/contrib[@contrib-type = 'author']">
      <dc:creator><xsl:value-of select="my:format-name(.)"/></dc:creator>
    </xsl:for-each>
    <xsl:for-each select="$meta/contrib-group/contrib[@contrib-type = 'contributor']">
      <dc:contributor><xsl:value-of select="my:format-name(.)"/></dc:contributor>
    </xsl:for-each>
    <xsl:if test="$meta/copyright-statement">
      <dc:rights><xsl:value-of select="normalize-space($meta/copyright-statement)"/></dc:rights>
    </xsl:if>

    <xsl:variable name="dc-types" as="xs:anyURI*"
        select="distinct-values(
                      for $n in current-group() return my:ext-to-dctype(my:get-ext($n/@name)))"/>
    <xsl:for-each select="$dc-types">
      <dc:type rdf:resource="{.}"/>
    </xsl:for-each>

    <dc_terms:isPartOf rdf:resource="{my:doi-to-uri($doi)}"/>

    <xsl:variable name="idx" as="xs:integer?"
        select="index-of(my:link-to-doi($sec-obj-refs/@xlink:href), my:fname-to-doi(./@name))"/>
    <xsl:variable name="next" as="xs:string?" select="$sec-obj-refs[$idx + 1]/@xlink:href"/>
    <xsl:if test="$next">
      <topaz:nextObject rdf:resource="{my:doi-to-uri(my:link-to-doi($next))}"/>
    </xsl:if>

    <xsl:variable name="title-obj" as="element()?"
        select="$sec-obj-refs[$idx]/(self::supplementary-material | parent::fig | parent::table-wrap)[1]"/>
    <xsl:if test="$title-obj">
      <xsl:if test="$title-obj/label">
        <dc:title><xsl:value-of select="$title-obj/label"/></dc:title>
      </xsl:if>
      <xsl:if test="$title-obj/caption">
        <dc:description rdf:parseType="Literal"><xsl:copy-of select="$title-obj/caption/node()"/></dc:description>
      </xsl:if>
    </xsl:if>

    <xsl:apply-templates select="current-group()" mode="ds-rdf"/>
  </xsl:template>

  <!-- generate the object's datastream definitions for the secondary object -->
  <xsl:template name="sec-ds">
    <xsl:apply-templates select="current-group()" mode="ds"/>
  </xsl:template>

  <!-- common templates for all datastream definitions -->
  <xsl:template match="ZipEntry" mode="ds-rdf" xmlns:topaz="http://rdf.topazproject.org/RDF/"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    <xsl:variable name="ext" select="my:get-ext(@name)"/>
    <xsl:variable name="rep-name" select="my:ext-to-ds-id($ext)"/>

    <topaz:hasRepresentation><xsl:value-of select="$rep-name"/></topaz:hasRepresentation>
    <xsl:element name="topaz:{$rep-name}-objectSize">
      <xsl:attribute name="rdf:datatype">http://www.w3.org/2001/XMLSchema#int</xsl:attribute>
      <xsl:value-of select="@size"/>
    </xsl:element>
    <xsl:element name="topaz:{$rep-name}-contentType">
      <xsl:value-of select="my:ext-to-mime($ext)"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ZipEntry" mode="ds">
    <xsl:variable name="ext" select="my:get-ext(@name)"/>

    <Datastream filename="{@name}" id="{my:ext-to-ds-id($ext)}"
                controlGroup="{my:ext-to-ctrlgrp($ext)}" mimeType="{my:ext-to-mime($ext)}"/>
  </xsl:template>


  <!-- Helper funtions -->

  <!-- Try to figure out which entry is the xml article -->
  <xsl:function name="my:find-pmc-xml" as="element(ZipEntry)">
    <xsl:param name="zip-info"/>
    <xsl:variable name="base-zip-name" as="xs:string?"
              select="if ($zip-info/@name) then my:get-root(my:basename($zip-info/@name)) else ()"/>

    <xsl:sequence select="
      if ($file-entries[my:basename(@name) = 'pmc.xml']) then
        $file-entries[my:basename(@name) = 'pmc.xml'][1]
      else if ($base-zip-name and
               $file-entries[my:basename(@name) = concat($base-zip-name, '.xml')]) then
        $file-entries[my:basename(@name) = concat($base-zip-name, '.xml')][1]
      else if ($file-entries[matches(my:basename(@name), '[a-z]+\.\d+\.xml')]) then
        $file-entries[matches(my:basename(@name), '[a-z]+\.\d+\.xml')][1]
      else
        error((), 'No article xml file found in zip file')
      "/>
  </xsl:function>

  <!-- Parse Filename into doi, ext -->
  <xsl:function name="my:parse-filename" as="xs:string+">
    <xsl:param name="fname" as="xs:string"/>
    <xsl:sequence select="(my:urldecode(replace($fname, '(.*)\..*', '$1')),
                           my:urldecode(replace($fname, '.*\.', '')))"/>
  </xsl:function>

  <!-- remove any directories from the filename -->
  <xsl:function name="my:basename" as="xs:string">
    <xsl:param name="path" as="xs:string"/>
    <xsl:value-of select="replace($path, '.*/', '')"/>
  </xsl:function>

  <!-- Get DOI from filename -->
  <xsl:function name="my:fname-to-doi" as="xs:string*">
    <xsl:param name="name" as="xs:string*"/>
    <xsl:for-each select="$name">
      <xsl:variable name="froot" select="my:get-root(my:basename(.))"/>
      <xsl:value-of select="
        if ($zip-fmt = 'TPZ') then
          $froot
        else if ($zip-fmt = 'AP') then
          concat($doi, substring($froot, string-length(my:get-root($pmc-entry/@name)) + 1))
        else
          error((), concat('internal error: unknown format ', $zip-fmt, ' in fct fname-to-doi'))
        "/>
    </xsl:for-each>
  </xsl:function>

  <!-- Get DOI for link -->
  <xsl:function name="my:link-to-doi" as="xs:string*">
    <xsl:param name="name" as="xs:string*"/>
    <xsl:for-each select="$name">
      <xsl:value-of select="
        if ($zip-fmt = 'TPZ') then
          if (starts-with(., 'info:doi/')) then
            substring(., 10)
          else if (starts-with(., 'doi:')) then
            substring(., 5)
          else
            error((), concat('error: unknown link uri ', ., ' in fct link-to-doi'))
        else if ($zip-fmt = 'AP') then
          my:fname-to-doi(.)
        else
          error((), concat('internal error: unknown format ', $zip-fmt, ' in fct link-to-doi'))
        "/>
    </xsl:for-each>
  </xsl:function>

  <!-- Get root of filename -->
  <xsl:function name="my:get-root" as="xs:string*">
    <xsl:param name="name" as="xs:string*"/>
    <xsl:for-each select="$name">
      <xsl:value-of select="my:parse-filename(.)[1]"/>
    </xsl:for-each>
  </xsl:function>

  <!-- Get extension from filename -->
  <xsl:function name="my:get-ext" as="xs:string*">
    <xsl:param name="name" as="xs:string*"/>
    <xsl:for-each select="$name">
      <xsl:value-of select="my:parse-filename(.)[last()]"/>
    </xsl:for-each>
  </xsl:function>

  <!-- DOI to Fedora-PID mapping -->
  <xsl:function name="my:doi-to-pid" as="xs:string">
    <xsl:param name="doi" as="xs:string"/>
    <xsl:value-of select="concat('doi:', my:urlencode($doi))"/>
  </xsl:function>

  <!-- Fedora-PID to URI mapping -->
  <xsl:function name="my:pid-to-uri" as="xs:string">
    <xsl:param name="pid" as="xs:string"/>
    <xsl:value-of select="concat('info:fedora/', $pid)"/>
  </xsl:function>

  <!-- DOI to URI mapping -->
  <xsl:function name="my:doi-to-uri" as="xs:string">
    <xsl:param name="doi" as="xs:string"/>
    <xsl:value-of select="my:pid-to-uri(my:doi-to-pid($doi))"/>
  </xsl:function>

  <!-- Fedora-PID to DOI mapping -->
  <xsl:function name="my:pid-to-doi" as="xs:string">
    <xsl:param name="pid" as="xs:string"/>
    <xsl:value-of select="my:urldecode(substring($pid, 5))"/>
  </xsl:function>

  <!-- URI to Fedora-PID mapping -->
  <xsl:function name="my:uri-to-pid" as="xs:string">
    <xsl:param name="uri" as="xs:string"/>
    <xsl:value-of select="substring($uri, 13)"/>
  </xsl:function>

  <!-- URI to DOI mapping -->
  <xsl:function name="my:uri-to-doi" as="xs:string">
    <xsl:param name="uri" as="xs:string"/>
    <xsl:value-of select="my:pid-to-doi(my:uri-to-pid($uri))"/>
  </xsl:function>

  <!-- article-DOI to auxiliary-DOI mapping -->
  <xsl:function name="my:doi-to-aux" as="xs:string">
    <xsl:param name="doi"  as="xs:string"/>
    <xsl:param name="type" as="xs:string"/>
    <xsl:param name="cnt"  as="xs:integer"/>
    <xsl:value-of select="concat($doi, '/', $type, '/', $cnt)"/>
  </xsl:function>

  <!-- determines if the filename is that of a secondary object or not -->
  <xsl:function name="my:is-main" as="xs:boolean">
    <xsl:param name="fname" as="xs:string"/>
    <xsl:value-of select="$fname = $pmc-entry/@name or my:fname-to-doi($fname) = $doi"/>
  </xsl:function>

  <xsl:function name="my:is-secondary" as="xs:boolean">
    <xsl:param name="fname" as="xs:string"/>
    <xsl:value-of select="not(my:is-main($fname))"/>
  </xsl:function>

  <!-- url-encode a string (replace reserved chars with %HH). Note this is the
     - same as the built-in encode-for-uri, except that it encodes all non-fedora-pid
     - chars -->
  <xsl:function name="my:urlencode" as="xs:string">
    <xsl:param name="str" as="xs:string"/>
    <xsl:value-of select="string-join(my:encodeSeq($str), '')"/>
  </xsl:function>

  <xsl:function name="my:encodeSeq" as="xs:string+">
    <xsl:param name="str" as="xs:string"/>

    <xsl:analyze-string select="$str" regex="[^A-Za-z0-9.~_-]">
      <xsl:matching-substring>
        <xsl:value-of select="concat('%', my:hex(string-to-codepoints(.)))"/>
      </xsl:matching-substring>
      <xsl:non-matching-substring>
        <xsl:value-of select="."/>
      </xsl:non-matching-substring>
    </xsl:analyze-string>
  </xsl:function>

  <xsl:function name="my:hex" as="xs:string">
    <xsl:param name="char" as="xs:integer"/>
    <xsl:value-of select="concat(my:hexChar($char idiv 16), my:hexChar($char mod 16))"/>
  </xsl:function>

  <xsl:function name="my:hexChar" as="xs:string">
    <xsl:param name="char" as="xs:integer"/>
    <xsl:variable name="cp" as="xs:integer" select="if ($char &gt; 9) then $char + 7 else $char"/>
    <xsl:value-of select="codepoints-to-string($cp + 48)"/>
  </xsl:function>

  <!-- url-decode a string (resolve the %HH) -->
  <xsl:function name="my:urldecode" as="xs:string">
    <xsl:param name="str" as="xs:string"/>
    <xsl:value-of select="string-join(my:decodeSeq($str), '')"/>
  </xsl:function>

  <xsl:function name="my:decodeSeq" as="xs:string+">
    <xsl:param name="str" as="xs:string"/>

    <xsl:analyze-string select="$str" regex="%[0-9A-Fa-f][0-9A-Fa-f]">
      <xsl:matching-substring>
        <xsl:value-of select="codepoints-to-string(my:unhex(substring(., 2)))"/>
      </xsl:matching-substring>
      <xsl:non-matching-substring>
        <xsl:value-of select="."/>
      </xsl:non-matching-substring>
    </xsl:analyze-string>
  </xsl:function>

  <xsl:function name="my:unhex" as="xs:integer">
    <xsl:param name="hex" as="xs:string"/>
    <xsl:variable name="u" as="xs:string" select="substring($hex, 1, 1)"/>
    <xsl:variable name="l" as="xs:string" select="substring($hex, 2, 1)"/>
    <xsl:value-of select="my:unhexChar($u) * 16 + my:unhexChar($l)"/>
  </xsl:function>

  <xsl:function name="my:unhexChar" as="xs:integer">
    <xsl:param name="hex" as="xs:string"/>
    <xsl:variable name="v" as="xs:integer" select="string-to-codepoints(upper-case($hex)) - 48"/>
    <xsl:value-of select="if ($v &gt; 9) then $v - 7 else $v"/>
  </xsl:function>

  <!-- separate out dublic-core from non-dublin-core -->
  <xsl:function name="my:filter-dc" as="element()*">
    <xsl:param name="rdf"    as="element()*"/>
    <xsl:param name="inc-dc" as="xs:boolean"/>

    <xsl:for-each select="$rdf">
      <xsl:if test="(self::dc:* or self::oai_dc:*) and $inc-dc or
                    not(self::dc:* or self::oai_dc:*) and not($inc-dc)"
          xmlns:dc="http://purl.org/dc/elements/1.1/"
          xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/">
        <xsl:sequence select="."/>
      </xsl:if>
    </xsl:for-each>
  </xsl:function>

  <!-- remove xsd:date datatype attributes for fedora unsupported datatypes -->
  <xsl:function name="my:filter-dt" as="element()*">
    <xsl:param name="rdf" as="element()*"/>

    <xsl:for-each select="$rdf" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
      <xsl:choose>
        <xsl:when test="@rdf:datatype and
                          @rdf:datatype != 'http://www.w3.org/2001/XMLSchema#int' and
                          @rdf:datatype != 'http://www.w3.org/2001/XMLSchema#long' and
                          @rdf:datatype != 'http://www.w3.org/2001/XMLSchema#float' and
                          @rdf:datatype != 'http://www.w3.org/2001/XMLSchema#double'">
          <xsl:copy>
            <xsl:sequence select="not(@rdf:datatype)"/>
          </xsl:copy>
        </xsl:when>
        <xsl:otherwise>
          <xsl:sequence select="."/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:for-each>
  </xsl:function>

  <!-- pmc structured name to simple string (for dc:creator etc) -->
  <xsl:function name="my:format-name" as="xs:string">
    <xsl:param name="contrib" as="element(contrib)"/>

    <xsl:choose>
      <xsl:when test="$contrib/name">
        <xsl:value-of select="
          if ($contrib/name/given-names) then
            concat($contrib/name/given-names, ' ', $contrib/name/surname)
          else
            $contrib/name/surname
          "/>
      </xsl:when>

      <xsl:when test="$contrib/collab">
        <xsl:value-of select="$contrib/collab"/>
      </xsl:when>

      <xsl:when test="$contrib/string-name">
        <xsl:value-of select="$contrib/string-name"/>
      </xsl:when>
    </xsl:choose>
  </xsl:function>

  <!-- Select the date to use for dc:date. The order of preference is:
     - 'epub', 'epub-ppub', 'ppub', 'ecorrected', 'pcorrected', no-type, first -->
  <xsl:function name="my:select-date" as="element(pub-date)">
    <xsl:param name="date" as="element(pub-date)+"/>

    <xsl:variable name="pref-date" select="(
      for $t in ('epub', 'epub-ppub', 'ppub', 'ecorrected', 'pcorrected')
        return $date[@pub-type = $t]
      )[1]"/>

    <xsl:sequence select="
      if ($pref-date) then $pref-date
      else if ($date[not(@pub-type)]) then $date[not(@pub-type)]
      else $date[1]
      "/>
  </xsl:function>

  <!-- pmc structured date to ISO-8601 (YYYY-MM-DD); seasons results in first day of the season,
     - or Jan 1st in the case of winter (to get the year right); missing fields are defaulted
     - from the current time -->
  <xsl:function name="my:format-date" as="xs:string">
    <xsl:param name="date" as="element()"/>

    <xsl:value-of select="concat(
      if ($date/year) then $date/year else year-from-date(current-date()),
      '-',
      if ($date/season) then
        if (lower-case($date/season) = 'spring') then '03-21'
        else if (lower-case($date/season) = 'summer') then '06-21'
        else if (lower-case($date/season) = 'fall') then '09-23'
        else if (lower-case($date/season) = 'winter') then '01-01'
        else ''
      else
        concat(
          my:twochar(if ($date/month) then $date/month else month-from-date(current-date())),
          '-',
          my:twochar(if ($date/day) then $date/day else day-from-date(current-date()))
        )
      )"/>
  </xsl:function>

  <xsl:function name="my:twochar" as="xs:string">
    <xsl:param    name="str" as="xs:integer"/>
    <xsl:variable name="s" select="xs:string($str)"/>
    <xsl:value-of select="
        if (string-length($s) = 1) then concat('0', $s) else $s
      "/>
  </xsl:function>

  <!-- Select the abstract to use for dc:description. The order of preference is:
     - 'short', 'web-summary', 'toc', 'summary', 'ASCII', no-type, first -->
  <xsl:function name="my:select-abstract" as="element(abstract)">
    <xsl:param name="abstracts" as="element(abstract)+"/>

    <xsl:variable name="pref-abstract" select="(
      for $t in ('short', 'web-summary', 'toc', 'summary', 'ASCII')
        return $abstracts[@abstract-type = $t]
      )[1]"/>

    <xsl:sequence select="
      if ($pref-abstract) then $pref-abstract
      else if ($abstracts[not(@abstract-type)]) then $abstracts[not(@abstract-type)]
      else $abstracts[1]
      "/>
  </xsl:function>

  <!-- Filename extension to datastream-id -->
  <xsl:function name="my:ext-to-ds-id" as="xs:string">
    <xsl:param name="ext" as="xs:string"/>
    <xsl:value-of select="upper-case($ext)"/>
  </xsl:function>

  <!-- Filename extension to Fedora control-group mapping: everything is 'Managed'. Note:
       don't use 'XML' because Fedora messes with it then. -->
  <xsl:function name="my:ext-to-ctrlgrp" as="xs:string">
    <xsl:param name="ext" as="xs:string"/>
    <xsl:value-of select="'Managed'"/>
  </xsl:function>

  <!-- Filename extension to mime-type mapping; defaults to application/octet-stream if extension
     - is not recognized -->
  <xsl:function name="my:ext-to-mime" as="xs:string">
    <xsl:param name="ext" as="xs:string"/>
    <xsl:variable name="e" as="xs:string" select="lower-case($ext)"/>
    <xsl:value-of select="
      if ($e = 'xml') then 'text/xml'
      else if ($e = 'htm' or $e = 'html') then 'text/html'
      else if ($e = 'txt') then 'text/plain'
      else if ($e = 'rtf') then 'text/rtf'
      else if ($e = 'pdf') then 'application/pdf'
      else if ($e = 'dvi') then 'application/x-dvi'
      else if ($e = 'latex') then 'application/x-latex'
      else if ($e = 'swf') then 'application/x-shockwave-flash'
      else if ($e = 'png') then 'image/png'
      else if ($e = 'gif') then 'image/gif'
      else if ($e = 'tif' or $e = 'tiff') then 'image/tiff'
      else if ($e = 'jpg' or $e = 'jpeg' or $e = 'jpe') then 'image/jpeg'
      else if ($e = 'bmp') then 'image/bmp'
      else if ($e = 'xpm') then 'image/x-xpixmap'
      else if ($e = 'pnm') then 'image/x-portable-anymap'
      else if ($e = 'ief') then 'image/ief'
      else if ($e = 'ras') then 'image/x-cmu-raster'
      else if ($e = 'doc') then 'application/msword'
      else if ($e = 'xls') then 'application/vnd.ms-excel'
      else if ($e = 'ppt') then 'application/vnd.ms-powerpoint'
      else if ($e = 'ppt') then 'application/vnd.ms-powerpoint'
      else if ($e = 'mpg' or $e = 'mpeg') then 'video/mpeg'
      else if ($e = 'mp4' or $e = 'mpg4') then 'video/mp4'
      else if ($e = 'mov' or $e = 'qt') then 'video/quicktime'
      else if ($e = 'avi') then 'video/x-msvideo'
      else if ($e = 'wmv') then 'video/x-ms-wmv'
      else if ($e = 'asf' or $e = 'asx') then 'video/x-ms-asf'
      else if ($e = 'divx') then 'video/x-divx'
      else if ($e = 'wav') then 'audio/x-wav'
      else if ($e = 'au' or $e = 'snd') then 'audio/basic'
      else if ($e = 'mp2' or $e = 'mp3') then 'audio/mpeg'
      else if ($e = 'ram' or $e = 'rm') then 'audio/x-pn-realaudio'
      else if ($e = 'ra') then 'audio/x-realaudio'
      else if ($e = 'aif' or $e = 'aiff') then 'audio/x-aiff'
      else if ($e = 'mid' or $e = 'midi' or $e = 'rmi') then 'audio/midi'
      else if ($e = 'wma') then 'audio/x-ms-wma'
      else 'application/octet-stream'
      "/>
  </xsl:function>

  <!-- Filename extension to dublin-core type mapping -->
  <xsl:function name="my:ext-to-dctype" as="xs:anyURI?">
    <xsl:param name="ext" as="xs:string"/>
    <xsl:variable name="mime-type"  as="xs:string" select="my:ext-to-mime($ext)"/>
    <xsl:variable name="media-type" as="xs:string" select="substring-before($mime-type, '/')"/>
    <xsl:sequence select="
      if      ($media-type = 'image') then xs:anyURI('http://purl.org/dc/dcmitype/StillImage')
      else if ($media-type = 'video') then xs:anyURI('http://purl.org/dc/dcmitype/MovingImage')
      else if ($media-type = 'audio') then xs:anyURI('http://purl.org/dc/dcmitype/Sound')
      else if ($media-type = 'text')  then xs:anyURI('http://purl.org/dc/dcmitype/Text')
      else if ($mime-type = 'application/vnd.ms-excel') then xs:anyURI('http://purl.org/dc/dcmitype/Dataset')
      else ()
      "/>
  </xsl:function>

  <!-- Filename extension to mime-type mapping; defaults to application/octet-stream if extension
     - is not recognized -->
  <xsl:function name="my:links-for-doi" as="element()*">
    <xsl:param    name="doi" as="xs:string"/>
    <xsl:sequence select="$article/body//*[my:link-to-doi(@xlink:href) = $doi]"/>
  </xsl:function>
</xsl:stylesheet>
