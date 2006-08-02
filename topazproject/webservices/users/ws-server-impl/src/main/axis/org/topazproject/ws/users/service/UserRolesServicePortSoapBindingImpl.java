/*
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.ws.users.service;

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
import org.topazproject.ws.users.UserRolesImpl;
import org.topazproject.ws.users.UserRolesPEP;
import org.topazproject.xacml.Util;

/** 
 * Implements the server-side of the user-roles webservice. This is really just a wrapper around
 * the actual implementation in core.
 * 
 * @author Ronald Tschalär
 */
public class UserRolesServicePortSoapBindingImpl implements UserRoles, ServiceLifecycle {
  private static final Log log = LogFactory.getLog(UserRolesServicePortSoapBindingImpl.class);

  private UserRolesImpl impl;

  public void init(Object context) throws ServiceException {
    if (log.isTraceEnabled())
      log.trace("ServiceLifecycle#init");

    try {
      // get the pep
      final UserRolesPEP pep = new WSUserRolesPEP((ServletEndpointContext) context);

      // get other config
      Configuration conf = ConfigurationStore.getInstance().getConfiguration();
      conf = conf.subset("topaz");

      if (!conf.containsKey("services.itql.uri"))
        throw new ConfigurationException("missing key 'topaz.services.itql.uri'");

      HttpSession      session   = ((ServletEndpointContext) context).getHttpSession();
      Configuration    itqlConf  = conf.subset("services.itql");
      ProtectedService itqlSvc   = ProtectedServiceFactory.createService(itqlConf, session);

      // create the impl
      impl = new UserRolesImpl(itqlSvc, pep);
    } catch (Exception e) {
      log.error("Failed to initialize UserRolesImpl.", e);
      throw new ServiceException(e);
    }
  }

  public void destroy() {
    if (log.isTraceEnabled())
      log.trace("ServiceLifecycle#destroy");

    impl = null;
  }

  /**
   * @see org.topazproject.ws.users.UserRoles#getRoles
   */
  public String[] getRoles(String userId) throws RemoteException, NoSuchIdException {
    try {
      synchronized (impl) {
        return impl.getRoles(userId);
      }
    } catch (org.topazproject.ws.users.NoSuchIdException nsie) {
      log.debug("", nsie);
      throw new NoSuchIdException(nsie.getId());
    } catch (RuntimeException re) {
      log.warn("", re);
      throw re;
    } catch (Error e) {
      log.error("", e);
      throw e;
    }
  }

  /**
   * @see org.topazproject.ws.users.UserRoles#setRoles
   */
  public void setRoles(String userId, String[] authIds) throws RemoteException, NoSuchIdException {
    try {
      synchronized (impl) {
        impl.setRoles(userId, authIds);
      }
    } catch (org.topazproject.ws.users.NoSuchIdException nsie) {
      log.debug("", nsie);
      throw new NoSuchIdException(nsie.getId());
    } catch (RuntimeException re) {
      log.warn("", re);
      throw re;
    } catch (Error e) {
      log.error("", e);
      throw e;
    }
  }

  private static class WSUserRolesPEP extends UserRolesPEP {
    static {
      init(WSUserRolesPEP.class, SUPPORTED_ACTIONS, SUPPORTED_OBLIGATIONS);
    }

    public WSUserRolesPEP(ServletEndpointContext context) throws Exception {
      super(Util.lookupPDP(context, "topaz.user-roles.pdpName"), Util.createSubjAttrs(context));
    }
  }
}
