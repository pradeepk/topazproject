<html>
  <head>
    <title>Welcome to PLoS ONE</title>
  </head>
  <body>

    <h1>PLoS ONE Administration</h1>
    <fieldset>
      <legend><b>Messages</b></legend>
      <p>
        <#list actionMessages as message>
          ${message} <br/>
        </#list>
      </p>
    </fieldset>
    <br/>

    <fieldset>
      <legend><b>Ingestable Archives</b></legend>
      <@s.form name="ingestArchives" action="ingestArchives" method="post" namespace="/admin">
        <#list uploadableFiles as file>
          <@s.checkbox name="filesToIngest" label="${file}" fieldValue="${file}"/><br/>
        </#list>
        <br/>
        <@s.submit value="Ingest Selected Archives" />
      </@s.form>
    </fieldset>
    <br/>

    <fieldset>
      <legend><b>Publishable Documents</b></legend>
      <@s.form id="publishArchives" name="publishArchives" action="publishArchives" method="post" namespace="/admin">
        <table>
          <tr>
            <td><b>Publish</b></td><td><b>Delete</b></td><td></td>
          </tr>
          <#list publishableFiles as article>
            <tr>
              <@s.url id="articleURL" includeParams="none" namespace="/article" action="fetchArticle" articleURI="${article}"/>
              <td><@s.checkbox name="articlesToPublish" fieldValue="${article}"/></td>
              <td><@s.checkbox name="articlesToDelete" fieldValue="${article}"/></td>
              <td><a target="_article" href="${articleURL}">${article}</a></td>
            </tr>
          </#list>
        </table>
        <@s.submit value="Publish/Delete Articles"/>
      </@s.form>
    </fieldset>
    <br/>

    <fieldset>
      <legend><b>Other links</b></legend>
      <@s.url id="findUserURL" namespace="/admin" action="findUser" />
      <@s.a href="%{findUserURL}">Find user</@s.a>
      <br/>
    </fieldset>
    <br/>

    <fieldset>
      <legend><b>Flagged Comments</b></legend>
      <@s.form name="processFlags" action="processFlags" method="post" namespace="/admin">
        <table width="100%">
          <tr><td><b>Time</b></td><td><b>Comment</b></td><td><b>By</b></td><td><b>Refers To</b></td><td><b>Reason</b></td><td><b>Action</b></td></tr>
          <tr><td colspan="6"><hr/></td></tr>
          <#list flaggedComments as flaggedComment>
            <#if flaggedComment.isAnnotation>
              <@s.url id="flagURL" namespace="/admin" action="viewAnnotation" annotationId="${flaggedComment.target}"/>
            <#else>
              <@s.url id="flagURL" namespace="/admin" action="viewReply" replyId="${flaggedComment.target}"/>
            </#if>
            <tr>
              <td>${flaggedComment.created}</td>
              <td width="20%">${flaggedComment.flagComment}</td>
              <td><a href="../user/displayUser.action?userId=${flaggedComment.creatorid}"/>${flaggedComment.creator}</a></td>
              <td width="20%"><a href="${flagURL}">${flaggedComment.targetTitle}</a></td>
              <td>${flaggedComment.reasonCode}</td>
              <td>
                <@s.checkbox name="commentsToDelete" label="Delete Sub-thread" fieldValue="${flaggedComment.root}_${flaggedComment.target}"/><br/>
                <@s.checkbox name="commentsToUnflag" label="Remove Flag" fieldValue="${flaggedComment.target}_${flaggedComment.flagId}"/>
              </td>
            </tr>
            <tr><td colspan="6"><hr/></td></tr>
          </#list>
        </table>
        <@s.submit value="Process Selected Flags" />
      </@s.form>
      <br/>
    </fieldset>
  </body>
</html>
