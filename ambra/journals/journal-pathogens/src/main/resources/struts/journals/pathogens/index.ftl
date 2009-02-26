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
              <h2>Featured Review</h2>
              <@s.url id="featured" namespace="/article" action="fetchArticle" articleURI="info:doi/10.1371/journal.ppat.0040034"/>

              <div class="article section lastSection">
                <h3><@s.a href="${featured}" title="Read Open-Access Article">Parallels between Pathogens and Gluten Peptides in Celiac Sprue 
                </@s.a></h3>
                <img src="images/home/ppat_0040034_hp.jpg" alt="article image" />
                <p>Bethune and Khosla argue that in celiac sprue, a disease triggered by partially hydrolyzed gluten peptides in the small intestine, the offending immunotoxins have many hallmarks of classical pathogens.</p>
                <div class="clearer">&nbsp;</div>
              </div>
            </div>
            <!-- end block -->
            <div class="block">
              <h2>Featured Research</h2>
              <@s.url id="newNoted1" namespace="/article" action="fetchArticle" articleURI="info:doi/10.1371/journal.ppat.1000012"/>
              <@s.url id="newNoted2" namespace="/article" action="fetchArticle" articleURI="info:doi/10.1371/journal.ppat.1000021"/>
              <@s.url id="newNoted3" namespace="/article" action="fetchArticle" articleURI="info:doi/10.1371/journal.ppat.1000003"/>
              <div class="article section">
                <h3><@s.a href="${newNoted1}" title="Read Open-Access Article">Multiple Reassortment Events in the Evolutionary History of H1N1 Influenza A Virus Since 1918</@s.a></h3>
                <img src="images/home/ppat_1000012_hp.jpg" alt="article image" />
                <p>Nelson et al use representative whole-genome sequences of A/H1N1 influenza virus sampled between 1918 - 2005 to show reassortment is an important factor in the long-term evolution of influenza A virus, including the periodic emergence of epidemic viruses.</p>
                <div class="clearer">&nbsp;</div>
              </div>
              <div class="article section">
                <h3><@s.a href="${newNoted2}" title="Read Open-Access Article">RNA Interference Screen Identifies Abl Kinase and PDGFR Signaling in <em>Chlamydia trachomatis</em> Entry</@s.a></h3>
                <img src="images/home/ppat_1000021_hp.jpg" alt="article image" />
                <p>Elwell et al use a large scale RNA interference screen to identify host factors essential for early steps in <em>C. trachomatis</em> infection, aiding the limited understanding of a crucial phase in the spread of this global infection.</p>
                <div class="clearer">&nbsp;</div>
              </div>
              <div class="article section lastSection">
                <h3><@s.a href="${newNoted3}" title="Read Open-Access Article">Evolution of a TRIM5-CypA Splice Isoform in Old World Monkeys</@s.a></h3>
                <img src="images/home/ppat_1000003_hp.jpg" alt="article image" />
                <p>Newman et al have identified a gene in Asian monkeys that may have evolved as a defense against lentiviruses, suggesting that AIDS is not a modern epidemic.  </p>
                <div class="clearer">&nbsp;</div>
              </div>
            </div><!-- end : block -->
            <div class="other block">
              <h2>Other PLoS Content</h2>
              <div class="section">
                <h3><a href="http://www.plosntds.org/"><em>PLoS Neglected Tropical Diseases</em></a></h3>
                <ul class="articles">
                  <li><a href="http://www.plosntds.org/article/info%3Adoi%2F10.1371%2Fjournal.pntd.0000175" title="Read Open Access Article">Increased Risk for <em>Entamoeba histolytica</em> Infection and Invasive Amebiasis in HIV Seropositive Men Who Have Sex with Men in Taiwan </a></li>
                </ul>
              </div>
              <div class="section lastSection">
                <h3><a href="http://medicine.plosjournals.org/"><em>PLoS Medicine</em></a></h3>
                <ul class="articles">
                  <li><a href="http://medicine.plosjournals.org/perlserv/?request=get-document&doi=10.1371%2Fjournal.pmed.0050055" title="Read Open Access Article">Eliminating Human African Trypanosomiasis: Where Do We Stand and What Comes Next </a></li>
         <li><a href="http://medicine.plosjournals.org/perlserv/?request=get-document&doi=10.1371%2Fjournal.pmed.0050032" title="Read Open Access Article">A Collaborative Epidemiological Investigation into the Criminal Fake Artesunate Trade in South East Asia</a></li>
                </ul>
              </div>
            </div><!-- end : other block -->
          </div><!-- end : col last -->
        </div><!-- end : wrapper for cols 1 & 2 -->
        <!-- begin : wrapper for cols 3 & 4 -->
        <div id="second" class="col">
          <!-- begin : col 3 -->
          <div class="subcol first">
            <div id="issue" class="block"><h3><a href="${browseIssueURL}">February 2008 Issue</a></h3><a href="${browseIssueURL}"><img src="images/home/ppat_04_02_251px.jpg" alt="issue cover image" /></a></div><!-- keep div#issue hmtl all on one line to avoid extra space below issue image in IE -->
            <!-- end : issue block -->
            <!-- begin : mission block -->
            <div id="mission" class="block">
              <p><strong><em><a href="${info}">PLoS Pathogens</a></em></strong> is an <a href="${license}">open-access</a> journal that publishes important new ideas on bacteria, fungi, parasites, prions, and viruses that contribute to our understanding of the biology of pathogens and pathogen-host interactions.</p>
            </div>
            <!-- end : mission block -->
            <!-- begin : advocacy blocks -->
            <div id="adWrap">
              <script language='JavaScript' type='text/javascript' src='http://ads.plos.org/adx.js'></script>
              <script language='JavaScript' type='text/javascript'>
              <!--
                 if (!document.phpAds_used) document.phpAds_used = ',';
                 phpAds_random = new String (Math.random()); phpAds_random = phpAds_random.substring(2,11);
                 
                 document.write ("<" + "script language='JavaScript' type='text/javascript' src='");
                 document.write ("http://ads.plos.org/adjs.php?n=" + phpAds_random);
                 document.write ("&#38;what=zone:157&#38;source=PAT&#38;block=1");
                 document.write ("&#38;exclude=" + document.phpAds_used);
                 if (document.referrer)
                    document.write ("&#38;referer=" + escape(document.referrer));
                 document.write ("'><" + "/script>");
              //-->
              </script><noscript><a href='http://ads.plos.org/adclick.php?n=aa6c94ce' target='_blank'><img src='http://ads.plos.org/adview.php?what=zone:157&#38;source=PAT&#38;n=aa6c94ce' border='0' alt=''/></a></noscript>
              <script language='JavaScript' type='text/javascript' src='http://ads.plos.org/adx.js'></script>
              <script language='JavaScript' type='text/javascript'>
              <!--
                 if (!document.phpAds_used) document.phpAds_used = ',';
                 phpAds_random = new String (Math.random()); phpAds_random = phpAds_random.substring(2,11);
                 
                 document.write ("<" + "script language='JavaScript' type='text/javascript' src='");
                 document.write ("http://ads.plos.org/adjs.php?n=" + phpAds_random);
                 document.write ("&#38;what=zone:158&#38;source=PAT&#38;block=1");
                 document.write ("&#38;exclude=" + document.phpAds_used);
                 if (document.referrer)
                    document.write ("&#38;referer=" + escape(document.referrer));
                 document.write ("'><" + "/script>");
              //-->
              </script><noscript><a href='http://ads.plos.org/adclick.php?n=a218be41' target='_blank'><img src='http://ads.plos.org/adview.php?what=zone:158&#38;source=PAT&#38;n=a218be41' border='0' alt=''/></a></noscript>
              <script language='JavaScript' type='text/javascript' src='http://ads.plos.org/adx.js'></script>
              <script language='JavaScript' type='text/javascript'>
              <!--
                 if (!document.phpAds_used) document.phpAds_used = ',';
                 phpAds_random = new String (Math.random()); phpAds_random = phpAds_random.substring(2,11);
                 
                 document.write ("<" + "script language='JavaScript' type='text/javascript' src='");
                 document.write ("http://ads.plos.org/adjs.php?n=" + phpAds_random);
                 document.write ("&#38;what=zone:159&#38;source=PAT&#38;block=1");
                 document.write ("&#38;exclude=" + document.phpAds_used);
                 if (document.referrer)
                    document.write ("&#38;referer=" + escape(document.referrer));
                 document.write ("'><" + "/script>");
              //-->
              </script><noscript><a href='http://ads.plos.org/adclick.php?n=aa64352f' target='_blank'><img src='http://ads.plos.org/adview.php?what=zone:159&#38;source=PAT&#38;n=aa64352f' border='0' alt=''/></a></noscript>
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
                 document.write ("&#38;what=zone:30&#38;source=PAT&#38;block=1&#38;blockcampaign=1");
                 document.write ("&#38;exclude=" + document.phpAds_used);
                 if (document.referrer)
                    document.write ("&#38;referer=" + escape(document.referrer));
                 document.write ("'><" + "/script>");
              //-->
              </script><noscript><a href='http://ads.plos.org/adclick.php?n=ad985188' target='_blank'><img src='http://ads.plos.org/adview.php?what=zone:30&#38;source=PAT&#38;n=ad985188' border='0' alt=''/></a></noscript>
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
