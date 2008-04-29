<#--
  $HeadURL::                                                                            $
  $Id$
  
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
    <!-- begin : home -->
    <div id="home">
      <!-- begin : layout wrapper -->
      <div class="col">
        <!-- begin : wrapper for cols 1 & 2 -->
        <div id="first" class="col">
        <!-- removed : col 1 -->
        <!-- begin : col 2 -->
          <div class="col last">
          <!-- begin : horizontalTabs -->
            <div class="horizontalTabs">
              <ul id="tabsContainer">
                <!-- Tabs generated by global file horizontalTabs.js - parameters are set in local file config_home.js -->
              </ul>
							<div id="tabPaneSet" class="contentwrap">
                  <#include "article/recentArticles.ftl">
              </div>
            </div>
            <!-- end : horizontalTabs -->
            <!-- begin : content block -->
            <div class="block">
              <h2>Featured Content</h2>
              <@s.url id="featured1" namespace="/article" action="fetchArticle" articleURI="info:doi/10.1371/journal.pgen.1000020"/>
              <@s.url id="featured2" namespace="/article" action="fetchArticle" articleURI="info:doi/10.1371/journal.pgen.1000002"/>
              <div class="article section">
                <h3><@s.a href="${featured1}" title="Read Open-Access Article">Velit Esse Molestie Consequat, vel Illum Dolore eu Feugiat Nulla Facilisis</@s.a></h3>
                <img src="images/thumbPlaceholder_90x90.jpg" alt="article image" />
                <p>At vero eros et accumsan et iusto odio dignissim qui blandit praesent luptatum zril delenit augue duis dolore te feugait facilisi. Oppeto sino metuo premo regula reprobo utinam.</p>
                <div class="clearer">&nbsp;</div>
              </div>
              <div class="article section lastSection">
                <h3><@s.a href="${featured2}" title="Read Open-Access Article">Consectetuer Adipiscing Elit, sed Diam Nonummy Nibh</@s.a></h3>
                <img src="images/thumbPlaceholder_90x90.jpg" alt="article image" />
                <p>Eusmod tincidunt ut laoreet dolore magna aliquam erat volputate <@s.a href="${featured1}" title="Read Open-Access Article">Ut wisi enim ad minim</@s.a> veniam, quis nostrud exerci tation ullamcorper suscipit lobortis nisl ut aliquip ex ea commodo consequat.</p>
                <div class="clearer">&nbsp;</div>
              </div>
            </div>
            <!-- end : content block -->
            
            <#if categoryInfos?size gt 0>
	    
            <#assign colSize = (categoryInfos?size / 2) + 0.5>
	    
            <!-- begin : explore by subject block -->
            <div class="explore block">
              <h2>Explore by Subject</h2>
              <p>(#) indicates the number of articles published in each subject category.</p>
              <ul>
                <#list categoryInfos?keys as category>
		  <#if (category_index + 1) lte colSize>
		  <#assign categoryId = category?replace("\\s|\'","","r")>
                    <@s.url id="browseURL" action="browse" namespace="/article" catName="${category}" includeParams="none"/>
                    <li>
                      <a id="widget${categoryId}" href="${browseURL}">${category} (${categoryInfos[category]})</a>&nbsp;
                      <a href="${freemarker_config.context}/article/feed?category=${category?replace(' ','+')}"><img src="${freemarker_config.context}/images/feed-icon-inline.gif" /></a>
                    </li>
		  </#if>
                </#list>
              </ul>
              <ul>
                <#list categoryInfos?keys as category>
		  <#if (category_index + 1) gt colSize>
                    <#assign categoryId = category?replace("\\s|\'","","r")>
                    <@s.url id="browseURL" action="browse" namespace="/article" catName="${category}" includeParams="none"/>
                    <li>
                      <a id="widget${categoryId}" href="${browseURL}">${category} (${categoryInfos[category]})</a>&nbsp;
                      <a href="${freemarker_config.context}/article/feed?category=${category?replace(' ','+')}"><img src="${freemarker_config.context}/images/feed-icon-inline.gif" /></a>
                    </li>
		  </#if>
                </#list>
              </ul>
              <div class="clearer">&nbsp;</div>
            </div><!-- end : explore by subject block -->
            </#if>
            
            <!-- begin : content block -->
            <div class="other block">
              <h2>Other Content</h2>
              <div class="section">
                <h3>Browse content from our partners</h3>
                <p><a href="#">Ipsum Lorem</a>; <a href="#">Ipsum Lorem</a>; <a href="#">Ipsum Lorem</a>; <a href="#">Ipsum Lorem</a></p>
              </div>
              <div class="section lastSection">
                <h3>Browse even more content</h3>
                <p><a href="#">Ipsum Lorem</a>; <a href="#">Ipsum Lorem</a>; <a href="#">Ipsum Lorem</a>;</p>
              </div>
            </div>
            <!-- end : content block -->
          </div>
          <!-- end : col last -->
        </div>
        <!-- end : wrapper for cols 1 & 2 -->
        <!-- begin : wrapper for cols 3 & 4 -->
        <div id="second" class="col">
          <!-- begin : col 3 -->
          <div class="subcol first">
          <!-- begin : issue block -->
            <div id="issue" class="block"><h3><a href="#">Current Issue</a></h3><a href="#"><img src="images/issueImage_placeholder_251x251.jpg" alt="issue cover image" /></a></div><!-- keep div#issue hmtl all on one line to avoid extra space below issue image in IE -->
            <!-- end : issue block -->
            <!-- begin : mission block -->
            <div id="mission" class="block">
              <p><strong><em><a href="#">Ambra Journal</a></em></strong> is a paulatim singularis, caecus nutus, mara melior euismod. Scisco lobortis dolore vulputate demoveo pala. Autem nunc suscipere ad in in vereor quis patria.</p>
            </div>
            <!-- end : mission block -->
            <!-- begin : stay-connected block -->
            <div id="connect" class="block">
              <h3>Stay Connected</h3>
              <ul>
                  <li><img src="images/icon_alerts_small.gif" alt="email alerts icon" /><a href="${freemarker_config.registrationURL}"><strong>E-mail Alerts</strong></a><br />Sign up for alerts by e-mail</li>
                  <li><img src="images/icon_rss_small.gif" alt="rss icon" /><@s.url action="rssInfo" namespace="/static" includeParams="none" id="rssinfo"/><a href="${Request[freemarker_config.journalContextAttributeKey].baseUrl}${rssPath}"><strong>RSS</strong></a> (<a href="${rssinfo}">What is RSS?</a>)<br />Subscribe to content feed</li>
                  <li><img src="images/icon_join.gif" alt="Join Us" /><a href="#" title="Join Us: Show Your Support"><strong>Join Us</strong></a><br />Support our organization!</li>
              </ul>
            </div>
            <!-- end : stay-connected block -->
          </div>
          <!-- end : subcol first -->
          <!-- end : col 3 -->
          <!-- begin : col 4 -->
          <div class="subcol last">
            <!-- begin : block banner -->
            <div class="block banner"><!--skyscraper-->
              <a href="#"><img src="images/adBanner_placeholder_120x600.png" alt=""/></a>
            </div>
            <!-- end : block banner -->
          </div>
          <!-- end : subcol last -->
        </div>
        <!-- end : wrapper for cols 3 & 4 -->
        <div id="lower">&nbsp;</div> <!-- displays lower background image -->
      </div>
      <!-- end : col -->
      <!-- begin : partners block -->
      <div class="partner">
        <a href="http://www.fedora-commons.org" title="Fedora-Commons.org"><img src="${freemarker_config.context}/images/home_fedoracommons.png" alt="Fedora-Commons.org"/></a>
        <a href="http://www.mulgara.org/" title="Mulgara.org"><img src="${freemarker_config.context}/images/home_mulgara.gif" alt="Mulgara.org"/></a>
      </div>
      <!-- end : partners block -->
    </div>
    <!-- end : home -->
  </div>
  <!-- end : home page wrapper -->
</div>
<!-- end : main content -->
