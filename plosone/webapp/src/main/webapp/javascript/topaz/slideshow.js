topaz.slideshow = new Object();

topaz.slideshow = {
  imgS: "PNG-S",
  
  imgM: "PNG-M",
  
  imgL: "PNG-L",
  
  imgTif: "TIF",
  
  show: function (obj, index) {
    var linkView   = dojo.byId("viewL");
    var linkTiff   = dojo.byId("downloadTiff");
    var linkPpt    = dojo.byId("downloadPpt");
    var figImg     = dojo.byId("figureImg");
    var figTitle   = dojo.byId("figureTitle");
    var figCaption = dojo.byId("figure-window-description");
    
    linkView.href = slideshow[index].imageUri + "&representation=" + this.imgL;
    linkTiff.href = slideshow[index].imageUri + "&representation=" + this.imgTif;
    linkPpt.href  = slideshow[index].imageUri + "&representation=" + this.imgM;
    
    figImg.src = slideshow[index].imageUri + "&representation=" + this.imgM;
    figImg.title = slideshow[index].titlePlain;
    
    figTitle.innerHTML = slideshow[index].title;
    
    figCaption.innerHTML = slideshow[index].description;
    
    var tbCurrent = document.getElementsByTagAndClassName('div', 'figure-window-nav-item-current');
    
    for (var i=0; i<tbCurrent.length; i++) {
      //alert("tbCurrent[" + i + "] = " + tbCurrent[i].nodeName + "\ntbCurrent[" + i + "].className = " + tbCurrent[i].className);
      tbCurrent[i].className = tbCurrent[i].className.replace(/\-current/, "");
      
    }
    
    var tbNew = obj.parentNode.parentNode;
    alert("tbNew = " + tbNew);
    tbNew.className = tbNew.className.concat("-current");
    
  } 
  
}  