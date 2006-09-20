/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.ws.users;

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
import org.topazproject.common.ExceptionUtils;
import org.topazproject.configuration.ConfigurationStore;
import org.topazproject.ws.users.impl.UserAccountsImpl;
import org.topazproject.ws.users.impl.UserAccountsPEP;
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
    } catch (Throwable t) {
      newExceptionHandler(t).createUser(null);
      return null;      // not reached
    }
  }

  /**
   * @see org.topazproject.ws.users.UserAccounts#deleteUser
   */
  public void deleteUser(String userId) throws RemoteException, NoSuchUserIdException {
    try {
      synchronized (impl) {
        impl.deleteUser(userId);
      }
    } catch (Throwable t) {
      newExceptionHandler(t).deleteUser(null);
    }
  }

  /**
   * @see org.topazproject.ws.users.UserAccounts#getState
   */
  public int getState(String userId) throws RemoteException, NoSuchUserIdException {
    try {
      synchronized (impl) {
        return impl.getState(userId);
      }
    } catch (Throwable t) {
      newExceptionHandler(t).getState(null);
      return 0;         // not reached
    }
  }

  /**
   * @see org.topazproject.ws.users.UserAccounts#setState
   */
  public void setState(String userId, int state) throws RemoteException, NoSuchUserIdException {
    try {
      synchronized (impl) {
        impl.setState(userId, state);
      }
    } catch (Throwable t) {
      newExceptionHandler(t).setState(null, 0);
    }
  }

  /**
   * @see org.topazproject.ws.users.UserAccounts#getAuthenticationIds
   */
  public String[] getAuthenticationIds(String userId)
      throws RemoteException, NoSuchUserIdException {
    try {
      synchronized (impl) {
        return impl.getAuthenticationIds(userId);
      }
    } catch (Throwable t) {
      newExceptionHandler(t).getAuthenticationIds(null);
      return null;      // not reached
    }
  }

  /**
   * @see org.topazproject.ws.users.UserAccounts#setAuthenticationIds
   */
  public void setAuthenticationIds(String userId, String[] authIds)
      throws RemoteException, NoSuchUserIdException {
    try {
      synchronized (impl) {
        impl.setAuthenticationIds(userId, authIds);
      }
    } catch (Throwable t) {
      newExceptionHandler(t).setAuthenticationIds(null, null);
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
    } catch (Throwable t) {
      newExceptionHandler(t).lookUpUserByAuthId(null);
      return null;      // not reached
    }
  }

  private static UserAccounts newExceptionHandler(Throwable t) {
    return ((UserAccounts) ExceptionUtils.newExceptionHandler(UserAccounts.class, t, log));
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
