<div id="contentHeader"><p>Open Access</p><p id="articleType">${articleType.heading}
<#if articleType.code??>
  <#if articleType.code != "research_article">
    <a class="info" title="What is a ${articleType.heading}?" href="#${articleType.code}">Info</a>
  </#if>
<#else>
  --!!ARTICLE TYPE CODE UNDEFINED!!--
</#if></p></div>