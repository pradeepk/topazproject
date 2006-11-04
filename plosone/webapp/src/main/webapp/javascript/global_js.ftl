<script type="text/javascript">
  var namespace="${freemarker_config.getContext()}";
</script>


<#list freemarker_config.getJavaScript(templateFile) as x>
<script type="text/javascript" src="${x}"></script>
</#list> 