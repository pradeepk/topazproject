<#function isFound collection value>
  <#list collection as element>
    <#if element = value>
      <#return "true">
    </#if>
  </#list>
  <#return "false">
</#function>

<div id="content">
<h2>Alerts</h2>
	<p><strong>instruction Title   Text.</strong> General Instructions- Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam erat volutpat. Ut wisi enim ad minim veniam, quis nostrud exerci tation ullamcorper suscipit lobortis nisl ut aliquip ex ea commodo consequat. Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat, vel illum dolore eu feugiat nulla facilisis at vero eros et accumsan et iusto odio dignissim qui blandit praesent luptatum zzril delenit augue duis dolore te feugait nulla facilisi.</p>
	<p>Field marked with an <span class="required">*</span> are required. </p>

  <@ww.form action="saveUserAlerts" namespace="/user/secure" method="post" cssClass="pone-form" method="post" title="Alert Form" name="userAlerts">

  <fieldset>
		<legend>Choose your alerts  </legend>
		<ol>
			<li>Check back soon for more PLoS One alerts</li>
      <@ww.textfield name="alertEmailAddress" label="Email address for alerts" required="true"/>
			<li>
        	<ol>
        		<li class="alerts-title">&nbsp;</li>
        		<li>
        			<label for="checkAllWeekly">
        				<input type="checkbox" name="checkAllWeekly" onclick="topaz.formUtil.selectAllCheckboxes(this, document.userAlerts.weeklyAlerts);" /> Check all weekly alerts
        			</label>
        		</li>
        		<li>
        			<label for="checkAllMonthly">
        				<input type="checkbox" name="checkAllMonthly" onclick="topaz.formUtil.selectAllCheckboxes(this, document.userAlerts.monthlyAlerts);" /> Check all monthly alerts
        			</label>
        		</li>
        	</ol>
			</li>
      <#list categoryBeans as category>
        <li>
          <ol>
            <li class="alerts-title">${category.name}</li>
            <li>
              <#if category.weeklyAvailable>
                <label for="${category.key}">
              <@ww.checkbox name="weeklyAlerts" fieldValue="${category.key}" value="${isFound(weeklyAlerts, category.key)}"/>
                Weekly </label>
              </#if>
            </li>

            <li>
              <#if category.monthlyAvailable>
                <label for="${category.key}">
              <@ww.checkbox name="monthlyAlerts" fieldValue="${category.key}" value="${isFound(monthlyAlerts, category.key)}"/>
                  Monthly </label>
              <#else>
              </#if>
            </li>
          </ol>
        </li>
      </#list>
		</ol>
		<br clear="all" />
			<input type="submit" name="cancel" id="cancel" value="Cancel" tabindex="199">
      <@ww.submit name="Save" tabindex="200"/>
	</fieldset>
  </@ww.form>

</div>
<!-- end : main content wrapper -->