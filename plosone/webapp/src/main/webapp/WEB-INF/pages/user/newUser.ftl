<html>
  <head>
    <title>Create a new user</title>
  </head>
  <body>
    <p>
      <fieldset>
          <legend>Create a User</legend>
          <@ww.form name="createNewUserForm" action="createNewUser" method="post">
            <@ww.textfield name="username" label="Username" value="" required="true"/>
            <@ww.textfield name="email" label="Email" value="" required="true"/>
            <@ww.textfield name="realName" label="Name" value=""/>
            <@ww.submit value="create user" />
          </@ww.form>
      </fieldset>
    </p>
  </body>
</html>
