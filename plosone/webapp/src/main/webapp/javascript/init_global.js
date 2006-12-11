function globalInit() {
  var container = dojo.byId("container");
  var footer = dojo.byId("ftr");
  var topBanner = dojo.byId("topBanner");
  var viewport = dojo.html.getViewport();
  
  if (container) {
    topaz.domUtil.setContainerWidth(container, 675, 940);
    
    dojo.event.connect(window, "onresize", function() {
        topaz.domUtil.setContainerWidth(container, 675, 940);
      }
    );
  }
  
  //if (footer) {
  //  topaz.domUtil.setContainerWidth(footer, 675, 940);
    
  //  dojo.event.connect(window, "onresize", function() {
  //      topaz.domUtil.setContainerWidth(footer, 675, 940);
  //    }
  //  );
  //}
  
  if (topBanner) {
    topaz.domUtil.setContainerWidth(topBanner, 942, 944);
    
    dojo.event.connect(window, "onresize", function() {
        topaz.domUtil.setContainerWidth(topBanner, 942, 944);
      }
    );
  }
}

dojo.addOnLoad(globalInit);
