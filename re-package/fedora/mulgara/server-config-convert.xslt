<xsl:stylesheet
  version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:wsdd="http://xml.apache.org/axis/wsdd/"
  xmlns="http://xml.apache.org/axis/wsdd/">

  <xsl:output method="xml" encoding="utf-8"/>

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="wsdd:service[last()]">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
    <xsl:call-template name="add-itql-svc"/>
  </xsl:template>

  <xsl:template name="add-itql-svc" xml:space="preserve">
 <service name="ItqlBeanService" provider="java:RPC">
  <parameter name="allowedMethods" value="setServerURI setAliasMap getAliasMap executeQueryToString executeUpdate beginTransaction commit rollback close"/>
  <parameter name="scope" value="Session"/>
  <parameter name="className" value="org.kowari.itql.ItqlInterpreterBean"/>
  <namespace>http://tucana.org/</namespace>
 </service>
  </xsl:template>

</xsl:stylesheet>
