#set( $service = $artifactId )
#set( $Svc = "${service.substring(0, 1).toUpperCase()}${service.substring(1)}" )
/*
 * $HeadURL::                                                                             $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package ${package};

import java.net.URI;
import java.rmi.RemoteException;
import javax.servlet.http.HttpSession;
import javax.xml.rpc.ServiceException;
import javax.xml.rpc.server.ServiceLifecycle;
import javax.xml.rpc.server.ServletEndpointContext;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.authentication.ProtectedService;
import org.topazproject.authentication.ProtectedServiceFactory;
import org.topazproject.configuration.ConfigurationStore;
import ${package}.impl.${Svc}Impl;
import ${package}.impl.${Svc}PEP;
import org.topazproject.xacml.ws.WSXacmlUtil;

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

      Configuration fedoraConf = conf.subset("services.fedora");
      Configuration itqlConf   = conf.subset("services.itql");

      HttpSession session = ((ServletEndpointContext) context).getHttpSession();

      ProtectedService fedoraSvc = ProtectedServiceFactory.createService(fedoraConf, session);
      ProtectedService itqlSvc   = ProtectedServiceFactory.createService(itqlConf, session);

      // create the impl
      impl = new ${Svc}Impl(itqlSvc, fedoraSvc, pep);
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
      super(WSXacmlUtil.lookupPDP(context, "topaz.${service}.pdpName"),
            WSXacmlUtil.createSubjAttrs(context));
    }
  }
}
