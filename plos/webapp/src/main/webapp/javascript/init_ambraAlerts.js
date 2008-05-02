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
var _alertForm;

dojo.addOnLoad(function() {
  _ldc = dijit.byId("LoadingCycle");
  
  _alertForm = document.userAlerts;

  _alertForm.action = _namespace + "/user/secure/saveAlerts.action";
  dojo.connect(_alertForm.formSubmit, "onclick", function() {
      _alertForm.submit(); 
      return true;
    }
  );
  
  topaz.formUtil.selectCheckboxPerCollection(_alertForm.checkAllWeekly, _alertForm.weeklyAlerts);
  topaz.formUtil.selectCheckboxPerCollection(_alertForm.checkAllMonthly, _alertForm.monthlyAlerts);
});
