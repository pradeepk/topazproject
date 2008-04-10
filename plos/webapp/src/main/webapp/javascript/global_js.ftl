<#--
  $HeadURL::                                                                            $
  $Id$
  
  Copyright (c) 2007-2008 by Topaz, Inc.
  http://topazproject.org
  
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
  
  http://www.apache.org/licenses/LICENSE-2.0
  
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->
<script type="text/javascript">
  var _namespace="${freemarker_config.getContext()}";
	<#if Session?exists && Session[freemarker_config.userAttributeKey]?exists>
		var loggedIn = true;
	<#else>
  	var loggedIn = false;
	</#if>

	var djConfig = {
		isDebug: false,
		debugContainerId : "dojoDebug",
		debugAtAllCosts: false,
  	bindEncoding: "UTF-8",
  	baseScriptUri: "${freemarker_config.context}/javascript/dojo/"
	};
</script>
<#list freemarker_config.getJavaScript(templateFile, journalContext) as x>
	<#if x?ends_with(".ftl")>
<script type="text/javascript">
<#include "${x}">
</script>	
	<#else>
<script type="text/javascript" src="${x}"></script>	
	</#if>
</#list>