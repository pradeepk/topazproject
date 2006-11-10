<div id="content" class="article">
	<#include "article_rhc.ftl">

	<#include "article_content.ftl">

	<div dojoType="regionalDialog" id="AnnotationDialog" bgColor="#333333" bgOpacity="0.6" toggle="fade" toggleDuration="250" style="padding:0;margin:0;">
		<#include "/widget/annotation_add.ftl">
	</div>

	<div dojoType="regionalDialog" id="CommentDialog" bgColor="white" bgOpacity="0" toggle="fade" toggleDuration="250" style="padding:0;margin:0;">
		<#include "/widget/commentDialog.ftl">
	</div>
</div>


