<#if Parameters.tabId?exists>
   <#assign tabId = Parameters.tabId>
<#else>
   <#assign tabId = "">
</#if>

<div id="contents">
	<h2>Profile</h2>
	
	<div class="horizontalTabs">
		<ol id="tabsContainer">
		</ol>
		
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
