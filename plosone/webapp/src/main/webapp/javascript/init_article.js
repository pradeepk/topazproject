  var dlg;
  var popup;
  var annotationForm;
  
  function init(e) {
    var triggerNode = dojo.byId(annotationConfig.trigger);
  	dojo.event.connect(triggerNode, 'onmousedown', function(e) {
	     topaz.annotation.createAnnotationOnMouseDown();
	     e.preventDefault();
	   }
  	);
       
 		annotationForm = document.createAnnotation;
    initAnnotationForm();
    
    formUtil.toggleFieldsByClassname('commentPublic', 'commentPrivate');
    
  	dlg = dojo.widget.byId("AnnotationDialog");
  	dlg.setTipDown(dojo.byId(annotationConfig.tipDownDiv));
  	dlg.setTipUp(dojo.byId(annotationConfig.tipUpDiv));

  	popup = dojo.widget.byId("CommentDialog");
  	var popupClose = dojo.byId('btn_close');
    popup.setCloseControl(popupClose);
  	popup.setTipDown(dojo.byId(commentConfig.tipDownDiv));
  	popup.setTipUp(dojo.byId(commentConfig.tipUpDiv));
    topaz.displayComment.init();
    dojo.event.connect(popupClose, 'onclick', function(e) {
        topaz.displayComment.mouseoutComment(topaz.displayComment.target);
      }
    );
    dojo.event.connect(popupClose, 'onblur', function(e) {
        topaz.displayComment.mouseoutComment(topaz.displayComment.target);
      }
    );
    
    //JSFX_FloatDiv("divTopLeft", 0,0).floatIt();
    
  }
  
  dojo.addOnLoad(init);
