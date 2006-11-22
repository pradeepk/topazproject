<html>
  <head>
    <title>Search</title>
  </head>
  <body>
    <p>
      <fieldset>
        <legend>Simple Search</legend>
        <@ww.form name="simpleSearchForm" action="simpleSearch" namespace="/search" method="post">
          <@ww.textfield name="query" label="Query" required="true"/>
          <@ww.submit value="simple search" />
        </@ww.form>
      </fieldset>

      <fieldset>
        <legend>Advanced Search</legend>
        <@ww.form name="advancedSearchForm" action="advancedSearch" namespace="/search" method="post">
          <@ww.textfield name="title" label="Title" />
          <@ww.textfield name="text" label="Text" />
          <@ww.submit value="advanced search" />
        </@ww.form>
      </fieldset>
    </p>
  </body>
</html>
