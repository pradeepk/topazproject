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

	<div dojoType="regionalDialog" id="AnnotationDialog" bgColor="white" toggle="fade" toggleDuration="250">
		<#include "/widget/annotation_add.ftl">
	</div>

	<div dojoType="regionalDialog" id="CommentDialog" bgColor="white" bgOpacity="0" toggle="fade" toggleDuration="250">
		<#include "/widget/commentDialog.ftl">
	</div>
	
<!-- end : research article -->

