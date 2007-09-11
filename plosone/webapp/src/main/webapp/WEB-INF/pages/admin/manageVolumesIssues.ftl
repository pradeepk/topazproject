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

      <!-- list Volumes -->
      <fieldset>
        <legend><b>Existing Volumes</b></legend>
        <table border="1" cellpadding="2" cellspacing="0">
          <tr>
            <th>Display Name</th>
            <th>DOI</th>
            <th>Image</th>
            <th>Previous</th>
            <th>Next</th>
            <th>Issues</th>
          </tr>
          <#list volumes as volume>
            <tr>
              <th>${volume.displayName}</th>
              <td>${volume.id}</td>
              <td>${volume.image!""}</td>
              <td>${volume.prevVolume!""}</td>
              <td>${volume.nextVolume!""}</td>
              <td>${volume.simpleCollection!""}</td>
            </tr>
          </#list>
        </table>
      </fieldset>

      <!-- list Issues -->
      <fieldset>
        <legend><b>Existing Issues</b></legend>
        <table border="1" cellpadding="2" cellspacing="0">
          <tr>
            <th>Display Name</th>
            <th>DOI</th>
            <th>Image</th>
            <th>Previous</th>
            <th>Next</th>
            <th>Articles</th>
          </tr>
          <#list issues as issue>
            <tr>
              <th>${issue.displayName}</th>
              <td>${issue.id}</td>
              <td>${issue.image!""}</td>
              <td>${issue.prevIssue!""}</td>
              <td>${issue.nextIssue!""}</td>
              <td>${issue.simpleCollection!""}</td>
            </tr>
          </#list>
        </table>
      </fieldset>

    </fieldset>

  </body>
</html>
