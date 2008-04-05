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
var _containerDiv;
var _topBannerDiv;

function globalInit() {
  if (dojo.render.html.ie) {
    _containerDiv = dojo.byId("container");
    _topBannerDiv = dojo.byId("topBanner");
    
    if (_containerDiv) {
      topaz.domUtil.setContainerWidth(_containerDiv, 675, 940);
      
      dojo.event.connect(window, "onresize", function() {
          setTimeout("topaz.domUtil.setContainerWidth(_containerDiv, 675, 940)", 100);
        }
      );
    }
    
    if (_topBannerDiv) {
      topaz.domUtil.setContainerWidth(_topBannerDiv, 942, 944);
      
      dojo.event.connect(window, "onresize", function() {
          setTimeout("topaz.domUtil.setContainerWidth(_topBannerDiv, 942, 944)", 100);
        }
      );
    }
  }
}

dojo.addOnLoad(globalInit);
