<html>
    <head>
        <title>Plosone registration</title>
    </head>
    <body>
        <h1>Please register</h1>

        <p>
            <fieldset>
                <legend>Please register</legend>
                <@ww.form method="post" name="registrationFormPart1" action="registerSubmit">
                  <@ww.textfield name="loginName1" label="Please enter your email address" />
                  <@ww.textfield name="loginName2" label="Please enter your email address again" />
                  <@ww.password name="password1" label="Enter your password" />
                  <@ww.password name="password2" label="Enter your password again" />
                  <@ww.submit value="register me" />
                </@ww.form>
            </fieldset>
        </p>
    </body>
</html>
