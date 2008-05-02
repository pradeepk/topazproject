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
  * topaz.corrections
  *
  * Decorates an article with correction specific markup. 
  * 
  * @author jkirton (jopaki@gmail.com)
  **/
dojo.provide("topaz.corrections");
topaz.corrections = {
  aroot: null, // the top-most element of the article below which corrections are applied
  fch: null,
  fclist: null, // the formal corrections ordered list element ref

  arrElmMc:null, // array of the minor correction elements for the article
  arrElmFc:null, // array of the formal correction elements for the article
  
  _num: function(arr) { return arr == null ? 0 : arr.length; },

  numMinorCrctns: function() { return this._num(this.arrElmMc); },
  numFormalCrctns: function() { return this._num(this.arrElmFc); },

  /**
   * Removes any existing formal correction entries from the formal correction header.
   */
  _clearFCEntries: function() {
    topaz.domUtil.removeChildren(this.fclist);
    // TODO handle IE memory leaks
  },

  /**
   * topaz.corrections.apply
   *
   * Applies correction specific decorations to the article
   */
  apply: function() {
    // [re-]identify node refs (as the article container is subject to refresh)
    this.aroot = dojo.byId(annotationConfig.articleContainer);
    this.fch = dojo.byId(formalCorrectionConfig.fchId);
    this.fclist = dojo.byId(formalCorrectionConfig.fcListId);

    this.arrElmMc = dojo.query('.'+annotationConfig.styleMinorCorrection, this.aroot);
    this.arrElmFc = dojo.query('.'+annotationConfig.styleFormalCorrection, this.aroot);

    this._clearFCEntries();
    var show = (this.numFormalCrctns() > 0);
    if(show) {
      // [re-]fetch the formal corrections for the article
      var targetUri = _annotationForm.target.value;
      _ldc.show();
      dojo.xhrGet({
        url: _namespace + "/annotation/getFormalCorrections.action?target=" + targetUri,
        handleAs:'json',
        error: function(response, ioArgs){
          handleXhrError(response, ioArgs);
        },
        load: function(response, ioArgs) {
          var jsonObj = response;
          if (jsonObj.actionErrors.length > 0) {
            var errorMsg = "";
            for (var i=0; i<jsonObj.actionErrors.length; i++) {
              errorMsg = errorMsg + jsonObj.actionErrors[i] + "\n";
            }
            alert("ERROR [actionErrors]: " + errorMsg);
          }
          else {
            // success
            topaz.corrections.addFormalCorrections(jsonObj.formalCorrections);
          }
          _ldc.hide();
        }
      });
    }
    this.fch.style.display = show? '' : 'none';

  },//apply

  /**
   * truncateFcText
   *
   * Truncates the comment of formal correction 
   * suitable for display in the formal correction header.
   *
   * @param etc The formal correction escaped truncated comment as a string 
   *            with expected format of: "<p>{markup}</p>"
   * @return String of HTML markup (suitable for innerHTML) 
   */
  truncateFcText: function(etc) {
    if(!etc) return "";
    // currently we grab the first paragraph
    var node = document.createElement('span');
    node.innerHTML = etc;
    node = node.childNodes[0];
    return '<p>' + topaz.domUtil.findTextNode(node,true).nodeValue + '</p>';
  },

  /**
   * _toLi
   *
   * Creates an html li element for the given formal correction 
   * used in the ordered list w/in the formal correction header.
   *
   * @param fc formal correction json obj
   * @retun li element
   */
  _toLi: function(fc) {
    var li = document.createElement('li');
    li.innerHTML = this.truncateFcText(fc.escapedTruncatedComment);
    var p = li.firstChild;
    var tn = p.firstChild;
    tn.nodeValue = tn.nodeValue + ' (';
    
    var a = document.createElement('a');
    a.setAttribute('href', '#');
    a.setAttribute(formalCorrectionConfig.annid, fc.id);
    a.innerHTML = 'read formal correction';
    dojo.connect(a, "onclick", topaz.corrections.onClickFC);
    p.appendChild(a);
    p.appendChild(document.createTextNode(')'));
    return li;
  },

  /**
   * addFormalCorrections
   *
   * Adds formal corrections to the formal correction header.
   *
   * @param arr Array of formal corrections
   * @return void
   */
  addFormalCorrections: function(arr) {
    for(var i=0; i<arr.length; i++) this.fclist.appendChild(this._toLi(arr[i]));
  },

  /**
   * _findFrmlCrctnByAnnId
   *
   * Finds a formal correction node given an annotation id
   * by searching the formal corrections node array property of this object
   *
   * @param annId The annotation (guid) id
   * @return The found formal correction node or null if not found
   */
  _findFrmlCrctnByAnnId: function(annId) {
    if(this.arrElmFc == null || annId == null) return null;
    var n, naid;
    for(var i=0; i<this.arrElmFc.length; i++){
      n = this.arrElmFc[i];
      naid = dojo.attr(n, 'annotationid');
      if(naid != null && naid.indexOf(annId)>=0) return n;
    }
    return null;
  },
  
  _getAnnAnchor: function(ancestor) {
  	var cns = ancestor.childNodes;
  	if(!cns || cns.length < 1) return null;
  	var cn;
  	for(var i=0; i<cns.length; i++) {
  		cn = cns[i];
  		if(cn.nodeName == 'A') return cn;
  	}
  	return null;
  },

  /**
   * onClickFC
   *
   * Event handler for links in the formal correctionn header's ordered list of formal corrections.
   *
   * Scrolls into view the portion of the article containing the given correction (annotation) id
   * then opens the note (comment) window for the bound bug.
   *
   * @param e event
   */
  onClickFC: function(e) {
    var annId = dojo.attr(e.target, formalCorrectionConfig.annid);
    e.preventDefault();
    var fcn = topaz.corrections._findFrmlCrctnByAnnId(annId);
    if(fcn) {
	    var annAnchor = topaz.corrections._getAnnAnchor(fcn);
	    if(!annAnchor) throw 'Unable to resolve annotation anchor!';
	    topaz.displayComment.show(annAnchor);
	  }
  }
}