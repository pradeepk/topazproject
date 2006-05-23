
package org.topazproject.ws.article.service;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import javax.xml.rpc.ServiceException;

import junit.framework.TestCase;

/**
 *
 */
public class ArticleServiceTest extends TestCase {
  private Article service;

  public ArticleServiceTest(String testName) {
    super(testName);
  }

  protected void setUp() throws MalformedURLException, ServiceException, RemoteException {
    URL url =
        new URL("http://localhost:9999/ws-articles-webapp-0.1/services/ArticleServicePort");
    ArticleServiceLocator locator = new ArticleServiceLocator();
    service = locator.getArticleServicePort(url);
  }

  public void testAll() throws RemoteException {
    basicArticleTest();
  }

  private void basicArticleTest() throws RemoteException {
  }
}
