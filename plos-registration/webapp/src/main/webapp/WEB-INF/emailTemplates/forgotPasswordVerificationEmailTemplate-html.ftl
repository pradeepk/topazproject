<html>
<body>
Thank you <b>${user.loginName}</b> for registering with our website.<br/>
Your password reset token: ${user.resetPasswordToken}<br/>

Please click on this link to verify your email address:

<a href="${url}?loginName=${user.loginName}&resetPasswordToken=${user.resetPasswordToken}">Reset password link</a>

</body>
</html>

