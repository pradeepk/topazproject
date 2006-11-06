<script type="text/javascript">
  var namespace="${freemarker_config.getContext()}";
</script>
<script type="text/javascript" src="${freemarker_config.getContext()}/javascript/prototype.js"></script>
<script type="text/javascript" src="${freemarker_config.getContext()}/javascript/config_article.js"></script>
<script type="text/javascript" src="${freemarker_config.getContext()}/javascript/dojo/dojo.js"></script>
<script type="text/javascript" src="${freemarker_config.getContext()}/javascript/topaz/topaz.js"></script>
<script type="text/javascript" src="${freemarker_config.getContext()}/javascript/topaz/annotation.js"></script>
<script type="text/javascript" src="${freemarker_config.getContext()}/javascript/topaz/formUtil.js"></script>
<script type="text/javascript">
	dojo.registerModulePath("topaz", "../topaz");
	dojo.require("topaz.topaz");
	dojo.require("dojo.widget.Dialog");
//	dojo.registerModulePath("dojo.dojo_custom", "dojo_custom.js");
	dojo.require("dojo.widget.RegionalDialog");
  dojo.require("dojo.io.*");
  dojo.require("dojo.event.*");
  dojo.require("dojo.json");

</script>
<script type="text/javascript" src="${freemarker_config.getContext()}/javascript/formSetup_article.js"></script>
<script type="text/javascript" src="${freemarker_config.getContext()}/javascript/displayComment.js"></script>
<script type="text/javascript" src="${freemarker_config.getContext()}/javascript/init_article.js"></script>

<!--
<#list freemarker_config.getJavaScript(templateFile) as x>
<script type="text/javascript" src="${x}"></script>
</#list> -->