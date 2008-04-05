<!--
  $HeadURL:: http://gandalf/svn/head/topaz/core/src/main/java/org/topazproject/otm/Abst#$
  $Id: AbstractConnection.java 4807 2008-02-27 11:06:12Z ronald $
  
  Copyright (c) 2007-2008 by Topaz, Inc.
  http://topazproject.org
  
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
  
  http://www.apache.org/licenses/LICENSE-2.0
  
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->

<!-- begin : main content -->
<div id="content" class="static">

<h1><em>PLoS Pathogens</em> Site Map</h1>

<h2>Home</h2>
<ul>
    <@s.url action="home" namespace="/" includeParams="none" id="homeURL"/>
    <@s.url action="rssFeeds" namespace="/static" includeParams="none" id="rssFeedURL"/>
	<@s.url action="rssInfo" namespace="/static" includeParams="none" id="rssInfoURL"/>
    <@s.url action="releaseNotes" namespace="/static" includeParams="none" id="releaseURL"/>
    <@s.url namespace="/user/secure" includeParams="none" id="loginURL" action="secureRedirect" goTo="${thisPage}"/>
	
    <li><@s.a href="${homeURL}" title="PLoS Pathogens | Home page">Home page</@s.a></li>
    <li><@s.a href="${rssFeedURL}" title="PLoS Pathogens | RSS Feeds">RSS Feeds</@s.a></li>
			<ul>
			<li><@s.a href="${rssInfoURL}" title="PLoS Computational Biology | About PLoS RSS Feeds">About PLoS RSS Feeds</@s.a></li>
		</ul>
    <li><@s.a href="${loginURL}" title="PLoS Pathogens | Account Login">Login</@s.a></li>
    <li><@s.a href="${freemarker_config.registrationURL}" title="PLoS Pathogens | Create a New Account">Create Account</@s.a></li>
    <li><@s.a href="${feedbackURL}" title="PLoS Pathogens | Send Us Your Feedback">Send Us Feedback</@s.a></li>
    <li><@s.a href="${releaseURL}" title="PLoS Pathogens | Release Notes">Release Notes</@s.a></li>
</ul>

<h2>Browse Articles</h2>
<ul>
	<@s.url action="browseIssue" field="issue" namespace="/article" includeParams="none" id="browseIssueURL"/>
	<@s.url action="browse" field="date" namespace="/article" includeParams="none" id="browseDateURL"/>
    <@s.url action="browse" namespace="/article" includeParams="none" id="browseSubURL"/>
	
	<li><@s.a href="${tocStatic}" title="PLoS Pathogens | Current Issue">Current Issue</@s.a></li><!-- Note that this is a temporary var to static TOC. Once dynamic TOC is in place, should be changed back to "browseIssueURL" -->
    <li><@s.a href="${browseDateURL}" title="PLoS Pathogens | Browse by Publication Date">By Publication Date</@s.a></li>
    <li><@s.a href="${browseSubURL}" title="PLoS Pathogens | Browse by Subject">By Subject</@s.a></li>
</ul>

<h2>About</h2>
 <ul>
    <@s.url action="information" namespace="/static" includeParams="none" id="infoURL"/>
    <@s.url action="edboard" namespace="/static" includeParams="none" id="edboardURL"/>
    <@s.url action="eic" namespace="/static" includeParams="none" id="eicURL"/>
    <@s.url action="license" namespace="/static" includeParams="none" id="licenseURL"/>
    <@s.url action="contact" namespace="/static" includeParams="none" id="contactURL"/>
	
    <li><@s.a href="${infoURL}" title="PLoS Pathogens | Journal Information">Journal Information</@s.a></li>
    <li><@s.a href="${edboardURL}" title="PLoS Pathogens | Editorial Board">Editorial Board</@s.a></li>
    <li><@s.a href="${eicURL}" title="PLoS Pathogens | Editor-in-Chief">Editor-in-Chief</@s.a></li>
    <li><@s.a href="${licenseURL}" title="PLoS Pathogens | Open-Access License">Open-Access License</@s.a></li>
	<li><@s.a href="http://www.plos.org/journals/embargopolicy.html">Media Inquiries</@s.a></li>
	<li><@s.a href="http://www.plos.org/journals/print.html">PLoS in Print</@s.a></li>
    <li><@s.a href="${contactURL}" title="PLoS Pathogens | Contact Us">Contact Us</@s.a></li>
  </ul>
  
 <h2>For Readers</h2>
<ul>
    <@s.url action="commentGuidelines" namespace="/static" includeParams="none" id="commentURL"/>
    <@s.url action="ratingGuidelines" namespace="/static" includeParams="none" id="ratingURL"/>
    <@s.url action="help" namespace="/static" includeParams="none" id="helpURL"/>
    <@s.url action="downloads" namespace="/static" includeParams="none" id="downloadsURL"/>
	
    <li><@s.a href="${commentURL}" title="PLoS Pathogens | Guidelines for Notes, Comments, and Corrections">Guidelines for Notes, Comments, and Corrections</@s.a></li>
    <li><@s.a href="${ratingURL}" title="PLoS Pathogens | Guidelines for Rating">Guidelines for Rating</@s.a></li>
    <li><@s.a href="${helpURL}" title="PLoS Pathogens | Help Using this Site">Help Using This Site</@s.a></li>
    <li>Site Map</li>
