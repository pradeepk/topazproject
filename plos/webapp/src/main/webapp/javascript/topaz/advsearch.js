dojo.provide("topaz.advsearch");

/**
  * topaz.advsearch
  *
  * Advanced search methods.
  * 
  * @author  jkirton            jopaki@gmail.com
  **/
topaz.advsearch = new Object();

// config
advsrchConfig = {
  idAuthNmePrototype:'as_anp',
  idOlAuthNmes:'as_ol_an',
  idInptAuthNme:'authorName',

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
  
  maxNumAuthNames: 10
};

topaz.advsearch = {
  authNmeProto:null,
  olAuthNmes:null,
  liAuthNmeOptions:null,
   
  init: function() {
    // search by author section...
    topaz.advsearch.authNmeProto = dojo.byId(advsrchConfig.idAuthNmePrototype);
    topaz.advsearch.olAuthNmes = dojo.byId(advsrchConfig.idOlAuthNmes);
    topaz.advsearch.liAuthNmeOptions = dojo.html.getElementsByClass('options', topaz.advsearch.olAuthNmes)[0];

    // dates section...
    dojo.event.connect(dojo.byId(advsrchConfig.idPublishDate), "onchange", topaz.advsearch.onChangePublishDate);

    // date part comment cue event bindings...
    var year1 = dojo.byId('range1');
    var month1 = dojo.byId('range-m1');
    var day1 = dojo.byId('range-d1');
    var year2 = dojo.byId('range2');
    var month2 = dojo.byId('range-m2');
    var day2 = dojo.byId('range-d2');
    
    dojo.event.connect(year1, "onfocus", topaz.advsearch.onFocusCommentCueInputHandler);
    dojo.event.connect(month1, "onfocus", topaz.advsearch.onFocusCommentCueInputHandler);
    dojo.event.connect(day1, "onfocus", topaz.advsearch.onFocusCommentCueInputHandler);
    
    dojo.event.connect(year1, "onblur", topaz.advsearch.onBlurCommentCueInputHandler);
    dojo.event.connect(month1, "onblur", topaz.advsearch.onBlurCommentCueInputHandler);
    dojo.event.connect(day1, "onblur", topaz.advsearch.onBlurCommentCueInputHandler);
    
    dojo.event.connect(year2, "onfocus", topaz.advsearch.onFocusCommentCueInputHandler);
    dojo.event.connect(month2, "onfocus", topaz.advsearch.onFocusCommentCueInputHandler);
    dojo.event.connect(day2, "onfocus", topaz.advsearch.onFocusCommentCueInputHandler);
    
    dojo.event.connect(year2, "onblur", topaz.advsearch.onBlurCommentCueInputHandler);
    dojo.event.connect(month2, "onblur", topaz.advsearch.onBlurCommentCueInputHandler);
    dojo.event.connect(day2, "onblur", topaz.advsearch.onBlurCommentCueInputHandler);
    
    // subject categories section...
    //dojo.event.connect(dojo.byId(advsrchConfig.idSubjCatsAll), "onchange", topaz.advsearch.onChangeSubjectCategories);
    //dojo.event.connect(dojo.byId(advsrchConfig.idSubjCatsSlct), "onchange", topaz.advsearch.onChangeSubjectCategories);
    
    //topaz.advsearch.toggleSubjCategories();
  },
  
  getCueText: function(inptId) {
    if(inptId.indexOf(advsrchConfig.idMonthPart) > 0) {
      return advsrchConfig.monthCue;
    }
    else if(inptId.indexOf(advsrchConfig.idDayPart) > 0) {
      return advsrchConfig.dayCue;
    }
    else {
      return advsrchConfig.yearCue;
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
    var show = (slct.options[slct.selectedIndex].text.indexOf('Specify ') == 0);
    dojo.byId(advsrchConfig.idPubDateOptions).style.display = (show ? '' : 'none');
    dojo.event.browser.stopEvent(e);
    return false;
  },

  /*
  onChangeSubjectCategories: function(e) {
    alert('onChangeSubjectCategories');
    topaz.advsearch.toggleSubjCategories();
    //dojo.event.browser.stopEvent(e);
    return true;
  },
  */
  
  toggleSubjCategories: function() {
    var rbAll = dojo.byId(advsrchConfig.idSubjCatsAll);
    var rbSlct = dojo.byId(advsrchConfig.idSubjCatsSlct);
    var enable = rbSlct.checked;
    var fs = dojo.byId(advsrchConfig.idFsSubjectCatOpt);
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
    var inpt = dojo.byId(this._assembleId(advsrchConfig.idInptAuthNme, num));
    if(inpt.value == '') {
      this._handleAddError('Specify an Author Name to add another.', inpt);
      return;
    }
    else if(num >= advsrchConfig.maxNumAuthNames) {
      this._handleAddError('Only ' + advsrchConfig.maxNumAuthNames + ' Author Names are allowed.', inpt);
      return;
    }
    lnkAddCrnt.style.display = 'none';
    var spnSpcr = dojo.byId(this._assembleId(advsrchConfig.idSpnSpcr, num));
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
    
    var spnRmv = dojo.byId(this._assembleId(advsrchConfig.idSpnRmvAuthNme, num));
    var lnkRmv = dojo.byId(this._assembleId(advsrchConfig.idLnkRmvAuthNme, num));
    var lnkAdd = dojo.byId(this._assembleId(advsrchConfig.idLnkAddAuthNme, num));
    spnSpcr = dojo.byId(this._assembleId(advsrchConfig.idSpnSpcr, num));

    spnRmv.style.display = '';
    lnkAdd.style.display = '';
    spnSpcr.style.display = '';
  },
  
  rmvAuthName: function(lnkRmvCrnt) {
    var num = this._getAuthNmeNum(lnkRmvCrnt);
    if(num < 1) return;
 
    // seek the parent li node to remove
    var liToRmv = lnkRmvCrnt;
    while(liToRmv.nodeName != 'LI') liToRmv = liToRmv.parentNode;
    
    // un-bind event handlers
    var lnkRmv = dojo.byId(this._assembleId(advsrchConfig.idLnkRmvAuthNme, num));
    var lnkAdd = dojo.byId(this._assembleId(advsrchConfig.idLnkAddAuthNme, num));

    var isLast = dojo.html.hasClass(liToRmv.nextSibling, 'options');

    // restore links above
    if(--num == 1) {
      if(isLast) {
        lnkAdd = dojo.byId(this._assembleId(advsrchConfig.idLnkAddAuthNme, num));
        lnkAdd.style.display = '';
      }
    }
    else {
      lnkRmv = dojo.byId(this._assembleId(advsrchConfig.idLnkRmvAuthNme, num));
      lnkRmv.style.display = '';
      var isLast = dojo.html.hasClass(liToRmv.nextSibling, 'options');
      if(isLast) {
        spnSpcr = dojo.byId(this._assembleId(advsrchConfig.idSpnSpcr, num));
        spnSpcr.style.display = '';
        lnkAdd = dojo.byId(this._assembleId(advsrchConfig.idLnkAddAuthNme, num));
        lnkAdd.style.display = '';
      }
    }
    
    // kill it
    dojo.dom.destroyNode(dojo.dom.removeNode(liToRmv));
    
    // reset the ids
    var cns = this.olAuthNmes.getElementsByTagName('li');
    for(var i=1; i<cns.length; i++) {
      var li = cns[i];
      if(dojo.html.hasClass(li, 'options')) break;
      this._setAuthNmeCopyIds(li, i+1);
    }
  }
  
};
dojo.addOnLoad(topaz.advsearch.init);