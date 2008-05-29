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
var _ldc;

dojo.addOnLoad(function() {
  _ldc = dijit.byId("LoadingCycle");
  _ldc.show();

  ambra.horizontalTabs.setTabPaneSet(dojo.byId(profileConfig.tabPaneSetId));
  ambra.horizontalTabs.setTabsListObject(tabsListMap);
  ambra.horizontalTabs.setTabsContainer(dojo.byId(profileConfig.tabsContainer));
  ambra.horizontalTabs.init(tabSelectId);
  
  dojo.addOnUnload(ambra.horizontalTabs.confirmChange(ambra.horizontalTabs.targetFormObj));
  
  if (tabSelectId == "alerts") {
    var alertsForm = document.userAlerts;
    ambra.formUtil.selectCheckboxPerCollection(alertsForm.checkAllWeekly, alertsForm.weeklyAlerts);
    ambra.formUtil.selectCheckboxPerCollection(alertsForm.checkAllMonthly, alertsForm.monthlyAlerts);
  }
  
  _ldc.hide();
});