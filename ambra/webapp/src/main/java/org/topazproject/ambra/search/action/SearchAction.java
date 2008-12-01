/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2008 by Topaz, Inc.
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
package org.topazproject.ambra.search.action;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.transaction.annotation.Transactional;
import org.topazproject.ambra.action.BaseActionSupport;
import org.topazproject.ambra.article.service.BrowseService;
import org.topazproject.ambra.configuration.ConfigurationStore;
import org.topazproject.ambra.search.SearchResultPage;
import org.topazproject.ambra.search.service.Results;
import org.topazproject.ambra.search.service.SearchHit;
import org.topazproject.ambra.search.service.SearchService;


/**
 * Search Action class to search for simple or advanced search.
 *
 * @author Viru
 */
@SuppressWarnings("serial")
public class SearchAction extends BaseActionSupport {
  private static final Log           log  = LogFactory.getLog(SearchAction.class);
  private static final Configuration CONF = ConfigurationStore.getInstance().getConfiguration();
  private static final DateFormat    luceneDateFormat = new SimpleDateFormat("yyyy-MM-dd");
  private static final int           DEF_PAGE_SIZE =
                                                CONF.getInt("ambra.services.search.pageSize", 10);

  private SearchService searchService;
  private BrowseService browseService;

  private String query;
  private int    startPage = 0;
  private int    pageSize  = DEF_PAGE_SIZE;

  private Collection<SearchHit> searchResults;
  private int                   totalNoOfResults;
  private Map<String, Integer>  categoryInfos = new HashMap<String, Integer>(); // empty map for non-null safety

  /**
   * Flag telling this action whether or not the search should be executed.
   */
  private String   noSearchFlag;

  private String[] creator = null;
  private String   authorNameOp;
  private String   textSearchAll;
  private String   textSearchExactPhrase;
  private String   textSearchAtLeastOne;
  private String   textSearchWithout;
  private String   textSearchOption;
  private String   dateTypeSelect;
  private String   startYear;
  private String   startMonth;
  private String   startDay;
  private String   endYear;
  private String   endMonth;
  private String   endDay;
  private String   subjectCatOpt;
  private String[] limitToCategory = null;

  /**
   * @return return simple search result
   */
  @Transactional(readOnly = true)
  public String executeSimpleSearch() {
    // the simple search text field correlates to advanced search's "for all the words" field
    this.textSearchAll = query;
    return executeSearch(query);
  }

  /**
   * @return return simple search result
   */
  @Transactional(readOnly = true)
  public String executeAdvancedSearch() {
    if(doSearch()) {
      query = buildAdvancedQuery();
      return executeSearch(query);
    }

    categoryInfos = browseService.getCategoryInfos();
    return INPUT;
  }

  private String executeSearch(final String queryString) {
    if (StringUtils.isBlank(queryString)) {
      addActionError("Please enter a query string.");
      categoryInfos = browseService.getCategoryInfos();
      return INPUT;
    }

    try {
      SearchResultPage results = searchService.find(queryString, startPage, pageSize);
      totalNoOfResults = results.getTotalNoOfResults();
      searchResults    = results.getHits();
    } catch (Exception e) {
      addActionError("Search failed ");
      log.error("Search failed with error with query string: " + queryString, e);
      return ERROR;
    }

    return SUCCESS;
  }

