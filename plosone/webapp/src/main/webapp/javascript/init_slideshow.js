
  function init(e) {
    var navContainer = dojo.byId("figure-window-nav");
    //topaz.slideshow.adjustContainerHeight(navContainer);

    topaz.slideshow.setLinkView(dojo.byId("viewL"));
    topaz.slideshow.setLinkTiff(dojo.byId("downloadTiff"));
    topaz.slideshow.setLinkPpt(dojo.byId("downloadPpt"));
    topaz.slideshow.setFigImg(dojo.byId("figureImg"));
    topaz.slideshow.setFigTitle(dojo.byId("figureTitle"));
    topaz.slideshow.setFigCaption(dojo.byId("figure-window-description"));
    
    /*
    dojo.event.connect(window, "onresize", function () {
        topaz.slideshow.adjustContainerHeight(navContainer);
      }
    );*/
    
  }
  
  dojo.addOnLoad(init);
