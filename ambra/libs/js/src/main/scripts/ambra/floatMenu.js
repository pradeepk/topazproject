/*
 * $HeadURL:: http://gandalf.topazproject.org/svn/head/plos/webapp/src/main/webapp/javas#$
 * $Id: floatMenu.js 5581 2008-05-02 23:01:11Z jkirton $
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
 * ambra.floatMenu
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

dojo.provide("ambra.floatMenu");
dojo.require("ambra.domUtil");
ambra.floatMenu = {
  el:null,
  changeHeight:null,
  
  doFloat: function() {
    this.el = dojo.byId('postcomment');
	  var marker = dojo.byId('floatMarker');
    if(!marker) return;
	  var markerParent = marker.parentNode;
	  var mOffset = ambra.domUtil.getCurrentOffset(marker);
	  var mpOffset = ambra.domUtil.getCurrentOffset(markerParent);
	  var scrollOffset = dojo._docScroll();
	  var vpOffset = dijit.getViewport();
	
	  var scrollY = scrollOffset.y;
	  
	  var y = 0;
	  if (dojo.isSafari) {
	    var floatDiv = dojo.byId("postcommentfloat");
	    if (this.el.style.display == "none") {
	      floatDiv.style.display = "none";
	      this.el.style.display = "block";
	    }
	  }
	  else {
	    dojo.removeClass(this.el, 'fixed');
	  }
	  
	  if (scrollY > mOffset.top) {
	    y = scrollY - mpOffset.top;
	    if (dojo.isSafari) {
	      var floatDiv = dojo.byId("postcommentfloat");
	      if (floatDiv.style.display = "none") {
	        floatDiv.style.display = "block";
	        this.el.style.display = "none";
	      }
	    }
	    else {
	      dojo.addClass(this.el, 'fixed');
	    }
	  }
	  
	  if (dojo.isIE && dojo.isIE < 7 && ((document.body.offsetHeight-scrollY) >= vpOffset.h)) {
	    this.changeHeight = y;
	    window.setTimeout("ambra.floatMenu.el.style.top = ambra.floatMenu.changeHeight + \"px\";", 100); 
	  }
	}
}

dojo.addOnLoad(function() {
  dojo.connect(window, "onscroll", function() {
     ambra.floatMenu.doFloat();
  });
  
  dojo.connect(window, "onresize", function() {
    ambra.floatMenu.doFloat();
  });

  dojo.connect(dojo.doc, "onscroll", function() {
      ambra.floatMenu.doFloat();
  });
  
  dojo.connect(dojo.doc, "onkey", function() {
    ambra.floatMenu.doFloat();
  });
  
  ambra.floatMenu.doFloat();
});