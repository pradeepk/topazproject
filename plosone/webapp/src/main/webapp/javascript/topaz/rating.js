/**
  * topaz.rating
  *
  * @param
  *
  **/
topaz.rating = new Object();

topaz.rating = {
	rateScale: 5,
	
  init: function() {
  },
  
  show: function(action){
    if (action && action == 'edit') {
      getRatingsForUser();
    }
    else {
      ratingDlg.show();
    }
    
    return false;
  },
  
  buildCurrentRating: function(liNode, rateIndex) {
		var ratedValue = (parseInt(rateIndex)/this.rateScale)*100;
		liNode.className += " pct" + ratedValue;
		liNode.textContent = "Currently " + rateIndex + "/" + this.rateScale + " Stars";
  },
  
  buildDialog: function(jsonObj) {
    var ratingList = document.getElementsByTagAndClassName('ul', 'star-rating');
    
    // build rating stars
    for (var i=0; i<ratingList.length; i++) {
    	var currentNode = ratingList[i];
    	
    	if (currentNode.className.match("edit") != null) {
	    	var rateChildNodes = currentNode.childNodes;
	      var rateItem = currentNode.id.substr(4).toLowerCase();
	      var rateItemCount = jsonObj[rateItem];
	
	/*			var newLi = document.createElement("li");
				newLi.className = "current-rating";
				var ratedValue = (rateItemCount/this.rateScale)*100;
				var ratedPercentage = ratedValue + "%";
				newLi.style.width = ratedPercentage;
				newLi.textContent = "Currently " + rateItemCount + "/" + this.rateScale + " Stars";
				
				dojo.dom.insertBefore(newLi, ratingList[i].firstChild);
	//			dojo.dom.replaceNode(ratingList[i].firstChild, newLi);
	*/
	
				     
	      var indexInt = 0;
				for (var n=0; n<rateChildNodes.length; n++) {
					var currentChild = rateChildNodes[n];
		      if (currentChild.nodeName == "#text" && (currentChild.nodeValue.match(new RegExp("\n")) || currentChild.nodeValue.match(new RegExp("\r")))) {
		        continue;
		      }
		      
		      if (currentChild.className.match("average") != null) {
		      	continue;
		      }
		      
		      if(currentChild.className.match("current-rating")) {
						this.buildCurrentRating(currentChild, rateItemCount);
						firstSet = true;
						continue;
		      }
		      
					if (indexInt < rateItemCount) {
						currentChild.onmouseover = function() { topaz.rating.hover.on(this); }
						currentChild.onmouseout  = function() { topaz.rating.hover.off(this); }
						
						indexInt++;
					}
				}
    	}
    }
    
    // add title
    if (jsonObj.commentTitle != null) {
    	ratingsForm.commentTitle.value = jsonObj.commentTitle;
    	ratingsForm.cTitle.value = jsonObj.commentTitle;
    }
    
    // add comments
    if (jsonObj.comment) {
    	ratingsForm.comment.value = jsonObj.comment;
    	ratingsForm.cArea.value = jsonObj.comment;
    }
  },
  
  resetDialog: function() {
    var ratingList = document.getElementsByTagAndClassName('li', 'current-rating');
    
    // build rating stars
    for (var i=0; i<ratingList.length; i++) {
    	if (ratingList[i].className.match("average") == null)
	      ratingList[i].className = ratingList[i].className.replaceStringArray(" ", "pct", "pct0");
    }
    
    // reset title
		ratingsForm.commentTitle.value = "";
		ratingsForm.cTitle.value = ratingTitleCue;
    
    // reset comments
  	ratingsForm.comment.value = "";
  	ratingsForm.cArea.value = ratingCommentCue;
  },
  
  hover: {
  	on: function(node) {
  		var sibling = topaz.domUtil.firstSibling(node);
  		sibling.style.display = "none"
  	},
  	
  	off: function(node) {
  		var sibling = topaz.domUtil.firstSibling(node);
  		sibling.style.display = "block";
  	}
  },
  
  setRatingCategory: function(node, categoryId, rateNum) {
  	ratingsForm[categoryId].value = rateNum;
  	var sibling = topaz.domUtil.firstSibling(node.parentNode);
  	var rateStyle = "pct" + (parseInt(rateNum) * 20);  
  	sibling.className = sibling.className.replaceStringArray(" ", "pct", rateStyle);
		this.buildCurrentRating(sibling, rateNum);
  }
}
  
