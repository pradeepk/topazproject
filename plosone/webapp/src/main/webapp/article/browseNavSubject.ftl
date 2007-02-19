	<div id="browseNav">
		<div>
			<form class="browseForm" action="browse.action" method="get" name="browseForm">
				<fieldset>
					<legend>How would you like to browse?</legend>
					<ol>
						<li><label for="date"><input onclick="document.browseForm.submit()" type="radio" name="field" id="date"  value="date" /> By Publication Date</label></li>
						<li><label for="subject"><input type="radio" name="field" id="subject" value="subject" checked /> By Subject</label></li>
					</ol>
				</fieldset>
			</form>
		</div>

		<ul class="subjects">
			<#list categoryNames as subjectName>
		  <@ww.url id="browseURL" action="browse" namespace="/article" field="${field}" catId="${subjectName_index}" includeParams="none"/>
			<li><@ww.a href="%{browseURL}">${subjectName} (${articlesByCategory[subjectName_index]?size})</@ww.a></li>
			</#list>
		</ul>
	</div> <!-- browse nav -->