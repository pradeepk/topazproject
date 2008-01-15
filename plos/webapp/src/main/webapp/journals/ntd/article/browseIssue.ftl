<#macro related articleInfo>
  <#if articleInfo.relatedArticles?size gt 0>
    <dl class="related">
      <dt>Related <em>PLoS</em> Articles</dt>
      <#list articleInfo.relatedArticles as ra>
      <@s.url id="fetchArticleURL" action="fetchArticle" namespace="/article" articleURI="${ra.uri}" includeParams="none"/>
      <dd><@s.a href="%{fetchArticleURL}" title="Read Open Access Article">${ra.title}</@s.a></dd>
      </#list>
     </dl>
  </#if>
</#macro>

<!-- begin : toc content -->
<div id="content" class="toc">
<a id="top" title="top" class="noshow">&nbsp;</a>
  <!-- begin : right-hand column -->
  <div id="rhc">
    <div id="sideNav">
    <@s.url id="archiveURL" action="browseVolume" namespace="/journals/ntd/article" includeParams="gotoVolume=${issueInfo.parentVolume}"/>
      <p id="issueNav">
        <#assign needSpacer=false/>
        <#if issueInfo.prevIssue?exists>
          <@s.url id="prevIssueURL" action="browseIssue" namespace="/journals/ntd/article" issue="${issueInfo.prevIssue}" includeParams="none"/>
          <a href="${prevIssueURL}">&lt;Previous Issue</a>
          <#assign needSpacer=true/>
        </#if>
        <#if needSpacer> | </#if>
        <a href="${archiveURL}">Archive</a>
        <#assign needSpacer=true/>
        <#if issueInfo.nextIssue?exists>
          <@s.url id="nextIssueURL" action="browseIssue" namespace="/journals/ntd/article" issue="${issueInfo.nextIssue}" includeParams="none"/>
          <!-- <#if needSpacer> | </#if> -->
          <a href="${nextIssueURL}">Next Issue&gt;</a>
          <#assign needSpacer=true/>
        </#if>
      </p>
      <div id="floatMarker">&nbsp;</div>
      <div id="postcomment">
        <div id="sectionNavTop" class="tools">
          <ul>
            <li><a class="first" href="#top">Top</a></li>
            <#list articleGroups as articleGrp>
              <li><a href="#${articleGrp.id}">${articleGrp.heading}</a></li>
            </#list>
          </ul>
        </div><!-- end : sectionNav -->
      </div>
      <div id="postcommentfloat" class="fixed">
        <div id="sectionNavTopFloat" class="tools">
          <ul>
            <li><a class="first" href="#top">Top</a></li>
            <#list articleGroups as articleGrp>
              <li><a href="#${articleGrp.id}">${articleGrp.heading}</a></li>
            </#list>
          </ul>
        </div>
      </div><!-- end : postcomment -->
    </div><!-- end : sideNav -->
  </div><!-- end : right-hand column -->
  <!-- begin : primary content area -->
  <div class="content">
  <h1>Table of Contents | ${issueInfo.displayName}</h1>
    <#if issueInfo.imageArticle?has_content>
      <@s.url id="imageSmURL" action="fetchObject" namespace="/article" uri="${issueInfo.imageArticle}.g001" representation="PNG_S" includeParams="none"/>
      <@s.url id="imageLgURL" action="slideshow" namespace="/article" uri="${issueInfo.imageArticle}" imageURI="${issueInfo.imageArticle}.g001" includeParams="none"/>
      <div id="issueImage">
        <div id="thumbnail">
	<a href="${imageLgURL}" onclick="window.open(this.href,'plosSlideshow','directories=no,location=no,menubar=no,resizable=yes,status=no,scrollbars=yes,toolbar=no,height=600,width=850');return false;">
	  	<img alt="Issue Image" src="${imageSmURL}"/>
	  </a>
	  <a href="${imageLgURL}" onclick="window.open(this.href,'plosSlideshow','directories=no,location=no,menubar=no,resizable=yes,status=no,scrollbars=yes,toolbar=no,height=600,width=850');return false;">
	  	View large image
	  </a>
        </div>
        <h3>About This Image</h3>
        ${issueDescription}
      </div>
    </#if>
    <div class="clearer">&nbsp;</div>
    <!-- begin : articleTypes -->
    <div id="articleTypeList">
      <#list articleGroups as articleGrp>
        <a id="${articleGrp.id}" class="noshow" title="${articleGrp.heading}">&nbsp;</a>
        <h2>${articleGrp.heading}</h2>
        <#list articleGrp.articles as articleInfo>
          <div class="article">
            <@s.url id="fetchArticleURL" action="fetchArticle" namespace="/article" articleURI="${articleInfo.id}" includeParams="none"/>
            <h3><@s.a href="%{fetchArticleURL}" title="Read Open Access Article">${articleInfo.title}</@s.a></h3>
            <p class="authors"><#list articleInfo.authors as auth><#if auth_index gt 0>, </#if>${auth}</#list></p>
            <@related articleInfo=articleInfo/>
          </div>
        </#list>
        </#list>
          </div>
    <!-- end : articleTypes -->
  </div>
</div> <!-- end : toc content-->


