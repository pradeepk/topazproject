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
  topaz.horizontalTabs.setTabPaneSet(dojo.byId(homeConfig.tabPaneSetId));
  topaz.horizontalTabs.setTabsListObject(tabsListMap);
  topaz.horizontalTabs.setTabsContainer(dojo.byId(homeConfig.tabsContainer));
  topaz.horizontalTabs.initSimple(tabSelectId);
});
