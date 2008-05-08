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


// the "loading..." widget
var _ldc;

// rating related globals
var ratingConfig =  {
  insight:  "rateInsight",
  reliability: "ratingReliability",
  style: "rateStyle",
  ratingContainer: "ratingRhc"
};
var _ratingDlg;
var _ratingsForm;
var _ratingTitle;
var _ratingComments;

// annotation related globals
var annotationConfig = {
  articleContainer: "articleContainer",
  rhcCount: "dcCount",
  trigger: "addAnnotation",
  lastAncestor: "researchArticle",
  xpointerMarker: "xpt",
  // NOTE: 'note-pending' class is used to identify js-based annotation 
  //  related document markup prior to persisting the annotation
  annotationMarker: "note note-pending",
  pendingAnnotationMarker: 'note-pending',
  annotationImgMarker: "noteImg",
  regionalDialogMarker : "rdm",
  excludeSelection: "noSelect",
  tipDownDiv: "dTip",
  tipUpDiv: "dTipu",
  isAuthor: false,  //TODO: *** Default to false when the hidden input is hooked up.
  isPublic: true,
  dfltAnnSelErrMsg: 'This area of text cannot be notated.',
  annSelErrMsg: null,
  rangeInfoObj: new Object(),
  annTypeMinorCorrection: 'MinorCorrection',
  annTypeFormalCorrection: 'FormalCorrection',
  styleMinorCorrection: 'minrcrctn', // generalized css class name for minor corrections
  styleFormalCorrection: 'frmlcrctn' // generalized css class name for formal corrections
};
var formalCorrectionConfig = {
  styleFormalCorrectionHeader: 'fch', // css class name for the formal correction header node
  fchId: 'fch', // the formal correction header node dom id
  fcListId: 'fclist', // the formal correction header sub-node referencing the ordered list
  annid: 'annid' // dom node attribute name to use to store annotation ids 
};
var _annotationDlg;
var _annotationForm;
var _noteType;
var _commentTitle;
var _comments;

// comment/multi-comment globals
var commentConfig = {
  cmtContainer: "cmtContainer",
  sectionTitle: "viewCmtTitle",
  sectionDetail: "viewCmtDetail",  
  sectionComment: "viewComment", 
  sectionLink: "viewLink", 
  retrieveMsg: "retrieveMsg",  
  tipDownDiv: "cTip",
  tipUpDiv: "cTipu"
};  
var multiCommentConfig = {
  sectionTitle: "viewCmtTitle",
  sectionDetail: "viewCmtDetail",  
  sectionComment: "viewComment",  
  retrieveMsg: "retrieveMsg",  
  tipDownDiv: "mTip",
  tipUpDiv: "mTipu"
};  
var _commentDlg;
var _commentMultiDlg;

var _titleCue          = 'Enter your note title...';
var _commentCue        = 'Enter your note...';
var _ratingTitleCue    = 'Enter your comment title...';
var _ratingCommentCue   = 'Enter your comment...';

var elLocation;

