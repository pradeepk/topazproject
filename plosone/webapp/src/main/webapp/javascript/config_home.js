var tabsListMap = new Array();

tabsListMap[tabsListMap.length] = {tabKey:   "recentlyPublished",
                                   title:    "Recently Published",
                                   urlLoad:  "/article/recentArticles.action",
                                   urlSave:  ""};

tabsListMap[tabsListMap.length] = {tabKey:   "mostCommented",
                                   title:    "Most Commented",
                                   urlLoad:  "/article/mostCommented.action",
                                   urlSave:  ""};

var querystring = topaz.htmlUtil.getQuerystring();
var tabSelectId = "";

for (var i=0; i<querystring.length; i++) {
  if (querystring[i].param == "tabId") {
    tabSelectId = querystring[i].value;
  }
}

var homeConfig = {
    tabPaneSetId: "tabPaneSet",
    tabsContainer: "tabsContainer",
    tabSelectId: tabSelectId
  }                                 