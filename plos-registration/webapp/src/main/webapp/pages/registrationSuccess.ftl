<#include "/global/global_config.ftl">
<#include "/global/global_top.ftl">

<!-- begin : main content -->
<div id="content">
<h1>Sign Up for a PLoS Journals Profile</h1>
	<p>
		<strong>Thanks for registering!</strong>
		Please check your e-mail inbox to confirm your registration.
 
		<!--
		<@ww.url includeParams="none" id="emailVerificationURL" action="emailVerification">
    	<@ww.param name="loginName" value="user.loginName"/>
      <@ww.param name="emailVerificationToken" value="user.emailVerificationToken"/>
    </@ww.url>
    <@ww.a href="%{emailVerificationURL}"  >${emailVerificationURL}</@ww.a>
		-->
	</p>
	<br/>
	<p>Continue to <a href="http://www.plosjournals.org" title="PLoS Journals: Peer-reviewed open-access journals from the Public Library of Science">PLoS Journals</a> or to <a href="${plosOneUrl}${plosOneContext}">PLoS ONE</a>.</p>
</div>
<!-- end : main contents -->

<#include "/global/global_bottom.ftl">