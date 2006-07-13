package org.plos.web;

import com.opensymphony.xwork.ActionSupport;

/**
 * $HeadURL$
 * @version: $Id$
 */
public class ForwardAction extends ActionSupport {

  /**
   * A default implementation that does nothing an returns "success".
   *
   * @return {@link #SUCCESS}
   */
  public String execute() throws Exception {
    return SUCCESS;
  }
}
