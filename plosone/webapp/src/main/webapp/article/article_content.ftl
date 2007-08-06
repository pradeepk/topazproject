  <div id="researchArticle" class="content">
    <a id="top" name="top" toc="top" title="Top"></a>
    <@s.url id="thisPageURL" includeParams="get" includeContext="true" encode="false"/>
    <@s.url id="feedbackURL" includeParams="none" namespace="/" action="feedbackCreate" page="${thisPageURL?url}"/>
    <div class="beta">We are still in beta! Help us make the site better and
      <a href="${feedbackURL}" title="Submit your feedback">report bugs</a>.
    </div>
    <div id="contentHeader">
      <p>Open Access</p>
      <p id="articleType">Research Article</p>
    </div>
    <div id="publisher"><p>Published in <a href="#">PLoS ONE</a>.</p></div>
    <@s.property value="transformedArticle" escape="false"/>
  </div>

