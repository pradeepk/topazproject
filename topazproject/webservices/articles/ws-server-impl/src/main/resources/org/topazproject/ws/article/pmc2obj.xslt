<xsl:stylesheet version="2.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:my="my:ingest.pmc#"
    exclude-result-prefixes="my">

  <!--
    - Convert a ZipInfo (zip.dtd) to an ObjectList (fedora.dtd). This contains the main 
    - object generation logic for ingest.
    -
    - This converter handles zip's according to TOPAZ's specs using PMC 2.0 for the main
    - article description.
    -->

  <xsl:output method="xml" omit-xml-declaration="yes" indent="yes"/>

  <xsl:param name="is_update" select="false()"/>

  <xsl:variable name="article"      select="document('pmc.xml')/article"/>
  <xsl:variable name="meta"         select="$article/front/article-meta"/>
  <xsl:variable name="doi"          select="$meta/article-id[@pub-id-type = 'doi']"/>
  <xsl:variable name="file-entries" select="/ZipInfo/ZipEntry[not(@isDirectory)]"/>

  <!-- top-level template - generates the ObjectList -->
  <xsl:template match="/ZipInfo">
    <ObjectList logMessage="Ingest of article '{$meta/title-group/article-title}'">
      <xsl:call-template name="main-entry"/>
      <xsl:for-each-group select="$file-entries[my:is-secondary(@name)]"
                          group-by="my:get-doi(@name)">
        <xsl:apply-templates select="." mode="sec"/>
      </xsl:for-each-group>
    </ObjectList>
  </xsl:template>

  <!-- templates for the main (pmc) entry -->
  <xsl:template name="main-entry">
    <Object pid="{my:doi-to-pid($doi)}" cModel="PlosArticle">
      <xsl:call-template name="main-dc"/>
      <xsl:call-template name="main-rdf"/>
      <xsl:call-template name="main-ds"/>
    </Object>
  </xsl:template>

  <xsl:template name="main-dc">
    <DC xmlns:dc="http://purl.org/dc/elements/1.1/">
      <dc:identifier><xsl:value-of select="concat('info:doi:', $doi)"/></dc:identifier>
      <dc:title><xsl:value-of select="$meta/title-group/article-title"/></dc:title>
      <dc:type>http://purl.org/dc/dcmitype/Text</dc:type>
      <dc:format>text/xml</dc:format>
      <dc:language>en</dc:language>
      <xsl:if test="$meta/pub-date">
          <dc:date><xsl:value-of select="my:format-date(my:select-date($meta/pub-date))"/></dc:date>
      </xsl:if>
      <xsl:for-each select="$meta/contrib-group/contrib[@contrib-type = 'author']">
        <dc:creator><xsl:value-of select="my:format-name(.)"/></dc:creator>
      </xsl:for-each>
      <xsl:for-each select="$meta/contrib-group/contrib[@contrib-type = 'contributor']">
        <dc:contributor><xsl:value-of select="my:format-name(.)"/></dc:contributor>
      </xsl:for-each>
      <xsl:for-each select="$meta/article-categories/subj-group[@subj-group-type = 'Discipline']/subject">
        <dc:subject><xsl:value-of select="."/></dc:subject>
      </xsl:for-each>
      <xsl:if test="$meta/abstract">
        <dc:description><xsl:value-of select="normalize-space(my:select-abstract($meta/abstract))"/></dc:description>
      </xsl:if>
      <xsl:if test="$article/front/journal-meta/publisher">
        <dc:publisher><xsl:value-of select="$article/front/journal-meta/publisher/publisher-name"/></dc:publisher>
      </xsl:if>
      <xsl:if test="$meta/copyright-statement">
        <dc:rights><xsl:value-of select="normalize-space($meta/copyright-statement)"/></dc:rights>
      </xsl:if>
    </DC>
  </xsl:template>

  <xsl:template name="main-rdf">
    <RDF xmlns:topaz="http://rdf.topazproject.org/RDF#">
      <xsl:for-each select="distinct-values(my:get-doi($file-entries[my:is-secondary(@name)]/@name))">
        <topaz:hasMember><xsl:value-of select="."/></topaz:hasMember>
      </xsl:for-each>
      <xsl:apply-templates select="$file-entries[my:is-main(@name)]" mode="ds-rdf"/>
    </RDF>
  </xsl:template>

  <xsl:template name="main-ds">
    <xsl:apply-templates select="$file-entries[my:is-main(@name)]" mode="ds"/>
  </xsl:template>

  <!-- templates for all secondary entries -->
  <xsl:template match="ZipEntry" mode="sec">
    <xsl:variable name="sdoi" select="my:get-doi(@name)"/>

    <Object pid="{my:doi-to-pid($sdoi)}" cModel="PlosArticleSecObj">
      <xsl:call-template name="sec-dc"/>
      <xsl:call-template name="sec-rdf"/>
      <xsl:call-template name="sec-ds"/>
    </Object>
  </xsl:template>

  <xsl:template name="sec-dc">
    <xsl:variable name="sdoi" select="my:get-doi(@name)"/>

    <DC xmlns:dc="http://purl.org/dc/elements/1.1/">
      <dc:identifier><xsl:value-of select="concat('info:doi:', $sdoi)"/></dc:identifier>
      <xsl:if test="$meta/pub-date">
          <dc:date><xsl:value-of select="my:format-date(my:select-date($meta/pub-date))"/></dc:date>
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
    </DC>
  </xsl:template>

  <xsl:template name="sec-rdf">
    <RDF xmlns:topaz="http://rdf.topazproject.org/RDF#">
      <topaz:isMemberOf><xsl:value-of select="$doi"/></topaz:isMemberOf>
      <xsl:apply-templates select="current-group()" mode="ds-rdf"/>
    </RDF>
  </xsl:template>

  <xsl:template name="sec-ds">
    <xsl:variable name="sdoi" select="my:get-doi(@name)"/>
    <xsl:apply-templates select="current-group()" mode="ds"/>
  </xsl:template>

  <!-- common templates for all datastream definitions -->
  <xsl:template match="ZipEntry" mode="ds-rdf" xmlns:topaz="http://rdf.topazproject.org/RDF#">
    <xsl:element name="topaz:{my:ext-to-ds-id(my:get-ext(@name))}-objectSize">
      <xsl:value-of select="@size"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ZipEntry" mode="ds">
    <xsl:variable name="ext" select="my:get-ext(@name)"/>

    <Datastream filename="{@name}" id="{my:ext-to-ds-id($ext)}"
                controlGroup="{my:ext-to-ctrlgrp($ext)}" mimeType="{my:ext-to-mime($ext)}"/>
  </xsl:template>


  <!-- Helper funtions -->

  <!-- Parse Filename into doi, ext -->
  <xsl:function name="my:parse-filename" as="xs:string+">
    <xsl:param name="fname" as="xs:string"/>
    <xsl:copy-of select="for $t in tokenize($fname, '\.') return my:urldecode($t)"/>
  </xsl:function>

  <!-- Get DOI from filename -->
  <xsl:function name="my:get-doi" as="xs:string*">
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

  <!-- determines if the filename is that of a secondary object or not -->
  <xsl:function name="my:is-main" as="xs:boolean">
    <xsl:param name="fname" as="xs:string"/>
    <xsl:value-of select="$fname = 'pmc.xml' or my:get-doi($fname) = $doi"/>
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

  <!-- pmc structured name to simple string (for dc:creator etc) -->
  <xsl:function name="my:format-name" as="xs:string">
    <xsl:param name="contrib" as="element(contrib)"/>

    <xsl:choose>
      <xsl:when test="$contrib/name">
        <xsl:value-of select="
          if ($contrib/name/given-names) then
            concat($contrib/name/surname, ', ', $contrib/name/given-names)
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

    <xsl:copy-of select="
      if ($pref-date) then $pref-date
      else if ($date[not(@pub-type)]) then $date[not(@pub-type)]
      else $date[1]
      "/>
  </xsl:function>

  <!-- pmc structured date to ISO-8601 (YYYY-MM-DD); seasons results in first day of the season,
     - or Jan 1st in the case of winter (to get the year right); missing fields are defaulted
     - from the current time -->
  <xsl:function name="my:format-date" as="xs:string">
    <xsl:param name="date" as="element(pub-date)"/>

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

    <xsl:copy-of select="
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

  <!-- Filename extension to Fedora control-group mapping: 'xml' results in 'XML', all others
     - in 'Managed' -->
  <xsl:function name="my:ext-to-ctrlgrp" as="xs:string">
    <xsl:param name="ext" as="xs:string"/>
    <xsl:value-of select="if (lower-case($ext) = 'xml') then 'XML' else 'Managed'"/>
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
      else if ($e = 'jpg' or $e = 'jpeg') then 'image/jpeg'
      else if ($e = 'doc') then 'application/msword'
      else if ($e = 'xls') then 'application/vnd.ms-excel'
      else if ($e = 'ppt') then 'application/vnd.ms-powerpoint'
      else if ($e = 'ppt') then 'application/vnd.ms-powerpoint'
      else if ($e = 'mpg' or $e = 'mpeg') then 'video/mpeg'
      else if ($e = 'mov' or $e = 'qt') then 'video/quicktime'
      else if ($e = 'avi') then 'video/x-msvideo'
      else if ($e = 'wav') then 'audio/x-wav'
      else if ($e = 'au' or $e = 'snd') then 'audio/basic'
      else if ($e = 'mp2' or $e = 'mp3') then 'audio/mpeg'
      else if ($e = 'ram' or $e = 'rm') then 'audio/x-pn-realaudio'
      else if ($e = 'ra') then 'audio/x-realaudio'
      else if ($e = 'aif' or $e = 'aiff') then 'audio/x-aiff'
      else if ($e = 'mid' or $e = 'midi') then 'audio/midi'
      else 'application/octet-stream'
      "/>
  </xsl:function>

</xsl:stylesheet>
