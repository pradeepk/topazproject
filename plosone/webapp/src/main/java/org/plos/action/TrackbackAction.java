/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.action;

import java.io.UnsupportedEncodingException;

import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import org.apache.struts2.ServletActionContext;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.plos.configuration.ConfigurationStore;
import org.plos.models.Trackback;
import org.plos.models.TrackbackContent;
import org.plos.web.VirtualJournalContext;

import org.springframework.beans.factory.annotation.Required;

import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.Transaction;
import org.topazproject.otm.criterion.Restrictions;

import org.apache.roller.util.LinkbackExtractor;

/**
 * Class to process trackback requests from external sites.  Writes information to
 * store if previous one does not exist and spam checking is passed.
 *
 * @author Stephen Cheng
 *
 */
public class TrackbackAction extends BaseActionSupport {

  private static final Log log = LogFactory.getLog(TrackbackAction.class);
  private int error = 0;
  private String errorMessage = "";
  private String title;
  private String url;
  private String excerpt;
  private String blog_name;
  private String trackbackId;
  private List<TrackbackContent> trackbackList;
  private VirtualJournalContext journalContext;

  private static final Configuration myConfig = ConfigurationStore.getInstance().getConfiguration();

  private Session          session;

  /**
   * Main action execution.
   *
   */
  public String execute() throws Exception {
    Transaction   tx = null;
    boolean inserted = false;

    if (!ServletActionContext.getRequest().getMethod().equals("POST")) {
      if (log.isDebugEnabled()) {
        log.debug("Returning error because HTTP POST was not used.");
      }
      return returnError ("HTTP method must be POST");
    }

    journalContext = (VirtualJournalContext)ServletActionContext.getRequest()
                      .getAttribute(VirtualJournalContext.PUB_VIRTUALJOURNAL_CONTEXT);

    URL permalink = null;
    URI trackback = null;
    try {
      permalink = new URL (url);
    } catch (Exception e) {
      if (log.isInfoEnabled()) {
        log.info ("Could not construct URL with parameter: " + url);
      }
      return returnError("URL invalid");
    }

    try {
      trackback = new URI (trackbackId);
    } catch (Exception e) {
      if (log.isInfoEnabled()) {
        log.info ("Could not construct URI with parameter: " + trackbackId);
      }
      return returnError("Object URI invalid");
    }

    try {
      tx = session.beginTransaction();

      List<Trackback> trackbackList = session
      .createCriteria(Trackback.class)
      .add(Restrictions.eq("annotates", trackback))
      .createCriteria("body")
      .add(Restrictions.eq("url", url))
      .list();

      if (trackbackList.size() == 0) {
        if (log.isDebugEnabled()) {
          log.debug("No previous trackback found for: " + permalink);
        }

        LinkbackExtractor linkback = new LinkbackExtractor(url,
                                     getArticleUrl(journalContext.getBaseUrl(), trackbackId));
        if (linkback.getExcerpt() == null) {
          if (log.isErrorEnabled()) {
            log.debug("Trackback failed verification: " + permalink);
          }
          tx.commit(); // Flush happens automatically
          return returnError("No trackback present");
        }

        TrackbackContent tc = new TrackbackContent (title, excerpt, blog_name, permalink);
        Trackback tb = new Trackback();
        tb.setBody(tc);
        tb.setAnnotates(trackback);
        tb.setCreated(new Date());

        session.saveOrUpdate(tb);
        inserted = true;
      }
      tx.commit(); // Flush happens automatically
    } catch (OtmException e) {
      try {
        if (tx != null)
          tx.rollback();
      } catch (OtmException re) {
        log.warn("rollback failed", re);
      }
      return returnError("Error inserting trackback");
    }

    if (log.isDebugEnabled() && inserted){
      StringBuilder msg = new StringBuilder ("Successfully inserted trackback with title: ")
                                             .append (title)
                                             .append ("; url: ")
                                             .append (url)
                                             .append ("; excerpt: ")
                                             .append (excerpt)
                                             .append ("; blog_name: ")
                                             .append (blog_name);
      log.debug(msg);
    }
    return SUCCESS;
  }

  public String getTrackbacks () {
    Transaction tx = null;
    try {
      tx = session.beginTransaction();

      if (log.isDebugEnabled()) {
        log.debug("retrieving rating summaries for: " + trackbackId);
      }

      List<Trackback> trackbacks = session
        .createCriteria(Trackback.class)
        .add(Restrictions.eq("annotates", trackbackId))
        .list();

      trackbackList = new ArrayList<TrackbackContent>(trackbacks.size());
      Iterator<Trackback> iter = trackbacks.iterator();
      while (iter.hasNext()) {
        Trackback t = iter.next();
        t.getBody().getBlog_name();  //for lazy load
        trackbackList.add(t.getBody());
      }
    } catch (OtmException e) {
      try {
        if (tx != null)
          tx.rollback();
      } catch (OtmException re) {
        log.warn("rollback failed", re);
      }
      throw e; // or display error message
    }
    return SUCCESS;
  }

  private String returnError (String errMsg) {
    error = 1;
    errorMessage = errMsg;
    return ERROR;
  }

  /**
   * @return Returns the blog_name.
   */
  public String getBlog_name() {
    return blog_name;
  }

  /**
   * @param blog_name The blog_name to set.
   */
  public void setBlog_name(String blog_name) {
    this.blog_name = blog_name;
  }

  /**
   * @return Returns the error.
   */
  public int getError() {
    return error;
  }

  /**
   * @param error The error to set.
   */
  public void setError(int error) {
    this.error = error;
  }

  /**
   * @return Returns the errorMessage.
   */
  public String getErrorMessage() {
    return errorMessage;
  }

  /**
   * @param errorMessage The errorMessage to set.
   */
  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  /**
   * @return Returns the excerpt.
   */
  public String getExcerpt() {
    return excerpt;
  }

  /**
   * @param excerpt The excerpt to set.
   */
  public void setExcerpt(String excerpt) {
    this.excerpt = excerpt;
  }

  /**
   * @return Returns the title.
   */
  public String getTitle() {
    return title;
  }

  /**
   * @param title The title to set.
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * @return Returns the url.
   */
  public String getUrl() {
    return url;
  }

  /**
   * @param url The url to set.
   */
  public void setUrl(String url) {
    this.url = url;
  }

  /**
   * @return Returns the trackbackId.
   */
  public String getTrackbackId() {
    return trackbackId;
  }

  /**
   * @param trackbackId The trackbackId to set.
   */
  public void setTrackbackId(String trackbackId) {
    this.trackbackId = trackbackId;
  }

  /**
   * Sets the otm util.
   *
   * @param session The otm session to set.
   */
  @Required
  public void setOtmSession(Session session) {
    this.session = session;
  }

  /**
   * @return Returns the trackbackList.
   */
  public List<TrackbackContent> getTrackbackList() {
    return trackbackList;
  }

  /**
   * @param trackbackList The trackbackList to set.
   */
  public void setTrackbackList(List<TrackbackContent> trackbackList) {
    this.trackbackList = trackbackList;
  }

  private String getArticleUrl (String baseURL, String articleURI) {
    String escapedURI = null;
    try {
      escapedURI = URLEncoder.encode(articleURI, "UTF-8");
    } catch (UnsupportedEncodingException ue) {
      escapedURI = articleURI;
    }

    return new StringBuilder(baseURL).append (myConfig.getString("pub.article-action"))
                                     .append(escapedURI).toString();
  }
}
