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
            <div id="importantStuff" class="block">
              <h2>New and Noted</h2>
              <@s.url id="newNoted1" namespace="/article" action="fetchArticle" articleURI="info:doi/10.1371/journal.pntd.0000096"/>
              <@s.url id="newNoted2" namespace="/article" action="fetchArticle" articleURI="info:doi/10.1371/journal.pntd.0000087"/>
              <@s.url id="newNoted3" namespace="/article" action="fetchArticle" articleURI="info:doi/10.1371/journal.pntd.0000143"/>
              <div class="article section">
                <h3>Recently Published</h3>
                <ul class="articles">
                  <li><a href="#">Autem Sino Nutus, Premo Conventio Virtus Epulae va Consequat at Dolus Appellatio Praesent Uxor Autem Lenis</a></li>
                  <li><a href="#">Capto Zelus Obruo, Sudo Abico Mara Ratis Aliquip Praesent</a></li>
                  <li><a href="${browseDateURL}">Browse all recently published articles</a></li>
                </ul>
                <div class="clearer">&nbsp;</div>
              </div>
              <div class="article section">
                <h3>Community Discussions</h3>
                <ul class="articles">
                  <li><a href="#">Premo Conventio Virtus Epulae va Consequat at Dolus Appellatio Praesent Uxor Autem Lenis</a></li>
                  <li><a href="#">Lorem Ipsum Dolor sit Amet, Consectetuer Adipiscing Elit</a></li>
                </ul>
                <div class="clearer">&nbsp;</div>
              </div>
              <div class="article section lastSection">
                <h3>In the News</h3>
                <p><a href="#">Structural Extremes in a Cretaceous Dinosaur</a></p>
                <p>Fossils of the new dinosaur Nigersaurus taqueti caused a global media sensation and activity.</p>
                <ul class="articles refs">
                  <li>New York Times: <a href="#">A Cowlike Dinosaur Comes Into Focus</a></li>
                  <li>National Geographic: <a href="#">Bizarre Dinosaur Grazed Like a Cow, Study Says</a></li>
                  <li>New Scientist: <a href="#">Odd-jawed dinosaur reveals bovine lifestyle</a></li>
                </ul>
                <div class="clearer">&nbsp;</div>
              </div>
            </div>
            <!-- end : block -->
            <!-- begin : calls to action blocks -->
            <div class="ctaWrap">
              <div id="cta1">
                <strong>Publish with PLoS</strong>
                <a href="#">We want to publish your work</a>
              </div>
              <div id="cta2">
                <strong>Have Your Say</strong>
                <a href="${comment}">Add ratings and discussions</a>
              </div>
              <div class="clearer">&nbsp;</div>
            </div>
            <!-- end : calls to action blocks -->
            
            <#if categoryInfos?size gt 0>
            <!-- begin : explore by subject block -->
            <div class="explore block">
              <h2>Explore by Subject</h2>
              <p>(#) indicates the number of articles published in each subject category.</p>
              <ul>
                <#list categoryInfos?keys as category>
                  <#assign categoryId = category?replace("\\s|\'","","r")>
                  <@s.url id="browseURL" action="browse" namespace="/article" catName="${category}" includeParams="none"/>
                  <li>
                    <a id="widget${categoryId}" href="${browseURL}">${category} (${categoryInfos[category]})</a>&nbsp;
                    <a href="${freemarker_config.context}/rss/${category?replace(' ','')?replace("'",'')}.xml"><img src="${freemarker_config.context}/images/feed-icon-inline.gif" /></a>
                  </li>
                </#list>
              </ul>
              <ul>
                <#list categoryInfos?keys as category>
                  <#assign categoryId = category?replace("\\s|\'","","r")>
                  <@s.url id="browseURL" action="browse" namespace="/article" catName="${category}" includeParams="none"/>
                  <li>
                    <a id="widget${categoryId}" href="${browseURL}">${category} (${categoryInfos[category]})</a>&nbsp;
                    <a href="${freemarker_config.context}/rss/${category?replace(' ','')?replace("'",'')}.xml"><img src="${freemarker_config.context}/images/feed-icon-inline.gif" /></a>
                  </li>
                </#list>
              </ul>
              <div class="clearer">&nbsp;</div>
            </div><!-- end : explore by subject block -->
            </#if>
            
            <div class="other block">
              <h2>Other PLoS Content</h2>
              <div class="section">
                <h3><a href="http://www.plospathogens.org/"><em>PLoS Pathogens</em></a></h3>
                <ul class="articles">
                  <li><a href="http://pathogens.plosjournals.org/perlserv/?request=get-document&doi=10.1371%2Fjournal.ppat.0030185" title="Read Open Access Article">Bradykinin B2 Receptors of Dendritic Cells, Acting as Sensors of Kinins Proteolytically Released by <em>Trypanosoma cruzi</em>, Are Critical for the Development of Protective Type-1 Responses</a></li>
                  <li><a href="http://pathogens.plosjournals.org/perlserv/?request=get-document&doi=10.1371%2Fjournal.ppat.0030183" title="Read Open Access Article">Secreted NS1 of Dengue Virus Attaches to the Surface of Cells via Interactions with Heparan Sulfate and Chondroitin Sulfate E </a></li>
                </ul>
              </div>
              <div class="section">
                <h3><a href="http://www.plosone.org/"><em>PLoS ONE</em></a></h3>
                <ul class="articles">
                  <li><a href="http://www.plosone.org/article/info%3Adoi%2F10.1371%2Fjournal.pone.0001189" title="Read Open Access Article">Mining Predicted Essential Genes of <em>Brugia malayi</em> for Nematode Drug Targets </a></li>
                </ul>
               </div>
               <div class="section lastSection">
                 <h3><a href="http://www.plosmedicine.org/"><em>PLoS Medicine</em></a></h3>
                 <ul class="articles">
                    <li><a href="http://medicine.plosjournals.org/perlserv/?request=get-document&doi=10.1371%2Fjournal.pmed.0040334" title="Read Open Access Article">Cytomegalovirus Retinitis: The Neglected Disease of the AIDS Pandemic</a></li>
                </ul>
              </div>
            </div><!-- end : other block -->
          </div><!-- end : col last -->
        </div><!-- end : wrapper for cols 1 & 2 -->
        <!-- begin : wrapper for cols 3 & 4 -->
        <div id="second" class="col">
          <!-- begin : col 3 -->
          <div class="subcol first">
          <!-- begin : about block -->
          <div class="block">
            <h3>What is PLoS ONE?</h3>
            <p>An interactive open-access journal for the communication of all peer-reviewed scientific and medical research. <a href="${info}">More</a></p>
          </div>
          <!-- end : about block -->
          <!-- begin : block -->
          <div class="block">
            <h3>PLoS ONE is 1 Year Old</h3>
            <p>We’re marking the occasion with this home page makeover that will help you find relevant content more quickly and encourage dialogue on articles. <a href="#">More</a></li></p>
          </div>
          <!-- end : block -->
          <!-- begin : block -->
          <div class="block">
            <h3>Author Survey Results</h3>
            <p>Thanks to the more than 2,000 of our authors who responded to our survey.</p>
            <ul>
              <li>97.2% % say they will publish with PLoS ONE again</li>
            </ul>
            <p>iPod shuffle winners: Marion Coolen, Thomas J. Baiga, Benedicte Lafay - congratulations.</p>
          </div>
          <!-- end : block -->
          <!-- begin : journal club block -->
          <div class="block">
            <h3>Journal Club</h3>
            <p>New one coming soon, in the meantime, <a href="${journalClub}">visit our archive</a>.</p>
            <p>Want to get involved? <a href="${feedbackURL}">Nominate your lab</a> and get your team and your work some free publicity.</p>
          </div>
          <!-- end : journal club block -->
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
          </div>
          <!-- end : advocacy blocks -->
            <!-- begin : stay-connected block -->
            <div id="connect" class="block">
              <h3>Stay Connected</h3>
              <ul>
                  <li><img src="images/icon_alerts_small.gif" alt="email alerts icon" /><a href="http://www.plosone.org/user/secure/editPrefsAlerts.action?tabId=alerts"><strong>E-mail Alerts</strong></a><br />Sign up for alerts by e-mail</li>
                  <li><img src="images/icon_rss_small.gif" alt="rss icon" /><@s.url action="rssInfo" namespace="/static" includeParams="none" id="rssinfo"/><a href="${Request[freemarker_config.journalContextAttributeKey].baseUrl}${rssPath}"><strong>RSS</strong></a> (<a href="${rssinfo}">What is RSS?</a>)<br />Subscribe to content feed</li>
                  <li><img src="images/icon_join.gif" alt="join PLoS icon" /><a href="http://www.plos.org/support/donate.php" title="Join PLoS: Show Your Support"><strong>Join PLoS</strong></a><br />Support the open-access movement!</li>
              </ul>
            </div>
            <!-- end : stay-connected block -->
            <!-- begin : blog block -->
            <div id="blog" class="block">
              <h3>From the PLoS Blog</h3>
              <p>Read the <a href="http://www.plos.org/cms/blog" title="PLoS Blog">PLoS Blog</a> <a href="http://feeds.feedburner.com/plos/Blog"><img alt="RSS" src="/plosone-webapp/images/feed-icon-inline.gif" /></a> and contribute your views on scientific research and open-access publishing.</p>
              <ul class="articles">
                <li><a href="http://www.plos.org/cms/node/297">Children's medicines matter</a></li>
                <li><a href="http://www.plos.org/cms/node/296">Accept no imitations, unless you're learning to speak</a></li>
                <li><a href="http://www.plos.org/cms/node/295">Zotero Translator for PLoS Articles</a></li>
              </ul>
            </div>
            <!-- end : blog block -->
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
        <a href="http://www.fedora-commons.org" title="Fedora-Commons.org"><img src="${freemarker_config.context}/images/pone_home_fedoracommons.png" alt="Fedora-Commons.org"/></a>
				<a href="http://www.moore.org" title="Gorden and Betty Moore Foundation"><img src="${freemarker_config.context}/images/pone_home_moore.gif" alt="Moore Foundation"/></a>
        <a href="http://www.mulgara.org/" title="Mulgara.org"><img src="${freemarker_config.context}/images/pone_home_mulgara.gif" alt="Mulgara.org"/></a>
        <a href="http://www.sciencecommons.org/" title="Science Commons"><img src="${freemarker_config.context}/images/pone_home_sciencecommons.gif"  alt="Science Commons"/></a>
				<a href="http://www.unitedlayer.com/" title="UnitedLayer, LLC"><img src="${freemarker_config.context}/images/pone_home_unitedlayer.gif" alt="UnitedLayer, LLC"/></a>
      </div><!-- end : block partners -->
    </div><!-- end : home -->
  </div><!-- end : wrap -->
</div><!-- end : content -->
