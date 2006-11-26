<div id="content">
	<h1>E-mail this Article</h1>
	<@ww.url id="fetchArticleURL" action="fetchArticle" articleURI="${articleURI}"/>
	<div class="source">
		<@ww.a href="%{fetchArticleURL}" title="Back to original article" class="article icon">${title}</@ww.a>
	</div>						

	<@ww.form name="emailThisArticle" cssClass="pone-form" action="emailThisArticleSubmit" namespace="/article" method="post" title="Email this article">
	  <@ww.hidden name="articleURI"/>
 	  <@ww.hidden name="title"/>
		<fieldset>
			<legend>Complete this form</legend>
			<ol>
		      <@ww.textfield label="Recipient's E-mail address" required="true" name="emailTo" size="40" />
 		      <@ww.textfield label="Your E-mail address" required="true" name="emailFrom" size="40" />
 		      <@ww.textfield label="Your name" required="true" name="senderName" size="40" />					
 		      <@ww.textarea label="Your comments to add to the E-mail" value="%{'I thought you would find this article interesting.'}" required="true" name="note" />					
			</ol>
		  <@ww.submit value="Send"/>
		</fieldset>
	</@ww.form>	
	<p class="citation"><a href="http://journals.plos.org/plosmedicine/privacy.php">PLoS Journals Privacy Statement</a></p>
</div>
