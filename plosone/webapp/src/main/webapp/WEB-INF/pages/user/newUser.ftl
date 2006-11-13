<html>
  <head>
    <title>Create a new user</title>
  </head>
  <body>
    <p>
      <fieldset>
          <legend>Create a User</legend>
          <@ww.form name="createNewUserForm" action="createNewUser" namespace="/user/create" method="post">
            <@ww.textfield name="username" label="Display name" required="true"/>
            <@ww.checkbox name="privateFields" label="Is it private?" fieldValue="username"/>
            <@ww.textfield name="email" label="Email" required="true" readonly="true"/>
            <@ww.checkbox name="privateFields" label="Is it private?" fieldValue="email"/>
            <@ww.textfield name="realName" label="Full name" />
            <@ww.checkbox name="privateFields" label="Is it private?" fieldValue="realName"/>
            <@ww.textfield name="displayName" label="Username" />
            <@ww.checkbox name="privateFields" label="Is it private?" fieldValue="displayName"/>
            <@ww.textfield name="givennames" label="Givennames" />
            <@ww.checkbox name="privateFields" label="Is it private?" fieldValue="givennames"/>
            <@ww.textfield name="surnames" label="Surnames" />
            <@ww.checkbox name="privateFields" label="Is it private?" fieldValue="surnames"/>
            <@ww.textfield name="positionType" label="PositionType" />
            <@ww.checkbox name="privateFields" label="Is it private?" fieldValue="positionType"/>
            <@ww.textfield name="organizationName" label="OrganizationName" />
            <@ww.checkbox name="privateFields" label="Is it private?" fieldValue="organizationName"/>
            <@ww.textfield name="organizationType" label="OrganizationType" />
            <@ww.checkbox name="privateFields" label="Is it private?" fieldValue="organizationType"/>
            <@ww.textfield name="postalAddress" label="PostalAddress" />
            <@ww.checkbox name="privateFields" label="Is it private?" fieldValue="postalAddress"/>
            <@ww.textfield name="biographyText" label="BiographyText" />
            <@ww.checkbox name="privateFields" label="Is it private?" fieldValue="biographyText"/>
            <@ww.textfield name="interestsText" label="InterestsText" />
            <@ww.checkbox name="privateFields" label="Is it private?" fieldValue="interestsText"/>
            <@ww.textfield name="researchAreasText" label="ResearchAreasText" />
            <@ww.checkbox name="privateFields" label="Is it private?" fieldValue="researchAreasText"/>
            <@ww.textfield name="city" label="City" />
            <@ww.checkbox name="privateFields" label="Is it private?" fieldValue="city"/>
            <@ww.textfield name="country" label="Country" />
            <@ww.checkbox name="privateFields" label="Is it private?" fieldValue="country"/>
            <@ww.submit value="create user" />
          </@ww.form>
      </fieldset>
    </p>
  </body>
</html>
