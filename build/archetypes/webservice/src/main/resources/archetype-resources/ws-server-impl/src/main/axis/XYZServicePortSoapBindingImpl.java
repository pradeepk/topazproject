#set( $service = $artifactId )
#set( $Svc = "${service.substring(0, 1).toUpperCase()}${service.substring(1)}" )

package ${package}.service;

import java.net.URI;
import java.rmi.RemoteException;
import javax.xml.rpc.ServiceException;
import javax.xml.rpc.server.ServiceLifecycle;
import javax.xml.rpc.server.ServletEndpointContext;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.configuration.ConfigurationStore;
import ${package}.${Svc}Impl;
import ${package}.${Svc}PEP;
import org.topazproject.xacml.Util;

/** 
 * Implements the server-side of the ${service} webservice. This is really just a wrapper around
 * the actual implementation in core.
 * 
 * @author foo
 */
public class ${Svc}ServicePortSoapBindingImpl implements ${Svc}, ServiceLifecycle {
  private static final Log log = LogFactory.getLog(${Svc}ServicePortSoapBindingImpl.class);

  private ${Svc}Impl impl;

  public void init(Object context) throws ServiceException {
    if (log.isTraceEnabled())
      log.trace("ServiceLifecycle#init");

    try {
      // get the pep
      ${Svc}PEP pep = new WS${Svc}PEP((ServletEndpointContext) context);

      // get other config
      Configuration conf = ConfigurationStore.getInstance().getConfiguration();
      conf = conf.subset("topaz");

      if (!conf.containsKey("services.fedora.uri"))
        throw new ConfigurationException("missing key 'topaz.services.fedora.uri'");
      if (!conf.containsKey("services.itql.uri"))
        throw new ConfigurationException("missing key 'topaz.services.itql.uri'");

      URI fedora = new URI(conf.getString("services.fedora.uri"));
      String username = conf.getString("services.fedora.userName", null);
      String password = conf.getString("services.fedora.password", null);
      URI mulgara = new URI(conf.getString("services.itql.uri"));

      // create the impl
      impl = new ${Svc}Impl(mulgara, fedora, username, password, pep);
    } catch (Exception e) {
      log.error("Failed to initialize ${Svc}Impl.", e);
      throw new ServiceException(e);
    }
  }

  public void destroy() {
    if (log.isTraceEnabled())
      log.trace("ServiceLifecycle#destroy");

    impl = null;
  }

  // The method delegations go here. You need to implement all methods from ${Svc} and
  // delegate them to the impl instance, converting parameters, return values, and exceptions
  // where necessary.

  private static class WS${Svc}PEP extends ${Svc}PEP {
    static {
      init(WS${Svc}PEP.class, SUPPORTED_ACTIONS, SUPPORTED_OBLIGATIONS);
    }

    public WS${Svc}PEP(ServletEndpointContext context) throws Exception {
      super(Util.lookupPDP(context, "topaz.${service}.pdpName"), Util.createSubjAttrs(context));
    }
  }
}
