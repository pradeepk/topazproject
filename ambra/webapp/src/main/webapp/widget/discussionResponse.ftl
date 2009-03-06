<#--
  $HeadURL::                                                                            $
  $Id$
  
  Copyright (c) 2007-2008 by Topaz, Inc.
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
<!-- begin : posting response -->
<div class="posting pane" id="DiscussionPanel">
	<h5>Post Your Response</h5>
  <div class="instructions">Please follow our <a  href="/static/commentGuidelines.action">guidelines for commenting/rating</a> and review our <a href="#">competing interests policy</a>. Comments that do not conform to our guidelines will be promptly removed and the user account disabled. The following must be avoided:
    <ol>
      <li>Remarks that could be interpreted as allegations of misconduct</li>
      <li>Unsupported assertions or statements</li>
      <li>Inflammatory or insulting language</li>
    </ol>
  </div>
  <form name="discussionResponse" method="post" action="">
  <input type="hidden" name="commentTitle" value="" />
  <input type="hidden" name="comment" value="" />
  <fieldset>
    <legend>Compose Your Response</legend>
    <div id="responseSubmitMsg" class="error"></div>
    <table class="layout">
        <tr>
          <td>
            <label for="title"><span class="none">Enter your response title</span></label>
            <input type="text" name="responseTitle" id="title" value="Enter your response title..." class="title" alt="Enter your response title..." />
            <label for="reponseArea"><span class="none">Enter your response</span></label>
            <textarea id="responseArea" title="Enter your response..." class="response" name="responseArea" >Enter your response...</textarea>
          <td>&nbsp;</td>
          <td class="coi">
            <fieldset>
              <legend>Declare any competing interests.</legend>
              <ul>
                <li><label><input id="temp" type="radio" checked="checked" name="noCOI" value="blah"  /> No, I don't have any competing interests to declare.</label></li>
                <li><label><input id="temp2" type="radio" name="yesCOI" value="blahblah"  /> Yes, I have competing interests to declare (enter below):</label></li>
              </ul>
              <textarea name="coi" id="temp" value="Enter your competing interests..." alt="Enter your competing interests...">Enter your competing interests...</textarea>
            </fieldset>
          </td>
        </tr>
        <tr>
          <td colspan="3" class="buttons">
            <input type="button" value="Cancel" title="Click to close and cancel" id="btnCancelResponse"/>
            <input name="post" value="Post" type="button" id="btnPostResponse" title="Click to Post Your Response" class="primary"/>
          </td>
        </tr>
      </table>
		</fieldset>
	</form>
</div>
<!-- end : posting response -->
