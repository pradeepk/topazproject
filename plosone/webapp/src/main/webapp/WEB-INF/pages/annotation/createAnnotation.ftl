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
            <@ww.textfield name="startPath" label="Start path" value="%{'id(\"x20060728a\")/p[1]'}" required="true"/>
            <@ww.textfield name="startOffset" label="Start offset" value="%{'288'}" required="true"/>
            <@ww.textfield name="endPath" label="End path" value="%{'id(\"x20060801a\")/h3[1]'}" required="true"/>
            <@ww.textfield name="endOffset" label="End offset" value="%{'39'}" required="true"/>
            <@ww.textfield name="title" label="Title" value="%{'title1'}"/>
            <@ww.textfield name="olderAnnotation" label="Older Annotation to supersede" value="%{'doi:anOlderAnnotation'}"/>
            <@ww.checkbox name="public" label="Is it Public?" value="public" fieldValue="false"/>
            <@ww.textarea name="body" label="Annotation text" value="%{'This article seems to cover the same grounds as this ...'}" rows="'3'" cols="'30'" required="true"/>
            <@ww.submit value="create annotation" />
          </@ww.form>
      </fieldset>
    </p>
  </body>
</html>
