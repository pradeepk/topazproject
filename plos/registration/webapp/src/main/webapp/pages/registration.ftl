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
<#include "/global/global_config.ftl">
<#include "/global/global_top.ftl">

<!-- begin : main content -->
<div id="content">
<h1>Sign Up for a PLoS Journals Profile</h1>
	<!--<p><strong>Instruction Title   Text.</strong> Additional Instructoins here.</p>-->
	<p>Fields marked with <span class="required">*</span> are required.</p>

  <@s.form cssClass="pone-form" method="post" name="registrationFormPart1" action="registerSubmit" title="Registration Form">

  <fieldset>
	<legend>Registration</legend>
	<ol>
	<@s.textfield label="E-mail " name="loginName1" required="true" tabindex="1" maxlength="256"/>
	<@s.textfield label="Please re-type your e-mail " name="loginName2" required="true" tabindex="2" maxlength="256"/>
	<@s.password label="Password " name="password1" required="true" tabindex="3" maxlength="255" after=" (Password must be at least 6 characters)"/>
	<@s.password label="Please re-type your password " name="password2" required="true" tabindex="4" maxlength="128"/>
	</ol>
	<div class="btnwrap">
		<@s.submit value="Submit" tabindex="5"/>
	</div>
	</fieldset>
  </@s.form>
  <ul>
      <li><a href="http://journals.plos.org/help.php#account">Help</a></li>
      <li>Already registered? <a href="${plosOneUrl}${plosOneContext}/profile">Login</a>.</li>
      <li><a href="/ambra-registration/forgotPassword.action" title="Click here if you forgot your password">Forgotten Password?</a></li>
      <li><a href="/ambra-registration/resendRegistration.action" title="Click here if you need to confirm your e-mail address">Resend e-mail address confirmation</a></li>  
   </ul>
</div>
<!-- end : main contents -->

<#include "/global/global_bottom.ftl">
