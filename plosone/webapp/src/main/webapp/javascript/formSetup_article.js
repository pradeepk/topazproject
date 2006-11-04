var commentTitle;
var comments;
var titleCue     = 'Enter your annotation title...';
var commentCue   = 'Enter your annotation...';
var btn_save;
var btn_post;
var submitMsg    = document.getElementById('submitMsg');

function initAnnotationForm() {	
	commentTitle     = document.getElementById("commentTitle");
	comments         = document.getElementById("comment");
	var privateFlag  = document.getElementById("privateFlag");
	var publicFlag   = document.getElementById("publicFlag");
	var btn_cancel    = document.getElementById("btn_cancel");
  btn_save         = document.getElementById("btn_save");
  btn_post         = document.getElementById("btn_post");
	
	// Annotation Dialog Box: Title field
	dojo.event.connect(commentTitle, "onfocus", function () { 
	  formUtil.textCues.off(commentTitle, titleCue); 
	});
	
	dojo.event.connect(commentTitle, "onblur", function () { 
	  formUtil.textCues.on(commentTitle, titleCue); 
	});
	
	// Annotation Dialog Box: Comment field
	dojo.event.connect(comments, "onfocus", function () {
	  formUtil.textCues.off(comments, commentCue);
	});
	
	dojo.event.connect(comments, "onblur", function () {
	  formUtil.textCues.on(comments, commentCue); 
	  formUtil.checkFieldStrLength(comments);
	});
	
	// Annotation Dialog Box: Private/Public radio buttons
	dojo.event.connect(privateFlag, "onclick", function() {
	  formUtil.toggleFieldsByClassname('commentPrivate', 'commentPublic'); 
	  dojo.widget.byId("AnnotationDialog").placeModalDialog();
  	//var btn = btn_save;
  	//dlg.setCloseControl(btn);
  });
  
	dojo.event.connect(publicFlag, "onclick", function() {
	  formUtil.toggleFieldsByClassname('commentPublic', 'commentPrivate'); 
	  dojo.widget.byId("AnnotationDialog").placeModalDialog();
  	//var btn = btn_post;
  	//dlg.setCloseControl(btn);
	});
	
	// Annotation Dialog Box: Save button
	dojo.event.connect(btn_save, "onclick", function() {
    validateNewComment();
  });
  
	// Annotation Dialog Box: Post buttons
	dojo.event.connect(btn_post, "onclick", function() {
	  validateNewComment();
  });

	dojo.event.connect(btn_cancel, "onclick", function() {
	  getArticle();
  });

}

function validateNewComment() {
  formUtil.textCues.off(commentTitle, titleCue);
  formUtil.textCues.off(comments, commentCue);
  var str = formUtil.checkFieldStrLength(comments);
  
  if (str < 0) {
     var bindArgs = {
      url: djConfig.namespace + "/annotation/secure/createAnnotationSubmit.action",
      method: "post",
      error: function(type, data, evt){
       //alert("An error occurred." + data.toSource());
       var err = document.createTextNode("ERROR:" + data.toSource());
       submitMsg.appendChild(err);
       return false;
      },
      load: function(type, data, evt){
       var jsonObj = dojo.json.evalJson(data);
       
       if (jsonObj.actionErrors.list.length > 0) {
         var errorMsg;
         
         for (var i=0; i<jsonObj.actionErrors.list.length; i++) {
           errorMsg = errorMsg + jsonObj.actionErrors.list[i] + "\n";
         }
         
         //alert("ERROR: " + errorMsg);
         var err = document.createTextNode("ERROR:" + errorMsg);
         submitMsg.appendChild(err);
         
         return false;
       }
       else if (jsonObj.numFieldErrors > 0) {
         var fieldErrors;
         
         for (var item in jsonObj.fieldErrors.map) {
           fieldErrors = fieldErrors + item + ": " + jsonObj.fieldErrors.map[item] + "\n";
         }
         
         //alert("ERROR: " + fieldErrors);
         var err = document.createTextNode("ERROR:" + fieldErrors);
         submitMsg.appendChild(err);

         return false;
       }
       else {
         getArticle();
         return true;
       }
       
      },
      mimetype: "text/plain",
      formNode: document.getElementById("createAnnotation")
     };
     dojo.io.bind(bindArgs);
  }
  else {
    return false;
  }
}  

function getArticle() {
  var refreshArea = document.getElementById(djConfig.articleContainer);
  var targetUri = annotationForm.target.value;
  
  var bindArgs = {
    url: djConfig.namespace + "/article/fetchBody.action?articleURI=" + targetUri,
    method: "get",
    error: function(type, data, evt){
     //alert("ERROR:" + data.toSource());
     var err = document.createTextNode("ERROR:" + data.toSource());
     submitMsg.appendChild(err);
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











