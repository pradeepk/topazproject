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
            <@ww.textfield name="email" label="Email" required="true" readonly="true"/>
            <@ww.textfield name="realName" label="Full name" />
            <@ww.textfield name="displayName" label="Username" />
            <@ww.textfield name="givennames" label="Givennames" />
            <@ww.textfield name="surnames" label="Surnames" />
            <@ww.textfield name="positionType" label="PositionType" />
            <@ww.textfield name="organizationName" label="OrganizationName" />
            <@ww.textfield name="organizationType" label="OrganizationType" />
            <@ww.textfield name="postalAddress" label="PostalAddress" />
            <@ww.textfield name="biographyText" label="BiographyText" />
            <@ww.textfield name="interestsText" label="InterestsText" />
            <@ww.textfield name="researchAreasText" label="ResearchAreasText" />
            <@ww.textfield name="city" label="City" />
            <@ww.textfield name="country" label="Country" />
            <@ww.submit value="create user" />
          </@ww.form>
      </fieldset>
    </p>
  </body>
</html>
