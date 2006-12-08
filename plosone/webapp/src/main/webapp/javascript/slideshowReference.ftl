<#list secondaryObjects as image>
  <@ww.url id="imageUrl" namespace="/article" includeParams="none"  action="fetchObject" uri="${image.uri}"/>
  <@ww.url id="imageAttachUrl" namespace="/article" includeParams="none"  action="fetchObjectAttachment" uri="${image.uri}"/>
	slideshow[${image_index}] = {imageUri: '${imageUrl?js_string}', imageAttachUri: '${imageAttachUrl?js_string}',
	                title: '<strong>${image.title?js_string}.</strong> ${image.transformedCaptionTitle?js_string}',
	                titlePlain: '${image.title?js_string} ${image.plainCaptionTitle?js_string}',
	                description: '${image.transformedDescription?js_string}'};
</#list>