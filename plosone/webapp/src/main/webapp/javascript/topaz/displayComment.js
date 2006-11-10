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
  
  retrieveMsg: "",
  
  init: function() {
    this.sectionTitle = dojo.byId(commentConfig.sectionTitle);
    this.sectionDetail = dojo.byId(commentConfig.sectionDetail);
    this.sectionComment = dojo.byId(commentConfig.sectionComment);
    this.retrieveMsg = dojo.byId(commentConfig.retrieveMsg);
    
    this.processBugCount();
  },
  
  parseAttributeToList: function(attr) {
    var attrList = new Array();
    attr = attr.split(' ');
    
    return attrList;
  },
  
  isMultiple: function(attr) {
    var attrList = this.parseAttributeToList(attr);
    
    return (attrList.length > 1) ? true : false;
  },
  
  setTarget: function(obj) {
    this.target = obj;
  },
  
  show: function(obj){
    this.setTarget(obj);
    
		popup.setMarker(this.target);
    getComment(this.target);
    
    return false;
  },
    
  buildDisplayView: function(jsonObj){
    var titleDocFrag = document.createDocumentFragment();
    
    // Insert title link text
    var titleLink = document.createElement('a');
    titleLink.href = "#"; 
    titleLink.className = "discuss icon";
    titleLink.title="View full annotation";
    //alert("jsonObj.annotation.commentTitle = " + jsonObj.annotation.commentTitle);
    titleLink.appendChild(document.createTextNode(jsonObj.annotation.commentTitle));
    titleDocFrag.appendChild(titleLink);
    
    dojo.dom.removeChildren(topaz.displayComment.sectionTitle);
    topaz.displayComment.sectionTitle.appendChild(titleDocFrag);
    
    // Insert creator detail info
    var creatorId = jsonObj.creatorUserName;
    var creatorLink = document.createElement('a');
    creatorLink.href = "#";
    creatorLink.title = "Annotation Author";
    creatorLink.className = "user icon";
    creatorLink.appendChild(document.createTextNode(creatorId));
    
    var d = new Date(topaz.domUtil.ISOtoJSDate(jsonObj.annotation.created));
    var day = d.getDate();
    var month = d.getMonth() + 1;
    var year = d.getFullYear();
    var hours = d.getHours();
    var minutes = d.getMinutes();
    
    
    var dateStr = document.createElement('strong');
    dateStr.appendChild(document.createTextNode(year + "-" + month + "-" + day));
    //dateStr.appendChild(document.createTextNode(topaz.domUtil.ISOtoJSDate(jsonObj.annotation.created)));
    
    var timeStr = document.createElement('strong');
    timeStr.appendChild(document.createTextNode(hours + ":" + minutes + "GMT"));
    
    var detailDocFrag = document.createDocumentFragment();
    detailDocFrag.appendChild(document.createTextNode('Posted by '));
    detailDocFrag.appendChild(creatorLink);
    detailDocFrag.appendChild(document.createTextNode(' on '));
    detailDocFrag.appendChild(dateStr);
    detailDocFrag.appendChild(document.createTextNode(' at '));
    detailDocFrag.appendChild(timeStr);
    
    dojo.dom.removeChildren(topaz.displayComment.sectionDetail);
    topaz.displayComment.sectionDetail.appendChild(detailDocFrag);

    // Insert formatted comment
    var commentFrag = document.createTextNode(jsonObj.annotation.commentWithUrlLinking);
    //alert(commentFrag);
    dojo.dom.removeChildren(topaz.displayComment.sectionComment);
    topaz.displayComment.sectionComment.appendChild(commentFrag);
    //alert("jsonObj.annotation.commentWithUrlLinking = " + jsonObj.annotation.commentWithUrlLinking);
  },
  
  mouseoverComment: function (obj, displayId) {
   var elementList = this.getDisplayMap(obj, displayId);
   
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
       if ((classList[i].match('public') || classList[i].match('private') || classList[i].match('bug')) && !classList[i].match('-active')) {
         classList[i] = classList[i].concat("-active");
         
       }
     }
     
     obj.className = classList.join(' ');
  },
  
  getDisplayMap: function(obj, displayId) {
    var displayIdList = (displayId != null) ? displayId : topaz.domUtil.getDisplayId(obj).split(',');
    //alert("displayIdList = " + displayIdList);
    
    var annoteEl = document.getElementsByTagAndAttributeName('span', 'annotationid');
    var elDisplayList = new Array();
    
    // Based on the list of displayId from the element object, figure out which element 
    // has an annotationId list in which there's an annotation id that matches the 
    // display id.
    for (var i=0; i<displayIdList.length; i++) {
      var elAttrList = new Array();
      for (var n=0; n<annoteEl.length; n++) {
        var attrList = topaz.domUtil.getAnnotationId(annoteEl[n]).split(',');
        
        for (var x=0; x<attrList.length; x++) {
          if(attrList[x].match(displayIdList[i])) {
            elAttrList.push(annoteEl[n]);
          }
        }
      }
      
      elDisplayList.push({'displayId': displayIdList[i],
                          'elementList': elAttrList,
                          'elementCount': elAttrList.length});
    }
    
    return elDisplayList;
  },
  
  processBugCount: function () {
    var bugList = document.getElementsByTagAndClassName(null, 'bug');
    
    for (var i=0; i<bugList.length; i++) {
      var bugCount = topaz.domUtil.getDisplayId(bugList[i]);
      
      if (bugCount != null) {
        var displayBugs = bugCount.split(',');
        var count = displayBugs.length;
        bugList[i].innerHTML = count;
      }
      else {
        bugList[i].innerHTML = 0;
      }
    }
  }
}

function getComment(obj) {
    var targetDiv = dojo.widget.byId("CommentDialog");
    
    //alert("topaz.displayComment.target = " + topaz.displayComment.target);
    var targetUri = topaz.domUtil.getDisplayId(obj);
    //alert("targetUri = " + targetUri);
    /*alert("this.sectionTitle.nodeName = " + this.sectionTitle.nodeName + "\n" +
          "this.sectionDetail.nodeName = " + this.sectionDetail.nodeName + "\n" +
          "this.sectionDetail.nodeName = " + this.sectionDetail.nodeName + "\n" +
          "this.retrieveMsg.nodeName = " + this.retrieveMsg.nodeName);*/
    
    var bindArgs = {
      url: namespace + "/annotation/getAnnotation.action?annotationId=" + targetUri,
      method: "get",
      error: function(type, data, evt){
       alert("ERROR [AJAX]:" + data.toSource());
       var err = document.createTextNode("ERROR:" + data.toSource());
       topaz.displayComment.retrieveMsg.innerHTML = err;
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
         topaz.displayComment.retrieveMsg.innerHTML = err;
         
         return false;
       }
       else if (jsonObj.numFieldErrors > 0) {
         var fieldErrors;
         //alert("jsonObj.numFieldErrors = " + jsonObj.numFieldErrors);
         for (var item in jsonObj.fieldErrors.map) {
           fieldErrors = fieldErrors + item + ": " + jsonObj.fieldErrors.map[item] + "\n";
         }
         
         alert("ERROR [numFieldErrors]: " + fieldErrors);
         var err = document.createTextNode("ERROR:" + fieldErrors);
         topaz.displayComment.retrieveMsg.innerHTML = err;

         return false;
       }
       else {
         //alert("success");
         topaz.displayComment.buildDisplayView(jsonObj);
         topaz.displayComment.mouseoverComment(topaz.displayComment.target);
  	     popup.show();  		

         return false;
       }
      },
      mimetype: "text/html"
     };
     dojo.io.bind(bindArgs);
    
  }
  
