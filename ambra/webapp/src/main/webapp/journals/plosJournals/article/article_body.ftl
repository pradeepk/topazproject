<#--
  $HeadURL::                                                                            $
  $Id$
  
  Copyright (c) 2007-2009 by Topaz, Inc.
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
<#include "/article/article_variables.ftl">
<div id="content" class="article" style="visibility:visible;">
<#include "article_rhc.ftl">
<form name="articleInfo" id="articleInfo" method="" action="">
<input type="hidden" name="isAuthor" value="true" />
<input type="hidden" name="authorIdList" value="" />
<input type="hidden" name="userIdList" value="" />
<input type="hidden" name="otherIdList" value="" />
<input type="hidden" name="annotationId" value="${annotationId}" />
<input type="hidden" name="isResearchArticle" value="${isResearchArticle?string}" />
</form>
<div id="articleContainer"><#include "/journals/plosJournals/article/article_content.ftl"></div>
<div style="visibility:hidden">
<#include "/widget/annotation_add.ftl">
<#include "/widget/contextAction.ftl">
<#include "/widget/commentDialog.ftl">
<#include "/widget/ratingDialog.ftl">
<#include "/widget/loadingCycle.ftl">
</div>
</div>