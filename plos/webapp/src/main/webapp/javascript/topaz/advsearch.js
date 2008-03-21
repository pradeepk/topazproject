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
  idAuthNmePrototype: 'as_anp',
  idOlAuthNmes: 'as_ol_an',
  idInptAuthNme: 'authorName',

  idSpnRmvAuthNme: 'as_spn_ra',
  idLnkRmvAuthNme: 'as_a_ra',
  idLnkAddAuthNme: 'as_a_aa',
  
  maxNumAuthNames: 10
};

topaz.advsearch = {
  authNmeProto:null,
  olAuthNmes:null,
   
  init: function() {
    topaz.advsearch.authNmeProto = dojo.byId(advsrchConfig.idAuthNmePrototype);
    topaz.advsearch.olAuthNmes = dojo.byId(advsrchConfig.idOlAuthNmes);

    // add on click handler for add another author link
    var id = topaz.advsearch._assembleId(advsrchConfig.idLnkAddAuthNme);
    var lnk = dojo.byId(id);
    dojo.event.connect(lnk, "onclick", topaz.advsearch.onClickAddAuthNameHandler);
  },
  
  // get the 1-based ordinal number for the author name list element associated with 
  // a given child element  
  _getAuthNmeNum: function(child) {
    var id = child.id;
    var num;
    var indx = id.lastIndexOf('_');
    if(indx > 0) {
      var num = parseInt(id.substr(indx+1));
      return isNaN(num) ? 1 : num;
    }
    return 1;
  },
  
  _assembleId: function(idTmplte, num) {
    return (!num || num == 1) ? idTmplte : (idTmplte + '_' + num);
  },

  // resursively sets the relevant ids in a just cloned auth name node set  
  _setAuthNmeCopyIds: function(n, num) {
    if(n.id && n.id.length > 0) n.id = n.id + '_' + num;
    if(n.nodeType == 1 && n.childNodes > 0) {
      var cns = n.childNodes;
      for(var i=0; i<cns.length; i++) this._setIds(cns[i], num);
    }
  },

  // handles adding additional auth names 
  onClickAddAuthNameHandler: function(e) {
    var lnk = e.target;
    var num = topaz.advsearch._getAuthNmeNum(lnk);
    var inptId = topaz.advsearch._assembleId(advsrchConfig.idInptAuthNme, num);
    var inpt = dojo.byId(inptId);
    var okToAdd = (inpt.value != '' && num < advsrchConfig.maxNumAuthNames);
    if(okToAdd) topaz.advsearch.addAuthName(num+1);
    dojo.event.browser.stopEvent(e);
  },
  
  addAuthName: function(num) {
    var cln = this.authNmeProto.cloneNode(true);
    this._setAuthNmeCopyIds(cln, num);
  },
  
  rmvAuthName: function() {
  }
  
};
dojo.addOnLoad(topaz.advsearch.init);