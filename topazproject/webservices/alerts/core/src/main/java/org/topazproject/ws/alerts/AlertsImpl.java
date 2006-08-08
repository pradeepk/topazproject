/* $HeadURL::                                                                             $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.ws.alerts;

import java.io.IOException;
import java.rmi.RemoteException;
import javax.xml.rpc.ServiceException;
import java.util.Iterator;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.LinkedList;
import java.util.LinkedHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.authentication.ProtectedService;
import org.topazproject.mulgara.itql.ItqlHelper;
import org.topazproject.mulgara.itql.StringAnswer;
import org.topazproject.mulgara.itql.AnswerException;
import org.topazproject.mulgara.itql.Answer.QueryAnswer;
import org.topazproject.fedora.client.APIMStubFactory;
import org.topazproject.fedora.client.FedoraAPIM;

/** 
 * This provides the implementation of the alerts service.
 * 
 * @author Eric Brown
 */
public class AlertsImpl implements Alerts {
  private static final Log log = LogFactory.getLog(AlertsImpl.class);
  private static final String MODEL = "<rmi://localhost/fedora#ri>";
  private static final String FEED_ITQL =
    "select $doi $title $description $date from ${MODEL} where " +
    " $doi <dc:title> $title and " +
    " $doi <dc:description> $description and " +
    " $doi <dc:date> $date " +
    " ${args} " +
    " order by $date desc;";
  private static final String FIND_SUBJECTS_ITQL =
    "select '${doi}' $subject from ${MODEL} where <${doi}> <dc:subject> $subject;";
  private static final String FIND_AUTHORS_ITQL =
    "select '${doi}' $author from ${MODEL} where <${doi}> <dc:creator> $author;";
  private static final String XML_RESPONSE =
    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<articles>\n${articles}</articles>\n";
  private static final String XML_ARTICLE_TAG =
    "  <article>\n" +
    "    <doi>${doi}</doi>\n" +
    "    <title>${title}</title>\n" +
    "    <description>${description}</description>\n" +
    "    <date>${date}</date>\n" +
    "    ${authors}\n" +
    "    ${categories}\n" +
    "  </article>\n";

  private final AlertsPEP   pep;
  private final ItqlHelper  itql;
  private final FedoraAPIM  apim;

  /**
   * Class to stash article data in while reading from Kowari.
   */
  static class ArticleData {
    String doi;
    String title;
    String description;
    String date;
    List   authors;
    List   categories;
  }
  
  /**
   * Create a new alerts service instance.
   *
   * @param itqlService   the itql web-service
   * @param fedoraService the fedora web-service
   * @param pep           the policy-enforcer to use for access-control
   * @throws ServiceException if an error occurred locating the itql or fedora services
   * @throws IOException if an error occurred talking to the itql or fedora services
   */
  public AlertsImpl(ProtectedService itqlService, ProtectedService fedoraService, AlertsPEP pep)
      throws IOException, ServiceException {
    this.pep = pep;
    itql = new ItqlHelper(itqlService);
    apim = APIMStubFactory.create(fedoraService);
  }

  public String getFeed(String startDate, String endDate, String[] categories, String[] authors)
      throws RemoteException {
    // Build up a list of categories or authors to append to the query string
    LinkedList params = new LinkedList();
    
    if (categories != null && categories.length > 0) {
      for (int i = 0; i < categories.length; i++)
        params.add("$doi <dc:subject> '" + categories[i] + "' ");
    } else if (authors != null && authors.length > 0) {
      for (int i = 0; i < authors.length; i++)
        params.add("$doi <dc:creator> '" + authors[i] + "' ");
    }

    StringBuffer args = new StringBuffer();
    if (params.size() > 0) {
      args.append(" and (");
      for (Iterator i = params.iterator(); i.hasNext(); ) {
        args.append(i.next());
        if (i.hasNext())
          args.append(" or ");
      }
      args.append(")");
    }

    // TODO: Search by date (requires modification to ingestion to support datatypes)

    LinkedHashMap articles = new LinkedHashMap();
    try {
      // Initial query to get raw list of articles
      Map values = new HashMap();
      values.put("MODEL", AlertsImpl.MODEL);
      values.put("args", args.toString());
      String query = ItqlHelper.bindValues(AlertsImpl.FEED_ITQL, values);
      if (log.isDebugEnabled())
        log.debug(query);

      StringAnswer result = new StringAnswer(this.itql.doQuery(query));
      QueryAnswer answer = (QueryAnswer)result.getAnswers().get(0);
    
      for (Iterator rowIt = answer.getRows().iterator(); rowIt.hasNext(); ) {
        String[] row = (String[])rowIt.next();

        // TODO: Remove this check once we retrofit Kowari/Fedora to search on date
        String date = row[3];
        if (!isDateInRange(date, startDate, endDate))
          continue;
        
        ArticleData article = new ArticleData();
        article.doi         = row[0];
        article.title       = row[1];
        article.description = row[2];
        article.date        = row[3];
        articles.put(article.doi, article);
      }

      this.getDetails(articles);
    } catch (AnswerException ae) {
      throw new RemoteException("Error querying RDF", ae);
    }
        
    return this.buildXml(articles.values());
  }

