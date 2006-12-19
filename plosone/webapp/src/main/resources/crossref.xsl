<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml" indent="no" encoding="UTF-8" omit-xml-declaration="no"/>
    <xsl:param name="plosDoiUrl"/>
    <xsl:template match="/">
        <xsl:variable name="currentDateTime" select="current-dateTime()"/>
        <xsl:variable name="timestamp" select="format-dateTime($currentDateTime, '[Y0001][M01][D01][H01][m01][s01]')"/>
        <xsl:variable name="article_doi" select="//article-id[@pub-id-type='doi'][1]"/>
        <doi_batch version="3.0.3" xmlns="http://www.crossref.org/schema/3.0.3" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.crossref.org/schema/3.0.3 http://www.crossref.org/schema/3.0.3/crossref3.0.3.xsd">
            <head>
                <doi_batch_id>
                    <xsl:value-of select="$article_doi"/>
                </doi_batch_id>
                <timestamp>
                    <xsl:value-of select="$timestamp"/>
                </timestamp>
                <depositor>
                    <name>Public Library of Science</name>
                    <email_address>doi@plos.org</email_address>
                </depositor>
                <registrant>Public Library of Science</registrant>
            </head>
            <body>
                <journal>
                    <journal_metadata language="en">
                        <full_title>
                            <xsl:value-of select="//journal-title[1]"/>
                        </full_title>
                        <abbrev_title>
                            <xsl:value-of select="//journal-title[1]"/>
                        </abbrev_title>
                        <issn media_type="electronic">
                            <xsl:value-of select="//issn[@pub-type='epub'][1]"/>
                        </issn>
                    </journal_metadata>
                    <journal_issue>
                        <publication_date media_type="online">
                            <month>
                                <xsl:value-of select="article/front/article-meta/pub-date[@pub-type='epub']/month"/>
                            </month>
                            <day>
                                <xsl:value-of select="article/front/article-meta/pub-date[@pub-type='epub']/day"/>
                            </day>
                            <year>
                                <xsl:value-of select="article/front/article-meta/pub-date[@pub-type='epub']/year"/>
                            </year>
                        </publication_date>
                        <journal_volume>
                            <volume>
                                <xsl:value-of select="article/front/article-meta/volume"/>
                            </volume>
                        </journal_volume>
                        <issue>
                            <xsl:value-of select="article/front/article-meta/issue"/>
                        </issue>
                    </journal_issue>
                    <journal_article publication_type="full_text">
                        <titles>
                            <title>
                                <xsl:value-of select="article/front/article-meta/title-group/article-title"/>
                            </title>
                        </titles>
                        <contributors>
                            <xsl:for-each select="article/front/article-meta/contrib-group/contrib[@contrib-type='author']">
                                <person_name contributor_role="author">
                                    <xsl:choose>
                                        <xsl:when test="position() = 1 or @equal-contrib='yes'">
                                            <xsl:attribute name="sequence">first</xsl:attribute>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:attribute name="sequence">additional</xsl:attribute>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                    <given_name><xsl:value-of select="name/given-names"/></given_name>
                                    <surname><xsl:value-of select="name/surname"/></surname>
                                </person_name>
                            </xsl:for-each>
                            <xsl:for-each select="article/front/article-meta/contrib-group/contrib[@contrib-type='editor']">
                                <person_name contributor_role="editor">
                                    <xsl:choose>
                                        <xsl:when test="position() = 1">
                                            <xsl:attribute name="sequence">first</xsl:attribute>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:attribute name="sequence">additional</xsl:attribute>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                    <given_name><xsl:value-of select="name/given-names"/></given_name>
                                    <surname><xsl:value-of select="name/surname"/></surname>
                                </person_name>
                            </xsl:for-each>
                            <xsl:for-each select="article/front/article-meta/contrib-group/contrib[@contrib-type='translator']">
                                <person_name contributor_role="translator">
                                    <xsl:choose>
                                        <xsl:when test="position() = 1">
                                            <xsl:attribute name="sequence">first</xsl:attribute>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:attribute name="sequence">additional</xsl:attribute>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                    <given_name><xsl:value-of select="name/given-names"/></given_name>
                                    <surname><xsl:value-of select="name/surname"/></surname>
                                </person_name>
                            </xsl:for-each>
                        </contributors>
                        <publication_date media_type="online">
                            <month>
                                <xsl:value-of select="article/front/article-meta/pub-date[@pub-type='epub']/month"/>
                            </month>
                            <day>
                                <xsl:value-of select="article/front/article-meta/pub-date[@pub-type='epub']/day"/>
                            </day>
                            <year>
                                <xsl:value-of select="article/front/article-meta/pub-date[@pub-type='epub']/year"/>
                            </year>                            
                        </publication_date> 
                        <pages>
                            <first_page><xsl:value-of select="article/front/article-meta/elocation-id"/></first_page>
                        </pages>
                        <publisher_item>
                            <item_number><xsl:value-of select="$article_doi"/></item_number>
                        </publisher_item>                        
                        <doi_data>
                            <doi><xsl:value-of select="$article_doi"/></doi>
                            <timestamp> <xsl:value-of select="$timestamp"/></timestamp>
                            <resource><xsl:value-of select="$plosDoiUrl"/><xsl:value-of select="$article_doi"/></resource>
                        </doi_data>
                        <component_list>
                            <xsl:for-each select="//fig">
                                <component parent_relation="isPartOf">
                                    <description><xsl:for-each select="caption"/></description>
                                    <doi_data>
                                        <doi><xsl:value-of select="object-id[@pub-id-type='doi']"/></doi>
                                        <resource><xsl:value-of select="$plosDoiUrl"/><xsl:value-of select="object-id[@pub-id-type='doi']"/></resource>
                                    </doi_data>
                                </component>
                            </xsl:for-each>
                            <xsl:for-each select="//table-wrap">
                                <component parent_relation="isPartOf">
                                    <description><xsl:for-each select="caption/title"/></description>
                                    <doi_data>
                                        <doi><xsl:value-of select="object-id[@pub-id-type='doi']"/></doi>
                                        <resource><xsl:value-of select="$plosDoiUrl"/><xsl:value-of select="object-id[@pub-id-type='doi']"/></resource>
                                    </doi_data>
                                </component>
                            </xsl:for-each>
                        </component_list>                    
                    </journal_article>
                </journal>
            </body>
        </doi_batch>
    </xsl:template>
</xsl:stylesheet>
