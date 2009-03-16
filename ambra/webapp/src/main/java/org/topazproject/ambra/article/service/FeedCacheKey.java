/* $HeadURL$
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

package org.topazproject.ambra.article.service;

import org.topazproject.ambra.action.BaseActionSupport;

import java.io.Serializable;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.net.URI;
import org.topazproject.ambra.article.service.ArticleFeedService.FEED_TYPES;

/**
 * The <code>class FeedCacheKey</code> serves three function:
 * <ul>
 * <li> It provides the data model used by the action.
 * <li> It is the cache key to the article ID's that reside in the feed cache
 * <li> It relays these input parameters to AmbraFeedResult.
 * </ul>
 *
 * Since the parameters uniquely identify the query they are used to generate the hash code for
 * the key. Only the parameters that can affect query results are used for this purpose. The cache
 * key is also made available to the AmbraFeedResult because it also contains parameters that
 * affect the output.
 *
 * @see       org.topazproject.ambra.article.service.ArticleFeedService
 * @see       org.topazproject.ambra.article.service.ArticleFeedService.Invalidator
 * @see       org.topazproject.ambra.article.service.ArticleFeedService.FEED_TYPES
 * @see       org.topazproject.ambra.struts2.AmbraFeedResult
 */
public class FeedCacheKey implements Serializable, Comparable {
  private static final long serialVersionUID = 1L;
  private String journal;
  private Date sDate;
  private Date eDate;

  // Fields set by Struts
  private String startDate;
  private String endDate;
  private String category;
  private String author;
  private boolean relLinks = false;
  private boolean extended = false;
  private String title;
  private String selfLink;
  private int maxResults;
  private URI issueURI;
  private String type = FEED_TYPES.Article.toString();

  final SimpleDateFormat dateFrmt = new SimpleDateFormat("yyyy-MM-dd");
  private int hashCode;

  /**
   * Key Constructor - currently does nothing.
   */
  public FeedCacheKey() {
  }

  /**
   * Calculates a hash code based on the query parameters. Parameters that do not affect the
   * results of the query (selfLink, relLinks, title etc) should not be included in the hash
   * calculation because this will improve the probability of a cache hit.
   *
   * @return <code>int hash code</code>
   */
  private int calculateHashKey() {
    final int ODD_PRIME_NUMBER = 37;  // Make values relatively prime
    int hash = 23;                    // Seed value

    if (this.journal != null)
      hash += ODD_PRIME_NUMBER * hash + this.journal.hashCode();
    if (this.type != null)
      hash += ODD_PRIME_NUMBER * hash + this.type.hashCode();
    if (this.sDate != null)
      hash += ODD_PRIME_NUMBER * hash + this.sDate.hashCode();
    if (this.eDate != null)
      hash += ODD_PRIME_NUMBER * hash + this.eDate.hashCode();
    if (this.category != null)
      hash += ODD_PRIME_NUMBER * hash + this.category.hashCode();
    if (this.author != null)
      hash += ODD_PRIME_NUMBER * hash + this.author.hashCode();

    hash += ODD_PRIME_NUMBER * hash + this.maxResults;

    return hash;
  }

  /**
   * The hash code is calculated after the validation is complete. The results are stored here.
   *
   * @return integer hash code
   */
  @Override
  public int hashCode() {
    return this.hashCode;
  }

  /**
   * Does a complete equality comparison of fields in the Key.  Only fields that will affect the
   * results are used.
   */
  @Override
  public boolean equals(Object o) {
    if (o == null || !(o instanceof FeedCacheKey))
      return false;
    FeedCacheKey key = (FeedCacheKey) o;
    return (
        key.hashCode == this.hashCode
            &&
            (key.getJournal() == null && this.journal == null
                || key.getJournal() != null && key.getJournal().equals(this.journal))
            &&
            (key.getType() == null && this.type == null
                || key.getType() != null && key.getType().equals(this.type))
            &&
            (key.getSDate() == null && this.sDate == null
                || key.getSDate() != null && key.getSDate().equals(this.sDate))
            &&
            (key.getEDate() == null && this.eDate == null
                || key.getEDate() != null && key.getEDate().equals(this.eDate))
            &&
            (key.getCategory() == null && this.category == null
                || key.getCategory() != null && key.getCategory().equals(this.category))
            &&
            (key.getAuthor() == null && this.author == null
                || key.getAuthor() != null && key.getAuthor().equals(this.author))
            &&
            (key.getMaxResults() == this.maxResults)
    );
  }

  /**
   * Builds a string using the data model prarmeters.  Only parameters that affect the search
   * results are used.
   *
   * @return a string representation of all the query parameters.
   */
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();

    builder.append("journal=").append(journal);
    builder.append("; type=").append(type);

    if (sDate != null)
      builder.append("; startDate=").append(sDate);

    if (eDate != null)
      builder.append("; endDate=").append(eDate);

