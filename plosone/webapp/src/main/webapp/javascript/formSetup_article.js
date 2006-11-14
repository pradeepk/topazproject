var commentTitle;
var comments;
var titleCue     = 'Enter your annotation title...';
var commentCue   = 'Enter your annotation...';
var btn_save;
var btn_post;

function initAnnotationForm() {	
	commentTitle     = annotationForm.cTitle;
	comments         = annotationForm.cArea;
	var privateFlag  = annotationForm.privateFlag;
	var publicFlag   = annotationForm.publicFlag;
	btn_save         = document.getElementById("btn_save");
	btn_post         = document.getElementById("btn_post");
	var btn_cancel   = document.getElementById("btn_cancel");
	
	// Annotation Dialog Box: Title field
	dojo.event.connect(commentTitle, "onfocus", function () { 
	  formUtil.textCues.off(commentTitle, titleCue); 
	});
	
	dojo.event.connect(commentTitle, "onchange", function () {
    if(annotationForm.cTitle.value != "" && annotationForm.cTitle.value != titleCue) {
      var fldTitle = annotationForm.commentTitle;
      fldTitle.value = annotationForm.cTitle.value;
    }
	});

	dojo.event.connect(commentTitle, "onblur", function () { 
    if(annotationForm.cTitle.value != "" && annotationForm.cTitle.value != titleCue) {
      var fldTitle = annotationForm.commentTitle;
      fldTitle.value = annotationForm.cTitle.value;
    }

	  formUtil.textCues.on(commentTitle, titleCue); 
	});
	
	// Annotation Dialog Box: Comment field
	dojo.event.connect(comments, "onfocus", function () {
	  formUtil.textCues.off(comments, commentCue);
	});

	dojo.event.connect(comments, "onchange", function () {
    if(annotationForm.cArea.value != "" && annotationForm.cArea.value != commentCue) {
      var fldTitle = annotationForm.comment;
      fldTitle.value = annotationForm.cArea.value;
    }
	});
	
	dojo.event.connect(comments, "onblur", function () {
    if(annotationForm.cArea.value != "" && annotationForm.cArea.value != commentCue) {
      var fldTitle = annotationForm.comment;
      fldTitle.value = annotationForm.cArea.value;
    }

	  formUtil.textCues.on(comments, commentCue); 
	  //formUtil.checkFieldStrLength(comments);
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
	dojo.event.connect(btn_save, "onclick", function(e) {
	  //getCommentValues();
    validateNewComment();
    e.preventDefault();
  });
  
	// Annotation Dialog Box: Post buttons
	dojo.event.connect(btn_post, "onclick", function(e) {
	  //getCommentValues();
    validateNewComment();
    e.preventDefault();
  });

	dojo.event.connect(btn_cancel, "onclick", function(e) {
  	var btn = btn_cancel;
  	dlg.setCloseControl(btn);
	  getArticle();
    topaz.displayComment.processBugCount();
    e.preventDefault();
  });

}

function validateNewComment() {
  //formUtil.textCues.off(commentTitle, titleCue);
  //formUtil.textCues.off(comments, commentCue);
  var str = formUtil.checkFieldStrLength(comments);
  var submitMsg = dojo.byId('submitMsg');
  dojo.dom.removeChildren(submitMsg);
  
  formUtil.disableFormFields(annotationForm);
  //topaz.domUtil.addNewClass('post', ' disable');
  
  ldc.show();

  if (str < 0) {
     var bindArgs = {
      url: namespace + "/annotation/secure/createAnnotationSubmit.action",
      method: "post",
      error: function(type, data, evt){
       //alert("An error occurred." + data.toSource());
       var err = document.createTextNode("ERROR [AJAX]:" + data.toSource());
       submitMsg.appendChild(err);

       formUtil.enableFormFields(annotationForm);
       //topaz.domUtil.removeNewClass('post', '\sdisable', 'div');
       ldc.hide();
       
       return false;
      },
      load: function(type, data, evt){
       var jsonObj = dojo.json.evalJson(data);
       
       //alert("jsonObj:\n" + jsonObj.toSource());
       //submitMsg.appendChild(document.createTextNode(jsonObj.toSource()));
       
       if (jsonObj.actionErrors.list.length > 0) {
         var errorMsg;
         
         for (var i=0; i<jsonObj.actionErrors.list.length; i++) {
           errorMsg = errorMsg + jsonObj.actionErrors.list[i] + "\n";
         }
         
         //alert("ERROR: " + errorMsg);
         var err = document.createTextNode("ERROR [Action]:" + errorMsg);
         submitMsg.appendChild(err);
         formUtil.enableFormFields(annotationForm);
         //topaz.domUtil.removeNewClass('post', '\sdisable', 'div');
         
         ldc.hide();

         return false;
       }
       else if (jsonObj.numFieldErrors > 0) {
         var fieldErrors;
         
         for (var item in jsonObj.fieldErrors.map) {
           fieldErrors = fieldErrors + item + ": " + jsonObj.fieldErrors.map[item] + "\n";
         }
         
         //alert("ERROR: " + fieldErrors);
         var err = document.createTextNode("ERROR [Field]:" + fieldErrors);
         submitMsg.appendChild(err);
         formUtil.enableFormFields(annotationForm);
         //topaz.domUtil.removeNewClass('post', '\sdisable', 'div');

         ldc.hide();

         return false;
       }
       else {
         getArticle();
         dlg.hide();

         formUtil.enableFormFields(annotationForm);
         //topaz.domUtil.removeNewClass('post', '\sdisable', 'div');
         return false;
       }
       
      },
      mimetype: "text/plain",
      formNode: annotationForm
     };
     dojo.io.bind(bindArgs);
  }
  else {
    return false;
  }
}  

function getArticle() {
  var refreshArea = dojo.byId(djConfig.articleContainer);
  var targetUri = annotationForm.target.value;

  ldc.show();
  
  var bindArgs = {
    url: namespace + "/article/fetchBody.action?articleURI=" + targetUri,
    method: "get",
    error: function(type, data, evt){
     alert("ERROR:" + data.toSource());
     var err = document.createTextNode("ERROR:" + data.toSource());
     //submitMsg.appendChild(err);
     return false;
    },
    load: function(type, data, evt){
      var docFragment = document.createDocumentFragment();
      docFragment = data;

      ldc.hide();

      refreshArea.innerHTML = docFragment;
      //dojo.dom.removeChildren(refreshArea);
      //refreshArea.appendChild(docFragment);
      
      formUtil.textCues.reset(commentTitle, titleCue);
      formUtil.textCues.reset(comments, commentCue);
      
      topaz.displayComment.processBugCount();
      
      return false;
    },
    mimetype: "text/html"
   };
   dojo.io.bind(bindArgs);
  
}











