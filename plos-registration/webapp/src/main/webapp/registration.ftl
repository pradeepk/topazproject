<#include "/global/global_config.ftl">
<#include "/global/global_top.ftl">

<!-- begin : main content -->
<div id="content">
<h2>Sign up for PLoS One </h2>
	<p><strong>Instruction Title   Text.</strong> General Instructions- Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam erat volutpat. Ut wisi enim ad minim veniam, quis nostrud exerci tation ullamcorper suscipit lobortis nisl ut aliquip ex ea commodo consequat. Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat, vel illum dolore eu feugiat nulla facilisis at vero eros et accumsan et iusto odio dignissim qui blandit praesent luptatum zzril delenit augue duis dolore te feugait nulla facilisi.</p>
	<p>Field marked with an <span class="required">*</span> are required. </p>
	<p class="required">Please correct the errors below. </p>

  <@ww.form cssClass="pone-form" method="post" name="registrationFormPart1" action="registerSubmit" title="Registration Form">

  <fieldset>
		<legend>Registration</legend>
		<ol>
      <@ww.textfield label="Email " name="loginName1" required="true" tabindex="101" maxlength="256"/>
      <@ww.textfield label="Please re-type your email " name="loginName2" required="true" tabindex="102" maxlength="256"/>
      <@ww.textfield label="Password " name="password1" required="true" tabindex="103" maxlength="128"/>
      <@ww.textfield label="Please re-type your password " name="password2" required="true" tabindex="104" maxlength="128"/>
		</ol>
      <@ww.submit name="submit" value="Submit" tabindex="105"/>
	</fieldset>
  </@ww.form>

</div>
<!-- end : main contents -->

<#include "/global/global_bottom.ftl">