<html>
  <head>
    <title>Create an flag</title>
  </head>
  <body>
    <p>
      <fieldset>
          <legend>Create a flag</legend>
          <@ww.form name="createFlagForm" action="createFlagSubmit" method="get">
            <@ww.textfield name="target" label="What does it flag" value="%{'info:doi/10.1371/annotation/13'}" required="true"/>
            <@ww.select name="reasonCode" label="Reason"
                        list="{'spam', 'Offensive', 'Inappropriate'}"/> 
            <@ww.textarea name="comment" label="Flag text" value="%{'Spammer guy attacks again. Who want more viagra...'}" rows="'3'" cols="'30'" required="true"/>
            <@ww.submit value="create flag" />
          </@ww.form>
      </fieldset>
    </p>
  </body>
</html>
