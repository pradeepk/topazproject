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
<html>
  <head>
    <title>PLoS ONE: Administration: Manage Users</title>
  </head>
  <body>

    <h1 style="text-align: center">PLoS ONE: Administration: Manage Users</h1>

    <@s.url id="adminTop" namespace="/admin" action="adminTop"/>
    <p style="text-align: right">
      <@s.a href="${adminTop}">Admin Top</@s.a>
    </p>
    <hr />

    <#include "../admin/templates/messages.ftl">

    <p>
      <fieldset>
          <legend><b>Find User by Id</b></legend>
          <@s.form name="findUserByUserIdForm" action="findUserByUserId" namespace="/admin" method="post">
            <@s.textfield name="authId" label="User Id" required="true"/>
            <@s.submit value="Find User Id" />
          </@s.form>
      </fieldset>
    </p>

    <p>
      <fieldset>
          <legend><b>Find User by Email</b></legend>
          <@s.form name="findUserByEmailAddressForm" action="findUserByEmailAddress" namespace="/admin" method="post">
            <@s.textfield name="emailAddress" label="Email Address" required="true"/>
            <@s.submit value="Find User Email" />
          </@s.form>
      </fieldset>
    </p>

    <#if topazUserIdList?exists>
      <p>
        <fieldset>
          <legend><b>Edit User Profiles</b></legend>
          <ul>
            <#list topazUserIdList as topazId>
              <@s.url id="editProfileByAdminURL" action="editProfileByAdmin" namespace="/admin" topazId="${topazId}" includeParams="none"/>
              <@s.url id="editPreferencesByAdminURL" action="retrieveUserAlertsByAdmin" namespace="/admin" topazId="${topazId}" includeParams="none"/>
              <li>
                Edit <@s.a href="%{editProfileByAdminURL}">profile</@s.a> for ${topazId}
              </li>
            </#list>
          </ul>
        </fieldset>
      </p>
    </#if>

    <p>
      <fieldset>
        <legend><b>Assign Admin Role To User</b></legend>
        <@s.form name="assignAdminRoleForm" action="assignAdminRole" namespace="/admin" method="post">
          <@s.textfield name="topazId" label="Id" required="true"/>
          &nbsp;
          <@s.submit value="Assign Admin Role"/>
        </@s.form>
      </fieldset>
    </p>
  </body>
</html>
