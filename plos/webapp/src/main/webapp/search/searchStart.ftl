<!-- begin : advanced search form -->
<div id="content" class="search">
  <!-- begin : right-hand column -->
  <div id="rhc">
    <div class="rhcBox">
      <h6>Quick Article Locator</h6>
      <form id="quickFind" name="quickFind" onsubmit="return true;" action="" method="post" enctype="multipart/form-data" class="" title="Quick Article Locator">
        <ol>
          <li>
            <fieldset>
              <legend><span>Go to a specific article fast if you know the following information.</span></legend>
              <label for="volNum">Volume#:</label>
              <input type="text" name="volNum" size="2" value="" id="volNum"/>,
              <label for="issueNum">Issue#:</label>
              <input type="text" name="issueNum" size="2" value="" id="issueNum"/>, <span class="noWrap"><em>and</em> <label for="eNum">E-Number:</label>
              <input type="text" name="eNum" size="15" value="" id="eNum"/></span>
            </fieldset>
          </li>
          <li>
            <label for="doi"><strong>or</strong>
            DOI: </label><input type="text" name="doi" size="25" value="" id="doi"/>
          </li>
          <li class="btnWrap"><input type="submit" id="button-find" value="Go"/></li>
        </ol>
      </form>              
    </div>
  </div>
  <!-- end : right-hand column -->
  <!-- begin : primary content area -->
  <div class="content">
    <h1>Advanced Search</h1>
    <p>Search the full text of all issues of <em>Current Journal Name</em></p>
    <@s.url id="advSearchURL" includeParams="none" namespace="/search" action="advancedSearch" />
    <form id="advSearchForm" name="advSearchForm" onsubmit="return true;" action="${advSearchURL}" 
          method="post" enctype="multipart/form-data" class="advSearch" title="Advanced Search">
      <fieldset id="author">
        <legend><span>Search by Author</span></legend>
        <ol>
          <li>
            <label for="authorName">Author Name (<a href="#">help</a>): </label>
            <input type="text" name="creator" size="35" value="" id="authorName"/>
            <span class="controls"><a href="#">Remove</a></span>
          </li>
          <li class="options">
            <input type="text" name="creator" size="35" value="" id="authorName2"/>
            <span class="controls"><a href="#">Remove</a> | <a href="#">Add another author...</a></span>
            <fieldset>
              <legend>Search for: </legend>
              <ol>
                <li><label><input type="radio" name="authorNameOption" value="any" checked="checked" /> <em>Any</em> of these authors</label></li>
                <li><label><input type="radio" name="authorNameOption" value="all"/> <em>All</em> of these</label></li>
              </ol>
            </fieldset>
          </li>
        </ol>
      </fieldset>
      <fieldset id="text">
        <legend><span>Search Article Text</span></legend>
        <ol>
          <li>
            <label for="textSearch-all">for <em>all</em> the words: </label>
            <input type="text" name="textSearchAll" size="50" value="" id="textSearch-all"/>
          </li>
          <li>
            <label for="textSearch-exactPhrase">for the <em>exact phrase</em>: </label>
            <input type="text" name="textSearchExactPhrase" size="50" value="" id="textSearch-exactPhrase"/>
          </li>
          <li>
            <label for="textSearch-atLeastOne">for <em>at least one</em> of the words: </label>
            <input type="text" name="textSearchAtLeastOne" size="50" value="" id="textSearch-atLeastOne"/>
          </li>
          <li>
            <label for="textSearch-without"><em>without</em> the words: </label>
            <input type="text" name="textSearchWithout" size="50" value="" id="textSearch-without"/>
          </li>
          <li>
            <label for="textSearch-where"><em>where</em> my words occur: </label>
            <select name="" id="textSearch-where">
              <option value="anywhere" selected="selected">Anywhere in the article</option>
              <option value="abstract">In the abstract</option>
              <option value="refs">In references</option>
              <option value="title">In the title</option>
            </select>
          </li>
        </ol>
      </fieldset>
      <fieldset id="dates">
        <legend><span>Dates</span></legend>
        <ol>
          <li>
            <label for="dateSelect">Published in the: </label>
            <span class="ie7fix"><!-- This wrapping span fixes wierd issue where IE7 ignores left margin on the select element when cursor enters browser canvas -->
              <select name="dateTypeSelect" id="dateSelect">
                <option value="week">Past week</option>
                <option value="month">Past month</option>
                <option value="3months">Past 3 months</option>
                <option value="range" SELECTED="true">Specify a date range...</option>
              </select>
            </span>
          </li>
          <li class="options">
            <fieldset>
              <legend>Published between: </legend>
              <ol>
                <li>
                  <span class="hide">(Year)</span><input type="text" name="startYear" size="4" maxlength="4" value="YYYY" id="range1"/>
                  <span class="hide">(Month)</span><input type="text" name="startMonth" size="2" maxlength="2" value="MM" id="range-m1"/>
                  <span class="hide">(Day)</span><input type="text" name="startDay" size="2" maxlength="2" value="DD" id="range-d2"/>
                  <label for="range2"> and </label>
                  <span class="hide">(Year)</span><input type="text" name="endYear" size="4" maxlength="4" value="YYYY" id="range2"/>
                  <span class="hide">(Month)</span><input type="text" name="endMonth" size="2" maxlength="2" value="MM" id="range-m2"/>
                  <span class="hide">(Day)</span><input type="text" name="endDay" size="2" maxlength="2" value="DD" id="range-d2"/>
                </li>
              </ol>
            </fieldset>
          </li>
        </ol>
      </fieldset>
      <fieldset id="subjCats">
        <legend><span>Subject Categories</span></legend>
        <ol>
          <li><label><input type="radio" name="subjectCatOpt" value="all" checked="checked" /> Search all subject catogories</label></li>
          <li class="options">
            <fieldset>
              <legend><label><input type="radio" name="subjectCatOpt" value="some" /> Only search in the following subject categories:</label></legend>
              <p>(#) indicates the number of articles published in each subject category.</p>
<#if categoryInfos?size gt 0>
<#assign colSize = (categoryInfos?size / 2) + 0.5>
              <ul>
<#list categoryInfos?keys as category>
<#if (category_index + 1) lte colSize>
<#assign categoryId = category?replace("\\s|\'","","r")>
<@s.url id="browseURL" action="browse" namespace="/article" catName="${category}" includeParams="none"/>
                <li>
                  <input name="limitToCategory" value="${categoryId}" type="checkbox"/>&nbsp;${category} (${categoryInfos[category]})
                </li>
</#if>
</#list>
              </ul>
              <ul>
<#list categoryInfos?keys as category>
<#if (category_index + 1) gt colSize>
<#assign categoryId = category?replace("\\s|\'","","r")>
<@s.url id="browseURL" action="browse" namespace="/article" catName="${category}" includeParams="none"/>
                <li>
                  <input name="limitToCategory" value="${categoryId}" type="checkbox"/>&nbsp;${category} (${categoryInfos[category]})
                </li>
</#if>
</#list>
              </ul>
<#else>
There are no subjects in the system. 
</#if>
            </fieldset>
          </li>
        </ol>
      </fieldset>
      <div class="btnwrap">
      <label for="results-sort">Sort results by: </label>
        <select id="results-sort">
          <option value="relevance">Relevance</option>
          <option value="chron-newFirst">Newest first</option>
          <option value="chron-oldFirst">Oldest first</option>
        </select>
        <input type="submit" id="button-search" value="Search"/>
      </div>
    </form>
  </div><!-- end : primary content area -->
</div><!-- end : advanced search form -->
