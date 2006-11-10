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
    if (obj.attributes['displayId'] != null) {
      var displayId = obj.attributes['displayid'].nodeValue;
      //alert(obj.nodeName + ".displayId = " + displayId);
      return displayId;
    }
    else {
      return null;
    }
  },
  
  getAnnotationId: function(obj) {
    if (obj.attributes['annotationId'] != null) {
      var annotationId = obj.attributes['annotationid'].nodeValue;
      //alert(obj.nodeName + ".displayId = " + displayId);
      return annotationId;
    }
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
      alert("obj has child nodes");
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
  }
}