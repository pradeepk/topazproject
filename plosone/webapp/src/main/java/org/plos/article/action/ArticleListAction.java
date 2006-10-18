/* $HeadURL::                                                                            $
 * $Id:ArticleListAction.java 722 2006-10-02 16:42:45Z viru $
 */
package org.plos.article.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.action.BaseActionSupport;

import java.util.ArrayList;

/**
 * Fetch as list of available articles.
 */
public class ArticleListAction extends BaseActionSupport {
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
    list.add("10.1371/journal.pone.0000008");
    return list;
  }

  public ArrayList<String> getMessages() {
    return messages;
  }
}
