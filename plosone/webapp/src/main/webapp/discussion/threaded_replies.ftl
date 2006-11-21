<div id="discussionContainer">
	<#macro writeReplyDetails reply replyToAuthorId replyToAuthorName>
			<div class="response">
				<div class="hd">
					<!-- begin response title -->
					<h3>${reply.commentTitle}</h3>
					<!-- end : response title -->
					<!-- begin : response poster details -->
					<div class="detail">
						<a href="${reply.creator}" class="user icon">${reply.creatorName}</a> replied to <a href="../viewUser?userURI=${replyToAuthorId}" class="user icon">${replyToAuthorName}</a> on <strong>${reply.createdAsDate?string("yyyy-MM-dd")}</strong> at <strong>${reply.createdAsDate?string("HH:mm")} GMT</strong>:</div>
					<!-- end : response poster details -->
				</div>
				<!-- begin : response body text -->
				<blockquote>
					<p>${reply.commentWithUrlLinking}</p>
				</blockquote>
				<!-- end : response body text -->
				<!-- begin : toolbar options -->
				<div class="toolbar">
					<ul>
						<li>
							<a href="#" onclick="topaz.responsePanel.show(this, dcf, 'toolbar', 'target=${baseAnnotation.id}'); return false;" class="flag tooltip" title="Flag this posting for moderation">Flag for moderation</a>
						</li>
						<li>
							<a href="#" onclick="topaz.responsePanel.show(this, dcr, 'toolbar', 'root=${baseAnnotation.id}&inReplyTo=${baseAnnotation.id}', '${reply.commentTitle}'); return false;" class="respond tooltip" title="Click to respond">Respond to this Posting</a>
						</li>
					</ul>
				</div>
				<!-- end : toolbar options -->
	      <#list reply.replies as subReply>
	        <@writeReplyDetails reply=subReply replyToAuthorId=reply.creator replyToAuthorName=reply.creatorName/>
	      </#list>
			</div>
	</#macro>
	
	<!-- begin : main content -->
	<div id="content">
		<h1>Annotation and Responses</h1>
		<div class="source">
			<span>Original Article</span>
			<a href="${freemarker_config.context}/article/fetchArticle.action?articleURI=${baseAnnotation.annotates}" title="Back to original article" class="article icon">${articleInfo.title}</a>
		</div>
		<div class="response original">
			<div class="hd">
				<!-- begin response title -->
				<h3>${baseAnnotation.commentTitle}</h3>
				<!-- end : response title -->
				<!-- begin : response poster detail -->
				<div class="detail">Posted by <a href="${baseAnnotation.creator}" title="Annotation Author" class="user icon">${baseAnnotation.creatorName}</a> on <strong>${baseAnnotation.createdAsDate?string("yyyy-MM-dd")}</strong> at <strong>${baseAnnotation.createdAsDate?string("HH:mm")} GMT</strong>
				</div>
				<!-- end : response poster details -->
			</div>
			<!-- begin : response body text -->
			<blockquote>
				<p>${baseAnnotation.commentWithUrlLinking}</p>
			</blockquote>
			<!-- end : response body text -->
			<!-- begin : toolbar options -->
			<div class="toolbar">
				<ul>
					<li>
						<a href="#" onclick="topaz.responsePanel.show(this, dcf, 'toolbar', 'target=${baseAnnotation.id}'); return false;" class="flag tooltip" title="Flag this posting for moderation">Flag for moderation</a>
					</li>
					<li>
						<a href="#" onclick="topaz.responsePanel.show(this, dcr, 'toolbar', 'root=${baseAnnotation.id}&inReplyTo=${baseAnnotation.id}', '${baseAnnotation.commentTitle}'); return false;" class="respond tooltip" title="Click to respond">Respond to this Posting</a>
					</li>
				</ul>
			</div>
			<!-- end : toolbar options -->
		</div>
		<!-- begin : response note that all responses TO this response get enclosed within this response container  -->
	  <#list replies as reply>
		  <@writeReplyDetails reply=reply replyToAuthorId=baseAnnotation.creator replyToAuthorName=baseAnnotation.creatorName/>
	  </#list>
		<!-- end : response -->
	</div>
	<!-- end : main contents -->
</div>



