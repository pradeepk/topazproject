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
    alert(obj.nodeName + ".displayId = " + obj.displayId);
    return obj.displayId;
  }

  
}