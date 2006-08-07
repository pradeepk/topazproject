<html>
	<head>
		<title>Articles you can view</title>
	</head>
	
	<body>
	
    <fieldset>
        <legend>Available articles</legend>
        <p>
            <@ww.url id="fetchArticleURL" action="fetchArticle">
              <@ww.param name="articleDOI" value="10.1371/journal.pbio.0020294"/>
            </@ww.url>
            <@ww.a href="%{fetchArticleURL}">${fetchArticleURL}</@ww.a>
        </p>

    </fieldset>

  </body>
</html>
