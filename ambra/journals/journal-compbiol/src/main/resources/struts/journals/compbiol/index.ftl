<#--
  $HeadURL::                                                                            $
  $Id$
  
  Copyright (c) 2007-2009 by Topaz, Inc.
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
<div id="content">
  <!-- begin : home page wrapper -->
  <div id="wrap">
    <div id="home">
      <!-- begin : layout wrapper -->
      <div class="col">
        <!-- begin : wrapper for cols 1 & 2 -->
        <div id="first" class="col">
        <!-- SWT removed col 1 -->
        <!-- begin : col 2 -->
          <div class="col last">
            <div class="horizontalTabs">
              <ul id="tabsContainer"></ul>
              <div id="tabPaneSet" class="contentwrap">
                <#include "/article/recentArticles.ftl">
              </div>
            </div><!-- end : horizontalTabs -->
            <!-- begin : calls to action blocks -->
            <div class="ctaWrap">
              <div id="cta1">
                <strong>Publish with PLoS</strong>
                <a href="${checklist}">We want to publish your work</a>
              </div>
              <div id="cta2">
                <strong>Have Your Say</strong>
                <a href="${comment}">Add ratings and discussions</a>
              </div>
              <div class="clearer">&nbsp;</div>
            </div>
            <!-- end : calls to action blocks -->
            <div class="block">
              <h2>Message from ISCB</h2>
              <@s.url id="featured" namespace="/article" action="fetchArticle" articleURI="info:doi/10.1371/journal.pcbi.0040016"/>
              <div class="article section lastSection">
                <h3><@s.a href="${featured}" title="Read Open-Access Article">Getting Started in Biological Pathway Construction and Analysis</@s.a></h3>
                <img src="images/home/message_iscb_hp.gif" alt="article image" />
                <p>From the Mount Sinai School of Medicine, Ganesh Viswanathan and colleagues provide an introduction to pathway building and a brief, practical orientation to existing knowledgebases.</p>
                <div class="clearer">&nbsp;</div>
              </div>
            </div>
            <!-- end block -->

            <div class="block">
              <h2>Education</h2>
              <@s.url id="featured" namespace="/article" action="fetchArticle" articleURI="info:doi/10.1371/journal.pcbi.0040006"/>
              <div class="article section lastSection">
                <h3><@s.a href="${featured}" title="Read Open-Access Article">Comprehensive Analysis of Affymetrix Exon Arrays Using BioConductor </@s.a></h3>
                <img src="images/home/ISMB2007_90.gif" alt="article image" />
                <p>Okoniewski and Miller introduce BioConductor, a collection of open source software packages designed to support the analysis of biological data, in this Tutorial for <em>PLoS Computational Biology</em>.</p>
                <div class="clearer">&nbsp;</div>
              </div>
            </div>
            <!-- end block -->

            <div class="block">
              <h2>Review</h2>
              <@s.url id="featured" namespace="/article" action="fetchArticle" articleURI="info:doi/10.1371/journal.pcbi.0040012"/>
              <div class="article section lastSection">
                <h3><@s.a href="${featured}" title="Read Open-Access Article">Computational Methods for Protein Identification from Mass Spectrometry Data </@s.a></h3>
                <img src="images/home/pcbi_0040012_hp.jpg" alt="article image" />
                <p>Arthur and McHugh from the University of Sydney undertake a systematic review of the currently available methods and algorithms for interpreting, managing, and analyzing biological data associated with protein identification.</p>
                <div class="clearer">&nbsp;</div>
              </div>
            </div>
            <!-- end block -->


            <div class="block">
              <h2>Featured Research</h2>
              <@s.url id="newNoted1" namespace="/article" action="fetchArticle" articleURI="info:doi/10.1371/journal.pcbi.1000023"/>
              <@s.url id="newNoted2" namespace="/article" action="fetchArticle" articleURI="info:doi/10.1371/journal.pcbi.0040030"/>
              <@s.url id="newNoted3" namespace="/article" action="fetchArticle" articleURI="info:doi/10.1371/journal.pcbi.0040029"/>

             <div class="article section">
                <h3><@s.a href="${newNoted1}" title="Read Open-Access Article">A Continuum Model for Metabolic Gas Exchange in Pear Fruit</@s.a></h3>
                <img src="images/home/pcbi_1000023_hp.jpg" alt="article image" />
                <p>Bart Nicola&#239; and colleagues from Katholieke Universiteit Leuven, Belgium create the most comprehensive model to date for simulating gas exchange in plant tissue and evaluating the effect of environmental stresses on fruit.</p>
                <div class="clearer">&nbsp;</div>
              </div> 
              <div class="article section">
                <h3><@s.a href="${newNoted2}" title="Read Open-Access Article">Stimulus Design for Model Selection and Validation in Cell Signaling</@s.a></h3>
                <img src="images/home/pcbi_0040030_hp.jpg" alt="article image" />
                <p>Bruce Tidor et al. from MIT address the problem of model ambiguity in biological signaling. Their results provide a strong basis for using designed input stimuli as a tool for the development of cell signaling models.</p>
                <div class="clearer">&nbsp;</div>
              </div>
              <div class="article section lastSection">
                <h3><@s.a href="${newNoted3}" title="Read Open-Access Article">Selective Adaptation in Networks of Heterogeneous Populations: Model, Simulation and Experiment</@s.a></h3>
                <img src="images/home/pcbi_0040029_hp.jpg" alt="article image" />
                <p>In the visual modality, a target violating a surrounding pattern - a 'pop-out' image - is easily detected. Wallach et al. develop a mechanistic model that demonstrates how a relatively simple system may express the phenomenon of such selective behavior.</p>
                <div class="clearer">&nbsp;</div>
              </div>
             
 
            </div><!-- end : block -->
            <div class="other block">
              <h2>Other PLoS Content</h2>
              <div class="section lastSection">
                <h3><a href="http://www.plosone.org/"><em>PLoS ONE</em></a></h3>
                <ul class="articles">
                  <li><a href="http://www.plosone.org/article/info%3Adoi%2F10.1371%2Fjournal.pone.0001562" title="Read Open Access Article">Protein Function Assignment through Mining Cross-Species Protein-Protein Interactions</a>
                  <!-- I dont think these should get blurbs <p>The authors, from the University of Ulster, assembled a global PPI network in human heart failure, establishing the significance of relationships between the differentiation of gene expression and connectivity degrees.</p>--></li>
                </ul>
              </div>
            </div><!-- end : other block -->
          </div><!-- end : col last -->
        </div><!-- end : wrapper for cols 1 & 2 -->
        <!-- begin : wrapper for cols 3 & 4 -->
        <div id="second" class="col">
          <!-- begin : col 3 -->
          <div class="subcol first">
            <div id="issue" class="block"><h3><a href="${browseIssueURL}">February 2008 Issue</a></h3><a href="${browseIssueURL}"><img src="images/home/pcbi_04_02_251px.jpg" alt="issue cover image" /></a></div><!-- keep div#issue hmtl all on one line to avoid extra space below issue image in IE -->
            <!-- end : issue block -->
            <!-- begin : mission block -->
            <div id="mission" class="block">
              <p><strong><em><a href="${info}">PLoS Computational Biology</a></em></strong> is a peer-reviewed, open-access journal featuring works of exceptional significance that further our understanding of living systems at all scales through the application of computational methods. It is the official journal of the <a href="http://www.iscb.org/">International Society for Computational Biology.</a></p>
            </div>
            <!-- end : mission block -->
            <!-- begin : iscb block -->
            <div id="iscb" class="block">
              <h3><a href="#"></a>ISCB</h3>
              <ul class="articles">
                  <li><a href="http://www.iscb.org/ismb2008/ismbnews.php">ISMB 2008 Toronto Calls for Participation</a></li>
                  <li><a href="http://www.iscb.org/ismb2008/special_session_details.php">Special Session Abstracts Now Online for ISMB 2008 Toronto</a></li>
                  <li><a href="http://www.iscb.org/cshals2008/">Announcing C-SHALS, Conference on Semantics in Healthcare and Life Sciences - Registration now open</a></li>
                  <li><a href="http://www.iscb.org/membership.shtml">Society Membership - Join Now</a></li>
              </ul>
            </div>
            <!-- end : iscb block -->
            <!-- begin : advocacy blocks -->
            <div id="adWrap">
            <script language='JavaScript' type='text/javascript' src='http://ads.plos.org/adx.js'></script>
            <script language='JavaScript' type='text/javascript'>
            <!--
               if (!document.phpAds_used) document.phpAds_used = ',';
               phpAds_random = new String (Math.random()); phpAds_random = phpAds_random.substring(2,11);
               
               document.write ("<" + "script language='JavaScript' type='text/javascript' src='");
               document.write ("http://ads.plos.org/adjs.php?n=" + phpAds_random);
               document.write ("&#38;what=zone:156&#38;source=CBI&#38;block=1");
               document.write ("&#38;exclude=" + document.phpAds_used);
               if (document.referrer)
                  document.write ("&#38;referer=" + escape(document.referrer));
               document.write ("'><" + "/script>");
            //-->
            </script><noscript><a href='http://ads.plos.org/adclick.php?n=a341eeed' target='_blank'><img src='http://ads.plos.org/adview.php?what=zone:156&#38;source=CBI&#38;n=a341eeed' border='0' alt=''/></a></noscript>
            <script language='JavaScript' type='text/javascript' src='http://ads.plos.org/adx.js'></script>
            <script language='JavaScript' type='text/javascript'>
            <!--
               if (!document.phpAds_used) document.phpAds_used = ',';
               phpAds_random = new String (Math.random()); phpAds_random = phpAds_random.substring(2,11);
               
               document.write ("<" + "script language='JavaScript' type='text/javascript' src='");
               document.write ("http://ads.plos.org/adjs.php?n=" + phpAds_random);
               document.write ("&#38;what=zone:155&#38;source=CBI&#38;block=1");
               document.write ("&#38;exclude=" + document.phpAds_used);
               if (document.referrer)
                  document.write ("&#38;referer=" + escape(document.referrer));
               document.write ("'><" + "/script>");
            //-->
            </script><noscript><a href='http://ads.plos.org/adclick.php?n=ae158f67' target='_blank'><img src='http://ads.plos.org/adview.php?what=zone:155&#38;source=CBI&#38;n=ae158f67' border='0' alt=''/></a></noscript>
            <script language='JavaScript' type='text/javascript' src='http://ads.plos.org/adx.js'></script>
            <script language='JavaScript' type='text/javascript'>
            <!--
               if (!document.phpAds_used) document.phpAds_used = ',';
               phpAds_random = new String (Math.random()); phpAds_random = phpAds_random.substring(2,11);
               
               document.write ("<" + "script language='JavaScript' type='text/javascript' src='");
               document.write ("http://ads.plos.org/adjs.php?n=" + phpAds_random);
               document.write ("&#38;what=zone:154&#38;source=CBI&#38;block=1");
               document.write ("&#38;exclude=" + document.phpAds_used);
               if (document.referrer)
                  document.write ("&#38;referer=" + escape(document.referrer));
               document.write ("'><" + "/script>");
            //-->
            </script><noscript><a href='http://ads.plos.org/adclick.php?n=ae53cfec' target='_blank'><img src='http://ads.plos.org/adview.php?what=zone:154&#38;source=CBI&#38;n=ae53cfec' border='0' alt=''/></a></noscript>
            </div>
            <!-- end : advocacy blocks -->
            <!-- begin : stay-connected block -->
            <div id="connect" class="block">
              <h3>Stay Connected</h3>
              <ul>
                  <li><img src="images/icon_alerts_small.gif" alt="email alerts icon" /><a href="${freemarker_config.registrationURL}"><strong>E-mail Alerts</strong></a><br />Sign up for alerts by e-mail</li>
                  <li><img src="images/icon_rss_small.gif" alt="rss icon" /><@s.url action="rssInfo" namespace="/static" includeParams="none" id="rssinfo"/><a href="${Request[freemarker_config.journalContextAttributeKey].baseUrl}${rssPath}"><strong>RSS</strong></a> (<a href="${rssinfo}">What is RSS?</a>)<br />Subscribe to content feed</li>
                  <li><img src="images/icon_join.gif" alt="join PLoS icon" /><a href="http://www.plos.org/support/donate.php" title="Join PLoS: Show Your Support"><strong>Join PLoS</strong></a><br />Support the open-access movement!</li>
              </ul>
            </div>
            <!-- end : stay-connected block -->
            <#include "/journals/plosJournals/blog.ftl">
          </div><!-- end : subcol first -->
          <!-- end : col 3 -->
          <!-- begin : col 4 -->
          <div class="subcol last">
            <div class="block banner"><!--skyscraper-->
              <script language='JavaScript' type='text/javascript' src='http://ads.plos.org/adx.js'></script>
              <script language='JavaScript' type='text/javascript'>
              <!--
                 if (!document.phpAds_used) document.phpAds_used = ',';
                 phpAds_random = new String (Math.random()); phpAds_random = phpAds_random.substring(2,11);
                 
                 document.write ("<" + "script language='JavaScript' type='text/javascript' src='");
                 document.write ("http://ads.plos.org/adjs.php?n=" + phpAds_random);
                 document.write ("&#38;what=zone:24&#38;source=CBI&#38;block=1&#38;blockcampaign=1");
                 document.write ("&#38;exclude=" + document.phpAds_used);
                 if (document.referrer)
                    document.write ("&#38;referer=" + escape(document.referrer));
                 document.write ("'><" + "/script>");
              //-->
              </script><noscript><a href='http://ads.plos.org/adclick.php?n=a92cb003' target='_blank'><img src='http://ads.plos.org/adview.php?what=zone:24&#38;source=CBI&#38;n=a92cb003' border='0' alt=''/></a></noscript>
            </div><!-- end : block banner -->
          </div><!-- end : subcol last -->
        </div><!-- end : wrapper for cols 3 & 4 -->
        <div id="lower">&nbsp;</div> <!-- displays lower background image -->
      </div><!-- end : col -->
      <div class="partner">
        <a href="http://www.fedora-commons.org" title="Fedora-Commons.org"><img src="${freemarker_config.context}/images/home_fedoracommons.png" alt="Fedora-Commons.org"/></a>
        <a href="http://www.moore.org" title="Gorden and Betty Moore Foundation"><img src="${freemarker_config.context}/images/home_moore.gif" alt="Moore Foundation"/></a>
        <a href="http://www.mulgara.org/" title="Mulgara.org"><img src="${freemarker_config.context}/images/home_mulgara.gif" alt="Mulgara.org"/></a>
        <a href="http://www.sciencecommons.org/" title="Science Commons"><img src="${freemarker_config.context}/journals/plosJournals/images/home_sciencecommons.gif"  alt="Science Commons"/></a>
        <a href="http://www.unitedlayer.com/" title="UnitedLayer, LLC"><img src="${freemarker_config.context}/journals/plosJournals/images/home_unitedlayer.gif" alt="UnitedLayer, LLC"/></a>
      </div><!-- end : block partners -->
    </div><!-- end : home -->
  </div><!-- end : wrap -->
</div><!-- end : content -->
