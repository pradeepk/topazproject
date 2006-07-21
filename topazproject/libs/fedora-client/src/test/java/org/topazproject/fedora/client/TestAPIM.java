package org.topazproject.fedora.client;

import org.topazproject.authentication.ProtectedService;
import org.topazproject.authentication.ProtectedServiceFactory;

import junit.framework.TestCase;

/**
 * Tests for APIM client stub.
 *
 * @author Pradeep Krishnan
 */
public class TestAPIM extends TestCase {
  private static String uri    = "http://localhost:9090/fedora/services/management";
  private static String uname  = "fedoraAdmin";
  private static String passwd = "fedoraAdmin";

  //
  private static ProtectedService svc =
    ProtectedServiceFactory.createService(uri, uname, passwd, true);

  //
  private static final String FOXML =
    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
    + "<foxml:digitalObject xmlns:foxml=\"info:fedora/fedora-system:def/foxml#\">"
    + "<foxml:objectProperties>"
    + "<foxml:property NAME=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#type\" VALUE=\"FedoraObject\"/>"
    + "<foxml:property NAME=\"info:fedora/fedora-system:def/model#state\" VALUE=\"A\"/>"
    + "<foxml:property NAME=\"info:fedora/fedora-system:def/model#label\" VALUE=\"Test Object\"/>"
    + "<foxml:property NAME=\"info:fedora/fedora-system:def/model#contentModel\" VALUE=\"TEST\"/>"
    + "</foxml:objectProperties>" + "</foxml:digitalObject>";

  //
  private FedoraAPIM apim;
  private boolean    skip = true;

  /**
   * Creates a new TestAPIM object.
   */
  public TestAPIM() {
    //skip = false;
  }

  /**
   * Sets up the tests. Creats the stub.
   *
   * @throws Exception on failure
   */
  public void setUp() throws Exception {
    if (skip)
      return;

    apim = APIMStubFactory.create(svc);
  }

  /**
   * Tests ingest/purge
   *
   * @throws Exception on failure
   */
  public void testIngest() throws Exception {
    if (skip)
      return;

    String pid = apim.ingest(FOXML.getBytes("UTF-8"), "foxml1.0", "created");
    apim.purgeObject(pid, "deleted", false);
  }

  /**
   * Tests Id generation
   *
   * @throws Exception on failure
   */
  public void testGetNextPID() throws Exception {
    if (skip)
      return;

    String pid = apim.getNextPID(new org.apache.axis.types.NonNegativeInteger("1"), "test")[0];
    assertTrue(pid.startsWith("test:"));
  }
}
