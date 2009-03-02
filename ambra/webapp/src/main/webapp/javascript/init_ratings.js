// rating related globals
var ratingConfig =  {
  insight:  "rateInsight",
  reliability: "ratingReliability",
  style: "rateStyle"
};
var _ratingDlg;
var _ratingsForm;
var _ratingTitle;
var _ratingComments;

var _ratingTitleCue    = 'Enter your comment title...';
var _ratingCommentCue   = 'Enter your comment...';

dojo.addOnLoad(function() {
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
    //ambra.formUtil.checkFieldStrLength(_ratingComments, 500);
  });

  dojo.connect(dojo.byId("btn_post_rating"), "onclick", function(e) {
    updateRating();
    e.preventDefault();
    return false;
  });

  dojo.connect(dojo.byId("btn_cancel_rating"), "onclick", function(e) {
    ambra.rating.hide();
    e.preventDefault();
    return false;
  });
});