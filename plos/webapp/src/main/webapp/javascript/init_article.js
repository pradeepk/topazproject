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
  var _dlg;
  var _commentDlg;
  var _commentMultiDlg;
  var _ldc;
  var _annotationForm;
  var _ratingDlg;
  var _ratingsForm;
  
  dojo.addOnLoad(function() {
    _ldc = dijit.byId("LoadingCycle");
    //_ldc.show();
    
    var tocObj = dojo.byId('sectionNavTop');
    ambra.navigation.buildTOC(tocObj);
    
    if (dojo.isSafari) {
      var tocObj = dojo.byId('sectionNavTopFloat');
      ambra.navigation.buildTOC(tocObj);
    }
        
 		_annotationForm = document.createAnnotation;

  	_ratingDlg = dijit.byId("Rating");
  	var ratingCancel = dojo.byId('btn_cancel_rating');
  	//_ratingDlg.setCloseControl(ratingCancel);
  	_ratingsForm = document.ratingForm;

    initAnnotationForm();
    
    ambra.formUtil.toggleFieldsByClassname('commentPublic', 'commentPrivate');
    
  	_dlg = dijit.byId("AnnotationDialog");
  	var dlgCancel = dojo.byId('btn_cancel');
  	//_dlg.setCloseControl(dlgCancel);
  	_dlg.setTipDown(dojo.byId(annotationConfig.tipDownDiv));
  	_dlg.setTipUp(dojo.byId(annotationConfig.tipUpDiv));

  	_commentDlg = dijit.byId("CommentDialog");
  	var commentDlgClose = dojo.byId('btn_close');
    //_commentDlg.setCloseControl(commentDlgClose);
  	_commentDlg.setTipDown(dojo.byId(commentConfig.tipDownDiv));
  	_commentDlg.setTipUp(dojo.byId(commentConfig.tipUpDiv));
    dojo.connect(commentDlgClose, 'onclick', function(e) {
        ambra.displayComment.mouseoutComment(ambra.displayComment.target);
      }
    );
    dojo.connect(commentDlgClose, 'onblur', function(e) {
        ambra.displayComment.mouseoutComment(ambra.displayComment.target);
      }
    );
  	
  	_commentMultiDlg = dijit.byId("CommentDialogMultiple");
  	var popupCloseMulti = dojo.byId('btn_close_multi');
    //_commentMultiDlg.setCloseControl(popupCloseMulti);
  	_commentMultiDlg.setTipDown(dojo.byId(multiCommentConfig.tipDownDiv));
  	_commentMultiDlg.setTipUp(dojo.byId(multiCommentConfig.tipUpDiv));
    dojo.connect(popupCloseMulti, 'onclick', function(e) {
        ambra.displayComment.mouseoutComment(ambra.displayComment.target);
      }
    );
    dojo.connect(popupCloseMulti, 'onblur', function(e) {
        ambra.displayComment.mouseoutComment(ambra.displayComment.target);
      }
    );
    
    ambra.displayComment.init();
    ambra.displayComment.processBugCount();
    ambra.corrections.apply();
    //ambra.rating.init();

    var anId = document.articleInfo.annotationId.value;
    var anEl = getAnnotationEl(anId);
    jumpToAnnotation(anId);
    //_ldc.hide();
  });
