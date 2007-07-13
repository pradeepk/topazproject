/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.web;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.plos.configuration.ConfigurationStore;

/**
 * A Filter that sets the virtual journal context as an attribute in the ServletRequest.
 *
 * Application usage:
 *
 * String virtualJournal = ServletRequest.getAttribute(PUB_VIRTUALJOURNAL_JOURNAL);
 * if (virtualJournal.equals(PUB_VIRTUALJOURNAL_DEFAULT_JOURNAL) {
 *   // in default context
 * }
 *
 * See WEB-INF/classes/org/plos/configuration/defaults.xml for configuration examples.
 */
public class VirtualJournalContextFilter implements Filter {

  /**
   * ServletRequest attribute for the virtual journal name.
   */
  public static final String PUB_VIRTUALJOURNAL_JOURNAL = "pub.virtualjournal.journal";

  /**
   * Default virtual journal name.
   */
  public static final String PUB_VIRTUALJOURNAL_DEFAULT_JOURNAL = "";

  public static final String CONF_VIRTUALJOURNALS          = "pub.virtualJournals";
  public static final String CONF_VIRTUALJOURNALS_DEFAULT  = CONF_VIRTUALJOURNALS + ".default";
  public static final String CONF_VIRTUALJOURNALS_JOURNALS = CONF_VIRTUALJOURNALS + ".journals";

  private static final Configuration configuration = ConfigurationStore.getInstance().getConfiguration();

  private static final Log log = LogFactory.getLog(VirtualJournalContextFilter.class);

  /*
   * @see javax.servlet.Filter#init
   */
  public void init(final FilterConfig filterConfig) throws ServletException {

    // settings & overrides are in the Configuration
    if (configuration == null) {
      // should never happen
      final String errorMessage = "No Configuration is available to set Virtual Journal context";
      log.error(errorMessage);
      throw new ServletException(errorMessage);
    }
  }

  /*
   * @see javax.servlet.Filter#destroy
   */
  public void destroy() {

    // nothing to do
  }

  /*
   * @see javax.servlet.Filter#doFilter
   */
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String virtualJournal = null;

    // was a simple config <default> specified?
    virtualJournal = configuration.getString(CONF_VIRTUALJOURNALS_DEFAULT + ".journal");

    if (log.isDebugEnabled()) {
      log.debug("virtual journal defaults: journal = \"" + virtualJournal + "\"");
    }

    // need to do <rule> based processing?
    if (virtualJournal == null) {
      virtualJournal = processVirtualJournalRules(configuration, (HttpServletRequest) request);
    }

    // use system default if not set
    if (virtualJournal == null) {
      virtualJournal = PUB_VIRTUALJOURNAL_DEFAULT_JOURNAL;

      if (log.isDebugEnabled()) {
        log.debug("setting virtual journal = \"" + virtualJournal + "\""
          + ", no <default> specified, no <rule>s match");
      }
    }

    if (log.isDebugEnabled()) {
      log.debug("setting virtual journal context to: journal = \"" + virtualJournal + "\"");
    }

    // put virtualJournal context in the ServletRequest for webapp to use
    request.setAttribute(PUB_VIRTUALJOURNAL_JOURNAL, virtualJournal);

    // continue the Filter chain ...
    filterChain.doFilter(request, response);
  }

  /**
   * Process all &lt;${journal-name}&gt;&lt;rules&gt;&lt;${http-header-name}&gt;s looking for a match.
   *
   * @param configuration <code>Configuration</code> that contains the rules.
   * @param request <code>HttpServletRequest</code> to apply the rules against.
   * @ return matching virtual journal.  May be <code>null</code>.
   */
  private String processVirtualJournalRules(Configuration configuration, HttpServletRequest request) {

    String virtualJournal = null;

    // process all <virtualjournal><journals> entries looking for a match
    final List<String> journals = configuration.getList(CONF_VIRTUALJOURNALS_JOURNALS);
    final Iterator onJournal = journals.iterator();
    while(onJournal.hasNext()
      && virtualJournal == null) {
      final String journal = (String) onJournal.next();

      if (log.isDebugEnabled()) {
        log.debug("processing virtual journal: " + journal);
      }

      // get the <rules> for this journal
      final String rulesPrefix = CONF_VIRTUALJOURNALS + "." + journal + ".rules";
      final Iterator rules = configuration.getKeys(rulesPrefix);
      while (rules.hasNext()
        && virtualJournal == null) {
        final String rule       = (String) rules.next();
        final String httpHeader = rule.substring(rulesPrefix.length() + 1);
        final String httpValue  = configuration.getString(rule);

        if (log.isDebugEnabled()) {
          log.debug("processing rule: " + httpHeader + " = " + httpValue);
        }

        // test Request HTTP header value against match
        final String reqHttpValue = request.getHeader(httpHeader);
        if (log.isDebugEnabled()) {
          log.debug("testing Request: " + httpHeader + "=" + reqHttpValue);
        }
        if (reqHttpValue == null) {
          if (httpValue == null) {
            virtualJournal = journal;
            break;
          }
          continue;
        }

        if (reqHttpValue.matches(httpValue)) {
          virtualJournal = journal;
          break;
        }
      }
    }

    // return match or null
    return virtualJournal;
  }
}
