<!-- begin : right hand column -->
<div id="rhc">

<div id="sideNav">
	<div class="links">
		<ul>
	    <@ww.url id="articleArticleRepXML"  action="fetchObjectAttachment" includeParams="none" uri="${articleURI}">
        <@ww.param name="representation" value="%{'XML'}"/>
	    </@ww.url>
			<li><a href="${articleArticleRepXML}" class="xml" title="Download XML">Download Article XML</a></li>
	    <@ww.url id="articleArticleRepPDF"  action="fetchObjectAttachment" includeParams="none" uri="${articleURI}">
	      <@ww.param name="representation" value="%{'PDF'}"/>
      </@ww.url>
			<li><a href="${articleArticleRepPDF}" class="pdf" title="Download PDF">Download Article PDF</a></li>
			<li><a href="#" class="email" title="Email This Article to a Friend or Colleague">Email This Article</a></li>
  		<li><a href="#" class="print last" title="Go to a Printer Ready Version">Print This Article</a></li>
		</ul>
	</div>
<!--
	<div class="links">
		<ul>
			<li><a href="#" class="plos" title="View Related PLoS Articles">Related PLoS Articles</a></li>
			<li><a href="#" class="article" title="Find Articles Citing this Article">Articles Citing This Article</a></li>
			<li><a href="#" class="ncbi" title="View PubMed Record">PubMed Record</a></li>
			<li><a href="#" class="google last" title="View Google Scholar Citation">Google Scholar Citation</a></li>
		</ul>
	</div>-->
<!--	<div class="tools">
		<h3>Overall Rating</h3>
		<img src="${freemarker_config.getContext()}/images/temp_tools_rating.gif" />
		<a href="#">Rate this article</a>
		<h3>My Rating</h3>
	</div> -->
	<div id="floatMarker"></div>
	<div id="postcomment">
		<div class="commentview">
			<h6>Commentary</h6>
			<ol>
				<#if Session.PLOS_ONE_USER?exists>
						<li><a href="#" id="addAnnotation" class="addannotation tooltip">Add your annotation</a>
			  <#else>
						<li><a href="${freemarker_config.context}/user/secure/secureRedirect.action?goTo=${thisPage}" id="addAnnotation" class="addannotation tooltip">Add your annotation</a>
			  </#if>
				<li><a href="#" onclick="toggleAnnotation('public'); return false;<!--return topaz.domUtil.swapDisplayMode('toggleAnnotations');-->" class="expand tooltip" title="Click to turn annotations on/off">Turn annotations on/off</a>
				<!-- begin : expanded block -->
				<!--	<fieldset>
						<form>
						<ol id="toggleAnnotations">
							<li>Yours  <div><input class="input" type="radio" title="Choose from one of the options" name="yours" value="On" checked>
										<label for="yours">On</label>
										<input class="input" type="radio" title="Choose from one of the options" name="yours" value="Off">
										<label for="yours">Off</label></div>
							</li>
							<li>Authors	<div><input class="input" type="radio" title="Choose from one of the options" name="authors" value="On" checked>
										<label for="authors">On</label>
										<input class="input" type="radio" title="Choose from one of the options" name="authors" value="Off">
										<label for="authors">Off</label></div>
							</li>
							<li>All Public
										<div><input class="input" type="radio" title="Choose from one of the options" name="public" value="On" checked>
										<label for="public">On</label>
										<input class="input" type="radio" title="Choose from one of the options" name="public" value="Off">
										<label for="public">Off</label></div>
							</li>
						</ol>
						</form>
					</fieldset>-->
				<!-- end : expanded block -->
				</li>
				<li><a href="#" class="discuss icon">Start a discussion</a> about this article</li>

		    <@ww.url id="commentsURL" namespace="/annotation" action="getCommentary" includeParams="none" target="${articleURI}"/>
				<li><a href="${commentsURL}" class="commentary icon">See all commentary</a> on this article</li>
			</ol>
		</div> 
		<div id="sectionNavTop" class="tools">
					<ul>
					<li><a href="#s0">Top</a></li>
					<li><a href="#s1">Introduction</a></li>
					<li><a href="#s2">Results</a></li>
	
					<li><a href="#s3">Discussion</a></li>
					<li><a href="#s4">Materials and Methods</a></li>
					<li><a href="#s5">Supporting Information</a></li>
					<li><a href="#s6">Acknowledgments</a></li>
					<li><a href="#s7" class="last">References</a></li>
				</ul>
	
		</div>
		<div id="dojoDebug"></div>
	</div>
	
	
</div>


</div>
<!-- end : right hand column -->
