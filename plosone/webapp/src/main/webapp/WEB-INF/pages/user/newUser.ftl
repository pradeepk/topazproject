<html>
  <head>
    <title>Create a new user</title>
  </head>
  <body>
    <p>
      <fieldset>
          <legend>Create a User</legend>
          <@ww.form name="createNewUserForm" action="createNewUser" namespace="/user/secure" method="post">
            <@ww.textfield name="username" label="Display name" required="true"/>
            <@ww.textfield name="email" label="Email" required="true" readonly="true"/>
            <@ww.textfield name="realName" label="Full name" />
            <@ww.submit value="create user" />
          </@ww.form>
      </fieldset>
    </p>
  </body>
</html>
