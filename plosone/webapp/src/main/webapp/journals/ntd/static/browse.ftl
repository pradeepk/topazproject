<!-- begin : main content -->
<div id="content" class="static">

  <h1>Browse Articles</h1>

  <p class="intro">Neglected Topical Diseases posts new content as often as once 
  per week. You can browse articles three ways:</p>

  <@s.url action="browse"    namespace="/article" field="date" includeParams="none" id="browseDateURL"/>
  <@s.url action="browse"    namespace="/article"              includeParams="none" id="browseSubjectURL"/>
  <@s.url action="browseToc" namespace="/article" field="toc"  includeParams="none" id="browseTocURL"/>

  <ul>
    <li>
      <@s.a href="${browseDateURL}" title="NTDs | Browse by Publication Date">
        By Publication Date</@s.a>
        - Browse articles by choosing a specific week or month of publication.
    </li>
    <li>
      <@s.a href="${browseSubjectURL}" title="NTDs | Browse by Subject">By Subject</@s.a>
      - Browse articles published in a specific subject area.
    </li>
    <li>
      <a href="${browseTocURL}" title="NTDs | Browse by Table of Contents">By Table of Contents</a>
      -  Browse articles published in a specific volume/issue.
    </li>
  </ul>
</div>
<!-- end : main contents -->
