dojo.provide("ambra.reporting.base");

dojo.require("ambra.domUtil");
dojo.require("ambra.formUtil");

dojo.require("dojox.charting.Chart2D");
dojo.require("dojox.charting.Chart3D");
dojo.require("dojox.gfx.fx");
dojo.require("dojox.fx");
dojo.require("dojox.charting.plot3d.Cylinders");
dojo.require("dojox.charting.plot3d.Bars");
dojo.require("dojo.io.script");
dojo.require("dojox.charting.themes.Grasshopper");

(function(){

	dojo.declare("ambra.reporting.base", null, {
	
		constructor:function(host,username,password){
			this.host = host;
			this.username = username;
			this.password = password;
			this.theme = dojox.charting.themes.Grasshopper;
		},
		
		destroy:function(){},
		
		/*
			host is the host and  to get the JSON response from
			chartIndex is the  current index of the charts[] array
			callback is the method that populates the chart of  "chartIndex"
		*/
		getChartData:function(request, chartIndex, callBack) 
		{		
			var url = "";
			
			if(this.username && this.password) 
			{
				url = "http://" + this.username + ":" + this.password + "@" + this.host + "/" + request;
			} else {
				url = "http://" + this.host + "/" + request;
			}

			console.log(url);

			var getArgs = {
				callbackParamName: "callback",
				url:url,
				chartIndex:chartIndex,
				caller:this,
				callback:callBack,
				load: function(response, args) {
					//Callback is the method being called.
					//args.args.caller is the object the method is part of
					callBack.call(args.args.caller,response, args.args);
					return response;
				},
				
				error: function(error, args){
					console.warn("error!", error);
					return response;
				}
			};

			var deferred = dojo.io.script.get(getArgs);
		}		
	});
})();

if(!charts) {
	var charts = [ {} ];
}
