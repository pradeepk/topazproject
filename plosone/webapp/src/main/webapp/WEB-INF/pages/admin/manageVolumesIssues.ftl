<html>
  <head>
    <title>PLoS ONE: Administration: Manage Virtual Journals : Volumes / Issues</title>
  </head>
  <body>
    <h1>PLoS ONE: Administration: Manage Virtual Journals : Volumes / Issues</h1>

    <@s.url id="adminTop" namespace="/admin" action="adminTop"/>
    <@s.url id="manageVirtualJournals" namespace="/admin" action="manageVirtualJournals"/>
    <p style="text-align: right">
      Return to <@s.a href="${adminTop}">Admin Top</@s.a>,
      <@s.a href="${manageVirtualJournals}">Manage Virtual Journals</@s.a>
    </p>
    <br />

    <hr />

    <fieldset>
      <legend><b>Messages</b></legend>
      <p>
        <#list actionMessages as message>
          ${message} <br/>
        </#list>
      </p>
    </fieldset>
    <br />

    <hr />

    <fieldset>
      <legend><b>${journal.key}</b> (${journal.getEIssn()!""})</legend>

      <!-- create a Volume -->
      <fieldset>
        <legend><b>Create Volume</b></legend>

        <@s.form id="manageVolumesIssues_createVolume" name="manageVolumesIssues_createVolume"
          namespace="/admin" action="manageVolumesIssues" method="post">

          <@s.hidden name="journalKey" label="Journal Key" required="true"
            value="${journal.key}"/>
          <@s.hidden name="journalEIssn" label="Journal eIssn" required="true"
            value="${journal.getEIssn()}"/>
          <@s.hidden name="manageVolumesIssuesAction" label="Action" required="true"
            value="CREATE_VOLUME"/>

          <@s.textfield name="doi"         label="DOI (human friendly)"  size="72" required="true"/><br />
          <@s.textfield name="displayName" label="DisplayName"           size="72" required="true"/><br />
          <@s.textfield name="image"       label="Image (URI)"           size="72" /><br />
          <@s.textfield name="prev"        label="Previous Volume (DOI)" size="72" /><br />
          <@s.textfield name="next"        label="Next Volume (DOI)"     size="72" /><br />
          <br />
          <@s.textfield name="aggregation" label="Issues"                size="100"/><br />
          <br />
          <@s.submit value="Create Volume"/>
        </@s.form>
      </fieldset>

      <#list volumes as volume>
        <fieldset>
          <legend><b>${volume.displayName}</b> (${volume.key})</legend>

          <#list getIssues(volume.key) as issue>
            <fieldset>
              <legend><b>${issue.displayName}</b> (${issue.key})</legend>
            </fieldset>
          </#list>
        </fieldset>
      </#list>
    </fieldset>

  </body>
</html>
