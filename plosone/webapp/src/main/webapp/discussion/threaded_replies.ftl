<#macro writeReplyDetails reply replyToAuthorId replyToAuthorName>
		<div class="response">
			<div class="hd">
				<!-- begin response title -->
				<h3>${reply.commentTitle}</h3>
				<!-- end : response title -->
				<!-- begin : response poster details -->
				<div class="detail">
					<a href="#" class="user icon">${reply.creator}</a> replied to <a href="../viewUser?userURI=${replyToAuthorId}" class="user icon">${replyToAuthorName}</a> on <strong>${reply.createdAsDate?string("yyyy-MM-dd")}</strong> at <strong>${reply.createdAsDate?string("HH:mm")} GMT</strong>:</div>
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
						<a href="secure/createAnnotationFlagSubmit.action?target=${reply.id}" class="flag tooltip" title="Flag this posting for moderation">Flag for moderation</a>
					</li>
					<li>
						<a href="secure/createReplySubmit.action?root=${baseAnnotation.id}&inReplyTo=${reply.id}" class="respond tooltip" title="Click to respond">Respond to this Posting</a>
					</li>
				</ul>
			</div>
			<!-- end : toolbar options -->
      <#list reply.replies as subReply>
        <@writeReplyDetails reply=subReply replyToAuthorId=reply.creator replyToAuthorName=reply.creator/>
      </#list>
		</div>
</#macro>

<!-- begin : main content -->
<div id="content">
	<h1>Annotation and Responses</h1>
	<div class="source">
		<span>Original Article</span>
		<a href="${baseAnnotation.annotates}" title="Back to original article" class="article icon">X Chromosomes Alternate between Two States prior to Random X-Inactivation</a>
	</div>
	<div class="response original">
		<div class="hd">
			<!-- begin response title -->
			<h3>${baseAnnotation.commentTitle}</h3>
			<!-- end : response title -->
			<!-- begin : response poster detail -->
			<div class="detail">Posted by <a href="#" title="Annotation Author" class="user icon">${baseAnnotation.creator}</a> on <strong>${baseAnnotation.createdAsDate?string("yyyy-MM-dd")}</strong> at <strong>${baseAnnotation.createdAsDate?string("HH:mm")} GMT</strong>
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
					<a href="secure/createAnnotationFlagSubmit.action?target=${baseAnnotation.id}" class="flag tooltip" title="Flag this posting for moderation">Flag for moderation</a>
				</li>
				<li>
					<a href="secure/createReplySubmit.action?root=${baseAnnotation.id}&inReplyTo=${baseAnnotation.id}" class="respond tooltip" title="Click to respond">Respond to this Posting</a>
				</li>
			</ul>
		</div>
		<!-- end : toolbar options -->
	</div>
	<!-- begin : response note that all responses TO this response get enclosed within this response container  -->
  <#list replies as reply>
	  <@writeReplyDetails reply=reply replyToAuthorId=reply.creator replyToAuthorName=reply.creator/>
  </#list>
	<!-- end : response -->
</div>
<!-- end : main contents -->
