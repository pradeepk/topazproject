/*$HeadURL::                                                                             $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.ws.permissions;

import java.rmi.RemoteException;

import java.security.Principal;

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

import org.topazproject.common.ExceptionUtils;

import org.topazproject.configuration.ConfigurationStore;

import org.topazproject.ws.permissions.impl.PermissionsImpl;
import org.topazproject.ws.permissions.impl.PermissionsPEP;

import org.topazproject.xacml.Util;

/**
 * Implements the permissions webservice.
 *
 * @author Pradeep Krishnan
 */
public class PermissionsServicePortSoapBindingImpl implements Permissions, ServiceLifecycle {
  private static final Log log  = LogFactory.getLog(PermissionsServicePortSoapBindingImpl.class);
  private PermissionsImpl  impl;

  /**
   * Initialize a permissions impl.
   *
   * @param context the jax-rpc context
   *
   * @throws ServiceException on an error in initialize
   */
  public void init(Object context) throws ServiceException {
    if (log.isTraceEnabled())
      log.trace("ServiceLifecycle#init");

    try {
      // get the pep
      final PermissionsPEP pep = new WSPermissionsPEP((ServletEndpointContext) context);

      // get other config
      Configuration conf = ConfigurationStore.getInstance().getConfiguration();
      conf = conf.subset("topaz");

      if (!conf.containsKey("services.itql.uri"))
        throw new ConfigurationException("missing key 'topaz.services.itql.uri'");

      HttpSession      session  = ((ServletEndpointContext) context).getHttpSession();
      Configuration    itqlConf = conf.subset("services.itql");
      ProtectedService itqlSvc  = ProtectedServiceFactory.createService(itqlConf, session);

      Principal        p    = ((ServletEndpointContext) context).getUserPrincipal();
      String           user = (p == null) ? null : p.getName();

      // create the impl
      impl = new PermissionsImpl(itqlSvc, pep, user);
    } catch (Exception e) {
      log.error("Failed to initialize PermissionsImpl.", e);
      throw new ServiceException(e);
    }
  }

  /*
   * Destroys the permissions impl.
   */
  public void destroy() {
    if (log.isTraceEnabled())
      log.trace("ServiceLifecycle#destroy");

    impl = null;
  }

  /**
   * @see org.topazproject.ws.permissions.Permissions#grant
   */
  public void grant(String resource, String[] permissions, String[] principals)
             throws RemoteException {
    try {
      synchronized (impl) {
        impl.grant(resource, permissions, principals);
      }
    } catch (Throwable t) {
      newExceptionHandler(t).grant(null, null, null);
    }
  }

  /**
   * @see org.topazproject.ws.permissions.Permissions#revoke
   */
  public void revoke(String resource, String[] permissions, String[] principals)
              throws RemoteException {
    try {
      synchronized (impl) {
        impl.revoke(resource, permissions, principals);
      }
    } catch (Throwable t) {
      newExceptionHandler(t).revoke(null, null, null);
    }
  }

  /**
   * @see org.topazproject.ws.permissions.Permissions#cancelGrants
   */
  public void cancelGrants(String resource, String[] permissions, String[] principals)
                    throws RemoteException {
    try {
      synchronized (impl) {
        impl.cancelGrants(resource, permissions, principals);
      }
    } catch (Throwable t) {
      newExceptionHandler(t).cancelGrants(null, null, null);
    }
  }

  /**
   * @see org.topazproject.ws.permissions.Permissions#cancelRevokes
   */
  public void cancelRevokes(String resource, String[] permissions, String[] principals)
                     throws RemoteException {
    try {
      synchronized (impl) {
        impl.cancelRevokes(resource, permissions, principals);
      }
    } catch (Throwable t) {
      newExceptionHandler(t).cancelRevokes(null, null, null);
    }
  }

  /**
   * @see org.topazproject.ws.permissions.Permissions#listGrants
   */
  public String[] listGrants(String resource, String principal)
                      throws RemoteException {
    try {
      synchronized (impl) {
        return impl.listGrants(resource, principal);
      }
    } catch (Throwable t) {
      newExceptionHandler(t).listGrants(null, null);
      return null;      // not reached
    }
  }

  /**
   * @see org.topazproject.ws.permissions.Permissions#listRevokes
   */
  public String[] listRevokes(String resource, String principal)
                       throws RemoteException {
    try {
      synchronized (impl) {
        return impl.listRevokes(resource, principal);
      }
    } catch (Throwable t) {
      newExceptionHandler(t).listRevokes(null, null);
      return null;      // not reached
    }
  }

  private static Permissions newExceptionHandler(Throwable t) {
    return ((Permissions) ExceptionUtils.newExceptionHandler(Permissions.class, t, log));
  }

  private static class WSPermissionsPEP extends PermissionsPEP {
    static {
      init(WSPermissionsPEP.class, SUPPORTED_ACTIONS, SUPPORTED_OBLIGATIONS);
    }

    public WSPermissionsPEP(ServletEndpointContext context)
                     throws Exception {
      super(Util.lookupPDP(context, "topaz.permissions.pdpName"), Util.createSubjAttrs(context));
    }
  }
}
