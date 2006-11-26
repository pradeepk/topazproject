<#function isFound collection value>
  <#list collection as element>
    <#if element== value>
      <#return
      "true"/>
    </#if>
  </#list>
  <#return
  "false"/>
</#function>

<!-- begin : main content wrapper -->
<div id="content">
<h2>PLoS ONE Member Profiles: Create a Profile</h2>
	<p><strong>instruction Title   Text.</strong> General Instructions- Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam erat volutpat. Ut wisi enim ad minim veniam, quis nostrud exerci tation ullamcorper suscipit lobortis nisl ut aliquip ex ea commodo consequat. Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat, vel illum dolore eu feugiat nulla facilisis at vero eros et accumsan et iusto odio dignissim qui blandit praesent luptatum zzril delenit augue duis dolore te feugait nulla facilisi.</p>
	<p>Field marked with an <span class="required">*</span> are required. </p>

<@ww.form name="createNewUserForm" action="createNewUser" namespace="/user" method="post" title="User Information Form" cssClass="pone-form">

<fieldset>
  <legend>Your Profile</legend>
  <ol>
    <li><label for="email">Email address</label>
      ${email}
    </li>
      <@ww.textfield name="displayName" label="Username" required="true" tabindex="101" after="Your user name will appear publicly"/>
			<li>
				<ol>
          <@ww.textfield name="realName" label="First Name" required="true" tabindex="102" after=" required field"/>
          <@ww.textfield name="surnames" label="Last Name" required="true" tabindex="103"/>
				</ol>
				<fieldset class="public-private">
				<legend>Choose display settings for your real name </legend>
          <@ww.checkbox name="privateFields" label="Is it private?" fieldValue="realName" value="${isFound(privateFields, 'realName')}"/>
					<label for="public-private-realname"><input name="public-private-realname" value="public" tabindex="104" type="radio" class="radio" />
					Public </label>
					<label for="public-private-realname"><input name="public-private-realname" value="private" tabindex="105" type="radio" class="radio" />
					Private </label>
				</fieldset>
			</li>
		</ol>
	</fieldset>
	<fieldset>
	<legend>Your Extended Profile</legend>
		<ol>
			<li>
        <@ww.textfield name="postalAddress" label="Address" cssClass="long-input" tabindex="106" />
				<ol>
          <@ww.textfield name="city" label="City" required="true" tabindex="107"/>
          <@ww.textfield name="country" label="Country" required="true" tabindex="111"/>
				</ol>
				<fieldset class="public-private">
				<legend>Choose display settings for your address </legend>
          <@ww.checkbox name="privateFields" label="Is it private?" fieldValue="postalAddress" value="${isFound(privateFields, 'postalAddress')}"/>
					<label for="public-private-address"><input name="public-private-address" value="public" tabindex="112" checked="checked" type="radio" class="radio" />
					Public </label>
					<label for="public-private-address"><input name="public-private-address" value="private" tabindex="113" type="radio" class="radio" />
					Private </label>
				</fieldset>
			</li>
			<li class="form-last-item">
				<ol>
          <@ww.action name="selectList" namespace="" id="selectList"/>
          <@ww.select label="Organization Type" name="organizationType" value="organizationType"
          list="%{#selectList.allOrganizationTypes}" tabindex="114" />
          <@ww.textfield name="organizationName" label="Organization Name" tabindex="115" />
				</ol>
				<ol>
            <@ww.select label="Title" name="title" value="title"
            list="%{#selectList.allTitles}" tabindex="116" />

            <@ww.select label="Position Type" name="positionType" value="positionType"
            list="%{#selectList.allPositionTypes}" tabindex="117" />
				</ol>
				<fieldset class="public-private">
				<legend>Choose display settings for your organization and title</legend>
					<label for="public-private-organization"><input name="public-private-organization" value="public" tabindex="118" checked="checked" type="radio" class="radio" />
					Public </label>
					<label for="public-private-organization"><input name="public-private-organization" value="private" tabindex="119" type="radio" class="radio" />
					Private </label>
				</fieldset>
		  </li>
		</ol>
	</fieldset>
	<fieldset>
		<legend>Optional Information that will appear publicly</legend>
		<ol>
      <@ww.textarea name="biographyText" label="About Me" rows="5" cols="50" tabindex="120"/>
      <@ww.textfield name="researchAreasText" label="Research Areas" cssClass="long-input" tabindex="121" />
      <@ww.textfield name="interestsText" label="Interests"  cssClass="long-input" tabindex="122" />
			<li>
        <!-- TODO: no mapping found for URL and URL-description -->
        <@ww.textfield name="homePage" label="URL"  cssClass="long-input" tabindex="123" />

        <label for="url-description">URL Description</label>
				<select name="url-description" id="url-description" tabindex="124">
					<option value="none selected" selected="selected">Choose One</option>
					<option value="">Personal</option>
					<option value="">Laboratory</option>
					<option value="">Departmental</option>
					<option value="">Blog</option>
				</select>
			</li>
		</ol>
    <@ww.submit value="Submit" tabindex="125"/>
	</fieldset>

</@ww.form>


</div>
<!-- end : main content wrapper -->
