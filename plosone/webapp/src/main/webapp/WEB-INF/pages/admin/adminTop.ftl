<html>
  <head>
    <title>Welcome to Plosone</title>
	<script type="text/javascript"
  		src="/plosone-webapp/javascript/multifile.js">
	</script>    
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
 	<legend><b>Upload Archives</b></legend>
 	  <br>
 	  Browse your local file system to find all document archives
 	  (*.zip) files you wish to upload for Plos submission [5 max]:
 	  <br>
 	  <br>
		<form enctype="multipart/form-data" action="/plosone-webapp/admin/multiFileIngest.action" method = "post">
			<!-- The file element -- NOTE: it has an ID -->
			<input id="my_file_element" type="file" name="file_1" accept="application/zip" size="50">
			<br><br>
			<fieldset>					
			<legend>Files pending upload</legend>
				<!-- This is where the output will appear -->
				<div id="files_list"></div>
			</fieldset>
			<br><input type="submit" value="Upload">
		</form>			
		<script>
			<!-- Create an instance of the multiSelector class, pass it the output target and the max number of files -->
			var multi_selector = new MultiSelector( document.getElementById( 'files_list' ), 5 );
			<!-- Pass in the file element -->
			multi_selector.addElement( document.getElementById( 'my_file_element' ) );
		</script>
	</fieldset>
	
	<fieldset>	
	<legend><b>Documents in Loaded State</b></legend>
	</fieldset>
  </body>
</html>
