						<#assign commentedArticles = action.getCommentedOnArticles(5)>
						<#if (((commentedArticles?size)!0) gt 0) >
							<ul class="articles">
								<#list commentedArticles as commented>
								<li>
									<a href="article/fetchArticle.action?articleURI=" title="Read Open Access Article">${commented.title}</a>
								</li>
								</#list>
							</ul>
						</#if>