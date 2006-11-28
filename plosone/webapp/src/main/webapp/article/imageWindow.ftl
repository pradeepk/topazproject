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
 	    <@ww.url id="currentImageAttachmentUrl" includeParams="none"  action="fetchObjectAttachment" uri="${currentImage.uri}"/>
		<div id="figure-window-hdr">
			<div class="figure-next">
				<a href="#" class="previous icon">Previous</a> | <a href="#" class="next icon">Next</a>
			</div>
			<div id="figure-window-hdr-links">
			<a href="${currentImageUrl}&representation=${currentImage.repLarge}" id="viewL" class="larger icon" title="Click to view a larger version of this image">View Larger Image</a> 
			<a href="${currentImageAttachmentUrl}&representation=TIF" id="downloadTiff" class="image icon" title="Click to download the original TIFF">		Download original TIFF</a> 
			<a href="${currentImageUrl}&representation=${currentImage.repMedium}" id="downloadPpt" class="ppt icon" title="Click to download a PowerPoint friendly version">Download PowerPoint Friendly Image</a>		</div>
				
		</div>
		
		<div id="figure-window-content">
			<div id="figure-window-viewer">
			<img border="1" src="${currentImageUrl}&representation=${currentImage.repMedium}" title="${currentImage.title} ${currentImage.plainCaptionTitle}" class="large" id="figureImg" />
			<span id="figureTitle"><strong>${currentImage.title}.</strong> ${currentImage.transformedCaptionTitle}</span>
			</div>
			<div id="figure-window-description">
					${currentImage.transformedDescription}
			</div>
	  </div>
	</div>
</div>
