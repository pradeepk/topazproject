/*
 * $HeadURL::                                                                            $
 * $Id$
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
var _noteType;
var _commentTitle;
var _comments;
var _ratingTitle;
var _ratingComments;
var _titleCue    			 = 'Enter your note title...';
var _commentCue    		 = 'Enter your note...';
var _ratingTitleCue		 = 'Enter your comment title...';
var _ratingCommentCue   = 'Enter your comment...';

function initAnnotationForm() {	
	_noteType = _annotationForm.cNoteType;
  _commentTitle    = _annotationForm.cTitle;
	_comments        = _annotationForm.cArea;
	//var privateFlag  = _annotationForm.privateFlag;
	//var publicFlag   = _annotationForm.publicFlag;
	//var btnSave      = dojo.byId("btn_save");  
	var btnPost      = dojo.byId("btn_post");
	var btnCancel    = dojo.byId("btn_cancel");
	var submitMsg    = dojo.byId('submitMsg');
	
  // Annotation Dialog Box: Note type field
  dojo.connect(_noteType, "onchange", function () {
    dojo.byId('cdls').style.visibility = _noteType.value == 'correction' ? 'visible' : 'hidden';
    _annotationForm.noteType.value = _noteType.value;
  });
  
	// Annotation Dialog Box: Title field
	dojo.connect(_commentTitle, "onfocus", function () { 
	  topaz.formUtil.textCues.off(_commentTitle, _titleCue); 
	});
	
	dojo.connect(_commentTitle, "onchange", function () {
    var fldTitle = _annotationForm.commentTitle;
    if(_annotationForm.cTitle.value != "" && _annotationForm.cTitle.value != _titleCue) {
      fldTitle.value = _annotationForm.cTitle.value;
    }
    else {
      fldTitle.value = "";
    }
	});

	dojo.connect(_commentTitle, "onblur", function () { 
    var fldTitle = _annotationForm.commentTitle;
    if(_annotationForm.cTitle.value != "" && _annotationForm.cTitle.value != _titleCue) {
      fldTitle.value = _annotationForm.cTitle.value;
    }
    else {
      fldTitle.value = "";
    }
	  topaz.formUtil.textCues.on(_commentTitle, _titleCue); 
	});
	
	// Annotation Dialog Box: Comment field
	dojo.connect(_comments, "onfocus", function () {
	  topaz.formUtil.textCues.off(_comments, _commentCue);
	});

	dojo.connect(_comments, "onchange", function () {
    var fldTitle = _annotationForm.comment;
    if(_annotationForm.cArea.value != "" && _annotationForm.cArea.value != _commentCue) {
      fldTitle.value = _annotationForm.cArea.value;
    }
    else {
      fldTitle.value = "";
    }
	});
	
	dojo.connect(_comments, "onblur", function () {
    var fldTitle = _annotationForm.comment;
    if(_annotationForm.cArea.value != "" && _annotationForm.cArea.value != _commentCue) {
      fldTitle.value = _annotationForm.cArea.value;
    }
    else {
      fldTitle.value = "";
    }
	  topaz.formUtil.textCues.on(_comments, _commentCue); 
	  //topaz.formUtil.checkFieldStrLength(_comments);
	});
	
	// Annotation Dialog Box: Private/Public radio buttons
	/*
  dojo.connect(privateFlag, "onclick", function() {
	  topaz.formUtil.toggleFieldsByClassname('commentPrivate', 'commentPublic'); 
	  _dlg.placeModalDialog();
  	//var btn = btnSave;
  	//__dlg.setCloseControl(btn);
  });
  dojo.connect(publicFlag, "onclick", function() {
	  topaz.formUtil.toggleFieldsByClassname('commentPublic', 'commentPrivate'); 
	  _dlg.placeModalDialog();
  	//var btn = btnPost;
  	//_dlg.setCloseControl(btn);
	});
  
	// Annotation Dialog Box: Save button
	dojo.connect(btnSave, "onclick", function(e) {
	  //getCommentValues();
    validateNewComment();
    e.preventDefault();
  });
  */
  
	// Annotation Dialog Box: Post buttons
	dojo.connect(btnPost, "onclick", function(e) {
	  //getCommentValues();
    validateNewComment();
    e.preventDefault();
  });

	dojo.connect(btnCancel, "onclick", function(e) {
    topaz.domUtil.removeChildren(submitMsg);
    _dlg.hide();
    topaz.formUtil.enableFormFields(_annotationForm);
    if(!annotationConfig.rangeInfoObj.isSimpleText) {
      // we are in an INDETERMINISTIC state for annotation markup
      // Article re-fetch is necessary to maintain the integrity of the existing annotation markup
      getArticle();
      topaz.displayComment.processBugCount();
    }
    else {
      // we can safely rollback the pending annotation markup from the dom
      topaz.annotation.undoPendingAnnotation();
    }
    e.preventDefault();
  });

	/******************************************************
	 * Ratings Initial Settings
	 ******************************************************/
  _ratingTitle               = _ratingsForm.cTitle;
	_ratingComments            = _ratingsForm.cArea;
	var btnPostRating        = dojo.byId("btn_post_rating");
	var btnCancelRating      = dojo.byId("btn_cancel_rating");
	var submitRatingMsg        = dojo.byId('submitRatingMsg');
	
	dojo.connect(_ratingTitle, "onfocus", function () { 
	  topaz.formUtil.textCues.off(_ratingTitle, _ratingTitleCue); 
	});
	
	dojo.connect(_ratingTitle, "onchange", function () {
    var fldTitle = _ratingsForm.commentTitle;
    if(_ratingsForm.cTitle.value != "" && _ratingsForm.cTitle.value != _ratingTitleCue) {
      fldTitle.value = _ratingsForm.cTitle.value;
    }
    else {
      fldTitle.value = "";
    }
	});

	dojo.connect(_ratingTitle, "onblur", function () { 
    var fldTitle = _ratingsForm.commentTitle;
    if(_ratingsForm.cTitle.value != "" && _ratingsForm.cTitle.value != _ratingTitleCue) {
      fldTitle.value = _ratingsForm.cTitle.value;
    }
    else {
      fldTitle.value = "";
    }
	  topaz.formUtil.textCues.on(_ratingTitle, _ratingTitleCue); 
	});
	
	// Annotation Dialog Box: Comment field
	dojo.connect(_ratingComments, "onfocus", function () {
	  topaz.formUtil.textCues.off(_ratingComments, _ratingCommentCue);
	});

	dojo.connect(_ratingComments, "onchange", function () {
    var fldTitle = _ratingsForm.comment;
    if(_ratingsForm.cArea.value != "" && _ratingsForm.cArea.value != _ratingCommentCue) {
      fldTitle.value = _ratingsForm.cArea.value;
    }
    else {
      fldTitle.value = "";
    }
	});
	
	dojo.connect(_ratingComments, "onblur", function () {
    var fldTitle = _ratingsForm.comment;
    if(_ratingsForm.cArea.value != "" && _ratingsForm.cArea.value != _ratingCommentCue) {
      fldTitle.value = _ratingsForm.cArea.value;
    }
    else {
      fldTitle.value = "";
    }
	  topaz.formUtil.textCues.on(_ratingComments, _ratingCommentCue); 
	  //topaz.formUtil.checkFieldStrLength(_ratingComments);
	});
	
	// Rating Dialog Box: Post buttons
	dojo.connect(btnPostRating, "onclick", function(e) {
    updateRating();
    topaz.rating.resetDialog();
    e.preventDefault();
  });

	dojo.connect(btnCancelRating, "onclick", function(e) {
    topaz.domUtil.removeChildren(submitMsg);
    _ratingDlg.hide();
    topaz.formUtil.enableFormFields(_ratingsForm);
    topaz.rating.resetDialog();
	  /* don't re-fetch article on cancel
    getArticle("rating");
    topaz.displayComment.processBugCount();
    */
    e.preventDefault();
  });

}

