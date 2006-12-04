<!-- begin : main contents wrapper -->
<div class="content">

<#if Parameters.title?exists>
	<#assign title = Parameters.title>
<#else>
	<#assign title = "">
</#if>

<#if Parameters.author?exists>
	<#assign author = Parameters.author>
<#else>
	<#assign author= "">
</#if>

		<h2>Find this article online</h2>
		<p>${title}</p>
		<p><img src="${freemarker_config.context}/images/pone_icon_pubmed_link.gif" border="0" alt="Go to article in PubMed/NCBI" width="23">
		This article may exist at <a href="http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?db=PubMed&cmd=Search&doptcmdl=Citation&defaultField=Title+Word&term=${author}%5Bauthor%5D+AND+${title?html}"
		 onclick="window.open(this.href, 'PLoSFindArticle','');return false;">PubMed/NCBI</a>.</p>
		<p><img src="${freemarker_config.context}/images/pone_icon_google_link.gif" border="0" alt="Go to article in Google Scholar">
		This article may exist at <a href="http://scholar.google.com/scholar?hl=en&safe=off&q=author%3A${author}+%22${title}%22"
				 onclick="window.open(this.href, 'PLoSFindArticle','');return false;">Google Scholar</a>.	</p>	

		<p><a href="#" onClick="history.back();return false;">Go back to article</a></p>
</div>