  /**
   * Determine if a date is between two other dates.
   *
   * @param date is the date to test
   * @param startDate is the date to start searching from. If empty, start from begining of time
   * @param endDate is the date to search until. If empty, search until prsent date
   * @return true if the date is between startDate and endDate inclusive.
   */
  protected static boolean isDateInRange(String date, String startDate, String endDate) {
    if (startDate != null && startDate.length() > 0 && date.compareTo(startDate) < 0)
      return false;
    if (endDate != null && endDate.length() > 0 && date.compareTo(endDate) > 0)
      return false;
    return true;
  }
  
  /**
   * Given a list of articles, lookup their authors and categories.
   *
   * @param articles Map of articles to work with
   */
  protected void getDetails(Map articles) throws AnswerException, RemoteException {
    StringBuffer queryBuffer = new StringBuffer();

    // Build up set of queries
    for (Iterator i = articles.values().iterator(); i.hasNext(); ) {
      ArticleData article = (ArticleData)i.next();
      if (article == null)
        continue; // should never happen
      Map values = new HashMap();
      values.put("MODEL", AlertsImpl.MODEL);
      values.put("doi", article.doi);
      queryBuffer.append(ItqlHelper.bindValues(AlertsImpl.FIND_SUBJECTS_ITQL, values));
      queryBuffer.append(ItqlHelper.bindValues(AlertsImpl.FIND_AUTHORS_ITQL, values));
    }
    
    String results = this.itql.doQuery(queryBuffer.toString());
    List answers = (new StringAnswer(results)).getAnswers();
    
    if (log.isDebugEnabled()) {
      log.debug("Categories/Authors queries: " + queryBuffer.toString());
      log.debug("Categories/Authors XML Results: " + results);
    }

    for (Iterator answersIt = answers.iterator(); answersIt.hasNext(); ) {
      Object ans = answersIt.next();
      if (ans instanceof String) {
        // This is Ronald trying to be helpful. It isn't something we're interested in!
        continue;
      }
      
      QueryAnswer answer = (QueryAnswer)ans;
      List rows = answer.getRows();
      
      // If the query returned nothing, just move on
      if (rows.size() == 0)
        continue;

      // Column name represents type of query we did -- for categories or authors
      String column = answer.getVariables()[1];
      String doi = ((String[])rows.get(0))[0];
      ArticleData article = (ArticleData)articles.get(doi);
      if (article == null) {
        // We should never get this - means something changed underneath us
        log.warn("Didn't find article " + doi + " on " + column + " query");
        continue;
      }

      // Get the list of values we want
      List values = new LinkedList();
      for (Iterator rowIt = rows.iterator(); rowIt.hasNext(); )
        values.add(((String[])rowIt.next())[1]);
      if (column.equals("subject")) article.categories = values;
      else if (column.equals("author")) article.authors = values;
    }
  }

  /**
   * Build an XML string from a collection of ArticleData records.
   *
   * @param articles A collection of articles
   */
  protected String buildXml(Collection articles) {
    String articlesXml = "";
    for (Iterator articleIt = articles.iterator(); articleIt.hasNext(); ) {
      ArticleData article = (ArticleData)articleIt.next();

      StringBuffer authorsSb = new StringBuffer();
      if (article.authors != null && article.authors.size() > 0) {
        for (Iterator authorsIt = article.authors.iterator(); authorsIt.hasNext(); ) {
          authorsSb.append("      <author>");
          authorsSb.append(authorsIt.next());
          authorsSb.append("</author>\n");
        }
        authorsSb.insert(0, "<authors>\n");
        authorsSb.append("    </authors>");
      }

      StringBuffer categoriesSb = new StringBuffer();
      if (article.categories != null && article.categories.size() > 0) {
        for (Iterator categoriesIt = article.categories.iterator(); categoriesIt.hasNext(); ) {
          categoriesSb.append("      <category>");
          categoriesSb.append(categoriesIt.next());
          categoriesSb.append("</category>\n");
        }
        categoriesSb.insert(0, "<categories>\n");
        categoriesSb.append("    </categories>");
      }

      Map values = new HashMap();
      values.put("doi", article.doi);
      values.put("title", article.title);
      values.put("description", article.description);
      values.put("date", article.date);
      values.put("authors", authorsSb.toString());
      values.put("categories", categoriesSb.toString());
      articlesXml += ItqlHelper.bindValues(AlertsImpl.XML_ARTICLE_TAG, values);
    }

    Map values = new HashMap();
    values.put("articles", articlesXml);
    return ItqlHelper.bindValues(AlertsImpl.XML_RESPONSE, values);
  }
}