  private String buildAdvancedQuery() {
    final Collection<String> fields = new ArrayList<String>();

    // Build Search terms for Authors if specified
    if ((creator != null) && (creator.length > 0) && (StringUtils.isNotBlank(creator[0]))) {
      StringBuilder buf = new StringBuilder("(creator:(");
      boolean allAuthors = false;
      if ("all".equals(authorNameOp)) {
        allAuthors = true;
      }
      for (int i=0; i<creator.length; i++) {
        String creatorName = creator[i];
        if (StringUtils.isNotBlank(creatorName)) {
          buf.append("\"");
          buf.append(escape(creatorName));
          buf.append("\"");
        }
        if ((i < creator.length-1) && (StringUtils.isNotBlank(creator[i+1]))) {
          if (allAuthors) {
            buf.append(" AND ");
          } else {
            buf.append(" OR ");
          }
        }
      }
      buf.append(" ))");
      fields.add(buf.toString());
    }

    // Build Search Article Text section of advanced search.

    String textSearchField = null;
    if (StringUtils.isNotBlank(textSearchOption)) {
      if ("abstract".equals(textSearchOption)) {
        textSearchField = "description:";
      }
      else if ("refs".equals(textSearchOption)) {
        textSearchField = "citation:";
      }
      else if ("title".equals(textSearchOption)) {
        textSearchField = "title:";
      }
    }
    // All words field should be in parenthesis with AND between each word.
    if (StringUtils.isNotBlank(textSearchAll)) {
      String[] words = StringUtils.split(textSearchAll);
      StringBuilder buf = new StringBuilder();
      if (textSearchField != null) {
        buf.append(textSearchField);
      }
      buf.append("( ");
      for (int i=0; i<words.length; i++) {
        buf.append(escape(words[i]));
        if (i < words.length-1) {
          buf.append(" AND ");
        }
      }
      buf.append(" )");
      fields.add(buf.toString());
    }

    // Exact phrase should be placed in quotes
    if (StringUtils.isNotBlank(textSearchExactPhrase)) {
      StringBuilder buf = new StringBuilder();
      if (textSearchField != null) {
        buf.append(textSearchField);
      }
      buf.append("(\"");
      buf.append(escape(textSearchExactPhrase));
      buf.append("\")");
      fields.add(buf.toString());
    }

    // At least one of should be placed in parenthesis separated by spaced (OR'ed)
    if (StringUtils.isNotBlank(textSearchAtLeastOne)) {
      StringBuilder buf = new StringBuilder();
      if (textSearchField != null) {
        buf.append(textSearchField);
      }
      buf.append("( ");
      buf.append(escape(textSearchAtLeastOne));
      buf.append(" )");
      fields.add(buf.toString());
    }

    // Without the words - in parenthesis with NOT operator and AND'ed together
    // E.g. (-"the" AND -"fat" AND -"cat")
    if (StringUtils.isNotBlank(textSearchWithout)) {
      // TODO - we might want to allow the entry of phrases to omit (entered using quotes?)
      // - in which case we have to be smarter about how we split...
      String[] words = StringUtils.split(textSearchWithout);
      StringBuilder buf = new StringBuilder();
      if (textSearchField != null) {
        buf.append(textSearchField);
      }
      buf.append("( ");
      for (int i=0; i<words.length; i++) {
        buf.append("-\"");
        buf.append(escape(words[i]));
        buf.append("\"");
        if (i < words.length-1) {
          buf.append(" AND ");
        }
      }
      buf.append(" )");
      fields.add(buf.toString());
    }

    if (StringUtils.isNotBlank(dateTypeSelect)) {
      if ("range".equals(dateTypeSelect)) {
        if (isDigit(startYear)&& isDigit(startMonth) && isDigit(startDay) &&
            isDigit(endYear) && isDigit(endMonth) && isDigit(endDay)) {
          StringBuilder buf = new StringBuilder("date:[");
          buf.append(padDatePart(startYear, true)).append('-').append(padDatePart(startMonth, false)).append('-').append(padDatePart(startDay, false));
          buf.append(" TO ");
          buf.append(padDatePart(endYear, true)).append('-').append(padDatePart(endMonth, false)).append('-').append(padDatePart(endDay, false));
          buf.append("]");
          fields.add(buf.toString());
        }
      } else {
        Calendar cal = new GregorianCalendar();
        if ("week".equals(dateTypeSelect)) {
          cal.add(Calendar.DATE, -7);
        }
        if ("month".equals(dateTypeSelect)) {
          cal.add(Calendar.MONTH, -1);
        }
        if ("3months".equals(dateTypeSelect)) {
          cal.add(Calendar.MONTH, -3);
        }

        String startDateStr, endDateStr;
        synchronized(luceneDateFormat) {
          endDateStr = luceneDateFormat.format(new Date());
          startDateStr = luceneDateFormat.format(cal.getTime());
        }

        StringBuilder buf = new StringBuilder("date:[");
        buf.append(startDateStr).append(" TO ").append(endDateStr).append("]");
        fields.add(buf.toString());
      }
    }

    if ("some".equals(subjectCatOpt)) {
      if ((limitToCategory != null) && (limitToCategory.length > 0)) {
        StringBuilder buf = new StringBuilder("subject:( " );
        for (int i=0; i < limitToCategory.length; i++) {
          buf.append("\"").append(escape(limitToCategory[i])).append("\" ");
        }
        buf.append(")");
        fields.add(buf.toString());
      }
    }

    String advSearchQueryStr = StringUtils.join(fields.iterator(), " AND ");
    if (log.isDebugEnabled()) {
      log.debug("Generated advanced search query: "+advSearchQueryStr);
    }
    return StringUtils.join(fields.iterator(), " AND ");
  }

