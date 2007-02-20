	<div id="browseNav">

		<div>
			<form class="browseForm" action="browse.action" method="get" name="browseForm">
				<fieldset>
					<legend>How would you like to browse?</legend>
					<ol>
						<li><label for="date"><input type="radio" name="field" id="date" checked="checked" /> By Publication Date</label></li>
						<li><label for="subject"><input onclick="document.browseForm.submit();" type="radio" name="field" id="subject" /> By Subject</label></li>
					</ol>
				</fieldset>
			</form>
		</div>

		<ul>
			<#if year == -1 &&  month == -1 && day == -1>
			<#assign infoText = "in the <strong>past 7 days</strong>">
			<li class="current">Past 7 days</li>
			<#else>
		  	<@ww.url id="browseDateURL" action="browse" namespace="/article" field="${field}" includeParams="none"/>
			<li><@ww.a href="%{browseDateURL}">Past 7 days</@ww.a></li>
			</#if>
			<#if month == -2>
			<li class="current">Past 30 days</li>
			<#assign infoText = "in the <strong>past 30 days</strong>">
			<#else>
			  <@ww.url id="browseDateURL" action="browse" namespace="/article" field="${field}" month="-2" includeParams="none"/>
			<li><@ww.a href="%{browseDateURL}">Past 30 days</@ww.a></li>
			</#if>
			<#if month == -3>
			<li class="current">Past 90 days</li>
			<#assign infoText = "in the <strong>past 90 days</strong>">
			<#else>
		  	<@ww.url id="browseDateURL" action="browse" namespace="/article" field="${field}" month="-3" includeParams="none"/>			
			<li><@ww.a href="%{browseDateURL}">Past 90 days</@ww.a></li>
			</#if>
		</ul>

		<ol>
		<#list articleDates?size-1 .. 0 as i>
			<#assign oneYear = articleDates[i]>
			<#assign aDay = oneYear[0][0]>
			<li>${aDay?string("yyyy")}
			<#list oneYear as oneMonth>
				<ol>
				<#assign oneDay = oneMonth[0]>
					<#if i == year && oneMonth_index == month && day == -1>
						<li class="current">
		  			<#assign infoText = "in <strong>" + oneDay?string("MMM") + " " + oneDay?string("yyyy") + "</strong>">
					<#else>
						<li>
					</#if>						
				  <@ww.url id="monthURL" action="browse" namespace="/article" field="${field}" year="${i}" month="${oneMonth_index}" includeParams="none"/>								
					<@ww.a href="%{monthURL}">${oneDay?string("MMM")}</@ww.a></li>
				<#list oneMonth as oneDay>
					<#if i == year && oneMonth_index == month && oneDay_index == day>
					<li class="current">
	  			<#assign infoText = "on <strong>" + oneDay?string("dd") + " " + oneDay?string("MMM") + " " + oneDay?string("yyyy") + "</strong>">
					<#else>
					<li>
					</#if>
				  <@ww.url id="dayURL" action="browse" namespace="/article" field="${field}" year="${i}" month="${oneMonth_index}" day="${oneDay_index}" includeParams="none"/>													
					<@ww.a href="%{dayURL}">${oneDay?string("dd")}</@ww.a></li>
				</#list>
				</ol>
			</#list>
			</li>
		</#list>
		</ol>

	</div> <!-- browse nav-->

