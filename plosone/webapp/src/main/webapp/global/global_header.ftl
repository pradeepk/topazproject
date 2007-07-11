	<!-- begin : logo -->
	<div id="logo" title="PLoS ONE: Publishing science, acclerating research"><a href="http://${freemarker_config.plosOneHost}${freemarker_config.context}" title="PLoS ONE: Publishing science, accelerating research"><span>PLoS ONE</span></a></div>
	<!-- end : logo -->
	<!-- begin : user controls -->
	<@s.url id="thisPageURL" includeParams="get" includeContext="true" encode="false"/>
	<#assign thisPage = thisPageURL?replace("&amp;", "&")?url>
	<#if Session[freemarker_config.userAttributeKey]?exists>
	<div id="user">
		<div>
		<@s.url id="editProfileURL" includeParams="none" namespace="/user/secure" action="editProfile" tabId="preferences"/>
				
					<p>Welcome, <!--<a href="${freemarker_config.context}/user/showUser.action?userId=${Session[freemarker_config.userAttributeKey].userId}" title="You are logged in as ${Session[freemarker_config.userAttributeKey].displayName}">--><strong>${Session[freemarker_config.userAttributeKey].displayName}</strong></a>!</p>
				<ul>
					<@s.url id="logoutURL" includeParams="none" namespace="/user/secure" action="secureRedirect" goTo="${freemarker_config.casLogoutURL}?service=http://${freemarker_config.plosOneHost}${freemarker_config.context}/logout.action"/>
					<li><a href="${editProfileURL}" title="Edit your account preferences and alert settings">Preferences</a></li>
                                        <li><a href="${logoutURL}" title="Logout of PLoS ONE">Logout</a></li>
				</ul>
		</div>
	</div>
	
	<#else>

	<div id="user">
		<div>
			<ul>
				<@s.url id="loginURL" includeParams="none" namespace="/user/secure" action="secureRedirect" goTo="${thisPage}"/>
				<li><a href="${freemarker_config.context}/user/secure/secureRedirect.action?goTo=${thisPage}" class="feedback"><strong>Login</strong></a></li>
				<li><a href="${freemarker_config.registrationURL}">Create Account</a></li>
	<@s.url id="feedbackURL" includeParams="none" namespace="/" action="feedbackCreate" page="${thisPageURL?url}"/>
				<li class="feedback"><a href="${feedbackURL}" title="Send us your feedback">Feedback</a></li>
			</ul>
		</div>
	</div>

	</#if>
	
	<!-- end : user controls -->
	<!-- begin search links -->
	<ul id="links"><@s.url id="browseURL" includeParams="none" namespace="/article" action="browse"/><li class="browse"><a href="${browseURL}" title="Browse PLoS ONE Articles">Browse</a></li><@s.url id="rssURL" includeParams="none" namespace="/static" action="rssFeeds"/><li class="rss"><a href="${rssURL}" title="PLoS ONE RSS Feeds">RSS</a></li></ul>
	<!-- end : search links -->
	<!-- begin : dashboard -->
	<div id="db">
		<@s.url id="searchURL" includeParams="none" namespace="/search" action="simpleSearch" />
		<form name="searchForm" action="${searchURL}" method="get">
			<fieldset>
				<legend>Search PLoS ONE</legend>
				<label for="search">Search</label>
				<div class="wrap"><input id="search" type="text" name="query" value="Search articles..." onfocus="if(this.value=='Search articles...')value='';" onblur="if(this.value=='')value='Search articles...';" class="searchField" alt="Search articles..."/></div>
				<input src="${freemarker_config.context}/images/pone_search_btn1.gif" onclick="submit();" value="ftsearch" alt="SEARCH" tabindex="3" class="button" type="image" />
			</fieldset>
		</form>
	</div>
	<!-- end : dashboard -->
	<!-- begin : navigation -->
	<#include "../global/global_navigation.ftl">
	<!-- end : navigation -->
