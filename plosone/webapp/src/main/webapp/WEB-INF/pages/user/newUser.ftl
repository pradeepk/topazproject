<#function isFound collection value>
  <#list collection as element>
    <#if element == value>
      <#return "true"/>
    </#if>
  </#list>
  <#return "false"/>
</#function>

<html>
  <head>
    <title>Create a new user</title>
  </head>
  <body>
    <p>
      <fieldset>
          <legend>Create a User</legend>
          <@ww.form name="createNewUserForm" action="createNewUser" namespace="/user" method="post">
            <@ww.textfield name="displayName" label="Display name" required="true"/>
            <@ww.checkbox name="privateFields" label="Is it private?" fieldValue="displayName" value="${isFound(privateFields, 'displayName')}"/>
            <@ww.textfield name="email" label="Email" required="true" readonly="true"/>
            <@ww.checkbox name="privateFields" label="Is it private?" fieldValue="email" value="${isFound(privateFields, 'email')}"/>
            <@ww.textfield name="realName" label="Full name" />
            <@ww.checkbox name="privateFields" label="Is it private?" fieldValue="realName" value="${isFound(privateFields, 'realName')}"/>
            <@ww.textfield name="givennames" label="Givennames" />
            <@ww.checkbox name="privateFields" label="Is it private?" fieldValue="givennames" value="${isFound(privateFields, 'givennames')}"/>
            <@ww.textfield name="surnames" label="Surnames" />
            <@ww.checkbox name="privateFields" label="Is it private?" fieldValue="surnames" value="${isFound(privateFields, 'surnames')}"/>
            <@ww.textfield name="positionType" label="PositionType" />
            <@ww.checkbox name="privateFields" label="Is it private?" fieldValue="positionType" value="${isFound(privateFields, 'positionType')}"/>
            <@ww.textfield name="organizationName" label="OrganizationName" />
            <@ww.checkbox name="privateFields" label="Is it private?" fieldValue="organizationName" value="${isFound(privateFields, 'organizationName')}"/>
            <@ww.textfield name="organizationType" label="OrganizationType" />
            <@ww.checkbox name="privateFields" label="Is it private?" fieldValue="organizationType" value="${isFound(privateFields, 'organizationType')}"/>
            <@ww.textfield name="postalAddress" label="PostalAddress" />
            <@ww.checkbox name="privateFields" label="Is it private?" fieldValue="postalAddress" value="${isFound(privateFields, 'postalAddress')}"/>
            <@ww.textfield name="biographyText" label="BiographyText" />
            <@ww.checkbox name="privateFields" label="Is it private?" fieldValue="biographyText" value="${isFound(privateFields, 'biographyText')}"/>
            <@ww.textfield name="interestsText" label="InterestsText" />
            <@ww.checkbox name="privateFields" label="Is it private?" fieldValue="interestsText" value="${isFound(privateFields, 'interestsText')}"/>
            <@ww.textfield name="researchAreasText" label="ResearchAreasText" />
            <@ww.checkbox name="privateFields" label="Is it private?" fieldValue="researchAreasText" value="${isFound(privateFields, 'researchAreasText')}"/>
            <@ww.textfield name="city" label="City" />
            <@ww.checkbox name="privateFields" label="Is it private?" fieldValue="city" value="${isFound(privateFields, 'city')}"/>
            <@ww.textfield name="country" label="Country" />
            <@ww.checkbox name="privateFields" label="Is it private?" fieldValue="country" value="${isFound(privateFields, 'country')}"/>
            <@ww.submit value="create user" />
          </@ww.form>
      </fieldset>
    </p>
  </body>
</html>
