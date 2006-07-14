<html>
    <head>
        <title>Reset password</title>
    </head>
    <body>
        <br/>
        <h3>Hello <@ww.property value="loginName"/></h3>

        <fieldset>
            <legend>Please enter a new password</legend>
            <p>

              <%--<@ww.form method="post" name="forgotPasswordChangePasswordForm" action="forgotPasswordChangePasswordSubmit">--%>
              <@ww.form name="forgotPasswordChangePasswordForm" action="forgotPasswordChangePasswordSubmit">
                <@ww.hidden name="loginName" />
                <@ww.hidden name="resetPasswordToken" />
                <@ww.textfield name="password1" label="Enter your new password" />
                <@ww.textfield name="password2" label="Enter your new password again" />
                <@ww.submit value="change my password" />
              </@ww.form>

            </p>
        </fieldset>

    </body>
</html>


