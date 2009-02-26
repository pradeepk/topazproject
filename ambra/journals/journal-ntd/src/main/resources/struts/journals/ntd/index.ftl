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
        <@s.url id="featured1" namespace="/article" action="fetchArticle" articleURI="info:doi/10.1371/journal.pntd.0000201"/>
              <h2>Featured Editorial</h2>
              <div class="article section lastSection">
                <h3><@s.a href="${featured1}" title="Read Open-Access Article">Reinventing Guantanamo: From Detainee Facility to Center for Research on Neglected Diseases of Poverty in the Americas</@s.a></h3>
                <@s.a href="${featured1}" title="Read Open-Access Article"><img src="images/home/icon_editorial.gif" alt="article image" /></@s.a>
                <p>If the United States government transformed Cuba's Guantanamo Bay facility into a biomedical research institute, it could directly address the poverty-promoting diseases and health disparities of the Americas.</p>
                <div class="clearer">&nbsp;</div>
              </div>
       </div>
       <!-- end block -->

       <div class="block">
        <@s.url id="featured2" namespace="/article" action="fetchArticle" articleURI="info:doi/10.1371/journal.pntd.0000183"/>
        <@s.url id="featured3" namespace="/article" action="fetchArticle" articleURI="info:doi/10.1371/journal.pntd.0000192"/>
        <@s.url id="featured4" namespace="/article" action="fetchArticle" articleURI="info:doi/10.1371/journal.pntd.0000158"/>
         <@s.url id="featured5" namespace="/article" action="fetchArticle" articleURI="info:doi/10.1371/journal.pntd.0000203"/>
        <@s.url id="featured6" namespace="/article" action="fetchArticle" articleURI="info:doi/10.1371/journal.pntd.0000190"/>
             <h2>Featured Research</h2>
              <div class="article section">
                <h3><@s.a href="${featured2}" title="Read Open-Access Article">T Helper 1-Inducing Adjuvant Protects against Experimental Paracoccidioidomycosis</@s.a></h3>
                <@s.a href="${featured2}" title="Read Open-Access Article"><img src="images/home/article_v02_i03_panunto.jpg" alt="article image" /></@s.a>
                <p>Researchers at University of S&atilde;o Paulo show that injecting a T helper (Th) 1-stimulating adjuvant in <em>P. brasiliensis</em>infected mice has a beneficial effect on the course of paracoccidioidomycosis, the most prevalent human systemic fungal infection in Latin America.</p>
                <div class="clearer">&nbsp;</div>
              </div>
              <div class="article section">
                <h3><@s.a href="${featured3}" title="Read Open-Access Article">Infections with Immunogenic Trypanosomes Reduce Tsetse Reproductive Fitness: Potential Impact of Different Parasite Strains on Vector Population Structure</@s.a></h3>
                <@s.a href="${featured3}" title="Read Open-Access Article"><img src="images/home/article_v02_i03_aksoy.jpg" alt="article image" /></@s.a>
                <p>Changyun Hu, Rita Rio, and colleagues report on tsetse-trypanosome interactions, with a focus on host immune response activation of gut infections and its subsequent impact on tsetse's reproductive fitness.</p>
                <div class="clearer">&nbsp;</div>
              </div>
 
               <div class="article section">
                <h3><@s.a href="${featured4}" title="Read Open-Access Article">Decision-Model Estimation of the Age-Specific Disability Weight for Schistosomiasis Japonica: A Systematic Review of the Literature</@s.a></h3>
                <@s.a href="${featured4}" title="Read Open-Access Article"><img src="images/home/article_v02_i03_finkelstein.jpg" alt="article image" /></@s.a>
                <p>Researchers use a decision model to quantify an alternative disability weight estimate of the global burden of schistosomiasis japonica, and they conclude that the disease is far more debilitating than current estimates.</p>
        <p><@s.a href="${featured5}" title="Read Open-Access Article">Read Expert Commentary</@s.a></p>
        <p><small>Image Credit: CDC</small></p>
                <div class="clearer">&nbsp;</div>
              </div>
              <div class="article section lastSection">
                <h3><@s.a href="${featured6}" title="Read Open-Access Article">Novel Triazine JPC-2067-B Inhibits <em>Toxoplasma gondii</em> <em>In Vitro</em> and <em>In Vivo</em></@s.a></h3>
                <@s.a href="${featured6}" title="Read Open-Access Article"><img src="images/home/article_v02_i03_mcleod.jpg" alt="article image" /></@s.a>
                <p>The authors present toxicology data showing that a triazine currently being advanced to clinical trials for malaria has the potential to be a more effective and less toxic treatment for toxoplasmosis than currently available medicines.</p>
                <div class="clearer">&nbsp;</div>
              </div>
       </div>
            <!-- end block -->

            <div class="other block">
              <h2>Other PLoS Content</h2>
        <div class="section">
                <h3><a href="http://www.plosmedicine.org/"><em>PLoS Medicine</em></a></h3>
                <ul class="articles">
                  <li><a href="http://medicine.plosjournals.org/perlserv/?request=get-document&doi=10.1371/journal.pmed.0050055" title="Read Open Access Article">Eliminating Human African Trypanosomiasis: Where Do We Stand and What Comes Next</a></li>
                  <li><a href="http://medicine.plosjournals.org/perlserv/?request=get-document&doi=10.1371/journal.pmed.0050059" title="Read Open Access Article">The Neglected Diseases Section in <em>PLoS Medicine</em>: Moving Beyond Tropical Infections</a></li>
        </ul>
       </div>
        
              <div class="section lastSection">
                <h3><a href="http://www.plosone.org/"><em>PLoS ONE</em></a></h3>
                <ul class="articles">
          <li><a href="http://www.plosone.org/article/info%3Adoi%2F10.1371%2Fjournal.pone.0001620" title="Read Open Access Article">The <em>Leishmania</em> ARL-1 and Golgi Traffic</a></li>
        </ul>
       </div>
            </div><!-- end : other block -->
          </div><!-- end : col last -->
        </div><!-- end : wrapper for cols 1 & 2 -->
        <!-- begin : wrapper for cols 3 & 4 -->
        <div id="second" class="col">
          <!-- begin : col 3 -->
          <div class="subcol first">
            <div id="issue" class="block"><h3><a href="${browseIssueURL}">February 2008 Issue</a></h3><a href="${browseIssueURL}"><img src="images/home/issue_v02_i02.jpg" alt="issue cover image" /></a></div><!-- keep div#issue hmtl all on one line to avoid extra space below issue image in IE -->
            <!-- end : issue block -->
            <!-- begin : mission block -->
            <div id="mission" class="block">
        <@s.url action="ratingGuidelines" namespace="/static" includeParams="none" id="rateGuideURL"/>

              <p><strong>Welcome to <em>PLoS Neglected Tropical Diseases</em></strong>, the first open-access journal devoted to the world's most <a href="${scope}">neglected tropical diseases</a>. <!--We encourage you to participate and <a href="${comment}">add your comments</a> to articles.--> We encourage you to <a href="${comment}">add your Notes, Comments</a>, and <@s.a href="${rateGuideURL}" title="Guidelines for Rating">Ratings</@s.a> to articles.</p>
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
   document.write ("&amp;what=zone:195&amp;source=NTD&amp;target=_top&amp;block=1");
   document.write ("&amp;exclude=" + document.phpAds_used);
   if (document.referrer)
      document.write ("&amp;referer=" + escape(document.referrer));
   document.write ("'><" + "/script>");
