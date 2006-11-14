  var dlg;
  var popup;
  var popupm;
  var ldc;
  var annotationForm;
  
  function init(e) {
    ldc = dojo.widget.byId("LoadingCycle");
    ldc.show();
        
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
    dojo.event.connect(popupClose, 'onclick', function(e) {
        topaz.displayComment.mouseoutComment(topaz.displayComment.target);
      }
    );
    dojo.event.connect(popupClose, 'onblur', function(e) {
        topaz.displayComment.mouseoutComment(topaz.displayComment.target);
      }
    );
  	
  	popupm = dojo.widget.byId("CommentDialogMultiple");
  	var popupCloseMulti = dojo.byId('btn_close_multi');
    popupm.setCloseControl(popupCloseMulti);
  	popupm.setTipDown(dojo.byId(multiCommentConfig.tipDownDiv));
  	popupm.setTipUp(dojo.byId(multiCommentConfig.tipUpDiv));
    dojo.event.connect(popupCloseMulti, 'onclick', function(e) {
        topaz.displayComment.mouseoutComment(topaz.displayComment.target);
      }
    );
    dojo.event.connect(popupCloseMulti, 'onblur', function(e) {
        topaz.displayComment.mouseoutComment(topaz.displayComment.target);
      }
    );
    
    topaz.displayComment.init();
    topaz.displayComment.processBugCount();
    
    ldc.hide();
    
    //JSFX_FloatDiv("divTopLeft", 0,0).floatIt();
    
  }
  
  dojo.addOnLoad(init);
