<#if rating.body.commentTitle?exists>
  <#assign commentTitle = rating.body.commentTitle>
<#else>
  <#assign commentTitle = '"Rating has no Title"'>
</#if>
<#if rating.body.commentValue?exists>
  <#assign commentText = rating.body.commentValue>
<#else>
  <#assign commentText = '"Rating has no Text"'>
</#if>
<html>
  <head>
    <title>Rating details</title>
  </head>
  <body>
    <fieldset>
      <legend><b>Rating details</b></legend>
      <table width="100%">
        <tr><td width="100px">&nbsp;</td><td/></tr>
        <tr><td>Id</td><td>${rating.body.id}</td></tr>
        <tr><td>Title</td><td>${commentTitle}</td></tr>
        <tr><td>Created</td><td>${rating.created?datetime}</td></tr>
        <tr><td>Creator</td><td><a href="../user/showUser.action?userId=${rating.creator}">${rating.creator}</a></td></tr>
        <tr>
          <td colspan="2">
            <fieldset>
              <legend><b>Content</b></legend>
              ${commentTitle}<br />
              ${commentText}
            </fieldset>
          </td>
        </tr>
      </table>
    </fieldset>
  </body>
</html>
