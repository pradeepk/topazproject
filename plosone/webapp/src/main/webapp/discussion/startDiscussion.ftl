<div id="content">
	<h1>Start a Discussion</h1>

	<div class="source">
		<span>On the Article</span><a href="${freemarker_config.context}/article/fetchArticle.action?articleURI=${articleInfo.uri}" title="Back to original article" class="article icon">${articleInfo.title}</a>
	</div>


<!-- begin : posting response -->
	<div style="display: block;" class="posting pane" id="DiscussionPanel">
		<h5>Post Your Discussion Comment</h5>
		<form name="discussionResponse" method="post" action="">
			<input name="commentTitle" value="" type="hidden">
			<input name="comment" value="" type="hidden">

			<div id="responseSubmitMsg" class="error"></div>
		
			<fieldset>
				<legend>Compose Your Response</legend>
		
				<label for="commentTitle"><span class="none">Enter your comment title</span><!-- error message text <em>A title is required for all public annotations</em>--></label>
				<input name="commentTitle" id="commentTitle" value="Enter your comment title..." class="title" alt="Enter your comment title..." type="text">
			
				<label for="comment"><span class="none">Enter your comment</span><!-- error message style <em>Please enter your response</em>--></label>
				<textarea id="comment" title="Enter your comment..." class="response" name="comment">Enter your comment...</textarea>

			
				<div class="btnwrap"><input name="post" value="Post" id="btnPostResponse" title="Click to Post Your Response" type="button"></div>
			
			</fieldset>
		</form>
	</div>
<!-- end : posting response -->
</div>

