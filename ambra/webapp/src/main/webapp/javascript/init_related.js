/*
 * $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2009 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
dojo.require("dojo.fx");

dojo.addOnLoad(function()
  {
    var almService = new ambra.alm('alm-dev.plos.org');
    var doi = escape(dojo.byId('doi').value);

    doi = doi.replace(new RegExp('/', 'g'),'%2F');

    almService.getRelatedBlogs(doi, setRelatedBlogs);
    almService.getCites(doi, setCites);
    almService.getIDs(doi, setIDs);
  }
);

function setIDs(response, args)
{
  dojo.fadeOut({ node:'relatedArticlesSpinner', duration: 1000 }).play();

  if(response == null)
  {
    setError('relatedArticlesSpinner');
    return;
  }

  var pubMedID = 0;
  
  if(response.article.pub_med) {
    pubMedID = response.article.pub_med;
    dojo.byId('pubMedRelatedURL').href="http://www.ncbi.nlm.nih.gov/sites/entrez?Db=pubmed&DbFrom=pubmed&Cmd=Link&LinkName=pubmed_pubmed&LinkReadableName=Related%20Articles&IdsFromResult=" + pubMedID + "&ordinalpos=1&itool=EntrezSystem2.PEntrez.Pubmed.Pubmed_ResultsPanel.Pubmed_RVCitation";
    dojo.fx.wipeIn({ node:'pubMedRelatedLI', duration: 500 }).play();
  }
}

function setCites(response, args)
{
  dojo.fadeOut({ node:'relatedCitesSpinner', duration: 1000 }).play();
  dojo.byId('relatedCites').style.display = 'none';

  if(response == null)
  {
    setError('relatedCitesSpinner');
    return;
  }

  if(response.article.sources.length > 0) {
    var html = "<ul>";

    for(var a = 0; a < response.article.sources.length; a++)
    {
      html = html + "<li><a href=\"\">" + response.article.sources[a].source + "(" + response.article.sources[a].count + ")</a></li>";
    }
    
    html = html + "</ul>"
  } else {
    html = "<ul><li>No related cites found</li></ul>";
  }

  dojo.byId('relatedCites').innerHTML = html;
  dojo.fx.wipeIn({ node:'relatedCites', duration: 500 }).play();
}

function setRelatedBlogs(response, args)
{
  var html = "";

  dojo.fadeOut({ node:'relatedBlogSpinner', duration: 1000 }).play();
  dojo.byId('relatedBlogPosts').style.display = 'none';

  if(response == null)
  {
    setError('relatedBlogSpinner');
    return;
  }

  if(response.article.sources.length > 0) {
    html = "<ul>";

    for(var a = 0; a < response.article.sources.length; a++)
    {
      html = html + "<li><a href=\"\">" + response.article.sources[a].source + "(" + response.article.sources[a].count + ")</a></li>";
    }

    html = html + "</ul>"
  } else {
    html = "<ul><li>No related blog enrties found</li></ul>"; 
  }

  dojo.byId('relatedBlogPosts').innerHTML = html;
  dojo.fx.wipeIn({ node:'relatedBlogPosts', duration: 1000 }).play();
}

function setError(id)
{
  dojo.byId(id).src = 'broken.jpg';
  dojo.byId(id).title = 'An error occured requesting the';
}