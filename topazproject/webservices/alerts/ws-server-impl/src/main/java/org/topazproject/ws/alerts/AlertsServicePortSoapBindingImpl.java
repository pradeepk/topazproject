/* $HeadURL::                                                                             $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.ws.alerts;

import java.util.Calendar;
import java.util.Date;
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
import org.topazproject.common.ExceptionUtils;
import org.topazproject.configuration.ConfigurationStore;
import org.topazproject.ws.alerts.impl.AlertsImpl;
import org.topazproject.ws.alerts.impl.AlertsPEP;
import org.topazproject.xacml.ws.WSXacmlUtil;

/** 
 * Implements the server-side of the alerts webservice. This is really just a wrapper around
 * the actual implementation in core.
 * 
 * @author Eric Brown
 */
public class AlertsServicePortSoapBindingImpl implements Alerts, ServiceLifecycle {
  private static final Log log = LogFactory.getLog(AlertsServicePortSoapBindingImpl.class);

  private AlertsImpl impl;

  public void init(Object context) throws ServiceException {
    if (log.isTraceEnabled())
      log.trace("ServiceLifecycle#init");

    try {
      // get the pep
      AlertsPEP pep = new WSAlertsPEP((ServletEndpointContext) context);

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
      impl = new AlertsImpl(itqlSvc, fedoraSvc, pep);
    } catch (Exception e) {
      log.error("Failed to initialize AlertsImpl.", e);
      throw new ServiceException(e);
    }
  }

  public void destroy() {
    if (log.isTraceEnabled())
      log.trace("ServiceLifecycle#destroy");

    impl = null;
  }

  // The method delegations go here. You need to implement all methods from Alerts and
  // delegate them to the impl instance, converting parameters, return values, and exceptions
  // where necessary.

  public boolean sendAlerts(String endDate, int count) throws RemoteException {
    try {
      return impl.sendAlerts(endDate, count);
    } catch (Throwable t) {
      newExceptionHandler(t).sendAlerts(null, 0);
      return false;     // not reached
    }
  }

  public boolean sendAllAlerts() throws RemoteException {
    try {
      return impl.sendAllAlerts();
    } catch (Throwable t) {
      newExceptionHandler(t).sendAllAlerts();
      return false;     // not reached
    }
  }

  public void startUser(String userId, String date) throws RemoteException {
    try {
      impl.startUser(userId, date);
    } catch (Throwable t) {
      newExceptionHandler(t).startUser(null, null);
    }
  }

  public void startUser(String userId) throws RemoteException {
    try {
      impl.startUser(userId);
    } catch (Throwable t) {
      newExceptionHandler(t).startUser(null);
    }
  }

  public void clearUser(String userId) throws RemoteException {
    try {
      impl.clearUser(userId);
    } catch (Throwable t) {
      newExceptionHandler(t).clearUser(null);
    }
  }

  private static Alerts newExceptionHandler(Throwable t) {
    return ((Alerts) ExceptionUtils.newExceptionHandler(Alerts.class, t, log));
  }

  private static class WSAlertsPEP extends AlertsPEP {
    static {
      init(WSAlertsPEP.class, SUPPORTED_ACTIONS, SUPPORTED_OBLIGATIONS);
    }

    public WSAlertsPEP(ServletEndpointContext context) throws Exception {
      super(WSXacmlUtil.lookupPDP(context, "topaz.alerts.pdpName"),
            WSXacmlUtil.createSubjAttrs(context));
    }
  }
}
