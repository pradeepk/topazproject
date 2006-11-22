<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
<title>Image Viewer</title>
<style type="text/css" media="all"> @import "${freemarker_config.context}/css/pone_images.css";</style>			

		
</head>

<body id="figure-window-wrapper">
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
    <img border="0" class="thumbnail" src="${imageUrl}&representation=${image.repSmall}" alt="${image.title}"/>
    </@ww.a>
		</div>
  </#list>
</div>
<div id="figure-window-container">
    <@ww.url id="currentImageUrl" includeParams="none"  action="fetchObject" uri="${currentImage.uri}"/>
	<div id="figure-window-hdr">
		<div id="figure-window-hdr-links">
		<a href="${currentImageUrl}&representation=${currentImage.repLarge}">View Larger Image</a> <a href="${currentImageUrl}&representation=TIF">		Download original TIFF</a> <a href="${currentImageUrl}&representation=${currentImage.repMedium}">Download PowerPoint Friendly Image</a>		</div>
		<div id="figure-window-close">
			<a href="#5">Close Window</a> <a href="#5"><img src="${freemarker_config.context}/images/pone_button_close.gif" alt="close window" name="closewindow" width="20" height="20"/></a>  	  </div>
	</div>
	<div id="figure-window-content">
		<div id="figure-window-viewer">
		<img border="1" src="${currentImageUrl}&representation=${currentImage.repMedium}" alt="large" class="large" />
		${currentImage.title}
		</div>
		<div id="figure-window-description">
				${currentImage.description}
		</div>
  </div>
</div>
</body>
</html>
