<#--
  $HeadURL$
  $Id$

  Copyright (c) 2006-2008 by Topaz, Inc.
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
<#list citation.authors as author>
  <#if (author_index > 4) >
    <span class="citation_author">et al. </span>
    <#break>
  </#if>
  <span class="citation_author">${author.surnames!}<#if author.givenNames??>
      <@abbreviation>${author.givenNames!}</@abbreviation></#if><#if author.suffix??>
      ${author.suffix}</#if>,
  </span>
</#list>
<span class="citation_date">${citation.year?string('0000')}</span>
<span class="citation_article_title"><@articleFormat>${citation.title}</@articleFormat>. </span>
<span class="citation_journal_title">${citation.journal!} </span>
<span class="citation_issue">${citation.volume}(${citation.issue}):</span>
<span class="citation_start_page">${citation.ELocationId!}.</span>
<span class="citation_doi">doi:${citation.doi}</span>
