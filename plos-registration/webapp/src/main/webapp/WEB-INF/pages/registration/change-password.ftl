<html>
    <head>
        <title>Change password</title>
    </head>
    <body>
        <br/>
        <fieldset>
            <legend>Change password</legend>
            <p>

              <@ww.form name="changePasswordForm" action="changePasswordSubmit">
                <@ww.textfield name="loginName" label="Enter your registered email address" />
                <@ww.textfield name="oldPassword" label="Enter your old password" />
                <@ww.textfield name="newPassword1" label="Enter your new password" />
                <@ww.textfield name="newPassword2" label="Enter your new password again" />
                <@ww.submit value="change my password" />
              </@ww.form>

            </p>
        </fieldset>

    </body>
</html>
