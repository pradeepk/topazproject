<html>
	<head>
		<title>Annotation details</title>
	</head>

	<body>

    <fieldset>
        <legend>Annotation details</legend>

          id          =${annotation.id}            <br/>
          annotates   =${annotation.annotates}     <br/>
          title       =${annotation.title}         <br/>
          body        =
          <@ww.url id="fetchAnnotationBodyURL" action="fetchBody" bodyUrl="${annotation.body}"/>
          <@ww.a href="%{fetchAnnotationBodyURL}">${annotation.body}</@ww.a> <br/>

          context     =${annotation.context}       <br/>
          created     =${annotation.created}       <br/>
          creator     =${annotation.creator}       <br/>
          mediator    =${annotation.mediator}      <br/>
          type        =${annotation.type}          <br/>

          <@ww.url id="createReplyURL" action="createReplySubmit" root="${annotation.id}" inReplyTo="${annotation.id}"/>
          <@ww.a href="%{createReplyURL}">create reply</@ww.a> <br/>

          <@ww.url id="listReplyURL" action="listReply" root="${annotation.id}" inReplyTo="${annotation.id}"/>
          <@ww.a href="%{listReplyURL}">list replies</@ww.a> <br/>

    </fieldset>

  </body>
</html>
