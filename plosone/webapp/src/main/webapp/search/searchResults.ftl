<!-- begin : main content wrapper -->
<#macro renderSearchPaginationLinks totalPages>
  <#if (totalPages > 1) >
    <#list 1..totalPages as pageNumber>
      
      <#if (startPage == (pageNumber-1))>
        ${pageNumber}
      <#else>
        <@ww.url id="searchPageURL" action="simpleSearch" namespace="/search" startPage="${pageNumber - 1}" pageSize="${pageSize}" query="${query}" includeParams="none"/>
        <@ww.a href="%{searchPageURL}">${pageNumber}</@ww.a>
      </#if>
      |
    </#list>
  </#if>
</#macro>

<div id="content">
 
<#assign totalPages=(totalNoOfResults/pageSize)?int>
  <#if (totalNoOfResults%pageSize > 0) >
    <#assign totalPages = totalPages + 1>
  </#if>
	<h1>Search Results</h1>

	<div id="search-results">
		<p>There
      <#if totalNoOfResults == 0>
        are <strong>${totalNoOfResults}</strong> results,
      <#elseif totalNoOfResults == 1>
        is <strong>${totalNoOfResults}</strong> result,
      <#else>
        are <strong>${totalNoOfResults}</strong> results, sorted by relevance,
      </#if>
      for <strong>${query}</strong>.</p>
	 <div class="resultsTab">
    <@renderSearchPaginationLinks totalPages/>
  	<#if totalNoOfResults gt 0>
	</div>
	<ul>
			<#list searchResults as hit>
			<li>
				<span class="date">Published ${hit.date?string("dd MMM yyyy")}</span>
				<span class="article">
            <#if hit.contentModel == "PlosArticle">
              <@ww.url id="fetchArticleURL" action="fetchArticle" namespace="/article" articleURI="${hit.pid}" includeParams="none"/>
              <@ww.a href="%{fetchArticleURL}" title="Read Open Access Article">${hit.title}</@ww.a></span>
            <#else>
              <a href="#">${hit.title}</a>
            </#if>
        </span>       
	   <span class="authors"> <!-- hitScore: ${hit.hitScore} -->
${hit.creator}</span>
		<span class="cite">${hit.highlight}</span>

</li>
			</#list>
		</ul>
	<div class="resultsTab">
    <@renderSearchPaginationLinks totalPages/>
	</div>
	</#if>
	</div>
</div>
<!-- end : main content wrapper -->
