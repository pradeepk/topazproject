	<!-- begin : logo -->
	<div id="logo" title="PLoS ONE: Publishing science, acclerating research"><a href="http://www.plosone.org" title="PLoS ONE: Publishing science, accelerating research"><span>PLoS ONE</span></a></div>
	<!-- end : logo -->
	<!-- begin : user controls -->
	<#if Session.PLOS_ONE_USER?exists>
	
	
	<div id="user">
		<div>
			<p>Logged in as <a href="#" class="icon user">${Session.PLOS_ONE_USER.displayName}</a></p>
				<ul>
					<li><a href="#" class="icon preferences">Preferences</a></li>
					<li><a href="${freemarker_config.casLogoutURL}?service=http://${freemarker_config.plosOneHost}${freemarker_config.context}/logout.action" class="icon logout">Logout</a></li>
				</ul>
		</div>
	</div>
	
	<#else>
	<@ww.url id="thisPageURL" includeParams="get" includeContext="true" encode="false"/>
	<#assign thisPage = thisPageURL?replace("&amp;", "&")?url>
	
	<div id="user" class="login">
		<div>
			<ul>
				<li><a href="#">Create Account</a></li>
				<li><a href="${freemarker_config.context}/user/secure/secureRedirect.action?goTo=${thisPage}" class="feedback">Login</a></li>
			</ul>
		</div>
	</div>

	</#if>
	
	<!-- end : user controls -->
	<!-- begin search links -->
	<ul id="links">
<!--			<li><a href="#" title="Search PLoS ONE with advanced criteria" class="icon advanced">Advanced Search</a></li>-->
			<li><a href="#" title="PLoS ONE RSS Feeds" class="icon rss">RSS</a></li>
			<li><a href="${freemarker_config.context}/feedbackCreate.action?page=${thisPageURL?url}" title="Send us your feedback" class="feedback">Feedback</a></li>
	</ul>
	<!-- end : search links -->
	<!-- begin : dashboard -->
	<div id="db">
		<form name="searchForm" action="${freemarker_config.context}/search/simpleSearch.action" method="get">
			<fieldset>
				<legend>Search PLoS ONE</legend>
				<label for="search">Search</label>
				<div class="wrap"><input type="text" name="query" value="Search PLoS ONE..." onfocus="if(this.value=='Search PLoS ONE...')value='';" onblur="if(this.value=='')value='Search PLoS ONE...';" class="searchField" alt="Search PLoS ONE..." /></div>
				<input src="${freemarker_config.context}/images/pone_searchinput_btn.gif" onclick="submit();" value="ftsearch" alt="SEARCH" tabindex="3" class="button" type="image" />
			</fieldset>
		</form>
	</div>
	<!-- end : dashboard -->
	<!-- begin : navigation -->
	<#include "../global/global_navigation.ftl">
	<!-- end : navigation -->