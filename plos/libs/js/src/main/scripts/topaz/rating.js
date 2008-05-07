/*
 * $HeadURL:: http://gandalf.topazproject.org/svn/head/plos/webapp/src/main/webapp/javas#$
 * $Id: rating.js 5581 2008-05-02 23:01:11Z jkirton $
 *
 * Copyright (c) 2006-2008 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
/**
  * topaz.rating
  *
  * This class uses a css-based ratings star and sets up the number of star 
  * rating to be displayed in the right hand column.  This also displays the 
  * rating dialog.
  *
  **/
dojo.provide("topaz.rating");
dojo.require("topaz.domUtil");
dojo.require("topaz.formUtil");
topaz.rating = {
	rateScale: 5,
	
  init: function() {
  },
  
  show: function(action){
    if (action && action == 'edit') {
      getRatingsForUser();
    }
    else {
      _ratingDlg.show();
    }
    
    return false;
  },
  
  buildCurrentRating: function(liNode, rateIndex) {
		var ratedValue = (parseInt(rateIndex)/this.rateScale)*100;
		liNode.className += " pct" + ratedValue;
		dojox.data.dom.textContent(liNode, "Currently " + rateIndex + "/" + this.rateScale + " Stars");
  },
  
  buildDialog: function(jsonObj) {
    var ratingList = document.getElementsByTagAndClassName('ul', 'star-rating');
    
    // build rating stars
    for (var i=0; i<ratingList.length; i++) {
    	var currentNode = ratingList[i];
    	
    	if (currentNode.className.match("edit") != null) {
	    	var rateChildNodes = currentNode.childNodes;
	      var rateItem = currentNode.id.substr(4);
        rateItem = rateItem.charAt(0).toLowerCase() + rateItem.substring(1); 
	      var rateItemCount = jsonObj[rateItem];
					     
	      var indexInt = 0;
				for (var n=0; n<rateChildNodes.length; n++) {
					var currentChild = rateChildNodes[n];
		      if (currentChild.nodeName == "#text" && (currentChild.nodeValue.match(new RegExp("\n")) || currentChild.nodeValue.match(new RegExp("\r")))) {
		        continue;
		      }
		      
		      if (currentChild.className.match("average") != null || ratingList[i].className.match("overall-rating") != null) {
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
				
	    	_ratingsForm[rateItem].value = jsonObj[rateItem];
				
    	}
    }
    
    // add title
    if (jsonObj.commentTitle != null) {
    	_ratingsForm.commentTitle.value = jsonObj.commentTitle;
    	_ratingsForm.cTitle.value = jsonObj.commentTitle;
    }
    
    // add comments
    if (jsonObj.comment) {
    	_ratingsForm.comment.value = jsonObj.comment;
    	_ratingsForm.cArea.value = jsonObj.comment;
    }
  },
  
  resetDialog: function() {
    var ratingList = document.getElementsByTagAndClassName('li', 'current-rating');
    
    // build rating stars
    for (var i=0; i<ratingList.length; i++) {
	      if (ratingList[i].className.match("average") != null || ratingList[i].className.match("overall-rating") != null) {
	      	continue;
	      }
	      
	      ratingList[i].className = ratingList[i].className.replaceStringArray(" ", "pct", "pct0");
    }
    
		topaz.formUtil.textCues.reset(_ratingTitle, _ratingTitleCue);
		topaz.formUtil.textCues.reset(_ratingComments, _ratingCommentCue);
  	
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
  	_ratingsForm[categoryId].value = rateNum;
  	var sibling = topaz.domUtil.firstSibling(node.parentNode);
  	var rateStyle = "pct" + (parseInt(rateNum) * 20);  
  	sibling.className = sibling.className.replaceStringArray(" ", "pct", rateStyle);
		this.buildCurrentRating(sibling, rateNum);
  }
}
  
function getRatingsForUser() {
	var targetUri = _ratingsForm.articleURI.value;
	dojo.xhrGet({
    url: _namespace + "/rate/secure/getRatingsForUser.action?articleURI=" + targetUri,
    handleAs:'json',
    error: function(response, ioArgs){
      handleXhrError(response, ioArgs);
    },
    load: function(response, ioArgs){
       var jsonObj = response;
       if (jsonObj.actionErrors.length > 0) {
         var errorMsg = "";
         for (var i=0; i<jsonObj.actionErrors.length; i++) {
           errorMsg = errorMsg + jsonObj.actionErrors[i] + "\n";
         }
         alert("ERROR: " + errorMsg);
       }
       else {
  		   _ratingDlg.show();
         topaz.rating.buildDialog(jsonObj);
       }
    }
  });
}

function updateRating() {
	topaz.formUtil.disableFormFields(_ratingsForm);
  var submitMsg = dojo.byId('submitRatingMsg');
  topaz.domUtil.removeChildren(submitMsg);
  var articleUri = _ratingsForm.articleURI.value;

  _ldc.show();
  dojo.xhrPost({
    url: _namespace + "/rate/secure/rateArticle.action",
    handleAs:'json',
    form: _ratingsForm,
    sync: true,
    error: function(response, ioArgs){
      handleXhrError(response, ioArgs);
    },
    load: function(response, ioArgs){
     var jsonObj = response;
     if (jsonObj.actionErrors.length > 0) {
       var errorMsg = "";
       for (var i=0; i<jsonObj.actionErrors.length; i++) {
         errorMsg += jsonObj.actionErrors[i] + "\n";
       }
       var err = document.createTextNode(errorMsg);
       submitMsg.appendChild(err);
       topaz.formUtil.enableFormFields(_ratingsForm);
       _ldc.hide();
     }
     else if (jsonObj.numFieldErrors > 0) {
       var fieldErrors = document.createDocumentFragment();
       for (var item in jsonObj.fieldErrors) {
         var errorString = "";
         for (var ilist in jsonObj.fieldErrors[item]) {
           var err = jsonObj.fieldErrors[item][ilist];
           if (err) {
             errorString += err;
             var error = document.createTextNode(errorString.trim());
             var brTag = document.createElement('br');

             fieldErrors.appendChild(error);
             fieldErrors.appendChild(brTag);
           }
         }
       }
	     submitMsg.appendChild(fieldErrors);
       topaz.formUtil.enableFormFields(_ratingsForm);
       _ldc.hide();
     }
     else {
       _ratingDlg.hide();
       getArticle("rating");
       topaz.formUtil.enableFormFields(_ratingsForm);
     }
   }
  });
}

function refreshRating(uri) {
	var refreshArea1 = dojo.byId(ratingConfig.ratingContainer + "1");
	var refreshArea2 = dojo.byId(ratingConfig.ratingContainer + "2");
  dojo.xhrGet({
    url: _namespace + "/rate/getUpdatedRatings.action?articleURI=" + uri,
    handleAs:'json',
    error: function(response, ioArgs){
     handleXhrError(response, ioArgs);
    },
    load: function(response, ioArgs){
     var jsonObj = response;
     var docFragment = document.createDocumentFragment();
     docFragment = data;
     refreshArea1.innerHTML = docFragment;
     refreshArea2.innerHTML = docFragment;
    }
  });
}

  
  
  
