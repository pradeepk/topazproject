  var dcr = new Object();
  var responseForm;
  var ldc;
  
  function init(e) {
    ldc = dojo.widget.byId("LoadingCycle");

    dcr.widget = dojo.byId("DiscussionPanel");
    dcr.btnCancel = dojo.byId("btnCancelResponse");
    dcr.btnSubmit = dojo.byId("btnPostResponse");
    dcr.form = document.discussionResponse;
    dcr.formAction = "/annotation/secure/createAnnotationSubmit.action";
    dcr.responseTitleCue = "Enter your comment title...";
    dcr.responseCue = "Enter your comment...";
    dcr.error = dojo.byId('responseSubmitMsg');
    dcr.requestType = "new";
    dcr.baseId = dcr.form.target.value;
    dcr.replyId = dcr.form.target.value;
    var responseTitle = dcr.form.responseTitle;
    var responseArea = dcr.form.responseArea;
    
    dojo.event.connect(dcr.btnSubmit, "onclick", function(e) {
        topaz.responsePanel.submit(dcr);
      }
    );    
    
    dojo.event.connect(responseTitle, "onfocus", function(e) {
        topaz.formUtil.textCues.off(responseTitle, dcr.responseTitleCue);
      }
    );    
       
    dojo.event.connect(responseArea, "onfocus", function(e) {
        topaz.formUtil.textCues.off(responseArea, dcr.responseCue);
      }
    );    
       
    dojo.event.connect(responseTitle, "onblur", function(e) {
        if(responseTitle.value != "" && responseTitle.value != dcr.responseCue) {
          var fldResponseTitle = dcr.form.commentTitle;
          fldResponseTitle.value = responseTitle.value;
        }
        topaz.formUtil.textCues.on(responseTitle, dcr.responseTitleCue);
      }
    );    

    dojo.event.connect(responseArea, "onblur", function(e) {
        if(responseArea.value != "" && responseArea.value != dcr.responseCue) {
          var fldResponse = dcr.form.comment;
          fldResponse.value = responseArea.value;
        }
        topaz.formUtil.textCues.on(responseArea, dcr.responseCue);
      }
    );    
       
    dojo.event.connect(responseTitle, "onchange", function(e) {
        if(responseTitle.value != "" && responseTitle.value != dcr.responseCue) {
          var fldResponseTitle = dcr.form.commentTitle;
          fldResponseTitle.value = responseTitle.value;
        }
      }
    );    
       
    dojo.event.connect(responseArea, "onchange", function(e) {
        if(responseArea.value != "" && responseArea.value != dcr.responseCue) {
          var fldResponse = dcr.form.comment;
          fldResponse.value = responseArea.value;
        }
      }
    );    
    
  }
  
  dojo.addOnLoad(init);
