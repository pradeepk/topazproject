dojo.provide("ambra.reporting.socialBookmarkChart");

dojo.require("ambra.domUtil");
dojo.require("ambra.formUtil");

dojo.require("dojox.charting.Chart2D");
dojo.require("dojox.charting.plot2d.Pie");
dojo.require("dojox.charting.action2d.Highlight");
dojo.require("dojox.charting.action2d.MoveSlice");
dojo.require("dojox.charting.action2d.Tooltip");
dojo.require("dojox.charting.widget.Legend");

dojo.require("dojo.io.script");
dojo.require("ambra.reporting.base");

(function(){

	dojo.declare("ambra.reporting.citations", ambra.reporting.base, {
		
		clicked:function(args)
		{
			alert('clicked');
		},
		
		load:function(response, args)
		{
			var doi = response.article.doi;
			var index = args.chartIndex;
			var series = [ new Object() ];
			var seriesCreated = 0;
			
			for(var a = 0; a < response.article.sources.length; a++) 
			{
				var curSource = response.article.sources[a];
			
				//TODO: find why curSource is sometimes null
				if(curSource != null) {
					series[seriesCreated] = { 
						y:curSource.count, 
						stroke: "black",
						//tooltip: curSource.source + ": "  + curSource.count,
						tooltip: curSource.count,
						text:curSource.source,
						click:this.clicked
					};
					
					charts[index].connectToPlot(series[seriesCreated], "onclick", this.clicked);
					
					seriesCreated++;
				}
			}
			
			//series[seriesCreated] = { y:null, text: null, color: null };

			charts[index].addSeries("Article CItations",series);
			
			var anim_a = new dojox.charting.action2d.MoveSlice(charts[index], "default");
			var anim_b = new dojox.charting.action2d.Highlight(charts[index], "default");
			var anim_c = new dojox.charting.action2d.Tooltip(charts[index], "default");
			
			charts[index].render();
			
			this.legend = new dojox.charting.widget.Legend({chart:charts[index]}, this.legendID);

		},
		
		create:function(objectID,legendID,doi) 
		{
			var i = charts.length;
			
			dojo.byId(objectID).innerHTML = "<div class=\"doi\">" + doi + "</div>"; //test
			  
			charts[i] = new dojox.charting.Chart2D(objectID);
			charts[i].addPlot("default", {
					type: "Pie",
					font: "normal normal 8pt Tahoma",
					fontColor: "black",
					labelOffset: -30//,
					//shadows: { dx: 2, dy: 2, dw: 2 }
					//labels: true,
					//ticks: false,
					//fixed: false,
					//precision: 1,
					//labelOffset: 20//,
					//labelStyle: "default"//,      // default/rows/auto
					//htmlLabels: true           // use HTML to draw labels
				});
			
			charts[i].setTheme(this.theme);
			
			this.getChartData("articles/" + doi + ".json?history=0&citations=1", i, this.load);
			this.legendID = legendID;
		}		
	});
})();