  private static String padDatePart(String datePart, boolean isYear) {
    if(isYear) {
      // presume we have a valid 4-digit year
      return datePart;
    }
    // month or day date part: ensure left padding w/ 0 digit for lucene date format compliance
    return StringUtils.leftPad(datePart, 2, '0');
  }

  /**
   * Static helper method to escape special characters in a Lucene search string with a \
   */
  protected static final String escapeChars = "+-&|!(){}[]^\"~*?:\\";
  protected static String escape(String in) {
    StringBuilder buf = new StringBuilder();
    for (int i=0; i < in.length(); i++) {
      if (escapeChars.indexOf(in.charAt(i)) != -1) {
        buf.append("\\");
      }
      buf.append(in.charAt(i));
    }
    return buf.toString();
  }

  protected static boolean isDigit(String str) {
    if (str == null) return false;
    for (int i=0; i < str.length(); i++) {
      if (!(Character.isDigit(str.charAt(i)))) {
        return false;
      }
    }
    return true;
  }

  /**
   * Set the simple query
   * @param query query
   */
  public void setQuery(final String query) {
    this.query = query;
  }

  /**
   * Set the startPage
   * @param startPage startPage
   */
  public void setStartPage(final int startPage) {
    this.startPage = startPage;
  }

  /**
   * Set the pageSize
   * @param pageSize pageSize
   */
  public void setPageSize(final int pageSize) {
    this.pageSize = pageSize;
  }

  /**
   * Set the searchService
   * @param searchService searchService
   */
  public void setSearchService(final SearchService searchService) {
    this.searchService = searchService;
  }

  /**
   * Spring injected BrowseService
   * @param browseService searchService
   */
  public void setBrowseService(final BrowseService browseService) {
    this.browseService = browseService;
  }

  /**
   * @return the search results.
   */
  public Collection<SearchHit> getSearchResults() {
    return searchResults;
  }


  /**
   * Getter for property 'pageSize'.
   * @return Value for property 'pageSize'.
   */
  public int getPageSize() {
    return pageSize;
  }

  /**
   * Getter for property 'query'.
   * @return Value for property 'query'.
   */
  public String getQuery() {
    return query;
  }

  /**
   * Getter for property 'startPage'.
   * @return Value for property 'startPage'.
   */
  public int getStartPage() {
    return startPage;
  }

  /**
   * Getter for property 'totalNoOfResults'.
   * @return Value for property 'totalNoOfResults'.
   */
  public int getTotalNoOfResults() {
    return totalNoOfResults;
  }

  public String getTextSearchAll() {
    return textSearchAll;
  }

  public void setTextSearchAll(String textSearchAll) {
    this.textSearchAll = textSearchAll;
  }

  public String getTextSearchExactPhrase() {
    return textSearchExactPhrase;
  }

  public void setTextSearchExactPhrase(String textSearchExactPhrase) {
    this.textSearchExactPhrase = textSearchExactPhrase;
  }

