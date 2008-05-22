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
<#macro pagination>
  <#assign totalPages = (totalArticles/pageSize)?int>
  <#if totalArticles % pageSize != 0>
    <#assign totalPages = totalPages + 1>
  </#if>
  <#if (totalPages gt 1) >
    <div class="resultsTab">
      <#if (startPage gt 0)>
        <@s.url id="prevPageURL" action="browse" namespace="/article" startPage="${startPage - 1}" pageSize="${pageSize}" includeParams="get"/>
        <@s.a href="%{prevPageURL}">&lt; Prev</@s.a> |
      </#if>
      <#list 1..totalPages as pageNumber>
        <#if (startPage == (pageNumber-1))>
          <strong>${pageNumber}</strong>
        <#else>
          <@s.url id="browsePageURL" action="browse" namespace="/article" startPage="${pageNumber - 1}" pageSize="${pageSize}" field="${field}" includeParams="get"/>
          <@s.a href="%{browsePageURL}">${pageNumber}</@s.a>
        </#if>
        <#if pageNumber != totalPages>|</#if>
      </#list>
      <#if (startPage lt totalPages - 1 )>
        <@s.url id="nextPageURL" action="browse" namespace="/article" startPage="${startPage + 1}" pageSize="${pageSize}" field="${field}" includeParams="get"/>
        <@s.a href="%{nextPageURL}">Next &gt;</@s.a>
      </#if>
    </div> <!-- results tab-->
  </#if>
</#macro>

<div id="content" class="browse static">
  <!-- begin : banner -->
  
  <#-- set banner codes per journal/page -->
  
	<#assign source = 'PHUBCT'>
 	<#if field == "date">
  	<#assign skyscraper = 217>
	<#else>
		<#assign skyscraper = 218>
	</#if>
  
  <div id="bannerRight">
    <script language='JavaScript' type='text/javascript'src='http://ads.plos.org/adx.js'></script>
    <script language='JavaScript' type='text/javascript'>
      <!--
        if (!document.phpAds_used) document.phpAds_used = ',';
        phpAds_random = new String (Math.random());
        phpAds_random = phpAds_random.substring(2,11);

        document.write ("<" + "script language='JavaScript'   type='text/javascript' src='");
        document.write ("http://ads.plos.org/adjs.php?n=" + phpAds_random);
        document.write ("&amp;what=zone:${skyscraper}&amp;source=${source}&amp;withText=1&amp;block=1");
        document.write ("&amp;exclude=" + document.phpAds_used);
        if (document.referrer)
          document.write ("&amp;referer=" + escape(document.referrer));
        document.write ("'><" + "/script>");
      //-->
    </script>
    <noscript>
      <a href='http://ads.plos.org/adclick.php?n=a98abd23' target='_blank'><img src='http://ads.plos.org/adview.php?what=zone:${skyscraper}&amp;source=${source}&amp;n=a98abd23' border='0' alt='' /></a>
    </noscript>
  </div>
  <!-- end : banner -->
  <h1>Browse Articles</h1>
  <#if field == "date">
    <#include "/article/browseNavDate.ftl">
  <#else>
    <#include "/article/browseNavSubject.ftl">
  </#if>

  <#assign startPgIndex = startPage * pageSize>
  <#assign endPgIndex = startPgIndex + pageSize - 1>
  <#if endPgIndex gte totalArticles>
    <#assign endPgIndex = totalArticles - 1 >
  </#if>

  <div id="search-results">
    <#if endPgIndex gte 0><p><strong>${startPgIndex + 1} - ${endPgIndex + 1}</strong> of </#if><strong>${totalArticles}</strong> article<#if totalArticles != 1>s</#if> published ${infoText}.</p>
    <@pagination />
    <ul>
      <#list articleList as art>
        <li>
          <span class="date">Published ${art.date?string("dd MMM yyyy")}</span>
          <@s.url id="fetchArticleURL" action="fetchArticle" namespace="/article" articleURI="${art.id}" includeParams="none"/>
          <span class="article"><@s.a href="%{fetchArticleURL}" title="Read Open Access Article">${art.title}</@s.a></span>
          <span class="authors">
            <#list art.authors as auth><#if auth_index gt 0>, </#if>${auth}</#list>
          </span>
        </li>
      </#list>
    </ul>
    <@pagination />
  </div> <!-- search results -->
</div> <!--content-->
