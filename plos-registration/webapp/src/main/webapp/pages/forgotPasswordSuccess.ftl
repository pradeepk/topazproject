<#include "/global/global_config.ftl">
<#include "/global/global_top.ftl">

<!-- begin : main content -->
<div id="content">
<h1>Forgot Your Password?</h1>
	<p><strong>Password Recovery</strong>  An email has been sent to: <@ww.property value="loginName"/> with instructions on how to reset your password.

	<!--
		<@ww.url includeParams="none" id="forgotPasswordEmailURL" action="forgotPasswordVerify">
    	<@ww.param name="loginName" value="user.loginName"/>
      <@ww.param name="resetPasswordToken" value="user.resetPasswordToken"/>
    </@ww.url>
    <@ww.a href="%{forgotPasswordEmailURL}"  >${forgotPasswordEmailURL}</@ww.a>
  -->
	</p>
	<br/>
		<p>Continue to <a href="${plosOneUrl}${plosOneContext}">PLoS ONE</a></p>
</div>
<!-- end : main contents -->

<#include "/global/global_bottom.ftl">