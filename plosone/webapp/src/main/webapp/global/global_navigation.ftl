	<ul id="nav">
		<li><a href="http://${freemarker_config.plosOneHost}${freemarker_config.context}">Home</a></li>
		
		<li><a href="#">About</a>
				<ul>
				<@ww.url action="information.action" namespace="/static" includeParams="none" id="info"/>
				<@ww.url action="edboard.action" namespace="/static" includeParams="none" id="edboard"/>
				<@ww.url action="media.action" namespace="/static" includeParams="none" id="media"/>
				<@ww.url action="license.action" namespace="/static" includeParams="none" id="license"/>
					<li><a href="${info}">Journal Information</a></li>
					<li><a href="${edboard}">Editorial Board</a></li>
					<li><a href="${media}">Media Inquiries</a></li>
					<li><a href="${license}">License</a></li>
				</ul>
			</li>			
		<li><a href="#">For Users</a>
				<ul>
				<@ww.url action="faq.action" namespace="/static" includeParams="none" id="faq"/>
				<@ww.url action="commentGuidelines.action" namespace="/static" includeParams="none" id="comment"/>
				<@ww.url action="help.action" namespace="/static" includeParams="none" id="help"/>
				<@ww.url action="sitemap.action" namespace="/static" includeParams="none" id="site"/>				
  			<@ww.url action="contact.action" namespace="/static" includeParams="none" id="contact"/>				
					<li><a href="${faq}">Frequently Asked Questions</a></li>
					<li><a href="${comment}">Commenting Guidelines</a></li>
					<li><a href="${help}">Help Using this Site</a></li>
					<li><a href="${site}">Site Map</a></li>
					<li><a href="${contact}">Contact Us</a></li>
				</ul>
			</li>
		<li><a href="#">For Authors and Reviewers</a>
				<ul>
				<@ww.url action="whypublish.action" namespace="/static" includeParams="none" id="why"/>
				<@ww.url action="policies.action" namespace="/static" includeParams="none" id="policies"/>
				<@ww.url action="guidelines.action" namespace="/static" includeParams="none" id="guidelines"/>
				<@ww.url action="figureGuidelines.action" namespace="/static" includeParams="none" id="figure"/>
				<@ww.url action="checklist.action" namespace="/static" includeParams="none" id="checklist"/>				
  			<@ww.url action="reviewerGuidelines.action" namespace="/static" includeParams="none" id="reviewer"/>								
					<li><a href="${why}">Why Publish With Us?</a></li>
					<li><a href="${policies}">Editorial and Publishing Policies</a></li>
					<li><a href="${guidelines}">Author Guidelines</a></li>
					<li><a href="${figure}">Figure Guidelines</a></li>
					<li><a href="${checklist}">Submit Your Paper</a></li>
					<li><a href="${reviewer}">Reviewer Guidlines</a></li>
				</ul>
			</li>
			<li class="journalnav"><a href="http://www.plos.org" tabindex="10">PLoS.org</a></li>

			<li class="journalnav"><a href="http://www.plosjournals.org" tabindex="9">PLoS Journals</a>
				<ul>
					<li><a href="http://biology.plosjournals.org" title="PLoSBiology.org">PLoS Biology</a></li>
					<li><a href="http://medicine.plosjournals.org" title="PLoSMedicine.org">PLoS Medicine</a></li>
					<li><a href="http://compbiol.plosjournals.org" title="PLoSCompBiol.org">PLoS Computational Biology</a></li>
					<li><a href="http://genetics.plosjournals.org" title="PLoSGenetics.org">PLoS Genetics</a></li>
					<li><a href="http://pathogens.plosjournals.org" title="PLoSPathogens.org">PLoS Pathogens</a></li>
					<li><a href="http://clinicaltrials.plosjournals.org" title="PLoSClinicalTrials.org">PLoS Clinical Trials</a></li>
					<li><a href="http://www.plosone.org/" title="PLoSONE.org">PLoS ONE</a></li>
					<li><a href="http://clinicaltrials.plosjournals.org" title="PLoSClinicalTrials.org">PLoS Clinical Trials</a></li>
					<li><a href="http://www.plosntd.org/" title="PLoSNTD.org">PLoS Neglected Tropical Diseases</a></li>
				</ul>
			</li>
		</ul>
