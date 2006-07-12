#set( $service = $artifactId )
#set( $Svc = "${service.substring(0, 1).toUpperCase()}${service.substring(1)}" )

package ${package};

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import javax.xml.rpc.ServiceException;

import junit.framework.TestCase;

/**
 *
 */
public class ${Svc}ServiceTest extends TestCase {
  private ${Svc} service;

  public ${Svc}ServiceTest(String testName) {
    super(testName);
  }

  protected void setUp() throws MalformedURLException, ServiceException, RemoteException {
    URL url =
        new URL("http://localhost:9997/ws-${service}-webapp-0.1/services/${Svc}ServicePort");
    ${Svc}ServiceLocator locator = new ${Svc}ServiceLocator();
    service = locator.get${Svc}ServicePort(url);
  }

  public void testAll() throws RemoteException, IOException {
    basic${Svc}Test();
  }

  private void basic${Svc}Test() throws RemoteException, IOException {
  }
}
