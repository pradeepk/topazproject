/*
 * $HeadURL:: http://gandalf.topazproject.org/svn/head/plos/webapp/src/main/webapp/javas#$
 * $Id: navigation.js 5581 2008-05-02 23:01:11Z jkirton $
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

/**
 * ambra.navigation
 * 
 * This class builds the table of content navigation in the right-hand column. 
 **/
dojo.provide("ambra.navigation");
dojo.require("ambra.general");
ambra.navigation = {
 buildTOC: function(tocObj){
   var tocEl = document.getElementsByTagAndAttributeName(null, 'toc');
   if(!tocObj || !tocEl) return;
   
   var ul = document.createElement('ul');
   
   for (var i=0; i<tocEl.length; i++) {
     var li = document.createElement('li');
     var anchor = document.createElement('a');
     anchor.href = "#" + tocEl[i].getAttributeNode('toc').nodeValue;
     if (i == tocEl.length -1) {
       anchor.className = 'last';
     }
     var tocText = document.createTextNode(tocEl[i].getAttributeNode('title').nodeValue);
     anchor.appendChild(tocText);
     li.appendChild(anchor);
     
     ul.appendChild(li);
   }
   
   tocObj.appendChild(ul);
 } 
}  