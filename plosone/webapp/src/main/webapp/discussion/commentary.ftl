<div id="content">
	<h1>Annotations and Discussions</h1>
	<@ww.url namespace="/article" includeParams="none" id="articleURL" action="fetchArticle" articleURI="${articleInfo.id}"/>
	<@ww.url namespace="/annotation/secure" includeParams="none" id="startDiscussionUrl" action="startDiscussion" target="${articleInfo.id}"/>

	<div class="source">
		<span>Original Article</span><a href="${articleURL}" title="Back to original article" class="article icon">${articleInfo.title}</a>
	</div>
	<table class="directory" cellpadding="0" cellspacing="0">
	<#if allCommentary?size == 0>
	<p>There are currently no annotations or discussions yet on this article. 
	You can <a href="${startDiscussionUrl}" title="Click to start a discussion on this article" class="discuss icon">start a discussion</a> or return to the original article to add an annotation.<p>
	<#else>


	<#list allCommentary as comment>
    <#if ((comment.annotation.context)!"")?length == 0>
   		<#assign class="discuss"/>
	 	<#else>
  		<#assign class="annotation"/>
	 	</#if>
	 	<#assign numReplies = comment.numReplies>
	 	<#if numReplies != 1>
	 		<#assign label = "responses">
	 	<#else>
	 		<#assign label = "response">
	 	</#if>
  	<@ww.url namespace="/annotation" includeParams="none" id="listThreadURL" action="listThread" root="${comment.annotation.id}" inReplyTo="${comment.annotation.id}"/>
  	<@ww.url namespace="/user" includeParams="none" id="showUserURL" action="showUser" userId="${comment.annotation.creator}"/>
		<td class="replies">${comment.numReplies} ${label}<br /></td>
		<td class="title"><a href="${listThreadURL}" title="View Full Discussion Thread" class="${class} icon">${comment.annotation.commentTitle}</a></td>
		<td class="info">Posted by <a href="${showUserURL}" title="Discussion Author" class="user icon">${comment.annotation.creatorName}</a> on <strong>${comment.annotation.createdAsDate?string("dd MMM yyyy '</strong>at<strong>' HH:mm zzz")}</strong></td>
	</tr>
	<tr><td colspan="4" class="last">Most recent response on <strong>${comment.lastModifiedAsDate?string("dd MMM yyyy '</strong>at<strong>' HH:mm zzz")}</strong></td>
	<tr>
	</#list>
	</#if>
	</table>
	
	<#if allCommentary?size gt 0>
	<p>You can also <a href="${startDiscussionUrl}" title="Click to start a discussion on this article" class="discuss icon">start a new discussion</a> on this article.</p>
  </#if>
</div>
