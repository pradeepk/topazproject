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

<#include "/global/global_config.ftl">
<#include "/global/global_top_headerless.ftl">

<!-- begin : main content -->
<#include "${templateFile}">
<!-- end : main contents -->

<#-- BEGIN MAJOR HACK FOR CONDITIONAL JOURNAL INCLUDE -->
<#if journalContext = "PLoSClinicalTrials" >
  <#include "/journals/clinicalTrials/global/global_bottom.ftl">
<#elseif journalContext = "PLoSCompBiol" >
  <#include "/journals/compbiol/global/global_bottom.ftl">
<#elseif journalContext = "PLoSGenetics" >
  <#include "/journals/genetics/global/global_bottom.ftl">
<#elseif journalContext = "PLoSNTD" >
  <#include "/journals/ntd/global/global_bottom.ftl">
<#elseif journalContext = "PLoSONE" >
  <#include "/journals/plosone/global/global_bottom.ftl">
<#elseif journalContext = "PLoSPathogens" >
  <#include "/journals/pathogens/global/global_bottom.ftl">
<#elseif journalContext = "AmbraJournal" >
  <#include "/journals/ambra/global/global_bottom.ftl">
<#elseif journalContext = "OverlayJournal" >
  <#include "/journals/overlayJournal/global/global_bottom.ftl">
<#else>
  <#include "/global/global_bottom.ftl">
</#if>
<#-- END MAJOR HACK -->