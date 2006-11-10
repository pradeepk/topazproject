dojo.provide("dojo.widget.RegionalDialog");

dojo.require("dojo.widget.*");
dojo.require("dojo.widget.ContentPane");
dojo.require("dojo.event.*");
dojo.require("dojo.gfx.color");
dojo.require("dojo.html.layout");
dojo.require("dojo.html.display");
dojo.require("dojo.widget.Dialog");		// for RegionalDialog
dojo.require("dojo.html.iframe");

// summary
//	Mixin for widgets implementing a modal dialog
dojo.declare(
	"dojo.widget.RegionalDialogBase", 
	[dojo.widget.DialogBase],
	{
		isContainer: true,

		// static variables
		shared: {bg: null, bgIframe: null},

		// String
		//	provide a focusable element or element id if you need to
		//	work around FF's tendency to send focus into outer space on hide
		focusElement: "",

		// String
		//	color of viewport when displaying a dialog
		bgColor: "black",
		
		// Number
		//	opacity (0~1) of viewport color (see bgColor attribute)
		bgOpacity: 0.4,

		// Boolean
		//	if true, readjusts the dialog (and dialog background) when the user moves the scrollbar
		followScroll: true,
		
		markerNode: null,
		
		tipDownNode: null,
		
		tipUpNode: null,
		
		activeNode: null,

		templatePath: dojo.uri.dojoUri("src/widget/templates/RegionalDialog.html"),

		trapTabs: function(/*Event*/ e){
			// summary
			//	callback on focus
			if(e.target == this.tabStartOuter) {
				if(this._fromTrap) {
					this.tabStart.focus();
					this._fromTrap = false;
				} else {
					this._fromTrap = true;
					this.tabEnd.focus();
				}
			} else if (e.target == this.tabStart) {
				if(this._fromTrap) {
					this._fromTrap = false;
				} else {
					this._fromTrap = true;
					this.tabEnd.focus();
				}
			} else if(e.target == this.tabEndOuter) {
				if(this._fromTrap) {
					this.tabEnd.focus();
					this._fromTrap = false;
				} else {
					this._fromTrap = true;
					this.tabStart.focus();
				}
			} else if(e.target == this.tabEnd) {
				if(this._fromTrap) {
					this._fromTrap = false;
				} else {
					this._fromTrap = true;
					this.tabStart.focus();
				}
			}
		},

		clearTrap: function(/*Event*/ e) {
			// summary
			//	callback on blur
			var _this = this;
			setTimeout(function() {
				_this._fromTrap = false;
			}, 100);
		},

		postCreate: function() {
			// summary
			//	if the target mixin class already defined postCreate,
			//	dojo.widget.ModalDialogBase.prototype.postCreate.call(this)
			//	should be called in its postCreate()
			with(this.domNode.style){
				position = "absolute";
				zIndex = 999;
				display = "none";
				overflow = "visible";
			}
			var b = dojo.body();
			b.appendChild(this.domNode);

			if(!this.shared.bg){
				this.shared.bg = document.createElement("div");
				this.shared.bg.className = "dialogUnderlay";
				with(this.shared.bg.style){
					position = "absolute";
					left = top = "0px";
					zIndex = 998;
					display = "none";
				}
				this.setBackgroundColor(this.bgColor);
				b.appendChild(this.shared.bg);
				this.shared.bgIframe = new dojo.html.BackgroundIframe(this.shared.bg);
			}
		},

		setBackgroundColor: function(/*String*/ color) {
			// summary
			//	changes background color specified by "bgColor" parameter
			//	usage:
			//		setBackgrounColor("black");
			//		setBackgroundColor(0xff, 0xff, 0xff);
			if(arguments.length >= 3) {
				color = new dojo.gfx.color.Color(arguments[0], arguments[1], arguments[2]);
			} else {
				color = new dojo.gfx.color.Color(color);
			}
			this.shared.bg.style.backgroundColor = color.toString();
			return this.bgColor = color;
		},

		setBackgroundOpacity: function(/*Number*/ op) {
			// summary
			//	changes background opacity set by "bgOpacity" parameter
			if(arguments.length == 0) { op = this.bgOpacity; }
			dojo.html.setOpacity(this.shared.bg, op);
			try {
				this.bgOpacity = dojo.html.getOpacity(this.shared.bg);
			} catch (e) {
				this.bgOpacity = op;
			}
			return this.bgOpacity;
		},

		_sizeBackground: function() {
			if(this.bgOpacity > 0) {
				
				var viewport = dojo.html.getViewport();
				var h = viewport.height;
				var w = viewport.width;
				with(this.shared.bg.style){
					width = w + "px";
					height = h + "px";
				}
				
				var scroll_offset = dojo.html.getScroll().offset;
				this.shared.bg.style.top = scroll_offset.y + "px";
				this.shared.bg.style.left = scroll_offset.x + "px";
				
				// process twice since the scroll bar may have been removed
				// by the previous resizing
				var viewport = dojo.html.getViewport();
				if (viewport.width != w) { this.shared.bg.style.width = viewport.width + "px"; }
				if (viewport.height != h) { this.shared.bg.style.height = viewport.height + "px"; }
			}
		},

		_showBackground: function() {
			if(this.bgOpacity > 0) {
				this.shared.bg.style.display = "block";
			}
		},
		
		_changeTipDirection: function(isTipDown, xShift) {
			var dTip = this.tipDownNode;
			var dTipu = this.tipUpNode;
			
			dTip.className = dTip.className.replace(/\son/, "");
    	dTipu.className = dTipu.className.replace(/\son/, ""); 
			
			var targetTip = (isTipDown) ? dTip : dTipu;
			
			targetTip.className = targetTip.className.concat(" on");
 			targetTip.style.marginLeft = (xShift) ? xShift + "px" : "auto";
		},
		
		placeModalDialog: function() {
			var scroll_offset = dojo.html.getScroll().offset;
			var viewport_size = dojo.html.getViewport();
			//var dialog_marker = dojo.byId(djConfig.regionalDialogMarker);
			var dialog_marker = this.markerNode;
			
			var curleft = curtop = 0;
			if (dialog_marker.offsetParent) {
				curleft = dialog_marker.offsetLeft
				curtop = dialog_marker.offsetTop
				while (dialog_marker = dialog_marker.offsetParent) {
					curleft += dialog_marker.offsetLeft  
					curtop += dialog_marker.offsetTop
				}
			}
			
			// find the size of the dialog
			var mb = dojo.html.getMarginBox(this.containerNode);
			
			var mbWidth = mb.width;
			var mbHeight = mb.height;
			var vpWidth = viewport_size.width;
			var vpHeight = viewport_size.height;
			var scrollX = scroll_offset.x;
			var scrollY = scroll_offset.y;
			
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
        // big error. Do something about it!
      }
			
			// Default values put the box generally above and to the right of the annotation "bug"
      var xTip = curleft - (tipWidth / 2);
      var yTip = curtop - tipHeight - (tipHeight/4);
      
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

      if (y < yMin) {
        tipDown = false; // flip the tip

        yTip = curtop + bugHeight - (tipHeight/4);
        y = yTip + tipHeight;
        
        if (y > yMax) {
          // this is bad, because it means that there isn't enough room above or below the annotation for the dialog box, the tip, and/or the minimum margins
        }
      }
      
      var xTipDiff = curleft - x;
      
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

		},
		
		showModalDialog: function() {
			// summary
			//	call this function in show() of subclass
			if (this.followScroll && !this._scrollConnected){
				this._scrollConnected = true;
				dojo.event.connect(window, "onscroll", this, "_onScroll");
			}
			
			this.setBackgroundOpacity();
			this._sizeBackground();
			this._showBackground();
		},

		hideModalDialog: function(){
			// summary
			//	call this function in hide() of subclass

			// workaround for FF focus going into outer space
			if (this.focusElement) { 
				dojo.byId(this.focusElement).focus(); 
				dojo.byId(this.focusElement).blur();
			}
			
			this.shared.bg.style.display = "none";
			this.shared.bg.style.width = this.shared.bg.style.height = "1px";

			if (this._scrollConnected){
				this._scrollConnected = false;
				dojo.event.disconnect(window, "onscroll", this, "false");
			}
		},
		
		_onScroll: function(){
			var scroll_offset = dojo.html.getScroll().offset;
			this.shared.bg.style.top = scroll_offset.y + "px";
			this.shared.bg.style.left = scroll_offset.x + "px";
			this.placeModalDialog();
		},

		checkSize: function() {
			if(this.isShowing()){
				this._sizeBackground();
				this.placeModalDialog();
				this.onResized();
			}
		}
	});

