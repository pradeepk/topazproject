<div class="dialog annotate">
	<div class="hd">
		<div class="c"></div>
	</div>
	<div class="bd">
		<div class="c">
			<div class="btn close" id="btn_cancel" title="Click to close this dialog box">Cancel</div>
			<div class="s">
				<div class="comment">
					<h5><span class="commentPrivate">Add Your Annotation (For Private Viewing)</span><span class="commentPublic">Post Your Annotation (For Public Viewing)</span></h5>
					<div class="posting pane">
						<form name="createAnnotation" id="createAnnotation" method="post" action="">
							<input type="hidden" name="target" value="${articleURI}" />	<!-- article id from steve-->
							<input type="hidden" name="startPath" value="" />
							<input type="hidden" name="startOffset" value="" />
							<input type="hidden" name="endPath" value="" />
							<input type="hidden" name="endOffset" value="" />
							<fieldset>
								<legend>Compose Your Annotation</legend>
							
								<label for="search" class="commentPublic"><span class="none">Enter your annotation title</span><!-- error message text <em>A title is required for all public annotations</em>--></label>
								<input type="text" name="commentTitle" id="commentTitle" value="Enter your annotation title..." class="title commentPublic" alt="Enter your annotation title..." />
								
								<label for="reponse"><span class="none">Enter your annotation</span><!-- error message text <em>Please enter your annotation</em>--></label>
								<textarea name="comment" id="commentArea" value="Enter your annotation..." alt="Enter your annotation...">Enter your annotation...</textarea>
								
								<div><input type="radio" id="privateFlag" class="radio" title="Choose from one of the options" name="public" value="false" checked /><label for="Private">Private</label></div>
								<div><input type="radio" id="publicFlag" class="radio" title="Choose from one of the options" name="public" value="true" /><label for="Public">Public</label></div>

								<div class="post btn commentPrivate"><a href="#" title="Click to save your annotation privately" id="btn_save"><img src="${freemarker_config.getContext()}/images/pone_icon_post.gif">Save</a></div>
								<div class="post btn commentPublic"><a href="#" title="Click to post your annotation publicly" id="btn_post"><img src="${freemarker_config.getContext()}/images/pone_icon_post.gif">Post</a></div>

								<div id="submitMsg"></div>
							</fieldset>
						</form>
					</div>
				</div>	
			</div>
		</div>
	</div>
	<div class="ft">
		<div class="c"></div>
	</div>
	<div class="tip" id="dTip"></div>
</div>

