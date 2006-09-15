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
import org.topazproject.configuration.ConfigurationStore;
import org.topazproject.ws.pap.impl.ProfilesImpl;
import org.topazproject.ws.pap.impl.ProfilesPEP;
import org.topazproject.ws.users.NoSuchUserIdException;
import org.topazproject.xacml.Util;

/** 
 * Implements the server-side of the profiles webservice. This is really just a wrapper around
 * the actual implementation in core.
 * 
 * @author Ronald Tschalär
 */
public class ProfilesServicePortSoapBindingImpl implements Profiles, ServiceLifecycle {
  private static final Log log = LogFactory.getLog(ProfilesServicePortSoapBindingImpl.class);

  private ProfilesImpl impl;

  public void init(Object context) throws ServiceException {
    if (log.isTraceEnabled())
      log.trace("ServiceLifecycle#init");

    try {
      // get the pep
      final ProfilesPEP pep = new WSProfilesPEP((ServletEndpointContext) context);

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
      impl = new ProfilesImpl(itqlSvc, fedoraSvc, pep);
    } catch (Exception e) {
      log.error("Failed to initialize ProfilesImpl.", e);
      throw new ServiceException(e);
    }
  }

  public void destroy() {
    if (log.isTraceEnabled())
      log.trace("ServiceLifecycle#destroy");

    impl = null;
  }

  /**
   * @see org.topazproject.ws.pap.Profiles#getProfile
   */
  public UserProfile getProfile(String userId) throws RemoteException, NoSuchUserIdException {
    try {
      synchronized (impl) {
        return impl.getProfile(userId);
      }
    } catch (NoSuchUserIdException nsuie) {
      log.debug("", nsuie);
      throw nsuie;
    } catch (RuntimeException re) {
      log.warn("", re);
      throw re;
    } catch (Error e) {
      log.error("", e);
      throw e;
    }
  }

  /**
   * @see org.topazproject.ws.pap.Profiles#setProfile
   */
  public void setProfile(String userId, UserProfile profile)
      throws RemoteException, NoSuchUserIdException {
    try {
      synchronized (impl) {
        impl.setProfile(userId, profile);
      }
    } catch (NoSuchUserIdException nsuie) {
      log.debug("", nsuie);
      throw nsuie;
    } catch (RuntimeException re) {
      log.warn("", re);
      throw re;
    } catch (Error e) {
      log.error("", e);
      throw e;
    }
  }

  private static class WSProfilesPEP extends ProfilesPEP {
    static {
      init(WSProfilesPEP.class, SUPPORTED_ACTIONS, SUPPORTED_OBLIGATIONS);
    }

    public WSProfilesPEP(ServletEndpointContext context) throws Exception {
      super(Util.lookupPDP(context, "topaz.profiles.pdpName"), Util.createSubjAttrs(context));
    }
  }
}
