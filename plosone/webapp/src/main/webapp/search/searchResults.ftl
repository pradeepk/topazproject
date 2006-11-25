<!-- begin : main content wrapper -->
<div id="content">
 
<#assign totalPages=(totalNoOfResults/pageSize)?int>
  <#if (totalNoOfResults%pageSize > 0) >
  <#assign totalPages = totalPages + 1>
</#if>
	<h2>Search Results</h2>

	<div id="search-results">
		<p><strong>There were ${totalNoOfResults} results for &quot;${query}&quot;</strong></p>
  	<#if (totalPages > 1) >
    	<#list 1..totalPages as pageNumber>
      	&lt;
	      <@ww.url id="searchPageURL" action="simpleSearch" namespace="/search" startPage="${pageNumber - 1}" pageSize="${pageSize}" query="${query}"/>
  	    <@ww.a href="%{searchPageURL}">${pageNumber}</@ww.a>
    	  &gt;&nbsp;
	    </#list>
  	</#if>
  	<#if totalNoOfResults gt 0>
		<table>
			<tr>
				<th class="results-title">Title</th>
				<th class="results-date">Date</th>
				<th class="results-author">Author</th>
			</tr>
			<#list searchResults as hit>
			<tr>
				<td class="results-title">
					<span class="results-item-title"><a href="#">${hit.title}</a></span>
					<span class="results-item-preview">${hit.description}</span>
				</td>
				<td class="results-date">${hit.date?string("yyyy-MM-dd")}</td>
				<td class="results-author">${hit.creator}</td>
			</tr>
			</#list>
		</table>
		</#if>
	</div>
</div>
<!-- end : main content wrapper -->
