<#if Parameters.tabId?exists>
   <#assign tabId = Parameters.tabId>
<#else>
   <#assign tabId = "">
</#if>


<h2>PLoS ONE Member Profiles: Create a Profile</h2>
	<p><strong>Create or Update Your Profile</strong></p>
	<p>Fields marked with an <span class="required">*</span> are required. </p>

<@ww.form name="userForm" id="userForm"  method="post" title="User Information Form" cssClass="pone-form">

<fieldset>
  <legend>Your Profile</legend>
  <ol>
    <li><label for="email">Email address</label>
      ${email}
    </li>
   	  <#if tabId?has_content>	
      	<@ww.textfield name="displayName" onfocus="topaz.horizontalTabs.setTempValue(this);" onchange="topaz.horizontalTabs.checkValue(this);" label="Username" required="true" tabindex="101" after="Your user name will appear publicly"/>
	  <#else>
      	<@ww.textfield name="displayName" label="Username" required="true" tabindex="101" after="Your user name will appear publicly"/>
	  </#if>
			<li>
				<ol>
   	  <#if tabId?has_content>	
          <@ww.textfield name="givenNames" onfocus="topaz.horizontalTabs.setTempValue(this);" onchange="topaz.horizontalTabs.checkValue(this);" label="First/Given Name" required="true" tabindex="102" />
	  <#else>
          <@ww.textfield name="givenNames" label="First/Given Name" required="true" tabindex="102" />
	  </#if>
   	  <#if tabId?has_content>	
          <@ww.textfield name="surnames" onfocus="topaz.horizontalTabs.setTempValue(this);" onchange="topaz.horizontalTabs.checkValue(this);" label="Last/Family Name" required="true" tabindex="103"/>
	  <#else>
          <@ww.textfield name="surnames" label="Last/Family Name" required="true" tabindex="103"/>
	  </#if>
				</ol>

			</li>
		</ol>
	</fieldset>
	<fieldset>
	<legend>Your Extended Profile</legend>
		<ol>
			<li>
   	  <#if tabId?has_content>	
        <@ww.textarea name="postalAddress" onfocus="topaz.horizontalTabs.setTempValue(this);" onchange="topaz.horizontalTabs.checkValue(this);" label="Address" cssClass="long-input"  rows="5" cols="50" tabindex="106" />
	  <#else>
        <@ww.textarea name="postalAddress" label="Address" cssClass="long-input"  rows="5" cols="50" tabindex="106" />
	  </#if>
				<ol>
   	  <#if tabId?has_content>	
          <@ww.textfield name="city" onfocus="topaz.horizontalTabs.setTempValue(this);" onchange="topaz.horizontalTabs.checkValue(this);" label="City" required="true" tabindex="107"/>
	  <#else>
          <@ww.textfield name="city" label="City" required="true" tabindex="107"/>
	  </#if>
   	  <#if tabId?has_content>	
          <@ww.textfield name="country" onfocus="topaz.horizontalTabs.setTempValue(this);" onchange="topaz.horizontalTabs.checkValue(this);" label="Country" required="true" tabindex="111"/>
	  <#else>
          <@ww.textfield name="country" label="Country" required="true" tabindex="111"/>
	  </#if>
				</ol>
				<fieldset class="public-private">
				<legend>Choose display settings for your address </legend>
   	  <#if tabId?has_content>	
          <@ww.radio name="extendedVisibility" onfocus="topaz.horizontalTabs.setTempValue(this);" onclick="topaz.horizontalTabs.checkValue(this);" label="Public" list="{'public'}" checked="true" tabindex="112" cssClass="radio" />
	  <#else>
          <@ww.radio name="extendedVisibility" label="Public" list="{'public'}" checked="true" tabindex="112" cssClass="radio" />
	  </#if>
   	  <#if tabId?has_content>	
          <@ww.radio name="extendedVisibility" onfocus="topaz.horizontalTabs.setTempValue(this);" onclick="topaz.horizontalTabs.checkValue(this);" label="Private" list="{'private'}" tabindex="113" cssClass="radio" />
	  <#else>
          <@ww.radio name="extendedVisibility" label="Private" list="{'private'}" tabindex="113" cssClass="radio" />
	  </#if>
				</fieldset>
			</li>
			<li class="form-last-item">
				<ol>
          <@ww.action name="selectList" namespace="" id="selectList"/>
   	  <#if tabId?has_content>	
          <@ww.select label="Organization Type" onfocus="topaz.horizontalTabs.setTempValue(this);" onselect="topaz.horizontalTabs.checkValue(this);" name="organizationType" value="organizationType"
          list="%{#selectList.allOrganizationTypes}" tabindex="114" />
	  <#else>
          <@ww.select label="Organization Type" name="organizationType" value="organizationType"
          list="%{#selectList.allOrganizationTypes}" tabindex="114" />
	  </#if>
          <@ww.textfield name="organizationName" onfocus="topaz.horizontalTabs.setTempValue(this);" onchange="topaz.horizontalTabs.checkValue(this);" label="Organization Name" tabindex="115" />
				</ol>
				<ol>
            <@ww.select label="Title" name="title" value="title"
            list="%{#selectList.allTitles}" tabindex="116" />

   	  <#if tabId?has_content>	
            <@ww.select label="Position Type" onfocus="topaz.horizontalTabs.setTempValue(this);" onselect="topaz.horizontalTabs.checkValue(this);" name="positionType" value="positionType"
            list="%{#selectList.allPositionTypes}" tabindex="117" />
	  <#else>
            <@ww.select label="Position Type" name="positionType" value="positionType" list="%{#selectList.allPositionTypes}" tabindex="117" />
	  </#if>
				</ol>
				<fieldset class="public-private">
				<legend>Choose display settings for your organization and title</legend>
   	  <#if tabId?has_content>	
          <@ww.radio name="orgVisibility" onfocus="topaz.horizontalTabs.setTempValue(this);" onclick="topaz.horizontalTabs.checkValue(this);" label="Public" list="{'public'}" tabindex="118" cssClass="radio" />
	  <#else>
          <@ww.radio name="orgVisibility" label="Public" list="{'public'}" tabindex="118" cssClass="radio" />
	  </#if>
   	  <#if tabId?has_content>	
          <@ww.radio name="orgVisibility" onfocus="topaz.horizontalTabs.setTempValue(this);" onclick="topaz.horizontalTabs.checkValue(this);" label="Private" list="{'private'}" tabindex="119" cssClass="radio" />
	  <#else>
          <@ww.radio name="orgVisibility" label="Private" list="{'private'}" tabindex="119" cssClass="radio" />
	  </#if>
				</fieldset>
		  </li>
		</ol>
	</fieldset>
	<fieldset>
		<legend>Optional Public Information</legend>
		<ol>
   	  <#if tabId?has_content>	
	      <@ww.textarea name="biographyText" onfocus="topaz.horizontalTabs.setTempValue(this);" onchange="topaz.horizontalTabs.checkValue(this);" label="About Me" rows="5" cols="50" tabindex="120"/>
	  <#else>
	      <@ww.textarea name="biographyText" label="About Me" rows="5" cols="50" tabindex="120"/>
	  </#if>
   	  <#if tabId?has_content>	
	      <@ww.textfield name="researchAreasText" onfocus="topaz.horizontalTabs.setTempValue(this);" onchange="topaz.horizontalTabs.checkValue(this);" label="Research Areas" cssClass="long-input" tabindex="121" />
	  <#else>
	      <@ww.textfield name="researchAreasText" label="Research Areas" cssClass="long-input" tabindex="121" />
	  </#if>
   	  <#if tabId?has_content>	
	      <@ww.textfield name="interestsText" onfocus="topaz.horizontalTabs.setTempValue(this);" onchange="topaz.horizontalTabs.checkValue(this);" label="Interests"  cssClass="long-input" tabindex="122" />
	  <#else>
	      <@ww.textfield name="interestsText" label="Interests"  cssClass="long-input" tabindex="122" />
	  </#if>
			<li>
   	  <#if tabId?has_content>	
        <@ww.textfield name="homePage" onfocus="topaz.horizontalTabs.setTempValue(this);" onchange="topaz.horizontalTabs.checkValue(this);" label="Home page"  cssClass="long-input" tabindex="123" />
	  <#else>
        <@ww.textfield name="homePage" label="Home page"  cssClass="long-input" tabindex="123" />
	  </#if>
   	  <#if tabId?has_content>	
        <@ww.textfield name="weblog" onfocus="topaz.horizontalTabs.setTempValue(this);" onchange="topaz.horizontalTabs.checkValue(this);" label="Weblog"  cssClass="long-input" tabindex="124" />
	  <#else>
        <@ww.textfield name="weblog" label="Weblog"  cssClass="long-input" tabindex="124" />
	  </#if>
			</li>
		</ol>
      <div class="btnwrap"><input type="button" id="formSubmit" name="formSubmit" value="Submit" tabindex="125"/></div>
	</fieldset>

</@ww.form>

