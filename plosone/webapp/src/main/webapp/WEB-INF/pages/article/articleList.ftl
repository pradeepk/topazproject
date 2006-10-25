<html>
	<head>
		<title>Articles you can view</title>
	</head>

	<body>
    <fieldset>
      <legend>Available articles</legend>

      <#list articles as article>

        <@ww.url id="fetchArticleURL" action="fetchArticle" articleDOI="${article}"/>
        <@ww.a href="%{fetchArticleURL}">${article}</@ww.a>

        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
        <@ww.url id="fetchSecondaryObjectsURL" action="fetchSecondaryObjects" doi="${article}"/>
        <@ww.a href="%{fetchSecondaryObjectsURL}">Images and more</@ww.a>

        <ul>
          <li>
            <@ww.url id="articleArticleRepXML"  action="fetchObject" doi="${article}">
              <@ww.param name="representation" value="%{'XML'}"/>
            </@ww.url>
            <@ww.a href="%{articleArticleRepXML}">View XML representation</@ww.a>
          </li>
          <li>
            <@ww.url id="articleArticleRepPDF"  action="fetchObject" doi="${article}">
              <@ww.param name="representation" value="%{'PDF'}"/>
            </@ww.url>
            <@ww.a href="%{articleArticleRepPDF}">View PDF representation</@ww.a>
          </li>
        </ul>

      </#list>

    </fieldset>

  </body>
</html>
