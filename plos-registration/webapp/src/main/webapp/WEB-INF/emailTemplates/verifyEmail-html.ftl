<html>
<body>
Thank you <b>${user.loginName}</b> for registering with our website.<br/>
Your email verification token: ${user.emailVerificationToken}<br/>

Please click on this link to verify your email address:

<a href="${verificationUrl}?loginName=${user.loginName}&emailVerificationToken=${user.emailVerificationToken}">Verification link</a>

</body>
</html>

