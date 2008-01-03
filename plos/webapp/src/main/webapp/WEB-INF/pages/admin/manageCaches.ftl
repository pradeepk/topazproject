<html>
  <head>
    <title>PLoS ONE: Administration: Cache Stats</title>
  </head>
  <body>
    <h1>PLoS ONE: Administration: Cache Stats</h1>

    <@s.url id="adminTop" namespace="/admin" action="adminTop"/>
    <p style="text-align: right">Return to <@s.a href="${adminTop}">Admin Top</@s.a></p>
    <br />

    <hr />

    <table border="1" cellpadding="2" cellspacing="0">
      <#list cacheStats.keySet().toArray() as cacheName>
        <tr>
          <th>${cacheName}</th>
          <#if cacheName != "">
            <@s.url id="removeAll" namespace="/admin" action="manageCaches"
              cacheAction="removeAll" cacheName="${cacheName}" />
            <td><@s.a href="%{removeAll}">removeAll</@s.a></td>
          <#else>
            <td>&nbsp;</td>
          </#if>
          <#assign colums=cacheStats.get(cacheName)>
          <#list colums as column>
            <td>${column}</td>
          </#list>
        </tr>
      </#list>
    </table>
  </body>
</html>
