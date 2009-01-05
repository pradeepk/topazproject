dojo.provide("ambra.reporting.socialBookmarkChart");

dojo.require("ambra.domUtil");
dojo.require("ambra.formUtil");

dojo.require("dojox.charting.Chart2D");
dojo.require("dojox.charting.plot2d.StackedBars");
dojo.require("dojox.charting.action2d.Highlight");
dojo.require("dojox.charting.action2d.Shake");
dojo.require("dojox.charting.action2d.Tooltip");
dojo.require("dojox.charting.widget.Legend");

dojo.require("dojo.io.script");
dojo.require("ambra.reporting.base");

(function(){

	dojo.declare("ambra.reporting.citationsHistory", ambra.reporting.base, {
		
		load:function(response, args)
		{
			var doi = response.article.doi;
			var index = args.chartIndex;
			var series = [ new Object() ];
			var seriesCreated = 0;
			
			charts[index].addSeries("CrossRef", [ 0, 5, 5, 6, 7, 7]);
			charts[index].addSeries("PostGenrmic", [0, 3, 6, 21, 21, 23]);
			charts[index].addSeries("Slashdot",[1, 4, 7, 10, 11, 12]);

			var anim_1a = new dojox.charting.action2d.Shake(charts[index], "default");
			var anim_1b = new dojox.charting.action2d.Highlight(charts[index], "default");
			var anim_1c = new dojox.charting.action2d.Tooltip(charts[index], "default");
			
			this.legend = new dojox.charting.widget.Legend({chart: charts[index]}, this.legendID);

			charts[index].render();
		},
		
		create:function(objectID,legendID, doi) 
		{
			var i = charts.length;
			
			dojo.byId(objectID).innerHTML = "<div class=\"doi\">" + doi + "</div>";
			
			charts[i] = new dojox.charting.Chart2D(objectID);
			charts[i].addPlot("default", { type: "StackedColumns", tension:2, labels:true, gap: 5 } );
			charts[i].addAxis("x", {fixLower: "major", fixUpper: "major", label: "5esf" });
			charts[i].addAxis("y", {vertical: true, fixLower: "major", fixUpper: "major", min: 0});
			charts[i].setTheme(this.theme);
			
			this.legendID = legendID;
			
			this.getChartData("articles/" + doi + ".json?detailed=2", i, this.load);
		}		
	});
})();
