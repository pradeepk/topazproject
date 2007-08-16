<#assign publisher=""/>
<#list journalList as jour>
  <#-- Special Case -->
  <#if (journalList?size == 1) && (jour.key == journalContext)>
    <#if jour.key == "PLoSClinicalTrials">
      <#assign publisher="Published in <em>PLoS Clinical Trials</em>" />
    </#if>
  <#else>
    <#if (articleInfo.EIssn = jour.EIssn) && (jour.key != journalContext) >
      <#assign publisher = "Published in <em>" + jour.dublinCore.title + "</em>" />
      <#break/>
    <#else>
      <#if jour.key != journalContext> 
        <#if publisher?length gt 0>
          <#assign publisher = publisher + ", " + jour.dublinCore.title />
        <#else>
          <#assign publisher = publisher + "Featured in " + jour.dublinCore.title />
        </#if>
        </#if>
    </#if>
  </#if>
</#list>
  <div id="researchArticle" class="content">
    <a id="top" name="top" toc="top" title="Top"></a>
    <@s.url id="thisPageURL" includeParams="get" includeContext="true" encode="false"/>
    <@s.url id="feedbackURL" includeParams="none" namespace="/" action="feedbackCreate" page="${thisPageURL?url}"/>
    <div class="beta">We are still in beta! Help us make the site better and
      <a href="${feedbackURL}" title="Submit your feedback">report bugs</a>.
    </div>
    <div id="contentHeader">
      <p>Open Access</p>
      <p id="articleType">Research Article</p>
    </div>
    <#if publisher != "">
      <div id="publisher"><p>${publisher}</p></div>
    </#if>
    <@s.property value="transformedArticle" escape="false"/>
  </div>

