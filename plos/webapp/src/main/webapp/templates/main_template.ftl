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
<#if Request[freemarker_config.journalContextAttributeKey]?exists>
  <#assign journalContext = Request[freemarker_config.journalContextAttributeKey].journal>
<#else>
  <#assign journalContext = "">
</#if>
<#assign orgName = "Ambra">

<#include "/global/global_config.ftl">

<#-- BEGIN MAJOR HACK FOR CONDITIONAL JOURNAL INCLUDE -->
<#if journalContext = "AmbraJournal" >
    <#include "/journals/ambraJournal/global/global_top.ftl">
<#elseif journalContext = "OverlayJournal" >
    <#include "/journals/overlayJournal/global/global_top.ftl">
<#else>
  <#include "/global/global_top.ftl">
</#if>
<#-- END MAJOR HACK -->

<!-- begin : main content -->
<#include "${templateFile}">
<!-- end : main contents -->

<#-- BEGIN MAJOR HACK FOR CONDITIONAL JOURNAL INCLUDE -->
<#if journalContext = "AmbraJournal" >
  <#include "/journals/ambraJournal/global/global_bottom.ftl">
<#elseif journalContext = "OverlayJournal" >
  <#include "/journals/overlayJournal/global/global_bottom.ftl">
<#else>
  <#include "/global/global_bottom.ftl">
</#if>
<#-- END MAJOR HACK -->

<div id="dojoDebug" style="background: yellow;"></div>