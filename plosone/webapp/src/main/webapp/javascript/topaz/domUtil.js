dojo.provide("topaz.domUtil");

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
          if(attrList[x].match(displayIdList[i])) {
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
      elList[i].className.concat(newClass);
    }
  },
  
  removeNewClass: function (sourceClass, newClass, el) {
    var elObj = (el) ? el : null;
    var elList = document.getElementsByTagAndClassName(elObj, sourceClass);
    
    for (var i=0; i<elList.length; i++) {
      var strRegExp = new RegExp(newClass);
      elList[i].className = elList[i].className.replace(strRegExp, "");
    }
  },
  
  swapClassNameBtwnSibling: function (obj, tagName, classNameValue) {
    var parentalNode = obj.parentNode;
    var siblings = parentalNode.getElementsByTagName(tagName);
    
    for (var i=0; i<siblings.length; i++) {
      if (siblings[i].className.match(classNameValue)){
        var strRegExp = new RegExp(classNameValue);
        siblings[i].className = siblings[i].className.replace(strRegExp, "");   
      }
    }
    
    obj.className = (!obj.className) ? classNameValue : obj.className.concat(" " + classNameValue);
  },

  swapAttributeByClassNameForDisplay: function (obj, triggerClass, attrName, attrValue) {
    var elements = this.getDisplayMap(obj, attrValue);
    
    //alert("attrValue = " + attrValue + "\n" +
    //      "elements = " + elements.toSource());
    
    for (var i=0; i<elements.elementCount; i++) {
      if (elements.elementList[i].className.match(triggerClass)) {
        var strRegExp = new RegExp(triggerClass);
        elements.elementList[i].className = elements.elementList[i].className.replace(strRegExp, "");
      }
    }
    
    obj.className = (!obj.className) ? triggerClass : obj.className.concat(" " + triggerClass);
    obj.parentNode.className = (!obj.parentNode.className) ? triggerClass : obj.parentNode.className.concat(" " + triggerClass);
    
    //alert("elements.displayId = " + elements.displayId + "\n" +
    //      "obj = " + obj.nodeName + "\n" +
    //      "obj.className = " + obj.className);
  }
  
}