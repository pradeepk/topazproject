<div id="content">
	<@ww.form name="feedbackForm" cssClass="pone-form" action="feedback" method="post" title="Feedback">
	  <@ww.hidden name="page"/>
  	  <fieldset>
			<legend>Feedback</legend>
			<ol>
  	    <@ww.textfield label="Name: " name="name" size="50" required="true"/>
    	  <@ww.textfield label="Email Address: " name="fromEmailAddress" size="50" required="true"/>
      	<@ww.textfield label="Subject: " name="subject" required="true" size="50"/>
	      <@ww.textarea label="Message" name="note" required="true" cols="50" rows="5" />
			</ol>
	  <@ww.submit value="Submit Query"/>
	</@ww.form>
</div>