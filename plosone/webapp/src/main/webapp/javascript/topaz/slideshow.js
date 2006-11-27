topaz.slideshow = new Object();

topaz.slideshow = {
  imgS: "PNG_S",
  
  imgM: "PNG_M",
  
  imgL: "PNG_L",
  
  imgTif: "TIF",
  
  linkView: "",
  
  linkTiff: "",
  
  linkPpt: "",
  
  figImg: "",
  
  figTitle: "",
  
  figCaption: "",
  
  targetDiv: "",
  
  setLinkView: function(aObj) {
    this.linkView = aObj;
  },
  
  setLinkTiff: function(aObj) {
    this.linkTiff = aObj;
  },
  
  setLinkPpt: function(aObj) {
    this.linkPpt = aObj;
  },
  
  setFigImg: function(dObj) {
    this.figImg = dObj;
  },

  setFigTitle: function(dObj) {
    this.figTitle = dObj;
  },
  
  setFigCaption: function(dObj) {
    this.figCaption = dObj;
  },
  
  show: function (obj, index) {
    if (this.linkView) this.linkView.href = slideshow[index].imageUri + "&representation=" + this.imgL;
    if (this.linkTiff) this.linkTiff.href = slideshow[index].imageAttachUri + "&representation=" + this.imgTif;
    if (this.linkPpt) this.linkPpt.href  = slideshow[index].imageUri + "&representation=" + this.imgM;
    
    if (this.figImg) {
      this.figImg.src = slideshow[index].imageUri + "&representation=" + this.imgM;
      this.figImg.title = slideshow[index].titlePlain;
    }
    
    if (this.figTitle) this.figTitle.innerHTML = slideshow[index].title;
    
    if (this.figCaption) this.figCaption.innerHTML = slideshow[index].description;
    
    var tbCurrent = document.getElementsByTagAndClassName('div', 'figure-window-nav-item-current');
    
    for (var i=0; i<tbCurrent.length; i++) {
      //alert("tbCurrent[" + i + "] = " + tbCurrent[i].nodeName + "\ntbCurrent[" + i + "].className = " + tbCurrent[i].className);
      tbCurrent[i].className = tbCurrent[i].className.replace(/\-current/, "");
      
    }
    
    var tbNew = obj.parentNode.parentNode;
    tbNew.className = tbNew.className.concat("-current");
    
  },
  
  showSingle: function (obj, index) {
    if (this.linkView) this.linkView.href = slideshow[index].imageUri + "&representation=" + this.imgL;
    if (this.linkTiff) this.linkTiff.href = slideshow[index].imageAttachUri + "&representation=" + this.imgTif;
    if (this.linkPpt) this.linkPpt.href  = slideshow[index].imageUri + "&representation=" + this.imgM;
    
    if (this.figImg) {
      this.figImg.src = slideshow[index].imageUri + "&representation=" + this.imgM;
      this.figImg.title = slideshow[index].titlePlain;
    }
    
    if (this.figTitle) this.figTitle.innerHTML = slideshow[index].title;
    
    if (this.figCaption) this.figCaption.innerHTML = slideshow[index].description;
    
    var tbCurrent = document.getElementsByTagAndClassName('div', 'figure-window-nav-item-current');
    
    for (var i=0; i<tbCurrent.length; i++) {
      //alert("tbCurrent[" + i + "] = " + tbCurrent[i].nodeName + "\ntbCurrent[" + i + "].className = " + tbCurrent[i].className);
      tbCurrent[i].className = tbCurrent[i].className.replace(/\-current/, "");
      
    }
    
    var tbNew = obj.parentNode.parentNode;
    tbNew.className = tbNew.className.concat("-current");
    
  },
  
  getFigureInfo: function (figureObj) {
    if (figureObj.hasChildNodes) {
      var caption = document.createDocumentFragment();
      
      for (var i=0; i<figureObj.childNodes.length; i++) {
        var child = figureObj.childNodes[i];
        
        if (child.nodeName == 'A') {
          for (var n=0; n<child.childNodes.length; n++) {
            var grandchild = child.childNodes[n];
            
            if (grandchild.nodeName == 'IMG') {
              this.figImg = grandchild;
            }
          }
        }
        else if (grandchild.nodeName == 'H5') {
          dojo.dom.copyChildren(grandchild, this.figTitle);
        }
        else {
          var newChild = grandchild;
          newChild.getAttributeNode('xpathlocation')='noSelect';
          caption.appendChild(newChild);
        }
      }
      
      dojo.dom.copyChildren(caption, this.figCaption);
      
      return;
    }
    else {
      return false;
    }
  },
  
  adjustContainerHeight: function (obj) {
    // get size viewport
    var viewportSize = dojo.html.getViewport();
    
    // get the offset of the container
		var objOffset = topaz.domUtil.getCurrentOffset(obj);
		
		// find the size of the container
		var objMb = dojo.html.getMarginBox(obj);

    var maxContainerHeight = viewportSize.height - (10 * objOffset.top);
    //alert("objOffset.top = " + objOffset.top + "\nviewportSize.height = " + viewportSize.height + "\nmaxContainerHeight = " + maxContainerHeight);
    
    obj.style.height = maxContainerHeight + "px";
    obj.style.overflow = "auto";
  }
  
}  