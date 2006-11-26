<html>
  <head>
    <title>Welcome to Plosone</title>
  </head>
  <body>

    <h1>Plosone Adminstration</h1>
	
	<fieldset>
    <legend><b>Messages</b></legend>
      <p>
        <#list actionMessages as message>
          ${message} <br/>
        </#list>
      </p>
    </fieldset>

	<fieldset>
 	<legend><b>Ingestable Archives</b></legend>
 	     <@ww.form name="ingestArchives" action="ingestArchives" method="get" namespace="/admin">
  			<#list uploadableFiles as file>
  				<@ww.checkbox name="filesToIngest" label="${file}" fieldValue="${file}"/><br/>
  			</#list>
  			<br/>
            <@ww.submit value="Ingest Selected Archives" />
         </@ww.form>
	</fieldset>
	
	<fieldset>	
	<legend><b>Publishable Documents</b></legend>
 	     <@ww.form name="publishArchives" action="publishArchives" method="get" namespace="/admin">
  			<#list publishableFiles as article>
  				<@ww.checkbox name="articlesToPublish" label="${article}" fieldValue="${article}"/><br/>
  			</#list>
  			<br/>
            <@ww.submit value="Publish Selected Archives" />
         </@ww.form>	
	</fieldset>
  </body>
</html>
