<div dojoType="regionalDialog" id="AnnotationDialog" bgColor="#333333" bgOpacity="0.6" toggle="fade" toggleDuration="250" style="padding:0;margin:0;">
	<div class="dialog annotate">
		<div class="tipu" id="dTipu"></div>
		<div class="btn close" title="Click to close and cancel">
			<a id="btn_cancel_annotation" title="Click to close and cancel">Cancel</a>
		</div>
		<div class="comment">
			<h5><span class="commentPrivate">Add Your Annotation (For Private Viewing)</span><span class="commentPublic">Post Your Annotation (For Public Viewing)</span></h5>
			<div class="posting pane">
				<form name="createAnnotation" id="createAnnotation" method="post" action="">
					<input type="hidden" name="target" value="${articleURI}" />	<!-- article id from steve-->
					<input type="hidden" name="startPath" value="" />
					<input type="hidden" name="startOffset" value="" />
					<input type="hidden" name="endPath" value="" />
					<input type="hidden" name="endOffset" value="" />
					<input type="hidden" name="commentTitle" id="commentTitle" value="" />
					<input type="hidden" name="comment" id="commentArea" value="" />
					<fieldset>
						<legend>Compose Your Annotation</legend>

						<div id="submitMsg"></div>
					
						<label for="search" class="commentPublic"><span class="none">Enter your annotation title</span><!-- error message text <em>A title is required for all public annotations</em>--></label>
						<input type="text" name="cTitle" id="cTitle" value="Enter your annotation title..." class="title commentPublic" alt="Enter your annotation title..." />
						
						<label for="reponse"><span class="none">Enter your annotation</span><!-- error message text <em>Please enter your annotation</em>--></label>
						<textarea name="cArea" id="cArea" value="Enter your annotation..." alt="Enter your annotation...">Enter your annotation...</textarea>
						
						<div><input type="radio" id="privateFlag" class="radio" title="Choose from one of the options" name="public" value="false" disabled="true" /><label for="Private">Private</label></div>
						<div><input type="radio" id="publicFlag" class="radio" title="Choose from one of the options" name="public" value="true" checked="true" /><label for="Public">Public</label></div>

						<div class="post btn commentPrivate"><a href="#" title="Click to save your annotation privately" id="btn_save">Save</a><!--<span>Save</span><span class="loading"><img src="${freemarker_config.getContext()}/images/pone_loading.gif" width="58" height="58" title="Loading..." /></span>--></div>
						<div class="post btn commentPublic"><a href="#" title="Click to post your annotation publicly" id="btn_post">Post</a><!--<span>Post</span><span class="loading"><img src="${freemarker_config.getContext()}/images/pone_loading.gif" width="58" height="58" title="Loading..." /></span>--></div>
					</fieldset>
				</form>
			</div>
		</div>
		<div class="tip" id="dTip"></div>
	</div>
</div>


