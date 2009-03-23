<#if !journalContext??>
  <#--In the fetchBody action a more "bare bones" copy of the article is rendered and not all of the
   templates are included.  We need some of these variables defined here.  This runs the variables
   definition template if it's not defined. -->
  <#include "article_variables.ftl">
</#if>
<#assign publisher = "">
<#list journalList as jour>
  <#if jour.key != journalContext && articleInfo.eIssn == jour.eIssn>
    <#assign publisher = "Published in <em><a href=\"" + freemarker_config.getJournalUrl(jour.key) + "\">" + jour.dublinCore.title + "</a></em>">
    <#break/>
  <#else>
    <#if jour.key != journalContext>
      <#assign jourAnchor = "<a href=\"" + freemarker_config.getJournalUrl(jour.key) + "\">"/>
      <#if (publisher?length > 0)>
        <#assign publisher = publisher + ", " + jourAnchor + jour.dublinCore.title + "</a>" />
      <#else>
        <#assign publisher = publisher + "Featured in " + jourAnchor + jour.dublinCore.title + "</a>" />
      </#if>
    </#if>
  </#if>
</#list>
<#if (publisher?length > 0)>
  <div id="publisher"><p>${publisher}</p></div>
</#if>
<div id="contentHeader"><p>Open Access</p><p id="articleType">${articleType.heading}
<#if articleType.code??>
  <#if articleType.code != "research_article">
    <a class="info" title="What is a ${articleType.heading}?" href="#${articleType.code}">Info</a>
  </#if>
<#else>
  --!!ARTICLE TYPE CODE UNDEFINED!!--
</#if></p></div>