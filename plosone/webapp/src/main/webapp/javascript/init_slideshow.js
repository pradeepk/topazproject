
  function init(e) {
    topaz.slideshow.setLinkView(dojo.byId("viewL"));
    topaz.slideshow.setLinkTiff(dojo.byId("downloadTiff"));
    topaz.slideshow.setLinkPpt(dojo.byId("downloadPpt"));
    topaz.slideshow.setFigImg(dojo.byId("figureImg"));
    topaz.slideshow.setFigTitle(dojo.byId("figureTitle"));
    topaz.slideshow.setFigCaption(dojo.byId("figure-window-description"));
    topaz.slideshow.setInitialThumbnailIndex();
    
    dojo.event.connect(window, "onload", function () {
        topaz.slideshow.adjustViewerHeight();
      }
    );
    
    dojo.event.connect(window, "onresize", function () {
        topaz.slideshow.adjustViewerHeight();
      }
    );
  }
  
  dojo.addOnLoad(init);
