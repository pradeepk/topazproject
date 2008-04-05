/*
 * $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2008 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
var tabsListMap = new Array();

tabsListMap[tabsListMap.length] = {tabKey:   "preferences",
                                   title:    "Preferences",
                                   formName: "userForm",
                                   urlLoad:  "/user/secure/editAjaxProfile.action",
                                   urlSave:  "/user/secure/saveAjaxProfile.action"};

tabsListMap[tabsListMap.length] = {tabKey:   "alerts",
                                   title:    "Alerts",
                                   formName: "userAlerts",
                                   urlLoad:  "/user/secure/editAjaxAlerts.action",
                                   urlSave:  "/user/secure/saveAjaxAlerts.action"};

var querystring = topaz.htmlUtil.getQuerystring();
var tabSelectId = "";

for (var i=0; i<querystring.length; i++) {
  if (querystring[i].param == "tabId") {
    tabSelectId = querystring[i].value;
  }
}

var profileConfig = {
    tabPaneSetId: "tabPaneSet",
    tabsContainer: "tabsContainer",
    tabSelectId: tabSelectId
  }                                 