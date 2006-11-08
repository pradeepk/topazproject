	<!-- begin : research article -->
	<form name="articleInfo" id="articleInfo" method="" action="">
		<input type="hidden" name="isAuthor" value="true" />
		<input type="hidden" name="authorIdList" value="" />
		<input type="hidden" name="userIdList" value="" />
		<input type="hidden" name="otherIdList" value="" />
	</form>

	<div id="articleContainer">
		${transformedArticle}
	</div>

	<div dojoType="regionalDialog" id="AnnotationDialog" bgColor="#333333" bgOpacity="0.6" toggle="fade" toggleDuration="250" style="padding:0;margin:0;">
		<#include "/widget/annotation_add.ftl">
	</div>

	<div dojoType="regionalDialog" id="CommentDialog" bgColor="white" bgOpacity="0" toggle="fade" toggleDuration="250" style="padding:0;margin:0;">
		<#include "/widget/commentDialog.ftl">
	</div>
	
<!-- end : research article -->

