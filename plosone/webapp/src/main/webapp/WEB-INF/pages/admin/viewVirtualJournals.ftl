<html>
  <head>
    <title>PLoS ONE: Administration: View Virtual Journals</title>
  </head>
  <body>
    <h1>PLoS ONE: Administration: View Virtual Journals</h1>

    <@s.url id="adminTop" namespace="/admin" action="adminTop"/>
    <p style="text-align: right">Return to <@s.a href="${adminTop}">Admin Top</@s.a></p>
    <br />

    <hr />

    <fieldset>
      <legend><b>Messages</b></legend>
      <p>
        <#list actionMessages as message>
          ${message} <br/>
        </#list>
      </p>
    </fieldset>
    <br />

    <hr />

    <#list journals as journal>
      <fieldset>
        <legend><b>${journal.key}</b></legend>
        <table border="1" cellpadding="2" cellspacing="0">
          <tr>
            <th>Simple Collection</th>
          </tr>
          <#list journal.simpleCollection as articleUri>
            <@s.url id="fetchArticle" namespace="/article" action="fetchArticle" articleURI="${articleUri}"/>
            <tr><td><@s.a href="${fetchArticle}">${articleUri}</@s.a></td></tr>
          </#list>
        </table>
      </fieldset>
    </#list>
  </body>
</html>
