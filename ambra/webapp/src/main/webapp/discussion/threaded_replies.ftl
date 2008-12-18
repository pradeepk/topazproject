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
<#if Session[freemarker_config.userAttributeKey]?exists>
	<#assign loginURL = "#">
<#else>
	<#assign loginURL = "${freemarker_config.context}/user/secure/secureRedirect.action?goTo=${thisPage}">
</#if>
	<#macro writeReplyDetails reply replyToAuthorId replyToAuthorName>
  	<@s.url namespace="/user" includeParams="none" id="showUserURL" action="showUser" userId="${reply.creator}"/>
  	<@s.url namespace="/user" includeParams="none" id="authorURL" action="showUser" userId="${replyToAuthorId}"/>
			<div class="response">
				<div class="hd">
					<!-- begin response title -->
					<h3>${reply.commentTitle}</h3>
					<!-- end : response title -->
					<!-- begin : response poster details -->
					<div class="detail">
						<a href="${showUserURL}" class="user icon">${reply.creatorName}</a> replied to <a href="${authorURL}" class="user icon">${replyToAuthorName}</a> on <strong>${reply.createdAsDate?string("dd MMM yyyy '</strong>at<strong>' HH:mm zzz")}</strong></div>
					<!-- end : response poster details -->
				</div>
				<!-- begin : response body text -->
				<blockquote>
					${reply.commentWithUrlLinking}
				</blockquote>
				<!-- end : response body text -->
				<!-- begin : toolbar options -->
				<div class="toolbar">
					<ul>
						<li>
						<#if Session[freemarker_config.userAttributeKey]?exists>
							<a href="${loginURL}" onclick="ambra.responsePanel.show(this, _dcf, 'toolbar', '${reply.id}', null, null, 1); return false;" class="flag tooltip" title="Request Review">Request review</a>
						<#else>							
							<a href="${loginURL}" class="flag tooltip" title="Request review">Request review</a>						
						</#if>
						</li>
						<li>
						<#if Session[freemarker_config.userAttributeKey]?exists>
							<a href="${loginURL}" onclick="ambra.responsePanel.show(this, _dcr, 'toolbar', '${baseAnnotation.id}', '${reply.id}', '${reply.commentTitle?js_string}'); return false;" class="respond tooltip" title="Click to respond">Respond to this Posting</a>
						<#else>
							<a href="${loginURL}" class="respond tooltip" title="Click to respond">Respond to this Posting</a>						
						</#if>
						</li>
					</ul>
				</div>
				<!-- end : toolbar options -->
	      <#list reply.replies as subReply>
	        <@writeReplyDetails reply=subReply replyToAuthorId=reply.creator replyToAuthorName=reply.creatorName/>
	      </#list>
			</div>
	</#macro>
	
	<#assign styleCorrections = "" />
	<#if baseAnnotation.type?index_of("Correction") gte 0>
		<#assign styleCorrections = " corrections" /> 
	</#if>
	<!-- begin : main content -->
	<div id="content" class="${styleCorrections}">
		<h1>${baseAnnotation.commentTitle}</h1>
		<div class="source">
			<span>Original Article</span>
			<@s.url id="origArticle" includeParams="none" namespace="/article" action="fetchArticle" articleURI="${baseAnnotation.annotates}" annotationId="${baseAnnotation.id}"/>
			<a href="${origArticle}" title="Back to original article" class="article icon">${articleInfo.dublinCore.title}</a>
		</div>
		<div class="response original">
			<div class="hd">
				<!-- begin response title -->
				<h3>${baseAnnotation.commentTitle}</h3>
				<!-- end : response title -->
				<!-- begin : response poster detail -->
				<@s.url namespace="/user" includeParams="none" id="baseAuthorURL" action="showUser" userId="${baseAnnotation.creator}"/>
				
				<div class="detail">Posted by <a href="${baseAuthorURL}" title="Annotation Author" class="user icon">${baseAnnotation.creatorName}</a> on <strong>${baseAnnotation.createdAsDate?string("dd MMM yyyy '</strong>at<strong>' HH:mm zzz")}</strong>
				</div>
				<!-- end : response poster details -->
			</div>
			<!-- begin : response body text -->
			<blockquote>
				${baseAnnotation.commentWithUrlLinking}
				<#if citation??><div class="citation"><strong>Citation: </strong><#assign isCorrection=true/><#assign doi=baseAnnotation.id?replace("info:doi/","") /><#include "/article/citation.ftl"/></#if>
			</blockquote>
			<!-- end : response body text -->
			<!-- begin : toolbar options -->
			<div class="toolbar">
				<ul>
					<li>
					<#if Session[freemarker_config.userAttributeKey]?exists>
						<a href="${loginURL}" onclick="ambra.responsePanel.show(this, _dcf, 'toolbar', '${baseAnnotation.id}', null, null, 0); return false;" class="flag tooltip" title="Request review">Request review</a>
					<#else>
						<a href="${loginURL}" class="flag tooltip" title="Request review">Request review</a>					
					</#if>
					</li>
					<li>
					<#if Session[freemarker_config.userAttributeKey]?exists>
						<a href="${loginURL}" onclick="ambra.responsePanel.show(this, _dcr, 'toolbar', '${baseAnnotation.id}', '${baseAnnotation.id}', '${baseAnnotation.commentTitle?js_string}'); return false;" class="respond tooltip" title="Click to respond">Respond to this Posting</a>
					<#else>
						<a href="${loginURL}" class="respond tooltip" title="Click to respond">Respond to this Posting</a>
					</#if>
					</li>
				</ul>
			</div>
			<!-- end : toolbar options -->
		</div>
		<!-- begin : response note that all responses TO this response get enclosed within this response container  -->
	  <#list replies as reply>
		  <@writeReplyDetails reply=reply replyToAuthorId=baseAnnotation.creator replyToAuthorName=baseAnnotation.creatorName/>
	  </#list>
		<!-- end : response -->
	</div>
	
			    <@s.url id="commentsURL" namespace="/annotation" action="getCommentary" includeParams="none" target="${baseAnnotation.annotates}"/>
				<p><a href="${commentsURL}" class="commentary icon">See all ongoing discussions</a> on this article</p>
	
	<!-- end : main contents -->




