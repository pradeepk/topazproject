<#if Request[freemarker_config.journalContextAttributeKey]?exists>
	<#assign journalContext = Request[freemarker_config.journalContextAttributeKey]>
<#else>
	<#assign journalContext = "">
</#if>
<#assign pgTitle = freemarker_config.getTitle(templateFile)>
<#if pgTitle = "CODE_ARTICLE_TITLE"> <#--to get article title in w/o a new template for now-->
	<#assign pgTitle = freemarker_config.getArticleTitlePrefix(journalContext) + articleInfo.title?replace('</?[a-z]*>', '', 'r')>
</#if>
	<title>${pgTitle}</title>


<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />

<link rel="shortcut icon" href="${freemarker_config.context}/images/pone_favicon.ico" type="image/x-icon" />
<link rel="home" title="home" href="http://${freemarker_config.plosOneHost}${freemarker_config.context}"></link>
<link rel="alternate" type="application/rss+xml" title="${freemarker_config.getArticleTitlePrefix(journalContext)} New Articles" href="http://${freemarker_config.plosOneHost}${freemarker_config.context}/rss/PLoSONE.xml" />

<#include "../css/global_css.ftl">
<#include "../javascript/global_js.ftl">

<meta name="description" content="${freemarker_config.getMetaDescription(journalContext)}" />

<meta name="keywords" content="${freemarker_config.getMetaKeywords(journalContext)}" />

<@s.url id="pgURL" includeParams="get" includeContext="true" encode="false"/>
<#assign rdfPgURL = pgURL?replace("&amp;", "&")>

<!--
<rdf:RDF xmlns="http://web.resource.org/cc/"
   xmlns:dc="http://purl.org/dc/elements/1.1/"
   xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
<Work rdf:about="http://${freemarker_config.plosOneHost}${rdfPgURL}">
   <license rdf:resource="http://creativecommons.org/licenses/by/2.5/" />
</Work>
<License rdf:about="http://creativecommons.org/licenses/by/2.5/">
   <permits rdf:resource="http://web.resource.org/cc/Reproduction" />
   <permits rdf:resource="http://web.resource.org/cc/Distribution" />
   <requires rdf:resource="http://web.resource.org/cc/Notice" />
   <requires rdf:resource="http://web.resource.org/cc/Attribution" />
   <permits rdf:resource="http://web.resource.org/cc/DerivativeWorks" />
</License>
</rdf:RDF>
-->
