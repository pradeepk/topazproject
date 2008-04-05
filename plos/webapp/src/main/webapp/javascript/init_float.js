/*
 * $HeadURL:: http://gandalf/svn/head/topaz/core/src/main/java/org/topazproject/otm/Abst#$
 * $Id: AbstractConnection.java 4807 2008-02-27 11:06:12Z ronald $
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
  var _annotationForm;
  
  function init(e) {

/*    if (loggedIn) {    
      var triggerNode = dojo.byId(annotationConfig.trigger);
    	dojo.event.connect(triggerNode, 'onmousedown', function(e) {
  	     topaz.annotation.createAnnotationOnMouseDown();
  	     e.preventDefault();
  	   }
    	);
    }
*/    
    var tocObj = dojo.byId('sectionNavTop');
    // topaz.navigation.buildTOC(tocObj);
    
    if (dojo.render.html.safari) {
      var tocObj = dojo.byId('sectionNavTopFloat');
      // topaz.navigation.buildTOC(tocObj);
    }
        
    dojo.event.connect(window, "onload", function() {
        if (dojo.render.html.safari) {
    //      var origDiv = dojo.byId("postcomment");
    //      var newDiv = origDiv.cloneNode(true);
    //      newDiv.id = "postcommentfloat";
    //      dojo.dom.insertAfter(newDiv, origDiv, true);
          var newDiv = dojo.byId("postcommentfloat");
          newDiv.style.display = "none";
        }
        floatMenu();
      }  
    );
    // Hack for browser compatibility on the use of scrolling in the viewport.
    //
    //if (BrowserDetect.browser == "Explorer") {
    dojo.event.connect(window, "onscroll", function() {
        floatMenu();
      }  
    );
    //}
    //else if (BrowserDetect.browser == "Firefox" && BrowserDetect.version < 2) {
    dojo.event.connect(document.documentElement, "onscroll", function() {
        floatMenu();
      }  
    );
    //}
    //else {
    dojo.event.connect(document.documentElement, "onkey", function() {
        floatMenu();
      }  
    );
    //}
    dojo.event.connect(window, "onresize", function() {
        floatMenu();
      }  
    );

    //errView = dojo.widget.byId("ErrorConsole");
    //var errClose = dojo.byId("btn_ok");
    //errView.setCloseControl(errClose);
    
  }
  
  dojo.addOnLoad(init);
