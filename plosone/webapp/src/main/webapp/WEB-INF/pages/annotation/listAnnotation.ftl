<html>
	<head>
		<title>Annotations list</title>
	</head>

	<body>

    <fieldset>
        <legend>Available annotations</legend>

        <#list annotations as annotation>
          id          =
          <@ww.url id="getAnnotationURL" includeParams="none" action="getAnnotation" annotationId="${annotation.id}"/>
          <@ww.a href="%{getAnnotationURL}">${annotation.id}</@ww.a> <br/>

          annotates   =${annotation.annotates}     <br/>
          title       =${annotation.commentTitle}         <br/>
          creator     =${annotation.creator}       <br/>

          <br/>
      
          <@ww.url id="createFlagURL" action="createFlag" target="${annotation.id}" namespace="/annotation/secure"/>
          <@ww.a href="%{createFlagURL}">create flag</@ww.a> <br/>

          <@ww.url id="deleteAnnotationURL" action="deleteAnnotation" annotationId="${annotation.id}" namespace="/annotation/secure" />
          <@ww.a href="%{deleteAnnotationURL}">delete</@ww.a><br/>
          <hr/>
        </#list>

    </fieldset>

  </body>
</html>
