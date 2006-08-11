<html>
	<head>
		<title>Articles you can view</title>
	</head>

	<body>

    <fieldset>
        <legend>Available articles</legend>

        <#list articles as article>

          <@ww.url id="fetchArticleURL" action="fetchArticle" articleDOI="${article}"/>
          <@ww.a href="%{fetchArticleURL}">${fetchArticleURL}</@ww.a><br/>
        </#list>

    </fieldset>

  </body>
</html>