    if (category != null)
      builder.append("; category=").append(category);

    if (author != null)
      builder.append("; author=").append(author);

    builder.append("; maxResults=").append(maxResults);

    return builder.toString();
  }

  /**
   * Implementation of the comparable interface. TODO: doesn't conform to compare interface
   * standard.
   *
   * @param o the object to compare to.
   * @return the value 0 if the argument is a string lexicographically equal to this string;
   *         a value less than 0 if the argument is a string lexicographically greater than
   *         this string; and a value greater than 0 if the argument is a string
   *         lexicographically less than this string.
   */
  public int compareTo(Object o) {
    if (o == null)
      return 1;
    return toString().compareTo(o.toString());
  }

  /**
   * The ArticleFeed supports the ModelDriven interface.  The Key class is the data model used by
   * ArticleFeed and validates user input parameters. By the time ArticleFeed.execute is invoked
   * the parameters should be a usable state.
   * <p/>
   * Defined a Maximum number of result = 200 articles.  Both sDate and eDate will not be null by
   * the end of validate.  If sDate &gt; eDate then set sDate = eDate.
   *
   * @param action - the BaseSupportAction allows reporting of field errors. Pass in a reference
   *               incase we want to report them.
   * @see ArticleFeedService
   */
  @SuppressWarnings("UnusedDeclaration")
  public void validate(BaseActionSupport action) {
    final int defaultMaxResult = 30;
    final int MAXIMUM_RESULTS = 200;

    try {
      if (startDate != null)
        setSDate(startDate);

      if (endDate != null)
        setEDate(endDate);
    } catch (ParseException e) {
      action.addFieldError("Feed date parsing error.", "endDate or startDate");
    }

    // If start > end then just use end.
    if ((sDate != null) && (eDate != null) && (sDate.after(eDate)))
      sDate = eDate;

    // If there is garbage in the type default to Article
    if (feedType() == FEED_TYPES.Invalid)
      type = FEED_TYPES.Article.toString();

    // Need a positive non-zero number of results
    if (maxResults <= 0)
      maxResults = defaultMaxResult;
    else if (maxResults > MAXIMUM_RESULTS)   // Don't let them crash our servers.
      maxResults = MAXIMUM_RESULTS;

    hashCode = calculateHashKey();
  }

  /**
   * Determine the feed type by comparing it to each of the
   * enumerated feed types until there is a match or return
   * Invalid.
   *
   * @return FEED_TYPES (the Invalid type signifies that the
   *         type field contains a string that does not match
   *         any of the types)
   */
  public FEED_TYPES feedType() {
    FEED_TYPES t;
    try {
      t = FEED_TYPES.valueOf(type);
    } catch (Exception e) {
      // It's ok just return invalid.
      t = FEED_TYPES.Invalid;
    }
    return t;
  }

  public String getJournal() {
    return journal;
  }

  public void setJournal(String journal) {
    this.journal = journal;
  }

  public Date getSDate() {
    return sDate;
  }

  /**
   * Convert the string to a date if possible else leave the startDate null.
   *
   * @param date string date to be converted to Date
   * @throws ParseException date failed to parse
   */
  public void setSDate(String date) throws ParseException {
    this.sDate = dateFrmt.parse(date);
  }

  public void setSDate(Date date) {
    this.sDate = date;
  }

  public Date getEDate() {
    return eDate;
  }

  /**
   * Convert the string to a date if possible else leave the endDate null.
   *
   * @param date string date to be converted to Date
   * @throws ParseException date failed to parse
   */
  public void setEDate(String date) throws ParseException {
    this.eDate = dateFrmt.parse(date);
  }

  public void setEDate(Date date) {
    this.eDate = date;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public boolean isRelLinks() {
    return relLinks;
  }

  public boolean getRelativeLinks() {
    return relLinks;
  }

  public void setRelativeLinks(boolean relative) {
    this.relLinks = relative;
  }

  public boolean isExtended() {
    return extended;
  }

  public boolean getExtended() {
    return extended;
  }

  public void setExtended(boolean extended) {
    this.extended = extended;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getSelfLink() {
    return selfLink;
  }

  public void setSelfLink(String link) {
    this.selfLink = link;
  }

  public String getStartDate() {
    return this.startDate;
  }

  public void setStartDate(String date) {
    this.startDate = date;
  }

  public String getEndDate() {
    return endDate;
  }

  public void setEndDate(String date) {
    this.endDate = date;
  }

  public int getMaxResults() {
    return maxResults;
  }

  public void setMaxResults(int max) {
    this.maxResults = max;
  }

  public void setIssueURI(String issueURI) {
    try {
      this.issueURI = URI.create(issueURI.trim());
    } catch (Exception e) {
      this.issueURI = null;
    }
  }

  public URI getIssueURI() {
    return issueURI;
  }
}

