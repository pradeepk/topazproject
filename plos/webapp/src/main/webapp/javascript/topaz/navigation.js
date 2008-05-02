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

/**
 * topaz.navigation
 * 
 * This class builds the table of content navigation in the right-hand column. 
 **/
dojo.provide("topaz.navigation");
topaz.navigation = {
 buildTOC: function(tocObj){
   var tocEl = document.getElementsByTagAndAttributeName(null, 'toc');
   
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