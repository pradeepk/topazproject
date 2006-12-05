<#if Parameters.tabId?exists>
   <#assign tabId = Parameters.tabId>
<#else>
   <#assign tabId = "">
</#if>

<div id="content">
	<h1>My PLoS Profile</h1>
	
	<div class="horizontalTabs">
		<ul id="tabsContainer">
		</ul>
		
		<div id="tabPaneSet" class="contentwrap">
		  <#if tabId == "alerts">
				<#include "alerts.ftl">
		  <#else>
				<#include "user.ftl">
			</#if>
		</div>
	</div>
	
</div>

<#include "/widget/loadingCycle.ftl">
