<html>
  <head>
    <title>Create an flag</title>
  </head>
  <body>
    <p>
      <fieldset>
          <legend>Create a flag</legend>
          <@ww.form name="createFlagForm" action="createFlagSubmit" method="get">
            <@ww.textfield name="target" label="What does it flag" value="%{'http://here.is/viru'}" required="true"/>
            <@ww.textarea name="comment" label="Flag text" value="%{'Spammer guy attacks again. Who want more viagra...'}" rows="'3'" cols="'30'" required="true"/>
            <@ww.submit value="create flag" />
          </@ww.form>
      </fieldset>
    </p>
  </body>
</html>
