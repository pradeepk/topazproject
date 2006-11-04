dojo.provide("dojo.gfx.common");

dojo.require("dojo.gfx.color");
dojo.require("dojo.lang.declare");
dojo.require("dojo.lang.extras");
dojo.require("dojo.dom");

//dojo.gfx.defaultRenderer.init();

dojo.lang.mixin(dojo.gfx, {
	defaultPath:     {type: "path",     path: ""},
	defaultPolyline: {type: "polyline", points: []},
	defaultRect:     {type: "rect",     x: 0, y: 0, width: 100, height: 100, r: 0},
	defaultEllipse:  {type: "ellipse",  cx: 0, cy: 0, rx: 100, ry: 200},
	defaultCircle:   {type: "circle",   cx: 0, cy: 0, r: 100},
	defaultLine:     {type: "line",     x1: 0, y1: 0, x2: 100, y2: 100},
	defaultImage:    {type: "image",    width: 0, height: 0, src: ""},

	defaultStroke: {color: "black", width: 1, cap: "butt", join: 4},
	defaultLinearGradient: {type: "linear", x1: 0, y1: 0, x2: 100, y2: 100, 
		colors: [{offset: 0, color: "black"}, {offset: 1, color: "white"}]},
	defaultRadialGradient: {type: "radial", cx: 0, cy: 0, r: 100, 
		colors: [{offset: 0, color: "black"}, {offset: 1, color: "white"}]},
	defaultPattern: {type: "pattern", x: 0, y: 0, width: 0, height: 0, src: ""},

	normalizeColor: function(color){
		return (color instanceof dojo.gfx.color.Color) ? color : new dojo.gfx.color.Color(color);
	},
	normalizeParameters: function(existed, update){
		if(update){
			var empty = {};
			for(var x in existed){
				if(x in update && !(x in empty)){
					existed[x] = update[x];
				}
			}
		}
		return existed;
	},
	makeParameters: function(defaults, update){
		if(!update) return dojo.lang.shallowCopy(defaults, true);
		var result = {};
		for(var i in defaults){
			if(!(i in result)){
				result[i] = dojo.lang.shallowCopy((i in update) ? update[i] : defaults[i], true);
			}
		}
		return result;
	},
	formatNumber: function(x, addSpace){
		var val = x.toString();
		if(val.indexOf("e") >= 0){
			val = x.toFixed(4);
		}else{
			var point = val.indexOf(".");
			if(point >= 0 && val.length - point > 5){
				val = x.toFixed(4);
			}
		}
		if(x < 0){
			return val;
		}
		return addSpace ? " " + val : val;
	}
});

// this is a Shape object, which knows how to apply graphical attributes and a transformation
dojo.gfx.Shape = function(){
	// underlying node
	this.rawNode = null;
	// abstract shape object
	this.shape = null;
	// transformation matrix
	this.matrix  = null;
	// graphical attributes
	this.fillStyle   = null;
	this.strokeStyle = null;
	// virtual group structure
	this.parent = null;
	this.parentMatrix = null;
	// bounding box
	this.bbox = null;
};

dojo.lang.extend(dojo.gfx.Shape, {
	// trivial getters
	getNode:        function(){ return this.rawNode; },
	getShape:       function(){ return this.shape; },
	getTransform:   function(){ return this.matrix; },
	getFill:        function(){ return this.fillStyle; },
	getStroke:      function(){ return this.strokeStyle; },
	getParent:      function(){ return this.parent; },
	getBoundingBox: function(){ return this.bbox; },
	getEventSource: function(){ return this.rawNode; },
	
	// empty settings
	setShape:  function(shape) { return this; },	// ignore
	setStroke: function(stroke){ return this; },	// ignore
	setFill:   function(fill)  { return this; },	// ignore
	
	// z-index
	moveToFront: function(){ return this; },		// ignore
	moveToBack:  function(){ return this; },		// ignore

	// apply transformations
	setTransform: function(matrix){
		this.matrix = dojo.gfx.matrix.clone(matrix ? dojo.gfx.matrix.normalize(matrix) : dojo.gfx.identity, true);
		return this._applyTransform();
	},
	
	// apply left & right transformation
	applyRightTransform: function(matrix){
		return matrix ? this.setTransform([this.matrix, matrix]) : this;
	},
	applyLeftTransform: function(matrix){
		return matrix ? this.setTransform([matrix, this.matrix]) : this;
	},

	// a shortcut for apply-right
	applyTransform: function(matrix){
		return matrix ? this.setTransform([this.matrix, matrix]) : this;
	},
	
	// virtual group methods
	remove: function(silently){
		if(this.parent){
			this.parent.remove(this, silently);
		}
		return this;
	},
	_setParent: function(parent, matrix){
		this.parent = parent;
		return this._updateParentMatrix(matrix);
	},
	_updateParentMatrix: function(matrix){
		this.parentMatrix = matrix ? dojo.gfx.matrix.clone(matrix) : null;
		return this._applyTransform();
	},
	_getRealMatrix: function(){
		return this.parentMatrix ? new dojo.gfx.matrix.Matrix2D([this.parentMatrix, this.matrix]) : this.matrix;
	}
});

dojo.declare("dojo.gfx.VirtualGroup", dojo.gfx.Shape, {
	initializer: function() {
		this.children = [];
	},
	
	// group management
	add: function(shape){
		var oldParent = shape.getParent();
		if(oldParent){
			oldParent.remove(shape, true);
		}
		this.children.push(shape);
		return shape._setParent(this, this._getRealMatrix());
	},
	remove: function(shape, silently){
		var i = 0;
		for(; i < this.children.length; ++i){
			if(this.children[i] == shape){
				if(silently){
					// skip for now
				}else{
					shape._setParent(null, null);
				}
				this.children.splice(i, 1);
				break;
			}
		}
		return this;
	},
	
	// apply transformation
	_applyTransform: function(){
		var matrix = this._getRealMatrix();
		for(var i = 0; i < this.children.length; ++i){
			this.children[i]._updateParentMatrix(matrix);
		}
		return this;
	}
});

// this is a Surface object
dojo.gfx.Surface = function(){
	// underlying node
	this.rawNode = null;
};

dojo.lang.extend(dojo.gfx.Surface, {
	getEventSource: function(){ return this.rawNode; }
});
