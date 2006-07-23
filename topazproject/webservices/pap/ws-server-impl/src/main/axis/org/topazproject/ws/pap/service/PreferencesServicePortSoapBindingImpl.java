/*
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.ws.pap.service;

import java.io.IOException;
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
import org.topazproject.ws.pap.PreferencesImpl;
import org.topazproject.ws.pap.PreferencesPEP;
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

      if (!conf.containsKey("services.fedora.uri"))
        throw new ConfigurationException("missing key 'topaz.services.fedora.uri'");
      if (!conf.containsKey("services.itql.uri"))
        throw new ConfigurationException("missing key 'topaz.services.itql.uri'");

      final URI mulgara = new URI(conf.getString("services.itql.uri"));

      // create the impl
      impl = new PreferencesImpl(mulgara, pep);
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
      throws RemoteException, NoSuchIdException {
    try {
      impl.setPreferences(appId, userId, fromSvcPrefs(prefs));
    } catch (org.topazproject.ws.pap.NoSuchIdException nsie) {
      log.info("", nsie);
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
   * @see org.topazproject.ws.pap.Preferences#getPreferences
   */
  public UserPreference[] getPreferences(String appId, String userId)
      throws RemoteException, NoSuchIdException {
    try {
      return toSvcPrefs(impl.getPreferences(appId, userId));
    } catch (org.topazproject.ws.pap.NoSuchIdException nsie) {
      log.info("", nsie);
      throw new NoSuchIdException(nsie.getId());
    } catch (RuntimeException re) {
      log.warn("", re);
      throw re;
    } catch (Error e) {
      log.error("", e);
      throw e;
    }
  }

  private static final org.topazproject.ws.pap.UserPreference[] fromSvcPrefs(UserPreference[] prefs) {
    if (prefs == null)
      return null;

    org.topazproject.ws.pap.UserPreference[] res =
        new org.topazproject.ws.pap.UserPreference[prefs.length];
    for (int idx = 0; idx < res.length; idx++) {
      res[idx] = new org.topazproject.ws.pap.UserPreference();
      res[idx].setName(prefs[idx].getName());
      res[idx].setValues(prefs[idx].getValues());
    }

    return res;
  }

  private static final UserPreference[] toSvcPrefs(org.topazproject.ws.pap.UserPreference[] prefs) {
    if (prefs == null)
      return null;

    UserPreference[] res = new UserPreference[prefs.length];
    for (int idx = 0; idx < res.length; idx++) {
      res[idx] = new UserPreference();
      res[idx].setName(prefs[idx].getName());
      res[idx].setValues(prefs[idx].getValues());
    }

    return res;
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