//-->
</script><noscript><a href='http://ads.plos.org/adclick.php?n=a1ec113d' target='_top'><img src='http://ads.plos.org/adview.php?what=zone:195&amp;source=NTD&amp;n=a1ec113d' border='0' alt=''></a></noscript>
<script language='JavaScript' type='text/javascript' src='http://ads.plos.org/adx.js'></script>
<script language='JavaScript' type='text/javascript'>
<!--
   if (!document.phpAds_used) document.phpAds_used = ',';
   phpAds_random = new String (Math.random()); phpAds_random = phpAds_random.substring(2,11);
   document.write ("<" + "script language='JavaScript' type='text/javascript' src='");
   document.write ("http://ads.plos.org/adjs.php?n=" + phpAds_random);
   document.write ("&amp;what=zone:196&amp;source=NTD&amp;target=_top&amp;block=1");
   document.write ("&amp;exclude=" + document.phpAds_used);
   if (document.referrer)
      document.write ("&amp;referer=" + escape(document.referrer));
   document.write ("'><" + "/script>");
//-->
</script><noscript><a href='http://ads.plos.org/adclick.php?n=ace5c997' target='_top'><img src='http://ads.plos.org/adview.php?what=zone:196&amp;source=NTD&amp;n=ace5c997' border='0' alt=''></a></noscript>
<script language='JavaScript' type='text/javascript' src='http://ads.plos.org/adx.js'></script>
<script language='JavaScript' type='text/javascript'>
<!--
   if (!document.phpAds_used) document.phpAds_used = ',';
   phpAds_random = new String (Math.random()); phpAds_random = phpAds_random.substring(2,11);
   document.write ("<" + "script language='JavaScript' type='text/javascript' src='");
   document.write ("http://ads.plos.org/adjs.php?n=" + phpAds_random);
   document.write ("&amp;what=zone:197&amp;source=NTD&amp;target=_top&amp;block=1");
   document.write ("&amp;exclude=" + document.phpAds_used);
   if (document.referrer)
      document.write ("&amp;referer=" + escape(document.referrer));
   document.write ("'><" + "/script>");
