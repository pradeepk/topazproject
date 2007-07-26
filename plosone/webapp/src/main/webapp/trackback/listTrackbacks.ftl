<table>
  <tr>
    <td>Title</td>
		<td>Excerpt</td>
    <td>URL</td>
    <td>Blog Name</td>
<#list trackbackList as t>
  <tr>
    <td>${t.title}</td>
    <td>${t.excerpt}</td>    
    <td>${t.url}</td>    
    <td>${t.blog_name}</td>
	</tr>
</#list>
</table>