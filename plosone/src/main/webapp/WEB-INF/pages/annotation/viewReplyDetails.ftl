<html>
	<head>
		<title>Reply details</title>
	</head>

	<body>

    <fieldset>
        <legend>Reply details</legend>

          id          =${reply.id}            <br/>
          root        =${reply.root}     <br/>
          inReplyTo   =${reply.inReplyTo}     <br/>
          title       =${reply.title}         <br/>
          body        =
          <@ww.url id="fetchBodyURL" action="fetchBody" bodyUrl="${reply.body}"/>
          <@ww.a href="%{fetchBodyURL}">${reply.body}</@ww.a> <br/>

          created     =${reply.created}       <br/>
          creator     =${reply.creator}       <br/>
          mediator    =${reply.mediator}      <br/>
          type        =${reply.type}          <br/>

          <@ww.url id="createReplyURL" action="createReplySubmit" root="${reply.root}" inReplyTo="${reply.id}"/>
          <@ww.a href="%{createReplyURL}">create reply</@ww.a> <br/>

          <@ww.url id="listReplyURL" action="listAllReplies" root="${reply.root}" inReplyTo="${reply.id}"/>
          <@ww.a href="%{listReplyURL}">list replies</@ww.a> <br/>

    </fieldset>

  </body>
</html>
