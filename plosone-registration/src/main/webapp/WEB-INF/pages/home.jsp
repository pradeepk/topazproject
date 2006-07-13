<%@ taglib prefix="ww" uri="/webwork" %>

<html>
    <head>
        <title>Welcome</title>
    </head>
    <body>
        <h1>Welcome to the Plosone webapp</h1>

        <p>
            <fieldset>
                <legend>A few things for you to do</legend>
                <p>
                    <ww:url id="registerURL" action="registerPart1" />
                    <ww:a href="%{registerURL}">Register</ww:a>
                </p>

                <p>
                    <ww:url id="forgotPasswordURL" action="forgotPassword" />
                    <ww:a href="%{forgotPasswordURL}">Forgot Password</ww:a>
                </p>


            </fieldset>
        </p>
    </body>
</html>
