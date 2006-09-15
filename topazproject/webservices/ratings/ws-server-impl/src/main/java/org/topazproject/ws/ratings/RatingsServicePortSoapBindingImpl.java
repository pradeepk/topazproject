/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.ws.ratings;

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
import org.topazproject.ws.ratings.impl.RatingsImpl;
import org.topazproject.ws.ratings.impl.RatingsPEP;
import org.topazproject.ws.users.NoSuchUserIdException;
import org.topazproject.xacml.Util;

/** 
 * Implements the server-side of the ratings webservice. This is really just a wrapper around
 * the actual implementation in core.
 * 
 * @author Ronald Tschalär
 */
public class RatingsServicePortSoapBindingImpl implements Ratings, ServiceLifecycle {
  private static final Log log = LogFactory.getLog(RatingsServicePortSoapBindingImpl.class);

  private RatingsImpl impl;

  public void init(Object context) throws ServiceException {
    if (log.isTraceEnabled())
      log.trace("ServiceLifecycle#init");

    try {
      // get the pep
      RatingsPEP pep = new WSRatingsPEP((ServletEndpointContext) context);

      // get other config
      Configuration conf = ConfigurationStore.getInstance().getConfiguration();
      conf = conf.subset("topaz");

      if (!conf.containsKey("services.itql.uri"))
        throw new ConfigurationException("missing key 'topaz.services.itql.uri'");

      Configuration    itqlConf = conf.subset("services.itql");
      HttpSession      session  = ((ServletEndpointContext) context).getHttpSession();
      ProtectedService itqlSvc  = ProtectedServiceFactory.createService(itqlConf, session);

      // create the impl
      impl = new RatingsImpl(itqlSvc, pep);
    } catch (Exception e) {
      log.error("Failed to initialize RatingsImpl.", e);
      throw new ServiceException(e);
    }
  }

  public void destroy() {
    if (log.isTraceEnabled())
      log.trace("ServiceLifecycle#destroy");

    impl = null;
  }

  /**
   * @see org.topazproject.ws.pap.Ratings#setRatings
   */
  public void setRatings(String appId, String userId, String object, ObjectRating[] ratings)
      throws RemoteException, NoSuchUserIdException {
    try {
      synchronized (impl) {
        impl.setRatings(appId, userId, object, ratings);
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
   * @see org.topazproject.ws.pap.Ratings#getRatings
   */
  public ObjectRating[] getRatings(String appId, String userId, String object)
      throws RemoteException, NoSuchUserIdException {
    try {
      synchronized (impl) {
        return impl.getRatings(appId, userId, object);
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
   * @see org.topazproject.ws.pap.Ratings#getRatingStats
   */
  public ObjectRatingStats[] getRatingStats(String appId, String object) throws RemoteException {
    try {
      synchronized (impl) {
        return impl.getRatingStats(appId, object);
      }
    } catch (RuntimeException re) {
      log.warn("", re);
      throw re;
    } catch (Error e) {
      log.error("", e);
      throw e;
    }
  }

  // The method delegations go here. You need to implement all methods from Ratings and
  // delegate them to the impl instance, converting parameters, return values, and exceptions
  // where necessary.

  private static class WSRatingsPEP extends RatingsPEP {
    static {
      init(WSRatingsPEP.class, SUPPORTED_ACTIONS, SUPPORTED_OBLIGATIONS);
    }

    public WSRatingsPEP(ServletEndpointContext context) throws Exception {
      super(Util.lookupPDP(context, "topaz.ratings.pdpName"), Util.createSubjAttrs(context));
    }
  }
}
