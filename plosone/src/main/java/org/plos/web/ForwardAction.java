/* $HeadURL::                                                                            $
 * $Id$
 *
 */
package org.plos.web;

import com.opensymphony.xwork.ActionSupport;

/**
 * Simply returns a success so as to forward to the the next flow.
 */
public class ForwardAction extends ActionSupport {

  /**
   * A default implementation that does nothing and returns "success".
   * @return {@link #SUCCESS}
   */
  public String execute() throws Exception {
    return SUCCESS;
  }
}