function getRatingsForUser() {
	 var targetUri = ratingsForm.articleUri.value;
	 
   var bindArgs = {
    url: namespace + "/rate/secure/getRatingsForUser.action?articleUri=" + targetUri,
    method: "get",
    error: function(type, data, evt){
     alert("An error occurred." + data.toSource());
     var err = document.createTextNode("ERROR [AJAX]:" + data.toSource());
     //topaz.errorConsole.writeToConsole(err);
     //topaz.errorConsole.show();
     
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
       
       alert("ERROR: " + errorMsg);
       
       return false;
     }
     else {
       
		   ratingDlg.show();
       topaz.rating.buildDialog(jsonObj);
       return false;
     }
     
    },
    mimetype: "text/plain",
    //formNode: ratingsForm,
    transport: "XMLHTTPTransport"
   };
   dojo.io.bind(bindArgs);
}

function updateRating() {
	topaz.formUtil.disableFormFields(ratingsForm);
  var submitMsg = dojo.byId('submitRatingMsg');
  dojo.dom.removeChildren(submitMsg);

  ldc.show();
   
  var bindArgs = {
    url: namespace + "/rate/secure/rateArticle.action",
    method: "post",
    error: function(type, data, evt){
     alert("An error occurred." + data.toSource());
     var err = document.createTextNode("ERROR [AJAX]:" + data.toSource());
     
     return false;
   },
   load: function(type, data, evt){
     var jsonObj = dojo.json.evalJson(data);
     
     if (jsonObj.actionErrors.list.length > 0) {
       var errorMsg;
       
       for (var i=0; i<jsonObj.actionErrors.list.length; i++) {
         errorMsg = errorMsg + jsonObj.actionErrors.list[i] + "\n";
       }
       
       //alert("ERROR: " + errorMsg);
       var err = document.createTextNode(errorMsg);
       submitMsg.appendChild(err);
       topaz.formUtil.enableFormFields(annotationForm);
       //ratingDlg.placeModalDialog();
       ldc.hide();
       
       return false;
     }
     else if (jsonObj.numFieldErrors > 0) {
       var fieldErrors = document.createDocumentFragment();
       
       for (var item in jsonObj.fieldErrors.map) {
         var errorString = "";
         for (var ilist in jsonObj.fieldErrors.map[item]) {
           for (var i=0; i<jsonObj.numFieldErrors; i++) {
             var err = jsonObj.fieldErrors.map[item][ilist][i];
             if (err) {
               errorString += err;
               var error = document.createTextNode(errorString.trim());
               var brTag = document.createElement('br');
               
               fieldErrors.appendChild(error);
               fieldErrors.appendChild(brTag);
             }
           }
         }
       }
       
	     submitMsg.appendChild(fieldErrors);
       topaz.formUtil.enableFormFields(ratingsForm);
       ldc.hide();

       return false;
     }
     else {
       if (djConfig.isDebug) {
         dojo.byId(djConfig.debugContainerId).innerHTML = "";
       }
       getArticle();
       ratingDlg.hide();

       topaz.formUtil.textCues.reset(ratingTitle, titleCue);
       topaz.formUtil.textCues.reset(ratingComments, commentCue);
        
       topaz.formUtil.enableFormFields(ratingsForm);
       return false;
     }
     
   },
   mimetype: "text/plain",
   formNode: ratingsForm,
   transport: "XMLHTTPTransport"
  };
  dojo.io.bind(bindArgs);
}


  
  
  
