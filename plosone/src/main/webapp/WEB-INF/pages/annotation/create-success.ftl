<html>
    <head>
        <title>Annotation created</title>
    </head>
    <body>
        <fieldset>
            <legend>Email body</legend>
            <p>

                Please click the following link to verify your email address:

                <@ww.url id="emailVerificationURL" action="emailVerification">
                  <@ww.param name="loginName" value="user.loginName"/>
                  <@ww.param name="emailVerificationToken" value="user.emailVerificationToken"/>
                </@ww.url>
                <@ww.a href="%{emailVerificationURL}"  >${emailVerificationURL}</@ww.a>
            </p>
        </fieldset>

    </body>
</html>
