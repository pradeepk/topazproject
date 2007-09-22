<div id="content" class="browse static">
  <!-- begin : banner -->
  <div id="bannerRight">
    <script language='JavaScript' type='text/javascript'src='http://ads.plos.org/adx.js'></script>
    <script language='JavaScript' type='text/javascript'>
      <!--
        if (!document.phpAds_used) document.phpAds_used = ',';
        phpAds_random = new String (Math.random()); 
        phpAds_random = phpAds_random.substring(2,11);

        document.write ("<" + "script language='JavaScript'   type='text/javascript' src='");
        document.write ("http://ads.plos.org/adjs.php?n=" + phpAds_random);
        document.write ("&amp;what=zone:177&amp;source=ONE&amp;withText=1&amp;block=1");
        document.write ("&amp;exclude=" + document.phpAds_used);
        if (document.referrer)
          document.write ("&amp;referer=" + escape(document.referrer));
        document.write ("'><" + "/script>");
      //-->
    </script>
    <noscript>
      <a href='http://ads.plos.org/adclick.php?n=a98abd23' target='_blank'><img src='http://ads.plos.org/adview.php?what=zone:177&amp;source=ONE&amp;n=a98abd23' border='0' alt='' /></a>
    </noscript>
  </div>
  <!-- end : banner -->

  <@s.url id="archiveURL" action="browseVolume" namespace="/journals/ntd/article"
    field="volume" includeParams="none"/>
  <p style="text-align: right">
    <#assign needSpacer=false/>
    <#if issueInfo.prevIssue?exists>
      <@s.url id="prevIssueURL" action="browseIssue" namespace="/journals/ntd/article"
        field="issue" issue="${issueInfo.prevIssue}" includeParams="none"/>
      <a href="${prevIssueURL}">&lt;Previous Issue</a>
      <#assign needSpacer=true/>
    </#if>
    <#if needSpacer> | </#if>
    <a href="${archiveURL}">Archive</a>
    <#assign needSpacer=true/>
    <#if issueInfo.nextIssue?exists>
      <@s.url id="nextIssueURL" action="browseIssue" namespace="/journals/ntd/article"
        field="issue" issue="${issueInfo.nextIssue}" includeParams="none"/>
      <#if needSpacer> | </#if>
      <a href="${nextIssueURL}">Next Issue&gt;</a>
      <#assign needSpacer=true/>
    </#if>
  </p>

  <h1>Table of Contents | ${issueInfo.displayName}</h1>

  <#if issueInfo.imageArticle?exists>
    <@s.url id="imageSmURL" action="fetchObject" namespace="/article"
      uri="${issueInfo.imageArticle}.g001" representation="PNG_S" includeParams="none"/>
    <@s.url id="imageLgURL" action="slideshow" namespace="/article"
      uri="${issueInfo.imageArticle}" imageURI="${issueInfo.imageArticle}.g001"
      includeParams="none"/>
    <p>
      <img class="thumbnail" border="1" align="left" alt="thumbnail" src="${imageSmURL}""/>
      ${issueInfo.description}<br/>
      <a href="${imageLgURL}">View larger image</a>
    </p>
  </#if>

  <div id="search-results">
    <#if issueInfo.editorials?exists>
      <h2>Editorial</h2>
      <hr/>

      <ul>
        <#list issueInfo.editorials as articleInfo>
          <li>
            <@s.url id="fetchArticleURL" action="fetchArticle" namespace="/article" articleURI="${articleInfo.id}" includeParams="none"/>
            <span class="article"><@s.a href="%{fetchArticleURL}" title="Read Open Access Article">${articleInfo.title}</@s.a></span>
            <span class="authors">
              <#list articleInfo.authors as auth><#if auth_index gt 0>, </#if>${auth}</#list>
            </span>
          </li>
        </#list>
      </ul>
    </#if>

    <#if issueInfo.researchArticles?exists>
      <h2>Research Articles</h2>
      <hr/>

      <ul>
        <#list issueInfo.researchArticles as articleInfo>
          <li>
            <@s.url id="fetchArticleURL" action="fetchArticle" namespace="/article" articleURI="${articleInfo.id}" includeParams="none"/>
            <span class="article"><@s.a href="%{fetchArticleURL}" title="Read Open Access Article">${articleInfo.title}</@s.a></span>
            <span class="authors">
              <#list articleInfo.authors as auth><#if auth_index gt 0>, </#if>${auth}</#list>
            </span>
          </li>
        </#list>
      </ul>
    </#if>

    <#if issueInfo.corrections?exists>
      <h2>Corrections</h2>
      <hr/>

      <ul>
        <#list issueInfo.corrections as articleInfo>
          <li>
            <@s.url id="fetchArticleURL" action="fetchArticle" namespace="/article" articleURI="${articleInfo.id}" includeParams="none"/>
            <span class="article"><@s.a href="%{fetchArticleURL}" title="Read Open Access Article">${articleInfo.title}</@s.a></span>
            <span class="authors">
              <#list articleInfo.authors as auth><#if auth_index gt 0>, </#if>${auth}</#list>
            </span>
          </li>
        </#list>
      </ul>
    </#if>
  </div> <!-- search results -->
</div> <!--content-->
