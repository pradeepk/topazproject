<html>
  <head>
    <title>Create an annotation</title>
  </head>
  <body>
    <p>
      <fieldset>
          <legend>Create an annotation</legend>
          <@ww.form name="createAnnotationForm" action="createAnnotationSubmit">
            <@ww.textfield name="target" label="What does it annotate" value="%{'http://here.is/viru'}" required="true"/>
            <@ww.textfield name="targetContext" label="Context" value="%{'xpointer(id(p31))'}" required="true"/>
            <@ww.textfield name="title" label="Title" value="%{'title1'}"/>
            <@ww.textarea name="body" label="Annotation text" value="%{'This article seems to cover the same grounds as this ...'}" rows="'3'" cols="'30'" required="true"/>
            <@ww.submit value="create annotation" />
          </@ww.form>
      </fieldset>
    </p>
  </body>
</html>
