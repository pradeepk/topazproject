<div dojoType="topaz:regionalDialog" id="AnnotationDialog" bgColor="#333333" bgOpacity="0.6" toggle="fade" toggleDuration="250" closeOnBackgroundClick="false" style="padding:0;margin:0;">
	<div class="dialog annotate">
		<div class="tipu" id="dTipu"></div>
		<div class="btn close" title="Click to close and cancel">
			<a id="btn_cancel" title="Click to close and cancel">Cancel</a>
		</div>
		<div class="comment">
			<h5><span class="commentPrivate">Add Your Annotation (For Private Viewing)</span><span class="commentPublic">Post Your Annotation (For Public Viewing)</span></h5>
			<div class="posting pane">
				<form name="createAnnotation" id="createAnnotation" method="post" action="">
					<input type="hidden" name="target" value="${articleURI}" />	
					<input type="hidden" name="startPath" value="" />
					<input type="hidden" name="startOffset" value="" />
					<input type="hidden" name="endPath" value="" />
					<input type="hidden" name="endOffset" value="" />
					<input type="hidden" name="commentTitle" id="commentTitle" value="" />
					<input type="hidden" name="comment" id="commentArea" value="" />
					<fieldset>
						<legend>Compose Your Annotation</legend>

						<span id="submitMsg" class="error"></span>
					
						<label for="cTitle" class="commentPublic"><span class="none">Enter your annotation title</span><!-- error message text <em>A title is required for all public annotations</em>--></label>
						<input type="text" name="cTitle" id="cTitle" value="Enter your annotation title..." class="title commentPublic" alt="Enter your annotation title..." />
						
						<label for="cArea"><span class="none">Enter your annotation</span><!-- error message text <em>Please enter your annotation</em>--></label>
						<textarea name="cArea" id="cArea" value="Enter your annotation..." alt="Enter your annotation...">Enter your annotation...</textarea>
						
						<input type="hidden" name="isPublic" value="true" />
						<!--
						<div><input type="radio" id="privateFlag" class="radio" title="Choose from one of the options" name="public" value="false" disabled="true" /><label for="Private">Private</label></div>
						<div><input type="radio" id="publicFlag" class="radio" title="Choose from one of the options" name="public" value="true" checked="true" /><label for="Public">Public</label></div>
						-->
						<div class="btnwrap commentPrivate"><input type="button" value="Save" class="commentPrivate" title="Click to save your annotation privately" id="btn_save"/></div>
						<div class="btnwrap commentPublic"><input type="button" value="Post" class="commentPublic" title="Click to post your annotation publicly" id="btn_post"/></div>
					</fieldset>
				</form>
			</div>
		</div>
		<div class="tip" id="dTip"></div>
	</div>
</div>


