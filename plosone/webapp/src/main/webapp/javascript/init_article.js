  var dlg;
  var popup;
  var annotationForm;
  
  function init(e) {
    var triggerNode = dojo.byId(annotationConfig.trigger);
  	dojo.event.connect(triggerNode, 'onmousedown', topaz.annotation, 'createAnnotationOnMouseDown');
       
 		annotationForm = document.createAnnotation;
    initAnnotationForm();
    
    formUtil.toggleFieldsByClassname('commentPublic', 'commentPrivate');
    
  	dlg = dojo.widget.byId("AnnotationDialog");

  	popup = dojo.widget.byId("CommentDialog");

    topaz.displayComment.init();
  }
  
  dojo.addOnLoad(init);
