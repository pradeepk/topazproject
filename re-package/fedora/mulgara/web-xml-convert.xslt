<xsl:stylesheet
  version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:output method="xml" encoding="utf-8"
    doctype-system="http://java.sun.com/j2ee/dtds/web-app_2.2.dtd"  
    doctype-public="-//Sun Microsystems, Inc.//DTD Web Application 2.2//EN"/>

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="security-constraint[last()]">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
    <xsl:call-template name="add-itql-sc"/>
  </xsl:template>

  <xsl:template name="add-itql-sc" xml:space="preserve">
        <xsl:comment> Security Constraints on SOAP-based Itql interface to the Kowari Server </xsl:comment>
        <security-constraint>
            <web-resource-collection>
                <web-resource-name>Fedora Repository Server</web-resource-name>
                <url-pattern>/services/ItqlBeanService</url-pattern>
                <http-method>GET</http-method>
                <http-method>HEAD</http-method>
                <http-method>POST</http-method>
            </web-resource-collection>
        </security-constraint>
  </xsl:template>

</xsl:stylesheet>
