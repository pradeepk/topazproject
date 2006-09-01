<html>
	<head>
		<title>Annotations list</title>
	</head>

	<body>

    <fieldset>
        <legend>Available annotations</legend>

        <#list annotations as annotation>
          id          =
          <@ww.url id="getAnnotationURL" action="getAnnotation" annotationId="${annotation.id}"/>
          <@ww.a href="%{getAnnotationURL}">${annotation.id}</@ww.a> <br/>

          annotates   =${annotation.annotates}     <br/>
          title       =${annotation.title}         <br/>
          creator     =${annotation.creator}       <br/>

          <@ww.url id="deleteAnnotationURL" action="deleteAnnotation" annotationId="${annotation.id}" namespace="/annotation/secure" />
          <@ww.a href="%{deleteAnnotationURL}">delete</@ww.a><br/>
          <hr/>
        </#list>

    </fieldset>

  </body>
</html>
