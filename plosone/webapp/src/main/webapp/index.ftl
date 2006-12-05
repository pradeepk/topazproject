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
					<div class="col first">
					<div class="block mainnav">
					<ul>
						<li><a href="http://www.plos.org/oa/index.html">Open Access</a></li>
						<li><a href="http://www.plos.org/support/donate.php">Join PLoS</a></li>
					</ul>
					</div>

					
						<div class="block banner">
							<img src="http://www.plosjournals.org/images/banners/v_pod_plo_01.GIF" />
						</div>
						<div class="block partner">

				<h6>Partners</h6>
				<a href="http://fedora.info/" title="Fedora.info"><img src="${freemarker_config.context}/images/pone_home_fedora.jpg" alt="Fedora.info"/></a>
				<a href="http://www.sciencecommons.org/" title="Science Commons" alt="Science Commons"><img src="${freemarker_config.context}/images/pone_home_sciencecommons.jpg"/></a>
				<a href="http://www.osafoundation.org" title="Open Source Applications Foundation" alt="OSAF"><img src="${freemarker_config.context}/images/pone_home_osaf.jpg"/></a>
				<a href="http://www.moore.org" title="Gorden and Betty Moore Foundation" alt="Moore Foundation"><img src="${freemarker_config.context}/images/pone_home_moore.jpg"/></a>
						</div>
													   
					</div>			
										<div class="col last">
							
					<div class="horizontalTabs">
						<ul id="tabsContainer">
						</ul>
						
						<div id="tabPaneSet" class="contentwrap">
						  <#include "article/recentArticles.ftl">
						</div>
					</div>
						
						


<div class="block feature">
<h3>New and Noted</h3>
<div>
<a href="#">Wnt and Hedgehog Are Critical Mediators of Cigarette Smoke-Induced Lung Cancer</a><p>The molecular basis of cigarette-induced lung cancer is poorly understood. This paper shows that genes more normally associated with the patterning of embryos may be involved in opening new avenues for the development of therapies.</p>
</div>
<div>
<a href="#">The Syntax and Meaning of Wild Gibbon Songs</a><p>Human languages are subtle. Identical words can mean different things when spoken in a different order. This paper shows that the same is true for gibbons. When threatened they change the structure of their calls, warning individuals within earshot of the danger.</p>
</div>
<div>
<a href="#">Mesenchymal Stem Cell-Mediated Functional Tooth Regeneration in Swine</a><p>Much discussion surrounds the use of stem cells for regenerating organs as a possible treatment for a diverse spectrum of diseases. This paper explores the potential of human stem cells isolated from dental papillae to regenerate functional teeth.</p>
</div>
</div>

<div class="block feature">

<h3>Also of Note</h3>
<ul class="articles">
	<li><a href="#">Control of Canalization and Evolvability by Hsp90</a></li>
	<li><a href="#">Predator Mimicry: Metalmark Moths Mimic Their Jumping Spider Predators</a></li>
	<li><a href="#">Physiological Mouse Brain A� levels Are Not Related to the Phosphorylation State of Threonine-668 of Alzheimer's APP</a></li>
	<li><a href="#">A Virtual Reprise of the Stanley Milgram Obedience Experiments</a></li>
</ul>


</div>




					</div>

					<!-- end : col 2 -->
				</div>
				<!-- end : wrapper for cols 1 & 2 -->
				<!-- begin : wrapper for cols 3 & 4 -->
				<div id="second" class="col">
				
				
					<!-- begin : col 3 -->
					<div class="subcol first">
					
											<#if categoryNames?size gt 0>
						<div class="subject block">

							<#list categoryNames as category>
							<dl class="category">
							  <#assign categoryId = category?replace("\\s|\'","","r")>
							  <#if categoryId?length lt 8>
								<#assign index = categoryId?length>
							  <#else>
  								<#assign index = 8>
							  </#if>
								<dt><a class="${categoryId?substring(0,index)?lower_case}"  onclick="topaz.domUtil.swapDisplayMode('${categoryId?js_string}');return false;">${category} (${articlesByCategory[category_index]?size})</a></dt>
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
<div class="block info">
<@ww.url action="information.action" namespace="/static" includeParams="none" id="info"/>
<h3>What is PLoS ONE?</h3>
<p>A new inclusive and interactive publication for the efficient dissemination and discussion of open access peer-reviewed research from all areas of science and medicine.<a href="${info}" title="Read More" class="more">Read More</a></p>
</div>
					</div>
					<!-- end : col 3 -->
					<!-- begin : col 4 -->
					<div class="subcol last">
						<div class="block banner">
							<img src="http://a248.e.akamai.net/7/800/1129/1154526507/oascentral-s.realmedia.com/RealMedia/ads/Creatives/sciam.com/m_sciam_2006-02_podcast_sky/pod_120x600_2.gif"/>
						</div>

					</div>
					<!-- end : col 4 -->
				</div>
				<!-- end : wrapper for cols 3 & 4 -->
				
				
				<div id="lower">
				
				<div class="col first">
				<div class="block ad">
				<a href="http://www.plos.org/contact.php?recipient=web"><img src="http://www.plosjournals.org/images/icons/t_hom_mar_04.png" />
				<strong>Feedback</strong>
				<span class="body">Tell us what you think about our new look</span></a>
			</div>

			<div class="block ad">
				<a href="http://www.taxpayeraccess.org/nih.html"><img src="http://www.plosjournals.org/images/icons/t_hom_mar_09.png" />

				<strong>More Reach for Research</strong>
				<span class="body">Support NIH Public Access</span></a>
			</div>
			
			<div class="block ad">
				<a href="http://www.plos.org/advertise"><img src="http://www.plosjournals.org/images/icons/plo_adv_hp.png" />
				<strong>Advertise with PLoS</strong>
				<span class="body">New high-profile realty available</span></a>

			</div>	
	
				</div>
			<div class="col last">
							<h3>What PLoS is Blogging...</h3>
							<ul>
								<li><a href="#">Comment on the PLoS Blog now</a></li>
								<li><a href="#">PLoS ONE: Accepting Submissions</a></li>
								<li><a href="#">Visit PLoS at ASMB</a></li>
								<li><a href="#">Support the Public Research Act</a></li>
								<li><a href="#">Read PLoS Clinical Trials</a></li>
							</ul>
							<h3>What You're Blogging...</h3>
							<ul>
								<li><a href="#">Comment on the PLoS Blog now</a></li>
								<li><a href="#">PLoS ONE: Accepting Submissions</a></li>
								<li><a href="#">Visit PLoS at ASMB</a></li>
								<li><a href="#">Support the Public Research Act</a></li>
								<li><a href="#">Read PLoS Clinical Trials</a></li>
							</ul>
							<h3>New at PLoS</h3>
							<ul>
								<li><a href="#">Comment on the PLoS Blog now</a></li>
								<li><a href="#">PLoS ONE: Accepting Submissions</a></li>
								<li><a href="#">Visit PLoS at ASMB</a></li>
								<li><a href="#">Support the Public Research Act</a></li>
								<li><a href="#">Read PLoS Clinical Trials</a></li>
							</ul>
					</div>
					
				</div>
				
			</div>
			<!-- end : layout wrapper -->
		</div>
	</div>
	<!-- end : home page wrapper -->
</div>
<!-- end : main contents -->
<#include "/widget/loadingCycle.ftl">