/*
var activeToggleId = "";
var activeWidget = "";

function setActiveToggle(widgetId, boxId) {
  activeToggleId = boxId;
  activeWidget = dojo.byId(widgetId);
}

function singleView(obj) {
  if (activeToggleId != "") {
    ambra.domUtil.swapDisplayMode(activeToggleId, "none");
    toggleExpand(activeWidget, false); 
  }
}

function singleExpand(obj, targetId) {
  if (targetId != activeToggleId) {
    singleView(obj);
  }
  setActiveToggle
   (obj.id, targetId);
  ambra.domUtil.swapDisplayMode(targetId);
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
    var elDisplay = ambra.domUtil.getDisplayId(elements[i]);
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

function jumpToAnnotation(annotationId) {
  jumpToElement(getAnnotationEl(annotationId));
}

function jumpToElement(elNode) {
  if (elNode) {
    elLocation = ambra.domUtil.getCurrentOffset(elNode);
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
  _annotationDlg.show();
}

function validateNewComment() {
  var submitMsg = dojo.byId('submitMsg');
  ambra.domUtil.removeChildren(submitMsg);
  ambra.formUtil.disableFormFields(_annotationForm);
  
  _ldc.show();
  dojo.xhrPost({
     url: _namespace + "/annotation/secure/createAnnotationSubmit.action",
     handleAs:'json',
     form: _annotationForm,
     error: function(response, ioArgs){
       handleXhrError(response, ioArgs);
       ambra.formUtil.enableFormFields(_annotationForm);
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
         ambra.formUtil.enableFormFields(_annotationForm);
         _annotationDlg.placeModalDialog();
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
         ambra.formUtil.enableFormFields(_annotationForm);
         _annotationDlg.placeModalDialog();
         _ldc.hide();
       }
       else {
         getArticle();
         _annotationDlg.hide();
         ambra.formUtil.textCues.reset(_commentTitle, _titleCue);
         ambra.formUtil.textCues.reset(_comments, _commentCue);
         ambra.formUtil.enableFormFields(_annotationForm);
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
      
      ambra.displayComment.processBugCount();
      
      // re-apply corrections
      ambra.corrections.apply();
      
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

dojo.addOnLoad(function() {
  // int loading "throbber"
  _ldc = dijit.byId("LoadingCycle");
  alert('init_article _ldc: ' + _ldc);
  
  // show loading... while we initialize
  _ldc.show();
  
  // build RHC table of contents
  var tocObj = dojo.byId('sectionNavTop');
  ambra.navigation.buildTOC(tocObj);
  if (dojo.isSafari) {
    var tocObj = dojo.byId('sectionNavTopFloat');
    ambra.navigation.buildTOC(tocObj);
  }
  
  // ---------------------
  // rating dialog related
  // ---------------------
  _ratingsForm = document.ratingForm;
  _ratingTitle = _ratingsForm.cTitle;
  _ratingComments = _ratingsForm.cArea;
  _ratingDlg = dijit.byId("Rating");
  //_ratingDlg.setCloseControl(dojo.byId('btn_cancel_rating'));
  
  dojo.connect(_ratingTitle, "onfocus", function () { 
    ambra.formUtil.textCues.off(_ratingTitle, _ratingTitleCue); 
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
    ambra.formUtil.textCues.on(_ratingTitle, _ratingTitleCue); 
  });
  
  dojo.connect(_ratingComments, "onfocus", function () {
    ambra.formUtil.textCues.off(_ratingComments, _ratingCommentCue);
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
    ambra.formUtil.textCues.on(_ratingComments, _ratingCommentCue); 
    //ambra.formUtil.checkFieldStrLength(_ratingComments);
  });
  
  dojo.connect(dojo.byId("btn_post_rating"), "onclick", function(e) {
    updateRating();
    ambra.rating.resetDialog();
    e.preventDefault();
  });

  dojo.connect(dojo.byId("btn_cancel_rating"), "onclick", function(e) {
    ambra.domUtil.removeChildren(dojo.byId('submitMsg'));
    _ratingDlg.hide();
    ambra.formUtil.enableFormFields(_ratingsForm);
    ambra.rating.resetDialog();
    /* don't re-fetch article on cancel
    getArticle("rating");
    ambra.displayComment.processBugCount();
    */
    e.preventDefault();
  });
  
  // --------------------------------
  // annotation (note) dialog related
  // --------------------------------
  _annotationForm = document.createAnnotation;
  _noteType = _annotationForm.cNoteType;
  _commentTitle    = _annotationForm.cTitle;
  _comments        = _annotationForm.cArea;
  //var privateFlag  = _annotationForm.privateFlag;
  //var publicFlag   = _annotationForm.publicFlag;
  //var btnSave      = dojo.byId("btn_save");  
  
  dojo.connect(_noteType, "onchange", function () {
    dojo.byId('cdls').style.visibility = _noteType.value == 'correction' ? 'visible' : 'hidden';
    _annotationForm.noteType.value = _noteType.value;
  });
  
  dojo.connect(_commentTitle, "onfocus", function () { 
    ambra.formUtil.textCues.off(_commentTitle, _titleCue); 
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
    ambra.formUtil.textCues.on(_commentTitle, _titleCue); 
  });
  
  dojo.connect(_comments, "onfocus", function () {
    ambra.formUtil.textCues.off(_comments, _commentCue);
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
    ambra.formUtil.textCues.on(_comments, _commentCue); 
    //ambra.formUtil.checkFieldStrLength(_comments);
  });
  
  /*
  dojo.connect(privateFlag, "onclick", function() {
    ambra.formUtil.toggleFieldsByClassname('commentPrivate', 'commentPublic'); 
    _annotationDlg.placeModalDialog();
    //var btn = btnSave;
    //__annotationDlg.setCloseControl(btn);
  });
  dojo.connect(publicFlag, "onclick", function() {
    ambra.formUtil.toggleFieldsByClassname('commentPublic', 'commentPrivate'); 
    _annotationDlg.placeModalDialog();
    //_annotationDlg.setCloseControl(dojo.byId("btn_post"));
  });
  
  // Annotation Dialog Box: Save button
  dojo.connect(btnSave, "onclick", function(e) {
    //getCommentValues();
    validateNewComment();
    e.preventDefault();
  });
  */
  
  dojo.connect(dojo.byId("btn_post"), "onclick", function(e) {
    //getCommentValues();
    validateNewComment();
    e.preventDefault();
  });

  dojo.connect(dojo.byId("btn_cancel"), "onclick", function(e) {
    ambra.domUtil.removeChildren(dojo.byId('submitMsg'));
    _annotationDlg.hide();
    ambra.formUtil.enableFormFields(_annotationForm);
    if(!annotationConfig.rangeInfoObj.isSimpleText) {
      // we are in an INDETERMINISTIC state for annotation markup
      // Article re-fetch is necessary to maintain the integrity of the existing annotation markup
      getArticle();
      ambra.displayComment.processBugCount();
    }
    else {
      // we can safely rollback the pending annotation markup from the dom
      ambra.annotation.undoPendingAnnotation();
    }
    e.preventDefault();
  });

  ambra.formUtil.toggleFieldsByClassname('commentPublic', 'commentPrivate');
  
  _annotationDlg = dijit.byId("AnnotationDialog");
  var dlgCancel = dojo.byId('btn_cancel');
  //_annotationDlg.setCloseControl(dlgCancel);
  _annotationDlg.setTipDown(dojo.byId(annotationConfig.tipDownDiv));
  _annotationDlg.setTipUp(dojo.byId(annotationConfig.tipUpDiv));

  
  // -------------------------
  // comment dialog related
  // -------------------------
  _commentDlg = dijit.byId("CommentDialog");
  var commentDlgClose = dojo.byId('btn_close');
  //_commentDlg.setCloseControl(commentDlgClose);
  _commentDlg.setTipDown(dojo.byId(commentConfig.tipDownDiv));
  _commentDlg.setTipUp(dojo.byId(commentConfig.tipUpDiv));
  
  dojo.connect(commentDlgClose, 'onclick', function(e) {
    _commentDlg.hide();
    ambra.displayComment.mouseoutComment(ambra.displayComment.target);
  });
  dojo.connect(commentDlgClose, 'onblur', function(e) {
    ambra.displayComment.mouseoutComment(ambra.displayComment.target);
  });
  
  // -------------------------
  // multi-comment dialog related
  // -------------------------
  _commentMultiDlg = dijit.byId("CommentDialogMultiple");
  var popupCloseMulti = dojo.byId('btn_close_multi');
  //_commentMultiDlg.setCloseControl(popupCloseMulti);
  _commentMultiDlg.setTipDown(dojo.byId(multiCommentConfig.tipDownDiv));
  _commentMultiDlg.setTipUp(dojo.byId(multiCommentConfig.tipUpDiv));
  
  dojo.connect(popupCloseMulti, 'onclick', function(e) {
    _commentMultiDlg.hide();
    ambra.displayComment.mouseoutComment(ambra.displayComment.target);
  });
  dojo.connect(popupCloseMulti, 'onblur', function(e) {
    ambra.displayComment.mouseoutComment(ambra.displayComment.target);
  });
  
  // init routines
  ambra.rating.init();
  ambra.displayComment.init();
  ambra.displayComment.processBugCount();
  ambra.corrections.apply();

  // jump to annotation?
  var anId = document.articleInfo.annotationId.value;
  if(anId) jumpToAnnotation(anId);
  
  // initialization complete
  _ldc.hide();
});