/*
var activeToggleId = "";
var activeWidget = "";

function setActiveToggle(widgetId, boxId) {
  activeToggleId = boxId;
  activeWidget = dojo.byId(widgetId);
}

function singleView(obj) {
  if (activeToggleId != "") {
    topaz.domUtil.swapDisplayMode(activeToggleId, "none");
    toggleExpand(activeWidget, false); 
  }
}

function singleExpand(obj, targetId) {
  if (targetId != activeToggleId) {
    singleView(obj);
  }
  setActiveToggle
   (obj.id, targetId);
  topaz.domUtil.swapDisplayMode(targetId);
  toggleExpand(obj); 
  
  return false;
}
*/

function toggleAnnotation(obj, userType) {
  _ldc.show();
  var bugs = document.getElementsByTagAndClassName('a', 'bug');
  
  for (var i=0; i<bugs.length; i++) {
    var classList = new Array();
    classList = bugs[i].className.split(' ');
    for (var n=0; n<classList.length; n++) {
      if (classList[n].match(userType))
        bugs[i].style.display = (bugs[i].style.display == "none") ? "inline" : "none";
    }
  }
  
  toggleExpand(obj, null, "Show notes", "Hide notes");
  
  _ldc.hide();
  
  return false;
}

function getAnnotationEl(annotationId) {
  var elements = document.getElementsByTagAndAttributeName('a', 'displayid');
     
  var targetEl
  for (var i=0; i<elements.length; i++) {
    var elDisplay = topaz.domUtil.getDisplayId(elements[i]);
    var displayList = elDisplay.split(',');

    for (var n=0; n<displayList.length; n++) {
      if (displayList[n] == annotationId) {
        targetEl = elements[i];
        return targetEl;
      }
    }
    
  }
  
  return false;
}

