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
	<br/>
	<fieldset>
 	<legend><b>Ingestable Archives</b></legend>
 	     <@ww.form name="ingestArchives" action="ingestArchives" method="post" namespace="/admin">
  			<#list uploadableFiles as file>
  				<@ww.checkbox name="filesToIngest" label="${file}" fieldValue="${file}"/><br/>
  			</#list>
  			<br/>
            <@ww.submit value="Ingest Selected Archives" />
         </@ww.form>
	</fieldset>
	<br/>	
	<fieldset>	
	<legend><b>Publishable Documents</b></legend>
 	     <@ww.form name="publishArchives" action="publishArchives" method="post" namespace="/admin">
  			<#list publishableFiles as article>
   				<@ww.url id="articleURL" includeParams="none" namespace="/article" action="fetchArticle" articleURI="${article}"/>
  				<@ww.checkbox name="articlesToPublish" fieldValue="${article}"/><a href="${articleURL}">${article}</a><br/>
  			</#list>
  			<br/>
            <@ww.submit value="Publish Selected Archives" />
         </@ww.form>	
	</fieldset>
	<br/>
	<fieldset>	
	<legend><b>Flagged Comments</b></legend>
			<@ww.form name="processFlags" action="processFlags" method="post" namespace="/admin">
				<table width="100%">
				<tr><td><b>Time</b></td><td><b>Comment</b></td><td><b>By</b></td><td><b>Refers To</b></td><td><b>Reason</b></td><td><b>Action</b></td></tr>
				<tr><td colspan="6"><hr/></td></tr>				
				<#list flaggedComments as flaggedComment>
				<tr>
					 <td>${flaggedComment.created}</td>				
					 <td width="20%">${flaggedComment.flagComment}</td>
					 <td><a href="../user/showUser.action?userId=${flaggedComment.creatorid}"/>${flaggedComment.creator}</a></td>
					 <td width="20%"><a href="${flaggedComment.targetDisplayURL}">${flaggedComment.targetTitle}</a></td>
					 <td>${flaggedComment.reasonCode}</td>
					 <td>
					   	<@ww.checkbox name="commentsToDelete" label="Delete" fieldValue="${flaggedComment.root}_${flaggedComment.target}"/><br/>
						<@ww.checkbox name="commentsToUnflag" label="Un-flag" fieldValue="${flaggedComment.target}_${flaggedComment.flagId}"/>					   						 </td>
				</tr>
				<tr><td colspan="6"><hr/></td></tr>
				</#list>
				<table>
				<@ww.submit value="Process Selected Flags" />
				</@ww.form>
  			<br/>
	</fieldset>	
  </body>
</html>
