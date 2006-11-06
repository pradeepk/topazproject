dojo.provide("topaz.commentDisplay");

/**
  * topaz.commentDisplay
  *
  * @param
  *
  **/
topaz.commentDisplay = new Object();

topaz.commentDisplay = {
  target: "",
  
  retrieveMsg: dojo.byId('retrieveMsg'),
  
  parseAttributeToList: function(attr) {
    var attrList = new Array();
    attr = attr.split(' ');
    
    return attrList;
  },
  
  isMultiple: function(attr) {
    var attrList = this.parseAttributeToList(attr);
    
    return (attrList.length > 1) ? true : false;
  },
  
  setTarget: function(obj) {
    this.target = obj;
  },
  
  show: function(obj){
    this.setTarget(obj);
    
    this.getComment(this.target);
      
		popup.setMarker(this.target);
  	popup.show();  		
  },
  
  getComment: function (obj) {
    var targetDiv = dojo.widget.byId("CommentDialog");
    var targetUri = topaz.domUtil.getDisplayId(this.target);
    
    alert("targetURI = " + targetUri);
    
    var bindArgs = {
      url: djConfig.namespace + "/annotation/getAnnotation.action?annotationId=" + targetUri,
      method: "get",
      error: function(type, data, evt){
       //alert("ERROR:" + data.toSource());
       var err = document.createTextNode("ERROR:" + data.toSource());
       retrieveMsg.appendChild(err);
       return false;
      },
      load: function(type, data, evt){
        var docFragment = document.createDocumentFragment();
        docFragment = data;
        refreshArea.innerHTML = docFragment;
        
        //formUtil.textCues.on(commentTitle, titleCue);
        //formUtil.textCues.on(comments, commentCue);
        
        dlg.hide();
        
        return true;
      },
      mimetype: "text/html"
     };
     dojo.io.bind(bindArgs);
    
  }
  
  
}
