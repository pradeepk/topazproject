/* $HeadURL::                                                                            $
 * $Id$
 *
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
import org.topazproject.ws.users.UserAccountsImpl;
import org.topazproject.ws.users.UserAccountsPEP;
import org.topazproject.xacml.Util;

/** 
 * Implements the server-side of the user-accounts webservice. This is really just a wrapper around
 * the actual implementation in core.
 * 
 * @author Ronald Tschalär
 */
public class UserAccountsServicePortSoapBindingImpl implements UserAccounts, ServiceLifecycle {
  private static final Log log = LogFactory.getLog(UserAccountsServicePortSoapBindingImpl.class);

  private UserAccountsImpl impl;

  public void init(Object context) throws ServiceException {
    if (log.isTraceEnabled())
      log.trace("ServiceLifecycle#init");

    try {
      // get the pep
      final UserAccountsPEP pep = new WSUserAccountsPEP((ServletEndpointContext) context);

      // get other config
      Configuration conf = ConfigurationStore.getInstance().getConfiguration();
      conf = conf.subset("topaz");

      if (!conf.containsKey("services.fedora.uri"))
        throw new ConfigurationException("missing key 'topaz.services.fedora.uri'");
      if (!conf.containsKey("services.itql.uri"))
        throw new ConfigurationException("missing key 'topaz.services.itql.uri'");

      HttpSession      session   = ((ServletEndpointContext) context).getHttpSession();
      Configuration    itqlConf  = conf.subset("services.itql");
      Configuration    fdraConf  = conf.subset("services.fedora");
      ProtectedService itqlSvc   = ProtectedServiceFactory.createService(itqlConf, session);
      ProtectedService fedoraSvc = ProtectedServiceFactory.createService(fdraConf, session);

      // create the impl
      impl = new UserAccountsImpl(itqlSvc, fedoraSvc, pep);
    } catch (Exception e) {
      log.error("Failed to initialize UserAccountsImpl.", e);
      throw new ServiceException(e);
    }
  }

  public void destroy() {
    if (log.isTraceEnabled())
      log.trace("ServiceLifecycle#destroy");

    impl = null;
  }

  /**
   * @see org.topazproject.ws.users.UserAccounts#createUser
   */
  public String createUser(String authId) throws RemoteException {
    try {
      synchronized (impl) {
        return impl.createUser(authId);
      }
    } catch (RuntimeException re) {
      log.warn("", re);
      throw re;
    } catch (Error e) {
      log.error("", e);
      throw e;
    }
  }

  /**
   * @see org.topazproject.ws.users.UserAccounts#deleteUser
   */
  public void deleteUser(String userId) throws RemoteException, NoSuchIdException {
    try {
      synchronized (impl) {
        impl.deleteUser(userId);
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
   * @see org.topazproject.ws.users.UserAccounts#getAuthenticationIds
   */
  public String[] getAuthenticationIds(String userId) throws RemoteException, NoSuchIdException {
    try {
      synchronized (impl) {
        return impl.getAuthenticationIds(userId);
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
   * @see org.topazproject.ws.users.UserAccounts#setAuthenticationIds
   */
  public void setAuthenticationIds(String userId, String[] authIds)
      throws RemoteException, NoSuchIdException {
    try {
      synchronized (impl) {
        impl.setAuthenticationIds(userId, authIds);
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
   * @see org.topazproject.ws.userusers.UserAccountLookup#lookUpUserByAuthId
   */
  public String lookUpUserByAuthId(String authId) throws RemoteException {
    try {
      synchronized (impl) {
        return impl.lookUpUserByAuthId(authId);
      }
    } catch (RuntimeException re) {
      log.warn("", re);
      throw re;
    } catch (Error e) {
      log.error("", e);
      throw e;
    }
  }

  private static class WSUserAccountsPEP extends UserAccountsPEP {
    static {
      init(WSUserAccountsPEP.class, SUPPORTED_ACTIONS, SUPPORTED_OBLIGATIONS);
    }

    public WSUserAccountsPEP(ServletEndpointContext context) throws Exception {
      super(Util.lookupPDP(context, "topaz.user-accounts.pdpName"), Util.createSubjAttrs(context));
    }
  }
}
