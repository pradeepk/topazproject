<#macro pagination>
	<#assign totalResults = articleList?size>
	<#assign totalPages = (totalResults/pageSize)?int>
	<#if totalResults % pageSize != 0>
		<#assign totalPages = totalPages + 1>
	</#if>
  <#if (totalPages gt 1) >
    <#if (startPage gt 0)>
     	<@ww.url id="prevPageURL" action="browse" namespace="/article" startPage="${startPage - 1}" pageSize="${pageSize}" includeParams="get"/>
      <@ww.a href="%{prevPageURL}">&lt; Prev</@ww.a> |
    </#if>
    <#list 1..totalPages as pageNumber>
      <#if (startPage == (pageNumber-1))>
      	<strong>${pageNumber}</strong>
      <#else>
      	<@ww.url id="browsePageURL" action="browse" namespace="/article" startPage="${pageNumber - 1}" pageSize="${pageSize}" field="${field}" includeParams="get"/>
      	<@ww.a href="%{browsePageURL}">${pageNumber}</@ww.a>
      </#if>
      <#if pageNumber != totalPages>|</#if>
    </#list>
    <#if (startPage lt totalPages - 1 )>
     	<@ww.url id="nextPageURL" action="browse" namespace="/article" startPage="${startPage + 1}" pageSize="${pageSize}" field="${field}" includeParams="get"/>
       <@ww.a href="%{nextPageURL}">Next &gt;</@ww.a> 
    </#if>
    
  </#if>
</#macro>


<div id="content" class="browse static">
	<h1>Browse Articles</h1>
	<#if field == "date">
		<#include "browseNavDate.ftl">
	<#else>
		<#include "browseNavSubject.ftl">
	</#if>

	<#assign totalResults = articleList?size>
	<#assign startIndex = startPage * pageSize>
	<#assign endIndex = startIndex + pageSize - 1>
	<#if endIndex gte totalResults>
		<#assign endIndex = totalResults - 1 >
	</#if>
	<div id="search-results">	<p><strong>${startIndex + 1} - ${endIndex + 1}</strong> of <strong>${totalResults}</strong> article<#if totalResults != 1>s</#if> published ${infoText}.</p>
		<div class="resultsTab">
			<@pagination />
		</div> <!-- results tab-->
		<ul>

		  <#list startIndex .. endIndex as idx>
				<#assign art = articleList[idx]>
				<li>
					<span class="date">Published ${art.articleDate?string("dd MMM yyyy")}</span>
          <@ww.url id="fetchArticleURL" action="fetchArticle" namespace="/article" articleURI="${art.uri}" includeParams="none"/>
					<span class="article"><@ww.a href="%{fetchArticleURL}" title="Read Open Access Article">${art.title}</@ww.a></span>
					<span class="authors">
						<#list art.authors as auth><#if auth_index gt 0>, </#if>${auth}</#list>
					</span>
				</li>
			</#list>
		</ul>

		<div class="resultsTab">
			<@pagination />
		</div> <!-- results tab-->
	</div> <!-- search results -->
</div> <!--content-->

