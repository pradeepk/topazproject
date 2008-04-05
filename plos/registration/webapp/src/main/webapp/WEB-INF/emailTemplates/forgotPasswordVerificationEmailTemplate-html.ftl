<!--
  $HeadURL::                                                                            $
  $Id$
  
  Copyright (c) 2007-2008 by Topaz, Inc.
  http://topazproject.org
  
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
  
  http://www.apache.org/licenses/LICENSE-2.0
  
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->
<html>
<body>
Thank you <b>${user.loginName}</b> for requesting to reset your PLoS password.<br/>

Please click on this link to reset your password:

<a href="${url}?loginName=${user.loginName}&resetPasswordToken=${user.resetPasswordToken}">Reset password</a>
<br/><br/>
or copy and paste this link if you have problems:<br/>
${url}?loginName=${user.loginName}&resetPasswordToken=${user.resetPasswordToken}

</body>
</html>

