/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 *
 * Modified from code part of Fedora. It's license reads:
 * License and Copyright: The contents of this file will be subject to the
 * same open source license as the Fedora Repository System at www.fedora.info
 * It is expected to be released with Fedora version 2.2.
 * Copyright 2006 by The Technical University of Denmark.
 * All rights reserved.
 */
package org.topazproject.fedoragsearch.topazlucene;

import java.io.IOException;
import java.io.StringReader;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.StringTokenizer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.SimpleFragmenter;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;

import dk.defxws.fedoragsearch.server.errors.GenericSearchException; // Wraps RMIException

// Stuff added by topaz:
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.NoSuchElementException;

import org.apache.lucene.search.Hit;

import org.topazproject.configuration.ConfigurationStore; // Wraps commons-config initialization

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This actually queries the lucene database.
 * 
 * @author  Eric Brown and <a href='mailto:gsp@dtv.dk'>Gert</a>
 * @version $Id$
 */
public class Statement {
  private static final Log      log              = LogFactory.getLog(Statement.class);
  
  private static final String   INDEX_PATH       = TopazConfig.INDEX_PATH;
  private static final String   INDEX_NAME       = TopazConfig.INDEX_NAME;
  private static final String   ANALYZER_NAME    = TopazConfig.ANALYZER_NAME;
  private static final long     CACHE_EXPIRATION = TopazConfig.CACHE_EXPIRATION;
  private static final List     DEFAULT_FIELDS   = TopazConfig.DEFAULT_FIELDS;
  private static       String[] DEFAULT_FIELD_ARRAY;
  
  private static IndexSearcher searcher;
  private static Analyzer analyzer;

  /** Map of open queries to hits */
  private static TemporaryCache queryCache = new TemporaryCache(CACHE_EXPIRATION);

  static {
    // Get the one instance of IndexSearcher that we need
    try {
      searcher = new IndexSearcher(INDEX_PATH);
      log.info("Using lucene index: " + INDEX_PATH);
    } catch (IOException ioe) {
      log.error("Unable to open lucene index: " + INDEX_PATH, ioe);
    }
    
    // Get the configured analyzer
    try {
      analyzer = (Analyzer) Class.forName(ANALYZER_NAME).newInstance();
      log.debug("Using lucene analyzer: " + ANALYZER_NAME);
    } catch (ClassNotFoundException cnfe) {
      log.error("Unable to find lucene analyzer class: " + ANALYZER_NAME, cnfe);
    } catch (InstantiationException ie) {
      log.error("Unable to instantiate lucene analyzer: " + ANALYZER_NAME, ie);
    } catch (IllegalAccessException iae) {
      log.error("Access violation instantiating lucene analyzer: " + ANALYZER_NAME, iae);
    }

    // Copy DEFAULT_FIELDS to DEFAULT_FIELD_ARRAY -- ONCE
    DEFAULT_FIELD_ARRAY = new String[DEFAULT_FIELDS.size()];
    for (int i = 0; i < DEFAULT_FIELD_ARRAY.length; i++)
      DEFAULT_FIELD_ARRAY[i] = (String) DEFAULT_FIELDS.get(i);
  }

