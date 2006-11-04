dojo.provide("topaz.commentDisplay");

/**
  * topaz.commentDisplay
  *
  * @param
  *
  **/
topaz.commentDisplay = new Object();

topaz.commentDisplay = {
  showComment: function(obj){
    this.getComment(obj);
    
    
		var bindThis = this;
		alert(bindThis);
		popup.setMarker(bindThis);
  	popup.show();  		
  },
  
  getComment: function (obj) {
    
  }
  
  
}
