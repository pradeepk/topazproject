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
  * topaz.commentDisplay
  *
  * @param
  *
  **/
topaz.domUtil = new Object();

topaz.domUtil = {
  getDisplayId: function(obj) {
    if (obj.getAttributeNode('displayid')) {
      var displayId = obj.getAttributeNode('displayid').nodeValue;
      return displayId;
    }
/*    else if (obj.attributes['displayId'] != null) {
      var displayId = obj.attributes['displayid'].nodeValue;
      //alert(obj.nodeName + ".displayId = " + displayId);
      return displayId;
    }*/
    else {
      return null;
    }
  },
  
  getAnnotationId: function(obj) {
    if (obj.getAttributeNode('annotationid') != null) {
      var annotationId = obj.getAttributeNode('annotationid').nodeValue;
      return annotationId;
    }
/*    else if (obj.attributes['annotationid'] != null) {
      var annotationId = obj.attributes['annotationid'].nodeValue;
      //alert(obj.nodeName + ".displayId = " + displayId);
      return annotationId;
    }*/
    else {
      return null;
    }
  },
  
  ISOtoJSDate: function (ISO_DT) {
     var temp = ISO_DT.split(/^(....).(..).(..).(..).(..).(..).*$/);
  
     var newDate = new Date();
     newDate.setUTCFullYear(temp[1], temp[2]-1, temp[3]);
     newDate.setUTCHours(temp[4]);
     newDate.setUTCMinutes(temp[5]);
     newDate.setUTCSeconds(temp[6]);
  
    //alert (newDate);
    return newDate;
  },
  
  removeChildNodes: function(obj) {
    if (obj.hasChildNodes()) {
      //alert("obj has child nodes");
      for (var i=0; i<obj.childNodes.length; i++) {
        alert(childNodes[i].hasChildNodes);
        if (obj.removeChild) {
          obj.removeChild(childNodes[i]);
        }
        else {
          obj.childNodes[i].removeNode(true);
        }
      }
    }
  },

  getDisplayMap: function(obj, displayId) {
    var displayIdList = (displayId != null) ? [displayId] : this.getDisplayId(obj).split(',');
    
    //alert("displayId = " + displayId + "\n" +
    //      "displayIdList = " + displayIdList);
    
    var annoteEl = document.getElementsByTagAndAttributeName('span', 'annotationid');
    var elDisplayList = new Array();
    
    // Based on the list of displayId from the element object, figure out which element 
    // has an annotationId list in which there's an annotation id that matches the 
    // display id.
    for (var i=0; i<displayIdList.length; i++) {
      var elAttrList = new Array();
      for (var n=0; n<annoteEl.length; n++) {
        var attrList = this.getAnnotationId(annoteEl[n]).split(',');
        
        for (var x=0; x<attrList.length; x++) {
          if(attrList[x] == displayIdList[i]) {
            elAttrList.push(annoteEl[n]);
          }
        }
      }
      
      elDisplayList.push({'displayId': displayIdList[i],
                          'elementList': elAttrList,
                          'elementCount': elAttrList.length});
    }
    
    return elDisplayList;
  },
  
  addNewClass: function (sourceClass, newClass, el) {
    var elObj = (el) ? el : null;
    var elList = document.getElementsByTagAndClassName(elObj, sourceClass);
    
    for (var i=0; i<elList.length; i++) {
       dojo.html.addClass(elList[i], newClass);
    }
  },
  
  removeNewClass: function (sourceClass, newClass, el) {
    var elObj = (el) ? el : null;
    var elList = document.getElementsByTagAndClassName(elObj, sourceClass);
    
    for (var i=0; i<elList.length; i++) {
      dojo.html.removeClass(elList[i], newClass);
    }
  },
  
  swapClassNameBtwnSibling: function (obj, tagName, classNameValue) {
    var parentalNode = obj.parentNode;
    var siblings = parentalNode.getElementsByTagName(tagName);
    
    for (var i=0; i<siblings.length; i++) {
      if (siblings[i].className.match(classNameValue)){
        dojo.html.removeClass(siblings[i], classNameValue);   
      }
    }
    
    dojo.html.addClass(obj, classNameValue);
  },

  swapAttributeByClassNameForDisplay: function (obj, triggerClass, displayId) {
    var elements = this.getDisplayMap(obj, displayId);
    
    //alert("attrValue = " + attrValue + "\n" +
    //      "elements = " + elements.toSource());
    
    for (var i=0; i<elements.elementCount; i++) {
      if (elements.elementList[i].className.match(triggerClass)) {
        var strRegExp = new RegExp(triggerClass);
        elements.elementList[i].className = elements.elementList[i].className.replace(strRegExp, "");
      }
    }
    
    dojo.html.addClass(obj, triggerClass);
    
    //alert("elements.displayId = " + elements.displayId + "\n" +
    //      "obj = " + obj.nodeName + "\n" +
    //      "obj.className = " + obj.className);
  },
  
  getCurrentOffset: function(obj) {
		var curleft = curtop = 0;
		if (obj.offsetParent) {
			curleft = obj.offsetLeft
			curtop = obj.offsetTop
			while (obj = obj.offsetParent) {
				curleft += obj.offsetLeft  
				curtop += obj.offsetTop
			}
		}

    var offset = new Object();
    offset.top = curtop;
    offset.left = curleft;
    
    return offset;
  },

  getFirstAncestorByClass: function ( selfNode, ancestorClassName ) {
    var parentalNode = selfNode;
    
    while ( parentalNode.className.search(ancestorClassName) < 0) {
      parentalNode = parentalNode.parentNode;
    }
    
    return parentalNode;
  },
  
  swapDisplayMode: function(objId, state) {
    var obj = dojo.byId(objId);
    
    if (state) 
      obj.style.display = state;
    else if(obj.style.display != "block")
      obj.style.display = "block";
    else
      obj.style.display = "none";
      
    return false;
  },
  
  removeExtraSpaces: function(text) {
    //alert("text = '" + text + "'");
    return text.replace(/([\r\n]+\s+)/g," ");
  },
  
  getChildElementsByTagAndClassName: function(node, tagName, className) {
    var children = node.childNodes;
    var elements = new Array();
    tagName = tagName.toUpperCase();
    
    //alert("node = " + node + "\ntagName = " + tagName + "\nclassName = " + className);
    
    if ( className != null || tagName != null) {
      //alert("children = " + children.length);

      for (var i = 0; i < children.length; i++) {
        var child = children[i];
        
        if (tagName != null) {
        
          //alert("child.nodeName.match(tagName) = " + child.nodeName.match(tagName));
          
          if (child.nodeName.match(tagName)) {
            if (className != null) {
              var classNames = child.className.split(' ');
              for (var j = 0; j < classNames.length; j++) {
                if (classNames[j] == className) {
                  elements.push(child);
                  break;
                }
              }
            }
            else {
              elements.push(child);
            }
          }
        }
        else if (className != null){
          var classNames = child.className.split(' ');
          for (var j = 0; j < classNames.length; j++) {
            if (classNames[j] == className) {
              elements.push(child);
              break;
            }
          }
        }
      }
    }
    else {
      return children;
    }    

    //alert("elements = " + elements);
    return elements;
  },

  adjustContainerHeight: function (obj) {
    // get size viewport
    var viewportSize = dojo.html.getViewport();
    
    // get the offset of the container
		var objOffset = topaz.domUtil.getCurrentOffset(obj);
		
		// find the size of the container
		var objMb = dojo.html.getMarginBox(obj);

    var maxContainerHeight = viewportSize.height - (10 * objOffset.top);
    //alert("objOffset.top = " + objOffset.top + "\nviewportSize.height = " + viewportSize.height + "\nmaxContainerHeight = " + maxContainerHeight);
    
    obj.style.height = maxContainerHeight + "px";
    obj.style.overflow = "auto";
  },
  
  setContainerWidth: function (obj, minWidth, maxWidth, variableWidth /* if the container between min and max */) {
    var viewport = dojo.html.getViewport();
    
    // min-width: 675px; max-width: 910px;
    obj.style.width = (minWidth && viewport.width < minWidth) ? minWidth + "px" : 
                      (maxWidth && viewport.width > maxWidth) ? maxWidth + "px" :
                      (!variableWidth && viewport.width < maxWidth) ? maxWidth + "px" : "auto" ;
    //alert("container.style.width = " + obj.style.width);
  },
  
  removeNode: function(node, /* boolean */ deep) {
    if (deep && node.hasChildNodes)
      dojo.dom.removeChildren(node);
      
    dojo.dom.removeNode(node);
  },
  
  insertAfterLast: function(srcNode, refNode) {
    if (refNode.hasChildNodes) 
      dojo.dom.insertAfter(srcNode, refNode[refNode.childNodes.length-1]);
    else
      refNode.appendChild(srcNode);
  },

  insertBeforeFirst: function(srcNode, refNode) {
    if (refNode.hasChildNodes) 
      dojo.dom.insertBefore(srcNode, refNode[0]);
    else
      refNode.appendChild(srcNode);
  }
}