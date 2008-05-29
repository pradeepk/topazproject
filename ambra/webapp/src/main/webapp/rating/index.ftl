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
<div id="discussionContainer">
  <#include "/rating/ratedComments.ftl">
</div>

<!-- TODO: need a refactor to work with all Anotea v. subclasses -->
<!-- need discussionResponse to avoid js errors -->
<#include "/widget/discussionResponse.ftl">
<#include "/widget/discussionFlag.ftl">
<#include "/widget/loadingCycle.ftl">