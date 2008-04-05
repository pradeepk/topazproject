<!--
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
<h1>Forgot Your Password?</h1>
	<!--<p><strong>Instruction Title   Text.</strong> Additional Instructoins here.</p>-->
	<p>Fields marked with <span class="required">*</span> are required. </p>
  <@s.form cssClass="pone-form" method="post" name="forgotPasswordForm" id="forgotPasswordForm" action="forgotPasswordSubmit" title="Forgot Password Form">
		
	<fieldset>
		<legend>Recover password</legend>
		<ol class="field-list">
    	<@s.textfield name="loginName" label="E-mail " required="true" id="email" tabindex="1" maxlength="256"/>
		</ol>
  	<div class="btnwrap">
	  <@s.submit name="submit" id="submit" value="Submit" tabindex="2"/>
	</div>
	</fieldset>
	
	</@s.form>

	<ul>
          <li><a href="http://journals.plos.org/help.php">Help</a></li>
	  <li>Already registered? <a href="${plosOneUrl}${plosOneContext}/profile">Login</a>.</li>
          <li><a href="/plos-registration/register.action">Register for a New Account</a></li>
	  <li><a href="/plos-registration/resendRegistration.action" title="Click here if you need to confirm your e-mail address">Resend e-mail address confirmation</a></li>
	</ul>
	
</div>
<!-- end : main contents -->
<#include "/global/global_bottom.ftl">