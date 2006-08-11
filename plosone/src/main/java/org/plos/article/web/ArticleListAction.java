/* $HeadURL::                                                                            $
 * $Id$
 */
package org.plos.article.web;

import com.opensymphony.xwork.ActionSupport;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Fetch as list of available articles.
 */
public class ArticleListAction extends ActionSupport {
  private ArrayList<String> messages = new ArrayList<String>();
  private static final Log log = LogFactory.getLog(ArticleListAction.class);

  public String execute() throws Exception {
    return SUCCESS;
  }

  /**
   * @return the list of available articles
   */
  public ArrayList<String> getArticles() {
    final ArrayList<String> list = new ArrayList<String>();
    list.add("10.1371/journal.pbio.0020294");
    list.add("10.1371/journal.pbio.0020042");
    list.add("10.1371/journal.pbio.0020382");
    list.add("10.1371/journal.pbio.0020317");
    return list;
  }

  public ArrayList<String> getMessages() {
    return messages;
  }
}
