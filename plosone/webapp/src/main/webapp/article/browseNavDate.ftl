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

    <#assign infoText = "">
    <ul>
      <#if year == -1 && month == -1 && day == -1>
      <#assign infoText = "in the <strong>past week</strong>">
      <li class="current">Past week</li>
      <#else>
      <@s.url id="browseDateURL" action="browse" namespace="/article" field="${field}" includeParams="none"/>
      <li><@s.a href="%{browseDateURL}">Past week</@s.a></li>
      </#if>
      <#if month == -2>
      <li class="current">Past month</li>
      <#assign infoText = "in the <strong>past month</strong>">
      <#else>
      <@s.url id="browseDateURL" action="browse" namespace="/article" field="${field}" month="-2" includeParams="none"/>
      <li><@s.a href="%{browseDateURL}">Past month</@s.a></li>
      </#if>
      <#if month == -3>
      <li class="current">Past 3 months</li>
      <#assign infoText = "in the <strong>past 3 months</strong>">
      <#else>
      <@s.url id="browseDateURL" action="browse" namespace="/article" field="${field}" month="-3" includeParams="none"/>
      <li><@s.a href="%{browseDateURL}">Past 3 months</@s.a></li>
      </#if>
    </ul>

    <ol>
    <#list articleDates?keys?reverse as curYear>
      <#assign curYearStr = curYear?string("#") >
      <li>${curYearStr}</li>
      <li><#list articleDates(curYear)?keys?reverse as curMon>
        <#assign curMonStr = curMon?date("MM")?string("MMM") >
        <ol>
          <#if curYear == year && curMon == month && day == -1>
            <li class="current">
            <#assign infoText = "in <strong>" + curMonStr + " " + curYearStr + "</strong>">
          <#else>
            <li>
          </#if>
          <@s.url id="monthURL" action="browse" namespace="/article" field="${field}" year="${curYear?c}" month="${curMon?c}" includeParams="none"/>
          <@s.a href="%{monthURL}">${curMonStr}</@s.a></li>
        <#list articleDates(curYear)(curMon) as curDay>
          <#assign curDayStr = curDay?string("00") >
          <#if curYear == year && curMon == month && curDay == day>
          <li class="current">
          <#assign infoText = "on <strong>" + curDayStr + " " + curMonStr + " " + curYearStr + "</strong>">
          <#else>
          <li>
          </#if>
          <@s.url id="dayURL" action="browse" namespace="/article" field="${field}" year="${curYear?c}" month="${curMon?c}" day="${curDay?c}" includeParams="none"/>
          <@s.a href="%{dayURL}">${curDayStr}</@s.a></li>
        </#list>
        </ol>
      </#list>
      </li>
    </#list>
    </ol>

  </div> <!-- browse nav-->

