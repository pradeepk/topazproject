package org.plos.management;

import com.sun.jdmk.comm.HtmlAdaptorServer;
import org.apache.log4j.Logger;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;


/**
 * $HeadURL: $
 *
 * @version: $Id: $
 *
 *  This class starts up non Plosone MBeans.  It is declared in web.xml.
 *  Plosone MBeans use Spring injection and are NOT listed here.
 *  The class defaults to checking if there are currently any JMX servers
 *  currently running, if so then it uses the first one it finds, otherwise
 *  it creates a JMX server.
 */
public class JMXStartupListener implements ServletContextListener
{

    private static Logger logger = Logger.getLogger("org.plos");
    private MBeanServer server;

    private HtmlAdaptorServer htmlAdaptor;

    public void contextDestroyed(ServletContextEvent arg0)
    {
        logger.debug("STOPPING MBEAN Server");
        htmlAdaptor.stop();
    }


  public void contextInitialized(ServletContextEvent arg0) {
    logger.debug("STARTING MBEAN Server");
    server = MBeanServerFactory.createMBeanServer("RegistrationManager");

    ArrayList servers = MBeanServerFactory.findMBeanServer(null);
    if (servers == null) {
      System.out.println("NO MBean Server found, creating one");
      //TODO log warning    check this probably the same thing
      server = ManagementFactory.getPlatformMBeanServer();
    } else {
      server = (MBeanServer) servers.get(0);
    }
    System.out.println("MBEAN Server" + server.toString());


    try {

          //
          //   Register your non Plosone MBeans Here
          //
          startHtmlAdaptor();

        } catch (Exception e) {
          logger.error("Cannot start JMX Listener " + e.toString());
          System.out.println(("Cannot start JMX Listener " + e.toString()));
        }
    }

    protected void startHtmlAdaptor() throws Exception
    {
        htmlAdaptor= new HtmlAdaptorServer();
        htmlAdaptor.setPort(9092);
        server.registerMBean(htmlAdaptor, new ObjectName("Server:name=HtmlAdaptor"));
        htmlAdaptor.start();
    }

/*

    protected void startRMIConnector()
    {
        RmiConnectorServer connector = new RmiConnectorServer();
    }


    public void handleNotification(Notification notification, Object handback)
    {

    }
*/

}