</ul>

<h2>For Authors and Reviewers</h2>
<ul>
	<@s.url action="policies" namespace="/static" includeParams="none" id="policiesURL"/>
	<@s.url action="competing" namespace="/static" includeParams="none" id="competingURL"/>
	<@s.url action="guidelines" namespace="/static" includeParams="none" id="guidelinesURL"/>
	<@s.url action="latexGuidelines" namespace="/static" includeParams="none" id="latexGuidelinesURL"/>
	<@s.url action="figureGuidelines" namespace="/static" includeParams="none" id="figureGuidelinesURL"/>
	<@s.url action="checklist" namespace="/static" includeParams="none" id="cklistURL"/>
	<@s.url action="reviewerGuidelines" namespace="/static" includeParams="none" id="reviewerGuidelinesURL"/>

	<li><@s.a href="${policiesURL}" title="PLoS Pathogens | Editorial and Publishing Policies">Editorial and Publishing Policies</@s.a></li>
	<ul>
		<li><@s.a href="${competingURL}" title="PLoS Pathogens | Competing Interests Policy">Competing Interests Policy</@s.a></li>
	</ul>
	<li><@s.a href="${guidelinesURL}" title="PLoS Pathogens | Guidelines for Authors">Author Guidelines</@s.a></li>
	<ul>
		<li><@s.a href="${latexGuidelinesURL}" title="PLoS Pathogens | LaTeX Guidelines">LaTeX Guidelines</@s.a></li>
	</ul>
	<li><@s.a href="${figureGuidelinesURL}" title="PLoS Pathogens | Guidelines for Table and Figure Preparation">Figure and Table Guidelines</@s.a></li>
	<li><@s.a href="${cklistURL}" title="PLoS Pathogens | Manuscript Submission Checklist">Submit Your Manuscript</@s.a></li>
	<ul>
		<li><a href="http://pathogens.plosjms.org/" title="PLoS Pathogens | Online Manuscript Submission and Review System">Submitting  Manuscript</a></li>
	</ul>
	<li><@s.a href="${reviewerGuidelinesURL}" title="PLoS Pathogens | Reviewer Guidelines ">Reviewer Guidelines</@s.a></li>
</ul>
				
<h2>General Links</h2>
<ul>
    <@s.url action="privacy" namespace="/static" includeParams="none" id="privacyURL"/>
    <@s.url action="terms" namespace="/static" includeParams="none" id="termsURL"/>
	
    <li><@s.a href="${privacyURL}" title="PLoS Pathogens | Privacy Statement">Privacy Statement</@s.a></li>
    <li><@s.a href="${termsURL}" title="PLoS Pathogens | Terms of Use">Terms of Use</@s.a></li>
    <li><a href="http://www.plos.org/advertise/" title="PLoS.org | Advertise">Advertise</a></li>
    <li><a href="http://www.plos.org/journals/embargopolicy.html" title="PLoS.org | Media Inquiries">Media Inquiries
	 <li><a href="http://www.plos.org/journals/print.html" title="PLoS.org | PLoS in Print">PLoS in Print</a></li>
	</a></li>
 
</ul>

<h2>PLoS Journals</h2>
<ul>
    <li><@s.a href="http://www.plosjournals.org/" title="PLoSJournals.org">All PLoS Journals</@s.a></li>
    <li><@s.a href="http://biology.plosjournals.org/" title="PLoSBiology.org"><em>PLoS Biology</em></@s.a></li>
    <li><@s.a href="http://medicine.plosjournals.org/" title="PLoSMedicine.org"><em>PLoS Medicine</em></@s.a></li>
    <li><@s.a href="http://www.ploscompbiol.org/" title="PLoSCompBiol.org"><em>PLoS Computational Biology</em></@s.a></li>
    <li><@s.a href="http://www.plosgenetics.org/" title="PLoSGenetics.org"><em>PLoS Genetics</em></@s.a></li>
    <li><@s.a href="http://www.plospathogens.org/" title="PLoSPathogens.org"><em>PLoS Pathogens</em></@s.a></li>
    <li><@s.a href="http://www.plosone.org/" title="PLoSONE.org"><em>PLoS ONE</em></@s.a></li>
    <li><@s.a href="http://www.plosntds.org/" title="PLoSNTDs.org"><em>PLoS Neglected Tropical Diseases</em></@s.a></li>
</ul>

<h2>PLoS Hubs</h2>
<ul>
    <li><@s.a href="http://clinicaltrials.ploshubs.org" title="PLoS Hub for Clinical Trials">Clinical Trials</@s.a></li>
</ul>

<h2>PLoS.org</h2>
<ul>
    <li><@s.a href="http://www.plos.org/" title="PLoS.org">PLoS.org</@s.a></li>
    <li><@s.a href="http://www.plos.org/oa/index.html" title="PLoS.org | Open Access">Open Access</@s.a></li>
    <li><@s.a href="http://www.plos.org/support/donate.php" title="PLoS.org | Join PLoS">Join PLoS</@s.a></li>
    <li><@s.a href="http://www.plos.org/cms/blog" title="PLoS.org | PLoS Blog">PLoS Blog</@s.a></li>
    <li><@s.a href="http://www.plos.org/connect.html" title="PLoS.org | Stay Connected">Stay Connected</@s.a></li>
</ul>

</div>
<!-- end : main contents -->