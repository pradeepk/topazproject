<#include "/${parameters.templateDir}/${parameters.theme}/controlheader.ftl" />
<#include "/${parameters.templateDir}/simple/password.ftl" />
<#if hasFieldErrors>
	<#list fieldErrors[parameters.name] as error>
		${error?html}
	</#list>
</#if>
<#include "/${parameters.templateDir}/xhtml/controlfooter.ftl" />
