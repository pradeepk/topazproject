<#--
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
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<title>User Created!</title>
	</head>
	<body>
		User was created with ID: ${internalId}.
		<a href="${freemarker_config.getContext()}">Continue</a> on to PLoS ONE

    <br/>
    <@s.url id="displayUserURL" namespace="/user" action="displayUser" userId="${internalId}"/>
    <@s.a href="%{displayUserURL}">Display user info</@s.a><br/>

    <@s.url id="displayUserURL" namespace="/user/secure" action="displayPrivateFieldNames" userId="${internalId}"/>
    <@s.a href="%{displayUserURL}">Display user info</@s.a>

  </body>
</html>