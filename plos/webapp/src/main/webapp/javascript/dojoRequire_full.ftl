<#--
  $HeadURL:: http://gandalf.topazproject.org/svn/head/plos/webapp/src/main/webapp/javas#$
  $Id: dojoRequire_default.ftl 5405 2008-04-10 16:28:43Z alex $
  
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

dojo.registerModulePath("topaz", "../../topaz");

dojo.require("dijit.layout.ContentPane");

dojo.require("topaz.domUtil");
dojo.require("topaz.htmlUtil");
dojo.require("topaz.formUtil");
dojo.require("topaz.widget.RegionalDialog");
dojo.require("topaz.navigation");
dojo.require("topaz.horizontalTabs");
dojo.require("topaz.floatMenu");
dojo.require("topaz.annotation");
dojo.require("topaz.corrections");
dojo.require("topaz.displayComment");
dojo.require("topaz.responsePanel");
dojo.require("topaz.rating");
dojo.require("topaz.slideshow");
