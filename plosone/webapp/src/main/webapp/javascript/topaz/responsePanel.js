var togglePanel = new Object();

topaz.responsePanel = new Object();

topaz.responsePanel = {
  upperContainer: "",
  
  newPanel: "",
  
  targetForm: "",
  
  setPanel: function(panel) {
    this.newPanel = panel;
  },
  
  setForm: function(formObj) {
    this.targetForm = formObj;
  },
  
  show: function(curNode, targetObj, targetElClassName, baseId, replyId, threadTitle) {
    this.setPanel(targetObj.widget);
    this.setForm(targetObj.form);
    targetObj.baseId = (baseId) ? baseId : "";
    targetObj.replyId = (replyId)? replyId : "";
    this.upperContainer = topaz.domUtil.getFirstAncestorByClass(curNode, targetElClassName);
    this.upperContainer.style.display = "none";
    togglePanel.newPanel = this.newPanel;
    togglePanel.upperContainer = this.upperContainer;
    
    dojo.dom.insertAfter(this.newPanel, this.upperContainer, false);
    this.newPanel.style.display = "block";
    
    if (threadTitle)
      this.targetForm.responseTitle.value = 'RE: ' + threadTitle;
  },
  
  hide: function() {
    togglePanel.newPanel.style.display = "none";
    togglePanel.upperContainer.style.display = "block";
  },
  
  submit: function(targetObj) {
    submitResponseInfo(targetObj);
  }
}  

function submitResponseInfo(targetObj) {
  var submitMsg = targetObj.error;
  var targetForm = targetObj.form;
  dojo.dom.removeChildren(submitMsg);
  //topaz.formUtil.disableFormFields(targetForm);

  var urlParam;
  if (targetObj.isFlag){
    urlParam = "target=" + targetObj.baseId;
  }
  else { 
    urlParam = "root=" + targetObj.baseId + "&inReplyTo=" + targetObj.replyId;
    topaz.formUtil.disableFormFields(targetForm);
  }
   
  ldc.show();

   var bindArgs = {
    url: namespace + targetObj.formAction + "?" + urlParam,
    method: "post",
    error: function(type, data, evt){
     alert("An error occurred." + data.toSource());
     var err = document.createTextNode("ERROR [AJAX]:" + data.toSource());
     //topaz.errorConsole.writeToConsole(err);
     //topaz.errorConsole.show();
     topaz.formUtil.enableFormFields(targetForm);
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
       var err = document.createTextNode(errorMsg);
       submitMsg.appendChild(err);
       topaz.formUtil.enableFormFields(targetForm);
       //topaz.domUtil.removeNewClass('post', '\sdisable', 'div');
       ldc.hide();
  
       return false;
     }
     else if (jsonObj.numFieldErrors > 0) {
       var fieldErrors = document.createDocumentFragment();
       var brTag = document.createElement('br');
       
       for (var item in jsonObj.fieldErrors.map) {
         var errorString = "";
         for (var ilist in jsonObj.fieldErrors.map[item]) {
           for (var i=0; i<jsonObj.numFieldErrors; i++) {
             errorString += jsonObj.fieldErrors.map[item][ilist][i];
             var error = document.createTextNode(errorString);
             
             fieldErrors.appendChild(error);
             fieldErrors.appendChild(brTag);
           }
         }
       }
       
       //alert("ERROR: " + fieldErrors);
       //var err = document.createTextNode("ERROR [Field]:");
       //submitMsg.appendChild(err);
       submitMsg.appendChild(fieldErrors);
       topaz.formUtil.enableFormFields(targetForm);
       //topaz.domUtil.removeNewClass('post', '\sdisable', 'div');

       ldc.hide();
  
       return false;
     }
     else {
       if (targetObj.isFlag) {
         ldc.hide();
         getFlagConfirm();
       }
       else {
         getDiscussion(targetObj);
         topaz.responsePanel.hide();
         topaz.formUtil.textCues.reset(targetForm.responseArea, targetObj.responseCue);
         topaz.formUtil.enableFormFields(targetForm);
       }
       return false;
     }
     
    },
    mimetype: "text/plain",
    formNode: targetForm
   };
   dojo.io.bind(bindArgs);
}

function getDiscussion(targetObj) {
  var refreshArea = dojo.byId(responseConfig.discussionContainer);

  ldc.show();
  
  var bindArgs = {
    url: namespace + "/annotation/listThreadRefresh.action?root=" + targetObj.baseId + "&inReplyTo=" + targetObj.baseId,
    method: "get",
    error: function(type, data, evt){
     var err = document.createTextNode("ERROR [AJAX]:" + data.toSource());
     //topaz.errorConsole.writeToConsole(err);
     //topaz.errorConsole.show();
     alert("ERROR:" + data.toSource());
     return false;
    },
    load: function(type, data, evt){
      var docFragment = document.createDocumentFragment();
      docFragment = data;

      refreshArea.innerHTML = docFragment;
      
      ldc.hide();

      return false;
    },
    mimetype: "text/html"
   };
   dojo.io.bind(bindArgs);
  
}

function getFlagConfirm() {
  dojo.byId('flagForm').style.display = "none";
  dojo.byId('flagConfirm').style.display = "block";  
}
