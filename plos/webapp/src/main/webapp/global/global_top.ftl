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
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
<head>
<#include "global_head.ftl">
</head>
<body>

<#-- this should be pulled out into an ftl file -->
<#if journalContext = "PLoSONE" >
<!-- begin : biocompare -->
<script src='http://comparenetworks.com/partnerAds/swfobject.js' type='text/javascript'></script>
<script language="javascript" type="text/javascript" src="http://comparenetworks.com/partnerAds/adSetup.js"></script>
<!-- end : biocompare -->
</#if>

<!-- begin : container -->
<div id="container">
  <!-- begin : top banner external ad space -->
  <div id="topBanner">
  <#-- BEGIN MAJOR HACK FOR CONDITIONAL JOURNAL INCLUDE -->
  <#if journalContext = "PLoSClinicalTrials" >
    <#include "/journals/clinicalTrials/global/global_topbanner.ftl">
  <#elseif journalContext = "PLoSCompBiol" >
    <#include "/journals/compbiol/global/global_topbanner.ftl">
  <#elseif journalContext = "PLoSGenetics" >
    <#include "/journals/genetics/global/global_topbanner.ftl">
  <#elseif journalContext = "PLoSNTD" >
    <#include "/journals/ntd/global/global_topbanner.ftl">
  <#elseif journalContext = "PLoSPathogens" >
    <#include "/journals/pathogens/global/global_topbanner.ftl">
  <#elseif journalContext = "PLoSONE" >
    <#include "/journals/plosone/global/global_topbanner.ftl">
  <#else>
    <#include "global_topbanner.ftl">
  </#if>
  <#-- END MAJOR HACK -->
  </div>
  <!-- end : top banner external ad space -->

  <#if Session?exists && Session[freemarker_config.userAttributeKey]?exists>
  <!-- begin : header -->
  <div id="hdr">
  <#else>
  <!-- begin : header -->
  <div id="hdr" class="login">
  </#if>

  <#include "global_header.ftl">
  </div>
  <!-- end : header --> 
