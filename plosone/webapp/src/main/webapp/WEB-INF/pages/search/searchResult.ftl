<html>
	<head>
		<title>Search results</title>
	</head>

	<body>
    <fieldset>
      <legend>Simple Search</legend>
      <@ww.form name="simpleSearchForm" action="simpleSearch" namespace="/search" method="post">
        <@ww.textfield name="query" label="Query" required="true"/>
        <@ww.submit value="simple search" />
      </@ww.form>
    </fieldset>
    <fieldset>
      <legend>Search results</legend>
      <#list searchResults as hit>
        <ul>
          <li>
            hitNumber = ${hit.hitNumber}<br/>      
            hitScore = ${hit.hitScore}<br/>
            pid = ${hit.pid}<br/>
            type = ${hit.type}<br/>
            state = ${hit.state}<br/>
            createdDate = ${hit.createdDate}<br/>
            lastModifiedDate    = ${hit.lastModifiedDate }<br/>
            contentModel = ${hit.contentModel}<br/>
            description = ${hit.description}<br/>
            publisher = ${hit.publisher}<br/>
            repositoryName = ${hit.repositoryName}<br/>
          </li>
        </ul>
        <hr/>
      </#list>

    </fieldset>

  </body>
</html>
