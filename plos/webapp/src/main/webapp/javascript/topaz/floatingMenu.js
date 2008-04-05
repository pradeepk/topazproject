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
 * floatMenu()
 * 
 * The function is activated when the page is scrolled or the window is resized.
 * "postcomment" is the outer container of the sections that floats.  "floatMarker"
 * is the point that indicates the topmost point that the floated menu should stop
 * floating.  This doesn't work so well in Safari.  The best way to do in Safari is 
 * to have 2 of these items, the other one being postcommentfloat.  This second one
 * is on the page is hidden unless you're in safari.
 * 
 * @author		Joycelyn Chung		joycelyn@orangetowers.com
 **/

var el;
var changeHeight;

function floatMenu() {
  el = dojo.byId('postcomment');
  var marker = dojo.byId('floatMarker');
  var markerParent = marker.parentNode;
  var mOffset = topaz.domUtil.getCurrentOffset(marker);
  var mpOffset = topaz.domUtil.getCurrentOffset(markerParent);
  var scrollOffset = dojo.html.getScroll().offset;
  var vpOffset = dojo.html.getViewport();

  var scrollY = scrollOffset.y;
  
  var y = 0;
  if (dojo.render.html.safari) {
    var floatDiv = dojo.byId("postcommentfloat");
    if (el.style.display == "none") {
      floatDiv.style.display = "none";
      el.style.display = "block";
    }
  }
  else {
    dojo.html.removeClass(el, 'fixed');
  }
  
  if (scrollY > mOffset.top) {
    y = scrollY - mpOffset.top;
    if (dojo.render.html.safari) {
      var floatDiv = dojo.byId("postcommentfloat");
      if (floatDiv.style.display = "none") {
        floatDiv.style.display = "block";
        el.style.display = "none";
      }
    }
    else {
      dojo.html.addClass(el, 'fixed');
    }
  }
  
  if (BrowserDetect.browser == "Explorer" && BrowserDetect.version < 7 && ((document.body.offsetHeight-scrollY) >= vpOffset.height)) {
    //el.style.top = y + "px";
    changeHeight = y;
    window.setTimeout("changeTopPosition()", 100); 
  }  
}

function changeTopPosition() {
  el.style.top = changeHeight + "px";
}




  