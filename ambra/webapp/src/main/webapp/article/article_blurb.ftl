<div id="contentHeader"><p>Open Access</p><p id="articleType">${articleType.heading}
<#if articleType.code??>
  <#if articleType.code != "research_article">
    <a class="info" title="What is a ${articleType.heading}?" href="#${articleType.code}">Info</a>
  </#if>
<#else>
  --!!ARTICLE TYPE CODE UNDEFINED!!--
</#if></p></div>

<#list journalList as jour> 
  <#if jour.key != journalContext && articleInfo.eIssn == jour.eIssn> 
    <div id="publisher"><p>Published in <em><a href=${freemarker_config.getJournalUrl(jour.key)}>${jour.dublinCore.title}</a></em></p></div> 
    <#break/> 
  </#if> 
</#list>  
