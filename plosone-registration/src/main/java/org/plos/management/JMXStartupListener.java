package org.plos.management;

import com.sun.jdmk.comm.HtmlAdaptorServer;
import org.apache.log4j.Logger;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * $HeadURL: $
 *
 * @version: $Id: $
 */
public class JMXStartupListener implements ServletContextListener
{

    private static Logger logger = Logger.getLogger("org.plos");
    private MBeanServer server;
    private HtmlAdaptorServer htmlAdaptor = new HtmlAdaptorServer();

    public void contextDestroyed(ServletContextEvent arg0)
    {
        logger.debug("STOPPING MBEAN Server");
        htmlAdaptor.stop();
    }


    public void contextInitialized(ServletContextEvent arg0)
    {
        logger.debug("STARTING MBEAN Server");
        server = MBeanServerFactory.createMBeanServer("RegistrationManager");

        try {

          //
          //   Register your MBeans Here
          //


/*
            server.registerMBean(new RegistrationManager(),
                new ObjectName("RegistrationManagerAgent:name=RegistrationManager"));
            logger.debug("Registered RegistrationManager");
*/


            startHtmlAdaptor();
        } catch (Exception e) {
          logger.error("Cannot start JMX Listener " + e.toString());
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
