/*
 * $HeadURL:: http://gandalf/svn/head/topaz/core/src/main/java/org/topazproject/otm/Abst#$
 * $Id: AbstractConnection.java 4807 2008-02-27 11:06:12Z ronald $
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
 * topaz.advsearch
 * Advanced search methods.
 * @author jkirton (jopaki@gmail.com)
 **/
dojo.provide("topaz.advsearch");
topaz.advsearch = {
	Config: {
	  idAuthNmePrototype:'as_anp',
	  idOlAuthNmes:'as_ol_an',
	  idInptAuthNme:'authorName',
	  idLiAuthNmesOpts:'as_an_opts',
	
	  idSpnRmvAuthNme:'as_spn_ra',
	  idLnkRmvAuthNme:'as_a_ra',
	  idLnkAddAuthNme:'as_a_aa',
	  idSpnSpcr:'as_a_spcr',
	  
	  // date part input fields template identifiers
	  idMonthPart:'-m',
	  idDayPart:'-d',
	  
	  yearCue:'YYYY',
	  monthCue:'MM',
	  dayCue:'DD',
	  
	  idPublishDate:'dateSelect',
	  idPubDateOptions:'pubDateOptions',
	  
	  idSubjCatsAll:'subjectCatOpt_all',
	  idSubjCatsSlct:'subjectCatOpt_slct',
	  idFsSubjectCatOpt:'fsSubjectCatOpt',
	  
	  maxNumAuthNames: 10,
	  
	  pubDateYearRange: $R(1900, 2008),
	  monthRange: $R(1, 12),
	  dayRange: $R(1, 31)
	},
  authNmeProto:null,
  olAuthNmes:null,
  liAuthNmeOptions:null,
  liAuthNmesOpts:null,
   
  init: function() {
    // search by author section...
    topaz.advsearch.authNmeProto = dojo.byId(topaz.advsearch.Config.idAuthNmePrototype);
    topaz.advsearch.olAuthNmes = dojo.byId(topaz.advsearch.Config.idOlAuthNmes);
    topaz.advsearch.liAuthNmeOptions = dojo.query('.options', topaz.advsearch.olAuthNmes)[0];
    topaz.advsearch.liAuthNmesOpts = dojo.byId(topaz.advsearch.Config.idLiAuthNmesOpts);

    // dates section...
    var slct = dojo.byId(topaz.advsearch.Config.idPublishDate);
    var showDates = (slct.options[slct.selectedIndex].value == 'range');
    dojo.byId(topaz.advsearch.Config.idPubDateOptions).style.display = showDates ? '' : 'none';
    dojo.connect(dojo.byId(topaz.advsearch.Config.idPublishDate), "onchange", topaz.advsearch.onChangePublishDate);

    // date part comment cue event bindings...
    var year1 = dojo.byId('range1');
    var month1 = dojo.byId('range-m1');
    var day1 = dojo.byId('range-d1');
    var year2 = dojo.byId('range2');
    var month2 = dojo.byId('range-m2');
    var day2 = dojo.byId('range-d2');
    
    dojo.connect(year1, "onfocus", topaz.advsearch.onFocusCommentCueInputHandler);
    dojo.connect(month1, "onfocus", topaz.advsearch.onFocusCommentCueInputHandler);
    dojo.connect(day1, "onfocus", topaz.advsearch.onFocusCommentCueInputHandler);
    
    dojo.connect(year1, "onblur", topaz.advsearch.onBlurCommentCueInputHandler);
    dojo.connect(month1, "onblur", topaz.advsearch.onBlurCommentCueInputHandler);
    dojo.connect(day1, "onblur", topaz.advsearch.onBlurCommentCueInputHandler);
    
    dojo.connect(year1, "onkeyup", topaz.advsearch.onKeyUpCommentCueInputHandler);
    dojo.connect(month1, "onkeyup", topaz.advsearch.onKeyUpCommentCueInputHandler);
    dojo.connect(day1, "onkeyup", topaz.advsearch.onKeyUpCommentCueInputHandler);
    
    dojo.connect(year2, "onfocus", topaz.advsearch.onFocusCommentCueInputHandler);
    dojo.connect(month2, "onfocus", topaz.advsearch.onFocusCommentCueInputHandler);
    dojo.connect(day2, "onfocus", topaz.advsearch.onFocusCommentCueInputHandler);
    
    dojo.connect(year2, "onblur", topaz.advsearch.onBlurCommentCueInputHandler);
    dojo.connect(month2, "onblur", topaz.advsearch.onBlurCommentCueInputHandler);
    dojo.connect(day2, "onblur", topaz.advsearch.onBlurCommentCueInputHandler);
    
    // subject categories section...
    if(document.selection) {
      // IE
      dojo.connect(dojo.byId(topaz.advsearch.Config.idSubjCatsAll), "onfocus", topaz.advsearch.onChangeSubjectCategories);
      dojo.connect(dojo.byId(topaz.advsearch.Config.idSubjCatsSlct), "onfocus", topaz.advsearch.onChangeSubjectCategories);
    } else {
      // gecko et al.
      dojo.connect(dojo.byId(topaz.advsearch.Config.idSubjCatsAll), "onchange", topaz.advsearch.onChangeSubjectCategories);
      dojo.connect(dojo.byId(topaz.advsearch.Config.idSubjCatsSlct), "onchange", topaz.advsearch.onChangeSubjectCategories);
    }
    
    // hijack form submission for validation...
    dojo.connect(dojo.byId('button-search'), "onclick", topaz.advsearch.onSubmitHandler);

    topaz.advsearch.liAuthNmesOpts.style.display = 'none';
    topaz.advsearch.tglSubCategories();
    
    topaz.advsearch.explodeAuthNames();
  },
  
  onSubmitHandler: function(e) {
    topaz.advsearch.handleSubmit();
    dojo.event.browser.stopEvent(e);
    return false;
  },
  
  handleSubmit: function() {
    var errs = this.validate();
    if(errs.length > 0) {
      var msg = '';
      for(var i=0; i<errs.length; i++) {
        msg += errs[i] + '\r\n';
      }
      alert(msg);
      return;
    }
    document.advSearchForm.submit();
  },
  
  validateDateNum: function(val, range, nme, errs) {
    if(val == '' || val == topaz.advsearch.Config.yearCue || val == topaz.advsearch.Config.monthCue || val == topaz.advsearch.Config.dayCue) {
      errs.push(nme + ' must be specified.');
    }
    else if(isNaN(parseInt(val))) {
      errs.push('Invalid ' + nme);
    }
    else {
      if(!range.include(parseInt(val))) {
        errs.push(nme + ' must be between ' + range.start + ' and ' + range.end);
      }
    }
  },
  
  // validates the adv search form data
  // returns array of error messages
  validate: function() {
    var errs = [];
    
    // validate published date range (if applicable)
    var slct = dojo.byId(topaz.advsearch.Config.idPublishDate);
    if(slct.options[slct.selectedIndex].value == 'range') {
      var year1 = dojo.byId('range1');
      var month1 = dojo.byId('range-m1');
      var day1 = dojo.byId('range-d1');
      var year2 = dojo.byId('range2');
      var month2 = dojo.byId('range-m2');
      var day2 = dojo.byId('range-d2');
      
      this.validateDateNum(year1.value, topaz.advsearch.Config.pubDateYearRange, 'Publish Date from Year', errs);
      this.validateDateNum(month1.value, topaz.advsearch.Config.monthRange, 'Publish Date from Month', errs);
      this.validateDateNum(day1.value, topaz.advsearch.Config.dayRange, 'Publish Date from Day', errs);
      
      this.validateDateNum(year2.value, topaz.advsearch.Config.pubDateYearRange, 'Publish Date to Year', errs);
      this.validateDateNum(month2.value, topaz.advsearch.Config.monthRange, 'Publish Date to Month', errs);
      this.validateDateNum(day2.value, topaz.advsearch.Config.dayRange, 'Publish Date to Day', errs);
    }
 
    return errs;
  },
  
  getCueText: function(inptId) {
    if(inptId.indexOf(topaz.advsearch.Config.idMonthPart) > 0) {
      return topaz.advsearch.Config.monthCue;
    }
    else if(inptId.indexOf(topaz.advsearch.Config.idDayPart) > 0) {
      return topaz.advsearch.Config.dayCue;
    }
    else {
      return topaz.advsearch.Config.yearCue;
    }
  },
  
  onFocusCommentCueInputHandler: function(e) {
    topaz.advsearch.onFocusCommentCueInput(e.target);
    dojo.event.browser.stopEvent(e);
    return false;
  },
  
  onBlurCommentCueInputHandler: function(e) {
    topaz.advsearch.onBlurCommentCueInput(e.target);
    dojo.event.browser.stopEvent(e);
    return false;
  },
  
  onBlurCommentCueInput: function(inpt) {
    if(inpt.value == '') inpt.value = this.getCueText(inpt.id);
  },
  
  onFocusCommentCueInput: function(inpt) {
    if(inpt.value == this.getCueText(inpt.id)) inpt.value = '';
  },
  
  onChangePublishDate: function(e) {
    var slct = e.target;
    var show = (slct.options[slct.selectedIndex].value == 'range');
    dojo.byId(topaz.advsearch.Config.idPubDateOptions).style.display = (show ? '' : 'none');
    dojo.event.browser.stopEvent(e);
    return false;
  },

  onChangeSubjectCategories: function(e) {
    topaz.advsearch.tglSubCategories();
    dojo.event.browser.stopEvent(e);
    return true;
  },
  
  tglSubCategories: function() {
    var rbAll = dojo.byId(topaz.advsearch.Config.idSubjCatsAll);
    var rbSlct = dojo.byId(topaz.advsearch.Config.idSubjCatsSlct);
    var enable = rbSlct.checked;
    var fs = dojo.byId(topaz.advsearch.Config.idFsSubjectCatOpt);
    if(enable) Form.enable(fs); else Form.disable(fs);
    rbSlct.disabled = '';
    rbAll.disabled = '';
  },

  // get the 1-based ordinal number for the author name list element associated with 
  // a given child element  
  _getAuthNmeNum: function(child) {
    var id = child.id;
    var num;
    var indx = id.lastIndexOf('__');
    if(indx > 0) {
      var num = parseInt(id.substr(indx+2));
      return isNaN(num) ? 1 : num;
    }
    return 1;
  },
  
  _assembleId: function(idTmplte, num) {
    return (!num || num == 1) ? idTmplte : (idTmplte + '__' + num);
  },

  // resursively sets the relevant ids for an auth name node set  
  _setAuthNmeCopyIds: function(n, num) {
    if(n.id && n.id.length > 0) {
      var idi = n.id.lastIndexOf('__');
      if(idi >0) n.id = n.id.substring(0, idi);
      n.id = n.id + '__' + num;
    }
    if(n.nodeType == 1 && n.childNodes.length > 0) {
      var cns = n.childNodes;
      for(var i=0; i<cns.length; i++) this._setAuthNmeCopyIds(cns[i], num);
    }
  },

  // handles adding additional auth names 
  onClickAddAuthNameHandler: function(e) {
    dojo.event.browser.fixEvent(e);
    topaz.advsearch.addAuthName(e.target);
    dojo.event.browser.stopEvent(e);
  },
  
  // handles removing previously added auth names 
  onClickRmvAuthNameHandler: function(e) {
    dojo.event.browser.fixEvent(e);
    topaz.advsearch.rmvAuthName(e.target);
    dojo.event.browser.stopEvent(e);
  },
  
  _handleAddError: function(msg, elmInpt) {
    alert(msg);
    elmInpt.focus();
  },
  
  addAuthName: function(lnkAddCrnt) {
    var num = this._getAuthNmeNum(lnkAddCrnt);
    var inpt = dojo.byId(this._assembleId(topaz.advsearch.Config.idInptAuthNme, num));
    if(inpt.value == '') {
      this._handleAddError('Specify an Author Name to add another.', inpt);
      return;
    }
    else if(num >= topaz.advsearch.Config.maxNumAuthNames) {
      this._handleAddError('Only ' + topaz.advsearch.Config.maxNumAuthNames + ' Author Names are allowed.', inpt);
      return;
    }
    lnkAddCrnt.style.display = 'none';
    var spnSpcr = dojo.byId(this._assembleId(topaz.advsearch.Config.idSpnSpcr, num));
    spnSpcr.style.display = 'none';
    num++;

    var cln = this.authNmeProto.cloneNode(true);
    this._setAuthNmeCopyIds(cln, num);

    // clear input value in clone
    var inpt = cln.getElementsByTagName('input')[0];
    inpt.value = '';

    // insert clone under a new li tag under the auth names list
    var li = document.createElement('li');
    var lbl = document.createElement('label');
    lbl.appendChild(document.createTextNode(' '));
    li.appendChild(lbl);
    li.appendChild(cln);
    this.olAuthNmes.insertBefore(li, this.liAuthNmeOptions);
    inpt.focus();
    
    var spnRmv = dojo.byId(this._assembleId(topaz.advsearch.Config.idSpnRmvAuthNme, num));
    var lnkRmv = dojo.byId(this._assembleId(topaz.advsearch.Config.idLnkRmvAuthNme, num));
    var lnkAdd = dojo.byId(this._assembleId(topaz.advsearch.Config.idLnkAddAuthNme, num));
    spnSpcr = dojo.byId(this._assembleId(topaz.advsearch.Config.idSpnSpcr, num));

    spnRmv.style.display = '';
    lnkAdd.style.display = '';
    spnSpcr.style.display = '';
    
    this.liAuthNmesOpts.style.display = '';
  },
  
  rmvAuthName: function(lnkRmvCrnt) {
    var num = this._getAuthNmeNum(lnkRmvCrnt);
    if(num < 1) return;
 
    // seek the parent li node to remove
    var liToRmv = lnkRmvCrnt;
    while(liToRmv.nodeName != 'LI') liToRmv = liToRmv.parentNode;
    var isLast = dojo.hasClass(liToRmv.nextSibling, 'options');

    // restore links above
    if(--num == 1) {
      if(isLast) {
        lnkAdd = dojo.byId(this._assembleId(topaz.advsearch.Config.idLnkAddAuthNme, num));
        lnkAdd.style.display = '';
      }
    }
    else {
      var lnkRmv = dojo.byId(this._assembleId(topaz.advsearch.Config.idLnkRmvAuthNme, num));
      lnkRmv.style.display = '';
      var isLast = dojo.hasClass(liToRmv.nextSibling, 'options');
      if(isLast) {
        var spnSpcr = dojo.byId(this._assembleId(topaz.advsearch.Config.idSpnSpcr, num));
        spnSpcr.style.display = '';
        var lnkAdd = dojo.byId(this._assembleId(topaz.advsearch.Config.idLnkAddAuthNme, num));
        lnkAdd.style.display = '';
      }
    }
    
    // kill it
    liToRmv.parentNode.removeChild(liToRmv);
    
    // reset the ids
    var cns = this.olAuthNmes.getElementsByTagName('li');
    var num = 0;
    for(var i=1; i<cns.length; i++) {
      var li = cns[i];
      if(dojo.hasClass(li, 'options')) break;
      num++;
      this._setAuthNmeCopyIds(li, i+1);
    }
    
    this.liAuthNmesOpts.style.display = (num>0 ? '' : 'none');
  },
  
  // auto-adds auth name edit fields based on the current value in the initial auth name edit field
  explodeAuthNames: function() {
    var fan = dojo.byId(this._assembleId(topaz.advsearch.Config.idInptAuthNme));
    var auths = fan.value;
    if(!auths || auths.length < 1) return;
    var j, arr = auths.split(','), auth, lnkAdd;
    if(arr.length > 1) {
      for(var i=0; i<arr.length; i++) {
        auth = arr[i];
        j = i + 1;
        if(i>0) this.addAuthName(lnkAdd);
        lnkAdd = dojo.byId(this._assembleId(topaz.advsearch.Config.idLnkAddAuthNme, j));
        dojo.byId(this._assembleId(topaz.advsearch.Config.idInptAuthNme, j)).value = auth;
      }
    }
  }
  
};
dojo.addOnLoad(topaz.advsearch.init);