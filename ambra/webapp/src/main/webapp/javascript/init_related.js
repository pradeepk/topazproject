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
    //TODO: Move this server setting to a configuration file
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
  
  if (response == null)
  {
    setError('pubMedRelatedErr');
    return;
  }

  var pubMedID = 0;
  
  if (response.article.pub_med) {
    pubMedID = response.article.pub_med;
    dojo.byId('pubMedRelatedURL').href="http://www.ncbi.nlm.nih.gov/sites/entrez?Db=pubmed&DbFrom=pubmed&Cmd=Link&LinkName=pubmed_pubmed&LinkReadableName=Related%20Articles&IdsFromResult=" + pubMedID + "&ordinalpos=1&itool=EntrezSystem2.PEntrez.Pubmed.Pubmed_ResultsPanel.Pubmed_RVCitation";
    dojo.fx.wipeIn({ node:'pubMedRelatedLI', duration: 500 }).play();
  }
}

function setCites(response, args)
{
  dojo.byId('relatedCites').style.display = 'none';
  dojo.fadeOut({ node:'relatedCitesSpinner', duration: 1000 }).play();

  if (response == null)
  {
    setError('relatedCites');
    return;
  }

  var numCitesRendered = 0;
  var doi = escape(dojo.byId('doi').value);

  if (response.article.sources.length > 0) {
    var html = "<ul>";

    for(var a = 0; a < response.article.sources.length; a++)
    {
      var url = "";

      /**
       * TODO: We really want to have the ALM server set these sources
       * In the meantime we'll set them here.
       *
       * I figure we'll append a DOI to these urls with the right parameter name
       * Or perhaps append an ID given to us from the ALM server?
       *
       * Nulling out any of the following values should stop the
       * item from being displayed. 
      */
      switch (response.article.sources[a].source) {
        case "CrossRef":
            //Example of what it would like like to pass the doi to the website
            url = "http://www.crossref.org/?paramName=" + doi; //+ response.article.sources[a].source.someID
            //Example of what it would like if we got a response from the ALM server
            //url = "http://www.crossref.org/?paramName=" + response.article.sources[a].source.someID;
            //Example of null:
            //url = null;
          break;
        case "PubMed":
            url = "http://www.ncbi.nlm.nih.gov/pubmed/?paramName="; //+ doi (the article DOI)
          break;
        case "Scopus":
            url = "http://www.scopus.com/scopus/home.url?paramName=";
          break;
        case "Citeulike":
            url = "http://www.citeulike.org/?paramName=";
          break;
      }

      //Only list links that HAVE DEFINED URLS
      if (url) {
        html = html + "<li><a href=\"" + url + "\">" + response.article.sources[a].source + "(" + response.article.sources[a].count + ")</a></li>";
        numCitesRendered++;
      }
    }
    
    html = html + "</ul>"
  }

  if (numCitesRendered == 0){
    html = "<ul><li>No related cites found</li></ul>";
  }

  dojo.byId('relatedCites').innerHTML = html;
  dojo.fx.wipeIn({ node:'relatedCites', duration: 500 }).play();
}

function setRelatedBlogs(response, args)
{
  var html = "";

  dojo.byId('relatedBlogPosts').style.display = 'none';
  dojo.fadeOut({ node:'relatedBlogSpinner', duration: 1000 }).play();

  if (response == null)
  {
    setError('relatedBlogPosts');
    return;
  }

  var numBlogsRendered = 0;
  var doi = escape(dojo.byId('doi').value);

  if (response.article.sources.length > 0) {
    html = "<ul>";

    for(var a = 0; a < response.article.sources.length; a++)
    {
      var url = "";

      /**
       * TODO: We really want to have the ALM server set these sources
       * In the meantime we'll set them here.
       *
       * I figure we'll append a DOI to these urls with the right parameter name
       * Or perhaps append an ID given to us from the ALM server?
       *
       * Nulling out any of the following values should stop the
       * item from being displayed.
      */
      switch(response.article.sources[a].source) {
        case "Bloglines":
            url = "http://www.crossref.org/?paramName=" + doi;
            //url = "http://www.crossref.org/?paramName=" + response.article.sources[a].source.someID;
          break;
        case "Nature":
            url = "http://www.nature.com/?paramName=";
          break;
        case "Postgenomic":
            url = "http://www.postgenomic.com/?paramName=";
          break;
      }

      //Only list links that HAVE DEFINED URLS
      if (url) {
        html = html + "<li><a href=\"" + url + "\">" + response.article.sources[a].source + "(" + response.article.sources[a].count + ")</a></li>";
        numBlogsRendered++;
      }
    }

    html = html + "</ul>"
  } else

  if (numBlogsRendered == 0) {
    html = "<ul><li>No related blog enrties found</li></ul>"; 
  }

  dojo.byId('relatedBlogPosts').innerHTML = html;
  dojo.fx.wipeIn({ node:'relatedBlogPosts', duration: 1000 }).play();
}

function setError(textID)
{
  dojo.byId(textID).innerHTML = '<span class="inlineError"><img src="../../images/icon_error.gif"/>&nbsp;Unable to retrieve this data at this time.</span>';
  dojo.fx.wipeIn({ node:textID, duration: 1000 }).play();
}