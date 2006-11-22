dojo.provide("topaz.annotation");

/**
  * topaz.annotation
  *
  * @param
  *
  **/
topaz.annotation = new Object();

topaz.annotation = {
/*
  initialize: function() {
  	if ( document.addEventListener )
    {
  		document.addEventListener( 'keyup', this._createAnnotationOnkeyup.bindAsEventListener(this), false );
    }
  	else if ( document.attachEvent ) 
    {
      document.attachEvent('onkeyup', this._createAnnotationOnkeyup.bindAsEventListener(this));
    }  
  	else  // for IE:
  	{
  		if ( document.onkeyup )
  			document.onkeyup = function( event ) { this._createAnnotationOnkeyup(event).bindAsEventListener(this); document.onkeyup; }
  		else
  			document.onkeyup = this._createAnnotationOnkeyup(event).bindAsEventListener(this);
  	}
  	
  },*/
/*  
  _LAST_ANCESTOR: "researchArticle",
  _XPOINTER_MARKER: "xpt",
  _ANNOTATION_MARKER: "note",
  _ANNOTATION_IMG_MARKER: "noteImg",
  _DIALOG_MARKER: "rdm",
  _IS_AUTHOR: false,  //TODO: *** Default to false when the hidden input is hooked up.
  _IS_PUBLIC: false,
  rangeInfoObj: new Object(),
*/  
  _createAnnotationOnkeyup: function (event) {
  	if ( keyname(event) == ENTER ) {
      var captureText = this.createNewAnnotation();
  		
      if ( captureText ) {
      	if (!event) var event = window.event;
      	event.cancelBubble = true;
      	if (event.stopPropagation) event.stopPropagation();
      }
    }
    return true;
  },
  
  createAnnotationOnMouseDown: function (event) {
	  topaz.formUtil.textCues.reset(commentTitle, titleCue); 
	  topaz.formUtil.textCues.reset(comments, commentCue); 
	  annotationForm.commentTitle.value = "";
	  annotationForm.comment.value = "";
	  
    var captureText = this.createNewAnnotation();
		
    if ( captureText ) {
    	if (!event) var event = window.event;
    	event.cancelBubble = true;
    	if (event.stopPropagation) event.stopPropagation();
    }

    //dojo.event.browser.preventDefault();

    return false;
  },
  
  createNewAnnotation: function () {
    annotationConfig.rangeInfoObj = this.getRangeOfSelection();
    
    /*alert("annotationConfig.rangeInfoObj.range.text = "     + annotationConfig.rangeInfoObj.range.text + "\n" +
          "annotationConfig.rangeInfoObj.startPoint = "     + annotationConfig.rangeInfoObj.startPoint + "\n" +
          "annotationConfig.rangeInfoObj.endPoint = "       + annotationConfig.rangeInfoObj.endPoint + "\n" +
          "annotationConfig.rangeInfoObj.startParent = "    + annotationConfig.rangeInfoObj.startParent + "\n" +
          "annotationConfig.rangeInfoObj.endParent = "      + annotationConfig.rangeInfoObj.endParent + "\n" +
          "annotationConfig.rangeInfoObj.startParentId = "  + annotationConfig.rangeInfoObj.startParentId + "\n" +
          "annotationConfig.rangeInfoObj.endParentId = "    + annotationConfig.rangeInfoObj.endParentId + "\n" +
          "annotationConfig.rangeInfoObj.startXpath = "     + annotationConfig.rangeInfoObj.startXpath + "\n" +
          "annotationConfig.rangeInfoObj.endXpath = "       + annotationConfig.rangeInfoObj.endXpath + "\n" +
          "annotationConfig.rangeInfoObj.pageOffsetX = "    + annotationConfig.rangeInfoObj.pageOffsetX + "\n" +
          "annotationConfig.rangeInfoObj.pageOffsetY = "    + annotationConfig.rangeInfoObj.pageOffsetY);*/
//          "annotationConfig.rangeInfoObj.startAncestors = " + annotationConfig.rangeInfoObj.startAncestors + "\n" +
//          "annotationConfig.rangeInfoObj.endAncestors = "   + annotationConfig.rangeInfoObj.endAncestors);
                      
    if (annotationConfig.rangeInfoObj == null) {
      alert("This area of text is not annotatable.");
    }
    else {      
     annotationForm.startPath.value = annotationConfig.rangeInfoObj.startXpath;
     annotationForm.startOffset.value = annotationConfig.rangeInfoObj.startPoint;
     annotationForm.endPath.value = annotationConfig.rangeInfoObj.endXpath;
     annotationForm.endOffset.value = annotationConfig.rangeInfoObj.endPoint;
     
     this.analyzeRange(annotationConfig.rangeInfoObj, 'span');
    }

  },
  
  getHTMLOfSelection: function () {
    var range;
    if (document.selection && document.selection.createRange) {
      range = document.selection.createRange();
      return range.htmlText;
    }
    else if (window.getSelection) {
      var selection = window.getSelection();
      if (selection.rangeCount > 0) {
        range = selection.getRangeAt(0);
        var clonedSelection = range.cloneContents();
        var div = document.createElement('div');
        div.appendChild(clonedSelection);
        return div.innerHTML;
      }
      else {
        return '';
      }
    }
    else {
      return '';
    }
  },
  
  getRangeOfSelection: function () {
    var rangeInfo = new Object();

    if (window.getSelection || document.getSelection) {
      rangeInfo = this.findMozillaRange();
    
      return rangeInfo;
    }
    else if (document.selection && document.selection.createRange) {
      rangeInfo = this.findIeRange();
    
      return rangeInfo;
    }
    else {
      return false;
    }
  },

  analyzeRange: function (rangeInfo, element) {
    var startParent = rangeInfo.startParent;
    var endParent   = rangeInfo.endParent;
    var childList   = new Array();
    var html        = this.getHTMLOfSelection();
    
    //alert("rangeInfo.range.htmlText = " + rangeInfo.range.htmlText + "\n" + 
    //      "startParent.id = " + startParent.id + "\n" +
    //      "endParent.id = " + endParent.id);
    
    if (startParent.xpathLocation == endParent.xpathLocation) {
      if (startParent.hasChildNodes) 
        childList = this.getChildList(startParent, element);
        
      if (childList.length > 0) {
        this.promoteChild(startParent, 'span', annotationConfig.annotationMarker);
        //this.promoteChild(startParent, 'img', annotationConfig.annotationImgMarker);
      }
      
      this.insertHighlightSpan(rangeInfo);
    }
    else {
      var parentArray = this.findXptElementsInRange(startPoint.element, endPoint.element);
      alert("parentArray = " + parentArray);
    }
    
  	var marker = dojo.byId(djConfig.regionalDialogMarker);
  	dlg.setMarker(marker);
    dlg.show();
  },

  findIeRange: function() {
    var range      = document.selection.createRange();
    var startRange = range.duplicate();
    var endRange   = startRange.duplicate();
    
    startRange.collapse(true);
    endRange.collapse(false);

    var startPoint     = this.getRangePoint(startRange);
    var endPoint       = this.getRangePoint(endRange);

    //var parentArray; // = this.getParentIdArray(startRange, startPoint.element, endRange, endPoint.element);
    
    if ( startPoint.element == null || endPoint.element == null ) {
      return null;
    }
    else {
	    var startParent    = startPoint.element;
	    var endParent      = endPoint.element;
	    var startXpath     = startPoint.xpathLocation;
	    var endXpath       = endPoint.xpathLocation;
	    var startParentId  = startPoint.element.id;
	    var endParentId    = endPoint.element.id;
	    //var startAncestors = this.getAncestorsForXml(startPoint.element, annotationConfig.lastAncestor);
	    //var endAncestors   = this.getAncestorsForXml(endPoint.element, annotationConfig.lastAncestor);
	    
      var ieRange = new Object();
      ieRange  = {range:          range,
                  startPoint:     startPoint.offset,
                  endPoint:       endPoint.offset,
                  startParent:    startParent,
                  endParent:      endParent,
                  startXpath:     startXpath,
                  endXpath:       endXpath,
                  startParentId:  startParentId,
                  endParentId:    endParentId,
                  //startAncestors: startAncestors,
                  //endAncestors:   endAncestors,
                  pageOffsetX:	  startPoint.pageOffsetX,
                  pageOffsetY:	  startPoint.pageOffsetY};
      
      return ieRange;
    }
  },

  findMozillaRange: function() {
    var rangeSelection = window.getSelection ? window.getSelection() : 
                         document.getSelection ? document.getSelection() : 0;
    var startRange     = rangeSelection.getRangeAt(0);
    
    var endRange   = startRange.cloneRange();
    var range      = startRange.cloneRange();
    
    startRange.collapse(true);
    endRange.collapse(false);

    var tempNode = document.createElement("span");
    endRange.insertNode(tempNode);
    
    var endPoint       = this.getRangePoint(endRange);
    var startPoint     = this.getRangePoint(startRange);
    
    range.setEndAfter(tempNode);
    tempNode.parentNode.removeChild(tempNode);

    if ( startPoint.element == null || endPoint.element == null ) {
      return null;
    }
    else {
	    var startParent    = startPoint.element;
	    var endParent      = endPoint.element;
	    var startXpath     = startPoint.xpathLocation;
	    var endXpath       = endPoint.xpathLocation;
	    var startParentId  = startPoint.element.id;
	    var endParentId    = endPoint.element.id;
	    //var startAncestors = this.getAncestorsForXml(startPoint.element, annotationConfig.lastAncestor);
	    //var endAncestors   = this.getAncestorsForXml(endPoint.element, annotationConfig.lastAncestor);
 	    
      var mozRange = new Object();
      mozRange = {range:          range,
                  startPoint:     startPoint.offset,
                  endPoint:       endPoint.offset,
                  startParent:    startParent,
                  endParent:      endParent,
                  startXpath:     startXpath,
                  endXpath:       endXpath,
                  startParentId:  startParentId,
                  endParentId:    endParentId,
                  //startAncestors: startAncestors,
                  //endAncestors:   endAncestors,
                  pageOffsetX:	  startPoint.pageOffsetX,
                  pageOffsetY:	  startPoint.pageOffsetY};    
                        
      return mozRange;
    }
  },
  
  getRangePoint: function (range) {
    var POINT_SPAN_ID = "POINT_SPAN";
    
    if (range.pasteHTML) {
      range.pasteHTML("<span id=\"" + POINT_SPAN_ID + "\"></span>");
    }
    else {
      var ptSpan = document.createElement("span");
      ptSpan.id = POINT_SPAN_ID;
      range.insertNode(ptSpan);
    }

    var pointSpan = document.getElementById(POINT_SPAN_ID);

    positionedOffset = Position.positionedOffset(pointSpan);
    
    var pointEl = this.getFirstAncestorByAttribute(pointSpan, "xpathLocation");
    
    var point = new Object();
    point.element = pointEl.element;
    point.xpathLocation = pointEl.xpathLocation;
    point.offset = this.getPointOffset(pointSpan);
    point.pageOffsetX = positionedOffset[0];
    point.pageOffsetY= positionedOffset[1];

    pointSpan.parentNode.removeChild(pointSpan);

    return point;
  },
  
  getPointOffset: function (obj) {
    var offset = 0;
    for (var currentNode = obj.previousSibling; currentNode != null; currentNode = currentNode.previousSibling) {
      if (currentNode.nodeType == 1) { // element
        var normalText = this.normalizeText( currentNode, "");
        offset += normalText.length;
      }
      else if (currentNode.nodeType == 3) { // node
        offset += currentNode.length;
      }
      else { // other
        // don't change the pointIndex
      }
    }
    
    return offset;    
  },

  getAncestorsForXml: function ( selfNode, lastAncestorId ) {
    var familyTree = selfNode.id;
    var parentalNode = selfNode;
    
    while ( parentalNode.id != lastAncestorId ) {
      parentalNode = parentalNode.parentNode;
      familyTree = parentalNode.id + "/" + familyTree;
    }
    
    return familyTree;
  },
  
  getFirstAncestorByAttribute: function ( selfNode, targetAttribute ) {
    var parentalNode = selfNode;
    
    while ( parentalNode.parentNode.getAttributeNode('xpathLocation') == null || parentalNode.parentNode.getAttributeNode('xpathLocation').nodeValue  == "" ) {
      parentalNode = parentalNode.parentNode;
    }
    
    var parentObj = new Object();
    parentObj.element = ( parentalNode.parentNode.getAttributeNode('xpathLocation').nodeValue  == "noSelect" ) ? null : parentalNode.parentNode;
    parentObj.xpathLocation = parentalNode.parentNode.getAttributeNode('xpathLocation').nodeValue;
    return parentObj;
  },
  
  getChildList: function (obj, element, elName) {
    var childSearch = obj.getElementsByTagName(element);
    
    var childList = new Array();
    for (var i=0; i<childSearch.length; i++) {
      var tmpText = this.normalizeText(childSearch[i], '');
      var startOffset = this.getPointOffset(childSearch[i]);
      var endOffset = tmpText.length;
      
      var listIndex = childList.length;
      //childList[i] = new Object();
      childList[childList.length] = {startOffset: startOffset,
                                     endOffset:   endOffset};
      //alert("tmpText = " + tmpText + "\n" +
      //      "childList[" + listIndex + "] = startOffset: " + childList[listIndex].startOffset + ", endOffset: " + childList[listIndex].endOffset);
    }
    
    
    return childList;
    
  },
  
  promoteChild: function (obj, element, elName) {
    var childSearch = obj.getElementsByTagName(element);
    
    //alert("childSearch.length = " + childSearch.length);
    
    for (var i=0; i<childSearch.length; i++) {
    
      if (childSearch[i].className.match(elName)) {
        if (childSearch[i].removeNode) 
          childSearch[i].removeNode(false);
        else {
          var docFragment = document.createDocumentFragment();
          for (var n=0; n<childSearch[i].childNodes.length; n++) {
            docFragment.appendChild(childSearch[i].childNodes[n].cloneNode(true));
          }
          
          obj.insertBefore(docFragment, childSearch[i]);
          
          obj.removeChild(childSearch[i]);
        }
      }
    }
  },
  
  replaceChild: function (obj, element, elName) {
    var childSearch = obj.getElementsByTagName(element);
    
    for (var i=0; i<childSearch.length; i++) {
    
      if (childSearch[i].className.match(elName)) {
        if (childSearch[i].removeNode) 
          childSearch[i].removeNode(false);
        else {
          var docFragment = document.createDocumentFragment();
          for (var n=0; n<childSearch[i].childNodes.length; n++) {
            docFragment.appendChild(childSearch[i].childNodes[n].cloneNode(true));
          }
          
          obj.insertBefore(docFragment, childSearch[i]);
          
          obj.removeChild(childSearch[i]);
        }
      }
    }
  },
  
  
  /************************************************************************
   *  topaz.intersectRange
   *
   *  @param: r1_start    start point of range1
   *          r1_end      end point of range1
   *          r2_start    start point of range2
   *          r2_end      end point of range2
   *  
   *  @return: -1   out of range             <r1>  </r1>  <r2>  </r2>
   *            0   ranges are the same      <r1, r2>     </r1, r2>
   *            1   range2 overlaps range1   <r1>  <r2>  </r1>  </r2>
   *            2   range2 is within range1  <r1>  <r2>  </r2>  </r1>
   *            3   range1 overlaps range2   <r2>  <r1>  </r2>  </r1>
   *            4   range1 is within range2  <r2>  <r1>  </r1>  </r2>
   *            5   range2 overlaps range1 with the same start point    <r1, r2>  </r1>  </r2>
   *            6   range2 overlaps range1 with the same end point      <r1>  <r2>     </r1, r2>
   *            7   range1 overlaps range2 with the same start point    <r1, r2>  </r2>  </r1>
   *            8   range1 overlaps range2 with the same end point      <r2>  <r1>     </r1, r2>
   *
   ************************************************************************/
  intersectRange: function (r1_start, r1_end, r2_start, r2_end) {
    if (r1_start == r2_start && r1_start == r2_end) {
      return 0;
    }
    else if (r2_start >= r1_start && r2_start < r1_end && r2_end > r1_start && r2_end >= r1_end) {
      return 1;
    }
    else if (r2_start >= r1_start && r2_start < r1_end && r2_end > r1_start && r2_end <=r1_end) {
      return 2;
    }
    else if (r1_start >= r2_start && r1_start < r2_end && r1_end > r2_start && r1_end >= r2_end) {
      return 3;
    }
    else if (r1_start >= r2_start && r1_start < r2_end && r1_end > r2_start && r1_end <= r2_end) {
      return 4;
    }
/*    else if (r1_start == r2_start && r2_end > r1_end) {
      return 5;
    }
    else if (r1_start < r2_start && r1_end  == r2_end) {
      return 6;
    }
    else if (r1_start == r2_start && r2_end  < r1_end) {
      return 7;
    }
    else if (r1_start > r2_start && r1_end  == r2_end) {
      return 8;
    }*/
    else {
      return -1;
    }
  },
  
  getChildrenInRange: function (elNode, elClassName, endId) {
    var objArray = new Array();
    var temp = new Array();
    var isEnd = false;
    
    for (var i=0; i<elNode.childNodes.length; i++) {
      var obj = elNode.childNodes[i];
      
      if (obj.id == endId || isEnd) {
        isEnd = true;
        alert("isEnd = " + isEnd);
        break;
      }
      else if (obj.className && obj.className.match(elClassName) != null && obj.className.match(elClassName) != "") {
        alert("obj.id = " + obj.id + "\n" +
              "obj.className = " + obj.className);

        if (obj.hasChildNodes) {
          alert("going into getChildrenInRange again");
          temp = this.getChildrenInRange(obj, elClassName, endId);
        }
        else 
          break;
        
        if (temp.length <= 0) 
          objArray.push(obj.id);
        else {
          alert("[getChildrenInRange] temp = " + temp);
          objArray.concat(temp); 
        }
         
      }
    }
    
    alert("[getChildrenInRange] objArray = " + objArray);
    return objArray;
  },
  
  getAllParentsInRange: function (startId, endId) {
    var objArray = new Array();
    var temp = new Array();
    var triggerNode = document.getElementById(startId);
    var targetNode = document.getElementById(endId);
    var isEnd = false;
    
    for (var currentNode = triggerNode.nextSibling; currentNode != null; currentNode = currentNode.nextSibling) {
        alert("isEnd = " + isEnd);
      
      if (targetNode.id == currentNode.id || isEnd) {
        isEnd = true;
        break;
      }
      else if (currentNode.nodeType == 1) {
        alert("triggerNode = " + triggerNode.id + "\n" +
              "currentNode = " + currentNode.id + "\n" +
              "targetNode = " + targetNode.id);
        if (currentNode.hasChildNodes) {
          temp = new Array( this.getChildrenInRange(currentNode, annotationConfig.xpointerMarker, targetNode.id) );
          
          alert("objArray = " + objArray + "\n" +
                "temp = " + temp);
          if (temp.length > 0)
            objArray.concat(temp);
          else if (currentNode.id != null && currentNode.id != "")
            objArray.push(currentNode.id);
        }
      }
    }
    
    alert("Outer objArray = " + objArray);
    
    return objArray;
  },
  
  getParentIdArray: function(startRange, startEl, endRange, endEl) {
      var parentArray = new Array();
      
      parentArray.push(startEl.id);
      
      var temp = new Array( this.getAllParentsInRange(startEl.id, endEl.id) );
      
      //alert("parentArray = " + parentArray + "\n" +
      //      "temp = " + temp);
  
      if (temp.length > 0)
        parentArray.concat(temp);
      
      //alert("Did it concat? " + parentArray);
      parentArray.push(endEl.id);

      return parentArray;  
  },

  contains: function(array, object) {
    if (array == null) {
      return false;
    }

    for (var i = 0; i < array.length; i++) {
      if (array[i] == object) {
        return true;
      }
    }
    return false;
  },

  findXptElementsInRange: function(startNode, endNode) {
    var startPath = this.getNodePath(startNode);
    var endPath   = this.getNodePath(endNode);
    var rootNode  = this.findLowestCommonNode(startPath, endPath);

//    this.setMultiBorder(startPath, "blue");
//    this.setMultiBorder(endPath, "red");
    this.setBorder(rootNode, "yellow");

    return this.accumulateLeafNodes(rootNode, startPath, endPath, new Array(1), new Array(annotationConfig.xpointerMarker));
  },

  getNodePath: function(node) {
    var path = new Array();
    for (var c = node; c != null; c = c.parentNode) {
      path.push(c);
    }
    return path;
  },

  findLowestCommonNode: function(startPath, endPath) {
    for (var i = 0; i < startPath.length; i++) {
      if (this.contains(endPath, startPath[i])) {
        return startPath[i];
      }
    }
  },

  accumulateLeafNodes: function (rootNode, startPath, endPath) {
    var nodeList = new Array();
    this.depthFirstTraversal(nodeList, rootNode, startPath, endPath, true, false);
    return nodeList;
  },

  setMultiBorder: function(nodeList, color) {
    for (var i = 0; i < nodeList.length; i++) {
      this.setBorder(nodeList[i], color);
    }
  },

  /**
   * Traverse a tree, accumulating those nodes in a subtree defined as being between a start path and an end path (inclusive).
   * All nodes that are to the right of the start path and to the left of the end path will be included.
   * nodeList is a list of all the nodes traversed so far, in order.
   * rootNode is the node to start traversing from. It may be added to the list, along with its descendents.
   * startPath is a list of all the nodes comprising the start path, in any order.
   * endPath is a list of all the nodes comprising the end path, in any order.
   * startPathPosition and endPathPosition tell us where we currently are in the tree relative to these two paths. This is a performance optimization for use when this method is called recursively.
   * A value of -1 indicates that we are to the left of the path, 0 indicates that we are on the path, 1 indicates that we are to the right of the path.
   * The first time the function is called, you should specify positions of -1 for both start and end paths, since the traversal starts from the left side of the tree.
   * includeLeaves should be true if you want leaf nodes to be accumulated.
   * includeNonLeaves should be true if you want non-leaf nodes to be accumulated.
   * If neither one is true, then this function won't do much.
   */
  depthFirstTraversal: function (nodeList, node, startPath, endPath, includeLeaves, includeNonLeaves) 
  {
    var onStartPath = this.contains(startPath, node);
    var onEndPath   = this.contains(endPath,   node);

    var childTraversed = false;
    var onPath = !onStartPath; // if this node is on the start path, then we assume the children are not on the path until the start path is encountered

//    this.setBorder(node, "blue");

    for (var i = 0; i < node.childNodes.length; i++) {
      var childNode = node.childNodes[i];

      if (!onPath) {
        onPath = this.contains(startPath, childNode);
      }

      if (!onPath) {
        this.setBorder(childNode, "red");
      }

      if (onPath) {
        if (this.isTypeAndClassOK(childNode)) {
          this.depthFirstTraversal(nodeList, node.childNodes[i], startPath, endPath, includeLeaves, includeNonLeaves);
          childTraversed = true;
        }

        if (onEndPath) {  // this check is not needed, but improves performance by skipping the block when we know we aren't going to encounter an end path.
          onPath = !this.contains(endPath, childNode);
        }
      }
    }

    if ( this.isTypeAndClassOK(node) &&
         ( (!childTraversed && includeLeaves) || 
           (childTraversed && includeNonLeaves) ) ) 
    {
      nodeList.push(node);

      alert("DING");
      this.setBorder(node, "green");
    }
    else {
      this.setBorder(node, null);
    }
  },

  isTypeAndClassOK: function(node) {
    return node.nodeType == 1 && node.className && node.className.indexOf(annotationConfig.xpointerMarker) != -1;
  },

  setBorder: function(node, color) {
    if (node.style) {
      node.style.border = color? "1px solid " + color : "none";  
    }
  },
    
  insertHighlightSpan: function (rangeObj) {
  	var noteClass = annotationConfig.annotationMarker + " " + 
    //      					(annotationConfig.isAuthor ? "author-" : "self-") +
          					(annotationConfig.isPublic ? "public" : "private") +
          					"-active";
  	var noteTitle = (annotationConfig.isAuthor ? "Author" : annotationConfig.isPublic ? "User" : "My") + 
          					" Annotation " + 
          					(annotationConfig.isPublic ? "(Public)" : "(Private)");
  	var spanId    = annotationConfig.dialogMarker;
  	var noteImg   = djConfig.namespace + "/images/" + "pone_note_" + (annotationConfig.isAuthor ? "author" : "private") + "_active.gif";
  	var noteImgClass = annotationConfig.annotationImgMarker;
  	  
    
    if (rangeObj.range.pasteHTML) {
      var html = rangeObj.range.htmlText;
      rangeObj.range.pasteHTML('<span class="' + noteClass + 
          								     '" title="'     + noteTitle +
          							        '" id="'       + spanId + 
          							        '"  annotationId=""' +
          							       '">' + 
//          							       '<img src="'  + noteImg + 
//          							       '" title="'   + noteTitle + 
//          							       '" class="'   + noteImgClass +
//          							       '" />' +
                               '<a href="#"" class="bug public" displayId=""  onclick="topaz.displayComment.show(this);" onmouseover="topaz.displayComment.mouseoverComment(this);" onmouseout="topaz.displayComment.mouseoutComment(this);" title="Click to preview this annotation">&nbsp;</a>' +
          							       html + '</span>');
    }
    else {
      // extract the contents (document fragment) from the range
      var contents = rangeObj.range.extractContents();

      // create a new span and insert it into the range in place of the original content
      var newSpan = document.createElement('span');
      newSpan.className = noteClass;
      newSpan.title     = noteTitle;
      newSpan.id        = spanId;
      newSpan.annotationId = "";

  	  var newImg = document.createElement('img');
  	  newImg.src       = noteImg;
  	  newImg.title     = noteTitle;
  	  newImg.className = noteImgClass;
      
      //newSpan.appendChild(newImg);
      
  	  var link = document.createElement("a");
  	  link.className = 'bug public';
  	  //link.href = '#';
  	  link.title = 'Click to preview this annotation';
  	  link.displayId = "";
  	  link.onclick = function() { topaz.displayComment.show(this); }
  	  link.onmouseover = function() { topaz.displayComment.mouseoverComment(this); }
  	  link.onmouseout = function() { topaz.displayComment.mouseoutComment(this); }
  	  link.appendChild(document.createTextNode('1'));

  	  newSpan.appendChild(link);
  	  //newSpan.innerHTML = '<a href="#"" class="bug" displayId=""  onclick="topaz.displayComment.show(this);" onmouseover="topaz.displayComment.mouseoverComment(this);" onmouseout="topaz.displayComment.mouseoutComment(this);" title="Click to preview this annotation">&nbsp;</a>';
  	  
      // populate the span with the content extracted from the range
      for (var i = 0; i < contents.childNodes.length; i++) {
        newSpan.appendChild(contents.childNodes[i].cloneNode(true));
      }

      rangeObj.range.insertNode(newSpan);
      
    }
  },
  
  normalizeText: function ( documentObj, resultStr ) {
    var tempStr = resultStr;
    
    for (var i=0; i<documentObj.childNodes.length; i++) {
      if (documentObj.childNodes[i].nodeType == 1) {
        tempStr = tempStr + this.normalizeText(documentObj.childNodes[i], '');
      }
      else if (documentObj.childNodes[i].nodeType == 3) {
        tempStr = tempStr + documentObj.childNodes[i].nodeValue;
      }
    }
    
    return tempStr;
  },
  
  alertMembers: function (obj, text) {
    if (obj) {
      var members = "";
      if (text) {
        members += text + ":\n";
      }
      
      for (var member in obj) {
        members += typeof obj[member];
        members += " ";
        members += member;
        members += ": ";
        members += (typeof obj[member] == 'function' ? '...' : obj[member]);
        members += "\n";
      }
      alert(members);
    } 
    else {
      alert("Object is not defined.");
    }
  },
  
  alertList: function (list) {
    var contents = "Contents of: " + list;
    for (var i = 0; i < list.length; i++) {
      contents += "\n";
      contents += typeof list[i];
      if (list[i].nodeType) {
        contents += " (";
        contents += list[i].nodeType;
        contents += ")";
      }
      contents += ": ";
      contents += list[i];
    }
    alert(contents);
  }
  
}







