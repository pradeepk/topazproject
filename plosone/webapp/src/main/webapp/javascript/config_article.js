var djConfig = {
	isDebug: true,
	debugContainerId : "dojoDebug",
	regionalDialogMarker : "rdm",
	articleContainer: "articleContainer"
};

var annotationConfig = {
	trigger: "addAnnotation",
  lastAncestor: "researchArticle",
  xpointerMarker: "xpt",
  annotationMarker: "note",
  annotationImgMarker: "noteImg",
  dialogMarker: "rdm",
  isAuthor: false,  //TODO: *** Default to false when the hidden input is hooked up.
  isPublic: false,
  rangeInfoObj: new Object()
};

var formConfig = {
	commentMaxLen: 250
};

var commentConfig = {
  sectionTitle: "viewCmtTitle",
  sectionDetail: "viewCmtDetail",  
  sectionComment: "viewComment",  
  retrieveMsg: "retrieveMsg"  
};  