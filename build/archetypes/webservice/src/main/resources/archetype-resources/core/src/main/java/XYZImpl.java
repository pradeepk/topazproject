#set( $service = $artifactId )
#set( $Svc = "${service.substring(0, 1).toUpperCase()}${service.substring(1)}" )
/*
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package ${package};

import java.io.IOException;
import java.rmi.RemoteException;
import javax.xml.rpc.ServiceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.authentication.ProtectedService;
import org.topazproject.mulgara.itql.ItqlHelper;
import org.topazproject.fedora.client.APIMStubFactory;
import org.topazproject.fedora.client.FedoraAPIM;

/** 
 * This provides the implementation of the ${service} service.
 * 
 * @author foo
 */
public class ${Svc}Impl implements ${Svc} {
  private static final Log log = LogFactory.getLog(${Svc}Impl.class);

  private final ${Svc}PEP pep;
  private final ItqlHelper  itql;
  private final FedoraAPIM  apim;

  /**
   * Create a new ${service} service instance.
   *
   * @param itqlService   the itql web-service
   * @param fedoraService the fedora web-service
   * @param pep           the policy-enforcer to use for access-control
   * @throws ServiceException if an error occurred locating the itql or fedora services
   * @throws IOException if an error occurred talking to the itql or fedora services
   */
  public ${Svc}Impl(ProtectedService itqlService, ProtectedService fedoraService, ${Svc}PEP pep)
      throws IOException, ServiceException {
    this.pep = pep;
    itql = new ItqlHelper(itqlService);
    apim = APIMStubFactory.create(fedoraSvc);
  }
}
