<div>
	<h1>Annotations and Discussions</h1>
	<@ww.url namespace="/article" includeParams="none" id="articleURL" action="fetchArticle" articleURI="${articleInfo.uri}"/>

	<div class="source">
		<span>Original Article</span><a href="${articleURL}" title="Back to original article" class="article icon">${articleInfo.title}</a>
	</div>



	<dl class="directory">
	<#list allCommentary as comment>
  	<#if comment.annotation.context?length == 0>
  		<#assign class="discuss"/>
	 	<#else>
  		<#assign class="annotation"/>
	 	</#if>
  	<@ww.url namespace="/annotation" includeParams="none" id="listThreadURL" action="listThread" root="${comment.annotation.id}" inReplyTo="${comment.annotation.id}"/>
  	<@ww.url namespace="/user" includeParams="none" id="showUserURL" action="showUser" userId="${comment.annotation.creator}"/>
	
		<dt><a href="${listThreadURL}" title="View Full Discussion Thread" class="${class} icon">${comment.annotation.commentTitle}</a></dt>
		<dd>Posted by <a href="${showUserURL}" title="Discussion Author" class="user icon">${comment.annotation.creatorName}</a> on <strong>${comment.annotation.createdAsDate?string("yyyy-MM-dd")}</strong> at <strong>${comment.annotation.createdAsDate?string("HH:mm")} GMT</strong></dd>
		<dd class="replies">${comment.replies?size} replies</dd>
	</#list>
	</dl>
</div>