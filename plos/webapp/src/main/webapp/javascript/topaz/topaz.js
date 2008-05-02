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
 * This file is not a dojo module rather simply contains general utility methods.
 */
document.getElementsByTagAndClassName = function(tagName, className) {
  if ( tagName == null )
    tagName = '*';
   
  var children = document.getElementsByTagName(tagName);
  var elements = new Array();
  
  if ( className == null )
    return children;
  
  for (var i = 0; i < children.length; i++) {
    var child = children[i];
    var classNames = child.className.split(' ');
    for (var j = 0; j < classNames.length; j++) {
      if (classNames[j] == className) {
        elements.push(child);
        break;
      }
    }
  }

  return elements;
}

document.getElementsByTagAndAttributeName = function(tagName, attributeName) {
  if ( tagName == null )
    tagName = '*';
   
  var children = document.getElementsByTagName(tagName);
  var elements = new Array();
  
  if ( attributeName == null )
    return children;
  
  for (var i = 0; i < children.length; i++) {
    var child = children[i];
    if (child.getAttributeNode(attributeName) != null) {
      elements.push(child);
    }
  }

  return elements;
}

document.getElementsByAttributeValue = function(tagName, attributeName, attributeValue) {
  if ( tagName == null )
    tagName = '*';
  else if ( attributeName == null )
    return "[getElementsByAttributeValue] attributeName is required.";
  else if ( attributeValue == null )
    return "[getElementsByAttributeValue] attributeValue is required.";

  var elements = document.getElementsByTagAndAttributeName(tagName, attributeName);
  var elValue = new Array();
  
  for (var i = 0; i < elements.length; i++) {
    var element = elements[i];
    if (element.getAttributeNode(attributeName).nodeValue == attributeValue) {
      elValue.push(element);
    }
  }

  return elValue;
}


/**
 * Extending the String object
 *
 **/
String.prototype.trim = function() {
  return this.replace(/(?:(?:^|\n)\s+|\s+(?:$|\n))/g,"");
}

String.prototype.rtrim = function() {
  return this.replace(/\s+$/,"");
}

String.prototype.ltrim = function() {
  return this.replace(/^\s+/, "");
}

String.prototype.isEmpty = function() {
  return (this == null || this == "");
}

String.prototype.replaceStringArray = function(delimiter, strMatch, newStr) {
	if (!strMatch || !newStr) {
		return "Missing required value";
	}
	
	var strArr = (delimiter) ? this.split(delimiter) : this.split(" ");
	var matchIndexStart = -1;
	var matchIndexEnd = -1;
	for (var i=0; i<strArr.length; i++) {
		if (strArr[i].match(strMatch) != null) {
			if (matchIndexStart < 0)
				matchIndexStart = i;
			
			matchIndexEnd = i;
		}
	}
	
	if (matchIndexEnd >= 0) {
		var diff = matchIndexEnd - matchIndexStart + 1;
		strArr.splice(matchIndexStart, diff, newStr);
	}
	
	var newStr = strArr.join(" ");
	
	return newStr;
}

function toggleAnnotation(obj, userType) {
  _ldc.show();
  var bugs = document.getElementsByTagAndClassName('a', 'bug');
  
  for (var i=0; i<bugs.length; i++) {
    var classList = new Array();
    classList = bugs[i].className.split(' ');
    for (var n=0; n<classList.length; n++) {
      if (classList[n].match(userType))
        bugs[i].style.display = (bugs[i].style.display == "none") ? "inline" : "none";
    }
  }
  
  toggleExpand(obj, null, "Show notes", "Hide notes");
  
  _ldc.hide();
  
  return false;
}

function getAnnotationEl(annotationId) {
  var elements = document.getElementsByTagAndAttributeName('a', 'displayid');
     
  var targetEl
  for (var i=0; i<elements.length; i++) {
    var elDisplay = topaz.domUtil.getDisplayId(elements[i]);
    var displayList = elDisplay.split(',');

    for (var n=0; n<displayList.length; n++) {
      if (displayList[n] == annotationId) {
        targetEl = elements[i];
        return targetEl;
      }
    }
    
  }
  
  return false;
}

var elLocation;
function jumpToAnnotation(annotationId) {
  var targetEl = getAnnotationEl(annotationId);

  jumpToElement(targetEl);
}

function jumpToElement(elNode) {
  if (elNode) {
    elLocation = topaz.domUtil.getCurrentOffset(elNode);
    window.scrollTo(0, elLocation.top);
  }
}

function toggleExpand(obj, isOpen, textOn, textOff) {
  if (isOpen == false) {
    obj.className = obj.className.replace(/collapse/, "expand");
    if (textOn) dojox.data.dom.textContent(obj, textOn);
  }
  else if (obj.className.match('collapse')) {
    obj.className = obj.className.replace(/collapse/, "expand");
    if (textOn) dojox.data.dom.textContent(obj, textOn);
  }
  else {
    obj.className = obj.className.replace(/expand/, "collapse");
    if (textOff) dojox.data.dom.textContent(obj, textOff);
  }
  
}

var activeToggleId = "";
var activeWidget = "";

function setActiveToggle(widgetId, boxId) {
  activeToggleId = boxId;
  activeWidget = dojo.byId(widgetId);
}

function singleView(obj) {
  if (activeToggleId != "") {
    topaz.domUtil.swapDisplayMode(activeToggleId, "none");
    toggleExpand(activeWidget, false); 
  }
}

function singleExpand(obj, targetId) {
  if (targetId != activeToggleId) {
    singleView(obj);
  }
  setActiveToggle
   (obj.id, targetId);
  topaz.domUtil.swapDisplayMode(targetId);
  toggleExpand(obj); 
  
  return false;
}

/**
 * One stop shopping for handling dojo xhr errors.
 * This method is intended to be called from within dojo.xhr 'handle' or 'error' callback methods.
 */
function handleXhrError(response, ioArgs) {
  if(response instanceof Error){
    _ldc.hide();
    if(response.dojoType == "cancel"){
      //The request was canceled by some other JavaScript code.
      console.debug("Request canceled.");
    }else if(response.dojoType == "timeout"){
      //The request took over 5 seconds to complete.
      console.debug("Request timed out.");
    }else{
      //Some other error happened.
      console.error(response);
      if(djConfig.isDebug) alert(response.toSource());
    }
  }
}
