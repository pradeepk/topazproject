  var dlg;
  var popup;
  var annotationForm;
  
  function init(e) {
    var triggerNode = dojo.byId(annotationConfig.trigger);
  	dojo.event.connect(triggerNode, 'onmousedown', topaz.annotation, 'createAnnotationOnMouseDown');
       
 		annotationForm = document.createAnnotation;
    initAnnotationForm();
    
    formUtil.toggleFieldsByClassname('commentPrivate', 'commentPublic');
    
  	dlg = dojo.widget.byId("AnnotationDialog");

  	popup = dojo.widget.byId("CommentDialog");
  	var objList = ['bug1', 'bug2', 'bug3'];
  	for (var i=0; i<objList.length; i++) {
  		var bugObj = dojo.byId(objList[i]);
  		
  		topaz.commentDisplay.setTarget(bugObj);
  		
	  	dojo.event.connect(objList[i], 'onclick', function() {
	  	  alert(objList[i]);
	  		topaz.commentDisplay.showComment(bugObj);
	  	});
  	}

  }
  
  dojo.addOnLoad(init);
