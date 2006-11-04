<html>
	<head>
		<title>Articles you can view</title>
	</head>

	<body>
    <fieldset>
      <legend>Available articles</legend>

      <#list articles as article>

        <@ww.url id="fetchArticleURL" action="fetchArticle" articleURI="${article}"/>
        <@ww.a href="%{fetchArticleURL}">${article}</@ww.a>

        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
        <@ww.url id="fetchSecondaryObjectsURL" action="fetchSecondaryObjects" uri="${article}"/>
        <@ww.a href="%{fetchSecondaryObjectsURL}">Images and more</@ww.a>

        <ul>
          <li>
            <@ww.url id="articleArticleRepXML"  action="fetchObject" uri="${article}">
              <@ww.param name="representation" value="%{'XML'}"/>
            </@ww.url>
            <@ww.a href="%{articleArticleRepXML}">View XML representation</@ww.a>
          </li>
          <li>
            <@ww.url id="articleArticleRepPDF"  action="fetchObject" uri="${article}">
              <@ww.param name="representation" value="%{'PDF'}"/>
            </@ww.url>
            <@ww.a href="%{articleArticleRepPDF}">View PDF representation</@ww.a>
          </li>
          <li>
            <@ww.url id="annotationURL" includeContext="false" namespace="../annotation" action="listAnnotation" target="${article}"/>
            <@ww.a href="%{annotationURL}">View Annotations for Article</@ww.a>
          </li>
          <li>
            <@ww.url id="annotatedArticleURL" action="fetchAnnotatedArticle" articleURI="${article}"/>
            <@ww.a href="%{annotatedArticleURL}">Get Annotated Article XML</@ww.a>
          </li>


        </ul>

      </#list>

    </fieldset>

  </body>
</html>
