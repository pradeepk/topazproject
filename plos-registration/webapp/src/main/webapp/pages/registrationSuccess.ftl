<#include "/global/global_config.ftl">
<#include "/global/global_top.ftl">

<!-- begin : main content -->
<div id="content">
<h2>Sign up for PLoS One</h2>
	<p>
		<strong>Thanks for registering!</strong>
		Please check your email inbox to confirm your registration.
 
		<!--
		<@ww.url includeParams="none" id="emailVerificationURL" action="emailVerification">
    	<@ww.param name="loginName" value="user.loginName"/>
      <@ww.param name="emailVerificationToken" value="user.emailVerificationToken"/>
    </@ww.url>
    <@ww.a href="%{emailVerificationURL}"  >${emailVerificationURL}</@ww.a>
		-->
	</p>
	<br/>
	<p>Continue to <a href="${plosOneUrl}${plosOneContext}">PLoS ONE</a></p>
</div>
<!-- end : main contents -->

<#include "/global/global_bottom.ftl">