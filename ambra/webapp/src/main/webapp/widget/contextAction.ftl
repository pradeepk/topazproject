<#--
  $HeadURL::                                                                            $
  $Id$

  Copyright (c) 2007-2009 by Topaz, Inc.
  http://topazproject.org

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->
<div dojoType="ambra.widget.ContextAction" id="ContextActionDialog" class="contextActionDialog">
  <div class="dialog context">
    <div class="tipu" id="caTipu"></div>
    <div class="contextActionContent">
      <h5><img src="../images/tooltip_addannotation.gif" /> Add a note to this text.</h5>
      Please follow our <a href="${comment}">guidelines for commenting/rating</a> and review our <a href="/static/competing.action">competing interests policy</a>. Comments that do not conform to our guidelines will be promptly removed and the user account disabled. The following must be avoided:
      <ul>
        <li>Remarks that could be interpreted as allegations of misconduct</li>
        <li>Unsupported assertations or statements</li>
        <li>Inflammatory and insulting language</li>
      </ul>
      <form name="contextActionForm" id="contextActionForm" class="clearfix buttons" method="post" action="">
        <input type="button" name="Continue" value="Continue" id="ContextActionDialogContinueButton" onmouseup="ambra.displayAnnotationContext.startComment(event);" title="Add a note to this text" class="primary"/></form>
    </div>
    <div class="tip" id="caTip"></div>
  </div>
</div>
<div dojoType="ambra.widget.ContextAction" id="ContextActionDialogNotLogged" class="contextActionDialog">
  <div class="dialog context">
    <div class="tipu" id="canlTipu"></div>
    <div class="contextActionContent">
      <h5><img src="../images/tooltip_addannotation.gif" /> Add a note to this text.</h5>
      You must be logged into to add a note to an article.
      You may log in by <a onmousedown="ambra.displayAnnotationContext.disconnect(event);" href="${freemarker_config.context}/user/secure/secureRedirect.action?goTo=${thisPage}">clicking here</a>.
    </div>
    <div class="tip" id="canlTip"></div>
  </div>
</div>
<div dojoType="ambra.widget.ContextAction" id="ContextActionDialogBadSelection" class="contextActionDialog">
  <div class="dialog context">
    <div class="tipu" id="canBDTipu"></div>
    <div class="contextActionContent">
      <h5 class="annotation icon"><img src="../images/tooltip_addannotation.gif" /> Add a note to this text.</h5>
      You cannot annotate this area of the document.
    </div>
    <div class="tip" id="canBDTip"></div>
  </div>
</div>
<div dojoType="ambra.widget.ContextAction" id="ContextActionDialogBadRangeSelection" class="contextActionDialog">
  <div class="dialog context">
    <div class="tipu" id="canbrTipu"></div>
    <div class="contextActionContent">
      <h5><img src="../images/tooltip_addannotation.gif" /> Add a note to this text.</h5>
      You cannot create an annotation that spans different sections of the document; please adjust your selection.
    </div>
    <div class="tip" id="canbrTip"></div>
  </div>
</div>