  /**
   * send a query to lucene and get back a set of hits.
   *
   * Most of these parameters are from configuration. The parameters that usually change
   * are the first 3: queryString, startRecord and maxResults.
   *
   * Builds up a list of hits that are available to be returned. 
   *
   * @param queryString the lucene query
   * @param startRecord the first record to return
   * @param maxResults the maximum number of results to return
   *
   * @param snippetsMax the maximum number of snippets to return per hit
   * @param fieldMaxLength the maximum length of a field name to return in a hit
   * @returns an xml document with a list of hits
   * @throws GenericSearchException if the is a problem getting the results
   */
  ResultSet executeQuery(String   queryString, 
                         long     startRecord, 
                         int      maxResults,
                         int      snippetsMax,
                         int      fieldMaxLength) throws GenericSearchException {
    if (searcher == null)
      throw new GenericSearchException("Failed to initialize Lucene");
    if (analyzer == null)
      throw new GenericSearchException("Failed to initialize Lucene analyzier");

    try {
      Results results = getResults(queryString);

      StringBuffer resultXml = new StringBuffer();
      resultXml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
        .append("<lucenesearch ")
        .append("   xmlns:dc=\"http://purl.org/dc/elements/1.1/")
        .append("\" query=\"").append(URLEncoder.encode(queryString, "UTF-8"))
        .append("\" indexName=\"").append(INDEX_NAME)
        .append("\" hitPageStart=\"").append(startRecord)
        .append("\" hitPageSize=\"").append(maxResults)
        .append("\" hitTotal=\"").append(results.size).append("\">"); // unreliable

      results.iter.gotoRecord((int)startRecord);
      int cnt = 0;
      
      while (maxResults-- > 0 && results.iter.hasNext()) {
        cnt++;
        Hit hit = (Hit) results.iter.next();
        Document doc = hit.getDocument();
        resultXml.append("<hit no=\"").append(cnt)
                 .append("\" score=\"").append(hit.getScore()).append("\">");
        
        for (Enumeration e = doc.fields(); e.hasMoreElements(); ) {
          Field f = (Field) e.nextElement();
          resultXml.append("<field name=\"").append(f.name()).append("\"");

          // Build snippets
          String snippets = null;
          if (snippetsMax > 0) {
            SimpleHTMLFormatter formatter = new SimpleHTMLFormatter(
              "<span class=\"highlight\">", "</span>");
            QueryScorer scorer = new QueryScorer(results.query, f.name());
            Highlighter highlighter = new Highlighter(formatter, scorer);
            Fragmenter fragmenter = new SimpleFragmenter(fieldMaxLength);
            highlighter.setTextFragmenter(fragmenter);
            TokenStream tokenStream =
              analyzer.tokenStream(f.name(), new StringReader(f.stringValue()));
            snippets =
              highlighter.getBestFragments(tokenStream, f.stringValue(), snippetsMax, " ... ");
            if (snippets != null && !snippets.equals(""))
              resultXml.append(" snippet=\"yes\">").append(snippets);
          }
          if (snippets == null || snippets.equals(""))
            if (fieldMaxLength > 0 && f.stringValue().length() > fieldMaxLength) {
              // Add portion of field to resultXml
              String snippet = f.stringValue().substring(0, fieldMaxLength);
              int iamp = snippet.lastIndexOf("&"); // TODO: Why are you doing this???
              if (iamp > -1 && iamp > fieldMaxLength-8)
                snippet = snippet.substring(0, iamp); // TODO: bug?? shouldn't we add to resultXml?
              else
                resultXml.append(">").append(snippet).append(" ... ");
            } else
              resultXml.append(">").append(f.stringValue()); // Just add field name
          resultXml.append("</field>");
        }
        resultXml.append("</hit>");
      }
      resultXml.append("</lucenesearch>");

      if (log.isDebugEnabled())
        log.debug(queryString + ":found " + cnt + " of " + (cnt + maxResults)
                  + " hits starting at " + startRecord);
      
      // Wrap resultXml String in an object to return it
      return new ResultSet(resultXml);
    } catch (IOException e) {
      throw new GenericSearchException("Some IOException", e);
    } catch (ParseException e) {
      throw new GenericSearchException("Some ParseException", e);
    } finally {
      if (searcher != null)
        try {
          searcher.close();
        } catch (IOException e) {
          // TODO: log something!
        }
      return null;
    }
  }
    
  void close() throws GenericSearchException {
  }

  
  /** Return stuff from cache */
  private static class Results {
    Query           query;
    int             size;
    CachingIterator iter;
  }

  private Results getResults(String queryString) throws IOException, ParseException {
    // See if we already have the query
    String userName = SearchContext.getUserName();
    String cacheKey = userName + "|" + queryString;
    // TODO: these should be SoftReferences
    Results results = (Results) queryCache.get(cacheKey);
    if (results != null)
      return results;

    Query query = getQuery(queryString);
    // Rewriting the query might make it more efficient
    // TODO: Not sure why Gert commented out
    // query.rewrite(IndexReader.open(indexPath));
    Hits hits = searcher.search(query);
    results = new Results();
    results.iter = new CachingIterator(new GuardedIterator(hits.iterator(), new TopazHitGuard()));
    results.size = hits.length(); // Approx before Guard - but returned in result string anyway
    results.query = query; // Needed by QueryScorer
    queryCache.put(cacheKey, results);

    if (log.isDebugEnabled())
      log.debug(cacheKey + ": " + results.size + " hits");
    
    return results;
  }

  private Query getQuery(String queryString) throws ParseException {
    // Build QueryParser or MultiFieldQueryParser based on # of default fields
    if (DEFAULT_FIELD_ARRAY.length == 0)
      throw new ParseException("No default fields. Must have at least 1.");
    else if (DEFAULT_FIELD_ARRAY.length == 1)
      return (new QueryParser((String) DEFAULT_FIELD_ARRAY[0], analyzer)).parse(queryString);
    else
      return (new MultiFieldQueryParser(DEFAULT_FIELD_ARRAY, analyzer)).parse(queryString);
  }
}
