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
          body        =${annotation.body}          <br/>
          context     =${annotation.context}       <br/>
          created     =${annotation.created}       <br/>
          creator     =${annotation.creator}       <br/>
          mediator    =${annotation.mediator}      <br/>
          type        =${annotation.type}          <br/>
          supersededBy=${annotation.supersededBy}  <br/>
          supersedes  =${annotation.supersedes}    <br/>

          <@ww.url id="deleteAnnotationURL" action="deleteAnnotation" annotationId="${annotation}"/>
          <@ww.a href="%{deleteAnnotationURL}">delete</@ww.a><br/>
          <hr/>
        </#list>

    </fieldset>

  </body>
</html>
