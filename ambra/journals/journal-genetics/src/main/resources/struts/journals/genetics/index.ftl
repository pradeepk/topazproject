<#--
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
                <#include "article/recentArticles.ftl">
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
              <h2>Featured Interview</h2>
              <@s.url id="featured" namespace="/article" action="fetchArticle" articleURI="info:doi/10.1371/journal.pgen.1000002"/>
              <div class="article section lastSection">
                <h3><@s.a href="${featured}" title="Read Open-Access Article">Ready for Her Close-Up: An Interview with Elaine Strass</@s.a></h3>
                <img src="images/home/pgen_1000002_hp.jpg" alt="article image" />
                <p>Read Jane Gitschier's <@s.a href="${featured}" title="Read Open-Access Article">interview with Elaine Strass</@s.a>; Elaine has been the hidden force behind both the Genetics Society of America and the American Society of Human Genetics for almost 20 years.</p>
                <div class="clearer">&nbsp;</div>
              </div>
            </div>
            <!-- end block -->
            <div class="block">
              <h2>Featured Research</h2>
              <@s.url id="newNoted1" namespace="/article" action="fetchArticle" articleURI="info:doi/10.1371/journal.pgen.1000020"/>
              <@s.url id="newNoted2" namespace="/article" action="fetchArticle" articleURI="info:doi/10.1371/journal.pgen.1000010"/>
              <@s.url id="newNoted3" namespace="/article" action="fetchArticle" articleURI="info:doi/10.1371/journal.pgen.1000021"/>
              <div class="article section">
                <h3><@s.a href="${newNoted1}" title="Read Open-Access Article">Identification of Genetic and Chemical Modulators of Zebrafish Mechanosensory Hair Cell Death</@s.a></h3>
                <img src="images/home/pgen_1000020_hp.jpg" alt="article image" />
                <p>The combination of chemical screening with traditional genetic approaches offers a new strategy for identifying drugs and drug targets to attenuate hearing and balance disorders.</p>
                <div class="clearer">&nbsp;</div>
              </div>
              <div class="article section">
                <h3><@s.a href="${newNoted2}" title="Read Open-Access Article">Identification of the <em>Yellow Skin</em> Gene Reveals a Hybrid Origin of the Domestic Chicken</@s.a></h3>
                <img src="images/home/pgen_1000010_hp.jpg" alt="article image" />
                <p>This study provides the first conclusive evidence for a hybrid origin of the domestic chicken and has important implications for our views of the domestication process.</p>
                <div class="clearer">&nbsp;</div>
              </div>
              <div class="article section lastSection">
                <h3><@s.a href="${newNoted3}" title="Read Open-Access Article">The Mediator Subunit MDT-15 Confers Metabolic Adaptation to Ingested Material</@s.a></h3>
                <img src="images/home/pgen_1000021_hp.jpg" alt="article image" />
                <p>The authors propose a model whereby MDT-15 integrates several transcriptional regulatory pathways to monitor both the availability and quality of ingested materials, including nutrients and xenobiotic compounds.</p>
                <div class="clearer">&nbsp;</div>
              </div>
            </div><!-- end : block -->
            <div class="other block">
              <h2>Other PLoS Content</h2>
              <div class="section">
                <h3>Browse Articles in the Genetics and Genomics Subject Area</h3>
                <p><a href="http://biology.plosjournals.org/perlserv/?request=browse&method=advanced&search_fulltext=1&row_start=1&issn=1545-7885&jrn_issn=1545-7885&pubdate_browse=any&subject_browse=selected&subj_id=25&limit=10&order=online_date&ct=1#results"><em>PLoS Biology</em></a>; <a href="http://medicine.plosjournals.org/perlserv/?request=browse&method=advanced&search_fulltext=1&row_start=1&issn=1549-1676&jrn_issn=1549-1676&pubdate_browse=any&subject_browse=selected&subj_id=25&limit=10&order=online_date#results"><em>PLoS Medicine</em></a>; <a href="http://www.ploscompbiol.org/article/browse.action?catName=Genetics+and+Genomics&field="><em>PLoS Computational Biology</em></a>; <a href="http://www.plospathogens.org/article/browse.action?catName=Genetics+and+Genomics&field="><em>PLoS Pathogens</em></a>; <a href="http://www.plosone.org/article/browse.action?catName=Genetics+and+Genomics&field="><em>PLoS ONE</em></a>; <a href="http://www.plosntds.org/article/browse.action?catName=Genetics+and+Genomics&field="><em>PLoS Neglected Tropical Diseases</em></a></p>
              </div>
              <div class="section lastSection">
                <h3>Browse Article Collections</h3>
                <p><a href="http://collections.plos.org/plosbiology/index.php"><em>PLoS Biology</em></a>; <a href="http://collections.plos.org/plosmedicine/index.php"><em>PLoS Medicine</em></a>; <a href="http://collections.plos.org/ploscompbiol/index.php"><em>PLoS Computational Biology</em></a>; <a href="http://collections.plos.org/plosntds/index.php"><em>PLoS Neglected Tropical Diseases</em></a></p>
              </div>
            </div><!-- end : other block -->
          </div><!-- end : col last -->
        </div><!-- end : wrapper for cols 1 & 2 -->
        <!-- begin : wrapper for cols 3 & 4 -->
        <div id="second" class="col">
          <!-- begin : col 3 -->
          <div class="subcol first">
            <div id="issue" class="block"><h3><a href="${browseIssueURL}">February 2008 Issue</a></h3><a href="${browseIssueURL}"><img src="images/home/pgen_04_02_251px.jpg" alt="issue cover image" /></a></div><!-- keep div#issue hmtl all on one line to avoid extra space below issue image in IE -->
            <!-- end : issue block -->
            <!-- begin : mission block -->
            <div id="mission" class="block">
              <p><strong><em><a href="${info}">PLoS Genetics</a></em></strong> is a peer-reviewed, <a href="${license}">open-access</a> journal that reflects the full breadth and interdisciplinary nature of <a href="http://en.wikipedia.org/wiki/Genetics">genetics</a> and <a href="http://en.wikipedia.org/wiki/Genomics">genomics</a> research by publishing outstanding original contributions in all areas of biology. View a list of <a href="http://scholar.google.com/scholar?hl=en&lr=&q=&as_publication=PLoS+Genet&btnG=Search">Google Scholar</a> citations and read about our first <a href="static/information.action#if">ISI impact factor</a> of 7.67.</p>
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
               document.write ("&#38;what=zone:151&#38;source=GEN&#38;block=1");
               document.write ("&#38;exclude=" + document.phpAds_used);
               if (document.referrer)
                  document.write ("&#38;referer=" + escape(document.referrer));
               document.write ("'><" + "/script>");
            //-->
            </script><noscript><a href='http://ads.plos.org/adclick.php?n=ae3f32ae' target='_blank'><img src='http://ads.plos.org/adview.php?what=zone:151&#38;source=GEN&#38;n=ae3f32ae' border='0' alt=''/></a></noscript>
            <script language='JavaScript' type='text/javascript' src='http://ads.plos.org/adx.js'></script>
            <script language='JavaScript' type='text/javascript'>
            <!--
               if (!document.phpAds_used) document.phpAds_used = ',';
               phpAds_random = new String (Math.random()); phpAds_random = phpAds_random.substring(2,11);
               
               document.write ("<" + "script language='JavaScript' type='text/javascript' src='");
               document.write ("http://ads.plos.org/adjs.php?n=" + phpAds_random);
               document.write ("&#38;what=zone:152&#38;source=GEN&#38;block=1");
               document.write ("&#38;exclude=" + document.phpAds_used);
               if (document.referrer)
                  document.write ("&#38;referer=" + escape(document.referrer));
               document.write ("'><" + "/script>");
            //-->
            </script><noscript><a href='http://ads.plos.org/adclick.php?n=a85b49cf' target='_blank'><img src='http://ads.plos.org/adview.php?what=zone:152&#38;source=GEN&#38;n=a85b49cf' border='0' alt=''/></a></noscript>
            <script language='JavaScript' type='text/javascript' src='http://ads.plos.org/adx.js'></script>
            <script language='JavaScript' type='text/javascript'>
            <!--
               if (!document.phpAds_used) document.phpAds_used = ',';
               phpAds_random = new String (Math.random()); phpAds_random = phpAds_random.substring(2,11);
               
               document.write ("<" + "script language='JavaScript' type='text/javascript' src='");
               document.write ("http://ads.plos.org/adjs.php?n=" + phpAds_random);
               document.write ("&#38;what=zone:153&#38;source=GEN&#38;block=1");
               document.write ("&#38;exclude=" + document.phpAds_used);
               if (document.referrer)
                  document.write ("&#38;referer=" + escape(document.referrer));
               document.write ("'><" + "/script>");
            //-->
            </script><noscript><a href='http://ads.plos.org/adclick.php?n=a7f95508' target='_blank'><img src='http://ads.plos.org/adview.php?what=zone:153&#38;source=GEN&#38;n=a7f95508' border='0' alt=''/></a></noscript>
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
                 document.write ("&#38;what=zone:17&#38;source=GEN&#38;block=1&#38;blockcampaign=1");
                 document.write ("&#38;exclude=" + document.phpAds_used);
                 if (document.referrer)
                    document.write ("&#38;referer=" + escape(document.referrer));
                 document.write ("'><" + "/script>");
              //-->
              </script><noscript><a href='http://ads.plos.org/adclick.php?n=a51ac3e7' target='_blank'><img src='http://ads.plos.org/adview.php?what=zone:17&#38;source=GEN&#38;n=a51ac3e7' border='0' alt=''/></a></noscript>
            </div><!-- end : block banner -->
          </div><!-- end : subcol last -->
        </div><!-- end : wrapper for cols 3 & 4 -->
        <div id="lower">&nbsp;</div> <!-- displays lower background image -->
      </div><!-- end : col -->
      <div class="partner">
        <a href="http://www.fedora-commons.org" title="Fedora-Commons.org"><img src="${freemarker_config.context}/images/home_fedoracommons.png" alt="Fedora-Commons.org"/></a>
        <a href="http://www.moore.org" title="Gorden and Betty Moore Foundation"><img src="${freemarker_config.context}/images/home_moore.gif" alt="Moore Foundation"/></a>
        <a href="http://www.mulgara.org/" title="Mulgara.org"><img src="${freemarker_config.context}/images/home_mulgara.gif" alt="Mulgara.org"/></a>
        <a href="http://www.sciencecommons.org/" title="Science Commons"><img src="${freemarker_config.context}/images/home_sciencecommons.gif"  alt="Science Commons"/></a>
        <a href="http://www.unitedlayer.com/" title="UnitedLayer, LLC"><img src="${freemarker_config.context}/images/home_unitedlayer.gif" alt="UnitedLayer, LLC"/></a>
      </div><!-- end : block partners -->
    </div><!-- end : home -->
  </div><!-- end : wrap -->
</div><!-- end : content -->
