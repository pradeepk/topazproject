<div class="dialog annotate">
	<div class="hd">
		<div class="c"></div>
	</div>
	<div class="bd">
		<div class="c">
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
							
								<label for="search" class="commentPublic"><span class="none">Enter your annotation title</span><em>A title is required for all public annotations</em></label>
								<input type="text" name="commentTitle" id="commentTitle" value="Enter your annotation title..." class="title commentPublic" alt="Enter your annotation title..." />
								
								<label for="body"><span class="none">Enter your annotation</span><em>Please enter your annotation</em></label>
								<textarea name="comment" id="comment" alt="Enter your annotation..." class="response">Enter your annotation...</textarea>
								<div><input type="radio" id="privateFlag" class="radio" title="Choose from one of the options" name="public" value="false" checked /><label for="Private">Private</label></div>
								<div><input type="radio" id="publicFlag" class="radio" title="Choose from one of the options" name="public" value="true" /><label for="Public">Public</label></div>
								<div class="right"><input type="button" value="Save" id="btn_save" alt="Save" class="btn important commentPrivate" />
									<input type="button" value="Post" id="btn_post" alt="Post" class="btn important commentPublic" /></div>
								<div class="right"><input type="button" id="btn_cancel" value="Cancel" alt="Cancel" class="btn" /></div>
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

