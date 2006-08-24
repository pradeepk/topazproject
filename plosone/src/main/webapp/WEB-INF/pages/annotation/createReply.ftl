<html>
  <head>
    <title>Create an annotation</title>
  </head>
  <body>
    <p>
      <fieldset>
          <legend>Create an annotation</legend>
          <@ww.form name="createReplyForm" action="createReplySubmit">
            <@ww.textfield name="root" label="What is the root of this reply" required="true"/>
            <@ww.textfield name="inReplyTo" label="What is it in reply to" required="true"/>
            <@ww.textfield name="title" label="Title"/>
            <@ww.textarea name="body" label="Reply text" rows="'3'" cols="'30'" required="true"/>
            <@ww.submit value="create reply" />
          </@ww.form>
      </fieldset>
    </p>
  </body>
</html>
