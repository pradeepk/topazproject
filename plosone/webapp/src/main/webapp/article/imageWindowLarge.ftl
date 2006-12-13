<a href="#" class="close" onclick="history.back(-1);return false;">Return to slideshow</a>
<#if Parameters.uri?exists>
   <#assign imageURI = Parameters.uri>
<#else>
   <#assign imageURI = "">
</#if>

<div id="figure-window-wrapper">
	<div id="figure-window-nav">
	<@ww.url id="imageUrl" includeParams="none"  action="fetchObject" uri="${imageURI}"/>
	<div id="figure-window-container">
		<div id="figure-window-viewer">
			<img src="${imageUrl}&representation=PNG_L" title="Larger Image" class="large" id="figureImg" />
		</div>
	</div>
</div>
