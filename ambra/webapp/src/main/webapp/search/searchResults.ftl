<#--
  $HeadURL::                                                                            $
  $Id$
  
  Copyright (c) 2007-2008 by Topaz, Inc.
  http://topazproject.org
  
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
  
  http://www.apache.org/licenses/LICENSE-2.0
  
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->
<!-- begin : main content wrapper -->
<#macro renderSearchPaginationLinks totalPages, hasMore>
  <#if (startPage gt 0)>
    <@s.url id="prevPageURL" action="simpleSearch" namespace="/search" startPage="${startPage - 1}" pageSize="${pageSize}" query="${query}" includeParams="none"/>
    <@s.a href="%{prevPageURL}">&lt; Prev</@s.a> |
  </#if>
  <#if (totalPages > 1) >
    <#list 1..totalPages as pageNumber>
      <#if (startPage == (pageNumber-1))>
        ${pageNumber}
      <#else>
        <@s.url id="searchPageURL" action="simpleSearch" namespace="/search" startPage="${pageNumber - 1}" pageSize="${pageSize}" query="${query}" includeParams="none"/>
        <@s.a href="%{searchPageURL}">${pageNumber}</@s.a>
      </#if>
      <#if pageNumber != totalPages>|</#if>
    </#list>
  </#if>
  <#if hasMore == 1>
    <#if (startPage gt 0)> | </#if>
    <@s.url id="nextPageURL" action="simpleSearch" namespace="/search" startPage="${startPage + 1}" pageSize="${pageSize}" query="${query}" includeParams="none"/>
    <@s.a href="%{nextPageURL}">Next &gt;</@s.a> 
  </#if>
</#macro>

<div id="content" class="static">

  <#-- We don't really know the total number of hits (or even if we've seen the total
       number yet. So we will only show the number of pages up to the hit count we've
       got so far. (This would best be done in code... and we ought to be passing in
       "knownHits" so when a user goes backward we can still know totoal hits) -->
  <#assign noOfResults = (startPage + 1) * pageSize>
  <#if (noOfResults >= totalNoOfResults)>
    <#assign noOfResults = totalNoOfResults>
    <#assign hasMore = 0>
  <#else>
    <#assign hasMore = 1>
  </#if>

  <#-- Compute number of pages -->
  <#assign totalPages = (noOfResults / pageSize)?int>
  <#if (noOfResults % pageSize > 0) >
    <#assign totalPages = totalPages + 1>
  </#if>

  <h1>Search Results</h1>

  <div id="search-results">
<#--  <p>Debug: noOfResults: ${noOfResults}, totalNoOfResults: ${totalNoOfResults}, startPage: ${startPage}, pageSize: ${pageSize}, hasMore: ${hasMore}</p> -->

    <#if noOfResults == 0>
      <p>There are no results for <strong>${query?html}</strong>.</p>
    <#else>
      <#assign startIndex = startPage * pageSize>
      <p>
      Viewing results
      <strong>
        ${startIndex + 1} -

        <#if startPage == totalPages - 1>
          ${noOfResults}
        <#else>
          ${startIndex + pageSize}
        </#if>

      </strong>

      <#if totalNoOfResults == noOfResults>
        of <strong>${totalNoOfResults}</strong> results, sorted by relevance,
      <#else>
        sorted by relevance,
      </#if>

      for <strong>${query?html}</strong>.
      </p>
    </#if>
    
    <@s.url id="searchHelpURL" includeParams="none" namespace="/static" action="searchHelp" />
    <@s.url id="advancedSearchURL" includeParams="none" namespace="/search" action="advancedSearch" />
    <div id="searchMore">
      <form name="reviseSearch" action="${advancedSearchURL}" method="get">
        <@s.hidden name="noSearchFlag" value="set" />
        <@s.hidden name="creator" />
        <@s.hidden name="authorNameOp" />
        <@s.hidden name="textSearchAll" />
        <@s.hidden name="textSearchExactPhrase" />
        <@s.hidden name="textSearchAtLeastOne" />
        <@s.hidden name="textSearchWithout" />
        <@s.hidden name="textSearchOption" />
        <@s.hidden name="dateTypeSelect" />
        <@s.hidden name="startYear" />
        <@s.hidden name="startMonth" />
        <@s.hidden name="startDay" />
        <@s.hidden name="endYear" />
        <@s.hidden name="endMonth" />
        <@s.hidden name="endDay" />
        <@s.hidden name="subjectCatOpt" />
        <@s.hidden name="limitToCategory" />
      </form>
      <form name="reviseQuery" action="${searchURL}" method="get">
        <@s.hidden name="noSearchFlag" value="set" />
        <@s.hidden name="creator" />
        <@s.hidden name="authorNameOp" />
        <@s.hidden name="textSearchAll" />
        <@s.hidden name="textSearchExactPhrase" />
        <@s.hidden name="textSearchAtLeastOne" />
        <@s.hidden name="textSearchWithout" />
        <@s.hidden name="textSearchOption" />
        <@s.hidden name="dateTypeSelect" />
        <@s.hidden name="startYear" />
        <@s.hidden name="startMonth" />
        <@s.hidden name="startDay" />
        <@s.hidden name="endYear" />
        <@s.hidden name="endMonth" />
        <@s.hidden name="endDay" />
        <@s.hidden name="subjectCatOpt" />
        <@s.hidden name="limitToCategory" />
        <a href="#" onclick="document.reviseSearch.submit();return false;">Go to Advanced Search</a> to revise <em>or</em>
        <label for="searchEdit" style="display:inline;">edit your query here</label>
        (<a href="${searchHelpURL}">help</a>):&nbsp;
        <input type="text" size="50" value="${query?html}" id="searchEdit" name="query"/>
        <input type="submit"  value="Go" class="button"/>
      </form>
    </div>

    <div class="resultsTab"><@renderSearchPaginationLinks totalPages, hasMore/></div>

    <#if totalNoOfResults gt 0>
      <ul>
        <#list searchResults as hit>
          <li>
            <span class="date">Published ${hit.date?string("dd MMM yyyy")}</span>
            <span class="article">
              <#if hit.contentModel == "PlosArticle">
                <a href="/ambra-doi-resolver/${hit.pid?replace('info:doi/','')}">${hit.title}</a>
              <#else>
                <a href="#">${hit.title}</a>
              </#if>
            </span>       
            <span class="authors"> <!-- hitScore: ${hit.hitScore} --> ${hit.creator!""}</span>
            <span class="cite">${hit.highlight}</span>
          </li>
        </#list>
      </ul>
      <div class="resultsTab"><@renderSearchPaginationLinks totalPages, hasMore/></div>
    </#if>

  </div> <!-- search-results -->
</div> <!-- content -->
<!-- end : main content wrapper -->