//-->
</script><noscript><a href='http://ads.plos.org/adclick.php?n=aec547bc' target='_top'><img src='http://ads.plos.org/adview.php?what=zone:197&amp;source=NTD&amp;n=aec547bc' border='0' alt=''></a></noscript>
            </div><!--ad wrap-->
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
   document.write ("&amp;what=zone:192&amp;source=NTD&amp;target=_top&amp;block=1&amp;blockcampaign=1");
   document.write ("&amp;exclude=" + document.phpAds_used);
   if (document.referrer)
      document.write ("&amp;referer=" + escape(document.referrer));
   document.write ("'><" + "/script>");
//-->
</script><noscript><a href='http://ads.plos.org/adclick.php?n=a93f3323' target='_top'><img src='http://ads.plos.org/adview.php?what=zone:192&amp;source=NTD&amp;n=a93f3323' border='0' alt=''></a></noscript>
            </div><!-- end : block banner -->
          </div><!-- end : subcol last -->
        </div><!-- end : wrapper for cols 3 & 4 -->
        <div id="lower">&nbsp;</div> <!-- displays lower background image -->
      </div><!-- end : col -->
      <div class="partner">
        <a href="http://www.gatesfoundation.org" title="Bill and Melinda Gates Foundation"><img src="${freemarker_config.context}/images/home_gatesFound.png" alt="Bill and Melinda Gates Foundation"/></a>
        <a href="http://www.fedora-commons.org" title="Fedora-Commons.org"><img src="${freemarker_config.context}/images/home_fedoracommons.png" alt="Fedora-Commons.org"/></a>
        <a href="http://www.moore.org" title="Gorden and Betty Moore Foundation"><img src="${freemarker_config.context}/images/home_moore.gif" alt="Moore Foundation"/></a>
        <a href="http://www.mulgara.org/" title="Mulgara.org"><img src="${freemarker_config.context}/images/home_mulgara.gif" alt="Mulgara.org"/></a>
        <a href="http://www.sciencecommons.org/" title="Science Commons"><img src="${freemarker_config.context}/journals/plosJournals/images/home_sciencecommons.gif"  alt="Science Commons"/></a>
        <a href="http://www.unitedlayer.com/" title="UnitedLayer, LLC"><img src="${freemarker_config.context}/journals/plosJournals/images/home_unitedlayer.gif" alt="UnitedLayer, LLC"/></a>
      </div><!-- end : block partners -->
    </div><!-- end : home -->
  </div><!-- end : wrap -->
</div><!-- end : content -->
