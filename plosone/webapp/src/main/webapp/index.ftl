<!-- begin : main content -->
<div id="content">
	<!-- begin : home page wrapper -->
	<div id="wrap">
		<div id="home">
			<!-- begin : layout wrapper -->
			<div class="col">
				<!-- begin : wrapper for cols 1 & 2 -->
				<div id="first" class="col">
					<!-- begin : col 1 -->

							<h1 style="font-size: 2.3em;">Welcome to PLoS ONE</h1>
							<p>Today is the day that we unveil Open Access 2.0. Blah blah lorem orem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam erat volutpat.</p>

						<#assign numArticles = recentArticles?size>
						<#if (numArticles > 0)>
							<#assign randomIndices = action.randomNumbers(5, numArticles)>
						<div class="block">
							<h1>Recently Published</h1>
							<ul class="articles">
							  <#list randomIndices as random>
									<#assign article = recentArticles[random]>
									<#if random_index % 2 == 0>
								<li class="even">
									<#else>
								<li>
									</#if>
									<a href="article/fetchArticle.action?articleURI=${article.uri?url}" title="Read Open Access Article" class="article icon">${article.title}</a>
								</li>
								</#list>
							</ul>
						</div>
						</#if>
	
						
						<#assign commentedArticles = action.getCommentedOnArticles(6)>
						<#if commentedArticles?size gt 0 >
						<div class="block">
							<h1>Most Commented On</h1>
							<ul class="articles">
								<#list commentedArticles as commented>
								<li>
									<a href="article/fetchArticle.action?articleURI=" title="Read Open Access Article" class="article icon">${commented.title}</a>
								</li>
								</#list>
							</ul>
						</div>
						</#if>
					<div class="col first">
						<div class="block banner">
							<img src="http://www.plosjournals.org/images/banners/v_pod_plo_01.GIF">
						</div>				   
					</div>
					<div class="col last">		
						<#if categoryNames?size gt 0>
						<div class="block">
							<h1>Subject Categories</h1>
							<#list categoryNames as category>
							<dl class="category">
							  <#assign categoryId = category?replace(" ","")>
								<dt><a href="#" onclick="topaz.domUtil.swapDisplayMode('${categoryId?js_string}');return false;">${category} (${articlesByCategory[category_index]?size})</a></dt>
								<dd id="${categoryId}">
									<ul>
										<#list articlesByCategory[category_index] as article>
										<li><a href="article/fetchArticle.action?articleURI=${article.uri?url}" title="Read Open Access Article">${article.title}</a></li>
										</#list>
									</ul>
								</dd>
							</dl>
							</#list>
						</div>
						</#if> 
					</div>

					<!-- end : col 2 -->
				</div>
				<!-- end : wrapper for cols 1 & 2 -->
				<!-- begin : wrapper for cols 3 & 4 -->
				<div id="second" class="col">
					<!-- begin : col 3 -->
					<div class="subcol first">
						<div class="block ad">
							<a href="http://www.plos.org/cms/node/40">
								<img src="http://www.plosjournals.org/images/icons/pbio_reuse_hp.png"/>
								<strong>Reuse</strong>
								<span class="body">Feel free to be creative with our content.</span>
							</a>
						</div>
						<div class="block ad">
							<a href="http://www.plos.org/journals/license.html">
								<img src="http://www.plosjournals.org/images/icons/pbio_permission_hp.png"/>
								<strong>Permission not required</strong>
								<span class="body">Our content can be used any way you wish.</span>
							</a>
						</div>
						<div class="block marketing">
							<h2>New at PLoS</h2>
							<ul>
								<li>
									<a href="#">Comment on the PLoS Blog now</a>
								</li>
								<li>
									<a href="#">PLoS ONE: Accepting Submissions</a>
								</li>
								<li>
									<a href="#">Visit PLoS at ASMB</a>
								</li>
								<li>
									<a href="#">Support the Public Research Act</a>
								</li>
								<li>
									<a href="#">Read PLoS Clinical Trials</a>
								</li>
							</ul>
						</div>
						<div class="block">
							<h2>From the Blogosphere</h2>
							<a href="http://www.earlham.edu/~peters/fos/fosblog.html" title="Peter Suber's Blog">
								<img src="http://www.plosjournals.org/images/icons/pbio_suber_hp.jpg" width="45" height="45" alt="Peter Suber"/>
							</a>
							<p>
								<a href="http://www.earlham.edu/~peters/fos/fosblog.html" title="Peter Suber's Blog">Read Peter Suber's blog</a> for the latest news from the open access movement.</p>
						</div>
						<div class="block journals">
							<h2>In the Journals</h2>
							<a href="http://www.plosbiology.org">
								<img src="http://www.plos.org/images/pbio_234x60.png" alt="PLoS Biology - www.plosbiology.org" border="0" height="60" width="234"/>
							</a>
							<p>
								<a href="http://biology.plosjournals.org/perlserv/?request=get-document&amp;doi=10.1371/journal.pbio.0040401">ONE for All: The Next Step for PLoS</a>
							</p>
							<p>PLoS ONE will initiate a radical departure from existing scientific publishing platforms by being more inclusive and by taking advantage of the increasing functionality of internet-based communication.</p>
						</div>
					</div>
					<!-- end : col 3 -->
					<!-- begin : col 4 -->
					<div class="subcol last">
						<div class="block banner">
							<img src="http://a248.e.akamai.net/7/800/1129/1154526507/oascentral-s.realmedia.com/RealMedia/ads/Creatives/sciam.com/m_sciam_2006-02_podcast_sky/pod_120x600_2.gif"/>
						</div>
						<h6>Partners</h6>
						<div class="block banner partner">
							<a href="http://fedora.info/" title="Fedora.info">
								<img src="${freemarker_config.context}/images/pone_home_fedora.jpg"/>
							</a>
						</div>
						<div class="block banner partner">
							<a href="http://www.sciencecommons.org/" title="Science Commons">
								<img src="${freemarker_config.context}/images/pone_home_sciencecommons.jpg"/>
							</a>
						</div>
						<div class="block banner partner">
							<a href="http://www.osafoundation.org" title="Open Source Applications Foundation">
								<img src="${freemarker_config.context}/images/pone_home_osaf.jpg"/>
							</a>
						</div>
						<div class="block banner partner">
							<a href="http://www.moore.org" title="Gorden and Betty Moore Foundation">
								<img src="${freemarker_config.context}/images/pone_home_moore.jpg"/>
							</a>
						</div>
						<div class="block banner partner">
							<a href="http://www.unitedlayer.com/" title="United Layer Built on IP Services">
								<img src="${freemarker_config.context}/images/pone_home_unitedlayer.jpg"/>
							</a>
						</div>

					</div>
					<!-- end : col 4 -->
				</div>
				<!-- end : wrapper for cols 3 & 4 -->
			</div>
			<!-- end : layout wrapper -->
		</div>
	</div>
	<!-- end : home page wrapper -->
</div>
<!-- end : main contents -->