  public String getTextSearchAtLeastOne() {
    return textSearchAtLeastOne;
  }

  public void setTextSearchAtLeastOne(String textSearchAtLeastOne) {
    this.textSearchAtLeastOne = textSearchAtLeastOne;
  }

  public String getTextSearchWithout() {
    return textSearchWithout;
  }

  public void setTextSearchWithout(String textSearchWithout) {
    this.textSearchWithout = textSearchWithout;
  }

  public String getTextSearchOption() {
    return textSearchOption;
  }

  public void setTextSearchOption(String textSearchOption) {
    this.textSearchOption = textSearchOption;
  }

  public String getDateTypeSelect() {
    return dateTypeSelect;
  }

  public void setDateTypeSelect(String dateTypeSelect) {
    this.dateTypeSelect = dateTypeSelect;
  }

  public String getStartYear() {
    return startYear;
  }

  public void setStartYear(String startYear) {
    this.startYear = startYear;
  }

  public String getStartMonth() {
    return startMonth;
  }

  public void setStartMonth(String startMonth) {
    this.startMonth = startMonth;
  }

  public String getStartDay() {
    return startDay;
  }

  public void setStartDay(String startDay) {
    this.startDay = startDay;
  }

  public String getEndYear() {
    return endYear;
  }

  public void setEndYear(String endYear) {
    this.endYear = endYear;
  }

  public String getEndMonth() {
    return endMonth;
  }

  public void setEndMonth(String endMonth) {
    this.endMonth = endMonth;
  }

  public String getEndDay() {
    return endDay;
  }

  public void setEndDay(String endDay) {
    this.endDay = endDay;
  }

  public String getSubjectCatOpt() {
    return subjectCatOpt;
  }

  public void setSubjectCatOpt(String subjectCatOpt) {
    this.subjectCatOpt = subjectCatOpt;
  }

  public Map<String, Integer> getCategoryInfos() {
    return categoryInfos;
  }

  public void setCategoryInfos(Map<String, Integer> categoryInfos) {
    this.categoryInfos = categoryInfos;
  }

  public String[] getCreator() {
    return creator;
  }

  /**
   * @return The {@link #creator} array as a single comma delimited String.
   */
  public String getCreatorStr() {
    if(creator == null) return "";
    StringBuilder sb = new StringBuilder();
    for(String auth : creator) {
      sb.append(',');
      sb.append(auth.trim());
    }
    return sb.toString().substring(1);
  }

  /**
   * Converts a String array whose first element may be a comma delimited String
   * into a new String array whose elements are the split comma delimited elements.
   * @param arr String array that may contain one or more elements having a comma delimited String.
   * @return Rectified String[] array or
   *         <code>null</code> when the given String array is <code>null</code>.
   */
  private String[] rectify(String[] arr) {
    if(arr != null && arr.length == 1 && arr[0].length() > 0) {
      arr = arr[0].split(",");
      for(int i = 0; i < arr.length; i++) {
        arr[i] = arr[i] == null ? null : arr[i].trim();
      }
    }
    return arr;
  }

  public void setCreator(String[] creator) {
    this.creator = rectify(creator);
  }

  public String[] getLimitToCategory() {
    return limitToCategory;
  }

  public void setLimitToCategory(String[] limitToCategory) {
    this.limitToCategory = rectify(limitToCategory);
  }

  /**
   * @return the authorNameOp
   */
  public String getAuthorNameOp() {
    return authorNameOp;
  }

  /**
   * @param authorNameOp the authorNameOp to set
   */
  public void setAuthorNameOp(String authorNameOp) {
    this.authorNameOp = authorNameOp;
  }

  /**
   * @return the noSearchFlag
   */
  public String getNoSearchFlag() {
    return noSearchFlag;
  }

  /**
   * @param noSearchFlag the noSearchFlag to set
   */
  public void setNoSearchFlag(String noSearchFlag) {
    this.noSearchFlag = noSearchFlag;
  }

  private boolean doSearch() {
    return noSearchFlag == null;
  }
}
