<#list freemarker_config.getJavaScript(templateFile) as x>
	<#if x?ends_with(".ftl")>
<script type="text/javascript">
<#include "${x}">
</script>	
	<#else>
<script type="text/javascript" src="${x}"></script>	
	</#if>
</#list>