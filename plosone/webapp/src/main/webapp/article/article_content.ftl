	<!-- begin : research article -->
	<form name="articleInfo" id="articleInfo" method="" action="">
		<input type="hidden" name="isAuthor" value="true" />
		<input type="hidden" name="authorIdList" value="" />
		<input type="hidden" name="userIdList" value="" />
		<input type="hidden" name="otherIdList" value="" />
	</form>
	<span class="note public" title="User Annotation"><a href="#" class="bug" id="bug1" title="Click to preview this annotation" ></a>This is a public note posted by someone else.</span>
	<div id="articleContainer">
		${transformedArticle}
	</div>

	<div dojoType="regionalDialog" id="AnnotationDialog" bgColor="white" toggle="fade" toggleDuration="250">
		<#include "/widget/annotation_add.ftl">
	</div>

	
	
<!-- end : research article -->

