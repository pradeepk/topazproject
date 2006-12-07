  var dcr = new Object();
  var dcf = new Object();
  var responseForm;
  var flagForm;
  var ldc;
  
  function init(e) {
    ldc = dojo.widget.byId("LoadingCycle");

    dcr.widget = dojo.byId("DiscussionPanel");
    dcr.widget.style.display = "none";
    dcr.btnCancel = dojo.byId("btnCancelResponse");
    dcr.btnSubmit = dojo.byId("btnPostResponse");
    dcr.form = document.discussionResponse;
    dcr.formAction = "/annotation/secure/createReplySubmit.action";
    dcr.responseTitleCue = "Enter your response title...";
    dcr.responseCue = "Enter your response...";
    dcr.error = dojo.byId('responseSubmitMsg');
    dcr.requestType = "response";
    var responseTitle = dcr.form.responseTitle;
    var responseArea = dcr.form.responseArea;
    
    dojo.event.connect(dcr.btnCancel, "onclick", function(e) {
        topaz.responsePanel.hide(dcr.widget);
      }
    );    

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

    dcf.widget = dojo.byId("FlaggingPanel");
    dcf.widget.style.display = "none";
    dcf.btnCancel = dojo.byId("btnCloseFlag");
    dcf.btnSubmit = dojo.byId("btnSubmit");
    dcf.btnFlagClose = dojo.byId("btnFlagConfirmClose");
    dcf.form = document.discussionFlag;
    dcf.formAction = new Array("/annotation/secure/createAnnotationFlagSubmit.action", "/annotation/secure/createReplyFlagSubmit.action");
    dcf.responseCue = "Add any additional information here...";
    dcf.error = dojo.byId('flagSubmitMsg');
    dcf.requestType = "flag";
    var responseAreaFlag = dcf.form.responseArea;
    
    dojo.event.connect(dcf.btnCancel, "onclick", function(e) {
        topaz.responsePanel.hide();
      }
    );    

    dojo.event.connect(dcf.btnFlagClose, "onclick", function(e) {
        topaz.responsePanel.hide();
        topaz.responsePanel.resetFlaggingForm(dcf);
      }
    );    

    dojo.event.connect(dcf.btnSubmit, "onclick", function(e) {
        topaz.responsePanel.submit(dcf);
      }
    );    
       
    dojo.event.connect(responseAreaFlag, "onfocus", function(e) {
        topaz.formUtil.textCues.off(responseAreaFlag, dcf.responseCue);
      }
    );    
       
    dojo.event.connect(responseAreaFlag, "onblur", function(e) {
        if(responseAreaFlag.value != "" && responseAreaFlag.value != dcf.responseCue) {
          var fldResponse = dcf.form.comment;
          fldResponse.value = responseAreaFlag.value;
        }
        topaz.formUtil.textCues.on(responseAreaFlag, dcf.responseCue);
      }
    );    
       
    dojo.event.connect(responseAreaFlag, "onchange", function(e) {
        if(responseAreaFlag.value != "" && responseAreaFlag.value != dcf.responseCue) {
          var fldResponse = dcf.form.comment;
          fldResponse.value = responseAreaFlag.value;
        }
      }
    );    
    
  }
  
  dojo.addOnLoad(init);
