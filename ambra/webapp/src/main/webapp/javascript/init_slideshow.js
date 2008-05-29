/*
 * $HeadURL::                                                                            $
 * $Id$
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

var figureWindow;
var image;
var imageWidth = 0;

dojo.addOnLoad(function() {
  ambra.slideshow.setLinkView(dojo.byId("viewL"));
  ambra.slideshow.setLinkTiff(dojo.byId("downloadTiff"));
  ambra.slideshow.setLinkPpt(dojo.byId("downloadPpt"));
  ambra.slideshow.setFigImg(dojo.byId("figureImg"));
  ambra.slideshow.setFigTitle(dojo.byId("figureTitle"));
  ambra.slideshow.setFigCaption(dojo.byId("figure-window-description"));
  ambra.slideshow.setInitialThumbnailIndex();
  
  //dojo.connect(window, "onload", function () {
      ambra.slideshow.adjustViewerHeight();
  //  }
  //);
  
  dojo.connect(window, "onresize", function () {
      ambra.slideshow.adjustViewerHeight();
    }
  );
  
  /*figureWindow = dojo.byId("figure-window-wrapper");
  var imageMarginBox = dojo.html.getMarginBox(ambra.slideshow.figureImg);
  imageWidth = imageMarginBox.width;
  //alert("imageWidth = " + imageWidth);
  
  ambra.domUtil.setContainerWidth(figureWindow, imageWidth + 250);
  
  dojo.connect(window, "onresize", function() {
      setTimeout("ambra.domUtil.setContainerWidth(figureWindow, imageWidth + 250)", 100);
    }
  );*/
  
});