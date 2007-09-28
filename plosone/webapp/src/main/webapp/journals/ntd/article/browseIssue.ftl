<div id="content" class="toc">
	<!-- begin : right-hand column -->
	<div id="rhc">
		<!--<div id="sideNav">-->
			<@s.url id="archiveURL" action="browseVolume" namespace="/journals/ntd/article"
				field="volume" includeParams="none"/>
			<p id="issueNav">
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
		<!--</div>-->
		<div id="sectionNavTop" class="tools">
			<ul>
				<li><a href="#top">Top</a></li>
				<li><a href="#">Editorial</a></li>
				<li><a href="#">Research Articles</a></li>
				<li><a class="last" href="#">Corrections</a></li>
			</ul>
		</div>
	</div><!-- end : right-hand column -->
	<div class="content">
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
				<#list issueInfo.editorials as articleInfo>
					<div class="article">
						<@s.url id="fetchArticleURL" action="fetchArticle" namespace="/article" articleURI="${articleInfo.id}" includeParams="none"/>
						<h3><@s.a href="%{fetchArticleURL}" title="Read Open Access Article">${articleInfo.title}</@s.a></h3>
						<p class="authors"><#list articleInfo.authors as auth><#if auth_index gt 0>, </#if>${auth}</#list></p>
						<dl class="related"> <!-- Definition list for related articles should only appear if there are one or more related articles -->
							<dt>Related PLoS Articles</dt>
							<dd><a href="#">Lorem Ipsum Dolor sit Amet Consectetuer Adipiscing Elit Sed Diam Nonummy</a></dd>
							<dd><a href="#">Duis Autem vel Eum Iriure Dolor in Hendrerit in Vulputate Velit Esse Molestie Consequat</a></dd>
						</dl>
					</div>
				</#list>
			</#if>
	
			<#if issueInfo.researchArticles?exists>
				<h2>Research Articles</h2>
				<#list issueInfo.researchArticles as articleInfo>
					<div class="article">
						<@s.url id="fetchArticleURL" action="fetchArticle" namespace="/article" articleURI="${articleInfo.id}" includeParams="none"/>
						<h3><@s.a href="%{fetchArticleURL}" title="Read Open Access Article">${articleInfo.title}</@s.a></h3>
						<p class="authors"><#list articleInfo.authors as auth><#if auth_index gt 0>, </#if>${auth}</#list></p>
						<p><a href="#">Author Summary</a></p> <!-- This should only appear if there is an author summary -->
						<dl class="related"> <!-- Definition list for related articles should only appear if there are one or more related articles -->
							<dt>Related PLoS Articles</dt>
							<dd><a href="#">Lorem Ipsum Dolor sit Amet Consectetuer Adipiscing Elit Sed Diam Nonummy</a></dd>
							<dd><a href="#">Duis Autem vel Eum Iriure Dolor in Hendrerit in Vulputate Velit Esse Molestie Consequat</a></dd>
						</dl>
					</div>
				</#list>
			</#if>
	
			<#if issueInfo.corrections?exists>
				<h2>Corrections</h2>
				<#list issueInfo.corrections as articleInfo>
					<div class="article">
						<@s.url id="fetchArticleURL" action="fetchArticle" namespace="/article" articleURI="${articleInfo.id}" includeParams="none"/>
						<h3><@s.a href="%{fetchArticleURL}" title="Read Open Access Article">${articleInfo.title}</@s.a></h3>
						<p class="authors"><#list articleInfo.authors as auth><#if auth_index gt 0>, </#if>${auth}</#list></p>
						<p><a href="#">Author Summary</a></p> <!-- This should only appear if there is an author summary -->
						<dl class="related"> <!-- Definition list for related articles should only appear if there are one or more related articles -->
							<dt>Related PLoS Articles</dt>
							<dd><a href="#">Lorem Ipsum Dolor sit Amet Consectetuer Adipiscing Elit Sed Diam Nonummy</a></dd>
							<dd><a href="#">Duis Autem vel Eum Iriure Dolor in Hendrerit in Vulputate Velit Esse Molestie Consequat</a></dd>
						</dl>
					</div>
				</#list>
			</#if>
		</div> <!-- search results -->
	</div>
</div> <!--content-->
