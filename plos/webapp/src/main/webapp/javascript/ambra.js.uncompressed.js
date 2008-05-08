/*
  Copyright (c) 2004-2008, The Dojo Foundation
  All Rights Reserved.

  Licensed under the Academic Free License version 2.1 or above OR the
  modified BSD license. For more information on Dojo licensing, see:

    http://dojotoolkit.org/book/dojo-book-0-9/introduction/licensing
*/

/*
  This is a compiled version of Dojo, built for deployment and not for
  development. To get an editable version, please visit:

    http://dojotoolkit.org

  for documentation and information on getting the source.
*/

if(!dojo._hasResource["dijit._base.focus"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit._base.focus"] = true;
dojo.provide("dijit._base.focus");

// summary:
//    These functions are used to query or set the focus and selection.
//
//    Also, they trace when widgets become actived/deactivated,
//    so that the widget can fire _onFocus/_onBlur events.
//    "Active" here means something similar to "focused", but
//    "focus" isn't quite the right word because we keep track of
//    a whole stack of "active" widgets.  Example:  Combobutton --> Menu -->
//    MenuItem.   The onBlur event for Combobutton doesn't fire due to focusing
//    on the Menu or a MenuItem, since they are considered part of the
//    Combobutton widget.  It only happens when focus is shifted
//    somewhere completely different.

dojo.mixin(dijit,
{
  // _curFocus: DomNode
  //    Currently focused item on screen
  _curFocus: null,

  // _prevFocus: DomNode
  //    Previously focused item on screen
  _prevFocus: null,

  isCollapsed: function(){
    // summary: tests whether the current selection is empty
    var _window = dojo.global;
    var _document = dojo.doc;
    if(_document.selection){ // IE
      return !_document.selection.createRange().text; // Boolean
    }else{
      var selection = _window.getSelection();
      if(dojo.isString(selection)){ // Safari
        return !selection; // Boolean
      }else{ // Mozilla/W3
        return selection.isCollapsed || !selection.toString(); // Boolean
      }
    }
  },

  getBookmark: function(){
    // summary: Retrieves a bookmark that can be used with moveToBookmark to return to the same range
    var bookmark, selection = dojo.doc.selection;
    if(selection){ // IE
      var range = selection.createRange();
      if(selection.type.toUpperCase()=='CONTROL'){
        if(range.length){
          bookmark=[];
          var i=0,len=range.length;
          while(i<len){
            bookmark.push(range.item(i++));
          }
        }else{
          bookmark=null;
        }
      }else{
        bookmark = range.getBookmark();
      }
    }else{
      if(window.getSelection){
        selection = dojo.global.getSelection();
        if(selection){
          range = selection.getRangeAt(0);
          bookmark = range.cloneRange();
        }
      }else{
        console.warn("No idea how to store the current selection for this browser!");
      }
    }
    return bookmark; // Array
  },

  moveToBookmark: function(/*Object*/bookmark){
    // summary: Moves current selection to a bookmark
    // bookmark: This should be a returned object from dojo.html.selection.getBookmark()
    var _document = dojo.doc;
    if(_document.selection){ // IE
      var range;
      if(dojo.isArray(bookmark)){
        range = _document.body.createControlRange();
        dojo.forEach(bookmark, "range.addElement(item)"); //range.addElement does not have call/apply method, so can not call it directly
      }else{
        range = _document.selection.createRange();
        range.moveToBookmark(bookmark);
      }
      range.select();
    }else{ //Moz/W3C
      var selection = dojo.global.getSelection && dojo.global.getSelection();
      if(selection && selection.removeAllRanges){
        selection.removeAllRanges();
        selection.addRange(bookmark);
      }else{
        console.warn("No idea how to restore selection for this browser!");
      }
    }
  },

  getFocus: function(/*Widget?*/menu, /*Window?*/openedForWindow){
    // summary:
    //  Returns the current focus and selection.
    //  Called when a popup appears (either a top level menu or a dialog),
    //  or when a toolbar/menubar receives focus
    //
    // menu:
    //  The menu that's being opened
    //
    // openedForWindow:
    //  iframe in which menu was opened
    //
    // returns:
    //  A handle to restore focus/selection

    return {
      // Node to return focus to
      node: menu && dojo.isDescendant(dijit._curFocus, menu.domNode) ? dijit._prevFocus : dijit._curFocus,

      // Previously selected text
      bookmark:
        !dojo.withGlobal(openedForWindow||dojo.global, dijit.isCollapsed) ?
        dojo.withGlobal(openedForWindow||dojo.global, dijit.getBookmark) :
        null,

      openedForWindow: openedForWindow
    }; // Object
  },

  focus: function(/*Object || DomNode */ handle){
    // summary:
    //    Sets the focused node and the selection according to argument.
    //    To set focus to an iframe's content, pass in the iframe itself.
    // handle:
    //    object returned by get(), or a DomNode

    if(!handle){ return; }

    var node = "node" in handle ? handle.node : handle,   // because handle is either DomNode or a composite object
      bookmark = handle.bookmark,
      openedForWindow = handle.openedForWindow;

    // Set the focus
    // Note that for iframe's we need to use the <iframe> to follow the parentNode chain,
    // but we need to set focus to iframe.contentWindow
    if(node){
      var focusNode = (node.tagName.toLowerCase()=="iframe") ? node.contentWindow : node;
      if(focusNode && focusNode.focus){
        try{
          // Gecko throws sometimes if setting focus is impossible,
          // node not displayed or something like that
          focusNode.focus();
        }catch(e){/*quiet*/}
      }     
      dijit._onFocusNode(node);
    }

    // set the selection
    // do not need to restore if current selection is not empty
    // (use keyboard to select a menu item)
    if(bookmark && dojo.withGlobal(openedForWindow||dojo.global, dijit.isCollapsed)){
      if(openedForWindow){
        openedForWindow.focus();
      }
      try{
        dojo.withGlobal(openedForWindow||dojo.global, dijit.moveToBookmark, null, [bookmark]);
      }catch(e){
        /*squelch IE internal error, see http://trac.dojotoolkit.org/ticket/1984 */
      }
    }
  },

  // _activeStack: Array
  //    List of currently active widgets (focused widget and it's ancestors)
  _activeStack: [],

  registerWin: function(/*Window?*/targetWindow){
    // summary:
    //    Registers listeners on the specified window (either the main
    //    window or an iframe) to detect when the user has clicked somewhere.
    //    Anyone that creates an iframe should call this function.

    if(!targetWindow){
      targetWindow = window;
    }

    dojo.connect(targetWindow.document, "onmousedown", function(evt){
      dijit._justMouseDowned = true;
      setTimeout(function(){ dijit._justMouseDowned = false; }, 0);
      dijit._onTouchNode(evt.target||evt.srcElement);
    });
    //dojo.connect(targetWindow, "onscroll", ???);

    // Listen for blur and focus events on targetWindow's body
    var body = targetWindow.document.body || targetWindow.document.getElementsByTagName("body")[0];
    if(body){
      if(dojo.isIE){
        body.attachEvent('onactivate', function(evt){
          if(evt.srcElement.tagName.toLowerCase() != "body"){
            dijit._onFocusNode(evt.srcElement);
          }
        });
        body.attachEvent('ondeactivate', function(evt){ dijit._onBlurNode(evt.srcElement); });
      }else{
        body.addEventListener('focus', function(evt){ dijit._onFocusNode(evt.target); }, true);
        body.addEventListener('blur', function(evt){ dijit._onBlurNode(evt.target); }, true);
      }
    }
    body = null;  // prevent memory leak (apparent circular reference via closure)
  },

  _onBlurNode: function(/*DomNode*/ node){
    // summary:
    //    Called when focus leaves a node.
    //    Usually ignored, _unless_ it *isn't* follwed by touching another node,
    //    which indicates that we tabbed off the last field on the page,
    //    in which case every widget is marked inactive
    dijit._prevFocus = dijit._curFocus;
    dijit._curFocus = null;

    if(dijit._justMouseDowned){
      // the mouse down caused a new widget to be marked as active; this blur event
      // is coming late, so ignore it.
      return;
    }

    // if the blur event isn't followed by a focus event then mark all widgets as inactive.
    if(dijit._clearActiveWidgetsTimer){
      clearTimeout(dijit._clearActiveWidgetsTimer);
    }
    dijit._clearActiveWidgetsTimer = setTimeout(function(){
      delete dijit._clearActiveWidgetsTimer;
      dijit._setStack([]);
      dijit._prevFocus = null;
    }, 100);
  },

  _onTouchNode: function(/*DomNode*/ node){
    // summary:
    //    Callback when node is focused or mouse-downed

    // ignore the recent blurNode event
    if(dijit._clearActiveWidgetsTimer){
      clearTimeout(dijit._clearActiveWidgetsTimer);
      delete dijit._clearActiveWidgetsTimer;
    }

    // compute stack of active widgets (ex: ComboButton --> Menu --> MenuItem)
    var newStack=[];
    try{
      while(node){
        if(node.dijitPopupParent){
          node=dijit.byId(node.dijitPopupParent).domNode;
        }else if(node.tagName && node.tagName.toLowerCase()=="body"){
          // is this the root of the document or just the root of an iframe?
          if(node===dojo.body()){
            // node is the root of the main document
            break;
          }
          // otherwise, find the iframe this node refers to (can't access it via parentNode,
          // need to do this trick instead). window.frameElement is supported in IE/FF/Webkit
          node=dijit.getDocumentWindow(node.ownerDocument).frameElement;
        }else{
          var id = node.getAttribute && node.getAttribute("widgetId");
          if(id){
            newStack.unshift(id);
          }
          node=node.parentNode;
        }
      }
    }catch(e){ /* squelch */ }

    dijit._setStack(newStack);
  },

  _onFocusNode: function(/*DomNode*/ node){
    // summary
    //    Callback when node is focused
    if(node && node.tagName && node.tagName.toLowerCase() == "body"){
      return;
    }
    dijit._onTouchNode(node);

    if(node==dijit._curFocus){ return; }
    if(dijit._curFocus){
      dijit._prevFocus = dijit._curFocus;
    }
    dijit._curFocus = node;
    dojo.publish("focusNode", [node]);
  },

  _setStack: function(newStack){
    // summary
    //  The stack of active widgets has changed.  Send out appropriate events and record new stack

    var oldStack = dijit._activeStack;    
    dijit._activeStack = newStack;

    // compare old stack to new stack to see how many elements they have in common
    for(var nCommon=0; nCommon<Math.min(oldStack.length, newStack.length); nCommon++){
      if(oldStack[nCommon] != newStack[nCommon]){
        break;
      }
    }

    // for all elements that have gone out of focus, send blur event
    for(var i=oldStack.length-1; i>=nCommon; i--){
      var widget = dijit.byId(oldStack[i]);
      if(widget){
        widget._focused = false;
        widget._hasBeenBlurred = true;
        if(widget._onBlur){
          widget._onBlur();
        }
        if (widget._setStateClass){
          widget._setStateClass();
        }
        dojo.publish("widgetBlur", [widget]);
      }
    }

    // for all element that have come into focus, send focus event
    for(i=nCommon; i<newStack.length; i++){
      widget = dijit.byId(newStack[i]);
      if(widget){
        widget._focused = true;
        if(widget._onFocus){
          widget._onFocus();
        }
        if (widget._setStateClass){
          widget._setStateClass();
        }
        dojo.publish("widgetFocus", [widget]);
      }
    }
  }
});

// register top window and all the iframes it contains
dojo.addOnLoad(dijit.registerWin);

}

if(!dojo._hasResource["dijit._base.manager"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit._base.manager"] = true;
dojo.provide("dijit._base.manager");

dojo.declare("dijit.WidgetSet", null, {
  // summary:
  //  A set of widgets indexed by id

  constructor: function(){
    this._hash={};
  },

  add: function(/*Widget*/ widget){
    if(this._hash[widget.id]){
      throw new Error("Tried to register widget with id==" + widget.id + " but that id is already registered");
    }
    this._hash[widget.id]=widget;
  },

  remove: function(/*String*/ id){
    delete this._hash[id];
  },

  forEach: function(/*Function*/ func){
    for(var id in this._hash){
      func(this._hash[id]);
    }
  },

  filter: function(/*Function*/ filter){
    var res = new dijit.WidgetSet();
    this.forEach(function(widget){
      if(filter(widget)){ res.add(widget); }
    });
    return res;   // dijit.WidgetSet
  },

  byId: function(/*String*/ id){
    return this._hash[id];
  },

  byClass: function(/*String*/ cls){
    return this.filter(function(widget){ return widget.declaredClass==cls; });  // dijit.WidgetSet
  }
  });

/*=====
dijit.registry = {
  // summary: A list of widgets on a page.
  // description: Is an instance of dijit.WidgetSet
};
=====*/
dijit.registry = new dijit.WidgetSet();

dijit._widgetTypeCtr = {};

dijit.getUniqueId = function(/*String*/widgetType){
  // summary
  //  Generates a unique id for a given widgetType

  var id;
  do{
    id = widgetType + "_" +
      (widgetType in dijit._widgetTypeCtr ?
        ++dijit._widgetTypeCtr[widgetType] : dijit._widgetTypeCtr[widgetType] = 0);
  }while(dijit.byId(id));
  return id; // String
};


if(dojo.isIE){
  // Only run this for IE because we think it's only necessary in that case,
  // and because it causes problems on FF.  See bug #3531 for details.
  dojo.addOnUnload(function(){
    dijit.registry.forEach(function(widget){ widget.destroy(); });
  });
}

dijit.byId = function(/*String|Widget*/id){
  // summary:
  //    Returns a widget by its id, or if passed a widget, no-op (like dojo.byId())
  return (dojo.isString(id)) ? dijit.registry.byId(id) : id; // Widget
};

dijit.byNode = function(/* DOMNode */ node){
  // summary:
  //    Returns the widget as referenced by node
  return dijit.registry.byId(node.getAttribute("widgetId")); // Widget
};

dijit.getEnclosingWidget = function(/* DOMNode */ node){
  // summary:
  //    Returns the widget whose dom tree contains node or null if
  //    the node is not contained within the dom tree of any widget
  while(node){
    if(node.getAttribute && node.getAttribute("widgetId")){
      return dijit.registry.byId(node.getAttribute("widgetId"));
    }
    node = node.parentNode;
  }
  return null;
};

// elements that are tab-navigable if they have no tabindex value set
// (except for "a", which must have an href attribute)
dijit._tabElements = {
  area: true,
  button: true,
  input: true,
  object: true,
  select: true,
  textarea: true
};

dijit._isElementShown = function(/*Element*/elem){
  var style = dojo.style(elem);
  return (style.visibility != "hidden")
    && (style.visibility != "collapsed")
    && (style.display != "none");
}

dijit.isTabNavigable = function(/*Element*/elem){
  // summary:
  //    Tests if an element is tab-navigable
  if(dojo.hasAttr(elem, "disabled")){ return false; }
  var hasTabindex = dojo.hasAttr(elem, "tabindex");
  var tabindex = dojo.attr(elem, "tabindex");
  if(hasTabindex && tabindex >= 0) {
    return true; // boolean
  }
  var name = elem.nodeName.toLowerCase();
  if(((name == "a" && dojo.hasAttr(elem, "href"))
      || dijit._tabElements[name])
    && (!hasTabindex || tabindex >= 0)){
    return true; // boolean
  }
  return false; // boolean
};

dijit._getTabNavigable = function(/*DOMNode*/root){
  // summary:
  //    Finds the following descendants of the specified root node:
  //    * the first tab-navigable element in document order
  //      without a tabindex or with tabindex="0"
  //    * the last tab-navigable element in document order
  //      without a tabindex or with tabindex="0"
  //    * the first element in document order with the lowest
  //      positive tabindex value
  //    * the last element in document order with the highest
  //      positive tabindex value
  var first, last, lowest, lowestTabindex, highest, highestTabindex;
  var walkTree = function(/*DOMNode*/parent){
    dojo.query("> *", parent).forEach(function(child){
      var isShown = dijit._isElementShown(child);
      if(isShown && dijit.isTabNavigable(child)){
        var tabindex = dojo.attr(child, "tabindex");
        if(!dojo.hasAttr(child, "tabindex") || tabindex == 0){
          if(!first){ first = child; }
          last = child;
        }else if(tabindex > 0){
          if(!lowest || tabindex < lowestTabindex){
            lowestTabindex = tabindex;
            lowest = child;
          }
          if(!highest || tabindex >= highestTabindex){
            highestTabindex = tabindex;
            highest = child;
          }
        }
      }
      if(isShown){ walkTree(child) }
    });
  };
  if(dijit._isElementShown(root)){ walkTree(root) }
  return { first: first, last: last, lowest: lowest, highest: highest };
}

dijit.getFirstInTabbingOrder = function(/*String|DOMNode*/root){
  // summary:
  //    Finds the descendant of the specified root node
  //    that is first in the tabbing order
  var elems = dijit._getTabNavigable(dojo.byId(root));
  return elems.lowest ? elems.lowest : elems.first; // Element
};

dijit.getLastInTabbingOrder = function(/*String|DOMNode*/root){
  // summary:
  //    Finds the descendant of the specified root node
  //    that is last in the tabbing order
  var elems = dijit._getTabNavigable(dojo.byId(root));
  return elems.last ? elems.last : elems.highest; // Element
};

}

if(!dojo._hasResource["dijit._base.place"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit._base.place"] = true;
dojo.provide("dijit._base.place");

// ported from dojo.html.util

dijit.getViewport = function(){
  //  summary
  //  Returns the dimensions and scroll position of the viewable area of a browser window

  var _window = dojo.global;
  var _document = dojo.doc;

  // get viewport size
  var w = 0, h = 0;
  var de = _document.documentElement;
  var dew = de.clientWidth, deh = de.clientHeight;
  if(dojo.isMozilla){
    // mozilla
    // _window.innerHeight includes the height taken by the scroll bar
    // clientHeight is ideal but has DTD issues:
    // #4539: FF reverses the roles of body.clientHeight/Width and documentElement.clientHeight/Width based on the DTD!
    // check DTD to see whether body or documentElement returns the viewport dimensions using this algorithm:
    var minw, minh, maxw, maxh;
    var dbw = _document.body.clientWidth;
    if(dbw > dew){
      minw = dew;
      maxw = dbw;
    }else{
      maxw = dew;
      minw = dbw;
    }
    var dbh = _document.body.clientHeight;
    if(dbh > deh){
      minh = deh;
      maxh = dbh;
    }else{
      maxh = deh;
      minh = dbh;
    }
    w = (maxw > _window.innerWidth) ? minw : maxw;
    h = (maxh > _window.innerHeight) ? minh : maxh;
  }else if(!dojo.isOpera && _window.innerWidth){
    //in opera9, dojo.body().clientWidth should be used, instead
    //of window.innerWidth/document.documentElement.clientWidth
    //so we have to check whether it is opera
    w = _window.innerWidth;
    h = _window.innerHeight;
  }else if(dojo.isIE && de && deh){
    w = dew;
    h = deh;
  }else if(dojo.body().clientWidth){
    // IE5, Opera
    w = dojo.body().clientWidth;
    h = dojo.body().clientHeight;
  }

  // get scroll position
  var scroll = dojo._docScroll();

  return { w: w, h: h, l: scroll.x, t: scroll.y };  //  object
};

dijit.placeOnScreen = function(
  /* DomNode */ node,
  /* Object */    pos,
  /* Object */    corners,
  /* boolean? */    tryOnly){
  //  summary:
  //    Keeps 'node' in the visible area of the screen while trying to
  //    place closest to pos.x, pos.y. The input coordinates are
  //    expected to be the desired document position.
  //
  //    Set which corner(s) you want to bind to, such as
  //    
  //      placeOnScreen(node, {x: 10, y: 20}, ["TR", "BL"])
  //    
  //    The desired x/y will be treated as the topleft(TL)/topright(TR) or
  //    BottomLeft(BL)/BottomRight(BR) corner of the node. Each corner is tested
  //    and if a perfect match is found, it will be used. Otherwise, it goes through
  //    all of the specified corners, and choose the most appropriate one.
  //    
  //    NOTE: node is assumed to be absolutely or relatively positioned.

  var choices = dojo.map(corners, function(corner){ return { corner: corner, pos: pos }; });

  return dijit._place(node, choices);
}

dijit._place = function(/*DomNode*/ node, /* Array */ choices, /* Function */ layoutNode){
  // summary:
  //    Given a list of spots to put node, put it at the first spot where it fits,
  //    of if it doesn't fit anywhere then the place with the least overflow
  // choices: Array
  //    Array of elements like: {corner: 'TL', pos: {x: 10, y: 20} }
  //    Above example says to put the top-left corner of the node at (10,20)
  //  layoutNode: Function(node, aroundNodeCorner, nodeCorner)
  //    for things like tooltip, they are displayed differently (and have different dimensions)
  //    based on their orientation relative to the parent.   This adjusts the popup based on orientation.

  // get {x: 10, y: 10, w: 100, h:100} type obj representing position of
  // viewport over document
  var view = dijit.getViewport();

  // This won't work if the node is inside a <div style="position: relative">,
  // so reattach it to dojo.doc.body.   (Otherwise, the positioning will be wrong
  // and also it might get cutoff)
  if(!node.parentNode || String(node.parentNode.tagName).toLowerCase() != "body"){
    dojo.body().appendChild(node);
  }

  var best = null;
  dojo.some(choices, function(choice){
    var corner = choice.corner;
    var pos = choice.pos;

    // configure node to be displayed in given position relative to button
    // (need to do this in order to get an accurate size for the node, because
    // a tooltips size changes based on position, due to triangle)
    if(layoutNode){
      layoutNode(node, choice.aroundCorner, corner);
    }

    // get node's size
    var style = node.style;
    var oldDisplay = style.display;
    var oldVis = style.visibility;
    style.visibility = "hidden";
    style.display = "";
    var mb = dojo.marginBox(node);
    style.display = oldDisplay;
    style.visibility = oldVis;

    // coordinates and size of node with specified corner placed at pos,
    // and clipped by viewport
    var startX = (corner.charAt(1) == 'L' ? pos.x : Math.max(view.l, pos.x - mb.w)),
      startY = (corner.charAt(0) == 'T' ? pos.y : Math.max(view.t, pos.y -  mb.h)),
      endX = (corner.charAt(1) == 'L' ? Math.min(view.l + view.w, startX + mb.w) : pos.x),
      endY = (corner.charAt(0) == 'T' ? Math.min(view.t + view.h, startY + mb.h) : pos.y),
      width = endX - startX,
      height = endY - startY,
      overflow = (mb.w - width) + (mb.h - height);

    if(best == null || overflow < best.overflow){
      best = {
        corner: corner,
        aroundCorner: choice.aroundCorner,
        x: startX,
        y: startY,
        w: width,
        h: height,
        overflow: overflow
      };
    }
    return !overflow;
  });

  node.style.left = best.x + "px";
  node.style.top = best.y + "px";
  if(best.overflow && layoutNode){
    layoutNode(node, best.aroundCorner, best.corner);
  }
  return best;
}

dijit.placeOnScreenAroundElement = function(
  /* DomNode */   node,
  /* DomNode */   aroundNode,
  /* Object */    aroundCorners,
  /* Function */    layoutNode){

  //  summary
  //  Like placeOnScreen, except it accepts aroundNode instead of x,y
  //  and attempts to place node around it.  Uses margin box dimensions.
  //
  //  aroundCorners
  //    specify Which corner of aroundNode should be
  //    used to place the node => which corner(s) of node to use (see the
  //    corners parameter in dijit.placeOnScreen)
  //    e.g. {'TL': 'BL', 'BL': 'TL'}
  //
  //  layoutNode: Function(node, aroundNodeCorner, nodeCorner)
  //    for things like tooltip, they are displayed differently (and have different dimensions)
  //    based on their orientation relative to the parent.   This adjusts the popup based on orientation.


  // get coordinates of aroundNode
  aroundNode = dojo.byId(aroundNode);
  var oldDisplay = aroundNode.style.display;
  aroundNode.style.display="";
  // #3172: use the slightly tighter border box instead of marginBox
  var aroundNodeW = aroundNode.offsetWidth; //mb.w;
  var aroundNodeH = aroundNode.offsetHeight; //mb.h;
  var aroundNodePos = dojo.coords(aroundNode, true);
  aroundNode.style.display=oldDisplay;

  // Generate list of possible positions for node
  var choices = [];
  for(var nodeCorner in aroundCorners){
    choices.push( {
      aroundCorner: nodeCorner,
      corner: aroundCorners[nodeCorner],
      pos: {
        x: aroundNodePos.x + (nodeCorner.charAt(1) == 'L' ? 0 : aroundNodeW),
        y: aroundNodePos.y + (nodeCorner.charAt(0) == 'T' ? 0 : aroundNodeH)
      }
    });
  }

  return dijit._place(node, choices, layoutNode);
}

}

if(!dojo._hasResource["dijit._base.window"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit._base.window"] = true;
dojo.provide("dijit._base.window");

dijit.getDocumentWindow = function(doc){
  //  summary
  //  Get window object associated with document doc

  // With Safari, there is not way to retrieve the window from the document, so we must fix it.
  if(dojo.isSafari && !doc._parentWindow){
    /*
      This is a Safari specific function that fix the reference to the parent
      window from the document object.
      TODO: #5711: should the use of document below reference dojo.doc instead
      in case they're not the same?
    */
    var fix=function(win){
      win.document._parentWindow=win;
      for(var i=0; i<win.frames.length; i++){
        fix(win.frames[i]);
      }
    }
    fix(window.top);
  }

  //In some IE versions (at least 6.0), document.parentWindow does not return a
  //reference to the real window object (maybe a copy), so we must fix it as well
  //We use IE specific execScript to attach the real window reference to
  //document._parentWindow for later use
  //TODO: #5711: should the use of document below reference dojo.doc instead in case they're not the same?
  if(dojo.isIE && window !== document.parentWindow && !doc._parentWindow){
    /*
    In IE 6, only the variable "window" can be used to connect events (others
    may be only copies).
    */
    doc.parentWindow.execScript("document._parentWindow = window;", "Javascript");
    //to prevent memory leak, unset it after use
    //another possibility is to add an onUnload handler which seems overkill to me (liucougar)
    var win = doc._parentWindow;
    doc._parentWindow = null;
    return win; //  Window
  }

  return doc._parentWindow || doc.parentWindow || doc.defaultView;  //  Window
}

}

if(!dojo._hasResource["dijit._base.popup"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit._base.popup"] = true;
dojo.provide("dijit._base.popup");





dijit.popup = new function(){
  // summary:
  //    This class is used to show/hide widgets as popups.
  //

  var stack = [],
    beginZIndex=1000,
    idGen = 1;

  this.prepare = function(/*DomNode*/ node){
    // summary:
    //    Prepares a node to be used as a popup
    //
    // description:
    //    Attaches node to dojo.doc.body, and
    //    positions it off screen, but not display:none, so that
    //    the widget doesn't appear in the page flow and/or cause a blank
    //    area at the bottom of the viewport (making scrollbar longer), but
    //    initialization of contained widgets works correctly
  
    dojo.body().appendChild(node);
    var s = node.style;
    if(s.display == "none"){
      s.display="";
    }
    s.visibility = "hidden";  // not needed for hiding, but used as flag that node is off-screen
    s.position = "absolute";
    s.top = "-9999px";
  };

  this.open = function(/*Object*/ args){
    // summary:
    //    Popup the widget at the specified position
    //
    // args: Object
    //    popup: Widget
    //      widget to display,
    //    parent: Widget
    //      the button etc. that is displaying this popup
    //    around: DomNode
    //      DOM node (typically a button); place popup relative to this node
    //    orient: Object
    //      structure specifying possible positions of popup relative to "around" node
    //    onCancel: Function
    //      callback when user has canceled the popup by
    //        1. hitting ESC or
    //        2. by using the popup widget's proprietary cancel mechanism (like a cancel button in a dialog);
    //           ie: whenever popupWidget.onCancel() is called, args.onCancel is called
    //    onClose: Function
    //      callback whenever this popup is closed
    //    onExecute: Function
    //      callback when user "executed" on the popup/sub-popup by selecting a menu choice, etc. (top menu only)
    //
    // examples:
    //    1. opening at the mouse position
    //      dijit.popup.open({popup: menuWidget, x: evt.pageX, y: evt.pageY});
    //    2. opening the widget as a dropdown
    //      dijit.popup.open({parent: this, popup: menuWidget, around: this.domNode, onClose: function(){...}  });
    //
    //  Note that whatever widget called dijit.popup.open() should also listen to it's own _onBlur callback
    //  (fired from _base/focus.js) to know that focus has moved somewhere else and thus the popup should be closed.

    var widget = args.popup,
      orient = args.orient || {'BL':'TL', 'TL':'BL'},
      around = args.around,
      id = (args.around && args.around.id) ? (args.around.id+"_dropdown") : ("popup_"+idGen++);

    // make wrapper div to hold widget and possibly hold iframe behind it.
    // we can't attach the iframe as a child of the widget.domNode because
    // widget.domNode might be a <table>, <ul>, etc.
    var wrapper = dojo.doc.createElement("div");
    dijit.setWaiRole(wrapper, "presentation");
    wrapper.id = id;
    wrapper.className="dijitPopup";
    wrapper.style.zIndex = beginZIndex + stack.length;
    wrapper.style.visibility = "hidden";
    if(args.parent){
      wrapper.dijitPopupParent=args.parent.id;
    }
    dojo.body().appendChild(wrapper);

    var s = widget.domNode.style;
    s.display = "";
    s.visibility = "";
    s.position = "";
    wrapper.appendChild(widget.domNode);

    var iframe = new dijit.BackgroundIframe(wrapper);

    // position the wrapper node
    var best = around ?
      dijit.placeOnScreenAroundElement(wrapper, around, orient, widget.orient ? dojo.hitch(widget, "orient") : null) :
      dijit.placeOnScreen(wrapper, args, orient == 'R' ? ['TR','BR','TL','BL'] : ['TL','BL','TR','BR']);

    wrapper.style.visibility = "visible";
    // TODO: use effects to fade in wrapper

    var handlers = [];

    // Compute the closest ancestor popup that's *not* a child of another popup.
    // Ex: For a TooltipDialog with a button that spawns a tree of menus, find the popup of the button.
    var getTopPopup = function(){
      for(var pi=stack.length-1; pi > 0 && stack[pi].parent === stack[pi-1].widget; pi--){
        /* do nothing, just trying to get right value for pi */
      }
      return stack[pi];
    }

    // provide default escape and tab key handling
    // (this will work for any widget, not just menu)
    handlers.push(dojo.connect(wrapper, "onkeypress", this, function(evt){
      if(evt.keyCode == dojo.keys.ESCAPE && args.onCancel){
        dojo.stopEvent(evt);
        args.onCancel();
      }else if(evt.keyCode == dojo.keys.TAB){
        dojo.stopEvent(evt);
        var topPopup = getTopPopup();
        if(topPopup && topPopup.onCancel){
          topPopup.onCancel();
        }
      }
    }));

    // watch for cancel/execute events on the popup and notify the caller
    // (for a menu, "execute" means clicking an item)
    if(widget.onCancel){
      handlers.push(dojo.connect(widget, "onCancel", null, args.onCancel));
    }

    handlers.push(dojo.connect(widget, widget.onExecute ? "onExecute" : "onChange", null, function(){
      var topPopup = getTopPopup();
      if(topPopup && topPopup.onExecute){
        topPopup.onExecute();
      }
    }));

    stack.push({
      wrapper: wrapper,
      iframe: iframe,
      widget: widget,
      parent: args.parent,
      onExecute: args.onExecute,
      onCancel: args.onCancel,
      onClose: args.onClose,
      handlers: handlers
    });

    if(widget.onOpen){
      widget.onOpen(best);
    }

    return best;
  };

  this.close = function(/*Widget*/ popup){
    // summary:
    //    Close specified popup and any popups that it parented
    while(dojo.some(stack, function(elem){return elem.widget == popup;})){
      var top = stack.pop(),
        wrapper = top.wrapper,
        iframe = top.iframe,
        widget = top.widget,
        onClose = top.onClose;
  
      if(widget.onClose){
        widget.onClose();
      }
      dojo.forEach(top.handlers, dojo.disconnect);
  
      // #2685: check if the widget still has a domNode so ContentPane can change its URL without getting an error
      if(!widget||!widget.domNode){ return; }
      
      this.prepare(widget.domNode);

      iframe.destroy();
      dojo._destroyElement(wrapper);
  
      if(onClose){
        onClose();
      }
    }
  };
}();

dijit._frames = new function(){
  // summary: cache of iframes
  var queue = [];

  this.pop = function(){
    var iframe;
    if(queue.length){
      iframe = queue.pop();
      iframe.style.display="";
    }else{
      if(dojo.isIE){
        var html="<iframe src='javascript:\"\"'"
          + " style='position: absolute; left: 0px; top: 0px;"
          + "z-index: -1; filter:Alpha(Opacity=\"0\");'>";
        iframe = dojo.doc.createElement(html);
      }else{
        iframe = dojo.doc.createElement("iframe");
        iframe.src = 'javascript:""';
        iframe.className = "dijitBackgroundIframe";
      }
      iframe.tabIndex = -1; // Magic to prevent iframe from getting focus on tab keypress - as style didnt work.
      dojo.body().appendChild(iframe);
    }
    return iframe;
  };

  this.push = function(iframe){
    iframe.style.display="";
    if(dojo.isIE){
      iframe.style.removeExpression("width");
      iframe.style.removeExpression("height");
    }
    queue.push(iframe);
  }
}();

// fill the queue
if(dojo.isIE && dojo.isIE < 7){
  dojo.addOnLoad(function(){
    var f = dijit._frames;
    dojo.forEach([f.pop()], f.push);
  });
}


dijit.BackgroundIframe = function(/* DomNode */node){
  //  summary:
  //    For IE z-index schenanigans. id attribute is required.
  //
  //  description:
  //    new dijit.BackgroundIframe(node)
  //      Makes a background iframe as a child of node, that fills
  //      area (and position) of node

  if(!node.id){ throw new Error("no id"); }
  if((dojo.isIE && dojo.isIE < 7) || (dojo.isFF && dojo.isFF < 3 && dojo.hasClass(dojo.body(), "dijit_a11y"))){
    var iframe = dijit._frames.pop();
    node.appendChild(iframe);
    if(dojo.isIE){
      iframe.style.setExpression("width", dojo._scopeName + ".doc.getElementById('" + node.id + "').offsetWidth");
      iframe.style.setExpression("height", dojo._scopeName + ".doc.getElementById('" + node.id + "').offsetHeight");
    }
    this.iframe = iframe;
  }
};

dojo.extend(dijit.BackgroundIframe, {
  destroy: function(){
    //  summary: destroy the iframe
    if(this.iframe){
      dijit._frames.push(this.iframe);
      delete this.iframe;
    }
  }
});

}

if(!dojo._hasResource["dijit._base.scroll"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit._base.scroll"] = true;
dojo.provide("dijit._base.scroll");

dijit.scrollIntoView = function(/* DomNode */node){
  //  summary
  //  Scroll the passed node into view, if it is not.

  // don't rely on that node.scrollIntoView works just because the function is there
  // it doesnt work in Konqueror or Opera even though the function is there and probably
  // not safari either
  // dont like browser sniffs implementations but sometimes you have to use it
  if(dojo.isMozilla){
    node.scrollIntoView(false);
  }else{
    // #6146: IE scrollIntoView is broken
    // It's not enough just to scroll the menu node into view if
    // node.scrollIntoView hides part of the parent's scrollbar,
    // so just manage the parent scrollbar ourselves
    var parent = node.parentNode;
    var parentBottom = parent.scrollTop + dojo.marginBox(parent).h; //PORT was getBorderBox
    var nodeBottom = node.offsetTop + dojo.marginBox(node).h;
    if(parentBottom < nodeBottom){
      parent.scrollTop += (nodeBottom - parentBottom);
    }else if(parent.scrollTop > node.offsetTop){
      parent.scrollTop -= (parent.scrollTop - node.offsetTop);
    }
  }
};

}

if(!dojo._hasResource["dijit._base.sniff"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit._base.sniff"] = true;
dojo.provide("dijit._base.sniff");

// ported from dojo.html.applyBrowserClass (style.js)

//  summary:
//    Applies pre-set class names based on browser & version to the
//    top-level HTML node.  Simply doing a require on this module will
//    establish this CSS.  Modified version of Morris' CSS hack.
(function(){
  var d = dojo;
  var ie = d.isIE;
  var opera = d.isOpera;
  var maj = Math.floor;
  var ff = d.isFF;
  var classes = {
    dj_ie: ie,
//    dj_ie55: ie == 5.5,
    dj_ie6: maj(ie) == 6,
    dj_ie7: maj(ie) == 7,
    dj_iequirks: ie && d.isQuirks,
// NOTE: Opera not supported by dijit
    dj_opera: opera,
    dj_opera8: maj(opera) == 8,
    dj_opera9: maj(opera) == 9,
    dj_khtml: d.isKhtml,
    dj_safari: d.isSafari,
    dj_gecko: d.isMozilla,
    dj_ff2: maj(ff) == 2
  }; // no dojo unsupported browsers

  for(var p in classes){
    if(classes[p]){
      var html = dojo.doc.documentElement; //TODO browser-specific DOM magic needed?
      if(html.className){
        html.className += " " + p;
      }else{
        html.className = p;
      }
    }
  }
})();

}

if(!dojo._hasResource["dijit._base.bidi"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit._base.bidi"] = true;
dojo.provide("dijit._base.bidi");

// summary: applies a class to the top of the document for right-to-left stylesheet rules

dojo.addOnLoad(function(){
  if(!dojo._isBodyLtr()){
    dojo.addClass(dojo.body(), "dijitRtl");
  }
});

}

if(!dojo._hasResource["dijit._base.typematic"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit._base.typematic"] = true;
dojo.provide("dijit._base.typematic");

dijit.typematic = {
  // summary:
  //  These functions are used to repetitively call a user specified callback
  //  method when a specific key or mouse click over a specific DOM node is
  //  held down for a specific amount of time.
  //  Only 1 such event is allowed to occur on the browser page at 1 time.

  _fireEventAndReload: function(){
    this._timer = null;
    this._callback(++this._count, this._node, this._evt);
    this._currentTimeout = (this._currentTimeout < 0) ? this._initialDelay : ((this._subsequentDelay > 1) ? this._subsequentDelay : Math.round(this._currentTimeout * this._subsequentDelay));
    this._timer = setTimeout(dojo.hitch(this, "_fireEventAndReload"), this._currentTimeout);
  },

  trigger: function(/*Event*/ evt, /* Object */ _this, /*DOMNode*/ node, /* Function */ callback, /* Object */ obj, /* Number */ subsequentDelay, /* Number */ initialDelay){
    // summary:
    //      Start a timed, repeating callback sequence.
    //      If already started, the function call is ignored.
    //      This method is not normally called by the user but can be
    //      when the normal listener code is insufficient.
    //  Parameters:
    //  evt: key or mouse event object to pass to the user callback
    //  _this: pointer to the user's widget space.
    //  node: the DOM node object to pass the the callback function
    //  callback: function to call until the sequence is stopped called with 3 parameters:
    //    count: integer representing number of repeated calls (0..n) with -1 indicating the iteration has stopped
    //    node: the DOM node object passed in
    //    evt: key or mouse event object
    //  obj: user space object used to uniquely identify each typematic sequence
    //  subsequentDelay: if > 1, the number of milliseconds until the 3->n events occur
    //    or else the fractional time multiplier for the next event's delay, default=0.9
    //  initialDelay: the number of milliseconds until the 2nd event occurs, default=500ms
    if(obj != this._obj){
      this.stop();
      this._initialDelay = initialDelay || 500;
      this._subsequentDelay = subsequentDelay || 0.90;
      this._obj = obj;
      this._evt = evt;
      this._node = node;
      this._currentTimeout = -1;
      this._count = -1;
      this._callback = dojo.hitch(_this, callback);
      this._fireEventAndReload();
    }
  },

  stop: function(){
    // summary:
    //    Stop an ongoing timed, repeating callback sequence.
    if(this._timer){
      clearTimeout(this._timer);
      this._timer = null;
    }
    if(this._obj){
      this._callback(-1, this._node, this._evt);
      this._obj = null;
    }
  },

  addKeyListener: function(/*DOMNode*/ node, /*Object*/ keyObject, /*Object*/ _this, /*Function*/ callback, /*Number*/ subsequentDelay, /*Number*/ initialDelay){
    // summary: Start listening for a specific typematic key.
    //  keyObject: an object defining the key to listen for.
    //    key: (mandatory) the keyCode (number) or character (string) to listen for.
    //    ctrlKey: desired ctrl key state to initiate the calback sequence:
    //      pressed (true)
    //      released (false)
    //      either (unspecified)
    //    altKey: same as ctrlKey but for the alt key
    //    shiftKey: same as ctrlKey but for the shift key
    //  See the trigger method for other parameters.
    //  Returns an array of dojo.connect handles
    return [
      dojo.connect(node, "onkeypress", this, function(evt){
        if(evt.keyCode == keyObject.keyCode && (!keyObject.charCode || keyObject.charCode == evt.charCode) &&
        (keyObject.ctrlKey === undefined || keyObject.ctrlKey == evt.ctrlKey) &&
        (keyObject.altKey === undefined || keyObject.altKey == evt.ctrlKey) &&
        (keyObject.shiftKey === undefined || keyObject.shiftKey == evt.ctrlKey)){
          dojo.stopEvent(evt);
          dijit.typematic.trigger(keyObject, _this, node, callback, keyObject, subsequentDelay, initialDelay);
        }else if(dijit.typematic._obj == keyObject){
          dijit.typematic.stop();
        }
      }),
      dojo.connect(node, "onkeyup", this, function(evt){
        if(dijit.typematic._obj == keyObject){
          dijit.typematic.stop();
        }
      })
    ];
  },

  addMouseListener: function(/*DOMNode*/ node, /*Object*/ _this, /*Function*/ callback, /*Number*/ subsequentDelay, /*Number*/ initialDelay){
    // summary: Start listening for a typematic mouse click.
    //  See the trigger method for other parameters.
    //  Returns an array of dojo.connect handles
    var dc = dojo.connect;
    return [
      dc(node, "mousedown", this, function(evt){
        dojo.stopEvent(evt);
        dijit.typematic.trigger(evt, _this, node, callback, node, subsequentDelay, initialDelay);
      }),
      dc(node, "mouseup", this, function(evt){
        dojo.stopEvent(evt);
        dijit.typematic.stop();
      }),
      dc(node, "mouseout", this, function(evt){
        dojo.stopEvent(evt);
        dijit.typematic.stop();
      }),
      dc(node, "mousemove", this, function(evt){
        dojo.stopEvent(evt);
      }),
      dc(node, "dblclick", this, function(evt){
        dojo.stopEvent(evt);
        if(dojo.isIE){
          dijit.typematic.trigger(evt, _this, node, callback, node, subsequentDelay, initialDelay);
          setTimeout(dojo.hitch(this, dijit.typematic.stop), 50);
        }
      })
    ];
  },

  addListener: function(/*Node*/ mouseNode, /*Node*/ keyNode, /*Object*/ keyObject, /*Object*/ _this, /*Function*/ callback, /*Number*/ subsequentDelay, /*Number*/ initialDelay){
    // summary: Start listening for a specific typematic key and mouseclick.
    //  This is a thin wrapper to addKeyListener and addMouseListener.
    //  mouseNode: the DOM node object to listen on for mouse events.
    //  keyNode: the DOM node object to listen on for key events.
    //  See the addMouseListener and addKeyListener methods for other parameters.
    //  Returns an array of dojo.connect handles
    return this.addKeyListener(keyNode, keyObject, _this, callback, subsequentDelay, initialDelay).concat(
      this.addMouseListener(mouseNode, _this, callback, subsequentDelay, initialDelay));
  }
};

}

if(!dojo._hasResource["dijit._base.wai"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit._base.wai"] = true;
dojo.provide("dijit._base.wai");

dijit.wai = {
  onload: function(){
    // summary:
    //    Detects if we are in high-contrast mode or not

    // This must be a named function and not an anonymous
    // function, so that the widget parsing code can make sure it
    // registers its onload function after this function.
    // DO NOT USE "this" within this function.

    // create div for testing if high contrast mode is on or images are turned off
    var div = dojo.doc.createElement("div");
    div.id = "a11yTestNode";
    div.style.cssText = 'border: 1px solid;'
      + 'border-color:red green;'
      + 'position: absolute;'
      + 'height: 5px;'
      + 'top: -999px;'
      + 'background-image: url("' + dojo.moduleUrl("dojo", "resources/blank.gif") + '");';
    dojo.body().appendChild(div);

    // test it
    var cs = dojo.getComputedStyle(div);
    if(cs){
      var bkImg = cs.backgroundImage;
      var needsA11y = (cs.borderTopColor==cs.borderRightColor) || (bkImg != null && (bkImg == "none" || bkImg == "url(invalid-url:)" ));
      dojo[needsA11y ? "addClass" : "removeClass"](dojo.body(), "dijit_a11y");
      dojo.body().removeChild(div);
    }
  }
};

// Test if computer is in high contrast mode.
// Make sure the a11y test runs first, before widgets are instantiated.
if(dojo.isIE || dojo.isMoz){  // NOTE: checking in Safari messes things up
  dojo._loaders.unshift(dijit.wai.onload);
}

dojo.mixin(dijit,
{
  hasWaiRole: function(/*Element*/ elem){
    // summary: Determines if an element has a role.
    // returns: true if elem has a role attribute and false if not.
    return elem.hasAttribute ? elem.hasAttribute("role") : !!elem.getAttribute("role");
  },

  getWaiRole: function(/*Element*/ elem){
    // summary: Gets the role for an element.
    // returns:
    //    The role of elem or an empty string if elem
    //    does not have a role.
    var value = elem.getAttribute("role");
    if(value){
      var prefixEnd = value.indexOf(":");
      return prefixEnd == -1 ? value : value.substring(prefixEnd+1);
    }else{
      return "";
    }
  },

  setWaiRole: function(/*Element*/ elem, /*String*/ role){
    // summary: Sets the role on an element.
    // description:
    //    On Firefox 2 and below, "wairole:" is
    //    prepended to the provided role value.
    elem.setAttribute("role", (dojo.isFF && dojo.isFF < 3) ? "wairole:" + role : role);
  },

  removeWaiRole: function(/*Element*/ elem){
    // summary: Removes the role from an element.
    elem.removeAttribute("role");
  },

  hasWaiState: function(/*Element*/ elem, /*String*/ state){
    // summary: Determines if an element has a given state.
    // description:
    //    On Firefox 2 and below, we check for an attribute in namespace
    //    "http://www.w3.org/2005/07/aaa" with a name of the given state.
    //    On all other browsers, we check for an attribute
    //    called "aria-"+state.
    // returns:
    //    true if elem has a value for the given state and
    //    false if it does not.
    if(dojo.isFF && dojo.isFF < 3){
      return elem.hasAttributeNS("http://www.w3.org/2005/07/aaa", state);
    }else{
      return elem.hasAttribute ? elem.hasAttribute("aria-"+state) : !!elem.getAttribute("aria-"+state);
    }
  },

  getWaiState: function(/*Element*/ elem, /*String*/ state){
    // summary: Gets the value of a state on an element.
    // description:
    //    On Firefox 2 and below, we check for an attribute in namespace
    //    "http://www.w3.org/2005/07/aaa" with a name of the given state.
    //    On all other browsers, we check for an attribute called
    //    "aria-"+state.
    // returns:
    //    The value of the requested state on elem
    //    or an empty string if elem has no value for state.
    if(dojo.isFF && dojo.isFF < 3){
      return elem.getAttributeNS("http://www.w3.org/2005/07/aaa", state);
    }else{
      var value = elem.getAttribute("aria-"+state);
      return value ? value : "";
    }
  },

  setWaiState: function(/*Element*/ elem, /*String*/ state, /*String*/ value){
    // summary: Sets a state on an element.
    // description:
    //    On Firefox 2 and below, we set an attribute in namespace
    //    "http://www.w3.org/2005/07/aaa" with a name of the given state.
    //    On all other browsers, we set an attribute called
    //    "aria-"+state.
    if(dojo.isFF && dojo.isFF < 3){
      elem.setAttributeNS("http://www.w3.org/2005/07/aaa",
        "aaa:"+state, value);
    }else{
      elem.setAttribute("aria-"+state, value);
    }
  },

  removeWaiState: function(/*Element*/ elem, /*String*/ state){
    // summary: Removes a state from an element.
    // description:
    //    On Firefox 2 and below, we remove the attribute in namespace
    //    "http://www.w3.org/2005/07/aaa" with a name of the given state.
    //    On all other browsers, we remove the attribute called
    //    "aria-"+state.
    if(dojo.isFF && dojo.isFF < 3){
      elem.removeAttributeNS("http://www.w3.org/2005/07/aaa", state);
    }else{
      elem.removeAttribute("aria-"+state);
    }
  }
});

}

if(!dojo._hasResource["dijit._base"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit._base"] = true;
dojo.provide("dijit._base");












//  FIXME: Find a better way of solving this bug!
if(dojo.isSafari){
  //  Ugly-ass hack to solve bug #5626 for 1.1; basically force Safari to re-layout.
  //  Note that we can't reliably use dojo.addOnLoad here because this bug is basically
  //    a timing / race condition; so instead we use window.onload.
  dojo.connect(window, "load", function(){
    window.resizeBy(1,0);
    setTimeout(function(){ window.resizeBy(-1,0); }, 10);
  });
}

}

if(!dojo._hasResource["dijit._Widget"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit._Widget"] = true;
dojo.provide("dijit._Widget");

dojo.require( "dijit._base" );

dojo.declare("dijit._Widget", null, {
  //  summary:
  //    The foundation of dijit widgets.  
  //
  //  id: String
  //    a unique, opaque ID string that can be assigned by users or by the
  //    system. If the developer passes an ID which is known not to be
  //    unique, the specified ID is ignored and the system-generated ID is
  //    used instead.
  id: "",

  //  lang: String
  //    Rarely used.  Overrides the default Dojo locale used to render this widget,
  //    as defined by the [HTML LANG](http://www.w3.org/TR/html401/struct/dirlang.html#adef-lang) attribute.
  //    Value must be among the list of locales specified during by the Dojo bootstrap,
  //    formatted according to [RFC 3066](http://www.ietf.org/rfc/rfc3066.txt) (like en-us).
  lang: "",

  //  dir: String
  //    Unsupported by Dijit, but here for completeness.  Dijit only supports setting text direction on the
  //    entire document.
  //    Bi-directional support, as defined by the [HTML DIR](http://www.w3.org/TR/html401/struct/dirlang.html#adef-dir)
  //    attribute. Either left-to-right "ltr" or right-to-left "rtl".
  dir: "",

  // class: String
  //    HTML class attribute
  "class": "",

  // style: String
  //    HTML style attribute
  style: "",

  // title: String
  //    HTML title attribute
  title: "",

  // srcNodeRef: DomNode
  //    pointer to original dom node
  srcNodeRef: null,

  // domNode: DomNode
  //    this is our visible representation of the widget! Other DOM
  //    Nodes may by assigned to other properties, usually through the
  //    template system's dojoAttachPonit syntax, but the domNode
  //    property is the canonical "top level" node in widget UI.
  domNode: null,

  // attributeMap: Object
  //    A map of attributes and attachpoints -- typically standard HTML attributes -- to set
  //    on the widget's dom, at the "domNode" attach point, by default.
  //    Other node references can be specified as properties of 'this'
  attributeMap: {id:"", dir:"", lang:"", "class":"", style:"", title:""},  // TODO: add on* handlers?

  //////////// INITIALIZATION METHODS ///////////////////////////////////////
//TODOC: params and srcNodeRef need docs.  Is srcNodeRef optional?
//TODOC: summary needed for postscript
  postscript: function(/*Object?*/params, /*DomNode|String*/srcNodeRef){
    this.create(params, srcNodeRef);
  },

  create: function(/*Object?*/params, /*DomNode|String*/srcNodeRef){
    //  summary:
    //    Kick off the life-cycle of a widget
    //  description:
    //    To understand the process by which widgets are instantiated, it
    //    is critical to understand what other methods create calls and
    //    which of them you'll want to override. Of course, adventurous
    //    developers could override create entirely, but this should
    //    only be done as a last resort.
    //
    //    Below is a list of the methods that are called, in the order
    //    they are fired, along with notes about what they do and if/when
    //    you should over-ride them in your widget:
    //
    // * postMixInProperties:
    //  | * a stub function that you can over-ride to modify
    //    variables that may have been naively assigned by
    //    mixInProperties
    // * widget is added to manager object here
    // * buildRendering:
    //  | * Subclasses use this method to handle all UI initialization
    //    Sets this.domNode.  Templated widgets do this automatically
    //    and otherwise it just uses the source dom node.
    // * postCreate:
    //  | * a stub function that you can over-ride to modify take
    //    actions once the widget has been placed in the UI

    // store pointer to original dom tree
    this.srcNodeRef = dojo.byId(srcNodeRef);

    // For garbage collection.  An array of handles returned by Widget.connect()
    // Each handle returned from Widget.connect() is an array of handles from dojo.connect()
    this._connects=[];

    // _attaches: String[]
    //    names of all our dojoAttachPoint variables
    this._attaches=[];

    //mixin our passed parameters
    if(this.srcNodeRef && (typeof this.srcNodeRef.id == "string")){ this.id = this.srcNodeRef.id; }
    if(params){
      this.params = params;
      dojo.mixin(this,params);
    }
    this.postMixInProperties();

    // generate an id for the widget if one wasn't specified
    // (be sure to do this before buildRendering() because that function might
    // expect the id to be there.
    if(!this.id){
      this.id=dijit.getUniqueId(this.declaredClass.replace(/\./g,"_"));
    }
    dijit.registry.add(this);

    this.buildRendering();

    // Copy attributes listed in attributeMap into the [newly created] DOM for the widget.
    // The placement of these attributes is according to the property mapping in attributeMap.
    // Note special handling for 'style' and 'class' attributes which are lists and can
    // have elements from both old and new structures, and some attributes like "type"
    // cannot be processed this way as they are not mutable.
    if(this.domNode){
      for(var attr in this.attributeMap){
        var value = this[attr];
        if(typeof value != "object" && ((value !== "" && value !== false) || (params && params[attr]))){
          this.setAttribute(attr, value);
        }
      }
    }

    if(this.domNode){
      this.domNode.setAttribute("widgetId", this.id);
    }
    this.postCreate();

    // If srcNodeRef has been processed and removed from the DOM (e.g. TemplatedWidget) then delete it to allow GC.
    if(this.srcNodeRef && !this.srcNodeRef.parentNode){
      delete this.srcNodeRef;
    } 
  },

  postMixInProperties: function(){
    // summary
    //  Called after the parameters to the widget have been read-in,
    //  but before the widget template is instantiated.
    //  Especially useful to set properties that are referenced in the widget template.
  },

  buildRendering: function(){
    // summary:
    //    Construct the UI for this widget, setting this.domNode.
    //    Most widgets will mixin TemplatedWidget, which overrides this method.
    this.domNode = this.srcNodeRef || dojo.doc.createElement('div');
  },

  postCreate: function(){
    // summary:
    //    Called after a widget's dom has been setup
  },

  startup: function(){
    // summary:
    //    Called after a widget's children, and other widgets on the page, have been created.
    //    Provides an opportunity to manipulate any children before they are displayed.
    //    This is useful for composite widgets that need to control or layout sub-widgets.
    //    Many layout widgets can use this as a wiring phase.
    this._started = true;
  },

  //////////// DESTROY FUNCTIONS ////////////////////////////////

  destroyRecursive: function(/*Boolean*/ finalize){
    // summary:
    //    Destroy this widget and it's descendants. This is the generic
    //    "destructor" function that all widget users should call to
    //    cleanly discard with a widget. Once a widget is destroyed, it's
    //    removed from the manager object.
    // finalize: Boolean
    //    is this function being called part of global environment
    //    tear-down?

    this.destroyDescendants();
    this.destroy();
  },

  destroy: function(/*Boolean*/ finalize){
    // summary:
    //    Destroy this widget, but not its descendants
    // finalize: Boolean
    //    is this function being called part of global environment
    //    tear-down?

    this.uninitialize();
    dojo.forEach(this._connects, function(array){
      dojo.forEach(array, dojo.disconnect);
    });

    // destroy widgets created as part of template, etc.
    dojo.forEach(this._supportingWidgets || [], function(w){ w.destroy(); });
    
    this.destroyRendering(finalize);
    dijit.registry.remove(this.id);
  },

  destroyRendering: function(/*Boolean*/ finalize){
    // summary:
    //    Destroys the DOM nodes associated with this widget
    // finalize: Boolean
    //    is this function being called part of global environment
    //    tear-down?

    if(this.bgIframe){
      this.bgIframe.destroy();
      delete this.bgIframe;
    }

    if(this.domNode){
      dojo._destroyElement(this.domNode);
      delete this.domNode;
    }

    if(this.srcNodeRef){
      dojo._destroyElement(this.srcNodeRef);
      delete this.srcNodeRef;
    }
  },

  destroyDescendants: function(){
    // summary:
    //    Recursively destroy the children of this widget and their
    //    descendants.

    // TODO: should I destroy in the reverse order, to go bottom up?
    dojo.forEach(this.getDescendants(), function(widget){ widget.destroy(); });
  },

  uninitialize: function(){
    // summary:
    //    stub function. Override to implement custom widget tear-down
    //    behavior.
    return false;
  },

  ////////////////// MISCELLANEOUS METHODS ///////////////////

  onFocus: function(){
    // summary:
    //    stub function. Override or connect to this method to receive
    //    notifications for when the widget moves into focus.
  },

  onBlur: function(){
    // summary:
    //    stub function. Override or connect to this method to receive
    //    notifications for when the widget moves out of focus.
  },

  _onFocus: function(e){
    this.onFocus();
  },

  _onBlur: function(){
    this.onBlur();
  },

  setAttribute: function(/*String*/ attr, /*anything*/ value){
    // summary
    //    Set native HTML attributes reflected in the widget,
    //    such as readOnly, disabled, and maxLength in TextBox widgets.
    // description
    //    In general, a widget's "value" is controlled via setValue()/getValue(), 
    //    rather than this method.  The exception is for widgets where the
    //    end user can't adjust the value, such as Button and CheckBox;
    //    in the unusual case that you want to change the value attribute of
    //    those widgets, use setAttribute().
    var mapNode = this[this.attributeMap[attr]||'domNode'];
    this[attr] = value;
    switch(attr){
      case "class":
        dojo.addClass(mapNode, value);
        break;
      case "style":
        if(mapNode.style.cssText){
          mapNode.style.cssText += "; " + value;// FIXME: Opera
        }else{
          mapNode.style.cssText = value;
        }
        break;
      default:
        if(/^on[A-Z]/.test(attr)){ // eg. onSubmit needs to be onsubmit
          attr = attr.toLowerCase();
        }
        if(typeof value == "function"){ // functions execute in the context of the widget
          value = dojo.hitch(this, value);
        }
        dojo.attr(mapNode, attr, value);
    }
  },

  toString: function(){
    // summary:
    //    returns a string that represents the widget. When a widget is
    //    cast to a string, this method will be used to generate the
    //    output. Currently, it does not implement any sort of reversable
    //    serialization.
    return '[Widget ' + this.declaredClass + ', ' + (this.id || 'NO ID') + ']'; // String
  },

  getDescendants: function(){
    // summary:
    //  Returns all the widgets that contained by this, i.e., all widgets underneath this.containerNode.
    if(this.containerNode){
      var list= dojo.query('[widgetId]', this.containerNode);
      return list.map(dijit.byNode);    // Array
    }else{
      return [];
    }
  },

//TODOC
  nodesWithKeyClick: ["input", "button"],

  connect: function(
      /*Object|null*/ obj,
      /*String*/ event,
      /*String|Function*/ method){
    //  summary:
    //    Connects specified obj/event to specified method of this object
    //    and registers for disconnect() on widget destroy.
    //    Special event: "ondijitclick" triggers on a click or enter-down or space-up
    //    Similar to dojo.connect() but takes three arguments rather than four.
    var handles =[];
    if(event == "ondijitclick"){
      // add key based click activation for unsupported nodes.
      if(!this.nodesWithKeyClick[obj.nodeName]){
        handles.push(dojo.connect(obj, "onkeydown", this,
          function(e){
            if(e.keyCode == dojo.keys.ENTER){
              return (dojo.isString(method))?
                this[method](e) : method.call(this, e);
            }else if(e.keyCode == dojo.keys.SPACE){
              // stop space down as it causes IE to scroll
              // the browser window
              dojo.stopEvent(e);
            }
          }));
        handles.push(dojo.connect(obj, "onkeyup", this,
          function(e){
            if(e.keyCode == dojo.keys.SPACE){
              return dojo.isString(method) ?
                this[method](e) : method.call(this, e);
            }
          }));
      }
      event = "onclick";
    }
    handles.push(dojo.connect(obj, event, this, method));

    // return handles for FormElement and ComboBox
    this._connects.push(handles);
    return handles;
  },

  disconnect: function(/*Object*/ handles){
    // summary:
    //    Disconnects handle created by this.connect.
    //    Also removes handle from this widget's list of connects
    for(var i=0; i<this._connects.length; i++){
      if(this._connects[i]==handles){
        dojo.forEach(handles, dojo.disconnect);
        this._connects.splice(i, 1);
        return;
      }
    }
  },

  isLeftToRight: function(){
    // summary:
    //    Checks the DOM to for the text direction for bi-directional support
    // description:
    //    This method cannot be used during widget construction because the widget
    //    must first be connected to the DOM tree.  Parent nodes are searched for the
    //    'dir' attribute until one is found, otherwise left to right mode is assumed.
    //    See HTML spec, DIR attribute for more information.

    if(!("_ltr" in this)){
      this._ltr = dojo.getComputedStyle(this.domNode).direction != "rtl";
    }
    return this._ltr; //Boolean
  },

  isFocusable: function(){
    // summary:
    //    Return true if this widget can currently be focused
    //    and false if not
    return this.focus && (dojo.style(this.domNode, "display") != "none");
  }
});

}

if(!dojo._hasResource["dijit._Container"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit._Container"] = true;
dojo.provide("dijit._Container");

dojo.declare("dijit._Contained",
  null,
  {
    // summary
    //    Mixin for widgets that are children of a container widget
    //
    // example:
    // |  // make a basic custom widget that knows about it's parents
    // |  dojo.declare("my.customClass",[dijit._Widget,dijit._Contained],{});
    // 
    getParent: function(){
      // summary:
      //    Returns the parent widget of this widget, assuming the parent
      //    implements dijit._Container
      for(var p=this.domNode.parentNode; p; p=p.parentNode){
        var id = p.getAttribute && p.getAttribute("widgetId");
        if(id){
          var parent = dijit.byId(id);
          return parent.isContainer ? parent : null;
        }
      }
      return null;
    },

    _getSibling: function(which){
      var node = this.domNode;
      do{
        node = node[which+"Sibling"];
      }while(node && node.nodeType != 1);
      if(!node){ return null; } // null
      var id = node.getAttribute("widgetId");
      return dijit.byId(id);
    },

    getPreviousSibling: function(){
      // summary:
      //    Returns null if this is the first child of the parent,
      //    otherwise returns the next element sibling to the "left".

      return this._getSibling("previous"); // Mixed
    },

    getNextSibling: function(){
      // summary:
      //    Returns null if this is the last child of the parent,
      //    otherwise returns the next element sibling to the "right".

      return this._getSibling("next"); // Mixed
    }
  }
);

dojo.declare("dijit._Container",
  null,
  {
    // summary:
    //    Mixin for widgets that contain a list of children.
    // description:
    //    Use this mixin when the widget needs to know about and
    //    keep track of it's widget children. Widgets like SplitContainer
    //    and TabContainer.  

    isContainer: true,

    addChild: function(/*Widget*/ widget, /*int?*/ insertIndex){
      // summary:
      //    Process the given child widget, inserting it's dom node as
      //    a child of our dom node

      if(insertIndex === undefined){
        insertIndex = "last";
      }
      var refNode = this.containerNode || this.domNode;
      if(insertIndex && typeof insertIndex == "number"){
        var children = dojo.query("> [widgetid]", refNode);
        if(children && children.length >= insertIndex){
          refNode = children[insertIndex-1]; insertIndex = "after";
        }
      }
      dojo.place(widget.domNode, refNode, insertIndex);

      // If I've been started but the child widget hasn't been started,
      // start it now.  Make sure to do this after widget has been
      // inserted into the DOM tree, so it can see that it's being controlled by me,
      // so it doesn't try to size itself.
      if(this._started && !widget._started){
        widget.startup();
      }
    },

    removeChild: function(/*Widget*/ widget){
      // summary:
      //    Removes the passed widget instance from this widget but does
      //    not destroy it
      var node = widget.domNode;
      node.parentNode.removeChild(node);  // detach but don't destroy
    },

    _nextElement: function(node){
      do{
        node = node.nextSibling;
      }while(node && node.nodeType != 1);
      return node;
    },

    _firstElement: function(node){
      node = node.firstChild;
      if(node && node.nodeType != 1){
        node = this._nextElement(node);
      }
      return node;
    },

    getChildren: function(){
      // summary:
      //    Returns array of children widgets
      return dojo.query("> [widgetId]", this.containerNode || this.domNode).map(dijit.byNode); // Array
    },

    hasChildren: function(){
      // summary:
      //    Returns true if widget has children
      var cn = this.containerNode || this.domNode;
      return !!this._firstElement(cn); // Boolean
    },

    _getSiblingOfChild: function(/*Widget*/ child, /*int*/ dir){
      // summary:
      //    Get the next or previous widget sibling of child
      // dir:
      //    if 1, get the next sibling
      //    if -1, get the previous sibling
      var node = child.domNode;
      var which = (dir>0 ? "nextSibling" : "previousSibling");
      do{
        node = node[which];
      }while(node && (node.nodeType != 1 || !dijit.byNode(node)));
      return node ? dijit.byNode(node) : null;
    }
  }
);

dojo.declare("dijit._KeyNavContainer",
  [dijit._Container],
  {

    // summary: A _Container with keyboard navigation of its children.
    // decscription:
    //    To use this mixin, call connectKeyNavHandlers() in
    //    postCreate() and call startupKeyNavChildren() in startup().
    //    It provides normalized keyboard and focusing code for Container
    //    widgets.
/*=====
    // focusedChild: Widget
    //    The currently focused child widget, or null if there isn't one
    focusedChild: null,
=====*/

    _keyNavCodes: {},

    connectKeyNavHandlers: function(/*Array*/ prevKeyCodes, /*Array*/ nextKeyCodes){
      // summary:
      //    Call in postCreate() to attach the keyboard handlers
      //    to the container.
      // preKeyCodes: Array
      //    Key codes for navigating to the previous child.
      // nextKeyCodes: Array
      //    Key codes for navigating to the next child.

      var keyCodes = this._keyNavCodes = {};
      var prev = dojo.hitch(this, this.focusPrev);
      var next = dojo.hitch(this, this.focusNext);
      dojo.forEach(prevKeyCodes, function(code){ keyCodes[code] = prev });
      dojo.forEach(nextKeyCodes, function(code){ keyCodes[code] = next });
      this.connect(this.domNode, "onkeypress", "_onContainerKeypress");
      this.connect(this.domNode, "onfocus", "_onContainerFocus");
    },

    startupKeyNavChildren: function(){
      // summary:
      //    Call in startup() to set child tabindexes to -1
      dojo.forEach(this.getChildren(), dojo.hitch(this, "_startupChild"));
    },

    addChild: function(/*Widget*/ widget, /*int?*/ insertIndex){
      // summary: Add a child to our _Container
      dijit._KeyNavContainer.superclass.addChild.apply(this, arguments);
      this._startupChild(widget);
    },

    focus: function(){
      // summary: Default focus() implementation: focus the first child.
      this.focusFirstChild();
    },

    focusFirstChild: function(){
      // summary: Focus the first focusable child in the container.
      this.focusChild(this._getFirstFocusableChild());
    },

    focusNext: function(){
      // summary: Focus the next widget or focal node (for widgets
      //    with multiple focal nodes) within this container.
      if(this.focusedChild && this.focusedChild.hasNextFocalNode
          && this.focusedChild.hasNextFocalNode()){
        this.focusedChild.focusNext();
        return;
      }
      var child = this._getNextFocusableChild(this.focusedChild, 1);
      if(child.getFocalNodes){
        this.focusChild(child, child.getFocalNodes()[0]);
      }else{
        this.focusChild(child);
      }
    },

    focusPrev: function(){
      // summary: Focus the previous widget or focal node (for widgets
      //    with multiple focal nodes) within this container.
      if(this.focusedChild && this.focusedChild.hasPrevFocalNode
          && this.focusedChild.hasPrevFocalNode()){
        this.focusedChild.focusPrev();
        return;
      }
      var child = this._getNextFocusableChild(this.focusedChild, -1);
      if(child.getFocalNodes){
        var nodes = child.getFocalNodes();
        this.focusChild(child, nodes[nodes.length-1]);
      }else{
        this.focusChild(child);
      }
    },

    focusChild: function(/*Widget*/ widget, /*Node?*/ node){
      // summary: Focus widget. Optionally focus 'node' within widget.
      if(widget){
        if(this.focusedChild && widget !== this.focusedChild){
          this._onChildBlur(this.focusedChild);
        }
        this.focusedChild = widget;
        if(node && widget.focusFocalNode){
          widget.focusFocalNode(node);
        }else{
          widget.focus();
        }
      }
    },

    _startupChild: function(/*Widget*/ widget){
      // summary:
      //    Set tabindex="-1" on focusable widgets so that we
      //    can focus them programmatically and by clicking.
      //    Connect focus and blur handlers.
      if(widget.getFocalNodes){
        dojo.forEach(widget.getFocalNodes(), function(node){
          dojo.attr(node, "tabindex", -1);
          this._connectNode(node);
        }, this);
      }else{
        var node = widget.focusNode || widget.domNode;
        if(widget.isFocusable()){
          dojo.attr(node, "tabindex", -1);
        }
        this._connectNode(node);
      }
    },

    _connectNode: function(/*Element*/ node){
      this.connect(node, "onfocus", "_onNodeFocus");
      this.connect(node, "onblur", "_onNodeBlur");
    },

    _onContainerFocus: function(evt){
      // focus bubbles on Firefox,
      // so just make sure that focus has really gone to the container
      if(evt.target === this.domNode){
        this.focusFirstChild();
      }
    },

    _onContainerKeypress: function(evt){
      if(evt.ctrlKey || evt.altKey){ return; }
      var func = this._keyNavCodes[evt.keyCode];
      if(func){
        func();
        dojo.stopEvent(evt);
      }
    },

    _onNodeFocus: function(evt){
      // while focus is on a child,
      // take the container out of the tab order so that
      // we can shift-tab to the element before the container
      dojo.attr(this.domNode, "tabindex", -1);
      // record the child that has been focused
      var widget = dijit.getEnclosingWidget(evt.target);
      if(widget && widget.isFocusable()){
        this.focusedChild = widget;
      }
      dojo.stopEvent(evt);
    },

    _onNodeBlur: function(evt){
      // when focus leaves a child,
      // reinstate the container's tabindex
      if(this.tabIndex){
        dojo.attr(this.domNode, "tabindex", this.tabIndex);
      }
      dojo.stopEvent(evt);
    },

    _onChildBlur: function(/*Widget*/ widget){
      // summary:
      //    Called when focus leaves a child widget to go
      //    to a sibling widget.
    },

    _getFirstFocusableChild: function(){
      return this._getNextFocusableChild(null, 1);
    },

    _getNextFocusableChild: function(child, dir){
      if(child){
        child = this._getSiblingOfChild(child, dir);
      }
      var children = this.getChildren();
      for(var i=0; i < children.length; i++){
        if(!child){
          child = children[(dir>0) ? 0 : (children.length-1)];
        }
        if(child.isFocusable()){
          return child;
        }
        child = this._getSiblingOfChild(child, dir);
      }
      // no focusable child found
      return null;
    }
  }
);

}

if(!dojo._hasResource["dijit.layout._LayoutWidget"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit.layout._LayoutWidget"] = true;
dojo.provide("dijit.layout._LayoutWidget");




dojo.declare("dijit.layout._LayoutWidget",
  [dijit._Widget, dijit._Container, dijit._Contained],
  {
    // summary
    //    Mixin for widgets that contain a list of children like SplitContainer.
    //    Widgets which mixin this code must define layout() to lay out the children

    isLayoutContainer: true,

    postCreate: function(){
      dojo.addClass(this.domNode, "dijitContainer");
    },

    startup: function(){
      // summary:
      //    Called after all the widgets have been instantiated and their
      //    dom nodes have been inserted somewhere under dojo.doc.body.
      //
      //    Widgets should override this method to do any initialization
      //    dependent on other widgets existing, and then call
      //    this superclass method to finish things off.
      //
      //    startup() in subclasses shouldn't do anything
      //    size related because the size of the widget hasn't been set yet.

      if(this._started){ return; }

      dojo.forEach(this.getChildren(), function(child){ child.startup(); });

      // If I am a top level widget
      if(!this.getParent || !this.getParent()){
        // Do recursive sizing and layout of all my descendants
        // (passing in no argument to resize means that it has to glean the size itself)
        this.resize();

        // since my parent isn't a layout container, and my style is width=height=100% (or something similar),
        // then I need to watch when the window resizes, and size myself accordingly
        // (passing in no argument to resize means that it has to glean the size itself)
        this.connect(window, 'onresize', function(){this.resize();});
      }
      
      this.inherited(arguments);
    },

    resize: function(args){
      // summary:
      //    Explicitly set this widget's size (in pixels),
      //    and then call layout() to resize contents (and maybe adjust child widgets)
      //  
      // args: Object?
      //    {w: int, h: int, l: int, t: int}

      var node = this.domNode;

      // set margin box size, unless it wasn't specified, in which case use current size
      if(args){
        dojo.marginBox(node, args);

        // set offset of the node
        if(args.t){ node.style.top = args.t + "px"; }
        if(args.l){ node.style.left = args.l + "px"; }
      }
      // If either height or width wasn't specified by the user, then query node for it.
      // But note that setting the margin box and then immediately querying dimensions may return
      // inaccurate results, so try not to depend on it.
      var mb = dojo.mixin(dojo.marginBox(node), args||{});

//      console.log(this, ": setting size to ", mb);

      // Save the size of my content box.
      this._contentBox = dijit.layout.marginBox2contentBox(node, mb);

      // Callback for widget to adjust size of it's children
      this.layout();
    },

    layout: function(){
      //  summary
      //    Widgets override this method to size & position their contents/children.
      //    When this is called this._contentBox is guaranteed to be set (see resize()).
      //
      //    This is called after startup(), and also when the widget's size has been
      //    changed.
    }
  }
);

dijit.layout.marginBox2contentBox = function(/*DomNode*/ node, /*Object*/ mb){
  // summary:
  //    Given the margin-box size of a node, return it's content box size.
  //    Functions like dojo.contentBox() but is more reliable since it doesn't have
  //    to wait for the browser to compute sizes.
  var cs = dojo.getComputedStyle(node);
  var me=dojo._getMarginExtents(node, cs);
  var pb=dojo._getPadBorderExtents(node, cs);
  return {
    l: dojo._toPixelValue(node, cs.paddingLeft),
    t: dojo._toPixelValue(node, cs.paddingTop),
    w: mb.w - (me.w + pb.w),
    h: mb.h - (me.h + pb.h)
  };
};

(function(){
  var capitalize = function(word){
    return word.substring(0,1).toUpperCase() + word.substring(1);
  };

  var size = function(widget, dim){
    // size the child
    widget.resize ? widget.resize(dim) : dojo.marginBox(widget.domNode, dim);

    // record child's size, but favor our own numbers when we have them.
    // the browser lies sometimes
    dojo.mixin(widget, dojo.marginBox(widget.domNode));
    dojo.mixin(widget, dim);
  };

  dijit.layout.layoutChildren = function(/*DomNode*/ container, /*Object*/ dim, /*Object[]*/ children){
    /**
     * summary
     *    Layout a bunch of child dom nodes within a parent dom node
     * container:
     *    parent node
     * dim:
     *    {l, t, w, h} object specifying dimensions of container into which to place children
     * children:
     *    an array like [ {domNode: foo, layoutAlign: "bottom" }, {domNode: bar, layoutAlign: "client"} ]
     */

    // copy dim because we are going to modify it
    dim = dojo.mixin({}, dim);

    dojo.addClass(container, "dijitLayoutContainer");

    // Move "client" elements to the end of the array for layout.  a11y dictates that the author
    // needs to be able to put them in the document in tab-order, but this algorithm requires that
    // client be last.
    children = dojo.filter(children, function(item){ return item.layoutAlign != "client"; })
      .concat(dojo.filter(children, function(item){ return item.layoutAlign == "client"; }));

    // set positions/sizes
    dojo.forEach(children, function(child){
      var elm = child.domNode,
        pos = child.layoutAlign;

      // set elem to upper left corner of unused space; may move it later
      var elmStyle = elm.style;
      elmStyle.left = dim.l+"px";
      elmStyle.top = dim.t+"px";
      elmStyle.bottom = elmStyle.right = "auto";

      dojo.addClass(elm, "dijitAlign" + capitalize(pos));

      // set size && adjust record of remaining space.
      // note that setting the width of a <div> may affect it's height.
      if(pos=="top" || pos=="bottom"){
        size(child, { w: dim.w });
        dim.h -= child.h;
        if(pos=="top"){
          dim.t += child.h;
        }else{
          elmStyle.top = dim.t + dim.h + "px";
        }
      }else if(pos=="left" || pos=="right"){
        size(child, { h: dim.h });
        dim.w -= child.w;
        if(pos=="left"){
          dim.l += child.w;
        }else{
          elmStyle.left = dim.l + dim.w + "px";
        }
      }else if(pos=="client"){
        size(child, dim);
      }
    });
  };

})();

}

if(!dojo._hasResource["dojo.date.stamp"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojo.date.stamp"] = true;
dojo.provide("dojo.date.stamp");

// Methods to convert dates to or from a wire (string) format using well-known conventions

dojo.date.stamp.fromISOString = function(/*String*/formattedString, /*Number?*/defaultTime){
  //  summary:
  //    Returns a Date object given a string formatted according to a subset of the ISO-8601 standard.
  //
  //  description:
  //    Accepts a string formatted according to a profile of ISO8601 as defined by
  //    [RFC3339](http://www.ietf.org/rfc/rfc3339.txt), except that partial input is allowed.
  //    Can also process dates as specified [by the W3C](http://www.w3.org/TR/NOTE-datetime)
  //    The following combinations are valid:
  //
  //      * dates only
  //      | * yyyy
  //      | * yyyy-MM
  //      | * yyyy-MM-dd
  //      * times only, with an optional time zone appended
  //      | * THH:mm
  //      | * THH:mm:ss
  //      | * THH:mm:ss.SSS
  //      * and "datetimes" which could be any combination of the above
  //
  //    timezones may be specified as Z (for UTC) or +/- followed by a time expression HH:mm
  //    Assumes the local time zone if not specified.  Does not validate.  Improperly formatted
  //    input may return null.  Arguments which are out of bounds will be handled
  //    by the Date constructor (e.g. January 32nd typically gets resolved to February 1st)
  //    Only years between 100 and 9999 are supported.
  //
    //  formattedString:
  //    A string such as 2005-06-30T08:05:00-07:00 or 2005-06-30 or T08:05:00
  //
  //  defaultTime:
  //    Used for defaults for fields omitted in the formattedString.
  //    Uses 1970-01-01T00:00:00.0Z by default.

  if(!dojo.date.stamp._isoRegExp){
    dojo.date.stamp._isoRegExp =
//TODO: could be more restrictive and check for 00-59, etc.
      /^(?:(\d{4})(?:-(\d{2})(?:-(\d{2}))?)?)?(?:T(\d{2}):(\d{2})(?::(\d{2})(.\d+)?)?((?:[+-](\d{2}):(\d{2}))|Z)?)?$/;
  }

  var match = dojo.date.stamp._isoRegExp.exec(formattedString);
  var result = null;

  if(match){
    match.shift();
    if(match[1]){match[1]--;} // Javascript Date months are 0-based
    if(match[6]){match[6] *= 1000;} // Javascript Date expects fractional seconds as milliseconds

    if(defaultTime){
      // mix in defaultTime.  Relatively expensive, so use || operators for the fast path of defaultTime === 0
      defaultTime = new Date(defaultTime);
      dojo.map(["FullYear", "Month", "Date", "Hours", "Minutes", "Seconds", "Milliseconds"], function(prop){
        return defaultTime["get" + prop]();
      }).forEach(function(value, index){
        if(match[index] === undefined){
          match[index] = value;
        }
      });
    }
    result = new Date(match[0]||1970, match[1]||0, match[2]||1, match[3]||0, match[4]||0, match[5]||0, match[6]||0);
//    result.setFullYear(match[0]||1970); // for year < 100

    var offset = 0;
    var zoneSign = match[7] && match[7].charAt(0);
    if(zoneSign != 'Z'){
      offset = ((match[8] || 0) * 60) + (Number(match[9]) || 0);
      if(zoneSign != '-'){ offset *= -1; }
    }
    if(zoneSign){
      offset -= result.getTimezoneOffset();
    }
    if(offset){
      result.setTime(result.getTime() + offset * 60000);
    }
  }

  return result; // Date or null
}

/*=====
  dojo.date.stamp.__Options = function(){
    //  selector: String
    //    "date" or "time" for partial formatting of the Date object.
    //    Both date and time will be formatted by default.
    //  zulu: Boolean
    //    if true, UTC/GMT is used for a timezone
    //  milliseconds: Boolean
    //    if true, output milliseconds
    this.selector = selector;
    this.zulu = zulu;
    this.milliseconds = milliseconds;
  }
=====*/

dojo.date.stamp.toISOString = function(/*Date*/dateObject, /*dojo.date.stamp.__Options?*/options){
  //  summary:
  //    Format a Date object as a string according a subset of the ISO-8601 standard
  //
  //  description:
  //    When options.selector is omitted, output follows [RFC3339](http://www.ietf.org/rfc/rfc3339.txt)
  //    The local time zone is included as an offset from GMT, except when selector=='time' (time without a date)
  //    Does not check bounds.  Only years between 100 and 9999 are supported.
  //
  //  dateObject:
  //    A Date object

  var _ = function(n){ return (n < 10) ? "0" + n : n; };
  options = options || {};
  var formattedDate = [];
  var getter = options.zulu ? "getUTC" : "get";
  var date = "";
  if(options.selector != "time"){
    var year = dateObject[getter+"FullYear"]();
    date = ["0000".substr((year+"").length)+year, _(dateObject[getter+"Month"]()+1), _(dateObject[getter+"Date"]())].join('-');
  }
  formattedDate.push(date);
  if(options.selector != "date"){
    var time = [_(dateObject[getter+"Hours"]()), _(dateObject[getter+"Minutes"]()), _(dateObject[getter+"Seconds"]())].join(':');
    var millis = dateObject[getter+"Milliseconds"]();
    if(options.milliseconds){
      time += "."+ (millis < 100 ? "0" : "") + _(millis);
    }
    if(options.zulu){
      time += "Z";
    }else if(options.selector != "time"){
      var timezoneOffset = dateObject.getTimezoneOffset();
      var absOffset = Math.abs(timezoneOffset);
      time += (timezoneOffset > 0 ? "-" : "+") + 
        _(Math.floor(absOffset/60)) + ":" + _(absOffset%60);
    }
    formattedDate.push(time);
  }
  return formattedDate.join('T'); // String
}

}

if(!dojo._hasResource["dojo.parser"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojo.parser"] = true;
dojo.provide("dojo.parser");


dojo.parser = new function(){
  // summary: The Dom/Widget parsing package

  var d = dojo;
  var dtName = d._scopeName + "Type";
  var qry = "[" + dtName + "]";

  function val2type(/*Object*/ value){
    // summary:
    //    Returns name of type of given value.

    if(d.isString(value)){ return "string"; }
    if(typeof value == "number"){ return "number"; }
    if(typeof value == "boolean"){ return "boolean"; }
    if(d.isFunction(value)){ return "function"; }
    if(d.isArray(value)){ return "array"; } // typeof [] == "object"
    if(value instanceof Date) { return "date"; } // assume timestamp
    if(value instanceof d._Url){ return "url"; }
    return "object";
  }

  function str2obj(/*String*/ value, /*String*/ type){
    // summary:
    //    Convert given string value to given type
    switch(type){
      case "string":
        return value;
      case "number":
        return value.length ? Number(value) : NaN;
      case "boolean":
        // for checked/disabled value might be "" or "checked".  interpret as true.
        return typeof value == "boolean" ? value : !(value.toLowerCase()=="false");
      case "function":
        if(d.isFunction(value)){
          // IE gives us a function, even when we say something like onClick="foo"
          // (in which case it gives us an invalid function "function(){ foo }"). 
          //  Therefore, convert to string
          value=value.toString();
          value=d.trim(value.substring(value.indexOf('{')+1, value.length-1));
        }
        try{
          if(value.search(/[^\w\.]+/i) != -1){
            // TODO: "this" here won't work
            value = d.parser._nameAnonFunc(new Function(value), this);
          }
          return d.getObject(value, false);
        }catch(e){ return new Function(); }
      case "array":
        return value.split(/\s*,\s*/);
      case "date":
        switch(value){
          case "": return new Date(""); // the NaN of dates
          case "now": return new Date();  // current date
          default: return d.date.stamp.fromISOString(value);
        }
      case "url":
        return d.baseUrl + value;
      default:
        return d.fromJson(value);
    }
  }

  var instanceClasses = {
    // map from fully qualified name (like "dijit.Button") to structure like
    // { cls: dijit.Button, params: {label: "string", disabled: "boolean"} }
  };
  
  function getClassInfo(/*String*/ className){
    // className:
    //    fully qualified name (like "dijit.Button")
    // returns:
    //    structure like
    //      { 
    //        cls: dijit.Button, 
    //        params: { label: "string", disabled: "boolean"}
    //      }

    if(!instanceClasses[className]){
      // get pointer to widget class
      var cls = d.getObject(className);
      if(!d.isFunction(cls)){
        throw new Error("Could not load class '" + className +
          "'. Did you spell the name correctly and use a full path, like 'dijit.form.Button'?");
      }
      var proto = cls.prototype;
  
      // get table of parameter names & types
      var params={};
      for(var name in proto){
        if(name.charAt(0)=="_"){ continue; }  // skip internal properties
        var defVal = proto[name];
        params[name]=val2type(defVal);
      }

      instanceClasses[className] = { cls: cls, params: params };
    }
    return instanceClasses[className];
  }

  this._functionFromScript = function(script){
    var preamble = "";
    var suffix = "";
    var argsStr = script.getAttribute("args");
    if(argsStr){
      d.forEach(argsStr.split(/\s*,\s*/), function(part, idx){
        preamble += "var "+part+" = arguments["+idx+"]; ";
      });
    }
    var withStr = script.getAttribute("with");
    if(withStr && withStr.length){
      d.forEach(withStr.split(/\s*,\s*/), function(part){
        preamble += "with("+part+"){";
        suffix += "}";
      });
    }
    return new Function(preamble+script.innerHTML+suffix);
  }

  this.instantiate = function(/* Array */nodes){
    // summary:
    //    Takes array of nodes, and turns them into class instances and
    //    potentially calls a layout method to allow them to connect with
    //    any children    
    var thelist = [];
    d.forEach(nodes, function(node){
      if(!node){ return; }
      var type = node.getAttribute(dtName);
      if((!type)||(!type.length)){ return; }
      var clsInfo = getClassInfo(type);
      var clazz = clsInfo.cls;
      var ps = clazz._noScript||clazz.prototype._noScript;

      // read parameters (ie, attributes).
      // clsInfo.params lists expected params like {"checked": "boolean", "n": "number"}
      var params = {};
      var attributes = node.attributes;
      for(var name in clsInfo.params){
        var item = attributes.getNamedItem(name);
        if(!item || (!item.specified && (!dojo.isIE || name.toLowerCase()!="value"))){ continue; }
        var value = item.value;
        // Deal with IE quirks for 'class' and 'style'
        switch(name){
        case "class":
          value = node.className;
          break;
        case "style":
          value = node.style && node.style.cssText; // FIXME: Opera?
        }
        var _type = clsInfo.params[name];
        params[name] = str2obj(value, _type);
      }

      // Process <script type="dojo/*"> script tags
      // <script type="dojo/method" event="foo"> tags are added to params, and passed to
      // the widget on instantiation.
      // <script type="dojo/method"> tags (with no event) are executed after instantiation
      // <script type="dojo/connect" event="foo"> tags are dojo.connected after instantiation
      // note: dojo/* script tags cannot exist in self closing widgets, like <input />
      if(!ps){
        var connects = [],  // functions to connect after instantiation
          calls = [];   // functions to call after instantiation

        d.query("> script[type^='dojo/']", node).orphan().forEach(function(script){
          var event = script.getAttribute("event"),
            type = script.getAttribute("type"),
            nf = d.parser._functionFromScript(script);
          if(event){
            if(type == "dojo/connect"){
              connects.push({event: event, func: nf});
            }else{
              params[event] = nf;
            }
          }else{
            calls.push(nf);
          }
        });
      }

      var markupFactory = clazz["markupFactory"];
      if(!markupFactory && clazz["prototype"]){
        markupFactory = clazz.prototype["markupFactory"];
      }
      // create the instance
      var instance = markupFactory ? markupFactory(params, node, clazz) : new clazz(params, node);
      thelist.push(instance);

      // map it to the JS namespace if that makes sense
      var jsname = node.getAttribute("jsId");
      if(jsname){
        d.setObject(jsname, instance);
      }

      // process connections and startup functions
      if(!ps){
        d.forEach(connects, function(connect){
          d.connect(instance, connect.event, null, connect.func);
        });
        d.forEach(calls, function(func){
          func.call(instance);
        });
      }
    });

    // Call startup on each top level instance if it makes sense (as for
    // widgets).  Parent widgets will recursively call startup on their
    // (non-top level) children
    d.forEach(thelist, function(instance){
      if( instance  && 
        instance.startup &&
        !instance._started && 
        (!instance.getParent || !instance.getParent())
      ){
        instance.startup();
      }
    });
    return thelist;
  };

  this.parse = function(/*DomNode?*/ rootNode){
    // summary:
    //    Search specified node (or root node) recursively for class instances,
    //    and instantiate them Searches for
    //    dojoType="qualified.class.name"
    var list = d.query(qry, rootNode);
    // go build the object instances
    var instances = this.instantiate(list);
    return instances;
  };
}();

//Register the parser callback. It should be the first callback
//after the a11y test.

(function(){
  var parseRunner = function(){ 
    if(dojo.config["parseOnLoad"] == true){
      dojo.parser.parse(); 
    }
  };

  // FIXME: need to clobber cross-dependency!!
  if(dojo.exists("dijit.wai.onload") && (dijit.wai.onload === dojo._loaders[0])){
    dojo._loaders.splice(1, 0, parseRunner);
  }else{
    dojo._loaders.unshift(parseRunner);
  }
})();

//TODO: ported from 0.4.x Dojo.  Can we reduce this?
dojo.parser._anonCtr = 0;
dojo.parser._anon = {}; // why is this property required?
dojo.parser._nameAnonFunc = function(/*Function*/anonFuncPtr, /*Object*/thisObj){
  // summary:
  //    Creates a reference to anonFuncPtr in thisObj with a completely
  //    unique name. The new name is returned as a String. 
  var jpn = "$joinpoint";
  var nso = (thisObj|| dojo.parser._anon);
  if(dojo.isIE){
    var cn = anonFuncPtr["__dojoNameCache"];
    if(cn && nso[cn] === anonFuncPtr){
      return anonFuncPtr["__dojoNameCache"];
    }
  }
  var ret = "__"+dojo.parser._anonCtr++;
  while(typeof nso[ret] != "undefined"){
    ret = "__"+dojo.parser._anonCtr++;
  }
  nso[ret] = anonFuncPtr;
  return ret; // String
}

}

if(!dojo._hasResource["dojo.string"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojo.string"] = true;
dojo.provide("dojo.string");

/*=====
dojo.string = { 
  // summary: String utilities for Dojo
};
=====*/

dojo.string.pad = function(/*String*/text, /*int*/size, /*String?*/ch, /*boolean?*/end){
  // summary:
  //    Pad a string to guarantee that it is at least `size` length by
  //    filling with the character `ch` at either the start or end of the
  //    string. Pads at the start, by default.
  // text: the string to pad
  // size: length to provide padding
  // ch: character to pad, defaults to '0'
  // end: adds padding at the end if true, otherwise pads at start

  var out = String(text);
  if(!ch){
    ch = '0';
  }
  while(out.length < size){
    if(end){
      out += ch;
    }else{
      out = ch + out;
    }
  }
  return out; // String
};

dojo.string.substitute = function(  /*String*/template, 
                  /*Object|Array*/map, 
                  /*Function?*/transform, 
                  /*Object?*/thisObject){
  // summary:
  //    Performs parameterized substitutions on a string. Throws an
  //    exception if any parameter is unmatched.
  // description:
  //    For example,
  //    | dojo.string.substitute("File '${0}' is not found in directory '${1}'.",["foo.html","/temp"]);
  //    | dojo.string.substitute("File '${name}' is not found in directory '${info.dir}'.",
  //    |   {name: "foo.html", info: {dir: "/temp"}});
  //    both return
  //    | "File 'foo.html' is not found in directory '/temp'."
  // template: 
  //    a string with expressions in the form `${key}` to be replaced or
  //    `${key:format}` which specifies a format function.
  // map: hash to search for substitutions
  // transform: 
  //    a function to process all parameters before substitution takes
  //    place, e.g. dojo.string.encodeXML
  // thisObject: 
  //    where to look for optional format function; default to the global
  //    namespace

  return template.replace(/\$\{([^\s\:\}]+)(?:\:([^\s\:\}]+))?\}/g, function(match, key, format){
    var value = dojo.getObject(key,false,map);
    if(format){ value = dojo.getObject(format,false,thisObject)(value);}
    if(transform){ value = transform(value, key); }
    return value.toString();
  }); // string
};

dojo.string.trim = function(/*String*/ str){
  // summary: trims whitespaces from both sides of the string
  // description:
  //  This version of trim() was taken from [Steven Levithan's blog](http://blog.stevenlevithan.com/archives/faster-trim-javascript).
  //  The short yet performant version of this function is 
  //  dojo.trim(), which is part of Dojo base.
  str = str.replace(/^\s+/, '');
  for(var i = str.length - 1; i > 0; i--){
    if(/\S/.test(str.charAt(i))){
      str = str.substring(0, i + 1);
      break;
    }
  }
  return str; // String
};

}

if(!dojo._hasResource["dojo.i18n"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojo.i18n"] = true;
dojo.provide("dojo.i18n");

/*=====
dojo.i18n = {
  // summary: Utility classes to enable loading of resources for internationalization (i18n)
};
=====*/

dojo.i18n.getLocalization = function(/*String*/packageName, /*String*/bundleName, /*String?*/locale){
  //  summary:
  //    Returns an Object containing the localization for a given resource
  //    bundle in a package, matching the specified locale.
  //  description:
  //    Returns a hash containing name/value pairs in its prototypesuch
  //    that values can be easily overridden.  Throws an exception if the
  //    bundle is not found.  Bundle must have already been loaded by
  //    `dojo.requireLocalization()` or by a build optimization step.  NOTE:
  //    try not to call this method as part of an object property
  //    definition (`var foo = { bar: dojo.i18n.getLocalization() }`).  In
  //    some loading situations, the bundle may not be available in time
  //    for the object definition.  Instead, call this method inside a
  //    function that is run after all modules load or the page loads (like
  //    in `dojo.addOnLoad()`), or in a widget lifecycle method.
  //  packageName:
  //    package which is associated with this resource
  //  bundleName:
  //    the base filename of the resource bundle (without the ".js" suffix)
  //  locale:
  //    the variant to load (optional).  By default, the locale defined by
  //    the host environment: dojo.locale

  locale = dojo.i18n.normalizeLocale(locale);

  // look for nearest locale match
  var elements = locale.split('-');
  var module = [packageName,"nls",bundleName].join('.');
  var bundle = dojo._loadedModules[module];
  if(bundle){
    var localization;
    for(var i = elements.length; i > 0; i--){
      var loc = elements.slice(0, i).join('_');
      if(bundle[loc]){
        localization = bundle[loc];
        break;
      }
    }
    if(!localization){
      localization = bundle.ROOT;
    }

    // make a singleton prototype so that the caller won't accidentally change the values globally
    if(localization){
      var clazz = function(){};
      clazz.prototype = localization;
      return new clazz(); // Object
    }
  }

  throw new Error("Bundle not found: " + bundleName + " in " + packageName+" , locale=" + locale);
};

dojo.i18n.normalizeLocale = function(/*String?*/locale){
  //  summary:
  //    Returns canonical form of locale, as used by Dojo.
  //
  //  description:
  //    All variants are case-insensitive and are separated by '-' as specified in [RFC 3066](http://www.ietf.org/rfc/rfc3066.txt).
  //    If no locale is specified, the dojo.locale is returned.  dojo.locale is defined by
  //    the user agent's locale unless overridden by djConfig.

  var result = locale ? locale.toLowerCase() : dojo.locale;
  if(result == "root"){
    result = "ROOT";
  }
  return result; // String
};

dojo.i18n._requireLocalization = function(/*String*/moduleName, /*String*/bundleName, /*String?*/locale, /*String?*/availableFlatLocales){
  //  summary:
  //    See dojo.requireLocalization()
  //  description:
  //    Called by the bootstrap, but factored out so that it is only
  //    included in the build when needed.

  var targetLocale = dojo.i18n.normalizeLocale(locale);
  var bundlePackage = [moduleName, "nls", bundleName].join(".");
  // NOTE: 
  //    When loading these resources, the packaging does not match what is
  //    on disk.  This is an implementation detail, as this is just a
  //    private data structure to hold the loaded resources.  e.g.
  //    `tests/hello/nls/en-us/salutations.js` is loaded as the object
  //    `tests.hello.nls.salutations.en_us={...}` The structure on disk is
  //    intended to be most convenient for developers and translators, but
  //    in memory it is more logical and efficient to store in a different
  //    order.  Locales cannot use dashes, since the resulting path will
  //    not evaluate as valid JS, so we translate them to underscores.
  
  //Find the best-match locale to load if we have available flat locales.
  var bestLocale = "";
  if(availableFlatLocales){
    var flatLocales = availableFlatLocales.split(",");
    for(var i = 0; i < flatLocales.length; i++){
      //Locale must match from start of string.
      if(targetLocale.indexOf(flatLocales[i]) == 0){
        if(flatLocales[i].length > bestLocale.length){
          bestLocale = flatLocales[i];
        }
      }
    }
    if(!bestLocale){
      bestLocale = "ROOT";
    }   
  }

  //See if the desired locale is already loaded.
  var tempLocale = availableFlatLocales ? bestLocale : targetLocale;
  var bundle = dojo._loadedModules[bundlePackage];
  var localizedBundle = null;
  if(bundle){
    if(dojo.config.localizationComplete && bundle._built){return;}
    var jsLoc = tempLocale.replace(/-/g, '_');
    var translationPackage = bundlePackage+"."+jsLoc;
    localizedBundle = dojo._loadedModules[translationPackage];
  }

  if(!localizedBundle){
    bundle = dojo["provide"](bundlePackage);
    var syms = dojo._getModuleSymbols(moduleName);
    var modpath = syms.concat("nls").join("/");
    var parent;

    dojo.i18n._searchLocalePath(tempLocale, availableFlatLocales, function(loc){
      var jsLoc = loc.replace(/-/g, '_');
      var translationPackage = bundlePackage + "." + jsLoc;
      var loaded = false;
      if(!dojo._loadedModules[translationPackage]){
        // Mark loaded whether it's found or not, so that further load attempts will not be made
        dojo["provide"](translationPackage);
        var module = [modpath];
        if(loc != "ROOT"){module.push(loc);}
        module.push(bundleName);
        var filespec = module.join("/") + '.js';
        loaded = dojo._loadPath(filespec, null, function(hash){
          // Use singleton with prototype to point to parent bundle, then mix-in result from loadPath
          var clazz = function(){};
          clazz.prototype = parent;
          bundle[jsLoc] = new clazz();
          for(var j in hash){ bundle[jsLoc][j] = hash[j]; }
        });
      }else{
        loaded = true;
      }
      if(loaded && bundle[jsLoc]){
        parent = bundle[jsLoc];
      }else{
        bundle[jsLoc] = parent;
      }
      
      if(availableFlatLocales){
        //Stop the locale path searching if we know the availableFlatLocales, since
        //the first call to this function will load the only bundle that is needed.
        return true;
      }
    });
  }

  //Save the best locale bundle as the target locale bundle when we know the
  //the available bundles.
  if(availableFlatLocales && targetLocale != bestLocale){
    bundle[targetLocale.replace(/-/g, '_')] = bundle[bestLocale.replace(/-/g, '_')];
  }
};

(function(){
  // If other locales are used, dojo.requireLocalization should load them as
  // well, by default. 
  // 
  // Override dojo.requireLocalization to do load the default bundle, then
  // iterate through the extraLocale list and load those translations as
  // well, unless a particular locale was requested.

  var extra = dojo.config.extraLocale;
  if(extra){
    if(!extra instanceof Array){
      extra = [extra];
    }

    var req = dojo.i18n._requireLocalization;
    dojo.i18n._requireLocalization = function(m, b, locale, availableFlatLocales){
      req(m,b,locale, availableFlatLocales);
      if(locale){return;}
      for(var i=0; i<extra.length; i++){
        req(m,b,extra[i], availableFlatLocales);
      }
    };
  }
})();

dojo.i18n._searchLocalePath = function(/*String*/locale, /*Boolean*/down, /*Function*/searchFunc){
  //  summary:
  //    A helper method to assist in searching for locale-based resources.
  //    Will iterate through the variants of a particular locale, either up
  //    or down, executing a callback function.  For example, "en-us" and
  //    true will try "en-us" followed by "en" and finally "ROOT".

  locale = dojo.i18n.normalizeLocale(locale);

  var elements = locale.split('-');
  var searchlist = [];
  for(var i = elements.length; i > 0; i--){
    searchlist.push(elements.slice(0, i).join('-'));
  }
  searchlist.push(false);
  if(down){searchlist.reverse();}

  for(var j = searchlist.length - 1; j >= 0; j--){
    var loc = searchlist[j] || "ROOT";
    var stop = searchFunc(loc);
    if(stop){ break; }
  }
};

dojo.i18n._preloadLocalizations = function(/*String*/bundlePrefix, /*Array*/localesGenerated){
  //  summary:
  //    Load built, flattened resource bundles, if available for all
  //    locales used in the page. Only called by built layer files.

  function preload(locale){
    locale = dojo.i18n.normalizeLocale(locale);
    dojo.i18n._searchLocalePath(locale, true, function(loc){
      for(var i=0; i<localesGenerated.length;i++){
        if(localesGenerated[i] == loc){
          dojo["require"](bundlePrefix+"_"+loc);
          return true; // Boolean
        }
      }
      return false; // Boolean
    });
  }
  preload();
  var extra = dojo.config.extraLocale||[];
  for(var i=0; i<extra.length; i++){
    preload(extra[i]);
  }
};

}

if(!dojo._hasResource["dijit.layout.ContentPane"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit.layout.ContentPane"] = true;
dojo.provide("dijit.layout.ContentPane");








dojo.declare(
  "dijit.layout.ContentPane",
  dijit._Widget,
{
  // summary:
  //    A widget that acts as a Container for other widgets, and includes a ajax interface
  // description:
  //    A widget that can be used as a standalone widget
  //    or as a baseclass for other widgets
  //    Handles replacement of document fragment using either external uri or javascript
  //    generated markup or DOM content, instantiating widgets within that content.
  //    Don't confuse it with an iframe, it only needs/wants document fragments.
  //    It's useful as a child of LayoutContainer, SplitContainer, or TabContainer.
  //    But note that those classes can contain any widget as a child.
  // example:
  //    Some quick samples:
  //    To change the innerHTML use .setContent('<b>new content</b>')
  //
  //    Or you can send it a NodeList, .setContent(dojo.query('div [class=selected]', userSelection))
  //    please note that the nodes in NodeList will copied, not moved
  //
  //    To do a ajax update use .setHref('url')
  //
  // href: String
  //    The href of the content that displays now.
  //    Set this at construction if you want to load data externally when the
  //    pane is shown.  (Set preload=true to load it immediately.)
  //    Changing href after creation doesn't have any effect; see setHref();
  href: "",

  // extractContent: Boolean
  //  Extract visible content from inside of <body> .... </body>
  extractContent: false,

  // parseOnLoad: Boolean
  //  parse content and create the widgets, if any
  parseOnLoad:  true,

  // preventCache: Boolean
  //    Cache content retreived externally
  preventCache: false,

  // preload: Boolean
  //  Force load of data even if pane is hidden.
  preload: false,

  // refreshOnShow: Boolean
  //    Refresh (re-download) content when pane goes from hidden to shown
  refreshOnShow: false,

  // loadingMessage: String
  //  Message that shows while downloading
  loadingMessage: "<span class='dijitContentPaneLoading'>${loadingState}</span>", 

  // errorMessage: String
  //  Message that shows if an error occurs
  errorMessage: "<span class='dijitContentPaneError'>${errorState}</span>", 

  // isLoaded: Boolean
  //  Tells loading status see onLoad|onUnload for event hooks
  isLoaded: false,

  // class: String
  //  Class name to apply to ContentPane dom nodes
  // TODO: this should be called "baseClass" like in the other widgets
  "class": "dijitContentPane",

  // doLayout: String/Boolean
  //  false - don't adjust size of children
  //  true - looks for the first sizable child widget (ie, having resize() method) and sets it's size to
  //      however big the ContentPane is (TODO: implement)
  //  auto - if there is a single sizable child widget (ie, having resize() method), set it's size to
  //      however big the ContentPane is
  doLayout: "auto",

  postCreate: function(){
    // remove the title attribute so it doesn't show up when i hover
    // over a node
    this.domNode.title = "";

    if(!this.containerNode){
      // make getDescendants() work
      this.containerNode = this.domNode;
    }

    if(this.preload){
      this._loadCheck();
    }

    var messages = dojo.i18n.getLocalization("dijit", "loading", this.lang);
    this.loadingMessage = dojo.string.substitute(this.loadingMessage, messages);
    this.errorMessage = dojo.string.substitute(this.errorMessage, messages);
    var curRole = dijit.getWaiRole(this.domNode);
    if (!curRole){
      dijit.setWaiRole(this.domNode, "group");
    }

    // for programatically created ContentPane (with <span> tag), need to muck w/CSS
    // or it's as though overflow:visible is set
    dojo.addClass(this.domNode, this["class"]);
  },

  startup: function(){
    if(this._started){ return; }
    if(this.doLayout != "false" && this.doLayout !== false){
      this._checkIfSingleChild();
      if(this._singleChild){
        this._singleChild.startup();
      }
    }
    this._loadCheck();
    this.inherited(arguments);
  },

  _checkIfSingleChild: function(){
    // summary:
    //  Test if we have exactly one widget as a child, and if so assume that we are a container for that widget,
    //  and should propogate startup() and resize() calls to it.

    // TODO: if there are two child widgets (a data store and a TabContainer, for example),
    //  should still find the TabContainer
    var childNodes = dojo.query(">", this.containerNode || this.domNode),
      childWidgets = childNodes.filter("[widgetId]");

    if(childNodes.length == 1 && childWidgets.length == 1){
      this.isContainer = true;
      this._singleChild = dijit.byNode(childWidgets[0]);
    }else{
      delete this.isContainer;
      delete this._singleChild;
    }
  },

  refresh: function(){
    // summary:
    //  Force a refresh (re-download) of content, be sure to turn off cache

    // we return result of _prepareLoad here to avoid code dup. in dojox.layout.ContentPane
    return this._prepareLoad(true);
  },

  setHref: function(/*String|Uri*/ href){
    // summary:
    //    Reset the (external defined) content of this pane and replace with new url
    //    Note: It delays the download until widget is shown if preload is false
    //  href:
    //    url to the page you want to get, must be within the same domain as your mainpage
    this.href = href;

    // we return result of _prepareLoad here to avoid code dup. in dojox.layout.ContentPane
    return this._prepareLoad();
  },

  setContent: function(/*String|DomNode|Nodelist*/data){
    // summary:
    //    Replaces old content with data content, include style classes from old content
    //  data:
    //    the new Content may be String, DomNode or NodeList
    //
    //    if data is a NodeList (or an array of nodes) nodes are copied
    //    so you can import nodes from another document implicitly

    // clear href so we cant run refresh and clear content
    // refresh should only work if we downloaded the content
    if(!this._isDownloaded){
      this.href = "";
      this._onUnloadHandler();
    }

    this._setContent(data || "");

    this._isDownloaded = false; // must be set after _setContent(..), pathadjust in dojox.layout.ContentPane

    if(this.parseOnLoad){
      this._createSubWidgets();
    }

    if(this.doLayout != "false" && this.doLayout !== false){
      this._checkIfSingleChild();
      if(this._singleChild && this._singleChild.resize){
        this._singleChild.startup();
        this._singleChild.resize(this._contentBox || dojo.contentBox(this.containerNode || this.domNode));
      }
    }

    this._onLoadHandler();
  },

  cancel: function(){
    // summary:
    //    Cancels a inflight download of content
    if(this._xhrDfd && (this._xhrDfd.fired == -1)){
      this._xhrDfd.cancel();
    }
    delete this._xhrDfd; // garbage collect
  },

  destroy: function(){
    // if we have multiple controllers destroying us, bail after the first
    if(this._beingDestroyed){
      return;
    }
    // make sure we call onUnload
    this._onUnloadHandler();
    this._beingDestroyed = true;
    this.inherited("destroy",arguments);
  },

  resize: function(size){
    dojo.marginBox(this.domNode, size);

    // Compute content box size in case we [later] need to size child
    // If either height or width wasn't specified by the user, then query node for it.
    // But note that setting the margin box and then immediately querying dimensions may return
    // inaccurate results, so try not to depend on it.
    var node = this.containerNode || this.domNode,
      mb = dojo.mixin(dojo.marginBox(node), size||{});

    this._contentBox = dijit.layout.marginBox2contentBox(node, mb);

    // If we have a single widget child then size it to fit snugly within my borders
    if(this._singleChild && this._singleChild.resize){
      this._singleChild.resize(this._contentBox);
    }
  },

  _prepareLoad: function(forceLoad){
    // sets up for a xhrLoad, load is deferred until widget onShow
    // cancels a inflight download
    this.cancel();
    this.isLoaded = false;
    this._loadCheck(forceLoad);
  },

  _isShown: function(){
    // summary: returns true if the content is currently shown
    if("open" in this){
      return this.open;   // for TitlePane, etc.
    }else{
      var node = this.domNode;
      return (node.style.display != 'none')  && (node.style.visibility != 'hidden');
    }
  },

  _loadCheck: function(/*Boolean*/ forceLoad){
    // call this when you change onShow (onSelected) status when selected in parent container
    // it's used as a trigger for href download when this.domNode.display != 'none'

    // sequence:
    // if no href -> bail
    // forceLoad -> always load
    // this.preload -> load when download not in progress, domNode display doesn't matter
    // this.refreshOnShow -> load when download in progress bails, domNode display !='none' AND
    //            this.open !== false (undefined is ok), isLoaded doesn't matter
    // else -> load when download not in progress, if this.open !== false (undefined is ok) AND
    //            domNode display != 'none', isLoaded must be false

    var displayState = this._isShown();

    if(this.href && 
      (forceLoad ||
        (this.preload && !this._xhrDfd) ||
        (this.refreshOnShow && displayState && !this._xhrDfd) ||
        (!this.isLoaded && displayState && !this._xhrDfd)
      )
    ){
      this._downloadExternalContent();
    }
  },

  _downloadExternalContent: function(){
    this._onUnloadHandler();

    // display loading message
    this._setContent(
      this.onDownloadStart.call(this)
    );

    var self = this;
    var getArgs = {
      preventCache: (this.preventCache || this.refreshOnShow),
      url: this.href,
      handleAs: "text"
    };
    if(dojo.isObject(this.ioArgs)){
      dojo.mixin(getArgs, this.ioArgs);
    }

    var hand = this._xhrDfd = (this.ioMethod || dojo.xhrGet)(getArgs);

    hand.addCallback(function(html){
      try{
        self.onDownloadEnd.call(self);
        self._isDownloaded = true;
        self.setContent.call(self, html); // onload event is called from here
      }catch(err){
        self._onError.call(self, 'Content', err); // onContentError
      }
      delete self._xhrDfd;
      return html;
    });

    hand.addErrback(function(err){
      if(!hand.cancelled){
        // show error message in the pane
        self._onError.call(self, 'Download', err); // onDownloadError
      }
      delete self._xhrDfd;
      return err;
    });
  },

  _onLoadHandler: function(){
    this.isLoaded = true;
    try{
      this.onLoad.call(this);
    }catch(e){
      console.error('Error '+this.widgetId+' running custom onLoad code');
    }
  },

  _onUnloadHandler: function(){
    this.isLoaded = false;
    this.cancel();
    try{
      this.onUnload.call(this);
    }catch(e){
      console.error('Error '+this.widgetId+' running custom onUnload code');
    }
  },

  _setContent: function(cont){
    this.destroyDescendants();

    try{
      var node = this.containerNode || this.domNode;
      while(node.firstChild){
        dojo._destroyElement(node.firstChild);
      }
      if(typeof cont == "string"){
        // dijit.ContentPane does only minimal fixes,
        // No pathAdjustments, script retrieval, style clean etc
        // some of these should be available in the dojox.layout.ContentPane
        if(this.extractContent){
          match = cont.match(/<body[^>]*>\s*([\s\S]+)\s*<\/body>/im);
          if(match){ cont = match[1]; }
        }
        node.innerHTML = cont;
      }else{
        // domNode or NodeList
        if(cont.nodeType){ // domNode (htmlNode 1 or textNode 3)
          node.appendChild(cont);
        }else{// nodelist or array such as dojo.Nodelist
          dojo.forEach(cont, function(n){
            node.appendChild(n.cloneNode(true));
          });
        }
      }
    }catch(e){
      // check if a domfault occurs when we are appending this.errorMessage
      // like for instance if domNode is a UL and we try append a DIV
      var errMess = this.onContentError(e);
      try{
        node.innerHTML = errMess;
      }catch(e){
        console.error('Fatal '+this.id+' could not change content due to '+e.message, e);
      }
    }
  },

  _onError: function(type, err, consoleText){
    // shows user the string that is returned by on[type]Error
    // overide on[type]Error and return your own string to customize
    var errText = this['on' + type + 'Error'].call(this, err);
    if(consoleText){
      console.error(consoleText, err);
    }else if(errText){// a empty string won't change current content
      this._setContent.call(this, errText);
    }
  },

  _createSubWidgets: function(){
    // summary: scan my contents and create subwidgets
    var rootNode = this.containerNode || this.domNode;
    try{
      dojo.parser.parse(rootNode, true);
    }catch(e){
      this._onError('Content', e, "Couldn't create widgets in "+this.id
        +(this.href ? " from "+this.href : ""));
    }
  },

  // EVENT's, should be overide-able
  onLoad: function(e){
    // summary:
    //    Event hook, is called after everything is loaded and widgetified
  },

  onUnload: function(e){
    // summary:
    //    Event hook, is called before old content is cleared
  },

  onDownloadStart: function(){
    // summary:
    //    called before download starts
    //    the string returned by this function will be the html
    //    that tells the user we are loading something
    //    override with your own function if you want to change text
    return this.loadingMessage;
  },

  onContentError: function(/*Error*/ error){
    // summary:
    //    called on DOM faults, require fault etc in content
    //    default is to display errormessage inside pane
  },

  onDownloadError: function(/*Error*/ error){
    // summary:
    //    Called when download error occurs, default is to display
    //    errormessage inside pane. Overide function to change that.
    //    The string returned by this function will be the html
    //    that tells the user a error happend
    return this.errorMessage;
  },

  onDownloadEnd: function(){
    // summary:
    //    called when download is finished
  }
});

}

if(!dojo._hasResource["dojo.dnd.common"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojo.dnd.common"] = true;
dojo.provide("dojo.dnd.common");

dojo.dnd._copyKey = navigator.appVersion.indexOf("Macintosh") < 0 ? "ctrlKey" : "metaKey";

dojo.dnd.getCopyKeyState = function(e) {
  // summary: abstracts away the difference between selection on Mac and PC,
  //  and returns the state of the "copy" key to be pressed.
  // e: Event: mouse event
  return e[dojo.dnd._copyKey];  // Boolean
};

dojo.dnd._uniqueId = 0;
dojo.dnd.getUniqueId = function(){
  // summary: returns a unique string for use with any DOM element
  var id;
  do{
    id = dojo._scopeName + "Unique" + (++dojo.dnd._uniqueId);
  }while(dojo.byId(id));
  return id;
};

dojo.dnd._empty = {};

dojo.dnd.isFormElement = function(/*Event*/ e){
  // summary: returns true, if user clicked on a form element
  var t = e.target;
  if(t.nodeType == 3 /*TEXT_NODE*/){
    t = t.parentNode;
  }
  return " button textarea input select option ".indexOf(" " + t.tagName.toLowerCase() + " ") >= 0; // Boolean
};

}

if(!dojo._hasResource["dojo.dnd.autoscroll"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojo.dnd.autoscroll"] = true;
dojo.provide("dojo.dnd.autoscroll");

dojo.dnd.getViewport = function(){
  // summary: returns a viewport size (visible part of the window)

  // FIXME: need more docs!!
  var d = dojo.doc, dd = d.documentElement, w = window, b = dojo.body();
  if(dojo.isMozilla){
    return {w: dd.clientWidth, h: w.innerHeight}; // Object
  }else if(!dojo.isOpera && w.innerWidth){
    return {w: w.innerWidth, h: w.innerHeight};   // Object
  }else if (!dojo.isOpera && dd && dd.clientWidth){
    return {w: dd.clientWidth, h: dd.clientHeight}; // Object
  }else if (b.clientWidth){
    return {w: b.clientWidth, h: b.clientHeight}; // Object
  }
  return null;  // Object
};

dojo.dnd.V_TRIGGER_AUTOSCROLL = 32;
dojo.dnd.H_TRIGGER_AUTOSCROLL = 32;

dojo.dnd.V_AUTOSCROLL_VALUE = 16;
dojo.dnd.H_AUTOSCROLL_VALUE = 16;

dojo.dnd.autoScroll = function(e){
  // summary:
  //    a handler for onmousemove event, which scrolls the window, if
  //    necesary
  // e: Event:
  //    onmousemove event

  // FIXME: needs more docs!
  var v = dojo.dnd.getViewport(), dx = 0, dy = 0;
  if(e.clientX < dojo.dnd.H_TRIGGER_AUTOSCROLL){
    dx = -dojo.dnd.H_AUTOSCROLL_VALUE;
  }else if(e.clientX > v.w - dojo.dnd.H_TRIGGER_AUTOSCROLL){
    dx = dojo.dnd.H_AUTOSCROLL_VALUE;
  }
  if(e.clientY < dojo.dnd.V_TRIGGER_AUTOSCROLL){
    dy = -dojo.dnd.V_AUTOSCROLL_VALUE;
  }else if(e.clientY > v.h - dojo.dnd.V_TRIGGER_AUTOSCROLL){
    dy = dojo.dnd.V_AUTOSCROLL_VALUE;
  }
  window.scrollBy(dx, dy);
};

dojo.dnd._validNodes = {"div": 1, "p": 1, "td": 1};
dojo.dnd._validOverflow = {"auto": 1, "scroll": 1};

dojo.dnd.autoScrollNodes = function(e){
  // summary:
  //    a handler for onmousemove event, which scrolls the first avaialble
  //    Dom element, it falls back to dojo.dnd.autoScroll()
  // e: Event:
  //    onmousemove event

  // FIXME: needs more docs!
  for(var n = e.target; n;){
    if(n.nodeType == 1 && (n.tagName.toLowerCase() in dojo.dnd._validNodes)){
      var s = dojo.getComputedStyle(n);
      if(s.overflow.toLowerCase() in dojo.dnd._validOverflow){
        var b = dojo._getContentBox(n, s), t = dojo._abs(n, true);
        // console.debug(b.l, b.t, t.x, t.y, n.scrollLeft, n.scrollTop);
        b.l += t.x + n.scrollLeft;
        b.t += t.y + n.scrollTop;
        var w = Math.min(dojo.dnd.H_TRIGGER_AUTOSCROLL, b.w / 2), 
          h = Math.min(dojo.dnd.V_TRIGGER_AUTOSCROLL, b.h / 2),
          rx = e.pageX - b.l, ry = e.pageY - b.t, dx = 0, dy = 0;
        if(rx > 0 && rx < b.w){
          if(rx < w){
            dx = -dojo.dnd.H_AUTOSCROLL_VALUE;
          }else if(rx > b.w - w){
            dx = dojo.dnd.H_AUTOSCROLL_VALUE;
          }
        }
        //console.debug("ry =", ry, "b.h =", b.h, "h =", h);
        if(ry > 0 && ry < b.h){
          if(ry < h){
            dy = -dojo.dnd.V_AUTOSCROLL_VALUE;
          }else if(ry > b.h - h){
            dy = dojo.dnd.V_AUTOSCROLL_VALUE;
          }
        }
        var oldLeft = n.scrollLeft, oldTop = n.scrollTop;
        n.scrollLeft = n.scrollLeft + dx;
        n.scrollTop  = n.scrollTop  + dy;
        // if(dx || dy){ console.debug(oldLeft + ", " + oldTop + "\n" + dx + ", " + dy + "\n" + n.scrollLeft + ", " + n.scrollTop); }
        if(oldLeft != n.scrollLeft || oldTop != n.scrollTop){ return; }
      }
    }
    try{
      n = n.parentNode;
    }catch(x){
      n = null;
    }
  }
  dojo.dnd.autoScroll(e);
};

}

if(!dojo._hasResource["dojo.dnd.Mover"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojo.dnd.Mover"] = true;
dojo.provide("dojo.dnd.Mover");




dojo.declare("dojo.dnd.Mover", null, {
  constructor: function(node, e, host){
    // summary: an object, which makes a node follow the mouse, 
    //  used as a default mover, and as a base class for custom movers
    // node: Node: a node (or node's id) to be moved
    // e: Event: a mouse event, which started the move;
    //  only pageX and pageY properties are used
    // host: Object?: object which implements the functionality of the move,
    //   and defines proper events (onMoveStart and onMoveStop)
    this.node = dojo.byId(node);
    this.marginBox = {l: e.pageX, t: e.pageY};
    this.mouseButton = e.button;
    var h = this.host = host, d = node.ownerDocument, 
      firstEvent = dojo.connect(d, "onmousemove", this, "onFirstMove");
    this.events = [
      dojo.connect(d, "onmousemove", this, "onMouseMove"),
      dojo.connect(d, "onmouseup",   this, "onMouseUp"),
      // cancel text selection and text dragging
      dojo.connect(d, "ondragstart",   dojo, "stopEvent"),
      dojo.connect(d, "onselectstart", dojo, "stopEvent"),
      firstEvent
    ];
    // notify that the move has started
    if(h && h.onMoveStart){
      h.onMoveStart(this);
    }
  },
  // mouse event processors
  onMouseMove: function(e){
    // summary: event processor for onmousemove
    // e: Event: mouse event
    dojo.dnd.autoScroll(e);
    var m = this.marginBox;
    this.host.onMove(this, {l: m.l + e.pageX, t: m.t + e.pageY});
  },
  onMouseUp: function(e){
    if(this.mouseButton == e.button){
      this.destroy();
    }
  },
  // utilities
  onFirstMove: function(){
    // summary: makes the node absolute; it is meant to be called only once
    var s = this.node.style, l, t;
    switch(s.position){
      case "relative":
      case "absolute":
        // assume that left and top values are in pixels already
        l = Math.round(parseFloat(s.left));
        t = Math.round(parseFloat(s.top));
        break;
      default:
        s.position = "absolute";  // enforcing the absolute mode
        var m = dojo.marginBox(this.node);
        l = m.l;
        t = m.t;
        break;
    }
    this.marginBox.l = l - this.marginBox.l;
    this.marginBox.t = t - this.marginBox.t;
    this.host.onFirstMove(this);
    dojo.disconnect(this.events.pop());
  },
  destroy: function(){
    // summary: stops the move, deletes all references, so the object can be garbage-collected
    dojo.forEach(this.events, dojo.disconnect);
    // undo global settings
    var h = this.host;
    if(h && h.onMoveStop){
      h.onMoveStop(this);
    }
    // destroy objects
    this.events = this.node = null;
  }
});

}

if(!dojo._hasResource["dojo.dnd.Moveable"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojo.dnd.Moveable"] = true;
dojo.provide("dojo.dnd.Moveable");



dojo.declare("dojo.dnd.Moveable", null, {
  // object attributes (for markup)
  handle: "",
  delay: 0,
  skip: false,
  
  constructor: function(node, params){
    // summary: an object, which makes a node moveable
    // node: Node: a node (or node's id) to be moved
    // params: Object: an optional object with additional parameters;
    //  following parameters are recognized:
    //    handle: Node: a node (or node's id), which is used as a mouse handle
    //      if omitted, the node itself is used as a handle
    //    delay: Number: delay move by this number of pixels
    //    skip: Boolean: skip move of form elements
    //    mover: Object: a constructor of custom Mover
    this.node = dojo.byId(node);
    if(!params){ params = {}; }
    this.handle = params.handle ? dojo.byId(params.handle) : null;
    if(!this.handle){ this.handle = this.node; }
    this.delay = params.delay > 0 ? params.delay : 0;
    this.skip  = params.skip;
    this.mover = params.mover ? params.mover : dojo.dnd.Mover;
    this.events = [
      dojo.connect(this.handle, "onmousedown", this, "onMouseDown"),
      // cancel text selection and text dragging
      dojo.connect(this.handle, "ondragstart",   this, "onSelectStart"),
      dojo.connect(this.handle, "onselectstart", this, "onSelectStart")
    ];
  },

  // markup methods
  markupFactory: function(params, node){
    return new dojo.dnd.Moveable(node, params);
  },

  // methods
  destroy: function(){
    // summary: stops watching for possible move, deletes all references, so the object can be garbage-collected
    dojo.forEach(this.events, dojo.disconnect);
    this.events = this.node = this.handle = null;
  },
  
  // mouse event processors
  onMouseDown: function(e){
    // summary: event processor for onmousedown, creates a Mover for the node
    // e: Event: mouse event
    if(this.skip && dojo.dnd.isFormElement(e)){ return; }
    if(this.delay){
      this.events.push(dojo.connect(this.handle, "onmousemove", this, "onMouseMove"));
      this.events.push(dojo.connect(this.handle, "onmouseup", this, "onMouseUp"));
      this._lastX = e.pageX;
      this._lastY = e.pageY;
    }else{
      new this.mover(this.node, e, this);
    }
    dojo.stopEvent(e);
  },
  onMouseMove: function(e){
    // summary: event processor for onmousemove, used only for delayed drags
    // e: Event: mouse event
    if(Math.abs(e.pageX - this._lastX) > this.delay || Math.abs(e.pageY - this._lastY) > this.delay){
      this.onMouseUp(e);
      new this.mover(this.node, e, this);
    }
    dojo.stopEvent(e);
  },
  onMouseUp: function(e){
    // summary: event processor for onmouseup, used only for delayed delayed drags
    // e: Event: mouse event
    dojo.disconnect(this.events.pop());
    dojo.disconnect(this.events.pop());
  },
  onSelectStart: function(e){
    // summary: event processor for onselectevent and ondragevent
    // e: Event: mouse event
    if(!this.skip || !dojo.dnd.isFormElement(e)){
      dojo.stopEvent(e);
    }
  },
  
  // local events
  onMoveStart: function(/* dojo.dnd.Mover */ mover){
    // summary: called before every move operation
    dojo.publish("/dnd/move/start", [mover]);
    dojo.addClass(dojo.body(), "dojoMove"); 
    dojo.addClass(this.node, "dojoMoveItem"); 
  },
  onMoveStop: function(/* dojo.dnd.Mover */ mover){
    // summary: called after every move operation
    dojo.publish("/dnd/move/stop", [mover]);
    dojo.removeClass(dojo.body(), "dojoMove");
    dojo.removeClass(this.node, "dojoMoveItem");
  },
  onFirstMove: function(/* dojo.dnd.Mover */ mover){
    // summary: called during the very first move notification,
    //  can be used to initialize coordinates, can be overwritten.
    
    // default implementation does nothing
  },
  onMove: function(/* dojo.dnd.Mover */ mover, /* Object */ leftTop){
    // summary: called during every move notification,
    //  should actually move the node, can be overwritten.
    this.onMoving(mover, leftTop);
    var s = mover.node.style;
    s.left = leftTop.l + "px";
    s.top  = leftTop.t + "px";
    this.onMoved(mover, leftTop);
  },
  onMoving: function(/* dojo.dnd.Mover */ mover, /* Object */ leftTop){
    // summary: called before every incremental move,
    //  can be overwritten.
    
    // default implementation does nothing
  },
  onMoved: function(/* dojo.dnd.Mover */ mover, /* Object */ leftTop){
    // summary: called after every incremental move,
    //  can be overwritten.
    
    // default implementation does nothing
  }
});

}

if(!dojo._hasResource["dojo.dnd.TimedMoveable"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojo.dnd.TimedMoveable"] = true;
dojo.provide("dojo.dnd.TimedMoveable");



(function(){
  // precalculate long expressions
  var oldOnMove = dojo.dnd.Moveable.prototype.onMove;
    
  dojo.declare("dojo.dnd.TimedMoveable", dojo.dnd.Moveable, {
    // summary:
    //  A specialized version of Moveable to support an FPS throttling.
    //  This class puts an upper restriction on FPS, which may reduce 
    //  the CPU load. The additional parameter "timeout" regulates
    //  the delay before actually moving the moveable object.
    
    // object attributes (for markup)
    timeout: 40,  // in ms, 40ms corresponds to 25 fps
  
    constructor: function(node, params){
      // summary: an object, which makes a node moveable with a timer
      // node: Node: a node (or node's id) to be moved
      // params: Object: an optional object with additional parameters.
      //  See dojo.dnd.Moveable for details on general parameters.
      //  Following parameters are specific for this class:
      //    timeout: Number: delay move by this number of ms
      //      accumulating position changes during the timeout
      
      // sanitize parameters
      if(!params){ params = {}; }
      if(params.timeout && typeof params.timeout == "number" && params.timeout >= 0){
        this.timeout = params.timeout;
      }
    },
  
    // markup methods
    markupFactory: function(params, node){
      return new dojo.dnd.TimedMoveable(node, params);
    },
  
    onMoveStop: function(/* dojo.dnd.Mover */ mover){
      if(mover._timer){
        // stop timer
        clearTimeout(mover._timer)
        // reflect the last received position
        oldOnMove.call(this, mover, mover._leftTop)
      }
      dojo.dnd.Moveable.prototype.onMoveStop.apply(this, arguments);
    },
    onMove: function(/* dojo.dnd.Mover */ mover, /* Object */ leftTop){
      mover._leftTop = leftTop;
      if(!mover._timer){
        var _t = this;  // to avoid using dojo.hitch()
        mover._timer = setTimeout(function(){
          // we don't have any pending requests
          mover._timer = null;
          // reflect the last received position
          oldOnMove.call(_t, mover, mover._leftTop);
        }, this.timeout);
      }
    }
  });
})();

}

if(!dojo._hasResource["dojo.fx"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojo.fx"] = true;
dojo.provide("dojo.fx");
dojo.provide("dojo.fx.Toggler");

/*=====
dojo.fx = {
  // summary: Effects library on top of Base animations
};
=====*/

(function(){
  var _baseObj = {
      _fire: function(evt, args){
        if(this[evt]){
          this[evt].apply(this, args||[]);
        }
        return this;
      }
    };

  var _chain = function(animations){
    this._index = -1;
    this._animations = animations||[];
    this._current = this._onAnimateCtx = this._onEndCtx = null;

    this.duration = 0;
    dojo.forEach(this._animations, function(a){
      this.duration += a.duration;
      if(a.delay){ this.duration += a.delay; }
    }, this);
  };
  dojo.extend(_chain, {
    _onAnimate: function(){
      this._fire("onAnimate", arguments);
    },
    _onEnd: function(){
      dojo.disconnect(this._onAnimateCtx);
      dojo.disconnect(this._onEndCtx);
      this._onAnimateCtx = this._onEndCtx = null;
      if(this._index + 1 == this._animations.length){
        this._fire("onEnd");
      }else{
        // switch animations
        this._current = this._animations[++this._index];
        this._onAnimateCtx = dojo.connect(this._current, "onAnimate", this, "_onAnimate");
        this._onEndCtx = dojo.connect(this._current, "onEnd", this, "_onEnd");
        this._current.play(0, true);
      }
    },
    play: function(/*int?*/ delay, /*Boolean?*/ gotoStart){
      if(!this._current){ this._current = this._animations[this._index = 0]; }
      if(!gotoStart && this._current.status() == "playing"){ return this; }
      var beforeBegin = dojo.connect(this._current, "beforeBegin", this, function(){
          this._fire("beforeBegin");
        }),
        onBegin = dojo.connect(this._current, "onBegin", this, function(arg){
          this._fire("onBegin", arguments);
        }),
        onPlay = dojo.connect(this._current, "onPlay", this, function(arg){
          this._fire("onPlay", arguments);
          dojo.disconnect(beforeBegin);
          dojo.disconnect(onBegin);
          dojo.disconnect(onPlay);
        });
      if(this._onAnimateCtx){
        dojo.disconnect(this._onAnimateCtx);
      }
      this._onAnimateCtx = dojo.connect(this._current, "onAnimate", this, "_onAnimate");
      if(this._onEndCtx){
        dojo.disconnect(this._onEndCtx);
      }
      this._onEndCtx = dojo.connect(this._current, "onEnd", this, "_onEnd");
      this._current.play.apply(this._current, arguments);
      return this;
    },
    pause: function(){
      if(this._current){
        var e = dojo.connect(this._current, "onPause", this, function(arg){
            this._fire("onPause", arguments);
            dojo.disconnect(e);
          });
        this._current.pause();
      }
      return this;
    },
    gotoPercent: function(/*Decimal*/percent, /*Boolean?*/ andPlay){
      this.pause();
      var offset = this.duration * percent;
      this._current = null;
      dojo.some(this._animations, function(a){
        if(a.duration <= offset){
          this._current = a;
          return true;
        }
        offset -= a.duration;
        return false;
      });
      if(this._current){
        this._current.gotoPercent(offset / _current.duration, andPlay);
      }
      return this;
    },
    stop: function(/*boolean?*/ gotoEnd){
      if(this._current){
        if(gotoEnd){
          for(; this._index + 1 < this._animations.length; ++this._index){
            this._animations[this._index].stop(true);
          }
          this._current = this._animations[this._index];
        }
        var e = dojo.connect(this._current, "onStop", this, function(arg){
            this._fire("onStop", arguments);
            dojo.disconnect(e);
          });
        this._current.stop();
      }
      return this;
    },
    status: function(){
      return this._current ? this._current.status() : "stopped";
    },
    destroy: function(){
      if(this._onAnimateCtx){ dojo.disconnect(this._onAnimateCtx); }
      if(this._onEndCtx){ dojo.disconnect(this._onEndCtx); }
    }
  });
  dojo.extend(_chain, _baseObj);

  dojo.fx.chain = function(/*dojo._Animation[]*/ animations){
    // summary: Chain a list of dojo._Animation s to run in sequence
    // example:
    //  | dojo.fx.chain([
    //  |   dojo.fadeIn({ node:node }),
    //  |   dojo.fadeOut({ node:otherNode })
    //  | ]).play();
    //
    return new _chain(animations) // dojo._Animation
  };

  var _combine = function(animations){
    this._animations = animations||[];
    this._connects = [];
    this._finished = 0;

    this.duration = 0;
    dojo.forEach(animations, function(a){
      var duration = a.duration;
      if(a.delay){ duration += a.delay; }
      if(this.duration < duration){ this.duration = duration; }
      this._connects.push(dojo.connect(a, "onEnd", this, "_onEnd"));
    }, this);
    
    this._pseudoAnimation = new dojo._Animation({curve: [0, 1], duration: this.duration});
    dojo.forEach(["beforeBegin", "onBegin", "onPlay", "onAnimate", "onPause", "onStop"], 
      function(evt){
        this._connects.push(dojo.connect(this._pseudoAnimation, evt, dojo.hitch(this, "_fire", evt)));
      },
      this
    );
  };
  dojo.extend(_combine, {
    _doAction: function(action, args){
      dojo.forEach(this._animations, function(a){
        a[action].apply(a, args);
      });
      return this;
    },
    _onEnd: function(){
      if(++this._finished == this._animations.length){
        this._fire("onEnd");
      }
    },
    _call: function(action, args){
      var t = this._pseudoAnimation;
      t[action].apply(t, args);
    },
    play: function(/*int?*/ delay, /*Boolean?*/ gotoStart){
      this._finished = 0;
      this._doAction("play", arguments);
      this._call("play", arguments);
      return this;
    },
    pause: function(){
      this._doAction("pause", arguments);
      this._call("pause", arguments);
      return this;
    },
    gotoPercent: function(/*Decimal*/percent, /*Boolean?*/ andPlay){
      var ms = this.duration * percent;
      dojo.forEach(this._animations, function(a){
        a.gotoPercent(a.duration < ms ? 1 : (ms / a.duration), andPlay);
      });
      this._call("gotoProcent", arguments);
      return this;
    },
    stop: function(/*boolean?*/ gotoEnd){
      this._doAction("stop", arguments);
      this._call("stop", arguments);
      return this;
    },
    status: function(){
      return this._pseudoAnimation.status();
    },
    destroy: function(){
      dojo.forEach(this._connects, dojo.disconnect);
    }
  });
  dojo.extend(_combine, _baseObj);

  dojo.fx.combine = function(/*dojo._Animation[]*/ animations){
    // summary: Combine a list of dojo._Animation s to run in parallel
    // example:
    //  | dojo.fx.combine([
    //  |   dojo.fadeIn({ node:node }),
    //  |   dojo.fadeOut({ node:otherNode })
    //  | ]).play();
    return new _combine(animations); // dojo._Animation
  };
})();

dojo.declare("dojo.fx.Toggler", null, {
  // summary:
  //    class constructor for an animation toggler. It accepts a packed
  //    set of arguments about what type of animation to use in each
  //    direction, duration, etc.
  //
  // example:
  //  | var t = new dojo.fx.Toggler({
  //  |   node: "nodeId",
  //  |   showDuration: 500,
  //  |   // hideDuration will default to "200"
  //  |   showFunc: dojo.wipeIn, 
  //  |   // hideFunc will default to "fadeOut"
  //  | });
  //  | t.show(100); // delay showing for 100ms
  //  | // ...time passes...
  //  | t.hide();

  // FIXME: need a policy for where the toggler should "be" the next
  // time show/hide are called if we're stopped somewhere in the
  // middle.

  constructor: function(args){
    var _t = this;

    dojo.mixin(_t, args);
    _t.node = args.node;
    _t._showArgs = dojo.mixin({}, args);
    _t._showArgs.node = _t.node;
    _t._showArgs.duration = _t.showDuration;
    _t.showAnim = _t.showFunc(_t._showArgs);

    _t._hideArgs = dojo.mixin({}, args);
    _t._hideArgs.node = _t.node;
    _t._hideArgs.duration = _t.hideDuration;
    _t.hideAnim = _t.hideFunc(_t._hideArgs);

    dojo.connect(_t.showAnim, "beforeBegin", dojo.hitch(_t.hideAnim, "stop", true));
    dojo.connect(_t.hideAnim, "beforeBegin", dojo.hitch(_t.showAnim, "stop", true));
  },

  // node: DomNode
  //  the node to toggle
  node: null,

  // showFunc: Function
  //  The function that returns the dojo._Animation to show the node
  showFunc: dojo.fadeIn,

  // hideFunc: Function 
  //  The function that returns the dojo._Animation to hide the node
  hideFunc: dojo.fadeOut,

  // showDuration:
  //  Time in milliseconds to run the show Animation
  showDuration: 200,

  // hideDuration:
  //  Time in milliseconds to run the hide Animation
  hideDuration: 200,

  /*=====
  _showArgs: null,
  _showAnim: null,

  _hideArgs: null,
  _hideAnim: null,

  _isShowing: false,
  _isHiding: false,
  =====*/

  show: function(delay){
    // summary: Toggle the node to showing
    return this.showAnim.play(delay || 0);
  },

  hide: function(delay){
    // summary: Toggle the node to hidden
    return this.hideAnim.play(delay || 0);
  }
});

dojo.fx.wipeIn = function(/*Object*/ args){
  // summary
  //    Returns an animation that will expand the
  //    node defined in 'args' object from it's current height to
  //    it's natural height (with no scrollbar).
  //    Node must have no margin/border/padding.
  args.node = dojo.byId(args.node);
  var node = args.node, s = node.style;

  var anim = dojo.animateProperty(dojo.mixin({
    properties: {
      height: {
        // wrapped in functions so we wait till the last second to query (in case value has changed)
        start: function(){
          // start at current [computed] height, but use 1px rather than 0
          // because 0 causes IE to display the whole panel
          s.overflow="hidden";
          if(s.visibility=="hidden"||s.display=="none"){
            s.height="1px";
            s.display="";
            s.visibility="";
            return 1;
          }else{
            var height = dojo.style(node, "height");
            return Math.max(height, 1);
          }
        },
        end: function(){
          return node.scrollHeight;
        }
      }
    }
  }, args));

  dojo.connect(anim, "onEnd", function(){ 
    s.height = "auto";
  });

  return anim; // dojo._Animation
}

dojo.fx.wipeOut = function(/*Object*/ args){
  // summary
  //    Returns an animation that will shrink node defined in "args"
  //    from it's current height to 1px, and then hide it.
  var node = args.node = dojo.byId(args.node);
  var s = node.style;

  var anim = dojo.animateProperty(dojo.mixin({
    properties: {
      height: {
        end: 1 // 0 causes IE to display the whole panel
      }
    }
  }, args));

  dojo.connect(anim, "beforeBegin", function(){
    s.overflow = "hidden";
    s.display = "";
  });
  dojo.connect(anim, "onEnd", function(){
    s.height = "auto";
    s.display = "none";
  });

  return anim; // dojo._Animation
}

dojo.fx.slideTo = function(/*Object?*/ args){
  // summary
  //    Returns an animation that will slide "node" 
  //    defined in args Object from its current position to
  //    the position defined by (args.left, args.top).
  // example:
  //  | dojo.fx.slideTo({ node: node, left:"40", top:"50", unit:"px" }).play()

  var node = (args.node = dojo.byId(args.node));
  
  var top = null;
  var left = null;
  
  var init = (function(n){
    return function(){
      var cs = dojo.getComputedStyle(n);
      var pos = cs.position;
      top = (pos == 'absolute' ? n.offsetTop : parseInt(cs.top) || 0);
      left = (pos == 'absolute' ? n.offsetLeft : parseInt(cs.left) || 0);
      if(pos != 'absolute' && pos != 'relative'){
        var ret = dojo.coords(n, true);
        top = ret.y;
        left = ret.x;
        n.style.position="absolute";
        n.style.top=top+"px";
        n.style.left=left+"px";
      }
    };
  })(node);
  init();

  var anim = dojo.animateProperty(dojo.mixin({
    properties: {
      top: { end: args.top||0 },
      left: { end: args.left||0 }
    }
  }, args));
  dojo.connect(anim, "beforeBegin", anim, init);

  return anim; // dojo._Animation
}

}

if(!dojo._hasResource["dijit._Templated"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit._Templated"] = true;
dojo.provide("dijit._Templated");





dojo.declare("dijit._Templated",
  null,
  {
    //  summary:
    //    Mixin for widgets that are instantiated from a template
    // 
    // templateNode: DomNode
    //    a node that represents the widget template. Pre-empts both templateString and templatePath.
    templateNode: null,

    // templateString: String
    //    a string that represents the widget template. Pre-empts the
    //    templatePath. In builds that have their strings "interned", the
    //    templatePath is converted to an inline templateString, thereby
    //    preventing a synchronous network call.
    templateString: null,

    // templatePath: String
    //  Path to template (HTML file) for this widget relative to dojo.baseUrl
    templatePath: null,

    // widgetsInTemplate: Boolean
    //    should we parse the template to find widgets that might be
    //    declared in markup inside it? false by default.
    widgetsInTemplate: false,

    // containerNode: DomNode
    //    holds child elements. "containerNode" is generally set via a
    //    dojoAttachPoint assignment and it designates where children of
    //    the src dom node will be placed
    containerNode: null,

    // skipNodeCache: Boolean
    //    if using a cached widget template node poses issues for a
    //    particular widget class, it can set this property to ensure
    //    that its template is always re-built from a string
    _skipNodeCache: false,

    _stringRepl: function(tmpl){
      var className = this.declaredClass, _this = this;
      // Cache contains a string because we need to do property replacement
      // do the property replacement
      return dojo.string.substitute(tmpl, this, function(value, key){
        if(key.charAt(0) == '!'){ value = _this[key.substr(1)]; }
        if(typeof value == "undefined"){ throw new Error(className+" template:"+key); } // a debugging aide
        if(!value){ return ""; }

        // Substitution keys beginning with ! will skip the transform step,
        // in case a user wishes to insert unescaped markup, e.g. ${!foo}
        return key.charAt(0) == "!" ? value :
          // Safer substitution, see heading "Attribute values" in
          // http://www.w3.org/TR/REC-html40/appendix/notes.html#h-B.3.2
          value.toString().replace(/"/g,"&quot;"); //TODO: add &amp? use encodeXML method?
      }, this);
    },

    // method over-ride
    buildRendering: function(){
      // summary:
      //    Construct the UI for this widget from a template, setting this.domNode.

      // Lookup cached version of template, and download to cache if it
      // isn't there already.  Returns either a DomNode or a string, depending on
      // whether or not the template contains ${foo} replacement parameters.
      var cached = dijit._Templated.getCachedTemplate(this.templatePath, this.templateString, this._skipNodeCache);

      var node;
      if(dojo.isString(cached)){
        node = dijit._Templated._createNodesFromText(this._stringRepl(cached))[0];
      }else{
        // if it's a node, all we have to do is clone it
        node = cached.cloneNode(true);
      }

      // recurse through the node, looking for, and attaching to, our
      // attachment points which should be defined on the template node.
      this._attachTemplateNodes(node);

      var source = this.srcNodeRef;
      if(source && source.parentNode){
        source.parentNode.replaceChild(node, source);
      }

      this.domNode = node;
      if(this.widgetsInTemplate){
        var cw = this._supportingWidgets  = dojo.parser.parse(node);
        this._attachTemplateNodes(cw, function(n,p){
          return n[p];
        });
      }

      this._fillContent(source);
    },

    _fillContent: function(/*DomNode*/ source){
      // summary:
      //    relocate source contents to templated container node
      //    this.containerNode must be able to receive children, or exceptions will be thrown
      var dest = this.containerNode;
      if(source && dest){
        while(source.hasChildNodes()){
          dest.appendChild(source.firstChild);
        }
      }
    },

    _attachTemplateNodes: function(rootNode, getAttrFunc){
      // summary: Iterate through the template and attach functions and nodes accordingly.  
      // description:   
      //    Map widget properties and functions to the handlers specified in
      //    the dom node and it's descendants. This function iterates over all
      //    nodes and looks for these properties:
      //      * dojoAttachPoint
      //      * dojoAttachEvent 
      //      * waiRole
      //      * waiState
      // rootNode: DomNode|Array[Widgets]
      //    the node to search for properties. All children will be searched.
      // getAttrFunc: function?
      //    a function which will be used to obtain property for a given
      //    DomNode/Widget

      getAttrFunc = getAttrFunc || function(n,p){ return n.getAttribute(p); };

      var nodes = dojo.isArray(rootNode) ? rootNode : (rootNode.all || rootNode.getElementsByTagName("*"));
      var x=dojo.isArray(rootNode)?0:-1;
      for(; x<nodes.length; x++){
        var baseNode = (x == -1) ? rootNode : nodes[x];
        if(this.widgetsInTemplate && getAttrFunc(baseNode,'dojoType')){
          continue;
        }
        // Process dojoAttachPoint
        var attachPoint = getAttrFunc(baseNode, "dojoAttachPoint");
        if(attachPoint){
          var point, points = attachPoint.split(/\s*,\s*/);
          while((point = points.shift())){
            if(dojo.isArray(this[point])){
              this[point].push(baseNode);
            }else{
              this[point]=baseNode;
            }
          }
        }

        // Process dojoAttachEvent
        var attachEvent = getAttrFunc(baseNode, "dojoAttachEvent");
        if(attachEvent){
          // NOTE: we want to support attributes that have the form
          // "domEvent: nativeEvent; ..."
          var event, events = attachEvent.split(/\s*,\s*/);
          var trim = dojo.trim;
          while((event = events.shift())){
            if(event){
              var thisFunc = null;
              if(event.indexOf(":") != -1){
                // oh, if only JS had tuple assignment
                var funcNameArr = event.split(":");
                event = trim(funcNameArr[0]);
                thisFunc = trim(funcNameArr[1]);
              }else{
                event = trim(event);
              }
              if(!thisFunc){
                thisFunc = event;
              }
              this.connect(baseNode, event, thisFunc);
            }
          }
        }

        // waiRole, waiState
        var role = getAttrFunc(baseNode, "waiRole");
        if(role){
          dijit.setWaiRole(baseNode, role);
        }
        var values = getAttrFunc(baseNode, "waiState");
        if(values){
          dojo.forEach(values.split(/\s*,\s*/), function(stateValue){
            if(stateValue.indexOf('-') != -1){
              var pair = stateValue.split('-');
              dijit.setWaiState(baseNode, pair[0], pair[1]);
            }
          });
        }

      }
    }
  }
);

// key is either templatePath or templateString; object is either string or DOM tree
dijit._Templated._templateCache = {};

dijit._Templated.getCachedTemplate = function(templatePath, templateString, alwaysUseString){
  // summary:
  //    Static method to get a template based on the templatePath or
  //    templateString key
  // templatePath: String
  //    The URL to get the template from. dojo.uri.Uri is often passed as well.
  // templateString: String?
  //    a string to use in lieu of fetching the template from a URL. Takes precedence
  //    over templatePath
  // Returns: Mixed
  //  Either string (if there are ${} variables that need to be replaced) or just
  //  a DOM tree (if the node can be cloned directly)

  // is it already cached?
  var tmplts = dijit._Templated._templateCache;
  var key = templateString || templatePath;
  var cached = tmplts[key];
  if(cached){
    return cached;
  }

  // If necessary, load template string from template path
  if(!templateString){
    templateString = dijit._Templated._sanitizeTemplateString(dojo._getText(templatePath));
  }

  templateString = dojo.string.trim(templateString);

  if(alwaysUseString || templateString.match(/\$\{([^\}]+)\}/g)){
    // there are variables in the template so all we can do is cache the string
    return (tmplts[key] = templateString); //String
  }else{
    // there are no variables in the template so we can cache the DOM tree
    return (tmplts[key] = dijit._Templated._createNodesFromText(templateString)[0]); //Node
  }
};

dijit._Templated._sanitizeTemplateString = function(/*String*/tString){
  // summary: 
  //    Strips <?xml ...?> declarations so that external SVG and XML
  //    documents can be added to a document without worry. Also, if the string
  //    is an HTML document, only the part inside the body tag is returned.
  if(tString){
    tString = tString.replace(/^\s*<\?xml(\s)+version=[\'\"](\d)*.(\d)*[\'\"](\s)*\?>/im, "");
    var matches = tString.match(/<body[^>]*>\s*([\s\S]+)\s*<\/body>/im);
    if(matches){
      tString = matches[1];
    }
  }else{
    tString = "";
  }
  return tString; //String
};


if(dojo.isIE){
  dojo.addOnUnload(function(){
    var cache = dijit._Templated._templateCache;
    for(var key in cache){
      var value = cache[key];
      if(!isNaN(value.nodeType)){ // isNode equivalent
        dojo._destroyElement(value);
      }
      delete cache[key];
    }
  });
}

(function(){
  var tagMap = {
    cell: {re: /^<t[dh][\s\r\n>]/i, pre: "<table><tbody><tr>", post: "</tr></tbody></table>"},
    row: {re: /^<tr[\s\r\n>]/i, pre: "<table><tbody>", post: "</tbody></table>"},
    section: {re: /^<(thead|tbody|tfoot)[\s\r\n>]/i, pre: "<table>", post: "</table>"}
  };

  // dummy container node used temporarily to hold nodes being created
  var tn;

  dijit._Templated._createNodesFromText = function(/*String*/text){
    // summary:
    //  Attempts to create a set of nodes based on the structure of the passed text.

    if(!tn){
      tn = dojo.doc.createElement("div");
      tn.style.display="none";
      dojo.body().appendChild(tn);
    }
    var tableType = "none";
    var rtext = text.replace(/^\s+/, "");
    for(var type in tagMap){
      var map = tagMap[type];
      if(map.re.test(rtext)){
        tableType = type;
        text = map.pre + text + map.post;
        break;
      }
    }

    tn.innerHTML = text;
    if(tn.normalize){
      tn.normalize();
    }

    var tag = { cell: "tr", row: "tbody", section: "table" }[tableType];
    var _parent = (typeof tag != "undefined") ?
            tn.getElementsByTagName(tag)[0] :
            tn;

    var nodes = [];
    while(_parent.firstChild){
      nodes.push(_parent.removeChild(_parent.firstChild));
    }
    tn.innerHTML="";
    return nodes; //  Array
  }
})();

// These arguments can be specified for widgets which are used in templates.
// Since any widget can be specified as sub widgets in template, mix it
// into the base widget class.  (This is a hack, but it's effective.)
dojo.extend(dijit._Widget,{
  dojoAttachEvent: "",
  dojoAttachPoint: "",
  waiRole: "",
  waiState:""
})

}

if(!dojo._hasResource["dijit.form.Form"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit.form.Form"] = true;
dojo.provide("dijit.form.Form");




dojo.declare("dijit.form._FormMixin", null,
  {
  //
  //  summary:
  //    Widget corresponding to HTML form tag, for validation and serialization
  //
  //  example:
  //  | <form dojoType="dijit.form.Form" id="myForm">
  //  |   Name: <input type="text" name="name" />
  //  | </form>
  //  | myObj = {name: "John Doe"};
  //  | dijit.byId('myForm').setValues(myObj);
  //  |
  //  | myObj=dijit.byId('myForm').getValues();

  //  TODO:
  //  * Repeater
  //  * better handling for arrays.  Often form elements have names with [] like
  //  * people[3].sex (for a list of people [{name: Bill, sex: M}, ...])
  //
  //  

    reset: function(){
      dojo.forEach(this.getDescendants(), function(widget){
        if(widget.reset){
          widget.reset();
        }
      });
    },

    validate: function(){
      // summary: returns if the form is valid - same as isValid - but
      //      provides a few additional (ui-specific) features.
      //      1 - it will highlight any sub-widgets that are not
      //        valid
      //      2 - it will call focus() on the first invalid 
      //        sub-widget
      var didFocus = false;
      return dojo.every(dojo.map(this.getDescendants(), function(widget){
        // Need to set this so that "required" widgets get their 
        // state set.
        widget._hasBeenBlurred = true;
        var valid = !widget.validate || widget.validate();
        if (!valid && !didFocus) {
          // Set focus of the first non-valid widget
          dijit.scrollIntoView(widget.containerNode||widget.domNode);
          widget.focus();
          didFocus = true;
        }
        return valid;
      }), "return item;");
    },
    
    setValues: function(/*object*/obj){
      // summary: fill in form values from a JSON structure

      // generate map from name --> [list of widgets with that name]
      var map = { };
      dojo.forEach(this.getDescendants(), function(widget){
        if(!widget.name){ return; }
        var entry = map[widget.name] || (map[widget.name] = [] );
        entry.push(widget);
      });

      // call setValue() or setAttribute('checked') for each widget, according to obj
      for(var name in map){
        var widgets = map[name],            // array of widgets w/this name
          values = dojo.getObject(name, false, obj);  // list of values for those widgets
        if(!dojo.isArray(values)){
          values = [ values ];
        }
        if(typeof widgets[0].checked == 'boolean'){
          // for checkbox/radio, values is a list of which widgets should be checked
          dojo.forEach(widgets, function(w, i){
            w.setValue(dojo.indexOf(values, w.value) != -1);
          });
        }else if(widgets[0]._multiValue){
          // it takes an array (e.g. multi-select)
          widgets[0].setValue(values);
        }else{
          // otherwise, values is a list of values to be assigned sequentially to each widget
          dojo.forEach(widgets, function(w, i){
            w.setValue(values[i]);
          });         
        }
      }

      /***
       *  TODO: code for plain input boxes (this shouldn't run for inputs that are part of widgets)

      dojo.forEach(this.containerNode.elements, function(element){
        if (element.name == ''){return};  // like "continue"  
        var namePath = element.name.split(".");
        var myObj=obj;
        var name=namePath[namePath.length-1];
        for(var j=1,len2=namePath.length;j<len2;++j){
          var p=namePath[j - 1];
          // repeater support block
          var nameA=p.split("[");
          if (nameA.length > 1){
            if(typeof(myObj[nameA[0]]) == "undefined"){
              myObj[nameA[0]]=[ ];
            } // if

            nameIndex=parseInt(nameA[1]);
            if(typeof(myObj[nameA[0]][nameIndex]) == "undefined"){
              myObj[nameA[0]][nameIndex] = { };
            }
            myObj=myObj[nameA[0]][nameIndex];
            continue;
          } // repeater support ends

          if(typeof(myObj[p]) == "undefined"){
            myObj=undefined;
            break;
          };
          myObj=myObj[p];
        }

        if (typeof(myObj) == "undefined"){
          return;   // like "continue"
        }
        if (typeof(myObj[name]) == "undefined" && this.ignoreNullValues){
          return;   // like "continue"
        }

        // TODO: widget values (just call setValue() on the widget)

        switch(element.type){
          case "checkbox":
            element.checked = (name in myObj) &&
              dojo.some(myObj[name], function(val){ return val==element.value; });
            break;
          case "radio":
            element.checked = (name in myObj) && myObj[name]==element.value;
            break;
          case "select-multiple":
            element.selectedIndex=-1;
            dojo.forEach(element.options, function(option){
              option.selected = dojo.some(myObj[name], function(val){ return option.value == val; });
            });
            break;
          case "select-one":
            element.selectedIndex="0";
            dojo.forEach(element.options, function(option){
              option.selected = option.value == myObj[name];
            });
            break;
          case "hidden":
          case "text":
          case "textarea":
          case "password":
            element.value = myObj[name] || "";
            break;
        }
        });
        */
    },

    getValues: function(){
      // summary: generate JSON structure from form values

      // get widget values
      var obj = { };
      dojo.forEach(this.getDescendants(), function(widget){
        var name = widget.name;
        if(!name){ return; }

        // Single value widget (checkbox, radio, or plain <input> type widget
        var value = (widget.getValue && !widget._getValueDeprecated) ? widget.getValue() : widget.value;

        // Store widget's value(s) as a scalar, except for checkboxes which are automatically arrays
        if(typeof widget.checked == 'boolean'){
          if(/Radio/.test(widget.declaredClass)){
            // radio button
            if(value !== false){
              dojo.setObject(name, value, obj);
            }
          }else{
            // checkbox/toggle button
            var ary=dojo.getObject(name, false, obj);
            if(!ary){
              ary=[];
              dojo.setObject(name, ary, obj);
            }
            if(value !== false){
              ary.push(value);
            }
          }
        }else{
          // plain input
          dojo.setObject(name, value, obj);
        }
      });

      /***
       * code for plain input boxes (see also dojo.formToObject, can we use that instead of this code?
       * but it doesn't understand [] notation, presumably)
      var obj = { };
      dojo.forEach(this.containerNode.elements, function(elm){
        if (!elm.name)  {
          return;   // like "continue"
        }
        var namePath = elm.name.split(".");
        var myObj=obj;
        var name=namePath[namePath.length-1];
        for(var j=1,len2=namePath.length;j<len2;++j){
          var nameIndex = null;
          var p=namePath[j - 1];
          var nameA=p.split("[");
          if (nameA.length > 1){
            if(typeof(myObj[nameA[0]]) == "undefined"){
              myObj[nameA[0]]=[ ];
            } // if
            nameIndex=parseInt(nameA[1]);
            if(typeof(myObj[nameA[0]][nameIndex]) == "undefined"){
              myObj[nameA[0]][nameIndex] = { };
            }
          } else if(typeof(myObj[nameA[0]]) == "undefined"){
            myObj[nameA[0]] = { }
          } // if

          if (nameA.length == 1){
            myObj=myObj[nameA[0]];
          } else{
            myObj=myObj[nameA[0]][nameIndex];
          } // if
        } // for

        if ((elm.type != "select-multiple" && elm.type != "checkbox" && elm.type != "radio") || (elm.type=="radio" && elm.checked)){
          if(name == name.split("[")[0]){
            myObj[name]=elm.value;
          } else{
            // can not set value when there is no name
          }
        } else if (elm.type == "checkbox" && elm.checked){
          if(typeof(myObj[name]) == 'undefined'){
            myObj[name]=[ ];
          }
          myObj[name].push(elm.value);
        } else if (elm.type == "select-multiple"){
          if(typeof(myObj[name]) == 'undefined'){
            myObj[name]=[ ];
          }
          for (var jdx=0,len3=elm.options.length; jdx<len3; ++jdx){
            if (elm.options[jdx].selected){
              myObj[name].push(elm.options[jdx].value);
            }
          }
        } // if
        name=undefined;
      }); // forEach
      ***/
      return obj;
    },

    // TODO: ComboBox might need time to process a recently input value.  This should be async?
    isValid: function(){
      // summary: make sure that every widget that has a validator function returns true
      return dojo.every(this.getDescendants(), function(widget){
        return !widget.isValid || widget.isValid();
      });
    }
  });

dojo.declare(
  "dijit.form.Form",
  [dijit._Widget, dijit._Templated, dijit.form._FormMixin],
  {
    // summary:
    // Adds conveniences to regular HTML form

    // HTML <FORM> attributes
    name: "",
    action: "",
    method: "",
    encType: "",
    "accept-charset": "",
    accept: "",
    target: "",

    templateString: "<form dojoAttachPoint='containerNode' dojoAttachEvent='onreset:_onReset,onsubmit:_onSubmit' name='${name}'></form>",

    attributeMap: dojo.mixin(dojo.clone(dijit._Widget.prototype.attributeMap),
      {action: "", method: "", encType: "", "accept-charset": "", accept: "", target: ""}),

    execute: function(/*Object*/ formContents){
      //  summary:
      //    Deprecated: use submit()
    },

    onExecute: function(){
      // summary:
      //    Deprecated: use onSubmit()
    },

    setAttribute: function(/*String*/ attr, /*anything*/ value){
      this.inherited(arguments);
      switch(attr){
        case "encType":
          if(dojo.isIE){ this.domNode.encoding = value; }
      }
    },

    postCreate: function(){
      // IE tries to hide encType
      if(dojo.isIE && this.srcNodeRef && this.srcNodeRef.attributes){
        var item = this.srcNodeRef.attributes.getNamedItem('encType');
        if(item && !item.specified && (typeof item.value == "string")){
          this.setAttribute('encType', item.value);
        }
      }
      this.inherited(arguments);
    },

    onReset: function(/*Event?*/e){ 
      //  summary:
      //    Callback when user resets the form. This method is intended
      //    to be over-ridden. When the `reset` method is called
      //    programmatically, the return value from `onReset` is used
      //    to compute whether or not resetting should proceed
      return true; // Boolean
    },

    _onReset: function(e){
      // create fake event so we can know if preventDefault() is called
      var faux = {
        returnValue: true, // the IE way
        preventDefault: function(){  // not IE
              this.returnValue = false;
            },
        stopPropagation: function(){}, currentTarget: e.currentTarget, target: e.target
      };
      // if return value is not exactly false, and haven't called preventDefault(), then reset
      if(!(this.onReset(faux) === false) && faux.returnValue){
        this.reset();
      }
      dojo.stopEvent(e);
      return false;
    },

    _onSubmit: function(e){
      var fp = dijit.form.Form.prototype;
      // TODO: remove ths if statement beginning with 2.0
      if(this.execute != fp.execute || this.onExecute != fp.onExecute){
        dojo.deprecated("dijit.form.Form:execute()/onExecute() are deprecated. Use onSubmit() instead.", "", "2.0");
        this.onExecute();
        this.execute(this.getValues());
      }
      if(this.onSubmit(e) === false){ // only exactly false stops submit
        dojo.stopEvent(e);
      }
    },
    
    onSubmit: function(/*Event?*/e){ 
      //  summary:
      //    Callback when user submits the form. This method is
      //    intended to be over-ridden, but by default it checks and
      //    returns the validity of form elements. When the `submit`
      //    method is called programmatically, the return value from
      //    `onSubmit` is used to compute whether or not submission
      //    should proceed

      return this.isValid(); // Boolean
    },

    submit: function(){
      // summary:
      //    programmatically submit form if and only if the `onSubmit` returns true
      if(!(this.onSubmit() === false)){
        this.containerNode.submit();
      }
    }
  }
);

}

if(!dojo._hasResource["dijit.Dialog"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit.Dialog"] = true;
dojo.provide("dijit.Dialog");










dojo.declare(
  "dijit.DialogUnderlay",
  [dijit._Widget, dijit._Templated],
  {
    // summary: The component that grays out the screen behind the dialog
  
    // Template has two divs; outer div is used for fade-in/fade-out, and also to hold background iframe.
    // Inner div has opacity specified in CSS file.
    templateString: "<div class='dijitDialogUnderlayWrapper' id='${id}_wrapper'><div class='dijitDialogUnderlay ${class}' id='${id}' dojoAttachPoint='node'></div></div>",

    attributeMap: {},

    postCreate: function(){
      // summary: Append the underlay to the body
      dojo.body().appendChild(this.domNode);
      this.bgIframe = new dijit.BackgroundIframe(this.domNode);
    },

    layout: function(){
      // summary: Sets the background to the size of the viewport
      //
      // description:
      //  Sets the background to the size of the viewport (rather than the size
      //  of the document) since we need to cover the whole browser window, even
      //  if the document is only a few lines long.

      var viewport = dijit.getViewport();
      var is = this.node.style,
        os = this.domNode.style;

      os.top = viewport.t + "px";
      os.left = viewport.l + "px";
      is.width = viewport.w + "px";
      is.height = viewport.h + "px";

      // process twice since the scroll bar may have been removed
      // by the previous resizing
      var viewport2 = dijit.getViewport();
      if(viewport.w != viewport2.w){ is.width = viewport2.w + "px"; }
      if(viewport.h != viewport2.h){ is.height = viewport2.h + "px"; }
    },

    show: function(){
      // summary: Show the dialog underlay
      this.domNode.style.display = "block";
      this.layout();
      if(this.bgIframe.iframe){
        this.bgIframe.iframe.style.display = "block";
      }
      this._resizeHandler = this.connect(window, "onresize", "layout");
    },

    hide: function(){
      // summary: hides the dialog underlay
      this.domNode.style.display = "none";
      if(this.bgIframe.iframe){
        this.bgIframe.iframe.style.display = "none";
      }
      this.disconnect(this._resizeHandler);
    },

    uninitialize: function(){
      if(this.bgIframe){
        this.bgIframe.destroy();
      }
    }
  }
);


dojo.declare("dijit._DialogMixin", null,
  {
    attributeMap: dijit._Widget.prototype.attributeMap,

    // execute: Function
    //  User defined function to do stuff when the user hits the submit button
    execute: function(/*Object*/ formContents){},

    // onCancel: Function
    //      Callback when user has canceled dialog, to notify container
    //      (user shouldn't override)
    onCancel: function(){},

    // onExecute: Function
    //  Callback when user is about to execute dialog, to notify container
    //  (user shouldn't override)
    onExecute: function(){},

    _onSubmit: function(){
      // summary: callback when user hits submit button
      this.onExecute(); // notify container that we are about to execute
      this.execute(this.getValues());
    },

    _getFocusItems: function(/*Node*/ dialogNode){
      // find focusable Items each time a dialog is opened
      var focusItem = dijit.getFirstInTabbingOrder(dialogNode);
      this._firstFocusItem = focusItem ? focusItem : dialogNode;
      focusItem = dijit.getLastInTabbingOrder(dialogNode);
      this._lastFocusItem = focusItem ? focusItem : this._firstFocusItem;
      if(dojo.isMoz && this._firstFocusItem.tagName.toLowerCase() == "input" && dojo.attr(this._firstFocusItem, "type").toLowerCase() == "file"){
          //FF doesn't behave well when first element is input type=file, set first focusable to dialog container
          dojo.attr(dialogNode, "tabindex", "0");
          this._firstFocusItem = dialogNode;
      }
    }
  }
);

dojo.declare(
  "dijit.Dialog",
  [dijit.layout.ContentPane, dijit._Templated, dijit.form._FormMixin, dijit._DialogMixin],
  {
    // summary: A modal dialog Widget
    //
    // description:
    //  Pops up a modal dialog window, blocking access to the screen
    //  and also graying out the screen Dialog is extended from
    //  ContentPane so it supports all the same parameters (href, etc.)
    //
    // example:
    // |  <div dojoType="dijit.Dialog" href="test.html"></div>
    //
    // example:
    // |  <div id="test">test content</div>
    // |  ...
    // |  var foo = new dijit.Dialog({ title: "test dialog" },dojo.byId("test"));
    // |  foo.startup();
    
    templateString: null,
    templateString:"<div class=\"dijitDialog\" tabindex=\"-1\" waiRole=\"dialog\" waiState=\"labelledby-${id}_title\">\r\n\t<div dojoAttachPoint=\"titleBar\" class=\"dijitDialogTitleBar\">\r\n\t<span dojoAttachPoint=\"titleNode\" class=\"dijitDialogTitle\" id=\"${id}_title\">${title}</span>\r\n\t<span dojoAttachPoint=\"closeButtonNode\" class=\"dijitDialogCloseIcon\" dojoAttachEvent=\"onclick: onCancel\">\r\n\t\t<span dojoAttachPoint=\"closeText\" class=\"closeText\">x</span>\r\n\t</span>\r\n\t</div>\r\n\t\t<div dojoAttachPoint=\"containerNode\" class=\"dijitDialogPaneContent\"></div>\r\n</div>\r\n",

    // open: Boolean
    //    is True or False depending on state of dialog
    open: false,

    // duration: Integer
    //    The time in milliseconds it takes the dialog to fade in and out
    duration: 400,

    // refocus: Boolean
    //    A Toggle to modify the default focus behavior of a Dialog, which
    //    is to re-focus the element which had focus before being opened.
    //    False will disable refocusing. Default: true
    refocus: true,

    // _firstFocusItem: DomNode
    //    The pointer to the first focusable node in the dialog
    _firstFocusItem:null,
    
    // _lastFocusItem: DomNode
    //    The pointer to which node has focus prior to our dialog
    _lastFocusItem:null,

    // doLayout: Boolean
    //    Don't change this parameter from the default value.
    //    This ContentPane parameter doesn't make sense for Dialog, since Dialog
    //    is never a child of a layout container, nor can you specify the size of
    //    Dialog in order to control the size of an inner widget. 
    doLayout: false,

    attributeMap: dojo.mixin(dojo.clone(dijit._Widget.prototype.attributeMap),
      {title: "titleBar"}),

    postCreate: function(){
      dojo.body().appendChild(this.domNode);
      this.inherited(arguments);
      var _nlsResources = dojo.i18n.getLocalization("dijit", "common");
      if(this.closeButtonNode){
        this.closeButtonNode.setAttribute("title", _nlsResources.buttonCancel);
      }
      if(this.closeText){
        this.closeText.setAttribute("title", _nlsResources.buttonCancel);
      }
      var s = this.domNode.style;
      s.visibility = "hidden";
      s.position = "absolute";
      s.display = "";
      s.top = "-9999px";

      this.connect(this, "onExecute", "hide");
      this.connect(this, "onCancel", "hide");
      this._modalconnects = [];
    },

    onLoad: function(){
      // summary: when href is specified we need to reposition the dialog after the data is loaded
      this._position();
      this.inherited(arguments);
    },

    _setup: function(){
      // summary: 
      //    stuff we need to do before showing the Dialog for the first
      //    time (but we defer it until right beforehand, for
      //    performance reasons)

      if(this.titleBar){
        this._moveable = new dojo.dnd.TimedMoveable(this.domNode, { handle: this.titleBar, timeout: 0 });
      }

      this._underlay = new dijit.DialogUnderlay({
        id: this.id+"_underlay",
        "class": dojo.map(this["class"].split(/\s/), function(s){ return s+"_underlay"; }).join(" ")
      });

      var node = this.domNode;
      this._fadeIn = dojo.fx.combine(
        [dojo.fadeIn({
          node: node,
          duration: this.duration
         }),
         dojo.fadeIn({
          node: this._underlay.domNode,
          duration: this.duration,
          onBegin: dojo.hitch(this._underlay, "show")
         })
        ]
      );

      this._fadeOut = dojo.fx.combine(
        [dojo.fadeOut({
          node: node,
          duration: this.duration,
          onEnd: function(){
            node.style.visibility="hidden";
            node.style.top = "-9999px";
          }
         }),
         dojo.fadeOut({
          node: this._underlay.domNode,
          duration: this.duration,
          onEnd: dojo.hitch(this._underlay, "hide")
         })
        ]
      );
    },

    uninitialize: function(){
      if(this._fadeIn && this._fadeIn.status() == "playing"){
        this._fadeIn.stop();
      }
      if(this._fadeOut && this._fadeOut.status() == "playing"){
        this._fadeOut.stop();
      }
      if(this._underlay){
        this._underlay.destroy();
      }
    },

    _position: function(){
      // summary: position modal dialog in center of screen
      
      if(dojo.hasClass(dojo.body(),"dojoMove")){ return; }
      var viewport = dijit.getViewport();
      var mb = dojo.marginBox(this.domNode);

      var style = this.domNode.style;
      style.left = Math.floor((viewport.l + (viewport.w - mb.w)/2)) + "px";
      style.top = Math.floor((viewport.t + (viewport.h - mb.h)/2)) + "px";
    },

    _onKey: function(/*Event*/ evt){
      // summary: handles the keyboard events for accessibility reasons
      if(evt.keyCode){
        var node = evt.target;
        if (evt.keyCode == dojo.keys.TAB){
          this._getFocusItems(this.domNode);
        }
        var singleFocusItem = (this._firstFocusItem == this._lastFocusItem);
        // see if we are shift-tabbing from first focusable item on dialog
        if(node == this._firstFocusItem && evt.shiftKey && evt.keyCode == dojo.keys.TAB){
          if(!singleFocusItem){
            dijit.focus(this._lastFocusItem); // send focus to last item in dialog
          }
          dojo.stopEvent(evt);
        }else if(node == this._lastFocusItem && evt.keyCode == dojo.keys.TAB && !evt.shiftKey){
          if (!singleFocusItem){
            dijit.focus(this._firstFocusItem); // send focus to first item in dialog
          }
          dojo.stopEvent(evt);
        }else{
          // see if the key is for the dialog
          while(node){
            if(node == this.domNode){
              if(evt.keyCode == dojo.keys.ESCAPE){
                this.hide(); 
              }else{
                return; // just let it go
              }
            }
            node = node.parentNode;
          }
          // this key is for the disabled document window
          if(evt.keyCode != dojo.keys.TAB){ // allow tabbing into the dialog for a11y
            dojo.stopEvent(evt);
          // opera won't tab to a div
          }else if(!dojo.isOpera){
            try{
              this._firstFocusItem.focus();
            }catch(e){ /*squelch*/ }
          }
        }
      }
    },

    show: function(){
      // summary: display the dialog

      if(this.open){ return; }
      
      // first time we show the dialog, there's some initialization stuff to do     
      if(!this._alreadyInitialized){
        this._setup();
        this._alreadyInitialized=true;
      }

      if(this._fadeOut.status() == "playing"){
        this._fadeOut.stop();
      }

      this._modalconnects.push(dojo.connect(window, "onscroll", this, "layout"));
      this._modalconnects.push(dojo.connect(dojo.doc.documentElement, "onkeypress", this, "_onKey"));

      dojo.style(this.domNode, "opacity", 0);
      this.domNode.style.visibility="";
      this.open = true;
      this._loadCheck(); // lazy load trigger

      this._position();

      this._fadeIn.play();

      this._savedFocus = dijit.getFocus(this);

      // find focusable Items each time dialog is shown since if dialog contains a widget the 
      // first focusable items can change
      this._getFocusItems(this.domNode);

      // set timeout to allow the browser to render dialog
      setTimeout(dojo.hitch(this, function(){
        dijit.focus(this._firstFocusItem);
      }), 50);
    },

    hide: function(){
      // summary: Hide the dialog

      // if we haven't been initialized yet then we aren't showing and we can just return   
      if(!this._alreadyInitialized){
        return;
      }

      if(this._fadeIn.status() == "playing"){
        this._fadeIn.stop();
      }
      this._fadeOut.play();

      if (this._scrollConnected){
        this._scrollConnected = false;
      }
      dojo.forEach(this._modalconnects, dojo.disconnect);
      this._modalconnects = [];
      if(this.refocus){
        this.connect(this._fadeOut,"onEnd",dojo.hitch(dijit,"focus",this._savedFocus));
      }
      this.open = false;
    },

    layout: function() {
      // summary: position the Dialog and the underlay
      if(this.domNode.style.visibility != "hidden"){
        this._underlay.layout();
        this._position();
      }
    },
    
    destroy: function(){
      dojo.forEach(this._modalconnects, dojo.disconnect);
      if(this.refocus && this.open){
        var fo = this._savedFocus;
        setTimeout(dojo.hitch(dijit,"focus",fo),25);
      }
      this.inherited(arguments);      
    }
  }
);

dojo.declare(
  "dijit.TooltipDialog",
  [dijit.layout.ContentPane, dijit._Templated, dijit.form._FormMixin, dijit._DialogMixin],
  {
    // summary:
    //    Pops up a dialog that appears like a Tooltip
    //
    // title: String
    //    Description of tooltip dialog (required for a11Y)
    title: "",

    // doLayout: Boolean
    //    Don't change this parameter from the default value.
    //    This ContentPane parameter doesn't make sense for TooltipDialog, since TooltipDialog
    //    is never a child of a layout container, nor can you specify the size of
    //    TooltipDialog in order to control the size of an inner widget. 
    doLayout: false,

    // _firstFocusItem: DomNode
    //    The pointer to the first focusable node in the dialog
    _firstFocusItem:null,
    
    // _lastFocusItem: DomNode
    //    The domNode that had focus before we took it.
    _lastFocusItem: null,

    templateString: null,
    templateString:"<div class=\"dijitTooltipDialog\" waiRole=\"presentation\">\r\n\t<div class=\"dijitTooltipContainer\" waiRole=\"presentation\">\r\n\t\t<div class =\"dijitTooltipContents dijitTooltipFocusNode\" dojoAttachPoint=\"containerNode\" tabindex=\"-1\" waiRole=\"dialog\"></div>\r\n\t</div>\r\n\t<div class=\"dijitTooltipConnector\" waiRole=\"presenation\"></div>\r\n</div>\r\n",

    postCreate: function(){
      this.inherited(arguments);
      this.connect(this.containerNode, "onkeypress", "_onKey");
      this.containerNode.title = this.title;
    },

    orient: function(/*DomNode*/ node, /*String*/ aroundCorner, /*String*/ corner){
      // summary: configure widget to be displayed in given position relative to the button
      this.domNode.className="dijitTooltipDialog " +" dijitTooltipAB"+(corner.charAt(1)=='L'?"Left":"Right")+" dijitTooltip"+(corner.charAt(0)=='T' ? "Below" : "Above");
    },

    onOpen: function(/*Object*/ pos){
      // summary: called when dialog is displayed
    
      this._getFocusItems(this.containerNode);
      this.orient(this.domNode,pos.aroundCorner, pos.corner);
      this._loadCheck(); // lazy load trigger
      dijit.focus(this._firstFocusItem);
    },
    
    _onKey: function(/*Event*/ evt){
      // summary: keep keyboard focus in dialog; close dialog on escape key
      var node = evt.target;
      if (evt.keyCode == dojo.keys.TAB){
          this._getFocusItems(this.containerNode);
      }
      var singleFocusItem = (this._firstFocusItem == this._lastFocusItem);
      if(evt.keyCode == dojo.keys.ESCAPE){
        this.onCancel();
      }else if(node == this._firstFocusItem && evt.shiftKey && evt.keyCode == dojo.keys.TAB){
        if(!singleFocusItem){
          dijit.focus(this._lastFocusItem); // send focus to last item in dialog
        }
        dojo.stopEvent(evt);
      }else if(node == this._lastFocusItem && evt.keyCode == dojo.keys.TAB && !evt.shiftKey){
        if(!singleFocusItem){
          dijit.focus(this._firstFocusItem); // send focus to first item in dialog
        }
        dojo.stopEvent(evt);
      }else if(evt.keyCode == dojo.keys.TAB){
        // we want the browser's default tab handling to move focus
        // but we don't want the tab to propagate upwards
        evt.stopPropagation();
      }
    }
  } 
);


}

if(!dojo._hasResource["dojox.data.dom"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojox.data.dom"] = true;
dojo.provide("dojox.data.dom");

//DOM type to int value for reference.
//Ints make for more compact code than full constant names.
//ELEMENT_NODE                  = 1;
//ATTRIBUTE_NODE                = 2;
//TEXT_NODE                     = 3;
//CDATA_SECTION_NODE            = 4;
//ENTITY_REFERENCE_NODE         = 5;
//ENTITY_NODE                   = 6;
//PROCESSING_INSTRUCTION_NODE   = 7;
//COMMENT_NODE                  = 8;
//DOCUMENT_NODE                 = 9;
//DOCUMENT_TYPE_NODE            = 10;
//DOCUMENT_FRAGMENT_NODE        = 11;
//NOTATION_NODE                 = 12;

//FIXME:  Remove this file when possible.
//This file contains internal/helper APIs as holders until the true DOM apis of Dojo 0.9 are finalized.
//Therefore, these should not be generally used, they are present only for the use by XmlStore and the
//wires project until proper dojo replacements are available.  When such exist, XmlStore and the like
//will be ported off these and this file will be deleted.
dojo.experimental("dojox.data.dom");

dojox.data.dom.createDocument = function(/*string?*/ str, /*string?*/ mimetype){
  //  summary:
  //    cross-browser implementation of creating an XML document object.
  //
  //  str:
  //    Optional text to create the document from.  If not provided, an empty XML document will be created.
  //  mimetype:
  //    Optional mimetype of the text.  Typically, this is text/xml.  Will be defaulted to text/xml if not provided.
  var _document = dojo.doc;

  if(!mimetype){ mimetype = "text/xml"; }
  if(str && (typeof dojo.global["DOMParser"]) !== "undefined"){
    var parser = new DOMParser();
    return parser.parseFromString(str, mimetype); //  DOMDocument
  }else if((typeof dojo.global["ActiveXObject"]) !== "undefined"){
    var prefixes = [ "MSXML2", "Microsoft", "MSXML", "MSXML3" ];
    for(var i = 0; i<prefixes.length; i++){
      try{
        var doc = new ActiveXObject(prefixes[i]+".XMLDOM");
        if(str){
          if(doc){
            doc.async = false;
            doc.loadXML(str);
            return doc; //  DOMDocument
          }else{
            console.log("loadXML didn't work?");
          }
        }else{
          if(doc){ 
            return doc; //DOMDocument
          }
        }
      }catch(e){ /* squelch */ };
    }
  }else if((_document.implementation)&&
    (_document.implementation.createDocument)){
    if(str){
      if(_document.createElement){
        // FIXME: this may change all tags to uppercase!
        var tmp = _document.createElement("xml");
        tmp.innerHTML = str;
        var xmlDoc = _document.implementation.createDocument("foo", "", null);
        for(var i = 0; i < tmp.childNodes.length; i++) {
          xmlDoc.importNode(tmp.childNodes.item(i), true);
        }
        return xmlDoc;  //  DOMDocument
      }
    }else{
      return _document.implementation.createDocument("", "", null); // DOMDocument
    }
  }
  return null;  //  DOMDocument
}

dojox.data.dom.textContent = function(/*Node*/node, /*string?*/text){
  //  summary:
  //    Implementation of the DOM Level 3 attribute; scan node for text
  //  description:
  //    Implementation of the DOM Level 3 attribute; scan node for text
  //    This function can also update the text of a node by replacing all child 
  //    content of the node.
  //  node:
  //    The node to get the text off of or set the text on.
  //  text:
  //    Optional argument of the text to apply to the node.
  if(arguments.length>1){
    var _document = node.ownerDocument || dojo.doc;  //Preference is to get the node owning doc first or it may fail
    dojox.data.dom.replaceChildren(node, _document.createTextNode(text));
    return text;  //  string
  } else {
    if(node.textContent !== undefined){ //FF 1.5
      return node.textContent;  //  string
    }
    var _result = "";
    if(node == null){
      return _result; //empty string.
    }
    for(var i = 0; i < node.childNodes.length; i++){
      switch(node.childNodes[i].nodeType){
        case 1: // ELEMENT_NODE
        case 5: // ENTITY_REFERENCE_NODE
          _result += dojox.data.dom.textContent(node.childNodes[i]);
          break;
        case 3: // TEXT_NODE
        case 2: // ATTRIBUTE_NODE
        case 4: // CDATA_SECTION_NODE
          _result += node.childNodes[i].nodeValue;
          break;
        default:
          break;
      }
    }
    return _result; //  string
  }
}

dojox.data.dom.replaceChildren = function(/*Element*/node, /*Node || array*/ newChildren){
  //  summary:
  //    Removes all children of node and appends newChild. All the existing
  //    children will be destroyed.
  //  description:
  //    Removes all children of node and appends newChild. All the existing
  //    children will be destroyed.
  //  node:
  //    The node to modify the children on
  //  newChildren:
  //    The children to add to the node.  It can either be a single Node or an
  //    array of Nodes.
  var nodes = [];
  
  if(dojo.isIE){
    for(var i=0;i<node.childNodes.length;i++){
      nodes.push(node.childNodes[i]);
    }
  }

  dojox.data.dom.removeChildren(node);
  for(var i=0;i<nodes.length;i++){
    dojo._destroyElement(nodes[i]);
  }

  if(!dojo.isArray(newChildren)){
    node.appendChild(newChildren);
  }else{
    for(var i=0;i<newChildren.length;i++){
      node.appendChild(newChildren[i]);
    }
  }
}

dojox.data.dom.removeChildren = function(/*Element*/node){
  //  summary:
  //    removes all children from node and returns the count of children removed.
  //    The children nodes are not destroyed. Be sure to call dojo._destroyElement on them
  //    after they are not used anymore.
  //  node:
  //    The node to remove all the children from.
  var count = node.childNodes.length;
  while(node.hasChildNodes()){
    node.removeChild(node.firstChild);
  }
  return count; // int
}


dojox.data.dom.innerXML = function(/*Node*/node){
  //  summary:
  //    Implementation of MS's innerXML function.
  //  node:
  //    The node from which to generate the XML text representation.
  if(node.innerXML){
    return node.innerXML; //  string
  }else if (node.xml){
    return node.xml;    //  string
  }else if(typeof XMLSerializer != "undefined"){
    return (new XMLSerializer()).serializeToString(node); //  string
  }
}


}

if(!dojo._hasResource["ambra.topaz"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["ambra.topaz"] = true;
/*
 * $HeadURL:: http://gandalf.topazproject.org/svn/head/plos/webapp/src/main/webapp/javas#$
 * $Id: ambra.js 5581 2008-05-02 23:01:11Z jkirton $
 *
 * Copyright (c) 2006-2008 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * This file is not a dojo module rather simply contains general utility methods.
 */
 
dojo.provide("ambra.topaz");

document.getElementsByTagAndClassName = function(tagName, className) {
  if ( tagName == null )
    tagName = '*';
   
  var children = document.getElementsByTagName(tagName);
  var elements = new Array();
  
  if ( className == null )
    return children;
  
  for (var i = 0; i < children.length; i++) {
    var child = children[i];
    var classNames = child.className.split(' ');
    for (var j = 0; j < classNames.length; j++) {
      if (classNames[j] == className) {
        elements.push(child);
        break;
      }
    }
  }

  return elements;
}

document.getElementsByTagAndAttributeName = function(tagName, attributeName) {
  if ( tagName == null )
    tagName = '*';
   
  var children = document.getElementsByTagName(tagName);
  var elements = new Array();
  
  if ( attributeName == null )
    return children;
  
  for (var i = 0; i < children.length; i++) {
    var child = children[i];
    if (child.getAttributeNode(attributeName) != null) {
      elements.push(child);
    }
  }

  return elements;
}

document.getElementsByAttributeValue = function(tagName, attributeName, attributeValue) {
  if ( tagName == null )
    tagName = '*';
  else if ( attributeName == null )
    return "[getElementsByAttributeValue] attributeName is required.";
  else if ( attributeValue == null )
    return "[getElementsByAttributeValue] attributeValue is required.";

  var elements = document.getElementsByTagAndAttributeName(tagName, attributeName);
  var elValue = new Array();
  
  for (var i = 0; i < elements.length; i++) {
    var element = elements[i];
    if (element.getAttributeNode(attributeName).nodeValue == attributeValue) {
      elValue.push(element);
    }
  }

  return elValue;
}


/**
 * Extending the String object
 *
 **/
String.prototype.trim = function() {
  return this.replace(/(?:(?:^|\n)\s+|\s+(?:$|\n))/g,"");
}

String.prototype.rtrim = function() {
  return this.replace(/\s+$/,"");
}

String.prototype.ltrim = function() {
  return this.replace(/^\s+/, "");
}

String.prototype.isEmpty = function() {
  return (this == null || this == "");
}

String.prototype.replaceStringArray = function(delimiter, strMatch, newStr) {
  if (!strMatch || !newStr) {
    return "Missing required value";
  }
  
  var strArr = (delimiter) ? this.split(delimiter) : this.split(" ");
  var matchIndexStart = -1;
  var matchIndexEnd = -1;
  for (var i=0; i<strArr.length; i++) {
    if (strArr[i].match(strMatch) != null) {
      if (matchIndexStart < 0)
        matchIndexStart = i;
      
      matchIndexEnd = i;
    }
  }
  
  if (matchIndexEnd >= 0) {
    var diff = matchIndexEnd - matchIndexStart + 1;
    strArr.splice(matchIndexStart, diff, newStr);
  }
  
  var newStr = strArr.join(" ");
  
  return newStr;
}

/**
 * One stop shopping for handling dojo xhr errors.
 * This method is intended to be called from within dojo.xhr 'handle' or 'error' callback methods.
 */
function handleXhrError(response, ioArgs) {
  if(response instanceof Error){
    _ldc.hide();
    if(response.dojoType == "cancel"){
      //The request was canceled by some other JavaScript code.
      console.debug("Request canceled.");
    }else if(response.dojoType == "timeout"){
      //The request took over 5 seconds to complete.
      console.debug("Request timed out.");
    }else{
      //Some other error happened.
      console.error(response);
      if(djConfig.isDebug) alert(response.toSource());
    }
  }
}

}

if(!dojo._hasResource["ambra.domUtil"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["ambra.domUtil"] = true;
/*
 * $HeadURL:: http://gandalf.topazproject.org/svn/head/plos/webapp/src/main/webapp/javas#$
 * $Id: domUtil.js 5581 2008-05-02 23:01:11Z jkirton $
 *
 * Copyright (c) 2006-2008 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
  * ambra.domUtil
  *
  * DOM Utilities.
  *
  * @author  Joycelyn Chung  joycelyn@orangetowers.com
  **/
dojo.provide("ambra.domUtil");

ambra.domUtil = {
  /**
   * ambra.domUtil.getDisplayId(Node obj)
   * 
   * Gets the values of the custom attribute displayId.
   * 
   * @param    obj         Node object    Element node from which to search for the displayId
   * 
   * @return  displayId    String         Display ID
   */
  getDisplayId: function(obj) {
    if (obj.getAttributeNode('displayid')) {
      var displayId = obj.getAttributeNode('displayid').nodeValue;
      return displayId;
    }
    else {
      return null;
    }
  },
  
  /**
   * ambra.domUtil.getAnnotationId(Node obj)
   * 
   * Gets the values of the custom attribute annotationId.
   * 
   * @param    obj            Node object    Element node from which to search for the annotationId.
   * 
   * @return  annotationId    String         Annotation ID
   */
  getAnnotationId: function(obj) {
    if (obj.getAttributeNode('annotationid') != null) {
      var annotationId = obj.getAttributeNode('annotationid').nodeValue;
      return annotationId;
    }
    else {
      return null;
    }
  },
  
  /**
   * ambra.domUtil.ISOtoJSDate(Date ISO_DT)
   * 
   * Converts ISO date formats to a javascript date format.
   * 
   * @param    ISO_DT      Date    ISO date.
   * 
   * @return   newDate     Date    JS date.
   */
  ISOtoJSDate: function (ISO_DT) {
     var temp = ISO_DT.split(/^(....).(..).(..).(..).(..).(..).*$/);
  
     var newDate = new Date();
     newDate.setUTCFullYear(temp[1], temp[2]-1, temp[3]);
     newDate.setUTCHours(temp[4]);
     newDate.setUTCMinutes(temp[5]);
     newDate.setUTCSeconds(temp[6]);
  
    //alert (newDate);
    return newDate;
  },
  
  /**
   * ambra.domUtil.removeChildNodes(Node obj)
   * 
   * Removes the child nodes of the node object that was passed in.
   * 
   * @param    obj          Node    Element node.
   */
   /*
  removeChildNodes: function(obj) {
    if (obj.hasChildNodes()) {
      //alert("obj has child nodes");
      for (var i=0; i<obj.childNodes.length; i++) {
        alert(childNodes[i].hasChildNodes);
        if (obj.removeChild) {
          obj.removeChild(childNodes[i]);
        }
        else {
          obj.childNodes[i].removeNode(true);
        }
      }
    }
  },
  */
  
  removeChildren: function(/*Element*/node){
    //  summary:
    //    removes all children from node and returns the count of children removed.
    //    The children nodes are not destroyed. Be sure to call destroyNode on them
    //    after they are not used anymore.
    var count = node.childNodes.length;
    while(node.hasChildNodes()){ this.removeNode(node.firstChild); }
    return count; // int
  },
  
  moveChildren: function(/*Element*/srcNode, /*Element*/destNode, /*boolean?*/trim){
    //  summary:
    //    Moves children from srcNode to destNode and returns the count of
    //    children moved; will trim off text nodes if trim == true
    var count = 0;
    if(trim) {
      while(srcNode.hasChildNodes() &&
        srcNode.firstChild.nodeType == dojo.dom.TEXT_NODE) {
        srcNode.removeChild(srcNode.firstChild);
      }
      while(srcNode.hasChildNodes() &&
        srcNode.lastChild.nodeType == dojo.dom.TEXT_NODE) {
        srcNode.removeChild(srcNode.lastChild);
      }
    }
    while(srcNode.hasChildNodes()){
      destNode.appendChild(srcNode.firstChild);
      count++;
    }
    return count; //  number
  },
  
  copyChildren: function(/*Element*/srcNode, /*Element*/destNode, /*boolean?*/trim){
    //  summary:
    //    Copies children from srcNde to destNode and returns the count of
    //    children copied; will trim off text nodes if trim == true
    var clonedNode = srcNode.cloneNode(true);
    return this.moveChildren(clonedNode, destNode, trim); //  number
  },
  
  /**
   * ambra.domUtil.getDisplayMap(Node obj, String/Arraylist displayId)
   * 
   * Parses the list of displayId(s).  Based on each displayId, searches for the matching ID in
   * the custom attribute annotationId.  Takes the displayId, the list of elements that has the
   * matching ID in the annotationId, and the count of the matching elements and puts it into an
   * object list.
   * 
   * @param    obj              Node                Element node
   * @param    displayId        String/Arraylist    displayId attribute value
   * 
   * @return   elDisplayList    Arraylist           List of displayId, matching element nodes, and
   *                                                 count.
   */
  getDisplayMap: function(obj, displayId) {
    var displayIdList = (displayId != null) ? [displayId] : ambra.domUtil.getDisplayId(obj).split(',');
    
    //alert("displayId = " + displayId + "\n" +
    //      "displayIdList = " + displayIdList);
    
    var annoteEl = document.getElementsByTagAndAttributeName('span', 'annotationid');
    var elDisplayList = new Array();
    
    // Based on the list of displayId from the element object, figure out which element 
    // has an annotationId list in which there's an annotation id that matches the 
    // display id.
    for (var i=0; i<displayIdList.length; i++) {
      var elAttrList = new Array();
      for (var n=0; n<annoteEl.length; n++) {
        var attrList = this.getAnnotationId(annoteEl[n]).split(',');
        
        for (var x=0; x<attrList.length; x++) {
          if(attrList[x] == displayIdList[i]) {
            elAttrList.push(annoteEl[n]);
          }
        }
      }
      
      elDisplayList.push({'displayId': displayIdList[i],
                          'elementList': elAttrList,
                          'elementCount': elAttrList.length});
    }
    
    return elDisplayList;
  },
  
  /**
   * ambra.domUtil.addNewClass(String sourceClass, String newClass, Node el)
   * 
   * Searches for elements with the sourceClass and adds the newClass to each of them.
   * 
   * @param    sourceClass  String    Existing class name for reference.
   * @param    newClass     String    New class name to be added.
   * @param    el           Node      Element node from which to search.
   */
  addNewClass: function (sourceClass, newClass, el) {
    var elObj = (el) ? el : null;
    var elList = document.getElementsByTagAndClassName(elObj, sourceClass);
    
    for (var i=0; i<elList.length; i++) {
       dojo.addClass(elList[i], newClass);
    }
  },
  
  /**
   * ambra.domUtil.swapClassNameBtwnSibling(Node obj, String tagName, String classNameValue)
   * 
   * Finds sibling elements that has a classNameValue that matches and removes them.  
   * Then adds the className to the source node object.
   * 
   * @param    obj              Node      Element node
   * @param    tagName          String    Element name to search.
   * @param    classNameValue   String    Class name to match.
   */
  swapClassNameBtwnSibling: function (obj, tagName, classNameValue) {
    var parentalNode = obj.parentNode;
    var siblings = parentalNode.getElementsByTagName(tagName);
    
    for (var i=0; i<siblings.length; i++) {
      if (siblings[i].className.match(classNameValue)){
        dojo.removeClass(siblings[i], classNameValue);   
      }
    }
    
    dojo.addClass(obj, classNameValue);
  },

  /**
   * ambra.domUtil.swapAttributeByClassNameForDisplay(Node obj, String triggerClass, String displayId)
   * 
   * Using the source node obj and the displayId, get a list of displayIds mapped to elements that has 
   * a matching ID in its annotationId attribute node.  Using this list of maps, iterate through to find
   * the element with the matching triggerClass.  Remove the triggerClass from the matching element.
   * Add it to the source node obj.
   * 
   * @param    obj            Node      Element node.
   * @param    triggerClass   String    ClassName string used for matching.
   * @param    displayId      String    DisplayId string also used for matching.
   */
  swapAttributeByClassNameForDisplay: function (obj, triggerClass, displayId) {
    var elements = this.getDisplayMap(obj, displayId);
    
    for (var i=0; i<elements.elementCount; i++) {
      if (elements.elementList[i].className.match(triggerClass)) {
        var strRegExp = new RegExp(triggerClass);
        elements.elementList[i].className = elements.elementList[i].className.replace(strRegExp, "");
      }
    }
    
    dojo.addClass(obj, triggerClass);
  },
  
  /**
   * ambra.domUtil.getCurrentOffset(Node obj)
   * 
   * Gets the offset of the node obj from it's parent.
   * 
   * @param    obj          Node        Element node.
   * 
   * @return   offset      Integer      Offset of the node obj.
   */
  getCurrentOffset: function(obj) {
    var curleft = curtop = 0;
    if (obj.offsetParent) {
      curleft = obj.offsetLeft
      curtop = obj.offsetTop
      while (obj = obj.offsetParent) {
        curleft += obj.offsetLeft  
        curtop += obj.offsetTop
      }
    }

    var offset = new Object();
    offset.top = curtop;
    offset.left = curleft;
    
    return offset;
  },

  /**
   * ambra.domUtil.getFirstAncestorByClass(Node selfNode, String ancestorClassName)
   * 
   * Gets the parent node of selfNode that has the matching classname to ancestorClassName.
   * 
   * @param    selfNode            Node object      Element node.
   * @param    ancestorClassName   String           Classname string.
   * 
   * @return   parentalNode        Node object      Parent node that has the matching
   *                                                 ancestorClassName.
   */
  getFirstAncestorByClass: function ( selfNode, ancestorClassName ) {
    var parentalNode = selfNode;
    
    while ( parentalNode.className.search(ancestorClassName) < 0) {
      parentalNode = parentalNode.parentNode;
    }
    
    return parentalNode;
  },
  
  /**
   * ambra.domUtil.swapDisplayMode(Node obj, String state)
   * 
   * Swaps the display mode of node obj between "block" and "none".
   * 
   * @param    obj          Node object    Element node.
   * @param    state        String         Optional value if there's a particular state that 
   *                                        the obj needs to be in.
   *                                        "block" = Show obj.
   *                                        "none"  = Hide obj.
   * 
   * @return   false        boolean        If obj is a link, prevents the link from fowarding.
   */
  swapDisplayMode: function(objId, state) {
    var obj = dojo.byId(objId);
    
    if (state && (state == "block" || state == "none")) 
      obj.style.display = state;
    else if(obj.style.display != "block")
      obj.style.display = "block";
    else
      obj.style.display = "none";
      
    return false;
  },
  
  /**
   * ambra.domUtil.swapDisplayTextMode(Node node, String objId, String state, String textOn, String textOff)
   * 
   * Swaps the display mode of node object based on the objId between "block" and "none".  
   * Also changes the text of node object node to the text specified in textOn and textOff.
   * 
   * @param    node        Node object       Element source node that triggers this method.
   * @param    objId       String            Id of remote element that you want to show or hide.
   * @param    state       String            Specific state the remote element needs to be in.
   * @param    textOn      String            Text string for the source node when the remote
   *                                          element is shown.
   * @param    textOff     String            Text string for the source node when the remote
   *                                          element is hidden.
   * 
   * @return   false       boolean           If node is a link, prevents the link from fowarding.
   */
  swapDisplayTextMode: function(node, objId, state, textOn, textOff) {
    var obj = dojo.byId(objId);
    
    if (state) 
      obj.style.display = state;
    else if(obj.style.display != "block")
      obj.style.display = "block";
    else
      obj.style.display = "none";
      
    if (obj.style.display == "block")
      dojox.data.dom.textContent(node, textOn);
    else
      dojox.data.dom.textContent(node, textOff);
      
    return false;
  },
  
  /**
   * ambra.domUtil.removeExtraSpaces(String text)
   * 
   * Removes all extra spaces, carriage returns, and line feeds and replacing them with 
   * one space.
   * 
   * @param    text     String    Text string to be modified.
   * 
   * @return  <text>    String   Modified text string.
   */
  removeExtraSpaces: function(text) {
    //alert("text = '" + text + "'");
    return text.replace(/([\r\n]+\s+)/g," ");
  },
  
  /**
   * ambra.domUtil.getChildElementsByTagAndClassName(Node node, String tagName, String className)
   * 
   * Looks through the child nodes of node object node for matching tagName and/or
   * matching className.
   * 
   * @param    node        Node object    Element node.
   * @param    tagName     String         Text string of the tag name to search for.
   * @param    className   String         Text string of the class name to search for.
   * 
   * @return   children    Array          Array of child nodes of node if neither the tag name or
   *                                       class name are passed in.
   * @return   elements    Array          Array of element nodes that matches the tag name and/or
   *                                       class name.
   */
  getChildElementsByTagAndClassName: function(node, tagName, className) {
    var children = node.childNodes;
    var elements = new Array();
    tagName = tagName.toUpperCase();
    
    //alert("node = " + node + "\ntagName = " + tagName + "\nclassName = " + className);
    
    if ( className != null || tagName != null) {
      //alert("children = " + children.length);

      for (var i = 0; i < children.length; i++) {
        var child = children[i];
        
        if (tagName != null) {
        
          //alert("child.nodeName.match(tagName) = " + child.nodeName.match(tagName));
          
          if (child.nodeName.match(tagName)) {
            if (className != null) {
              var classNames = child.className.split(' ');
              for (var j = 0; j < classNames.length; j++) {
                if (classNames[j] == className) {
                  elements.push(child);
                  break;
                }
              }
            }
            else {
              elements.push(child);
            }
          }
        }
        else if (className != null){
          var classNames = child.className.split(' ');
          for (var j = 0; j < classNames.length; j++) {
            if (classNames[j] == className) {
              elements.push(child);
              break;
            }
          }
        }
      }
    }
    else {
      return children;
    }    

    //alert("elements = " + elements);
    return elements;
  },

  /**
   * ambra.domUtil.adjustContainerHeight(Node obj)
   * 
   * Adjusts the height of node object obj.
   * 
   * @param    obj          Node    Element node.
   */
  adjustContainerHeight: function (obj) {
    // get size viewport
    var viewport = dijit.getViewport();
    
    // get the offset of the container
    var objOffset = this.getCurrentOffset(obj);
    
    // find the size of the container
    var objMb = dojo._getMarginBox(obj);

    var maxContainerHeight = viewport.h - (10 * objOffset.t);
    //alert("objOffset.top = " + objOffset.top + "\nviewport.h = " + viewport.h + "\nmaxContainerHeight = " + maxContainerHeight);
    
    obj.style.height = maxContainerHeight + "px";
    obj.style.overflow = "auto";
  },
  
  /**
   * ambra.domUtil.setContainerWidth(Node obj, Integer minWidth, Integer maxWidth, Integer variableWidth)
   * 
   * Resets the size of the node object obj based on minWidth, maxWidth and variableWidth.
   * 
   * @param    obj            Node object    Element node.
   * @param    minWidth       Integer        Minimum width.
   * @param    maxWidth       Integer        Maximum width.
   * @param    variableWidth  Integer        Variable width.
   */
  setContainerWidth: function (obj, minWidth, maxWidth, variableWidth /* if the container between min and max */) {
    var viewport = dijit.getViewport();
    
    // min-width: 675px; max-width: 910px;
    obj.style.width = (minWidth && viewport.w < minWidth) ? minWidth + "px" : 
                      (maxWidth && viewport.w > maxWidth) ? maxWidth + "px" :
                      (!variableWidth && viewport.w < maxWidth) ? maxWidth + "px" : "auto" ;
    //alert("container.style.width = " + obj.style.width);
  },
  
  /**
   * ambra.domUtil.removeNode(Node node, boolean deep)
   * 
   * Removes a node.  If deep is set to true, the children of the node object node is 
   * also removed.  
   * 
   * @param    node     Node object   Element node from which to search for the displayId
   * @param    deep     boolean       Is set to true, the children of the node is also 
   *   removed.
   */
  removeNode: function(node, /* boolean */ deep) {
    if (deep && node.hasChildNodes)
      this.removeChildren(node);
      
    if(node && node.parentNode){
      // return a ref to the removed child
      return node.parentNode.removeChild(node); //Node
    }
  },
  
  replaceNode: function(/*Element*/node, /*Element*/newNode){
    //  summary:
    //    replaces node with newNode and returns a reference to the removed node.
    //    To prevent IE memory leak, call destroyNode on the returned node when
    //    it is no longer needed.
    return node.parentNode.replaceChild(newNode, node); // Node
  },

  insertBefore: function(/*Node*/node, /*Node*/ref, /*boolean?*/force){
    //  summary:
    //    Try to insert node before ref
    if( (force != true)&&
      (node === ref || node.nextSibling === ref)){ return false; }
    var parent = ref.parentNode;
    parent.insertBefore(node, ref);
    return true;  //  boolean
  },

  insertAfter: function(/*Node*/node, /*Node*/ref, /*boolean?*/force){
    //  summary:
    //    Try to insert node after ref
    var pn = ref.parentNode;
    if(ref == pn.lastChild){
      if((force != true)&&(node === ref)){
        return false; //  boolean
      }
      pn.appendChild(node);
    }else{
      return this.insertBefore(node, ref.nextSibling, force); //  boolean
    }
    return true;  //  boolean
  },
  
  /**
   * ambra.domUtil.insertAfterLast(Node srcNode, Node refNode)
   * 
   * Inserts the srcNode after the refNode.
   * 
   * @param    srcNode     Node object   Element node source.
   * @param    refNode     Node object   Element node reference.
   */
  insertAfterLast: function(srcNode, refNode) {
    if (refNode.hasChildNodes) 
      this.insertAfter(srcNode, refNode[refNode.childNodes.length-1]);
    else
      refNode.appendChild(srcNode);
  },

  /**
   * ambra.domUtil.insertBeforeFirst(Node srcNode, Node refNode)
   * 
   * Inserts the srcNode before the refNode.
   * 
   * @param    srcNode     Node object   Element node source.
   * @param    refNode     Node object   Element node reference.
   */
  insertBeforeFirst: function(srcNode, refNode) {
    if (refNode.hasChildNodes) 
      this.insertBefore(srcNode, refNode[0]);
    else
      refNode.appendChild(srcNode);
  },
  
  /**
   * ambra.domUtil.modifyNodeChildClassname(Node node, String targetNodeName, String className, boolean isAdd)
   * 
   * Modifies the class name of child nodes of node.  The child nodes must match the targetNodeName.  If isAdd
   * is set to true, the className is added.  Otherwise, the className is removed.
   * 
   * @param    node             Node object    Element node source.
   * @param    targetNodeName   String         Text string of the name of the node that will be target of the 
   *                                            search.
   * @param    classNameString  Text           string of the class name that will either be added or removed.
   * @param    isAdd            boolean        If set to true, the class name will be added to the child elements'
   *                                            class attribute.  Otherwise, the class name will be removed.
   * 
   * @return  false             boolean        The node has no child nodes.
   * @return  true              boolean        The node does have child nodes and the modification was successful.
   */
  modifyNodeChildClassname: function(/* Node */node, /* String */targetNodeName, /* String */className, /* Boolean */isAdd) {
    if (node.hasChildNodes) {
      var nodeChildren = node.childNodes;
  
      for (var i=0; i<nodeChildren.length; i++) {
        if (nodeChildren[i].nodeName == targetNodeName) {
          if (isAdd) {
            dojo.addClass(nodeChildren[i], className);
          }
          else {
            dojo.removeClass(nodeChildren[i], className);
          }
        }
        
        if (nodeChildren[i].hasChildNodes) {
          this.modifyNodeChildClassname(nodeChildren[i], refNodeName, className, isAdd);
        }
      }
      
      return true;
    }
    else {
      return false;
    }
  },
  
  /**
   * ambra.domUtil.isClassNameExist(Node node, String className)
   * 
   * Determines whether the className exists in the class attribute of node.
   * 
   * @param    node        Node object   Element node.
   * @param className String  Text string of the class name.
   * 
   * @return   falsebooleanNo match found.
   * @return truebooleanMatch found.
   */
  isClassNameExist: function(node, className) {
    var classArray = new Array();
    classArray = node.className.split(" ");
    
    for (var i=0; i<classArray.length; i++) {
      if (classArray[i] == className) 
        return true;
    }
    
    return false;
  },
  
  /**
   * ambra.domUtil.isChildContainAttributeValue(Node node, String attributeName, String attributeValue)
   * 
   * Determines whether the child nodes contain an attributeName attribute and, if specified, is equal
   * to attributeValue.
   * 
   * @param    node            Node object    Element node
   * @param    attributeName   String         Text string of the attribute name to search.
   * @param    attributeValue  String         If specified, text string of an attribute value to search.
   * 
   * @return   itemsFound      Array          A collection of element nodes that matches the criteria.
   */
  isChildContainAttributeValue: function (node, attributeName, attributeValue) {
    var childlist = node.childNodes;
    var itemsFound = new Array();
    
    console.debug("[ambra.domUtil.isChildContainAttributeValue]");

    for (var i=0; i<=childlist.length-1; i++) {
      var attrObj = new Object();

      console.debug("attributeValue = " + attributeValue + "\nchildlist[" + i + "].nodeName = " + childlist[i].nodeName); 
      
      if (childlist[i].nodeType == 1 &&
          (((attributeValue || attributeValue !=null) && childlist[i].getAttribute(attributeName) == attributeValue) ||
          ((!attributeValue || attributeValue == null) && childlist[i].getAttributeNode(attributeName) != null))) {

        console.debug("\nchildlist[" + i + "].getAttribute(" + attributeName + ") = " + childlist[i].getAttribute(attributeName));

        attrObj.node = childlist[i];
        attrObj.value = childlist[i].getAttribute(attributeName);
        itemsFound.push(attrObj);
      }
    }
    
    return itemsFound;
  },
  
  /**
   * ambra.domUtil.firstSibling(Node siblingObj)
   * 
   * Finds the first sibling of siblingObj.
   * 
   * @param    siblingObj    Node object    Element node
   * 
   * @return   fSibling      Node object    Element node that is the first sibling of siblingObj.
   */
  firstSibling: function(siblingObj) {
    var fSibling;
    for (var current = siblingObj; current != null; current = current.previousSibling) {
      if (current.nodeName == "#text" && (current.nodeValue.match(new RegExp("\n")) || current.nodeValue.match(new RegExp("\r")))) {
        continue;
      }
      
      fSibling = current;
    }
    
    return fSibling;
  },
  
  /**
   * firstDescendantTextNode
   *
   * Recursively drills into the given node's children searching for the "nearest" text node.
   * Used to "textualize" element based user selections.
   *
   * @param node A dom node
   * @return first encountered text node or null if no text node found
   */
  firstDescendantTextNode: function(node) {
    if(!node) return null;
    var dtn, c, cn = node.childNodes;
    for(var i = 0; i < cn.length; i++) {
      c = cn[i];
      if(c.nodeType == 3) return c;
      else if(c.nodeType == 1) {
        if(dtn = this.firstDescendantTextNode(c)) return dtn;
      }
    }
    return null;
  },
  
  /**
   * lastDescendantTextNode
   *
   * Recursively drills into the given node's children searching for the "furthest" text node.
   * Used to "textualize" element based user selections.
   *
   * @param node A dom node
   * @return first encountered text node that is furthest away or null if no text node found
   */
  lastDescendantTextNode: function(node) {
    if(!node) return null;
    var dtn, c, cn = node.childNodes;
    for(var i = cn.length-1; i >=0; i--) {
      c = cn[i];
      if(c.nodeType == 3) return c;
      else if(c.nodeType == 1) {
        if(dtn = this.lastDescendantTextNode(c)) return dtn;
      }
    }
    return null;
  },
  
  /**
   * findTextNode
   *
   * Recursively searches the given node and contained nodes returning either the nearest or furthest text node.  
   * If the given node itself is a text node, this node is returned.
   * Used to "textualize" element based user selections.
   *
   * @param node A dom node
   * @param first (boolean) When true, the nearest text node is sought
   * @return The first encountered text node or null if no text node found
   * @see firstDescendantTextNode
   * @see lastDescendantTextNode
   */
  findTextNode: function(n, first) {
    if(n.nodeType == 3) return n;
    var t = first ? this.firstDescendantTextNode(n) : this.lastDescendantTextNode(n);
    return t ? t : n;
  }  
}

}

if(!dojo._hasResource["ambra.htmlUtil"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["ambra.htmlUtil"] = true;
/*
 * $HeadURL:: http://gandalf.topazproject.org/svn/head/plos/webapp/src/main/webapp/javas#$
 * $Id: htmlUtil.js 5581 2008-05-02 23:01:11Z jkirton $
 *
 * Copyright (c) 2006-2008 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * ambra.htmlUtil
 * 
 * Utility to help get the attribute from the url to be used in the JS.
 * 
 **/
dojo.provide("ambra.htmlUtil");
ambra.htmlUtil = {
  getQuerystring: function() {
    var paramQuery = unescape(document.location.search.substring(1));
    var paramArray = paramQuery.split("&");
    
    var queryArray = new Array();
    
    for (var i=0;i<paramArray.length;i++) {
      var pair = paramArray[i].split("=");
      
      queryArray.push({param: pair[0], value: pair[1]});
    }     
    
    return queryArray;
  }  
}  

}

if(!dojo._hasResource["ambra.formUtil"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["ambra.formUtil"] = true;
/*
 * $HeadURL:: http://gandalf.topazproject.org/svn/head/plos/webapp/src/main/webapp/javas#$
 * $Id: formUtil.js 5581 2008-05-02 23:01:11Z jkirton $
 *
 * Copyright (c) 2006-2008 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * ambra.formUtil
 * 
 * @author    Joycelyn Chung      joycelyn@orangetowers.com
 **/
dojo.provide("ambra.formUtil");
ambra.formUtil = {
  
  /**
   * ambra.formUtil.textCues
   * 
   * This turns the form field cues on and off.  It also resets them.  It takes the form object
   * and the text string for each state.
   * 
   * @param    formEl     Form object     Form object
   * @param    textCue    String          Text string to be displayed or removed.  Must match the
   *                                      string that's currently in the field.
   */
  textCues: {
    on: function ( formEl, textCue ) {
    if (formEl.value == "")
      formEl.value = textCue;
    },
    
    off: function ( formEl, textCue ) {
      if (formEl.value == textCue)
        formEl.value = "";
    },
    
    reset: function ( formEl, textCue ){
      formEl.value = textCue;
    }
  },
  
  /**
   * ambra.formUtil.toggleFieldsByClassname(String toggleClassOn, String toggleClassOff)
   * 
   * Toggles all elements with a class attribute containing toggleClassOn/toggleClassOff on and off,
   * respectively.
   * 
   * @param     toggleClassOn     String        Text string of the class name that will be toggled on.
   * @param     toggleClassOff    String        Text string of the class name that will be toggled off.
   */
  toggleFieldsByClassname: function ( toggleClassOn, toggleClassOff ) {
    var targetElOn = document.getElementsByTagAndClassName(null, toggleClassOn);
    var targetElOff = document.getElementsByTagAndClassName(null, toggleClassOff);

    for (var i=0; i<targetElOn.length; i++) {
      targetElOn[i].style.display = "block";
    }

    for (var i=0; i<targetElOff.length; i++) {
      targetElOff[i].style.display = "none";
    }
  },
  
  /**
   * ambra.formUtil.checkFieldStrLength(Form field  fieldObj, Integer maxLength)
   * 
   * Checks the specified fieldObj value exceeds the maxLength.
   * 
   * @param     fieldObj    Form field object     A form input object.
   * @param     maxLength   Integer               Maximum length the value can be.
   * 
   * @return    -1          Integer               The field value did not exceed the maxLength.
   * @return     0          Integer               The field value has exceeded the maxLength.
   */
  checkFieldStrLength: function ( fieldObj, maxLength ) {
    if(fieldObj.value && fieldObj.value.length > maxLength) {
      alert("Your comment exceeds the allowable limit of " + maxLength + " characters by " + (fieldObj.value.length - maxLength) + " characters.");
      fieldObj.focus();
      return 0;
    }
    else {
      return -1;
    }
  },
  
  /**
   * ambra.formUtil.disableFormFields(Form formObj)
   * 
   * Method goes through all the elements of the form object formObj and disables all
   * except hidden fields.
   * 
   * @param     formObj     Form object       Form object.
   */
  disableFormFields: function (formObj) {
    for (var i=0; i<formObj.elements.length; i++) {
      if (formObj.elements[i].type != 'hidden') {
        formObj.elements[i].disabled = true;
      } 
    }
  },
  
  /**
   * ambra.formUtil.enableFormFields(Form formObj)
   * 
   * Method goes through all the elements of the form object formObj and enables all
   * except hidden fields.
   * 
   * @param     formObj     Form object       Form object.
   */
  enableFormFields: function (formObj) {
    for (var i=0; i<formObj.elements.length; i++) {
      if (formObj.elements[i].type != 'hidden') {
        formObj.elements[i].disabled = false;
      } 
    }
  },
  
  /**
   * ambra.formUtil.createHiddenFields(Form formObj)
   * 
   * Method goes through the form object formObj looking for elements other than hidden fields,
   * buttons and submits and where the names are not null.  The elements that does meet the 
   * criteria has a hidden counterpart created and attached at the end of the form.  
   */
/*  createHiddenFields: function (formObj) {
    for (var i=0; i<formObj.elements.length; i++) {
      if(formObj.elements[i].type != 'hidden' && 
         formObj.elements[i].type != 'button' && 
         formObj.elements[i].type != 'submit' && 
         formObj.elements[i].name != null) {
        if (formObj.elements["hdn" + formObj.elements[i].name] == null) {
          var newHdnEl = document.createElement("input");
          
          newHdnEl.type = "hidden";
          newHdnEl.name = "hdn" + formObj.elements[i].name;
          
          if (formObj.elements[i].type == "radio") {
            var radioName = formObj.elements[i].name;
            
            for (var n=0; n<formObj.elements[radioName].length; n++) {
              if (formObj.elements[radioName][n].checked) {
                newHdnEl.value = formObj.elements[radioName][n].value;

                break;
              }
            }
          }
          else if (formObj.elements[i].type == "checkbox") {
            var checkboxName = formObj.elements[i].name;
            
            for (var n=0; n<formObj.elements[checkboxName].length; n++) {
              if (formObj.elements[checkboxName][n].checked) {
                newHdnEl.value = (newHdnEl.value == "") ? formObj.elements[checkboxName].value : newHdnEl.value + "," + formObj.elements[checkboxName].value;
              }
            }
          }
          else if (formObj.elements[i].type == "select-one") {
            //alert("formObj.elements[" + i + "][" + formObj.elements[i].selectedIndex + "].value = " + formObj.elements[i][formObj.elements[i].selectedIndex].value);
            newHdnEl.value = formObj.elements[i][formObj.elements[i].selectedIndex].value; 
          }
          else {
            newHdnEl.value = formObj.elements[i].value;
          }
    
          formObj.appendChild(newHdnEl);
        }
      }
    }
    
  },
*/  
  
  /**
   * ambra.formUtil.createFormValueObject(Form formObj)
   * 
   * Method goes through the form object and looks for all fields that are not hidden,
   * buttons, or submits.  For all other fields, the field names are stored as keys while
   * their values are stored as values in an associative array.  Most of the values in 
   * the associative arrays are strings with the exception of checkboxes.  Since in a
   * checkbox, you can have more than one value selected.  The value from the checkboxes
   * are stored in an array.  That array is then stored as the value mapped to checkbox
   * fields.
   * 
   * @param     formObj           Form object         Form object
   * 
   * @return    formValueObject   Associative array   Map of all the field names and their values.
   */
  createFormValueObject: function (formObj) {
    var formValueObject = new Object();
    
    for (var i=0; i<formObj.elements.length; i++) {
      if(formObj.elements[i].type != 'hidden' && 
         formObj.elements[i].type != 'button' && 
         formObj.elements[i].type != 'submit' && 
         formObj.elements[i].name != null) {
        
        if (formObj.elements[i].type == "radio") {
          var radioName = formObj.elements[i].name;
          var radioObj = formObj.elements[radioName];
          
          for (var n=0; n<radioObj.length; n++) {
            if (radioObj[n].checked) {
              formValueObject[radioObj[n].name] = radioObj[n].value;
  
              break;
            }
          }
        }
        else if (formObj.elements[i].type == "checkbox") {
          var checkboxName = formObj.elements[i].name;
          var checkboxObj = formObj.elements[checkboxName];
          
          var cbArray = new Array();
          if (checkboxObj.length) {
            for (var n=0; n<checkboxObj.length; n++) {
              if (checkboxObj[n].checked) {
                 cbArray.push(checkboxObj[n].value);
              }
            }
            
            formValueObject[checkboxName] = cbArray;
          }
          else {
            formValueObject[checkboxObj.name] = checkboxObj.value;
          }
        }
        else if (formObj.elements[i].type == "select-one") {
          formValueObject[formObj.elements[i].name] = formObj.elements[i][formObj.elements[i].selectedIndex].value;
        }
        else {
          formValueObject[formObj.elements[i].name] = formObj.elements[i].value;
        }
      }
    }

    return formValueObject;
  },
  
  /**
   * ambra.formUtil.hasFieldChange(Form formObj)
   * 
   * Checks the fields in formObj that are not hidden, buttons, or submits, to see they have changed.
   */
/*  hasFieldChange: function (formObj) {
    var thisChanged = false;
    
    for (var i=0; i<formObj.elements.length; i++) {
      if(formObj.elements[i].type != 'hidden' && 
         formObj.elements[i].type != 'button' && 
         formObj.elements[i].type != 'submit' && 
         formObj.elements[i].name != null) {
        
        var hdnFieldName = "hdn" + formObj.elements[i].name;
        
        //alert("formObj.elements[" + hdnFieldName + "] = " + formObj.elements[hdnFieldName]);
        
        if (formObj.elements[hdnFieldName] != null) {
          
          //alert("formObj.elements[" + i + "].type = " + formObj.elements[i].type);
          if (formObj.elements[i].type == "radio") {
            var radioName = formObj.elements[i].name;
            
            for (var n=0; n<formObj.elements[radioName].length; n++) {
              if (formObj.elements[radioName][n].checked) {
                alert("formObj.elements[" + radioName + "][" + n + "].value = " + formObj.elements[radioName][n].value + "\n" +
                      "formObj.elements[" + hdnFieldName + "].value = " + formObj.elements[hdnFieldName].value);
                if (formObj.elements[radioName][n].value != formObj.elements[hdnFieldName].value) {
                  thisChanged = true;
                  break;
                }
              }
            }
          }
          else if (formObj.elements[i].type == "checkbox") {
            var checkboxName = formObj.elements[i].name;
            
            var hdnCheckboxList = formObj.elements[hdnFieldName].value.split(",");
            
            for (var n=0; n<formObj.elements[checkboxName].length; n++) {
              if (formObj.elements[checkboxName][n].checked) {
                var isCheckedPreviously = false;
                
                for (var p=0; p<hdnCheckboxList; p++) {
                  if (formObj.elements[checkboxName][n].value == hdnCheckboxList[p])
                    isCheckedPreviously = true;
                }
                
                alert("isCheckedPreviously = " + isCheckedPreviously);
                if (!isCheckedPreviously) {
                  thisChanged = true;
                  break;
                }
              }
            }
          }
          else if (formObj.elements[i].type == "select-one") {
            alert("formObj.elements[" + i + "][" + formObj.elements[i].selectedIndex + "].value = " + formObj.elements[i][formObj.elements[i].selectedIndex].value + "\n" +
                  "formObj.elements[" + hdnFieldName + "].value = " + formObj.elements[hdnFieldName].value);
            if (formObj.elements[hdnFieldName].value != formObj.elements[i][formObj.elements[i].selectedIndex].value) {
              thisChanged = true; 
              break;
            }
          }
          else {
            alert("formObj.elements[" + i + "].value = " + formObj.elements[i].value + "\n" +
                  "formObj.elements[" + hdnFieldName + "].value = " + formObj.elements[hdnFieldName].value);
            if (formObj.elements[hdnFieldName].value != formObj.elements[i].value) {
              thisChanged = true;
              break;
            }
          }
        }
      }
    }
    
    //alert("thisChanged = " + thisChanged);
    
    return thisChanged;
  },

  removeHiddenFields: function (formObj) {
    alert("removeHiddenFields");
    for (var i=0; i<formObj.elements.length; i++) {
      if (formObj.elements[i].type == 'hidden') {
        ambra.domUtil.removeNode(formObj.elements[i]);
      }
    }
  },

  addItemInArray: function (array, item) {
    var foundItem = false;
    for (var i=0; i<array.length; i++) {
      alert("array[" + i + "] = " + array[i] + "\n" +
            "item = " + item);
      if (array[i] == item) 
        foundItem = true;
    }
    
    alert("foundItem = " + foundItem);
    
    if (!foundItem)
      array.push(item);
  },
  
  isItemInArray: function (array, item) {
    var foundItem = false;
    for (var i=0; i<array.length; i++) {
      if (array[i] == item) 
        foundItem = true;
    }
    
    if (foundItem)
      return true;
    else
      return false;
  },
*/  
  
  /**
   * ambra.formUtil.selectAllCheckboxes(Form field  srcObj, Form field  targetCheckboxObj)
   * 
   * If the form field srcObj has been selected, all the checkboxes in the form field targetCheckboxObj
   * gets selected.  When srcObj is not selected, all the checkboxes in targetCheckboxObj gets
   * deselected.
   * 
   * @param     srcObj              Form field        Checkbox field.
   * @param     targetCheckboxObj   Form field        Checkbox field.
   */
  selectAllCheckboxes: function (srcObj, targetCheckboxObj) {
    if (srcObj.checked) {
      for (var i=0; i<targetCheckboxObj.length; i++) {
        targetCheckboxObj[i].checked = true;
      }
    }
    else {
      for (var i=0; i<targetCheckboxObj.length; i++) {
        targetCheckboxObj[i].checked = false;
      }
    }
  },
  
  /**
   * ambra.formUtil.selectCheckboxPerCollection(Form field  srcObj, Form field  collectionObj)
   * 
   * Checks to see if all of the checkboxes in the collectionObj are selected.  If it is, select srcObj
   * also.  If all of the checkboxes in collectionObj are not selected, deselect srcObj.
   * 
   * @param     srcObj              Form field        Checkbox field.
   * @param     targetCheckboxObj   Form field        Checkbox field.
   */
  selectCheckboxPerCollection: function (srcObj, collectionObj) {
    var count = 0;
    
    for (var i=0; i<collectionObj.length; i++) {
      if (collectionObj[i].checked)
        count++;
    }
    
    srcObj.checked = (count == collectionObj.length) ? true : false;
  }
}

}

if(!dojo._hasResource["dojo.io.iframe"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojo.io.iframe"] = true;
dojo.provide("dojo.io.iframe");

dojo.io.iframe = {
  create: function(/*String*/fname, /*String*/onloadstr, /*String?*/uri){
    //  summary:
    //    Creates a hidden iframe in the page. Used mostly for IO
    //    transports.  You do not need to call this to start a
    //    dojo.io.iframe request. Just call send().
    //  fname: String
    //    The name of the iframe. Used for the name attribute on the
    //    iframe.
    //  onloadstr: String
    //    A string of JavaScript that will be executed when the content
    //    in the iframe loads.
    //  uri: String
    //    The value of the src attribute on the iframe element. If a
    //    value is not given, then dojo/resources/blank.html will be
    //    used.
    if(window[fname]){ return window[fname]; }
    if(window.frames[fname]){ return window.frames[fname]; }
    var cframe = null;
    var turi = uri;
    if(!turi){
      if(dojo.config["useXDomain"] && !dojo.config["dojoBlankHtmlUrl"]){
        console.debug("dojo.io.iframe.create: When using cross-domain Dojo builds,"
          + " please save dojo/resources/blank.html to your domain and set djConfig.dojoBlankHtmlUrl"
          + " to the path on your domain to blank.html");
      }
      turi = (dojo.config["dojoBlankHtmlUrl"]||dojo.moduleUrl("dojo", "resources/blank.html"));
    }
    var ifrstr = dojo.isIE ? '<iframe name="'+fname+'" src="'+turi+'" onload="'+onloadstr+'">' : 'iframe';
    cframe = dojo.doc.createElement(ifrstr);
    with(cframe){
      name = fname;
      setAttribute("name", fname);
      id = fname;
    }
    dojo.body().appendChild(cframe);
    window[fname] = cframe;
  
    with(cframe.style){
      if(dojo.isSafari < 3){
        //We can't change the src in Safari 2.0.3 if absolute position. Bizarro.
        position = "absolute";
      }
      left = top = "1px";
      height = width = "1px";
      visibility = "hidden";
    }

    if(!dojo.isIE){
      this.setSrc(cframe, turi, true);
      cframe.onload = new Function(onloadstr);
    }

    return cframe;
  },

  setSrc: function(/*DOMNode*/iframe, /*String*/src, /*Boolean*/replace){
    //summary:
    //    Sets the URL that is loaded in an IFrame. The replace parameter
    //    indicates whether location.replace() should be used when
    //    changing the location of the iframe.
    try{
      if(!replace){
        if(dojo.isSafari){
          iframe.location = src;
        }else{
          frames[iframe.name].location = src;
        }
      }else{
        // Fun with DOM 0 incompatibilities!
        var idoc;
        if(dojo.isIE || dojo.isSafari > 2){
          idoc = iframe.contentWindow.document;
        }else if(dojo.isSafari){
          idoc = iframe.document;
        }else{ //  if(d.isMozilla){
          idoc = iframe.contentWindow;
        }
  
        //For Safari (at least 2.0.3) and Opera, if the iframe
        //has just been created but it doesn't have content
        //yet, then iframe.document may be null. In that case,
        //use iframe.location and return.
        if(!idoc){
          iframe.location = src;
          return;
        }else{
          idoc.location.replace(src);
        }
      }
    }catch(e){ 
      console.debug("dojo.io.iframe.setSrc: ", e); 
    }
  },

  doc: function(/*DOMNode*/iframeNode){
    //summary: Returns the document object associated with the iframe DOM Node argument.
    var doc = iframeNode.contentDocument || // W3
      (
        (
          (iframeNode.name) && (iframeNode.document) && 
          (document.getElementsByTagName("iframe")[iframeNode.name].contentWindow) &&
          (document.getElementsByTagName("iframe")[iframeNode.name].contentWindow.document)
        )
      ) ||  // IE
      (
        (iframeNode.name)&&(document.frames[iframeNode.name])&&
        (document.frames[iframeNode.name].document)
      ) || null;
    return doc;
  },

  /*=====
  dojo.io.iframe.__ioArgs = function(kwArgs){
    //  summary:
    //    All the properties described in the dojo.__ioArgs type, apply
    //    to this type. The following additional properties are allowed
    //    for dojo.io.iframe.send():
    //  method: String?
    //    The HTTP method to use. "GET" or "POST" are the only supported
    //    values.  It will try to read the value from the form node's
    //    method, then try this argument. If neither one exists, then it
    //    defaults to POST.
    //  handleAs: String?
    //    Specifies what format the result data should be given to the
    //    load/handle callback. Valid values are: text, html, javascript,
    //    json. IMPORTANT: For all values EXCEPT html, The server
    //    response should be an HTML file with a textarea element. The
    //    response data should be inside the textarea element. Using an
    //    HTML document the only reliable, cross-browser way this
    //    transport can know when the response has loaded. For the html
    //    handleAs value, just return a normal HTML document.  NOTE: xml
    //    or any other XML type is NOT supported by this transport.
    //  content: Object?
    //    If "form" is one of the other args properties, then the content
    //    object properties become hidden form form elements. For
    //    instance, a content object of {name1 : "value1"} is converted
    //    to a hidden form element with a name of "name1" and a value of
    //    "value1". If there is not a "form" property, then the content
    //    object is converted into a name=value&name=value string, by
    //    using dojo.objectToQuery().
  }
  =====*/

  send: function(/*dojo.io.iframe.__ioArgs*/args){
    //summary: function that sends the request to the server.
    //This transport can only process one send() request at a time, so if send() is called
    //multiple times, it will queue up the calls and only process one at a time.
    if(!this["_frame"]){
      this._frame = this.create(this._iframeName, dojo._scopeName + ".io.iframe._iframeOnload();");
    }

    //Set up the deferred.
    var dfd = dojo._ioSetArgs(
      args,
      function(/*Deferred*/dfd){
        //summary: canceller function for dojo._ioSetArgs call.
        dfd.canceled = true;
        dfd.ioArgs._callNext();
      },
      function(/*Deferred*/dfd){
        //summary: okHandler function for dojo._ioSetArgs call.
        var value = null;
        try{
          var ioArgs = dfd.ioArgs;
          var dii = dojo.io.iframe;
          var ifd = dii.doc(dii._frame);
          var handleAs = ioArgs.handleAs;

          //Assign correct value based on handleAs value.
          value = ifd; //html
          if(handleAs != "html"){
            value = ifd.getElementsByTagName("textarea")[0].value; //text
            if(handleAs == "json"){
              value = dojo.fromJson(value); //json
            }else if(handleAs == "javascript"){
              value = dojo.eval(value); //javascript
            }
          }
        }catch(e){
          value = e;
        }finally{
          ioArgs._callNext();       
        }
        return value;
      },
      function(/*Error*/error, /*Deferred*/dfd){
        //summary: errHandler function for dojo._ioSetArgs call.
        dfd.ioArgs._hasError = true;
        dfd.ioArgs._callNext();
        return error;
      }
    );

    //Set up a function that will fire the next iframe request. Make sure it only
    //happens once per deferred.
    dfd.ioArgs._callNext = function(){
      if(!this["_calledNext"]){
        this._calledNext = true;
        dojo.io.iframe._currentDfd = null;
        dojo.io.iframe._fireNextRequest();
      }
    }

    this._dfdQueue.push(dfd);
    this._fireNextRequest();
    
    //Add it the IO watch queue, to get things like timeout support.
    dojo._ioWatch(
      dfd,
      function(/*Deferred*/dfd){
        //validCheck
        return !dfd.ioArgs["_hasError"];
      },
      function(dfd){
        //ioCheck
        return (!!dfd.ioArgs["_finished"]);
      },
      function(dfd){
        //resHandle
        if(dfd.ioArgs._finished){
          dfd.callback(dfd);
        }else{
          dfd.errback(new Error("Invalid dojo.io.iframe request state"));
        }
      }
    );

    return dfd;
  },

  _currentDfd: null,
  _dfdQueue: [],
  _iframeName: dojo._scopeName + "IoIframe",

  _fireNextRequest: function(){
    //summary: Internal method used to fire the next request in the bind queue.
    try{
      if((this._currentDfd)||(this._dfdQueue.length == 0)){ return; }
      var dfd = this._currentDfd = this._dfdQueue.shift();
      var ioArgs = dfd.ioArgs;
      var args = ioArgs.args;

      ioArgs._contentToClean = [];
      var fn = dojo.byId(args["form"]);
      var content = args["content"] || {};
      if(fn){
        if(content){
          // if we have things in content, we need to add them to the form
          // before submission
          for(var x in content){
            if(!fn[x]){
              var tn;
              if(dojo.isIE){
                tn = dojo.doc.createElement("<input type='hidden' name='"+x+"'>");
              }else{
                tn = dojo.doc.createElement("input");
                tn.type = "hidden";
                tn.name = x;
              }
              tn.value = content[x];
              fn.appendChild(tn);
              ioArgs._contentToClean.push(x);
            }else{
              fn[x].value = content[x];
            }
          }
        }
        //IE requires going through getAttributeNode instead of just getAttribute in some form cases, 
        //so use it for all.  See #2844
        var actnNode = fn.getAttributeNode("action");
        var mthdNode = fn.getAttributeNode("method");
        var trgtNode = fn.getAttributeNode("target");
        if(args["url"]){
          ioArgs._originalAction = actnNode ? actnNode.value : null;
          if(actnNode){
            actnNode.value = args.url;
          }else{
            fn.setAttribute("action",args.url);
          }
        }
        if(!mthdNode || !mthdNode.value){
          if(mthdNode){
            mthdNode.value= (args["method"]) ? args["method"] : "post";
          }else{
            fn.setAttribute("method", (args["method"]) ? args["method"] : "post");
          }
        }
        ioArgs._originalTarget = trgtNode ? trgtNode.value: null;
        if(trgtNode){
          trgtNode.value = this._iframeName;
        }else{
          fn.setAttribute("target", this._iframeName);
        }
        fn.target = this._iframeName;
        fn.submit();
      }else{
        // otherwise we post a GET string by changing URL location for the
        // iframe
        var tmpUrl = args.url + (args.url.indexOf("?") > -1 ? "&" : "?") + ioArgs.query;
        this.setSrc(this._frame, tmpUrl, true);
      }
    }catch(e){
      dfd.errback(e);
    }
  },

  _iframeOnload: function(){
    var dfd = this._currentDfd;
    if(!dfd){
      this._fireNextRequest();
      return;
    }

    var ioArgs = dfd.ioArgs;
    var args = ioArgs.args;
    var fNode = dojo.byId(args.form);
  
    if(fNode){
      // remove all the hidden content inputs
      var toClean = ioArgs._contentToClean;
      for(var i = 0; i < toClean.length; i++) {
        var key = toClean[i];
        if(dojo.isSafari < 3){
          //In Safari (at least 2.0.3), can't use form[key] syntax to find the node,
          //for nodes that were dynamically added.
          for(var j = 0; j < fNode.childNodes.length; j++){
            var chNode = fNode.childNodes[j];
            if(chNode.name == key){
              dojo._destroyElement(chNode);
              break;
            }
          }
        }else{
          dojo._destroyElement(fNode[key]);
          fNode[key] = null;
        }
      }
  
      // restore original action + target
      if(ioArgs["_originalAction"]){
        fNode.setAttribute("action", ioArgs._originalAction);
      }
      if(ioArgs["_originalTarget"]){
        fNode.setAttribute("target", ioArgs._originalTarget);
        fNode.target = ioArgs._originalTarget;
      }
    }

    ioArgs._finished = true;
  }
}

}

if(!dojo._hasResource["ambra.widget.RegionalDialog"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["ambra.widget.RegionalDialog"] = true;
/*
 * $HeadURL:: http://gandalf.topazproject.org/svn/head/plos/webapp/src/main/webapp/javas#$
 * $Id: RegionalDialog.js 5581 2008-05-02 23:01:11Z jkirton $
 *
 * Copyright (c) 2006-2008 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
dojo.provide("ambra.widget.RegionalDialog");





// summary
//  Mixin for widgets implementing a modal dialog
dojo.declare(
  "ambra.widget.RegionalDialogBase", 
  [dijit.Dialog],
{
  _changeTipDirection: function(isTipDown, xShift) {
    var dTip = this.tipDownNode;
    var dTipu = this.tipUpNode;
    
    dTip.className = dTip.className.replace(/\son/, "");
    dTipu.className = dTipu.className.replace(/\son/, ""); 
    
    var targetTip = (isTipDown) ? dTip : dTipu;
    
    targetTip.className = targetTip.className.concat(" on");

    //if (dojo.isIE < 7) 
    //  targetTip.style.marginLeft = (xShift) ? xShift + "px" : "auto";
    //else
    targetTip.style.left = (xShift) ? xShift + "px" : "auto";
  },
    
  placeModalDialog: function() {
    var docscroll = dojo._docScroll();
    var viewport = dijit.getViewport();
    var markerOffset = ambra.domUtil.getCurrentOffset(this.markerNode);
    var mb = dojo.marginBox(this.containerNode);

    /*
    console.debug('RegionalDialog. placeModalDialog(): '
    + '\ndocscroll x:' + docscroll.x + ',y:' + docscroll.y 
    + '\nviewport w:' + viewport.w + ',h:' + viewport.h
    + '\nmarkerOffset left:' + markerOffset.left+ ',top:' + markerOffset.top
    + '\nmb.w:' + mb.w+ ',h:' + mb.h);
    */
    
    var mbWidth = mb.w;
    var mbHeight = mb.h;
    var vpWidth = viewport.w;
    var vpHeight = viewport.h;
    var scrollX = docscroll.x;
    var scrollY = docscroll.y;
    
    // The height of the tip.
    var tipHeight = 22;
    
    // The width of the tip.
    var tipWidth = 39;
    
    // The minimum distance from the left edge of the dialog box to the left edge of the tip.
    var tipMarginLeft = 22;
    
    // The minimum distance from the right edge of the dialog box to the right edge of the tip.
    var tipMarginRight = 22;
    
    // The minimum distance from either side edge of the dialog box to the corresponding side edge of the viewport.
    var mbMarginX = 10;
    
    // The minimum distance from the top or bottom edge of the dialog box to the top or bottom, respectively, of the viewport.
    var mbMarginY = 10;

    // The height of the bug. This is used when the tip points up to figure out how far down to push everything.
    var bugHeight = 15;

    // The minimum x-offset of the dialog box top-left corner, relative to the page.
    var xMin = scrollX + mbMarginX;
    
    // The minimum y-offset of the dialog box top-left corner, relative to the page.
    var yMin = scrollY + mbMarginY;
    
    // The maximum x-offset of the dialog box top-left corner, relative to the page.
    var xMax = scrollX + vpWidth - mbMarginX - mbWidth;

    // The maximum y-offset of the dialog box top-left corner, relative to the page.
    var yMax = scrollY + vpHeight - mbMarginY - mbHeight;
    
    // The minimum x-offset of the tip left edge, relative to the page.
    var xTipMin = xMin + tipMarginLeft;

    // The maximum x-offset of the tip left edge, relative to the page.
    var xTipMax = xMax + mbWidth - tipMarginRight - tipWidth;

    // True if the tip is pointing down (the default)
    var tipDown = true;

    // Sanity check to make sure that the viewport is large enough to accomodate the dialog box, the tip, and the minimum margins
    if (xMin > xMax || yMin > yMax || xTipMin > xTipMax) {
      // TODO handle this!
    }
    
    // Default values put the box generally above and to the right of the annotation "bug"
    var xTip = markerOffset.left - (tipWidth / 2);
    var yTip = markerOffset.top - tipHeight - (tipHeight/4);
    
    var x = xTip - tipMarginLeft;
    var y = yTip - mbHeight;

    // If the box is too far to the left, try sliding it over to the right. The tip will slide with it, and thus no longer be pointing directly to the bug.
    if (x < xMin) {
      x = xMin;
      if (xTip < xTipMin) {
        xTip = xTipMin;
      }
    }
    // If the box is too far to the right, slide it over to the left, but leave the tip in the same place if possible.
    else if (x > xMax) {
      x = xMax;
      if (xTip > xTipMax) {
        xTip = xTipMax;
      }
    }

    // If the box is too far up, flip it over and put it below the annotation.
    if (y < yMin) {
      tipDown = false; // flip the tip

      yTip = markerOffset.top + bugHeight - (tipHeight/4);
      y = yTip + tipHeight;
      
      if (y > yMax) {
        // this is bad, because it means that there isn't enough room above or below the annotation for the dialog box, the tip, and/or the minimum margins
      }
    }
    
    var xTipDiff = markerOffset.left - x;
    
    if(xTipDiff < tipMarginLeft) {
      xTipPos = tipMarginLeft - (tipWidth / 4);
      x = x - (tipMarginLeft - xTipDiff);
    }
    else {
      xTipPos = xTipDiff - (tipWidth / 4);
      //x = x - (tipMarginLeft - xTipDiff);
    }
          
    this._changeTipDirection(tipDown, xTipPos);

    with(this.domNode.style){
      left = x + "px";
      top = y + "px";
    }

    console.debug("RegionalDialogBase.placeModalDialog: \nleft = " + this.domNode.style.left + "\ntop = "  + this.domNode.style.top);
  },
    
  // override position as we want control over dialog placement!
  _position: function() {
    console.debug("RegionalDialogBase._position");
    //this.inherited(arguments);
    this.placeModalDialog();
  }
});

dojo.declare(
  "ambra.widget.RegionalDialog",
  [ambra.widget.RegionalDialogBase],
{
  templateString:"<div id=\"${id}\" class=\"dojoDialog\" dojoAttachPoint=\"wrapper\"><div dojoAttachPoint=\"containerNode\" style=\"position: relative; z-index: 2;\"></div></div>\r\n",
  
  /*
  postMixInProperties: function(){
    //ambra.widget.RegionalDialog.superclass.postMixInProperties.apply(this, arguments);
    this.inherited(arguments);
  },
  
  postCreate: function(){
    //ambra.widget.RegionalDialog.superclass.postCreate.apply(this, arguments);
    //ambra.widget.RegionalDialogBase.prototype.postCreate.apply(this, arguments);
    this.inherited(arguments);
  },
  */
  
  show: function() {
    console.debug('RegionalDialog.show()');
    this.inherited(arguments);
    window.scrollTo(0, ambra.domUtil.getCurrentOffset(this.domNode).top);
    this.placeModalDialog();
  },
  
  // onscroll hook
  layout: function() {
    console.debug('RegionalDialog.layout()');
    this.inherited(arguments);
  },
  
  onLoad: function(){
    console.debug('RegionalDialog.onLoad()');
    this.inherited(arguments);
    this.placeModalDialog();
  },
  
  setMarker: function(node) {
    // summary
    // when specified is clicked, pass along the marker object
    this.markerNode = node;
  },
  
  setTipUp: function(node) {
    // summary
    // when specified is clicked, pass along the marker object
    this.tipUpNode = node;
  },
  
  setTipDown: function(node) {
    // summary
    // when specified is clicked, pass along the marker object
    this.tipDownNode = node;
  }
});

}

if(!dojo._hasResource["ambra.navigation"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["ambra.navigation"] = true;
/*
 * $HeadURL:: http://gandalf.topazproject.org/svn/head/plos/webapp/src/main/webapp/javas#$
 * $Id: navigation.js 5581 2008-05-02 23:01:11Z jkirton $
 *
 * Copyright (c) 2006-2008 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * ambra.navigation
 * 
 * This class builds the table of content navigation in the right-hand column. 
 **/
dojo.provide("ambra.navigation");
ambra.navigation = {
 buildTOC: function(tocObj){
   var tocEl = document.getElementsByTagAndAttributeName(null, 'toc');
   
   var ul = document.createElement('ul');
   
   for (var i=0; i<tocEl.length; i++) {
     var li = document.createElement('li');
     var anchor = document.createElement('a');
     anchor.href = "#" + tocEl[i].getAttributeNode('toc').nodeValue;
     if (i == tocEl.length -1) {
       anchor.className = 'last';
     }
     var tocText = document.createTextNode(tocEl[i].getAttributeNode('title').nodeValue);
     anchor.appendChild(tocText);
     li.appendChild(anchor);
     
     ul.appendChild(li);
   }
   
   tocObj.appendChild(ul);
 } 
}  

}

if(!dojo._hasResource["ambra.horizontalTabs"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["ambra.horizontalTabs"] = true;
/*
 * $HeadURL:: http://gandalf.topazproject.org/svn/head/plos/libs/js/src/main/scripts/top#$
 * $Id: horizontalTabs.js 5581 2008-05-02 23:01:11Z jkirton $
 *
 * Copyright (c) 2006-2008 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
/**
 * ambra.horizontalTabs
 * 
 * The horizontal tabs are the secondary navigation that can be found on the
 * home page and the profile page.  This class uses a map object set in the 
 * configuration file that will be used for building the tabs.  This map
 * object contains key-value pairs, for example,
 *      tabKey:    "recentlyPublished",
 *      title:     "Recently Published",
 *      className: "published",
 *      urlLoad:   "/article/recentArticles.action",
 *      urlSave:   ""
 * 
 * These values are set for each tab.  Using the setters, initialize the 
 * tab in the page init.js files.
 **/


dojo.provide("ambra.horizontalTabs");

ambra.horizontalTabs = {
  
  proceedFlag:false,
  tempValue:'',
  changeFlag:false,
  
  tabPaneSet: "",
  
  tabsListObject: "",
  
  tabsContainer: "",
  
  targetFormObj: "",
  
  targetObj: "",
  
  newTarget: "",
  
  setTabPaneSet: function (obj) {
    this.tabPaneSet = obj;
  },
  
  setTabsListObject: function (listObj) {
    this.tabsListObject = listObj;
  },
  
  setTabsContainer: function (listObj) {
    this.tabsContainer = listObj;
  },
  
  setTargetFormObj: function (formObj) {
    this.targetFormObj = formObj;
  },
  
  setTargetObj: function (targetObj) {
    this.targetObj = targetObj;
  },
  
  setNewTarget: function (newTarget) {
    this.newTarget = newTarget;
  },
  
  getMapObject: function (value) {
    if (value) {
      for (var i=0; i<this.tabsListObject.length; i++) {
        if (this.tabsListObject[i].tabKey == value)
          return this.tabsListObject[i];
      }
    }
    else {
      return this.tabsListObject[0];
    }
  },
  
  init: function(initId) {
    var targetObj;
    
    if (initId)
      targetObj = this.getMapObject(initId);
    else 
      targetObj = this.getMapObject();
    
    this.buildTabs(targetObj);
    this.tabSetup(targetObj);
    //this.attachFormEvents(formObj);
  },
  
  initSimple: function(initId) {
    var targetObj;
    
    if (initId)
      targetObj = this.getMapObject(initId);
    else 
      targetObj = this.getMapObject();
    
    this.buildTabsHome(targetObj);
    this.setTargetObj(targetObj);
  },
  
  tabSetup: function (targetObj) {
    this.setTargetObj(targetObj);
    
    var formName = this.targetObj.formName;
    var formObj = document.forms[formName];
    
    this.setTargetFormObj(formObj);
    //ambra.formUtil.createHiddenFields(this.targetFormObj);
    
    //alert("formObj.formSubmit = " + formObj.formSubmit.value);
    /*dojo.connect(formObj.formSubmit, "onclick", function() {
        //alert("tabKey = " + ambra.horizontalTabs.targetObj.tabKey);
        submitContent(ambra.horizontalTabs.targetObj);
      }
    );*/
    
    formObj.formSubmit.onclick = function () {
        submitContent();
      }
  },

  setTempValue: function (obj) {
    if (obj.type == "radio") {
      var radioName = obj.name;
      
      var radioObjs = obj.form.elements[radioName];
        
      for (var n=0; n<radioObjs.length; n++) {
        if (radioObjs[n].checked) {
          this.tempValue = radioObjs[n].value;
        }
      }
    }
    else if (obj.type == "checkbox") {
      var checkboxName = obj.name;
      
      var checkboxObjs = obj.form.elements[checkboxName];
      
      if (checkboxObjs.length) {  
        for (var n=0; n<checkboxObjs.length; n++) {
          if (checkboxObjs[n].checked) {
            this.tempValue = checkboxObjs[n].value;
          }
        }
      }
      else {
        this.tempValue = checkboxObjs.checked;
      }
    }
    else if (obj.type == "select-one") {
      //alert("formObj.elements[" + i + "][" + obj.selectedIndex + "].value = " + obj[obj.selectedIndex].value);
      this.tempValue = obj[obj.selectedIndex].value; 
    }
    else {
      this.tempValue = obj.value;
    }
    
    //alert("tempValue = " + tempValue);
  },
  
  checkValue: function (obj) {
    //alert("obj = " + obj.type);
    if (obj.type == "radio") {
      var radioName = obj.name;
      
      var radioObjs = obj.form.elements[radioName];

      //alert("obj.form.elements[" + checkboxName + "].length = " + obj.form.elements[checkboxName].toSource());
      for (var n=0; n<radioObjs.length; n++) {
        if (radioObjs[n].checked) {
          if (this.tempValue != radioObjs[n].value)
            this.changeFlag = true;
        }
      }
    }
    else if (obj.type == "checkbox") {
      var checkboxName = obj.name;
      
      var checkboxObjs = obj.form.elements[checkboxName];
        
      //alert("obj.form.elements[" + checkboxName + "].length = " + obj.form.elements[checkboxName].toSource());
      if (checkboxObjs.length) {
        for (var n=0; n<checkboxObjs.length; n++) {
          if (checkboxObjs[n].checked) {
            if (this.tempValue != checkboxObjs[n].value)
              this.changeFlag = true;
          }
        }
      }
      else {
        if (this.tempValue != checkboxObjs.checked) 
          this.changeFlag = true;
      }
    }
    else if (obj.type == "select-one") {
      //alert("formObj.elements[" + i + "][" + obj.selectedIndex + "].value = " + obj[obj.selectedIndex].value);
      if (this.tempValue != obj[obj.selectedIndex].value)
        this.changeFlag = true;
    }
    else {
      if (this.tempValue != obj.value)
        this.changeFlag = true;
    }
    
    //alert("changeFlag = " + changeFlag);
  },
  
  attachFormEvents: function (formObj) {
    ambra.horizontalTabs.tempValue = "";

    for (var i=0; i<formObj.elements.length; i++) {
      if (formObj.elements[i].type != 'hidden') {
        var formName = formObj.name;
        var fieldName = formObj.elements[i].name;
        //alert("formName = " + formName + "\n" +
        //      "fieldName = " + fieldName);
        dojo.connect(document.forms[formName].elements[fieldName], "onfocus", function() {
        //    alert("tempValue = " + tempValue + "\n" +
        //          "this.id = " + document.forms[formName].elements[fieldName].value);
            ambra.horizontalTabs.tempValue = this.value;
          }  
        );

        dojo.connect(formObj.elements[i], "onchange", function() {
        //    alert("tempValue = " + tempValue + "\n" +
        //          "this.value = " + this.value);
          
            if (ambra.horizontalTabs.tempValue == this.value) 
              ambra.horizontalTabs.changeFlag = true;
          }  
        );
      }
    }
  },
  
  buildTabs: function(obj) {
    for (var i=0; i<this.tabsListObject.length; i++) {
      var li = document.createElement("li");
      li.id = this.tabsListObject[i].tabKey;
      if (obj.className) li.className = obj.className;
      if (this.tabsListObject[i].tabKey == obj.tabKey) {
        //li.className = li.className.concat(" active");
        dojo.addClass(li, "active");
      }
      li.onclick = function () { 
          ambra.horizontalTabs.show(this.id);
          return false; 
        }
      li.appendChild(document.createTextNode(this.tabsListObject[i].title));

      this.tabsContainer.appendChild(li);
    }
    
    this.tempValue = "";
  },
  
  buildTabsHome: function(obj) {
    for (var i=0; i<this.tabsListObject.length; i++) {
      var li = document.createElement("li");
      li.id = this.tabsListObject[i].tabKey;
      if (this.tabsListObject[i].className) li.className = this.tabsListObject[i].className;
      if (this.tabsListObject[i].tabKey == obj.tabKey) {
        //li.className = li.className.concat(" active");
        dojo.addClass(li, "active");
      }
      li.onclick = function () { 
          ambra.horizontalTabs.showHome(this.id);
          return false; 
        }
      var span = document.createElement("span");
      span.appendChild(document.createTextNode(this.tabsListObject[i].title));
      li.appendChild(span);

      this.tabsContainer.appendChild(li);
    }
  },
  
  toggleTab: function(obj) {
    for (var i=0; i<this.tabsListObject.length; i++) {
      var tabNode = dojo.byId(this.tabsListObject[i].tabKey);
      
      if (tabNode.className.match("active"))
        dojo.removeClass(tabNode, "active");
        //tabNode.className = tabNode.className.replace(/active/, "").trim();
    }
    
    var targetNode = dojo.byId(obj.tabKey);
    dojo.addClass(targetNode, "active");
    //targetNode.className = targetNode.className.concat(" active");
  },
  
  confirmChange: function (formObj) {
    //var isChanged = false;
    //isChanged = ambra.formUtil.hasFieldChange(ambra.horizontalTabs.targetFormObj);
   
    //alert("[confirmChange] changeFlag = " + changeFlag);
    if (this.changeFlag) {
      var warning = confirm("You have made changes, are you sure you want to leave this tab without saving?  If you want to proceed, click \"OK\".  Otherwise click \"Cancel\" to go to save.");
      
      this.proceedFlag = warning;
    }
    else {
      this.proceedFlag = true;
    }
  },
    
  getContent: function() {
    if (!this.proceedFlag) {
      _ldc.hide();
  
      this.targetFormObj.formSubmit.focus();
      return false;
    }
    else {
      //ambra.formUtil.removeHiddenFields(this.targetFormObj);
      loadContent(this.newTarget);
    }
  },

  saveContent: function(targetId) {
    var newTarget = this.getMapObject(targetId);
    
    submitContent(newTarget);
  },

  show: function(id) {
    var newTarget = this.getMapObject(id);
    this.setNewTarget(newTarget);
    _ldc.show();
    this.confirmChange();
    
    setTimeout("getContentFunc()", 1000);
  },
  
  showHome: function(id) {
    var newTarget = this.getMapObject(id);
    this.setNewTarget(newTarget);
    
    loadContentHome(newTarget);
  }
  
}  

function getContentFunc () {
  ambra.horizontalTabs.getContent();
}

function loadContent(targetObj) {
  var refreshArea = dojo.byId(profileConfig.tabPaneSetId);
  var targetUri = targetObj.urlLoad + "?tabId=" + targetObj.tabKey;

  _ldc.show();
  dojo.xhrGet({
    url: _namespace + targetUri,
    handleAs:'text',
    headers: { "AJAX_USER_AGENT": "Dojo/" +  dojo.version },
    error: function(response, ioArgs){
      handleXhrError(response, ioArgs);
    },
    load: function(response, ioArgs){
      refreshArea.innerHTML = response;
      ambra.horizontalTabs.toggleTab(targetObj);
      ambra.horizontalTabs.tabSetup(targetObj);
      ambra.horizontalTabs.tempValue = "";
      ambra.horizontalTabs.changeFlag = false;
      ambra.horizontalTabs.proceedFlag = true;
      _ldc.hide();
    }
  });
}  

function loadContentHome(targetObj) {
  var refreshArea = dojo.byId(homeConfig.tabPaneSetId);
  var targetUri = targetObj.urlLoad;

  dojo.xhrGet({
    url: _namespace + targetUri,
    headers: { "AJAX_USER_AGENT": "Dojo/" +  dojo.version },
    error: function(response, ioArgs){
      handleXhrError(response, ioArgs);
    },
    load: function(response, ioArgs){
      refreshArea.innerHTML = response;
      ambra.horizontalTabs.setTargetObj(targetObj);
      ambra.horizontalTabs.toggleTab(targetObj);
    }
  });
}  

function submitContent() {
  var refreshArea = dojo.byId(profileConfig.tabPaneSetId);
  var srcObj = ambra.horizontalTabs.targetObj;
  var targetUri = srcObj.urlSave;
  
  var formObj = document.forms[srcObj.formName];
  var formValueObj = ambra.formUtil.createFormValueObject(formObj);
  
  _ldc.show();
  dojo.xhrPost({
    url: _namespace + targetUri,
    content: formValueObj,
    headers: { "AJAX_USER_AGENT": "Dojo/" +  dojo.version },
    error: function(response, ioArgs){
      handleXhrError(response, ioArgs);
    },
    load: function(response, ioArgs){
      ambra.horizontalTabs.tabSetup(srcObj);
      refreshArea.innerHTML = response;
      ambra.horizontalTabs.tempValue = "";
      ambra.horizontalTabs.changeFlag = false;
      
      var formObj = document.forms[srcObj.formName];
      
      formObj.formSubmit.onclick = function () { submitContent(); }
      
      var errorNodes = document.getElementsByTagAndClassName(null, "form-error");
      
      if (errorNodes.length >= 0)
        jumpToElement(errorNodes[0]);
      else
        jumpToElement(errorNodes);
        
      _ldc.hide();
    }
  });
}  


}

if(!dojo._hasResource["ambra.floatMenu"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["ambra.floatMenu"] = true;
/*
 * $HeadURL:: http://gandalf.topazproject.org/svn/head/plos/webapp/src/main/webapp/javas#$
 * $Id: floatMenu.js 5581 2008-05-02 23:01:11Z jkirton $
 *
 * Copyright (c) 2006-2008 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * ambra.floatMenu
 * 
 * The function is activated when the page is scrolled or the window is resized.
 * "postcomment" is the outer container of the sections that floats.  "floatMarker"
 * is the point that indicates the topmost point that the floated menu should stop
 * floating.  This doesn't work so well in Safari.  The best way to do in Safari is 
 * to have 2 of these items, the other one being postcommentfloat.  This second one
 * is on the page is hidden unless you're in safari.
 * 
 * @author    Joycelyn Chung    joycelyn@orangetowers.com
 **/

dojo.provide("ambra.floatMenu");

ambra.floatMenu = {
  el:null,
  changeHeight:null,
  
  doFloat: function() {
    this.el = dojo.byId('postcomment');
    var marker = dojo.byId('floatMarker');
    if(!marker) return;
    var markerParent = marker.parentNode;
    var mOffset = ambra.domUtil.getCurrentOffset(marker);
    var mpOffset = ambra.domUtil.getCurrentOffset(markerParent);
    var scrollOffset = dojo._docScroll();
    var vpOffset = dijit.getViewport();
  
    var scrollY = scrollOffset.y;
    
    var y = 0;
    if (dojo.isSafari) {
      var floatDiv = dojo.byId("postcommentfloat");
      if (this.el.style.display == "none") {
        floatDiv.style.display = "none";
        this.el.style.display = "block";
      }
    }
    else {
      dojo.removeClass(this.el, 'fixed');
    }
    
    if (scrollY > mOffset.top) {
      y = scrollY - mpOffset.top;
      if (dojo.isSafari) {
        var floatDiv = dojo.byId("postcommentfloat");
        if (floatDiv.style.display = "none") {
          floatDiv.style.display = "block";
          this.el.style.display = "none";
        }
      }
      else {
        dojo.addClass(this.el, 'fixed');
      }
    }
    
    if (dojo.isIE && dojo.isIE < 7 && ((document.body.offsetHeight-scrollY) >= vpOffset.h)) {
      this.changeHeight = y;
      window.setTimeout("ambra.floatMenu.el.style.top = ambra.floatMenu.changeHeight + \"px\";", 100); 
    }
  }
}

dojo.addOnLoad(function() {
  dojo.connect(window, "onscroll", function() {
     ambra.floatMenu.doFloat();
  });
  
  dojo.connect(window, "onresize", function() {
    ambra.floatMenu.doFloat();
  });

  dojo.connect(dojo.doc, "onscroll", function() {
      ambra.floatMenu.doFloat();
  });
  
  dojo.connect(dojo.doc, "onkey", function() {
    ambra.floatMenu.doFloat();
  });
  
  ambra.floatMenu.doFloat();
});

}

if(!dojo._hasResource["ambra.displayComment"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["ambra.displayComment"] = true;
/*
 * $HeadURL:: http://gandalf.topazproject.org/svn/head/plos/webapp/src/main/webapp/javas#$
 * $Id: displayComment.js 5581 2008-05-02 23:01:11Z jkirton $
 *
 * Copyright (c) 2006-2008 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
  * ambra.displayComment
  * 
  * This object builds the dialog that displays the comments for a specific 
  * annotation bug.
  *
  * @author  Joycelyn Chung     joycelyn@orangetowers.com
  *
  **/
dojo.provide("ambra.displayComment");

ambra.displayComment = {
  target: "",
  
  targetSecondary: "",
  
  sectionTitle: "",
  
  sectionDetail: "",
  
  sectionComment: "",
  
  sectionLink: "",
  
  retrieveMsg: "",
  
  init: function() {
    this.sectionTitle   = dojo.byId(commentConfig.sectionTitle);
    this.sectionDetail  = dojo.byId(commentConfig.sectionDetail);
    this.sectionComment = dojo.byId(commentConfig.sectionComment);
    this.sectionLink    = dojo.byId(commentConfig.sectionLink);
    this.retrieveMsg    = dojo.byId(commentConfig.retrieveMsg);    
  },
  
  isMultiple: function(attr) {
    var attrList = this.parseAttributeToList(attr);
    
    return (attrList.length > 1) ? true : false;
  },
  
  setTarget: function(obj) {
    this.target = obj;
  },
  
  setTargetSecondary: function(obj) {
    this.targetSecondary = obj;
  },
  
  setSectionTitle: function(configObj) {
    this.sectionTitle = dojo.byId(configObj.sectionTitle);
  },
  
  setSectionDetail: function(configObj) {
    this.sectionDetail = dojo.byId(configObj.sectionDetail);
  },
  
  setSectionComment: function(configObj) {
    this.sectionComment = dojo.byId(configObj.sectionComment);
  },
  
  setSectionLink: function(configObj) {
    this.sectionLink = dojo.byId(configObj.sectionLink);
  },
  
  setRetrieveMsg: function(configObj) {
    this.retrieveMsg = dojo.byId(configObj.retrieveMsg);
  },
  
  /**
   * ambra.displayComment.show(Node node)
   * 
   * Method that triggers the display of the dialog box.
   * 
   * @param   node    Node      Node where the action was triggered and the
   *                             dialog box will positioned relative to.
   * @return  false   boolean   In the link that triggered this call, sending 
   *                              false back prevents the page from forwarding.     
   */
  show: function(node){
    this.setTarget(node);
    
    _commentDlg.setMarker(this.target);
    _commentMultiDlg.setMarker(this.target);
    getComment(this.target);
    
    return false;
  },

  /**
   * ambra.displayComment.buildDisplayHeader(JSON jsonObj)
   * 
   * Builds the header of the annotation comment display dialog.
   * 
   * @param   jsonObj       JSON object         JSON object containing the data that
   *                                             retrieved from the database.
   * 
   * @return  titleDocFrag  Document fragment   Resulting document fragment created.
   */
  buildDisplayHeader: function (jsonObj) {
    var titleDocFrag = document.createDocumentFragment();
    
    // Insert title link text
    var titleLink = document.createElement('a');
    titleLink.href = _namespace + '/annotation/listThread.action?inReplyTo=' + jsonObj.annotationId + '&root=' + jsonObj.annotationId; 
    titleLink.className = "discuss icon";
    titleLink.title="View full note";
    //alert("jsonObj.annotation.commentTitle = " + jsonObj.annotation.commentTitle);
    titleLink.innerHTML = jsonObj.annotation.commentTitle;
    titleDocFrag.appendChild(titleLink);

    return titleDocFrag;    
  },

  /**
   * ambra.displayComment.buildDisplayDetail(JSON jsonObj)
   * 
   * Builds the details of the annotation comment display dialog.
   * 
   * @param   jsonObj       JSON object         JSON object containing the data that
   *                                             retrieved from the database.
   * 
   * @return  detailDocFrag Document fragment   Resulting document fragment created.
   */
  buildDisplayDetail: function (jsonObj) {
    // Insert creator detail info
    var annotationId = jsonObj.annotationId;
    var tooltipId = annotationId.substring(annotationId.lastIndexOf('/') + 1, annotationId.length);
    //alert("tooltipId = " + tooltipId);
    
    var creatorId = jsonObj.creatorUserName;
    var creatorLink = document.createElement('a');
    creatorLink.href = _namespace + '/user/showUser.action?userId=' + jsonObj.annotation.creator;
//   creatorLink.title = "Annotation Author";
    creatorLink.className = "user icon";
    creatorLink.appendChild(document.createTextNode(creatorId));
    creatorLink.id = tooltipId;
    
/*    var divTooltip = document.createElement('div');
    var dojoType = document.createAttribute('dojoType');
    dojoType.value = "PostTooltip";
    divTooltip.setAttributeNode(dojoType);
    var connectId = document.createAttribute('dojo:connectId');
    connectId.value = tooltipId;
    divTooltip.setAttributeNode(connectId);
    var uniqueId = document.createAttribute('dojo:uniqId');
    uniqueId.value = "tt" + tooltipId;
    divTooltip.setAttributeNode(uniqueId);
    var contentUrl = document.createAttribute('dojo:contentUrl');
    contentUrl.value = _namespace + "/user/displayUserAJAX.action?userId=" + creatorId;
    divTooltip.setAttributeNode(contentUrl);
    var executeScripts = document.createAttribute('dojo:executeScripts');
    executeScripts.value = "true";
    divTooltip.setAttributeNode(executeScripts);
    //var caption = document.createAttribute('dojo:caption');
    //caption.value = "The tooltip";
    //divTooltip.setAttributeNode(caption);*/
    
    var userInfoDiv = document.createElement('div');
    userInfoDiv.className = "userinfo";
    //divTooltip.appendChild(userInfoDiv);
    
    var d = new Date(jsonObj.annotation.createdAsMillis);
    var MONTH_NAMES = new String('JanFebMarAprMayJunJulAugSepOctNovDec');
    var dayInt = d.getUTCDate();
    var day = (dayInt >= 10 ? "" : "0") + dayInt;
    var monthInt = d.getUTCMonth() * 3;
    var month = MONTH_NAMES.substring (monthInt, monthInt + 3);
    var year = d.getUTCFullYear();
    var hrsInt = d.getUTCHours(); 
    var hours = (hrsInt >= 10 ? "" : "0") + hrsInt;
    var minInt = d.getUTCMinutes();
    var minutes = (minInt >= 10 ? "" : "0") + minInt;
    
    var dateStr = document.createElement('strong');
    dateStr.appendChild(document.createTextNode(day + " " + month + " " + year));
    var timeStr = document.createElement('strong');
    timeStr.appendChild(document.createTextNode(hours + ":" + minutes + " GMT"));
    
    var detailDocFrag = document.createDocumentFragment();
    detailDocFrag.appendChild(document.createTextNode('Posted by '));
    detailDocFrag.appendChild(creatorLink);
    detailDocFrag.appendChild(document.createTextNode(' on '));
    detailDocFrag.appendChild(dateStr);
    detailDocFrag.appendChild(document.createTextNode(' at '));
    detailDocFrag.appendChild(timeStr);
    //detailDocFrag.appendChild(divTooltip);
    
    return detailDocFrag;
  },
  
  /**
   * ambra.displayComment.buildDisplayBody(JSON jsonObj)
   * 
   * Builds the comment body of the annotation comment display dialog.
   * 
   * @param   jsonObj       JSON object         JSON object containing the data that
   *                                             retrieved from the database.
   * 
   * @return  commentFrag   Document fragment   Resulting document fragment created.
   */
  buildDisplayBody: function (jsonObj) {
    // Insert formatted comment
    var commentFrag = document.createDocumentFragment();
    commentFrag = jsonObj.annotation.escapedTruncatedComment;
    
    return commentFrag;
  },
  
  /**
   * ambra.displayComment.buildDisplayViewLink(JSON jsonObj)
   * 
   * Builds the link that takes the user to the discussion section.
   * 
   * @param   jsonObj       JSON object         JSON object containing the data that
   *                                             retrieved from the database.
   * 
   * @return  commentLink Document fragment   Resulting document fragment created.
   */
  buildDisplayViewLink: function (jsonObj) {
    var commentLink = document.createElement('a');
    commentLink.href = _namespace + '/annotation/listThread.action?inReplyTo=' + jsonObj.annotationId + '&root=' + jsonObj.annotationId;
    commentLink.className = 'commentary icon';
    commentLink.title = 'Click to view full thread and respond';
    commentLink.appendChild(document.createTextNode('View/respond to this'));
    
    return commentLink;
  },
  
  /**
   * ambra.displayComment.buildDisplayView(JSON jsonObj)
   * 
   * Builds the comment dialog box for a single comment.  Empties out the inner 
   * containers if text already exists in it.
   * 
   * @param   jsonObj       JSON object         JSON object containing the data that
   *                                             retrieved from the database.
   * 
   * @return  <nothing>
   */
  buildDisplayView: function(jsonObj){
    if (ambra.displayComment.sectionTitle.hasChildNodes) ambra.domUtil.removeChildren(ambra.displayComment.sectionTitle);
    ambra.displayComment.sectionTitle.appendChild(this.buildDisplayHeader(jsonObj));
    
    if (ambra.displayComment.sectionDetail.hasChildNodes) ambra.domUtil.removeChildren(ambra.displayComment.sectionDetail);
    ambra.displayComment.sectionDetail.appendChild(this.buildDisplayDetail(jsonObj));

    //alert(commentFrag);
    if (ambra.displayComment.sectionComment.hasChildNodes) ambra.domUtil.removeChildren(ambra.displayComment.sectionComment);
    ambra.displayComment.sectionComment.innerHTML = this.buildDisplayBody(jsonObj);
    //alert("jsonObj.annotation.commentWithUrlLinking = " + jsonObj.annotation.commentWithUrlLinking);
    
    if (ambra.displayComment.sectionLink.hasChildNodes) ambra.domUtil.removeChildren(ambra.displayComment.sectionLink);
    this.sectionLink.appendChild(this.buildDisplayViewLink(jsonObj));
    
    // set correction related styling
    var cmtId = dojo.byId(commentConfig.cmtContainer);
    if(jsonObj.annotation.type.indexOf(annotationConfig.annTypeMinorCorrection) >= 0) {
      // minor correction
      dojo.addClass(cmtId, annotationConfig.styleMinorCorrection);
    }
    else if(jsonObj.annotation.type.indexOf(annotationConfig.annTypeFormalCorrection) >= 0) {
      // formal correction
      dojo.addClass(cmtId, annotationConfig.styleFormalCorrection);
    }
    else {
      dojo.removeClass(cmtId, annotationConfig.styleMinorCorrection);
      dojo.removeClass(cmtId, annotationConfig.styleFormalCorrection);
    }
  },
  
  /**
   * ambra.displayComment.buildDisplayViewMultiple(JSON jsonObj)
   * 
   * Builds the comment dialog box for a multiple comments.  Empties out the inner 
   * containers if text already exists in it.
   * 
   * @param   jsonObj       JSON object         JSON object containing the data that
   *                                             retrieved from the database.
   * 
   * @return  <nothing>
   */
  buildDisplayViewMultiple: function(jsonObj, iter, container, secondaryContainer){
    var newListItem = document.createElement('li');
    
    if (iter <= 0)
      newListItem.className = 'active';
      
    newListItem.appendChild(document.createTextNode(jsonObj.annotation.commentTitle));
    //newListItem.appendChild(this.buildDisplayHeader(jsonObj));
    var detailDiv = document.createElement('div');
    detailDiv.className = 'detail';
    detailDiv.appendChild(this.buildDisplayDetail(jsonObj)); 
    newListItem.appendChild(detailDiv);   
    
    var contentDiv = document.createElement('div');
    if (iter <=0)
      contentDiv.className = 'contentwrap active';
    else
      contentDiv.className = 'contentwrap';

    // set correction related styling
    if(jsonObj.annotation.type.indexOf(annotationConfig.annTypeMinorCorrection) >= 0) {
      // minor correction
      dojo.addClass(newListItem, annotationConfig.styleMinorCorrection);
      contentDiv.className += ' ' + annotationConfig.styleMinorCorrection;
    }
    else if(jsonObj.annotation.type.indexOf(annotationConfig.annTypeFormalCorrection) >= 0) {
      // formal correction
      dojo.addClass(newListItem, annotationConfig.styleFormalCorrection);
      contentDiv.className += ' ' + annotationConfig.styleFormalCorrection;
    }

    contentDiv.innerHTML = this.buildDisplayBody(jsonObj);
    
    var cDetailDiv = document.createElement('div');
    cDetailDiv.className = 'detail';
    /*var commentLink = document.createElement('a');
    commentLink.href = '#';
    commentLink.className = 'commentary icon';
    commentLink.title = 'Click to view full thread and respond';
    commentLink.appendChild(document.createTextNode('View full commentary'));
    
    var responseLink = document.createElement('a');
    responseLink.href = '#';
    responseLink.className = 'respond tooltip';
    responseLink.title = 'Click to respond to this posting';
    responseLink.appendChild(document.createTextNode('Respond to this'));
    
    cDetailDiv.appendChild(commentLink);
    cDetailDiv.appendChild(responseLink);*/
    cDetailDiv.appendChild(this.buildDisplayViewLink(jsonObj));
    contentDiv.appendChild(cDetailDiv);

    if (iter <= 0) {
      container.appendChild(newListItem);
      secondaryContainer.appendChild(contentDiv);
    }
    else {
      var liList = ambra.domUtil.getChildElementsByTagAndClassName(container, 'li', null);
      ambra.domUtil.insertAfter(newListItem, liList[liList.length - 1]);
    
      var divList = ambra.domUtil.getChildElementsByTagAndClassName(secondaryContainer, 'div', null);
      ambra.domUtil.insertAfter(contentDiv, divList[divList.length - 1]);
    }

    var multiDetailDivChild = secondaryContainer.childNodes[secondaryContainer.childNodes.length - 1];
    newListItem.onclick = function() {
        ambra.displayComment.mouseoutComment(ambra.displayComment.target);
        ambra.displayComment.mouseoverComment(ambra.displayComment.target, jsonObj.annotationId);
        ambra.domUtil.swapClassNameBtwnSibling(this, this.nodeName, 'active');
        ambra.domUtil.swapClassNameBtwnSibling(multiDetailDivChild, multiDetailDivChild.nodeName, 'active');
        ambra.domUtil.swapAttributeByClassNameForDisplay(ambra.displayComment.target, ' active', jsonObj.annotationId);
        
        ambra.displayComment.adjustDialogHeight(container, secondaryContainer, 50);
      }

  },
  
  /**
   * ambra.displayComment.mouseoverComment(Node obj, String displayId)
   * 
   * This method gets a map of all element nodes that contain the same display ID
   * and iterates through the map and modifies the classname to show highlight.
   * 
   * @param   obj         Node object       Source element to start highlight.
   * @param   displayId   String            Id reference for display.
   * 
   * @return  <nothing>
   */
  mouseoverComment: function (obj, displayId) {
   var elementList = ambra.domUtil.getDisplayMap(obj, displayId);
   
   // Find the displayId that has the most span nodes containing that has a 
   // corresponding id in the annotationId attribute.  
   var longestAnnotElements;
   for (var i=0; i<elementList.length; i++) {
     if (i == 0) {
       longestAnnotElements = elementList[i];
     }
     else if (elementList[i].elementCount > elementList[i-1].elementCount){
       longestAnnotElements = elementList[i];
     }
   }
   
   //this.modifyClassName(obj);
   
   // the annotationId attribute, modify class name.
   for (var n=0; n<longestAnnotElements.elementCount; n++) {
     var classList = new Array();
     var elObj = longestAnnotElements.elementList[n];

     this.modifyClassName(elObj);
     
     if (n == 0) {
       var bugObj = ambra.domUtil.getChildElementsByTagAndClassName(elObj, 'a', 'bug');
       
       for (var i=0; i<bugObj.length; i++) {
         this.modifyClassName(bugObj[i]);
       }
     }
   }

  },

  /**
   * ambra.displayComment.mouseoutComment(Node obj) 
   * 
   * Resets span tags that were modified to highlight to no highlight.
   * 
   * @param   obj   Node object       Object needed to be reset.
   */
  mouseoutComment: function (obj) {
    var elList = document.getElementsByTagName('span');
    
    for(var i=0; i<elList.length; i++) {
      elList[i].className = elList[i].className.replace(/\-active/, "");
    }
    obj.className = obj.className.replace(/\-active/, "");
  },
  
  /**
   * ambra.displayComment.modifyClassName(Node obj)
   * 
   * Modifies the className
   * 
   * @param   obj   Node object   Source node.
   */
  modifyClassName: function (obj) {
     classList = obj.className.split(" ");
     for (var i=0; i<classList.length; i++) {
       if ((classList[i].match('public') || classList[i].match('private')) && !classList[i].match('-active')) {
         classList[i] = classList[i].concat("-active");
       }
     }
     
     obj.className = classList.join(' ');
  },
  
  /**
   * ambra.displayComment.processBugCount()
   * 
   * Searches the document for tags that has the classname of "bug" indicating
   * that it's an annotation bug.  Looks at the node id which should have a list
   * of IDs corresponding to an annotation.  This ID list is counted and the 
   * result is shown in the bug.
   */
  processBugCount: function () {
    var bugList = document.getElementsByTagAndClassName(null, 'bug');
    
    for (var i=0; i<bugList.length; i++) {
      var bugCount = ambra.domUtil.getDisplayId(bugList[i]);
      var spn = document.createElement('span');

      if (bugCount != null) {
        var displayBugs = bugCount.split(',');
        var count = displayBugs.length;
        var ctText = document.createTextNode(count);
      }
      else {
        var ctText = document.createTextNode('0');
      }
      spn.appendChild(ctText);
      ambra.domUtil.removeChildren(bugList[i]);
      bugList[i].appendChild(spn);
    }
  },
  
  /**
   * ambra.displayComment.adjustDialogHeight(Node container1, Node container2, Integer addPx)
   * 
   * The height of the margin box of container1 and container2 are compared and
   * the height are adjusted accordingly.  
   * 
   * @param   container1    Node object     Container node object.
   * @param   container2    Node object     Container node object.
   * @param   addPx         Integer         Pixel amount to adjust height.
   */
  adjustDialogHeight: function(container1, container2, addPx) {
    var container1Mb = dojo._getMarginBox(container1).height;
    var container2Mb = dojo._getMarginBox(container2).height;
    
    if (container1Mb > container2Mb) {
      container1.parentNode.style.height = (container1Mb + addPx) + "px";
      
      var contentDivs = ambra.domUtil.getChildElementsByTagAndClassName(container2, 'div', 'contentwrap');
      for (var i=0; i<contentDivs.length; i++) {
        if (contentDivs[i].className.match('active')) {
          contentDivs[i].style.height = (container1Mb - container1Mb/(3.59*contentDivs.length)) + "px";
          //contentDivs[i].style.backgroundColor = "#fff";
        }
      }
    }
    else
      container1.parentNode.style.height = (container2Mb + addPx) + "px";
      
    // TODO jpk dojo1.1 - do we need this?
    //_commentMultiDlg.placeModalDialog();
  }
}

function getComment(obj) {
    _ldc.show();
    
    var targetUri = ambra.domUtil.getDisplayId(obj);
          
    var uriArray = targetUri.split(",");

    if (uriArray.length > 1) {
      var targetContainer = document.getElementById('multilist');
      ambra.domUtil.removeChildren(targetContainer);
      var targetContainerSecondary = document.getElementById('multidetail');
      ambra.domUtil.removeChildren(targetContainerSecondary);
    }
    else {
      var targetContainer =  dijit.byId("CommentDialog");
    }
    
    var maxShown = 4;
    var stopPt = (uriArray.length < maxShown) ? uriArray.length : maxShown;
    
    var count = 0;
    
    for (var i=0; i<stopPt; i++) {
      //alert("uriArray[" + i + "] = " + uriArray[i]);
      dojo.xhrGet({
        url: _namespace + "/annotation/getAnnotation.action?annotationId=" + uriArray[i],
        handleAs:'json',
        error: function(response, ioArgs){
          handleXhrError(response, ioArgs);
        },
        load: function(response, ioArgs){
         var jsonObj = response;
         if (jsonObj.actionErrors.length > 0) {
           var errorMsg = "";
           for (var i=0; i<jsonObj.actionErrors.length; i++) {
             errorMsg = errorMsg + jsonObj.actionErrors[i] + "\n";
           }
           alert("ERROR [actionErrors]: " + errorMsg);
         }
         else if (jsonObj.numFieldErrors > 0) {
           var fieldErrors;
           //alert("jsonObj.numFieldErrors = " + jsonObj.numFieldErrors);
           for (var item in jsonObj.fieldErrors) {
             var errorString = "";
             for (var i=0; i<jsonObj.fieldErrors[item].length; i++) {
               errorString += jsonObj.fieldErrors[item][i];
             }
             fieldErrors = fieldErrors + item + ": " + errorString + "<br/>";
           }
           alert("ERROR [numFieldErrors]: " + fieldErrors);
         }
         else {
           var isMulti = (uriArray.length > 1);
           var dlg;
           if(isMulti) {             
             ambra.displayComment.buildDisplayViewMultiple(jsonObj, targetContainer.childNodes.length, targetContainer, targetContainerSecondary);
             
             if (targetContainer.childNodes.length == stopPt) {
               ambra.displayComment.mouseoverComment(ambra.displayComment.target, uriArray[0]);
                
               _commentMultiDlg.show();

               ambra.displayComment.adjustDialogHeight(targetContainer, targetContainerSecondary, 50);
             }
             dlg = _commentMultiDlg;
           }
           else {
             ambra.displayComment.buildDisplayView(jsonObj);
             ambra.displayComment.mouseoverComment(ambra.displayComment.target);
             _commentDlg.show();
             dlg = _commentDlg;
           }
           // ensure the dialog is scrolled into view
           // TODO jpk dojo1.1 - we are now doing this in RegionalDialog.show()
           /*
           if(dlg && dlg.domNode) {
             window.scrollTo(0, ambra.domUtil.getCurrentOffset(dlg.domNode).top);
           }
           */
         }
         _ldc.hide();
        }
       });
    }

  }

}

if(!dojo._hasResource["ambra.annotation"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["ambra.annotation"] = true;
/*
 * $HeadURL:: http://gandalf.topazproject.org/svn/head/plos/webapp/src/main/webapp/javas#$
 * $Id: annotation.js 5581 2008-05-02 23:01:11Z jkirton $
 *
 * Copyright (c) 2006-2008 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
  * ambra.annotation
  *
  * This object takes a selection that has been made on screen and locates the XPath and 
  * offset for the start and end of the selection.  It then takes the selection and wraps 
  * it in span tags specifically used for highlighting the text.  The selection is 
  * deselected when the annotation dialog box appears but is replaced with the spans.  
  * This is triggered by the user clicking on the "Add your annotation" in the right hand 
  * column on the article page.
  * 
  * @author  Joycelyn Chung     joycelyn@orangetowers.com
  * @author  jkirton            jopaki@gmail.com
  **/

dojo.provide("ambra.annotation");



ambra.annotation = {
  /** 
   * ambra.annotation._createAnnotationOnkeyup(event)
   * 
   * Method triggered when the event is tied to the document and the user presses a key.
   * If the key pressed is ENTER, creates an annotation using the currently-selected text.
   * Parameter 'event' is a key press event.
   * 
   * @param  event      Event object         Event triggered by the keypress of the "ENTER" 
   *                                          button.
   * 
   * @return true                            Success.
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
  
  /** 
   * ambra.annotation.createAnnotationOnMouseDown()
   * 
   * Method triggered on onmousedown or onclick event of a tag.  When this method is 
   * triggered, it initiates an annotation creation using the currently-selected text.
   * 
   * @return true                            Success.
   */
  createAnnotationOnMouseDown: function () {
    ambra.formUtil.textCues.reset(_commentTitle, _titleCue); 
    ambra.formUtil.textCues.reset(_comments, _commentCue); 
    _annotationForm.commentTitle.value = "";
    _annotationForm.comment.value = "";
    
    var captureText = this.createNewAnnotation();
    
/*    if ( captureText ) {
      if (!event) var event = window.event;
      event.cancelBubble = true;
      if (event.stopPropagation) event.stopPropagation();
    }
*/
    //dojo.event.browser.preventDefault();

    return false;
  },

  handleUserSelectionError: function() {
    var msg = annotationConfig.annSelErrMsg ? 
      annotationConfig.annSelErrMsg : annotationConfig.dfltAnnSelErrMsg;
    alert(msg);
    annotationConfig.annSelErrMsg = null; // reset
  },

  /** 
   * ambra.annotation.createNewAnnotation()
   * 
   * Method that takes in the selection that was made and sends it to getRangeOfSelection to 
   * figure out the XPath and offset of the selection.  An object is returned with the text of 
   * the selection, the start and end points, the start and end xpath, and the start and end 
   * parent node and the nodes' ids.  This information is then sent to have the spans placed 
   * around the selection.  If getRangeOfSelection or analyzeRange() returns "noSelect", that 
   * indicates that the user have selected in a region of the page that is not selectable.  
   * createNewAnnotation() then returns the appropriate message.  If getRangeOfSelection returns 
   * null, that indicates that the user have not made a selection but have clicked on the "Add 
   * your annotation" link.  createNewAnnotation() then returns the appropriate message.
   * Otherwise createNewAnnotation() return true;
   * 
   * @return null                           No selection was made.
   * @return false                          A non-selectable region was selected.
   * @return true                           Success.
   */
  createNewAnnotation: function () {
    annotationConfig.rangeInfoObj = this.getRangeOfSelection();
    
    if (annotationConfig.rangeInfoObj == annotationConfig.excludeSelection) {
      this.handleUserSelectionError();
      this.undoPendingAnnotation();
      return false;
    }
    else if (!annotationConfig.rangeInfoObj) {
      alert("Using your mouse, select the area of the article you wish to notate.");
      return false;
    }
    else {      
      console.debug( 
              "annotationConfig.rangeInfoObj.range.text = '"    + annotationConfig.rangeInfoObj.range.text + "'\n" +
              "annotationConfig.rangeInfoObj.startPoint = "     + annotationConfig.rangeInfoObj.startPoint + "\n" +
              "annotationConfig.rangeInfoObj.endPoint = "       + annotationConfig.rangeInfoObj.endPoint + "\n" +
              "annotationConfig.rangeInfoObj.startParent = "    + annotationConfig.rangeInfoObj.startParent + "\n" +
              "annotationConfig.rangeInfoObj.endParent = "      + annotationConfig.rangeInfoObj.endParent + "\n" +
              "annotationConfig.rangeInfoObj.startParentId = "  + annotationConfig.rangeInfoObj.startParentId + "\n" +
              "annotationConfig.rangeInfoObj.endParentId = "    + annotationConfig.rangeInfoObj.endParentId + "\n" +
              "annotationConfig.rangeInfoObj.startXpath = "     + annotationConfig.rangeInfoObj.startXpath + "\n" +
              "annotationConfig.rangeInfoObj.endXpath = "       + annotationConfig.rangeInfoObj.endXpath);

      _annotationForm.startPath.value = annotationConfig.rangeInfoObj.startXpath;
      _annotationForm.startOffset.value = annotationConfig.rangeInfoObj.startPoint + 1;
      _annotationForm.endPath.value = annotationConfig.rangeInfoObj.endXpath;
      _annotationForm.endOffset.value = annotationConfig.rangeInfoObj.endPoint + 1;
     
      var mod = this.analyzeRange(annotationConfig.rangeInfoObj);
     
      if (mod == annotationConfig.excludeSelection) {
        alert("This area of text cannot be notated.");
        this.undoPendingAnnotation();
        return false;
      }

      return true;
    }
  },
  
  /** 
   * ambra.annotation.getHTMLOfSelection()
   * 
   * Method returns the html markup of the selection.
   * 
   * @return ""                             No html fragment available.
   * @return html   Document fragment       Success.
   */
  /*
  getHTMLOfSelection: function () {
    var range;
    
    // IE
    if (document.selection && document.selection.createRange) {
      range = document.selection.createRange();
      return this.getHTMLOfRange(range); //range.htmlText;
    }
    // Mozilla
    else if (window.getSelection) {
      var selection = window.getSelection();
      if (selection.rangeCount > 0) {
        range = selection.getRangeAt(0);
        return this.getHTMLOfRange(range); 
      }
      else {
        return '';
      }
    }
    else {
      return '';
    }
  },
  */

  /** 
   * ambra.annotation.getHTMLOfRange()
   * 
   * Method takes in the range object and returns the html markup for the selection.
   * 
   * @return ""                             No html fragment available.
   * @return html   Document fragment       Success.
   */
  /*
  getHTMLOfRange: function (range) {
    // IE
    if (document.selection && document.selection.createRange) {
      return range.htmlText;
    }
    // Mozilla
    else if (window.getSelection) {
      var clonedSelection = range.cloneContents();
      var div = document.createElement('div');
      div.appendChild(clonedSelection);
      return div.innerHTML;
    }
    else {
      return '';
    }
  },
  */

  isSimpleTextSelection: function(range) {
    if(range) {
      // IE
      if (document.selection && document.selection.createRange) {
        return (range.htmlText == range.text);
      }
      // Mozilla
      else if (window.getSelection) {
        var clonedSelection = range.cloneContents();
        var div = document.createElement('div');
        div.appendChild(clonedSelection);
        var html = div.innerHTML;
        return (html == range.toString());
      }
    }
    return false;
  },

  /**
   * ambra.annotation.getRangeOfSelection()
   * 
   * Method determines which selection object that the user's browser recognizes and 
   * forwards to the appropriate method that will the range.
   * 
   * @return false                          Failure.
   * @return rangeInfo      Object          Object containing the range and the start and 
   *                                         end point offsets, parent elements, xpaths, 
   *                                         parent ID, and the selection.
   */
  getRangeOfSelection: function () {
    var rangeInfo = new Object();

    // IE
    if (document.selection && document.selection.createRange) {
      rangeInfo = this.findIeRange();
    }
    // Mozilla
    else if (window.getSelection || document.getSelection) {
      rangeInfo = this.findMozillaRange();
    }
    else {
      return false;
    }

    // is this range just simple text?
    rangeInfo.isSimpleText = this.isSimpleTextSelection(rangeInfo.range);
    
    return rangeInfo;
  },

  /**
   * ambra.annotation.analyzeRange(Object rangeInfo)
   * 
   * This method takes in the rangeInfo object and passes it to insertHighlightWrapper().
   * If insertHighlightWrapper() returns with "noSelect", this method returns "noSelect".
   * Otherwise, the marker for the regionalDialog is set and show() is called on it to 
   * display the dialog box.
   * 
   * @param  rangeInfo      Object          Object containing the range and the start and 
   *                                         end point offsets, parent elements, xpaths, 
   *                                         parent ID, and the selection.
   *
   * @return "noSelect"     text            Non-selectable area was selected.
   * @return <nothing>                      Success. 
   */
  analyzeRange: function (rangeInfo) {
    var mod = this.insertHighlightWrapper(rangeInfo);
    
    if (mod == annotationConfig.excludeSelection) {
      return annotationConfig.excludeSelection;
    }

    console.debug('ambra.annotation.analyzeRange() - annotationConfig.regionalDialogMarker: ' + annotationConfig.regionalDialogMarker);
    var marker = dojo.byId(annotationConfig.regionalDialogMarker);
    _annotationDlg.setMarker(marker);
    showAnnotationDialog();
  },

  /**
   * ambra.annotation.findIeRange()
   * 
   * This method is only valid for Internet Explorer because the selection and range 
   * object is different for this browser.
   * 
   * Method first checks to make sure a selection was made and is of type text.  If 
   * it's not, false is returned.
   * 
   * It then locates the selection and sets up the range.  This range is then 
   * duplicated twice.  One is collapsed to the beginning of the range and is used 
   * as the start of range reference.  The other duplicate range is collapsed to the
   * end and is used as the end of range reference.  These two references are then 
   * passed to getRangePoint(<range>) method.  If getRangePoint returns "noSelect",
   * that value is returned by this method also.
   * 
   * Once the points are found, a temporary span is placed at the beginning and end
   * of the range.  These temporary spans are needed when the actual highlighting 
   * spans are wrapped around the range.  This is explained further in 
   * insertHighlightWrapper().  The start and end points and the xpathLocation for 
   * the end point is passed into isAncestorOf to check if the the xpathLocation
   * for the end point is an ancestor of the start point.  If it is, it means that the
   * user probably selected to the end of a paragraph, for example.  In IE, when the 
   * user selects to the end of a container, the end pointer is usually outside the
   * container and not in the next sibling container.  When this happens, the parent 
   * container is the parent of the parent container of the start point.  In this
   * case, the range is moved at the end back by one character so the endpoint will be
   * within the container the selection is actually in.
   * 
   * If neither start or end points are found, this method returns a null.  Otherwise, 
   * the range and the start and end point offsets, parent elements, xpaths, parent ID,
   * and the selection are placed into an object that stores these values.  This new
   * object is then returned.
   * 
   * @return null                           No selection was made.
   * @return "noSelect"     text            Non-selectable area was selected.
   * @return false          boolean         Failure.
   * @return ieRange        Object          Object containing the range and the start and 
   *                                         end point offsets, parent elements, xpaths, 
   *                                         parent ID, and the selection. 
   */
  findIeRange: function() {
    if (document.selection.type == "Text") {
      var range      = document.selection.createRange();

      // textualize selection range if not already 
      // and do element-based selection range validation if applicable 
      if(this.validateAndTextualizeRange(range) == annotationConfig.excludeSelection) {
        return annotationConfig.excludeSelection;
      }

      var startRange = range.duplicate();
      var endRange   = startRange.duplicate();
      
      startRange.collapse(true);
      endRange.collapse(false);
  
      var startPoint = this.getRangePoint(startRange);
      if (startPoint == annotationConfig.excludeSelection)
        return annotationConfig.excludeSelection;
      
      startRange.pasteHTML("<span id=\"tempStartPoint\" class=\"temp\"></span>");

      var endPoint = this.getRangePoint(endRange);
      if (endPoint == annotationConfig.excludeSelection)
        return annotationConfig.excludeSelection;
        
      endRange.pasteHTML("<span id=\"tempEndPoint\" class=\"temp\"></span>");
        
      var isAncestor = this.isAncestorOf(startPoint.element, endPoint.element, "xpathLocation", endPoint.xpathLocation);
      
      if (isAncestor) {
        range.moveEnd("character", -1);
        endRange = range.duplicate();
        endRange.collapse(false);
        
        endPoint = this.getRangePoint(endRange);
        if (endPoint == annotationConfig.excludeSelection)
          return annotationConfig.excludeSelection;
      }
  
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
                    selection:      null};
        
        return ieRange;
      }
    }
    else {
      return false;
    }
  },
  
  /**
   * validateAndTextualizeRange(range)
   *
   * Verifies then textualizes a range.
   *
   * @param range A DOM range object.
   * @return null When successful.
   * @return "noSelect" upon error.
   */
  validateAndTextualizeRange: function(range) {
    // IE
    if (document.selection && document.selection.createRange) {
      // textualize
      // NOTE: IE's range.findText method will by design return false 
      // if the selection spans over multiple elements when the selection range is element based 
      if(!range.findText(range.text, 0, 0)) {
        return annotationConfig.excludeSelection;
      }
    }
    // Mozilla
    else if (window.getSelection) {
      var nt = range.startContainer.nodeType;
      if(nt == 1) {
        // element-based user selection...
        // enfore for element selections the range spans only a single element in its entirety
        if(range.endContainer.nodeType != 1 || range.startContainer != range.endContainer) {
          return annotationConfig.excludeSelection;
        }
        // enforce that only one element is selectable when we have an element-based user selection
        // (this is usually an li tag)
        if(Math.abs(range.startOffset - range.endOffset) != 1) {
          annotationConfig.annSelErrMsg = 'Only one item may be selected for notation.';
          return annotationConfig.excludeSelection;
        }
        // it is presumed all contained text w/in the following container (node) is selected
        var ftn = ambra.domUtil.findTextNode(range.startContainer.childNodes[range.startOffset], true);
        var ltn = ambra.domUtil.findTextNode(range.startContainer.childNodes[range.endOffset - 1], false);
        range.setStart(ftn, 0);
        range.setEnd(ltn, ltn.length);
      }
      else if(nt == 3) {
        // text-based user selection...
        // ensure we are not spanning multiple li tags
        // NOTE: verified w/ Susanne DeRisi
        // TODO finish
      }
      else {
        // un-handled node type
        return annotationConfig.excludeSelection;
      }
    }
    else {
      return 'noSelect';
    }
  },

  /**
   * ambra.annotation.findMozillaRange()
   * 
   * This method works for many Gecko based browsers such as Firefox and Safari.
   * 
   * Method first locates the selection.  It then checks to make sure a selection was 
   * made.  If it's empty or null, false is returned.  
   * 
   * It then checks to see if the getRangeAt() method exists.  If it doesn't, that means
   * the browser doesn't support getRangeAt which appears to occur in Safari.  In Safari,
   * you have to set the range manually.
   * 
   * Once the range is set, it is then cloned twice.  One of the cloned range is collapsed
   * to the beginning of the range.  The other cloned range is collapsed to the end of the
   * range.  Both of these collapsed ranges will be used as references to mark the start 
   * and end of the selection.  Then each of these are sent to the getRangePoint(<range>)
   * method.  If getRangePoint returns "noSelect", that value is returned by this method 
   * also.  The temporary spans are then removed.
   * 
   * If neither start or end points are found, this method returns a null.  Otherwise, 
   * the range and the start and end point offsets, parent elements, xpaths, parent ID,
   * and the selection are placed into an object that stores these values.  This new
   * object is then returned.
   * 
   * @return null                           No selection was made.
   * @return "noSelect"     text            Non-selectable area was selected.
   * @return false          boolean         Failure.
   * @return mozRange       Object          Object containing the range and the start and 
   *                                         end point offsets, parent elements, xpaths, 
   *                                         parent ID, and the selection. 
   */
  findMozillaRange: function() {
    var rangeSelection = window.getSelection ? window.getSelection() : 
                         document.getSelection ? document.getSelection() : 0;
                         
    if (rangeSelection != "" && rangeSelection != null) {
      console.debug("Inside findMozillaRange");
      var startRange;
      
      // Firefox
      if (typeof rangeSelection.getRangeAt != "undefined") {
         startRange = rangeSelection.getRangeAt(0);
      }
      // Safari
      else if (typeof rangeSelection.baseNode != "undefined") {
        console.debug(
          "rangeSelection.baseNode = '"     + rangeSelection.baseNode + "'\n" +
            "rangeSelection.baseOffset = '"   + rangeSelection.baseOffset + "'\n" +
            "rangeSelection.extentNode = '"   + rangeSelection.extentNode + "'\n" +
            "rangeSelection.extentOffset  = " + rangeSelection.extentOffset);
        
        startRange = window.createRange ? window.createRange() :
                     document.createRange ? document.createRange() : 0;
        startRange.setStart(rangeSelection.baseNode, rangeSelection.baseOffset);
        startRange.setEnd(rangeSelection.extentNode, rangeSelection.extentOffset);
        
        if (startRange.collapsed) {
          startRange.setStart(rangeSelection.extentNode, rangeSelection.extentOffset);
          startRange.setEnd(rangeSelection.baseNode, rangeSelection.baseOffset);
        }
      }

      // textualize selection range if not already 
      // and do element-based selection range validation if applicable 
      if(this.validateAndTextualizeRange(startRange) == annotationConfig.excludeSelection) {
        return annotationConfig.excludeSelection;
      }

      var endRange   = startRange.cloneRange();
      var range      = startRange.cloneRange();
      
      startRange.collapse(true);
      endRange.collapse(false);
  
      var tempNode = document.createElement("span");
      endRange.insertNode(tempNode);

      var endPoint       = this.getRangePoint(endRange);
      
      if (endPoint == annotationConfig.excludeSelection)
        return annotationConfig.excludeSelection;
      
      var startPoint     = this.getRangePoint(startRange);
      
      if (startPoint == annotationConfig.excludeSelection)
        return annotationConfig.excludeSelection;
      
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
          selection:      rangeSelection};    
                          
        return mozRange;
      }
    }
    else {
      return false;
    }
  },
  
  /**
   * ambra.annotation.getRangePoint(Object range)
   * 
   * This method takes in the range object that has been collapsed.  A temporary span is 
   * created with an id of "POINT_SPAN".  The temporary span is then inserted into the range.
   * When the span is inserted back into the range, it is then within the context of the document
   * object.  So it's necessary to look for the temporary span again based on the id to use as a
   * marker.  Using getFirstXpathAncestor(), the parent element that has a xpathlocation is 
   * located.  If it returns "noSelect", that in turn is returned.  Otherwise, the offset of the 
   * marker from the parent element is calculated.  An object is created where the parent element
   * node, xpathlocation, and the offset is set called point.  The temporary span is removed.  The 
   * point is the return value.
   * 
   * @param  range          Range object    Collapsed range.
   * 
   * @return "noSelect"     text            Non-selectable area was selected.
   * @return point          Object          Object containing the parent element, the parent element
   *                                         xpathlocation attribute value, and the offset from the
   *                                         parent.
   */
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

    var pointEl = this.getFirstXpathAncestor(pointSpan);
    //alert("pointEl = " + pointEl.element.nodeName + ", " + pointEl.xpathLocation);

    if (!pointEl || pointEl == annotationConfig.excludeSelection) 
      return annotationConfig.excludeSelection;

    var point = new Object();
    point.element = pointEl.element;
    point.xpathLocation = pointEl.xpathLocation;
    point.offset = this.getPointOffset(pointSpan, pointEl);

    pointSpan.parentNode.removeChild(pointSpan);
    
    return point;
  },
  
  /**
   * ambra.annotation.getPointOffset(Node sourceNode, Node targetNode)
   * 
   * Method takes in the sourceNode from which the offset is counted from and the targetNode
   * where the counting ends.  Recursively looping through the sourceNode's previousSibling,
   * element nodes that does not contain the classname "bug", which indicates an existing
   * annotation span, or, if in IE, classname of "temp", are normalized.  The normalized 
   * text of the element nodes and preceeding siblings that are text nodes are counted and 
   * added to the running total named offset.
   * 
   * If the targetNode was not passed in, the count ends when the loop reaches the first 
   * child node.  If the targetNode exists and the parent node of the source node does not
   * contain the xpathlocation attribute, the parent of the sourceNode and the targetNode
   * gets sent into this method again.  The total from which is added to the running total.
   * Offset is the returning value.  
   * 
   * @param  sourceNode     Node object     Origin from which to start the count.
   * @param  targetNode     Node object     If passed in, counting continues until this node
   *                                         has been reached.
   * 
   * @return offset         Integer         Count of the text and normalized text between the
   *                                         sourceNode and targetNode. 
   */
  getPointOffset: function (sourceNode, targetNode) {
    var offset = 0;
    var node = sourceNode;
    
    for (var currentNode = node.previousSibling; currentNode != null; currentNode = currentNode.previousSibling) {
      if (currentNode.nodeType == 1) { // element
        if (currentNode.className.match('bug') || (dojo.isIE && currentNode.className.match('temp'))) {
          // skip this
        }
        else {
          var normalText = this.normalizeText( currentNode, "");
          //alert("normalText = '" + normalText + "'\nlength = " + normalText.length + "\ncurrentNode = " + currentNode.nodeName + ", " + currentNode.name);
          offset += normalText.length;
        }
      }
      else if (currentNode.nodeType == 3) { // text
        //alert("currentNode = " + currentNode.nodeValue + "\nlength = " + currentNode.length);
        offset += currentNode.length;
      }
      else { // other
        // don't change the offset
      }
    }
    
    if (targetNode) {
      var nodeParent = sourceNode.parentNode;
      
      if (nodeParent.getAttributeNode('xpathLocation') == null) {
        offset += this.getPointOffset(nodeParent, targetNode);
      }
    }
    
    //alert("offset = " + offset);
    return offset;    
  },

  /**
   * ambra.annotation.getAncestors(Node sourceNode, text lastAncestorId)
   * 
   * This method traverses up the node tree until it finds the parent where the id matches 
   * lastAncestorId collecting an array of the parents at each level of the node tree.
   * 
   * @param   sourceNode      Node object   Source node from which the traversal begins.
   * @param   lastAncestorId  String          Id of the last parent.
   * 
   * @return  familyTree      Array         Collection of parents at each node level and in
   *                                         the path of the traversal from the sourceNode 
   *                                         to the parent with the id matching lastAncestorId.
   */
  getAncestors: function ( sourceNode, lastAncestorId ) {
    var familyTree = new Array();
    //familyTree.push(selfNode);
    var parentalNode = sourceNode;
    
    while ( parentalNode.id != lastAncestorId ) {
      parentalNode = parentalNode.parentNode;
      
      var nodeObj = new Object();
      nodeObj.element = parentalNode;
      nodeObj.id = parentalNode.id;
      nodeObj.name = parentalNode.name;
      nodeObj.xpathLocation = (parentalNode.getAttributeNode("xpathLocation")) ? parentalNode.getAttributeNode("xpathLocation").nodeValue : "";
      familyTree.push(nodeObj);
    }
    
    return familyTree;
  },
  
  /**
   * ambra.annotation.isAncestorOf(Node sourceNode, text attributeName, text attributeValue)
   * 
   * Method takes in a sourceNode and an attribute and it's value to match.  First it finds all the 
   * ancestors of the sourceNode up to the last ancestor of the annotation section.  As it recurses 
   * through the familyTree, it looks to see if one of the ancestors have the attributeName and 
   * attributeValue.
   * 
   * @param   sourceNode      Node object         Node used as the source.
   * @param   attributeName   String              Name of attribute used for matching.
   * @param   attributeValue  String              Value of the attribute to match.
   * 
   * @return  false           boolean             Failed to find a match.
   * @return  true            boolean             Match found.
   */
  isAncestorOf: function ( sourceNode, attributeName, attributeValue ) {
    var familyTree = this.getAncestors(sourceNode, annotationConfig.lastAncestor);
    var parentalNode = sourceNode;
    
    for (var i=0; i<familyTree.length; i++) {
      if (familyTree[i][attributeName] == attributeValue) {
        return true;
      }
    }
    
    return false;
  },
  
  /**
   * ambra.annotation.getFirstXpathAncestor(Node sourceNode)
   * 
   * Method traverses up the node tree looking for the first parent that contains the 
   * xpathlocation attribute.  When a parent node is found with the correct attribute,
   * the parent element and the xpathlocation value is stored in a parentObj object.
   * 
   * @param   sourceNode    Node object         Node source to begin the search.
   * 
   * @return  "noSelect"    String              Non-selectable area has been selected.
   * @return  false         boolean             Failed search.
   * @return  parentObj     Object              Object that stores the parent element
   *                                             and its xpathlocation value.
   */
  getFirstXpathAncestor: function ( sourceNode ) {
    var parentalNode = sourceNode;

   {
    try {
      if (parentalNode.getAttributeNode('xpathLocation') == null) {
        for (var pNode=parentalNode; pNode.getAttributeNode('xpathLocation') == null; pNode = pNode.parentNode) {
          //alert("pNode = " + pNode.nodeName);
          parentalNode = pNode;
        }
        //alert("parentalNode [before] = " + parentalNode.nodeName);
        parentalNode = parentalNode.parentNode;
        //alert("parentalNode [after] = " + parentalNode.nodeName);
        
        //alert("parentalNode.nodeName = " + parentalNode.nodeType);
        if (parentalNode.getAttributeNode('xpathLocation') != null && parentalNode.getAttributeNode('xpathLocation').nodeValue == annotationConfig.excludeSelection) {
          //alert("getFirstXpathAncestor: noSelect");
          return annotationConfig.excludeSelection;
        }
        else if (parentalNode.nodeName == 'BODY') {
          return annotationConfig.excludeSelection;
        }
      }
          
      //alert("parentalNode.getAttributeNode('xpathLocation').nodeValue = " + parentalNode.getAttributeNode('xpathLocation').nodeValue + ", " + parentalNode.getAttributeNode('xpathLocation').nodeValue.length);

      var parentObj = new Object();
      parentObj.element = ( parentalNode.getAttributeNode('xpathLocation')!= null & parentalNode.getAttributeNode('xpathLocation').nodeValue  == annotationConfig.excludeSelection ) ? null : parentalNode;
      parentObj.xpathLocation = (parentalNode.getAttributeNode('xpathLocation') != null) ? parentalNode.getAttributeNode('xpathLocation').nodeValue : "";
      return parentObj;
    }
    catch(err) {
      //txt="There was an error on this page.\n\n";
      //txt+="Error description: [getFirstXpathAncestor] " + err.description + "\n\n";
      //txt+="Click OK to continue.\n\n";
      //alert(txt);
    }
   }
   
   return false;
  },
  
  /**
   * ambra.annotation.getChildList(Object parentObj, Node element)
   * 
   * Method takes in the parent object and the element node to search for.  A search is
   * made on the parent object to see if it has any child that matches that specific element
   * node.  A list of the child nodes are returned.  Based on this list, the text within the
   * child node is normalized.  The normalized text is used to calculate the end offset of the
   * child.  The start offset of the child is sent to getPointOffset to calculate the offset.
   * Both information is placed into an array of objects called childList.
   * 
   * @param   parentNode      Object          Source node for searching.
   * @param   element         Node object     Element to search for in the children of the 
   *                                           parentNode.
   * 
   * @return  childList       Array           Collection of childnodes and their start and end
   *                                           end offsets from the parent.
   */
  /*
  getChildList: function (parentNode, element) {
    var childSearch = parentNode.getElementsByTagName(element);
    
    var childList = new Array();
    for (var i=0; i<childSearch.length; i++) {
      var tmpText = this.normalizeText(childSearch[i], '');
      var startOffset = this.getPointOffset(childSearch[i]);
      var endOffset = tmpText.length;
      
      childList[childList.length] = {startOffset: startOffset,
                                     endOffset:   endOffset};
      //alert("tmpText = " + tmpText + "\n" +
      //      "childList[" + listIndex + "] = startOffset: " + childList[listIndex].startOffset + ", endOffset: " + childList[listIndex].endOffset);
    }
    
    
    return childList;
    
  },
  */
 
  /**
   * ambra.annotation.insertHighlightWrapper(Object rangeObj)
   * 
   * This method creates a span tag with all the attribute set to highlight some text.
   * It also creates a linked image to be used at the beginning of the selection.  Both 
   * of these elements are used as templates and are passed into modifySelection() along 
   * with the html fragment of the selection and an id that marks the beginning of the 
   * selection.  The modifySelection() method then returns the modified html fragment with
   * the spans that allows it to be highlighted are put back into the document context.
   * 
   * The method for extracting the html fragment is straightforward in Firefox.  In IE, 
   * however, the range object has a method for getting the html fragment of the selection 
   * but it's read-only.  To get around this, the read-only fragment is converted to an 
   * html fragment using innerHTML and stored in a temporary node.  This fragment is then 
   * copied into a variable named content.  Content is the html fragment that get sent into
   * modifySelection().  Since this is not a true extraction of the html fragment, a couple of 
   * temporary spans are placed at the beginning and end to keep the place of the start and
   * end of the range.  After the modified content is returned, these temporary spans are 
   * removed and the selection is cleared from the document.  In Safari, on the other hand, 
   * will allow you to manipulate the html directly but calling the extraction method on it 
   * causes the browser to crash.  To get around this, the html fragment is cloned and the 
   * original fragment is removed.
   * 
   * @param   rangeObj    Range object      Object containing the parent element, the parent element
   *                                         xpathlocation attribute value, and the offset from the
   *                                         parent.
   * 
   * @return  "noSelect"  String            Non-selectable area has been selected.
   * @return  <nothing>                     Success.
   */
  insertHighlightWrapper: function (rangeObj) {
    var noteClass = annotationConfig.annotationMarker + " " + 
    //                (annotationConfig.isAuthor ? "author-" : "self-") +
                    (annotationConfig.isPublic ? "public" : "private") +
                    "-active";
    var noteTitle = (annotationConfig.isAuthor ? "Author" : annotationConfig.isPublic ? "User" : "My") + 
                    " Note " + 
                    (annotationConfig.isPublic ? "(Public)" : "(Private)");
    var markerId     = annotationConfig.regionalDialogMarker;
    var noteImg   = _namespace + "/images/" + "note_" + (annotationConfig.isAuthor ? "author" : "private") + "_active.gif";
    var noteImgClass = annotationConfig.annotationImgMarker;
    var contents = document.createDocumentFragment();
      
    // create a new span and insert it into the range in place of the original content
    var newSpan = document.createElement('span');
    newSpan.className = noteClass;
    newSpan.title     = noteTitle;
    //newSpan.id        = markerId;
    newSpan.annotationId = "";

    /*
    var newImg = document.createElement('img');
    newImg.src       = noteImg;
    newImg.title     = noteTitle;
    newImg.className = noteImgClass;
    newSpan.appendChild(newImg);
    */
    
    var link = document.createElement("a");
    link.className = 'bug public';
    //link.href = '#';
    //link.id = markerId;
    link.title = 'Click to preview this note';
    link.displayId = "";
    link.onclick = function() { ambra.displayComment.show(this); }
    link.onmouseover = function() { ambra.displayComment.mouseoverComment(this); }
    link.onmouseout = function() { ambra.displayComment.mouseoutComment(this); }
    link.appendChild(document.createTextNode('1'));

    // Insertion for IE
    if (rangeObj.range.pasteHTML) {
      var html = rangeObj.range.htmlText;
/*      rangeObj.range.pasteHTML('<span class="' + noteClass + 
                               '" title="'     + noteTitle +
                                '"  annotationId=""' +
                               '">' + 
                               '<a href="#" class="bug public" id="' + markerId + 
                               '"  onclick="ambra.displayComment.show(this);"' + 
                               ' onmouseover="ambra.displayComment.mouseoverComment(this);"' + 
                               ' onmouseout="ambra.displayComment.mouseoutComment(this);"' + 
                               ' title="Click to preview this note">1</a>' +
                               html + '</span>');
*/
      var tempNode = document.createElement("div");
      tempNode.innerHTML = html;
      ambra.domUtil.copyChildren(tempNode, contents);
  
        
      var modContents = this.modifySelection(rangeObj, contents, newSpan, link, markerId);
      
      if (modContents == annotationConfig.excludeSelection) {
        return annotationConfig.excludeSelection;
      }

      if (djConfig.isDebug) {
        console.debug("== Modified Content =="); 
        console.debug(modContents.cloneNode(true));
      }
      
      if (modContents.hasChildNodes()) {  
        ambra.domUtil.removeChildren(tempNode);
        ambra.domUtil.copyChildren(modContents, tempNode);
        rangeObj.range.pasteHTML(tempNode.innerHTML);
      }
      
/*      var startPoint = dojo.byId("tempStartPoint");
      if (!modContents.hasChildNodes()) {
        for (var currentNode = startPoint.nextSibling; currentNode != null; currentNode = currentNode.nextSibling) {
          ambra.domUtil.removeNode(currentNode);
        }
      }
      ambra.domUtil.removeNode(startPoint);
      
      var endPoint = dojo.byId("tempEndPoint");
      if (!modContents.hasChildNodes()) {
        for (var currentNode = endPoint.previousSibling; currentNode != null; currentNode = currentNode.previousSibling) {
          ambra.domUtil.removeNode(currentNode);
        }
        
        document.selection.empty();
      }
      ambra.domUtil.removeNode(endPoint);
*/
      var startPoint = dojo.byId("tempStartPoint");
      ambra.domUtil.removeNode(startPoint);
      var endPoint = dojo.byId("tempEndPoint");
      ambra.domUtil.removeNode(endPoint);
      document.selection.empty();
    }
    else {
      if (dojo.isSafari) {  //Insertion for Safari
          contents = rangeObj.range.cloneContents();
          rangeObj.range.deleteContents();
      }
      else {  // Insertion for Firefox
        contents = rangeObj.range.extractContents();
      }

      console.debug("== Calling modifySelection ===");
      
      var modContents = this.modifySelection(rangeObj, contents, newSpan, link, markerId);

      if (modContents == annotationConfig.excludeSelection) {
        return annotationConfig.excludeSelection;
      }

      rangeObj.range.insertNode(modContents);
      return;
    }
  },
  
  /**
   * ambra.annotation.modifySelection(Object rangeObj, Document fragment  contents, Node newspan, Node link, String markerId)
   * 
   * Method determines whether the start and end of the range is within the same container 
   * and routes the content accordingly.
   * 
   * @param   rangeObj      Range object        Object containing the parent element, the parent element
   *                                             xpathlocation attribute value, and the offset from the
   *                                             parent.
   * @param   contents      Document fragment   HTML fragment of the range.
   * @param   newSpan       Node object         Template of span node used to highlight the text.
   * @param   link          Node object         Template of linked image of the annotation bug marking the
   *                                             beginning of the selection.
   * @param   markerId      String              Id of marker to indicate the beginning of the selection.
   *    
   * @return  modContents   Document fragment   Modified document fragment of the selection.
   */
  modifySelection: function(rangeObj, contents, newSpan, link, markerId) {
    console.debug("=== Inside modifySelelection ===");
    
    var modContents;

    if (rangeObj.startXpath == rangeObj.endXpath) {
      modContents = document.createDocumentFragment();
      modContents.appendChild(this.insertWrapper(rangeObj, contents, newSpan, link, markerId, null));
    }
    else {
      modContents = this.modifyMultiSelection(rangeObj, contents, newSpan, link, markerId);    
    }
    
    return modContents;
  },
  
  /**
   * ambra.annotation.modifyMultiSelection(Object rangeObj, Document fragment  contents, Node newspan, Node link, String markerId)
   * 
   * Method takes selections that spans across multiple containers, such as <p>, and figures 
   * out if the container is at the beginning, middle, or end of the selection and a value of 
   * -1, 0, 1, respectively, is assigned to insertMode.  As the document fragment is recursively 
   * looped through, each child node is inspected to see if they contain a xpathlocation 
   * attribute.  If it has a xpathlocation attribute in one of the child nodes, that child node 
   * is sent through this method again for further parsing.  Otherwise, the fragment is sent to 
   * have the wrapper placed around the content and the modified content is then returned from 
   * this method.  In all cases, if "noSelect" is returned, that gets returned from this method.
   * 
   * The reason the selections over multiple containers are treated specially is because of the 
   * way the range object extracts the fragment.  Both IE and Mozilla terminates the container of
   * the start and end pieces so effectively splitting each of the two containers.  To avoid 
   * creating new paragraphs, the first and last fragment of the selection are removed from
   * the html fragment extracted from the range and that modified fragment gets inserted directly 
   * into the document. Since they are being removed, the number of childnodes in the overall range
   * fragment gets smaller so the loop index has to be decremented.  In IE, since the html fragment 
   * cannot be modified directly, the copied version replaces the actual fragment in the document.  
   * In this case, only the middle containers are being decremented.
   * 
   * @param   rangeObj      Range object        Object containing the parent element, the parent element
   *                                             xpathlocation attribute value, and the offset from the
   *                                             parent.
   * @param   contents      Document fragment   HTML fragment of the range.
   * @param   newSpan       Node object         Template of span node used to highlight the text.
   * @param   link          Node object         Template of linked image of the annotation bug marking the
   *                                             beginning of the selection.
   * @param   markerId      String              Id of marker to indicate the beginning of the selection.
   *    
   * @return  "noSelect"    String              Non-selectable area has been selected.
   * @return  modContents   Document fragment   Modified document fragment of the selection.
   */
  modifyMultiSelection: function(rangeObj, contents, newSpan, link, markerId) {
    var modContents = document.createDocumentFragment();
    var multiContent = contents.childNodes;
    
    //var xpathloc;
    for (var i=0; i < multiContent.length; i++) {
      // If the node is a text node and the value is either a linefeed and/or carriage return, skip this loop
      if (multiContent[i].nodeName == "#text" && (multiContent[i].nodeValue.match(new RegExp("\n")) || multiContent[i].nodeValue.match(new RegExp("\r")))) {
        continue;
      }


      var xpathloc = (multiContent[i].getAttribute) ? multiContent[i].getAttribute("xpathlocation") : null;
      var insertMode = 0;
      
      console.debug(
          "=== MODIFYMULTISELECTION ==="
        + "\n" + "[MODIFYMULTISELECTION] i = " + i
        + "\n" + "node = " + multiContent[i].nodeName + ", " + xpathloc + ", " + multiContent[i].nodeValue 
        + "\n" + "multiContent.length = " + multiContent.length);
                  
      if (xpathloc != null) {
        if (xpathloc == annotationConfig.excludeSelection) {
          return annotationConfig.excludeSelection;  
        }
        else if (xpathloc == rangeObj.startXpath || xpathloc == rangeObj.endXpath) {
          var parentEl = null;
        
          if (xpathloc == rangeObj.startXpath) {
            parentEl = rangeObj.startParent.nodeName;
          }
          else if (xpathloc == rangeObj.endXpath) {
            parentEl = rangeObj.endParent.nodeName;
          }
          var xpathMatch = document.getElementsByAttributeValue(parentEl, "xpathlocation", xpathloc);
  
          if (xpathMatch != null && xpathMatch.length > 0) {
            if (i == 0) {
              insertMode = -1;
            }
            else {
              insertMode = 1;
            }
          }
        }
      }

      var doesChildrenContainXpath = this.isContainXpath(multiContent[i], insertMode);
      console.debug("doesChildrenContainXpath = " + doesChildrenContainXpath); 

      if (doesChildrenContainXpath == annotationConfig.excludeSelection) {
        return annotationConfig.excludeSelection;
      }
      else if (doesChildrenContainXpath == "true") {
        var newContent = this.modifyMultiSelection(rangeObj, multiContent[i], newSpan, link, markerId);
        
        if (newContent == annotationConfig.excludeSelection) {
          return annotationConfig.excludeSelection;
        }
        
        modContents.appendChild(newContent);
      }
      else if (multiContent[i].hasChildNodes()){
        var modFragment = this.insertWrapper(rangeObj, multiContent[i], newSpan, link, markerId, insertMode);
        
        if (dojo.isIE) {
          if (insertMode == 0) {
            modContents.appendChild(modFragment);
            --i;
          }
        }
        else {
          modContents.appendChild(modFragment);
          --i;
        }
      }
    }
    
    //if (dojo.isIE && multiContent.length == 2)
      //modContents = null;
    
    return modContents;
  },
  
  /**
   * ambra.annotation.isContainXpath(Node node, Integer multiPosition)
   * 
   * Method examines a node to determines if any of its children contain the xpathlocation attribute.
   * 
   * @param   node            Node object       Parent node to start search.
   * @param multiPosition     Integer           Numerical indication of a multiple selection.
   *                                                null = Not a multiple selection
   *                                                  -1 = The first container of a multi-selection
   *                                                   0 = The middle container(s) of a multi-selection
   *                                                   1 = The last container of a multi-selection
   * 
   * @return  "false"         String            No children has an xpathlocation attribute.
   * @return  "true"          String            A child node has been found to contain the xpathlocation
   *                                             attribute.
   */
  isContainXpath: function(node, multiPosition) {
    var xpathAttr = ambra.domUtil.isChildContainAttributeValue(node, "xpathlocation", null);
    console.debug("xpathAttr.length = " + xpathAttr.length);

    if (xpathAttr.length == 0) {
      return "false";
    }
    else {
      var start = 0;
      var arrayLength = xpathAttr.length;
      
      if (multiPosition == -1) {
        start = 1;
      }
      else if (multiPosition == 1) {
        arrayLength -= 1;
      }
      
      for (var i=start; i<arrayLength; i++) {  //exclude first and last node
        if (xpathAttr[i].value == annotationConfig.excludeSelection) {
          return annotationConfig.excludeSelection;
        }
      }
      
      return "true";
    }
  },
      
  /**
   * ambra.annotation.insertWrapper(rangeObject, rangeContent, refWrapperNode, linkObject, markerId, multiPosition)
   *
   * Inner function to process selection and place the appropriate markers for displaying the selection 
   * highlight and annotation dialog box.
   *  
   * The reason the selections over multiple containers are treated specially is because of the 
   * way the range object extracts the fragment.  Both IE and Mozilla terminates the container of
   * the start and end pieces so effectively splitting each of the two containers.  To avoid 
   * creating new paragraphs, the first and last fragment of the selection are removed from
   * the html fragment extracted from the range and that modified fragment gets inserted directly 
   * into the document. 
   * 
   * @param rangeObject      Range object         Range object containing the range and additional xpath information.
   * @param rangeContent     Document fragment    Extracted html from the selection contained in either a block node
   *                                               or a document fragment.
   * @param refWrapperNode   Node object          Reference to the node object, which contains the correct 
   *                                               attributes, that will contain the selection.
   * @param linkObject       Node oject           Link node object containing the annotation bug and the marker used
   *                                               to position dialog box.
   * @param markerId         text                 Marker ID string.
   * @param multiPosition    Integer              Numerical indication of a multiple selection.
   *                                                null = Not a multiple selection
   *                                                  -1 = The first container of a multi-selection
   *                                                   0 = The middle container(s) of a multi-selection
   *                                                   1 = The last container of a multi-selection
   * @param xpath            String                Value of xpathlocation node attribute.  Used for second passes for 
   *                                                the first and last container of a multiple selection.
   * 
   * @return rangeContent    Document fragment
  */ 
  insertWrapper: function(rangeObject, rangeContent, refWrapperNode, linkObject, markerId, multiPosition, elXpathValue) {
    var childContents = rangeContent.childNodes;  
    var nodelistLength = childContents.length;
    var insertIndex = 0;
    var nodesToRemove = new Array();
    var indexFound = null;
    var startTime = new Date();
    var ieDocFrag = document.createDocumentFragment();
    
    if (dojo.isSafari) {
      if (multiPosition == null) {
        nodelistLength = childContents.length - 1;
      }
      else if (multiPosition == 1) {
        //nodelistLength = childContents.length + 1;
      } 
    }
    
    // populate the span with the content extracted from the range
    for (var i = 0; i < nodelistLength; i++) {
      var xpathloc = (childContents[i].getAttribute) ? childContents[i].getAttribute("xpathlocation") : null;
      console.debug(
          "=== INSERTWRAPPER ==="
        + "\nnode = " + childContents[i].nodeName + ", " + xpathloc + ", " + childContents[i].nodeValue 
        + "\nmultiPosition = " + multiPosition
        + "\ni = " + i
        + "\nchildContents[" + i + "].nodeName = " + childContents[i].nodeName
        + "\nchildContents[" + i + "].nodeType = " + childContents[i].nodeType
        + "\nchildContents[" + i + "].className = " + childContents[i].className
        + "\nchildContents[" + i + "].hasChildNodes = " + childContents[i].hasChildNodes());

      // If the node is a text node and the value is either a linefeed and/or carriage return, skip this loop
      if (childContents[i].nodeName == "#text" && (childContents[i].nodeValue.match(new RegExp("\n")) || childContents[i].nodeValue.match(new RegExp("\r")))) {
        continue;
      }

      var spanToInsert;
      var existingNode = childContents[i];
      
      // modify the existing note span
      if (existingNode.nodeName == "SPAN" && ambra.domUtil.isClassNameExist(existingNode, "note")) {
        spanToInsert = existingNode.cloneNode(true);
        spanToInsert.className = refWrapperNode.getAttributeNode("class").nodeValue;
      }
      // wrap in a new note span
      else {
        spanToInsert = refWrapperNode.cloneNode(true);
        spanToInsert.appendChild(existingNode.cloneNode(true));
      }

      // insert the marker ID and bug
      if (i == 0 && (multiPosition == null || multiPosition == -1)) {
        if (dojo.isIE) {
          linkObject.setAttribute("id", markerId);
        }
        else {
          spanToInsert.setAttribute("id", markerId);
        }

        ambra.domUtil.insertBefore(linkObject, spanToInsert.firstChild, false);
      }

      // insert into the range content before the existing node (the existing node will be deleted, leaving only the new one)
      if (multiPosition == null || multiPosition == 0) {
        ambra.domUtil.replaceNode(existingNode, spanToInsert);
      }
      // insert into the document 
      else {
        var elXpathValue = rangeContent.getAttributeNode("xpathlocation").nodeValue;
        var parentEl = null;
        
        if (multiPosition == -1) {
          parentEl = rangeObject.startParent.nodeName;
        }
        else if (multiPosition == 1) {
          parentEl = rangeObject.endParent.nodeName;
        }
        
        var elements = document.getElementsByAttributeValue(parentEl, "xpathlocation", elXpathValue);

        if (elements.length > 0) {
          var tempPointStart = dojo.byId("tempStartPoint");
          var tempPointEnd = dojo.byId("tempEndPoint");
          
          if (multiPosition < 0) {
            if (tempPointStart && tempPointStart != null) {
              ambra.domUtil.insertBefore(spanToInsert, tempPointStart);
              ambra.domUtil.removeNode(tempPointStart.nextSibling);
            }
            else {
              elements[elements.length-1].appendChild(spanToInsert);
            }
          }
          else {
            var elToInsert = document.createDocumentFragment();
            if (dojo.isSafari && multiPosition == 1 && i == (childContents.length-1)) {
              ambra.domUtil.copyChildren(spanToInsert, elToInsert);
            }
            else {
              elToInsert = spanToInsert;
            }

            if (dojo.isIE && insertIndex == 0) {
              ieDocFrag.appendChild(elToInsert);
              ambra.domUtil.removeNode(tempPointEnd.previousSibling);
            }
            else {
              elements[elements.length-1].insertBefore(elToInsert, elements[elements.length-1].childNodes[insertIndex]);
              ++insertIndex;
            }            
          }
        }
       
        nodesToRemove.push(existingNode);
      } 
      
    }
    
    if (dojo.isIE && multiPosition == 1) {
      ambra.domUtil.insertAfter(ieDocFrag, tempPointEnd);
      return;
    }     
   
    // remove the existing node from the range content
    if (nodesToRemove.length > 0) {
      for (var i = 0; i < nodesToRemove.length; i++) {
        ambra.domUtil.removeNode(nodesToRemove[i]);
      }
    }   
   

    var endTime = new Date();
    
    console.debug("Duration: " + (endTime.getTime() - startTime.getTime() + "ms"));
    
    return rangeContent;
  },

  /**
   * ambra.annotation.undoPendingAnnotation()
   *
   * Removes all pending annotation markup from the document 
   * restoring it to the state it was in prior to a user selection.
   *
   * IMPT: This method only works when the selection consists soley of text (no HTML markup).
   *
   * @see isSimpleTextSelection(range) method
   */
  undoPendingAnnotation: function() {
    var arr;    
    
    // remove POINT_SPAN node (if present)
    var pointSpan = document.getElementById('POINT_SPAN');
    if(pointSpan) ambra.domUtil.removeNode(pointSpan);

    // remove temp (IE) nodes
    arr = dojo.query('.temp', annotationConfig.articleContainer);
    if(arr) for(var i=0; i<arr.length; i++) {
      ambra.domUtil.removeNode(arr[i]);
    }
    
    // remove the rdm node (for IE this is the a.bug node, for Gecko this a span node)
    var rdm = dojo.byId('rdm');
    if(rdm) ambra.domUtil.removeNode(rdm);

    // remove all span nodes having class marked as pending annotation 
    // promoting any text node children as succeeding siblings of the span node to be removed
    arr = dojo.query('.'+annotationConfig.pendingAnnotationMarker, annotationConfig.articleContainer);
    if(arr) { 
      var i, j, n, cns, cn, pn;
      
      // promote all found text node children
      for(i=0; i<arr.length; i++) {
        n = arr[i];
        cns = n.childNodes;
        if(cns.length > 0) {
          for(j=0; j<cns.length; j++) {
            cn = cns[j];
            if(cn.nodeType == 3) {
              // promote child to succeeding sibling
              n.parentNode.insertBefore(n.removeChild(cn), n.nextSibling);
            }
          }
        }
      }
      
      // kill the pending annotation markup
      for(var i=0; i<arr.length; i++) {
        n = arr[i];
        pn = n.parentNode;
        ambra.domUtil.removeNode(n);
        if(!(document.selection && document.selection.createRange)) {
          // non IE
          pn.normalize(); // to merge adjacent text nodes (non-IE only)
        }
      }
    }
    
    // invoke native normalize() dom method to merge any adjacent text nodes
    
  },
  
  /**
   * ambra.annotation.normalizeText( Document object  documentObj, String resultStr)
   * 
   * Method nomalizes a string.  This method is used instead of the prebuilt version
   * for IE is because it has a tendency to crash the browser.
   * 
   * @param   documentObj     Document object       Document fragment to be normalize.
   * @param   resultStr       String                An existing string that subsequent normal
   *                                                 strings should be added to.
   * 
   * @return  tempStr         String                The new normalized string.
   */
  normalizeText: function ( documentObj, resultStr ) {
    var tempStr = resultStr, carr = documentObj.childNodes, cn;
    for (var i=0; i<carr.length; i++) {
      cn = carr[i];
      if (cn.nodeType == 1) {
        if (cn.className.match('bug')) {
          // skip this
        }
        else {
          tempStr += this.normalizeText(cn, '');
        }
      }
      else if (cn.nodeType == 3) {
        tempStr += cn.nodeValue;
      }
    }
    return tempStr;
  }
}

}

if(!dojo._hasResource["ambra.corrections"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["ambra.corrections"] = true;
/*
 * $HeadURL:: http://gandalf/svn/head/topaz/core/src/main/java/org/topazproject/otm/Abst#$
 * $Id: AbstractConnection.java 4807 2008-02-27 11:06:12Z ronald $
 *
 * Copyright (c) 2006-2008 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
  * ambra.corrections
  *
  * Decorates an article with correction specific markup. 
  * 
  * @author jkirton (jopaki@gmail.com)
  **/
dojo.provide("ambra.corrections");


ambra.corrections = {
  aroot: null, // the top-most element of the article below which corrections are applied
  fch: null,
  fclist: null, // the formal corrections ordered list element ref

  arrElmMc:null, // array of the minor correction elements for the article
  arrElmFc:null, // array of the formal correction elements for the article
  
  _num: function(arr) { return arr == null ? 0 : arr.length; },

  numMinorCrctns: function() { return this._num(this.arrElmMc); },
  numFormalCrctns: function() { return this._num(this.arrElmFc); },

  /**
   * Removes any existing formal correction entries from the formal correction header.
   */
  _clearFCEntries: function() {
    ambra.domUtil.removeChildren(this.fclist);
    // TODO handle IE memory leaks
  },

  /**
   * ambra.corrections.apply
   *
   * Applies correction specific decorations to the article
   */
  apply: function() {
    // [re-]identify node refs (as the article container is subject to refresh)
    this.aroot = dojo.byId(annotationConfig.articleContainer);
    this.fch = dojo.byId(formalCorrectionConfig.fchId);
    this.fclist = dojo.byId(formalCorrectionConfig.fcListId);

    this.arrElmMc = dojo.query('.'+annotationConfig.styleMinorCorrection, this.aroot);
    this.arrElmFc = dojo.query('.'+annotationConfig.styleFormalCorrection, this.aroot);

    this._clearFCEntries();
    var show = (this.numFormalCrctns() > 0);
    if(show) {
      // [re-]fetch the formal corrections for the article
      var targetUri = _annotationForm.target.value;
      _ldc.show();
      dojo.xhrGet({
        url: _namespace + "/annotation/getFormalCorrections.action?target=" + targetUri,
        handleAs:'json',
        error: function(response, ioArgs){
          handleXhrError(response, ioArgs);
        },
        load: function(response, ioArgs) {
          var jsonObj = response;
          if (jsonObj.actionErrors.length > 0) {
            var errorMsg = "";
            for (var i=0; i<jsonObj.actionErrors.length; i++) {
              errorMsg = errorMsg + jsonObj.actionErrors[i] + "\n";
            }
            alert("ERROR [actionErrors]: " + errorMsg);
          }
          else {
            // success
            ambra.corrections.addFormalCorrections(jsonObj.formalCorrections);
          }
          _ldc.hide();
        }
      });
    }
    this.fch.style.display = show? '' : 'none';

  },//apply

  /**
   * truncateFcText
   *
   * Truncates the comment of formal correction 
   * suitable for display in the formal correction header.
   *
   * @param etc The formal correction escaped truncated comment as a string 
   *            with expected format of: "<p>{markup}</p>"
   * @return String of HTML markup (suitable for innerHTML) 
   */
  truncateFcText: function(etc) {
    if(!etc) return "";
    // currently we grab the first paragraph
    var node = document.createElement('span');
    node.innerHTML = etc;
    node = node.childNodes[0];
    return '<p>' + ambra.domUtil.findTextNode(node,true).nodeValue + '</p>';
  },

  /**
   * _toLi
   *
   * Creates an html li element for the given formal correction 
   * used in the ordered list w/in the formal correction header.
   *
   * @param fc formal correction json obj
   * @retun li element
   */
  _toLi: function(fc) {
    var li = document.createElement('li');
    li.innerHTML = this.truncateFcText(fc.escapedTruncatedComment);
    var p = li.firstChild;
    var tn = p.firstChild;
    tn.nodeValue = tn.nodeValue + ' (';
    
    var a = document.createElement('a');
    a.setAttribute('href', '#');
    a.setAttribute(formalCorrectionConfig.annid, fc.id);
    a.innerHTML = 'read formal correction';
    dojo.connect(a, "onclick", ambra.corrections.onClickFC);
    p.appendChild(a);
    p.appendChild(document.createTextNode(')'));
    return li;
  },

  /**
   * addFormalCorrections
   *
   * Adds formal corrections to the formal correction header.
   *
   * @param arr Array of formal corrections
   * @return void
   */
  addFormalCorrections: function(arr) {
    for(var i=0; i<arr.length; i++) this.fclist.appendChild(this._toLi(arr[i]));
  },

  /**
   * _findFrmlCrctnByAnnId
   *
   * Finds a formal correction node given an annotation id
   * by searching the formal corrections node array property of this object
   *
   * @param annId The annotation (guid) id
   * @return The found formal correction node or null if not found
   */
  _findFrmlCrctnByAnnId: function(annId) {
    if(this.arrElmFc == null || annId == null) return null;
    var n, naid;
    for(var i=0; i<this.arrElmFc.length; i++){
      n = this.arrElmFc[i];
      naid = dojo.attr(n, 'annotationid');
      if(naid != null && naid.indexOf(annId)>=0) return n;
    }
    return null;
  },
  
  _getAnnAnchor: function(ancestor) {
    var cns = ancestor.childNodes;
    if(!cns || cns.length < 1) return null;
    var cn;
    for(var i=0; i<cns.length; i++) {
      cn = cns[i];
      if(cn.nodeName == 'A') return cn;
    }
    return null;
  },

  /**
   * onClickFC
   *
   * Event handler for links in the formal correctionn header's ordered list of formal corrections.
   *
   * Scrolls into view the portion of the article containing the given correction (annotation) id
   * then opens the note (comment) window for the bound bug.
   *
   * @param e event
   */
  onClickFC: function(e) {
    var annId = dojo.attr(e.target, formalCorrectionConfig.annid);
    e.preventDefault();
    var fcn = ambra.corrections._findFrmlCrctnByAnnId(annId);
    if(fcn) {
      var annAnchor = ambra.corrections._getAnnAnchor(fcn);
      if(!annAnchor) throw 'Unable to resolve annotation anchor!';
      ambra.displayComment.show(annAnchor);
    }
  }
}

}

if(!dojo._hasResource["ambra.responsePanel"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["ambra.responsePanel"] = true;
/*
 * $HeadURL:: http://gandalf.topazproject.org/svn/head/plos/webapp/src/main/webapp/javas#$
 * $Id: responsePanel.js 5581 2008-05-02 23:01:11Z jkirton $
 *
 * Copyright (c) 2006-2008 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
/**
 * ambra.responsePanel
 * 
 * This class sets up and displays the response panel in the commentary page.  It also
 * sets up and displays the flagging panel.
 **/

dojo.provide("ambra.responsePanel");


ambra.responsePanel = {
  togglePanel:{},
  
  upperContainer: "",
  
  newPanel: "",
  
  targetForm: "",
  
  previousNode: "",
  
  setPanel: function(panel) {
    this.newPanel = panel;
  },
  
  setForm: function(formObj) {
    this.targetForm = formObj;
  },
  
  show: function(curNode, targetObj, targetElClassName, baseId, replyId, threadTitle, actionIndex) {
    this.setPanel(targetObj.widget);
    this.setForm(targetObj.form);
    targetObj.baseId = (baseId) ? baseId : "";
    targetObj.replyId = (replyId)? replyId : "";
    targetObj.actionIndex = (actionIndex) ? actionIndex : 0;
    this.upperContainer = ambra.domUtil.getFirstAncestorByClass(curNode, targetElClassName);
    this.upperContainer.style.display = "none";
    this.togglePanel.newPanel = this.newPanel;
    this.togglePanel.upperContainer = this.upperContainer;
    
    ambra.domUtil.insertAfter(this.newPanel, this.upperContainer, false);

    if (this.previousUpperContainer) this.previousUpperContainer.style.display = "block";

    if (targetObj.requestType == "flag"){
      this.resetFlaggingForm(targetObj);
    }
    
    this.newPanel.style.display = "block";

    if (threadTitle) {
      this.targetForm.responseTitle.value = 'RE: ' + threadTitle;
      this.targetForm.commentTitle.value = 'RE: ' + threadTitle;
    }
        
    this.previousUpperContainer = this.upperContainer;
  },
  
  hide: function() {
    if (this.togglePanel.newPanel) this.togglePanel.newPanel.style.display = "none";
    if (this.togglePanel.upperContainer) this.togglePanel.upperContainer.style.display = "block";
  },
  
  submit: function(targetObj) {
    submitResponseInfo(targetObj);
  },
  
  resetFlaggingForm: function(targetObj) {
    this.getFlagForm();  
    this.targetForm.reasonCode[0].checked = true;
    this.targetForm.comment.value = "";
    this.targetForm.responseArea.value = targetObj.responseCue;
    var submitMsg = targetObj.error;
    ambra.domUtil.removeChildren(submitMsg);
  },
  
  getFlagConfirm: function() {
    dojo.byId('flagForm').style.display = "none";
    dojo.byId('flagConfirm').style.display = "block";  
  },
  
  getFlagForm: function() {
    dojo.byId('flagForm').style.display = "block";
    dojo.byId('flagConfirm').style.display = "none";  
  }
}  

function submitResponseInfo(targetObj) {
  var submitMsg = targetObj.error;
  var targetForm = targetObj.form;
  ambra.domUtil.removeChildren(submitMsg);
  //ambra.formUtil.disableFormFields(targetForm);

  var urlParam = "";
  if (targetObj.requestType == "flag"){
    urlParam = targetObj.formAction[targetObj.actionIndex] + "?target=" + targetObj.baseId;
  }
  else if (targetObj.requestType == "new"){
    urlParam = targetObj.formAction;
    ambra.formUtil.disableFormFields(targetForm);
  }
  else { 
    urlParam = targetObj.formAction + "?root=" + targetObj.baseId + "&inReplyTo=" + targetObj.replyId;
    ambra.formUtil.disableFormFields(targetForm);
  }
   
  _ldc.show();
  dojo.xhrPost({
    url: (_namespace + urlParam),
    form: targetForm,
    handleAs:'json',
    error: function(response, ioArgs){
      handleXhrError(response, ioArgs);
      ambra.formUtil.enableFormFields(targetForm);
    },
    load: function(response, ioArgs){
      var jsonObj = response;
      if (jsonObj.actionErrors.length > 0) {
       var errorMsg = "";
       for (var i=0; i<jsonObj.actionErrors.length; i++) {
         errorMsg = errorMsg + jsonObj.actionErrors[i] + "\n";
       }
       var err = document.createTextNode(errorMsg);
       submitMsg.appendChild(err);
       ambra.formUtil.enableFormFields(targetForm);
       _ldc.hide();
     }
     else if (jsonObj.numFieldErrors > 0) {
       var fieldErrors = document.createDocumentFragment();
       for (var item in jsonObj.fieldErrors) {
         var errorString = "";
         var err = jsonObj.fieldErrors[item];
         if (err) {
           errorString += err;
           var error = document.createTextNode(errorString.trim());
           var brTag = document.createElement('br');

           fieldErrors.appendChild(error);
           fieldErrors.appendChild(brTag);
         }
       }
       submitMsg.appendChild(fieldErrors);
       ambra.formUtil.enableFormFields(targetForm);
       _ldc.hide();
     }
     else {
       if (targetObj.requestType == "flag"){
         _ldc.hide();
         ambra.responsePanel.getFlagConfirm();
       }
       else if (targetObj.requestType == "new"){
         var rootId = jsonObj.annotationId;
         window.location.href = _namespace + "/annotation/listThread.action?inReplyTo=" + rootId +"&root=" + rootId;
       }
       else {
         if (dojo.isIE)
           ambra.domUtil.insertAfter(ambra.responsePanel.togglePanel.newPanel, document.lastChild, false);
         getDiscussion(targetObj);
         ambra.responsePanel.hide();
         ambra.formUtil.textCues.reset(targetForm.responseArea, targetObj.responseCue);
         ambra.formUtil.enableFormFields(targetForm);
       }
     }
    }
   });
}

function getDiscussion(targetObj) {
  var refreshArea = dojo.byId(responseConfig.discussionContainer);

  _ldc.show();
  dojo.xhrGet({
    url: _namespace + "/annotation/listThreadRefresh.action?root=" + targetObj.baseId + "&inReplyTo=" + targetObj.baseId,
    error: function(response, ioArgs){
      handleXhrError(response, ioArgs);
    },
    load: function(response, ioArgs){
      refreshArea.innerHTML = response;
      _ldc.hide();
    }
  });
}


}

if(!dojo._hasResource["ambra.rating"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["ambra.rating"] = true;
/*
 * $HeadURL:: http://gandalf.topazproject.org/svn/head/plos/webapp/src/main/webapp/javas#$
 * $Id: rating.js 5581 2008-05-02 23:01:11Z jkirton $
 *
 * Copyright (c) 2006-2008 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
/**
  * ambra.rating
  *
  * This class uses a css-based ratings star and sets up the number of star 
  * rating to be displayed in the right hand column.  This also displays the 
  * rating dialog.
  *
  **/
dojo.provide("ambra.rating");


ambra.rating = {
  rateScale: 5,
  
  init: function() {
  },
  
  show: function(action){
    if (action && action == 'edit') {
      getRatingsForUser();
    }
    else {
      _ratingDlg.show();
    }
    
    return false;
  },
  
  buildCurrentRating: function(liNode, rateIndex) {
    var ratedValue = (parseInt(rateIndex)/this.rateScale)*100;
    liNode.className += " pct" + ratedValue;
    dojox.data.dom.textContent(liNode, "Currently " + rateIndex + "/" + this.rateScale + " Stars");
  },
  
  buildDialog: function(jsonObj) {
    var ratingList = document.getElementsByTagAndClassName('ul', 'star-rating');
    
    // build rating stars
    for (var i=0; i<ratingList.length; i++) {
      var currentNode = ratingList[i];
      
      if (currentNode.className.match("edit") != null) {
        var rateChildNodes = currentNode.childNodes;
        var rateItem = currentNode.id.substr(4);
        rateItem = rateItem.charAt(0).toLowerCase() + rateItem.substring(1); 
        var rateItemCount = jsonObj[rateItem];
               
        var indexInt = 0;
        for (var n=0; n<rateChildNodes.length; n++) {
          var currentChild = rateChildNodes[n];
          if (currentChild.nodeName == "#text" && (currentChild.nodeValue.match(new RegExp("\n")) || currentChild.nodeValue.match(new RegExp("\r")))) {
            continue;
          }
          
          if (currentChild.className.match("average") != null || ratingList[i].className.match("overall-rating") != null) {
            continue;
          }
          
          if(currentChild.className.match("current-rating")) {
            this.buildCurrentRating(currentChild, rateItemCount);
            firstSet = true;
            continue;
          }
          
          if (indexInt < rateItemCount) {
            currentChild.onmouseover = function() { ambra.rating.hover.on(this); }
            currentChild.onmouseout  = function() { ambra.rating.hover.off(this); }
            
            indexInt++;
          }
        }
        
        _ratingsForm[rateItem].value = jsonObj[rateItem];
        
      }
    }
    
    // add title
    if (jsonObj.commentTitle != null) {
      _ratingsForm.commentTitle.value = jsonObj.commentTitle;
      _ratingsForm.cTitle.value = jsonObj.commentTitle;
    }
    
    // add comments
    if (jsonObj.comment) {
      _ratingsForm.comment.value = jsonObj.comment;
      _ratingsForm.cArea.value = jsonObj.comment;
    }
  },
  
  resetDialog: function() {
    var ratingList = document.getElementsByTagAndClassName('li', 'current-rating');
    
    // build rating stars
    for (var i=0; i<ratingList.length; i++) {
        if (ratingList[i].className.match("average") != null || ratingList[i].className.match("overall-rating") != null) {
          continue;
        }
        
        ratingList[i].className = ratingList[i].className.replaceStringArray(" ", "pct", "pct0");
    }
    
    ambra.formUtil.textCues.reset(_ratingTitle, _ratingTitleCue);
    ambra.formUtil.textCues.reset(_ratingComments, _ratingCommentCue);
    
  },
  
  hover: {
    on: function(node) {
      var sibling = ambra.domUtil.firstSibling(node);
      sibling.style.display = "none"
    },
    
    off: function(node) {
      var sibling = ambra.domUtil.firstSibling(node);
      sibling.style.display = "block";
    }
  },
  
  setRatingCategory: function(node, categoryId, rateNum) {
    _ratingsForm[categoryId].value = rateNum;
    var sibling = ambra.domUtil.firstSibling(node.parentNode);
    var rateStyle = "pct" + (parseInt(rateNum) * 20);  
    sibling.className = sibling.className.replaceStringArray(" ", "pct", rateStyle);
    this.buildCurrentRating(sibling, rateNum);
  }
}
  
function getRatingsForUser() {
  var targetUri = _ratingsForm.articleURI.value;
  dojo.xhrGet({
    url: _namespace + "/rate/secure/getRatingsForUser.action?articleURI=" + targetUri,
    handleAs:'json',
    error: function(response, ioArgs){
      handleXhrError(response, ioArgs);
    },
    load: function(response, ioArgs){
       var jsonObj = response;
       if (jsonObj.actionErrors.length > 0) {
         var errorMsg = "";
         for (var i=0; i<jsonObj.actionErrors.length; i++) {
           errorMsg = errorMsg + jsonObj.actionErrors[i] + "\n";
         }
         alert("ERROR: " + errorMsg);
       }
       else {
         _ratingDlg.show();
         ambra.rating.buildDialog(jsonObj);
       }
    }
  });
}

function updateRating() {
  ambra.formUtil.disableFormFields(_ratingsForm);
  var submitMsg = dojo.byId('submitRatingMsg');
  ambra.domUtil.removeChildren(submitMsg);
  var articleUri = _ratingsForm.articleURI.value;

  _ldc.show();
  dojo.xhrPost({
    url: _namespace + "/rate/secure/rateArticle.action",
    handleAs:'json',
    form: _ratingsForm,
    sync: true,
    error: function(response, ioArgs){
      handleXhrError(response, ioArgs);
    },
    load: function(response, ioArgs){
     var jsonObj = response;
     if (jsonObj.actionErrors.length > 0) {
       var errorMsg = "";
       for (var i=0; i<jsonObj.actionErrors.length; i++) {
         errorMsg += jsonObj.actionErrors[i] + "\n";
       }
       var err = document.createTextNode(errorMsg);
       submitMsg.appendChild(err);
       ambra.formUtil.enableFormFields(_ratingsForm);
       _ldc.hide();
     }
     else if (jsonObj.numFieldErrors > 0) {
       var fieldErrors = document.createDocumentFragment();
       for (var item in jsonObj.fieldErrors) {
         var errorString = "";
         for (var ilist in jsonObj.fieldErrors[item]) {
           var err = jsonObj.fieldErrors[item][ilist];
           if (err) {
             errorString += err;
             var error = document.createTextNode(errorString.trim());
             var brTag = document.createElement('br');

             fieldErrors.appendChild(error);
             fieldErrors.appendChild(brTag);
           }
         }
       }
       submitMsg.appendChild(fieldErrors);
       ambra.formUtil.enableFormFields(_ratingsForm);
       _ldc.hide();
     }
     else {
       _ratingDlg.hide();
       getArticle("rating");
       ambra.formUtil.enableFormFields(_ratingsForm);
     }
   }
  });
}

function refreshRating(uri) {
  var refreshArea1 = dojo.byId(ratingConfig.ratingContainer + "1");
  var refreshArea2 = dojo.byId(ratingConfig.ratingContainer + "2");
  dojo.xhrGet({
    url: _namespace + "/rate/getUpdatedRatings.action?articleURI=" + uri,
    handleAs:'json',
    error: function(response, ioArgs){
     handleXhrError(response, ioArgs);
    },
    load: function(response, ioArgs){
     var jsonObj = response;
     var docFragment = document.createDocumentFragment();
     docFragment = data;
     refreshArea1.innerHTML = docFragment;
     refreshArea2.innerHTML = docFragment;
    }
  });
}

  
  
  

}

if(!dojo._hasResource["ambra.slideshow"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["ambra.slideshow"] = true;
/*
 * $HeadURL:: http://gandalf.topazproject.org/svn/head/plos/webapp/src/main/webapp/javas#$
 * $Id: slideshow.js 5581 2008-05-02 23:01:11Z jkirton $
 *
 * Copyright (c) 2006-2008 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
/**
 * ambra.slideshow
 * 
 * This class builds and controls the slideshow thumbnails to display the appropriate
 * images when the thumbnails are clicked.  When an image is selected, the container 
 * will adjust to envelope the enlarged image.  This also ensures that when the user 
 * clicks on the next and previous links, the highlighting of the thumbnails changes 
 * as well to correspond to the link and vice versa.
 **/

dojo.provide("ambra.slideshow");

ambra.slideshow = {
  imgS: "PNG_S",
  
  imgM: "PNG_M",
  
  imgL: "PNG_L",
  
  imgTif: "TIF",
  
  linkView: "",
  
  linkTiff: "",
  
  linkPpt: "",
  
  figImg: "",
  
  figImgWidth: "",
  
  figTitle: "",
  
  figCaption: "",
  
  targetDiv: "",
  
  activeItemIndex: "",
  
  itemCount: "",
  
  setLinkView: function(aObj) {
    this.linkView = aObj;
  },
  
  setLinkTiff: function(aObj) {
    this.linkTiff = aObj;
  },
  
  setLinkPpt: function(aObj) {
    this.linkPpt = aObj;
  },
  
  setFigImg: function(dObj) {
    this.figImg = dObj;
  },

  setFigTitle: function(dObj) {
    this.figTitle = dObj;
  },
  
  setFigCaption: function(dObj) {
    this.figCaption = dObj;
  },
  
  setInitialThumbnailIndex: function() {
    var tn = document.getElementsByTagAndClassName('div', 'figure-window-nav-item');
    this.itemCount = tn.length;
    
    for (var i=0; i<this.itemCount; i++) {
      if (tn[i].className.match('current')) {
        this.activeItemIndex = i;
      }
    }
  },
  
  show: function (obj, index) {
    if (this.linkView) this.linkView.href = slideshow[index].imageLargeUri + "&representation=" + this.imgL;
    if (this.linkTiff) this.linkTiff.href = slideshow[index].imageAttachUri + "&representation=" + this.imgTif;
    if (this.linkPpt) this.linkPpt.href  = slideshow[index].imageAttachUri + "&representation=" + this.imgM;
    
    if (this.figImg) {
      this.figImg.src = slideshow[index].imageUri + "&representation=" + this.imgM;
      this.figImg.title = slideshow[index].titlePlain;
    }
    
    if (this.figTitle) this.figTitle.innerHTML = slideshow[index].title;
    
    if (this.figCaption) this.figCaption.innerHTML = slideshow[index].description;
    
    var tbCurrent = document.getElementsByTagAndClassName('div', 'current');
    
    for (var i=0; i<tbCurrent.length; i++) {
      //alert("tbCurrent[" + i + "] = " + tbCurrent[i].nodeName + "\ntbCurrent[" + i + "].className = " + tbCurrent[i].className);
      //tbCurrent[i].className = tbCurrent[i].className.replace(/\-current/, "");
      dojo.removeClass(tbCurrent[i], "current");
    }
    
    var tbNew = obj.parentNode.parentNode;
    //tbNew.className = tbNew.className.concat("-current");
    dojo.addClass(tbNew, "current");
    
    if (index == 0) 
      dojo.addClass(dojo.byId("previous"), "hidden");
    else
      dojo.removeClass(dojo.byId("previous"), "hidden");
    
    if (index == this.itemCount-1) 
      dojo.addClass(dojo.byId("next"), "hidden");
    else
      dojo.removeClass(dojo.byId("next"), "hidden");
    
    
    this.activeItemIndex = index;
    
    window.setTimeout("ambra.slideshow.adjustViewerHeight()", 100);
    
  },
  
  showSingle: function (obj, index) {
    if (this.linkView) this.linkView.href = slideshow[index].imageLargeUri + "&representation=" + this.imgL;
    if (this.linkTiff) this.linkTiff.href = slideshow[index].imageAttachUri + "&representation=" + this.imgTif;
    if (this.linkPpt) this.linkPpt.href  = slideshow[index].imageAttachUri + "&representation=" + this.imgM;
    
    if (this.figImg) {
      this.figImg.src = slideshow[index].imageUri + "&representation=" + this.imgM;
      this.figImg.title = slideshow[index].titlePlain;
    }
    
    if (this.figTitle) this.figTitle.innerHTML = slideshow[index].title;
    
    if (this.figCaption) this.figCaption.innerHTML = slideshow[index].description;
    
    var tbCurrent = document.getElementsByTagAndClassName('div', 'figure-window-nav-item-current');
    
    for (var i=0; i<tbCurrent.length; i++) {
      //alert("tbCurrent[" + i + "] = " + tbCurrent[i].nodeName + "\ntbCurrent[" + i + "].className = " + tbCurrent[i].className);
      tbCurrent[i].className = tbCurrent[i].className.replace(/\-current/, "");
      
    }
    
    var tbNew = obj.parentNode.parentNode;
    tbNew.className = tbNew.className.concat("-current");
    
  },
  
  getFigureInfo: function (figureObj) {
    if (figureObj.hasChildNodes) {
      var caption = document.createDocumentFragment();
      
      for (var i=0; i<figureObj.childNodes.length; i++) {
        var child = figureObj.childNodes[i];
        
        if (child.nodeName == 'A') {
          for (var n=0; n<child.childNodes.length; n++) {
            var grandchild = child.childNodes[n];
            
            if (grandchild.nodeName == 'IMG') {
              this.figImg = grandchild;
            }
          }
        }
        else if (grandchild.nodeName == 'H5') {
          ambra.domUtil.copyChildren(grandchild, this.figTitle);
        }
        else {
          var newChild = grandchild;
          newChild.getAttributeNode('xpathlocation')='noSelect';
          caption.appendChild(newChild);
        }
      }
      
      ambra.domUtil.copyChildren(caption, this.figCaption);
      
      return;
    }
    else {
      return false;
    }
  },
  
  adjustContainerHeight: function (obj) {
    // get size viewport
    var viewport = dijit.getViewport();
    
    // get the offset of the container
    var objOffset = ambra.domUtil.getCurrentOffset(obj);
    
    // find the size of the container
    var objMb = dojo._getMarginBox(obj);

    var maxContainerHeight = viewport.h - (10 * objOffset.top);
    //alert("objOffset.top = " + objOffset.top + "\nviewport.h = " + viewport.h + "\nmaxContainerHeight = " + maxContainerHeight);
    
    obj.style.height = maxContainerHeight + "px";
    obj.style.overflow = "auto";
  },
  
  adjustViewerHeight: function() {
    var container1 = dojo.byId("figure-window-nav");
    var container2 = dojo.byId("figure-window-container");
    var container1Mb = dojo._getMarginBox(container1).height;
    var container2Mb = dojo._getMarginBox(container2).height;
    
    if (container1Mb > container2Mb) {
      container2.parentNode.style.height = container1Mb + "px";
      container1.style.borderRight = "2px solid #ccc";
      container2.style.borderLeft = "none";
    }
    else {
      container2.parentNode.style.height = "auto";
      container1.style.borderRight = "none";
      container2.style.borderLeft = "2px solid #ccc";
    }    
  },
  
  adjustViewerWidth: function(figureWindow, maxWidth) {
    var imageMarginBox = dojo._getMarginBox(ambra.slideshow.figureImg);
    imageWidth = imageMarginBox.width;
    ambra.domUtil.setContainerWidth(figureWindow, imageWidth, maxWidth, 1);
  },

  showPrevious: function(obj) {
    if (this.activeItemIndex <= 0) {
      return false;
    }
    else {
      var newIndex = this.activeItemIndex - 1;
      var newTnObj = dojo.byId('tn' + newIndex);
      this.show(newTnObj, newIndex);
      
      if (newIndex == 0) 
        dojo.addClass(obj, 'hidden');
      
      if (this.activeItemIndex == this.itemCount-1)
        dojo.removeClass(dojo.byId('next'), 'hidden');
        
      this.activeItemIndex = newIndex;
    }
  },
  
  showNext: function(obj) {
    if (this.activeItemIndex == this.itemCount-1) {
      return false;
    }
    else {
      var newIndex = this.activeItemIndex + 1;
      var newTnObj = dojo.byId('tn' + newIndex);
      this.show(newTnObj, newIndex);
      
      if (newIndex == this.itemCount-1) 
        dojo.addClass(obj, 'hidden');
      
      if (this.activeItemIndex == 0)
        dojo.removeClass(dojo.byId('previous'), 'hidden');
        
      this.activeItemIndex = newIndex;
    }
  },
  
  openViewer: function(url) {
    var newWindow = window.open(url,'plosSlideshow','directories=no,location=no,menubar=no,resizable=yes,status=no,scrollbars=yes,toolbar=no,height=600,width=800');
    
    return false;
  },
  
  closeReturn: function() {
    self.close();
    window.opener.focus();
  }
}  

}

dojo.provide("dojo.nls.ambra_xx");dojo.provide("dijit.nls.loading");dijit.nls.loading._built=true;dojo.provide("dijit.nls.loading.xx");dijit.nls.loading.xx={"loadingState":"Loading...","errorState":"Sorry, an error occurred"};dojo.provide("dijit.nls.common");dijit.nls.common._built=true;dojo.provide("dijit.nls.common.xx");dijit.nls.common.xx={"buttonOk":"OK","buttonCancel":"Cancel","buttonSave":"Save","itemClose":"Close"};
dojo.provide("dojo.nls.ambra_ROOT");dojo.provide("dijit.nls.loading");dijit.nls.loading._built=true;dojo.provide("dijit.nls.loading.ROOT");dijit.nls.loading.ROOT={"loadingState":"Loading...","errorState":"Sorry, an error occurred"};dojo.provide("dijit.nls.common");dijit.nls.common._built=true;dojo.provide("dijit.nls.common.ROOT");dijit.nls.common.ROOT={"buttonOk":"OK","buttonCancel":"Cancel","buttonSave":"Save","itemClose":"Close"};
dojo.provide("dojo.nls.ambra_en");dojo.provide("dijit.nls.loading");dijit.nls.loading._built=true;dojo.provide("dijit.nls.loading.en");dijit.nls.loading.en={"loadingState":"Loading...","errorState":"Sorry, an error occurred"};dojo.provide("dijit.nls.common");dijit.nls.common._built=true;dojo.provide("dijit.nls.common.en");dijit.nls.common.en={"buttonOk":"OK","buttonCancel":"Cancel","buttonSave":"Save","itemClose":"Close"};
dojo.provide("dojo.nls.ambra_en-us");dojo.provide("dijit.nls.loading");dijit.nls.loading._built=true;dojo.provide("dijit.nls.loading.en_us");dijit.nls.loading.en_us={"loadingState":"Loading...","errorState":"Sorry, an error occurred"};dojo.provide("dijit.nls.common");dijit.nls.common._built=true;dojo.provide("dijit.nls.common.en_us");dijit.nls.common.en_us={"buttonOk":"OK","buttonCancel":"Cancel","buttonSave":"Save","itemClose":"Close"};
dojo.i18n._preloadLocalizations("dojo.nls.ambra", ["xx","ROOT","en","en-us"]);