// summary
//	Pops up a modal dialog window, blocking access to the screen and also graying out the screen
//	Dialog is extended from ContentPane so it supports all the same parameters (href, etc.)
dojo.widget.defineWidget(
	"dojo.widget.RegionalDialog",
	[dojo.widget.ContentPane, dojo.widget.RegionalDialogBase],
	{
		// Integer
		//	number of seconds for which the user cannot dismiss the dialog
		blockDuration: 0,
		
		// Integer
		//	if set, this controls the number of seconds the dialog will be displayed before automatically disappearing
		lifetime: 0,

		show: function() {
			if(this.lifetime){
				this.timeRemaining = this.lifetime;
				if(!this.blockDuration){
					dojo.event.connect(this.shared.bg, "onclick", this, "hide");
				}else{
					dojo.event.disconnect(this.shared.bg, "onclick", this, "hide");
				}
				if(this.timerNode){
					this.timerNode.innerHTML = Math.ceil(this.timeRemaining/1000);
				}
				if(this.blockDuration && this.closeNode){
					if(this.lifetime > this.blockDuration){
						this.closeNode.style.visibility = "hidden";
					}else{
						this.closeNode.style.display = "none";
					}
				}
				this.timer = setInterval(dojo.lang.hitch(this, "_onTick"), 100);
			}

			this.showModalDialog();
			dojo.widget.Dialog.superclass.show.call(this);
		},

		onLoad: function(){
			// when href is specified we need to reposition
			// the dialog after the data is loaded
			this.placeModalDialog();
			dojo.widget.Dialog.superclass.onLoad.call(this);
		},
		
		fillInTemplate: function(){
			// dojo.event.connect(this.domNode, "onclick", this, "killEvent");
		},

		hide: function(){
			this.hideModalDialog();
			dojo.widget.Dialog.superclass.hide.call(this);

			if(this.timer){
				clearInterval(this.timer);
			}
		},
		
		setTimerNode: function(node){
			// summary
			//	specify into which node to write the remaining # of seconds
			// TODO: make this a parameter too
			this.timerNode = node;
		},

		setCloseControl: function(node) {
			// summary
			//	specify which node is the close button for this dialog
			// TODO: make this a parameter too
			this.closeNode = node;
			dojo.event.connect(node, "onclick", this, "hide");
		},

		setShowControl: function(node) {
			// summary
			//	when specified node is clicked, show this dialog
			// TODO: make this a parameter too
			dojo.event.connect(node, "onclick", this, "show");
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
		},
		
		_onTick: function(){
			// summary
			//	callback every second that the timer clicks
			if(this.timer){
				this.timeRemaining -= 100;
				if(this.lifetime - this.timeRemaining >= this.blockDuration){
					dojo.event.connect(this.shared.bg, "onclick", this, "hide");
					if(this.closeNode){
						this.closeNode.style.visibility = "visible";
					}
				}
				if(!this.timeRemaining){
					clearInterval(this.timer);
					this.hide();
				}else if(this.timerNode){
					this.timerNode.innerHTML = Math.ceil(this.timeRemaining/1000);
				}
			}
		}
	}
);
