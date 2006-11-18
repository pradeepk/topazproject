<#include "/global/global_config.ftl">
<#include "/global/global_top.ftl">

<!-- begin : main content -->
<div id="content">
<h2>Sign up for PLoS One </h2>
	<!--<p><strong>Instruction Title   Text.</strong> Additional Instructoins here.</p>-->
	<p>Field marked with an <span class="required">*</span> are required. </p>


  <@ww.form cssClass="pone-form" method="post" name="registrationFormPart1" action="registerSubmit" title="Registration Form">

  <fieldset>
		<legend>Registration</legend>
		<ol>
      <@ww.textfield label="Email " name="loginName1" required="true" tabindex="101" maxlength="256"/>
      <@ww.textfield label="Please re-type your email " name="loginName2" required="true" tabindex="102" maxlength="256"/>
      <@ww.password label="Password " name="password1" required="true" tabindex="103" maxlength="128"/>
      <@ww.password label="Please re-type your password " name="password2" required="true" tabindex="104" maxlength="128"/>
		</ol>
      <@ww.submit name="submit" value="Submit" tabindex="105"/>
	</fieldset>
  </@ww.form>

</div>
<!-- end : main contents -->

<#include "/global/global_bottom.ftl">