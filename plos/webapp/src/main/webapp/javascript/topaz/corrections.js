dojo.provide("topaz.corrections");

/**
  * topaz.corrections
  *
  * Decorates an article with correction specific markup. 
  * 
  * @author jkirton (jopaki@gmail.com)
  **/
topaz.corrections = new Object();

topaz.corrections = {
  style_minrcrctn: 'minrcrctn', // css class name for minor corrections
  style_frmlcrctn: 'frmlcrctn', // css class name for formal corrections
  style_fch: 'fch', // css class name for the formal correction header
  
  aroot: null, // the top-most element of the article below which corrections are applied
  fch: null, // the formal corrections header element ref
  fch: null, // the formal corrections header element ref
  fclist: null, // the formal corrections ordered list element ref

  arrElmMc:null, // array of the minor correction elements for the article
  arrElmFc:null, // array of the formal correction elements for the article
  
  init: function() {
    this.aroot = dojo.byId('articleContainer');
    this.fch = dojo.byId('fch');
    this.fclist = dojo.byId('fclist');

    // identify article corrections
    this.arrElmMc = dojo.html.getElementsByClass(this.style_minrcrctn, this.aroot);
    this.arrElmFc = dojo.html.getElementsByClass(this.style_frmlcrctn, this.aroot);
  },
  
  _num: function(arr) { return arr == null ? 0 : arr.length; },

  numMinorCrctns: function() { return this._num(this.arrElmMc); },
  numFormalCrctns: function() { return this._num(this.arrElmFc); },

  /**
   * Removes any existing formal correction entries from the formal correction header.
   */
  _clearFCEntries: function() {
    topaz.domUtil.removeChildNodes(this.fclist);
    // TODO handle IE memory leaks
  },

  /**
   * topaz.corrections.apply
   *
   * Applies correction specific decorations to the article
   */
  apply: function() {
    this._clearFCEntries();
    var show = (this.numFormalCrctns() > 0);
    if(show) {
      // add or update formal correction header
      if(!this.fch) this._clearFCEntries();
    }
    this.fch.style.display = show? '' : 'none';

  }//apply
}