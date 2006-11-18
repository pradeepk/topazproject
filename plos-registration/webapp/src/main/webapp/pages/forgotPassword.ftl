<#include "/global/global_config.ftl">
<#include "/global/global_top.ftl">

<!-- begin : main content -->
<div id="content">
<h2>Forgot Your Password?</h2>
	<!--<p><strong>Instruction Title   Text.</strong> Additional Instructoins here.</p>-->
	<p>Field marked with an <span class="required">*</span> are required. </p>
  <@ww.form cssClass="pone-form" method="post" name="forgotPasswordForm" id="forgotPasswordForm" action="forgotPasswordSubmit" title="Forgot Password Form">

	<fieldset>
		<legend>Recover password</legend>
		<ol class="field-list">
    	<@ww.textfield name="loginName" label="Email " required="true" id="email" tabindex="101" maxlength="256"/>
		</ol>
	  <@ww.submit name="submit" id="submit" value="Submit" tabindex="102"/>
	</fieldset>
	
	</@ww.form>

</div>
<!-- end : main contents -->

<#include "/global/global_bottom.ftl">