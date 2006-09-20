/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.ws.pap;

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
import org.topazproject.ws.pap.impl.PreferencesImpl;
import org.topazproject.ws.pap.impl.PreferencesPEP;
import org.topazproject.ws.users.NoSuchUserIdException;
import org.topazproject.xacml.Util;

/** 
 * Implements the server-side of the preferences webservice. This is really just a wrapper around
 * the actual implementation in core.
 * 
 * @author Ronald Tschalär
 */
public class PreferencesServicePortSoapBindingImpl implements Preferences, ServiceLifecycle {
  private static final Log log = LogFactory.getLog(PreferencesServicePortSoapBindingImpl.class);

  private PreferencesImpl impl;

  public void init(Object context) throws ServiceException {
    if (log.isTraceEnabled())
      log.trace("ServiceLifecycle#init");

    try {
      // get the pep
      final PreferencesPEP pep = new WSPreferencesPEP((ServletEndpointContext) context);

      // get other config
      Configuration conf = ConfigurationStore.getInstance().getConfiguration();
      conf = conf.subset("topaz");

      if (!conf.containsKey("services.itql.uri"))
        throw new ConfigurationException("missing key 'topaz.services.itql.uri'");

      HttpSession      session  = ((ServletEndpointContext) context).getHttpSession();
      Configuration    itqlConf = conf.subset("services.itql");
      ProtectedService itqlSvc  = ProtectedServiceFactory.createService(itqlConf, session);

      // create the impl
      impl = new PreferencesImpl(itqlSvc, pep);
    } catch (Exception e) {
      log.error("Failed to initialize PreferencesImpl.", e);
      throw new ServiceException(e);
    }
  }

  public void destroy() {
    if (log.isTraceEnabled())
      log.trace("ServiceLifecycle#destroy");

    impl = null;
  }

  /**
   * @see org.topazproject.ws.pap.Preferences#setPreferences
   */
  public void setPreferences(String appId, String userId, UserPreference[] prefs)
      throws RemoteException, NoSuchUserIdException {
    try {
      synchronized (impl) {
        impl.setPreferences(appId, userId, prefs);
      }
    } catch (Throwable t) {
      newExceptionHandler(t).setPreferences(null, null, null);
    }
  }

  /**
   * @see org.topazproject.ws.pap.Preferences#getPreferences
   */
  public UserPreference[] getPreferences(String appId, String userId)
      throws RemoteException, NoSuchUserIdException {
    try {
      synchronized (impl) {
        return impl.getPreferences(appId, userId);
      }
    } catch (Throwable t) {
      newExceptionHandler(t).getPreferences(null, null);
      return null;      // not reached
    }
  }

  private static Preferences newExceptionHandler(Throwable t) {
    return ((Preferences) ExceptionUtils.newExceptionHandler(Preferences.class, t, log));
  }

  private static class WSPreferencesPEP extends PreferencesPEP {
    static {
      init(WSPreferencesPEP.class, SUPPORTED_ACTIONS, SUPPORTED_OBLIGATIONS);
    }

    public WSPreferencesPEP(ServletEndpointContext context) throws Exception {
      super(Util.lookupPDP(context, "topaz.preferences.pdpName"), Util.createSubjAttrs(context));
    }
  }
}