var elLocation;
function jumpToAnnotation(annotationId) {
  var targetEl = getAnnotationEl(annotationId);

  jumpToElement(targetEl);
}

function jumpToElement(elNode) {
  if (elNode) {
    elLocation = topaz.domUtil.getCurrentOffset(elNode);
    window.scrollTo(0, elLocation.top);
  }
}

function toggleExpand(obj, isOpen, textOn, textOff) {
  if (isOpen == false) {
    obj.className = obj.className.replace(/collapse/, "expand");
    if (textOn) dojox.data.dom.textContent(obj, textOn);
  }
  else if (obj.className.match('collapse')) {
    obj.className = obj.className.replace(/collapse/, "expand");
    if (textOn) dojox.data.dom.textContent(obj, textOn);
  }
  else {
    obj.className = obj.className.replace(/expand/, "collapse");
    if (textOff) dojox.data.dom.textContent(obj, textOff);
  }
  
}

function showAnnotationDialog() {
   // reset
  _noteType.selectedIndex = 0;
  dojo.byId('cdls').style.visibility = 'hidden';
  _dlg.show();
}

function validateNewComment() {
  var submitMsg = dojo.byId('submitMsg');
  topaz.domUtil.removeChildren(submitMsg);
  topaz.formUtil.disableFormFields(_annotationForm);
  
  _ldc.show();
  dojo.xhrPost({
     url: _namespace + "/annotation/secure/createAnnotationSubmit.action",
     handleAs:'json',
     form: _annotationForm,
     error: function(response, ioArgs){
       handleXhrError(response, ioArgs);
       topaz.formUtil.enableFormFields(_annotationForm);
     },
     load: function(response, ioArgs){
       var jsonObj = response;
       if(jsonObj.actionErrors.length > 0) {
         var errorMsg = "";
         for (var i=0; i<jsonObj.actionErrors.length; i++) {
           errorMsg += jsonObj.actionErrors[i] + "\n";
         }
         var err = document.createTextNode(errorMsg);
         submitMsg.appendChild(err);
         topaz.formUtil.enableFormFields(_annotationForm);
         _dlg.placeModalDialog();
         _ldc.hide();
         
         return false;
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
         topaz.formUtil.enableFormFields(_annotationForm);
         _dlg.placeModalDialog();
         _ldc.hide();
       }
       else {
         getArticle();
         _dlg.hide();
         topaz.formUtil.textCues.reset(_commentTitle, _titleCue);
         topaz.formUtil.textCues.reset(_comments, _commentCue);
         topaz.formUtil.enableFormFields(_annotationForm);
       }
      }
  });
}  

/**
 * getArticle
 * 
 * Re-fetches the article from the server 
 * refreshing the article content area(s) of the page.
 */
function getArticle(refreshType) {
  var refreshArea = dojo.byId(annotationConfig.articleContainer);
  var targetUri = _annotationForm.target.value;
  _ldc.show();
  dojo.xhrGet({
    url: _namespace + "/article/fetchBody.action?articleURI=" + targetUri,
    handleAs:'text',
    error: function(response, ioArgs){
      handleXhrError(response);
    },
    load: function(response, ioArgs){
      refreshArea.innerHTML = response;
      
      if (refreshType  == "rating") {
        refreshRating(targetUri);
     	}
     	else {
	      getAnnotationCount();
     	}
     	
      topaz.displayComment.processBugCount();
      
      // re-apply corrections
      topaz.corrections.apply();
      
      _ldc.hide();

      return false;
    }
   });
}

function getAnnotationCount() {
  var refreshArea1 = dojo.byId(annotationConfig.rhcCount + "1");
  var refreshArea2 = dojo.byId(annotationConfig.rhcCount + "2");
  var targetUri = _annotationForm.target.value;
  dojo.xhrGet({
    url: _namespace + "/article/fetchArticleRhc.action?articleURI=" + targetUri,
    handleAs:'text',
    error: function(response, ioArgs){
      handleXhrError(response, ioArgs);
    },
    load: function(response, ioArgs){
      var docFragment = document.createDocumentFragment();
      docFragment = response;
      refreshArea1.innerHTML = docFragment;
      refreshArea2.innerHTML = docFragment;
      return false;
    }
   });
}