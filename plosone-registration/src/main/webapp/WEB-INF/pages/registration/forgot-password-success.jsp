<%@ taglib prefix="ww" uri="/webwork" %>

<html>
    <head>
        <title>Email sent</title>
    </head>
    <body>
        <br/>
        <h3>Hello <ww:property value="email"/> : Please check your email account for further instructions to reset your password.</h3>

        <p>
          Your password: <ww:property value="password1"/>
        </p>


        <fieldset>
            <legend>Email body</legend>
            <p>

              Please click the following link to verify your email address:

              <ww:url id="forgotPasswordVerificationURL" action="forgotPasswordVerification">
                <ww:param name="emailAddress" value="user.emailAddress"/>
                <ww:param name="emailVerificationToken" value="user.resetemailVerificationToken"/>
              </ww:url>
              <ww:a href="%{forgotPasswordVerificationURL}"  >%{forgotPasswordVerificationURL}</ww:a>
            </p>
        </fieldset>

    </body>
</html>
