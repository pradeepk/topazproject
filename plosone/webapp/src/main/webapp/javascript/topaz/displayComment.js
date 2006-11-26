/**
  * topaz.commentDisplay
  *
  * @param
  *
  **/
topaz.displayComment = new Object();

topaz.displayComment = {
  target: "",
  
  sectionTitle: "",
  
  sectionDetail: "",
  
  sectionComment: "",
  
  sectionLink: "",
  
  retrieveMsg: "",
  
  init: function() {
    this.sectionTitle   = dojo.byId(commentConfig.sectionTitle);
    this.sectionDetail  = dojo.byId(commentConfig.sectionDetail);
    this.sectionComment = dojo.byId(commentConfig.sectionComment);
    this.sectionLink    = dojo.byId(commentConfig.sectionLink);
    this.retrieveMsg    = dojo.byId(commentConfig.retrieveMsg);    
  },
  
  isMultiple: function(attr) {
    var attrList = this.parseAttributeToList(attr);
    
    return (attrList.length > 1) ? true : false;
  },
  
  setTarget: function(obj) {
    this.target = obj;
  },
  
  setSectionTitle: function(configObj) {
    this.sectionTitle = dojo.byId(configObj.sectionTitle);
  },
  
  setSectionDetail: function(configObj) {
    this.sectionDetail = dojo.byId(configObj.sectionDetail);
  },
  
  setSectionComment: function(configObj) {
    this.sectionComment = dojo.byId(configObj.sectionComment);
  },
  
  setSectionLink: function(configObj) {
    this.sectionLink = dojo.byId(configObj.sectionLink);
  },
  
  setRetrieveMsg: function(configObj) {
    this.retrieveMsg = dojo.byId(configObj.retrieveMsg);
  },
  
  show: function(obj){
    this.setTarget(obj);
    
		popup.setMarker(this.target);
		popupm.setMarker(this.target);
    getComment(this.target);
    
    return false;
  },
    
  buildDisplayHeader: function (jsonObj) {
    var titleDocFrag = document.createDocumentFragment();
    
    // Insert title link text
    var titleLink = document.createElement('a');
    titleLink.href = "#"; 
    titleLink.className = "discuss icon";
    titleLink.title="View full annotation";
    //alert("jsonObj.annotation.commentTitle = " + jsonObj.annotation.commentTitle);
    titleLink.appendChild(document.createTextNode(jsonObj.annotation.commentTitle));
    titleDocFrag.appendChild(titleLink);

    return titleDocFrag;    
  },

  buildDisplayDetail: function (jsonObj) {
    // Insert creator detail info
    var creatorId = jsonObj.creatorUserName;
    var creatorLink = document.createElement('a');
    creatorLink.href = "#";
    creatorLink.title = "Annotation Author";
    creatorLink.className = "user icon";
    creatorLink.appendChild(document.createTextNode(creatorId));
    
    var d = new Date(jsonObj.annotation.createdAsDate.time);
    var day = d.getDate();
    var month = d.getMonth() + 1;
    var year = d.getFullYear();
    var hours = d.getHours();
    var minutes = d.getMinutes();
    
    var dateStr = document.createElement('strong');
    dateStr.appendChild(document.createTextNode(year + "-" + month + "-" + day));
    var timeStr = document.createElement('strong');
    timeStr.appendChild(document.createTextNode(hours + ":" + minutes + " GMT"));
    
    var detailDocFrag = document.createDocumentFragment();
    detailDocFrag.appendChild(document.createTextNode('Posted by '));
    detailDocFrag.appendChild(creatorLink);
    detailDocFrag.appendChild(document.createTextNode(' on '));
    detailDocFrag.appendChild(dateStr);
    detailDocFrag.appendChild(document.createTextNode(' at '));
    detailDocFrag.appendChild(timeStr);
    
    return detailDocFrag;
  },
  
  buildDisplayBody: function (jsonObj) {
    // Insert formatted comment
    var commentFrag = document.createDocumentFragment();
    commentFrag = jsonObj.annotation.commentWithUrlLinking;
    
    return commentFrag;
  },
  
  buildDisplayViewLink: function (jsonObj) {
    var commentLink = document.createElement('a');
    commentLink.href = namespace + '/annotation/listThread.action?inReplyTo=' + jsonObj.annotationId + '&root=' + jsonObj.annotationId;
    commentLink.className = 'commentary icon';
    commentLink.title = 'Click to view full thread and respond';
    commentLink.appendChild(document.createTextNode('View all responses'));
    
    return commentLink;
  },
  
  buildDisplayView: function(jsonObj){
    if (topaz.displayComment.sectionTitle.hasChildNodes) dojo.dom.removeChildren(topaz.displayComment.sectionTitle);
    topaz.displayComment.sectionTitle.appendChild(this.buildDisplayHeader(jsonObj));
    
    if (topaz.displayComment.sectionDetail.hasChildNodes) dojo.dom.removeChildren(topaz.displayComment.sectionDetail);
    topaz.displayComment.sectionDetail.appendChild(this.buildDisplayDetail(jsonObj));

    //alert(commentFrag);
    if (topaz.displayComment.sectionComment.hasChildNodes) dojo.dom.removeChildren(topaz.displayComment.sectionComment);
    topaz.displayComment.sectionComment.innerHTML = this.buildDisplayBody(jsonObj);
    //alert("jsonObj.annotation.commentWithUrlLinking = " + jsonObj.annotation.commentWithUrlLinking);
    
    if (topaz.displayComment.sectionLink.hasChildNodes) dojo.dom.removeChildren(topaz.displayComment.sectionLink);
    this.sectionLink.appendChild(this.buildDisplayViewLink(jsonObj));
  },
  
  buildDisplayViewMultiple: function(jsonObj, iter){
    var newListItem = document.createElement('li');
    newListItem.onclick = function() {
        topaz.displayComment.mouseoutComment(topaz.displayComment.target);
        topaz.displayComment.mouseoverComment(topaz.displayComment.target, jsonObj.annotationId);
        topaz.domUtil.swapClassNameBtwnSibling(this, this.nodeName, 'active');
        topaz.domUtil.swapAttributeByClassNameForDisplay(topaz.displayComment.target, ' active', 'annotationid', jsonObj.annotationId);
      }
    
    
    if (iter <= 0)
      newListItem.className = 'active';
    
    var dl = document.createElement('dl');
    
    var dt = document.createElement('dt');
    dt.appendChild(this.buildDisplayHeader(jsonObj));
    var detailDiv = document.createElement('div');
    detailDiv.className = 'detail';
    detailDiv.appendChild(this.buildDisplayDetail(jsonObj)); 
    dt.appendChild(detailDiv);   
    
    var dd = document.createElement('dd');
    var contentDiv = document.createElement('div');
    contentDiv.className = 'contentwrap';
    contentDiv.innerHTML = this.buildDisplayBody(jsonObj);
    
    var cDetailDiv = document.createElement('div');
    cDetailDiv.className = 'detail';
    /*var commentLink = document.createElement('a');
    commentLink.href = '#';
    commentLink.className = 'commentary icon';
    commentLink.title = 'Click to view full thread and respond';
    commentLink.appendChild(document.createTextNode('View full commentary'));
    
    var responseLink = document.createElement('a');
    responseLink.href = '#';
    responseLink.className = 'respond tooltip';
    responseLink.title = 'Click to respond to this posting';
    responseLink.appendChild(document.createTextNode('Respond to this'));
    
    cDetailDiv.appendChild(commentLink);
    cDetailDiv.appendChild(responseLink);*/
    cDetailDiv.appendChild(this.buildDisplayViewLink(jsonObj));
    
    contentDiv.appendChild(cDetailDiv);
    dd.appendChild(contentDiv);
    dl.appendChild(dt);
    dl.appendChild(dd);
    newListItem.appendChild(dl);
    
    return newListItem;
  },
  
  mouseoverComment: function (obj, displayId) {
   var elementList = topaz.domUtil.getDisplayMap(obj, displayId);
   
   // Find the displayId that has the most span nodes containing that has a 
   // corresponding id in the annotationId attribute.  
   var longestAnnotElements;
   for (var i=0; i<elementList.length; i++) {
     if (i == 0) {
       longestAnnotElements = elementList[i];
     }
     else if (elementList[i].elementCount > elementList[i-1].elementCount){
       longestAnnotElements = elementList[i];
     }
   }
   
   //this.modifyClassName(obj);
   
   // the annotationId attribute, modify class name.
   for (var n=0; n<longestAnnotElements.elementCount; n++) {
     var classList = new Array();
     var elObj = longestAnnotElements.elementList[n];

     this.modifyClassName(elObj);
   }

  },

  mouseoutComment: function (obj) {
    var elList = document.getElementsByTagName('span');
    
    for(var i=0; i<elList.length; i++) {
      elList[i].className = elList[i].className.replace(/\-active/, "");
    }
    obj.className = obj.className.replace(/\-active/, "");
  },
  
  modifyClassName: function (obj) {
     classList = obj.className.split(" ");
     for (var i=0; i<classList.length; i++) {
       if ((classList[i].match('public') || classList[i].match('private') || classList[i].match('bug')) && !classList[i].match(' active')) {
         classList[i] = classList[i].concat("-active");
       }
     }
     
     obj.className = classList.join(' ');
  },
  
  processBugCount: function () {
    var bugList = document.getElementsByTagAndClassName(null, 'bug');
    
    for (var i=0; i<bugList.length; i++) {
      var bugCount = topaz.domUtil.getDisplayId(bugList[i]);
      
      if (bugCount != null) {
        var displayBugs = bugCount.split(',');
        var count = displayBugs.length;
        var ctText = document.createTextNode(count);
      }
      else {
        var ctText = document.createTextNode('0');
      }

      dojo.dom.removeChildren(bugList[i]);
      bugList[i].appendChild(ctText);
    }
  }
}

