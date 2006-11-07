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
      var displayId = obj.attributes['displayId'].nodeValue;
      //alert(obj.nodeName + ".displayId = " + displayId);
      return displayId;
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
  }
  
}