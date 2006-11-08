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
  	var popupClose = dojo.byId('btn_close');
    popup.setCloseControl(popupClose);
    topaz.displayComment.init();
    dojo.event.connect(popupClose, 'onclick', function() {
        topaz.displayComment.mouseoutComment(topaz.displayComment.target);
      }
    );
    dojo.event.connect(popupClose, 'onblur', function() {
        topaz.displayComment.mouseoutComment(topaz.displayComment.target);
      }
    );
    
  }
  
  dojo.addOnLoad(init);
