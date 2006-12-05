<#function isFound collection value>
  <#list collection as element>
    <#if element = value>
      <#return "true">
    </#if>
  </#list>
  <#return "false">
</#function>
<#if Parameters.tabId?exists>
   <#assign tabId = Parameters.tabId>
<#else>
   <#assign tabId = "">
</#if>


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
        			<#if tabId?has_content>
        				<input type="checkbox" value="checkAllWeekly" name="checkAllWeekly" onfocus="topaz.horizontalTabs.setTempValue(this);" onclick="topaz.formUtil.selectAllCheckboxes(this, document.userAlerts.weeklyAlerts); topaz.horizontalTabs.checkValue(this);" /> Check all weekly alerts
        			<#else>
        				<input type="checkbox" value="checkAllWeekly" name="checkAllWeekly" onclick="topaz.formUtil.selectAllCheckboxes(this, document.userAlerts.weeklyAlerts);" /> Check all weekly alerts
        			</#if>
        			</label>
        		</li>
        		<li>
        			<label for="checkAllMonthly">
         			<#if tabId?has_content>
        				<input type="checkbox" value="checkAllMonthly" name="checkAllMonthly" onfocus="topaz.horizontalTabs.setTempValue(this);" onclick="topaz.formUtil.selectAllCheckboxes(this, document.userAlerts.monthlyAlerts); topaz.horizontalTabs.checkValue(this);" /> Check all monthly alerts
        			<#else>
        				<input type="checkbox" value="checkAllMonthly" name="checkAllMonthly" onclick="topaz.formUtil.selectAllCheckboxes(this, document.userAlerts.monthlyAlerts);" /> Check all monthly alerts
        			</#if>
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
				<#if tabId?has_content>
	              <@ww.checkbox name="weeklyAlerts" onfocus="topaz.horizontalTabs.setTempValue(this);" onclick="topaz.horizontalTabs.checkValue(this); topaz.formUtil.selectCheckboxPerCollection(this.form.checkAllWeekly, this.form.weeklyAlerts);" onchange="topaz.horizontalTabs.checkValue(this);" fieldValue="${category.key}" value="${isFound(weeklyAlerts, category.key)}"/>
				<#else>
	              <@ww.checkbox name="weeklyAlerts" onclick="topaz.formUtil.selectCheckboxPerCollection(this.form.checkAllWeekly, this.form.weeklyAlerts);" fieldValue="${category.key}" value="${isFound(weeklyAlerts, category.key)}"/>
				</#if>
                Weekly </label>
              </#if>
            </li>

            <li>
              <#if category.monthlyAvailable>
                <label for="${category.key}">
    			<#if tabId?has_content>
	              <@ww.checkbox name="monthlyAlerts" onfocus="topaz.horizontalTabs.setTempValue(this);" onclick="topaz.horizontalTabs.checkValue(this); topaz.formUtil.selectCheckboxPerCollection(this.form.checkAllMonthly, this.form.monthlyAlerts);" onchange="topaz.horizontalTabs.checkValue(this);"  fieldValue="${category.key}" value="${isFound(monthlyAlerts, category.key)}"/>
    			<#else>
                  <@ww.checkbox name="monthlyAlerts" onclick="topaz.formUtil.selectCheckboxPerCollection(this.form.checkAllMonthly, this.form.monthlyAlerts);"  fieldValue="${category.key}" value="${isFound(monthlyAlerts, category.key)}"/>
    			</#if>
                  Monthly </label>
              <#else>
              </#if>
            </li>
          </ol>
        </li>
      </#list>
		</ol>
		<br clear="all" />
      <div class="btnwrap"><input type="button" id="formSubmit" name="formSubmit" value="Save" tabindex="200"/></div>
	</fieldset>
  </@ww.form>


