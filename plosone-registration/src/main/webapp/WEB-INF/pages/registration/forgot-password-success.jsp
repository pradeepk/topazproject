<%@ taglib prefix="ww" uri="/webwork" %>

<html>
    <head>
        <title>Reset password email sent</title>
    </head>
    <body>
        <br/>
        <h3>Hello <ww:property value="loginName"/> : Please check your email account for further instructions to reset your password.</h3>

        <fieldset>
            <legend>Email body</legend>
            <p>

              Please click the following link to verify your email address:

              <ww:url id="forgotPasswordVerificationURL" action="forgotPasswordChangePassword">
                <ww:param name="loginName" value="user.loginName"/>
                <ww:param name="resetPasswordToken" value="user.resetPasswordToken"/>
              </ww:url>
              <ww:a href="%{forgotPasswordVerificationURL}"  >%{forgotPasswordVerificationURL}</ww:a>
            </p>
        </fieldset>

    </body>
</html>
