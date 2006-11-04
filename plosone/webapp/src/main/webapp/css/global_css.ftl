<#list freemarker_config.getCss(templateFile) as x>
<style type="text/css" media="all"> @import "${x}";</style>
</#list> 
