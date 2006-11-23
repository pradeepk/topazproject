<div id="figure-window-wrapper">
<#if Parameters.imageURI?exists>
   <#assign imageURI = Parameters.imageURI>
<#else>
   <#assign imageURI = "">
</#if>
	<div id="figure-window-nav">

	  <#list secondaryObjects as image>
			<#if image.uri == imageURI>
				<#assign currentImage = image>
				<div class="figure-window-nav-item-current">
			<#else>
				<div class="figure-window-nav-item">
			</#if>
	    <@ww.url id="imageUrl" includeParams="none"  action="fetchObject" uri="${image.uri}"/>
	    <@ww.a title="Click for larger image" href="#"> <!--put code here for onclick and change the pane-->
	    <img border="0" class="thumbnail" src="${imageUrl}&representation=${image.repSmall}" onclick="topaz.slideshow.show(this, ${image_index});" title="${image.title} ${image.plainCaptionTitle}" />
	    </@ww.a>
			</div>
	  </#list>
		<#if !currentImage?exists && secondaryObjects?size gt 0>
			<#assign currentImage = secondaryObjects?first>
		</#if>

	</div>
	<div id="figure-window-container">
	    <@ww.url id="currentImageUrl" includeParams="none"  action="fetchObject" uri="${currentImage.uri}"/>
		<div id="figure-window-hdr">
			<div id="figure-window-hdr-links">
			<a href="${currentImageUrl}&representation=${currentImage.repLarge}" id="viewL">View Larger Image</a> 
			<a href="${currentImageUrl}&representation=TIF" id="downloadTiff">		Download original TIFF</a> 
			<a href="${currentImageUrl}&representation=${currentImage.repMedium}" id="downloadPpt">Download PowerPoint Friendly Image</a>		</div>
			<div id="figure-window-close">
				<a href="#5">Close Window</a> <a href="#5"><img src="${freemarker_config.context}/images/pone_button_close.gif" title="close window" name="closewindow" width="20" height="20"/></a>  	  </div>
		</div>
		<div id="figure-window-content">
			<div id="figure-window-viewer">
			<img border="1" src="${currentImageUrl}&representation=${currentImage.repMedium}" title="${currentImage.title} ${currentImage.plainCaptionTitle}" class="large" id="figureImg" />
			<span id="figureTitle">${currentImage.title} ${currentImage.transformedCaptionTitle}</span>
			</div>
			<div id="figure-window-description">
					${currentImage.transformedDescription}
			</div>
	  </div>
	</div>
</div>
