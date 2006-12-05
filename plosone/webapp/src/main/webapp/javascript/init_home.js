var ldc;
var preferenceForm;

function init(e) {
  ldc = dojo.widget.byId("LoadingCycle");
  
  ldc.show();
  
  topaz.horizontalTabs.setTabPaneSet(dojo.byId(homeConfig.tabPaneSetId));
  topaz.horizontalTabs.setTabsListObject(tabsListMap);
  topaz.horizontalTabs.setTabsContainer(dojo.byId(homeConfig.tabsContainer));
  topaz.horizontalTabs.initSimple(tabSelectId);
  
  ldc.hide();
}

dojo.addOnLoad(init);
