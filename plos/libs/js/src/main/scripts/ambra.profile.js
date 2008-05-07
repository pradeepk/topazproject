dependencies = {
  layers: [
    {
      name: "ambra.js",
      layerDependencies: [
      ],
      dependencies: [
        "dijit.layout.ContentPane",
        "dijit.Dialog",
        "dojox.data.dom",
        "topaz.topaz",
        "topaz.domUtil",
        "topaz.htmlUtil",
        "topaz.formUtil",
        "topaz.widget.RegionalDialog",
        "topaz.navigation",
        "topaz.horizontalTabs",
        "topaz.floatMenu",
        "topaz.annotation",
        "topaz.corrections",
        "topaz.displayComment",
        "topaz.responsePanel",
        "topaz.rating",
        "topaz.slideshow"
      ]
    }
  ],

  prefixes: [
    [ "dijit", "../dijit" ],
    [ "dojox", "../dojox" ],
    [ "topaz", "../../topaz" ]
  ]
}
