
package org.topazproject.ws.article.service;

import java.io.InputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
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
        new URL("http://localhost:9997/ws-articles-webapp-0.1/services/ArticleServicePort");
    ArticleServiceLocator locator = new ArticleServiceLocator();
    service = locator.getArticleServicePort(url);
  }

  public void testAll() throws RemoteException, IOException {
    //basicArticleTest();
  }

  private void basicArticleTest() throws RemoteException, IOException {
    byte[] zip = loadURL(getClass().getResource("/test_article.zip"));
    service.ingestNew(zip);
    service.delete("10.1371/journal.pbio.0020294", -1, true);
  }

  private static byte[] loadURL(URL url) throws IOException {
    URLConnection con = url.openConnection();
    con.connect();
    byte[] res = new byte[con.getContentLength()];

    InputStream is = con.getInputStream();
    is.read(res);
    is.close();

    return res;
  }
}
