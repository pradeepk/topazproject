<%@ taglib prefix="ww" uri="/webwork" %>

<html>
    <head>
        <title>Plosone registration</title>
    </head>
    <body>
        <!--<h1>Forgot password</h1>-->

        <p>
            <fieldset>
                <legend>Forgot Password</legend>
                <ww:form name="forgotPasswordForm" action="forgotPassword">
                  <ww:textfield name="email" label="Please enter your previously registered email address" />
                  <ww:submit value="i want to reset my password" />
                </ww:form>
            </fieldset>
        </p>
    </body>
</html>
