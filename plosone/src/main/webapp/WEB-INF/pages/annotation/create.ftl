<html>
  <head>
    <title>Create an annotation</title>
  </head>
  <body>
    <p>
      <fieldset>
          <legend>Create an annotation</legend>
          <@ww.form name="createAnnotationForm" action="createAnnotationSubmit">
            <@ww.textfield name="target" label="What does it annotate" />
            <@ww.textfield name="context" label="Context" />
            <@ww.textfield name="title" label="Title" />
            <@ww.textfield name="body" label="Annotation text" />
            <@ww.submit value="create annotation" />
          </@ww.form>
      </fieldset>
    </p>
  </body>
</html>
