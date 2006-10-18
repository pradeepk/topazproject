<html>
	<head>
		<title>Available replies</title>
	</head>

	<body>

    <fieldset>
        <legend>Available replies</legend>

        <#list replies as reply>
          id          =
          <@ww.url id="getReplyURL" action="getReply" replyId="${reply.id}"/>
          <@ww.a href="%{getReplyURL}">${reply.id}</@ww.a> <br/>

          inReplyTo   =${reply.inReplyTo}     <br/>
          root        =${reply.root}     <br/>
          title       =${reply.commentTitle}         <br/>
          creator     =${reply.creator}       <br/>

          <@ww.url id="deleteReplyURL" action="deleteReply" replyId="${reply.id}" namespace="/annotation/secure" />
          <@ww.a href="%{deleteReplyURL}">delete</@ww.a><br/>
          <hr/>
        </#list>

    </fieldset>

  </body>
</html>