function getComment(obj) {
    ldc.show();
    
    var targetUri = topaz.domUtil.getDisplayId(obj);
          
    var uriArray = targetUri.split(",");

    if (uriArray.length > 1) {
      var targetContainer = document.getElementById('multilist');
      dojo.dom.removeChildren(targetContainer);
    }
    else {
      var targetContainer =  dojo.widget.byId("CommentDialog");
    }
    
    var maxShown = 4;
    var stopPt = (uriArray.length < maxShown) ? uriArray.length : maxShown;
    
    var count = 0;
    
    for (var i=0; i<stopPt; i++) {
      //alert("uriArray[" + i + "] = " + uriArray[i]);
      var bindArgs = {
        url: namespace + "/annotation/getAnnotation.action?annotationId=" + uriArray[i],
        method: "get",
        error: function(type, data, evt){
         alert("ERROR [AJAX]:" + data.toSource());
         var err = document.createTextNode("ERROR [AJAX]:" + data.toSource());
         //topaz.errorConsole.writeToConsole("ERROR [AJAX]:" + data.toSource());
         //errView.show();
         return false;
        },
        load: function(type, data, evt){
         var jsonObj = dojo.json.evalJson(data);
         
         //alert("jsonObj:\n" + jsonObj.toSource());
         
         if (jsonObj.actionErrors.list.length > 0) {
           var errorMsg;
           //alert("jsonObj.actionErrors.list.length = " + jsonObj.actionErrors.list.length);
           for (var i=0; i<jsonObj.actionErrors.list.length; i++) {
             errorMsg = errorMsg + jsonObj.actionErrors.list[i] + "\n";
           }
           
           alert("ERROR [actionErrors]: " + errorMsg);
           var err = document.createTextNode("ERROR:" + errorMsg);
           //topaz.displayComment.retrieveMsg.innerHTML = err;
           ldc.hide();
           
           return false;
         }
         else if (jsonObj.numFieldErrors > 0) {
           var fieldErrors;
           //alert("jsonObj.numFieldErrors = " + jsonObj.numFieldErrors);
           for (var item in jsonObj.fieldErrors.map) {
             var errorString = "";
             for (var i=0; i<jsonObj.fieldErrors.map[item].list[0].length; i++) {
               errorString += jsonObj.fieldErrors.map[item].list[0][i];
             }
             fieldErrors = fieldErrors + item + ": " + errorString + "<br/>";
           }
           
           alert("ERROR [numFieldErrors]: " + fieldErrors);
           var err = document.createTextNode("ERROR:" + fieldErrors);
           //topaz.displayComment.retrieveMsg.innerHTML = err;
           ldc.hide();
  
           return false;
         }
         else {
           if (uriArray.length > 1) {             
             var newListItem = topaz.displayComment.buildDisplayViewMultiple(jsonObj, targetContainer.childNodes.length);
             targetContainer.appendChild(newListItem);
             if (targetContainer.childNodes.length == stopPt) {
               topaz.displayComment.mouseoverComment(topaz.displayComment.target, uriArray[0]);
                
               popupm.show();
               ldc.hide();
            
               return false;
             }
           }
           else {
             topaz.displayComment.buildDisplayView(jsonObj);
             topaz.displayComment.mouseoverComment(topaz.displayComment.target);
 
             popup.show();
             ldc.hide();
          
             return false;
           }
           
           //alert("targetContainer.childNodes.length = " + targetContainer.childNodes.length + "\n" + "stopPt = " + stopPt);
           
         }
        },
        mimetype: "text/html"
       };
       dojo.io.bind(bindArgs);
    }

  }
  
  
  
