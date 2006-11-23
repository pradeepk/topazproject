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

      <#assign totalPages=(totalNoOfResults/pageSize)?int>
      <#if (totalNoOfResults%pageSize > 0) >
        <#assign totalPages = totalPages + 1>
      </#if>

      Total number of results found: ${totalNoOfResults} <br/>
      Total number of pages found: ${totalPages} <br/>
      <#if (totalPages > 1) >
        <#list 1..totalPages as pageNumber>
          &lt;
          <@ww.url id="searchPageURL" action="simpleSearch" namespace="/search" startPage="${pageNumber - 1}" pageSize="${pageSize}" query="${query}"/>
          <@ww.a href="%{searchPageURL}">${pageNumber}</@ww.a>
          &gt;&nbsp;
        </#list>
      </#if>

      <#list searchResults as hit>
        <ul>
          <li>
            hitNumber = ${hit.hitNumber}<br/>      
            hitScore = ${hit.hitScore}<br/>
            pid = ${hit.pid}<br/>
            type = ${hit.type}<br/>
            state = ${hit.state}<br/>
            createdDate = ${hit.createdDate.toString()}<br/>
            lastModifiedDate    = ${hit.lastModifiedDate.toString() }<br/>
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
