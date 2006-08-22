<html>
	<head>
		<title>Annotations list</title>
	</head>

	<body>

    <fieldset>
        <legend>Available annotations</legend>

        <#list annotations as annotation>
          id          =${annotation.id}            <br/>
          annotates   =${annotation.annotates}     <br/>
          title       =${annotation.title}         <br/>
          body        =<@ww.a href="${annotation.body}">${annotation.body}</@ww.a>  
                    [Text : ${annotationBodyContent(annotation.id)}]<br/>
          context     =${annotation.context}       <br/>
          created     =${annotation.created}       <br/>
          creator     =${annotation.creator}       <br/>
          mediator    =${annotation.mediator}      <br/>
          type        =${annotation.type}          <br/>


          <@ww.url id="deleteAnnotationURL" action="deleteAnnotation" annotationId="${annotation.id}"/>
          <@ww.a href="%{deleteAnnotationURL}">delete</@ww.a><br/>
          <hr/>
        </#list>

    </fieldset>

  </body>
</html>
