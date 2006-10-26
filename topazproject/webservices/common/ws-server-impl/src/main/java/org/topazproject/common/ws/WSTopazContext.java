/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.common.ws;

import java.io.IOException;

import java.lang.ref.SoftReference;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import java.rmi.RemoteException;

import java.security.Principal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import javax.xml.rpc.ServiceException;
import javax.xml.rpc.server.ServletEndpointContext;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;

import org.topazproject.authentication.ProtectedService;
import org.topazproject.authentication.ProtectedServiceFactory;

import org.topazproject.common.impl.TopazContext;
import org.topazproject.common.impl.TopazContextListener;

import org.topazproject.configuration.ConfigurationStore;

import org.topazproject.fedora.client.APIMStubFactory;
import org.topazproject.fedora.client.FedoraAPIA;
import org.topazproject.fedora.client.FedoraAPIM;
import org.topazproject.fedora.client.Uploader;

import org.topazproject.mulgara.itql.ItqlHelper;

/**
 * A TopazContext implementation that wraps a jax-rpc context.
 *
 * @author Pradeep Krishnan
 */
public class WSTopazContext implements TopazContext {
  private static final String                   serverName;
  private static final URI                      objectBaseUri;
  private static final URI                      fedoraBaseUri;
  private static final Configuration            itqlConfig;
  private static final Configuration            apimConfig;
  private static final Configuration            upldConfig;
  private static final HashMap                  poolMap    = new HashMap();
  private static final GenericObjectPool.Config poolConfig = new GenericObjectPool.Config();

  static {
    Configuration root = ConfigurationStore.getInstance().getConfiguration();
    itqlConfig   = root.subset("topaz.services.itql");
    apimConfig   = root.subset("topaz.services.fedora");
    upldConfig   = root.subset("topaz.services.fedoraUploader");
    serverName   = root.getString("topaz.server.hostname");

    String objectBase = root.getString("topaz.objects.base-uri");
    String fedoraBase = root.getString("topaz.services.fedora.uri");

    ItqlHelper.validateUri(itqlConfig.getString("uri"), "topaz.services.itql.uri");
    ItqlHelper.validateUri(upldConfig.getString("uri"), "topaz.services.fedoraUploader.uri");

    objectBaseUri = ItqlHelper.validateUri(objectBase, "topaz.objects.base-uri");

    URI uri = ItqlHelper.validateUri(fedoraBase, "topaz.services.fedora.uri");

    if (uri.getHost().equals("localhost")) {
      try {
        uri = new URI(uri.getScheme(), null, serverName, uri.getPort(), uri.getPath(), null, null);
      } catch (URISyntaxException use) {
        throw new Error(use); // Can't happen
      }
    }

    fedoraBaseUri   = uri;

    // xxx: Get this from config
    poolConfig.maxActive                        = 20;
    poolConfig.maxIdle                          = 20;
    poolConfig.maxWait                          = 1000; // time to block for handle if none available
    poolConfig.minEvictableIdleTimeMillis       = 5 * 60 * 1000; // 5 min
    poolConfig.minIdle                          = 0;
    poolConfig.numTestsPerEvictionRun           = 5;
    poolConfig.softMinEvictableIdleTimeMillis   = 5 * 60 * 1000; // 5 min
    poolConfig.testOnBorrow                     = false;
    poolConfig.testOnReturn                     = false;
    poolConfig.testWhileIdle                    = true;
    poolConfig.timeBetweenEvictionRunsMillis    = 5 * 60 * 1000; // 5 min
    poolConfig.whenExhaustedAction              = GenericObjectPool.WHEN_EXHAUSTED_BLOCK;
  }

  private final String           sessionKey;
  private List                   listeners   = new ArrayList();
  private boolean                active      = false;
  private FedoraAPIM             apim        = null;
  private Uploader               upld        = null;
  private ItqlHelperWrapper      itqlWrapper = null;
  private HandleCache            cache       = null;
  private HttpSession            session     = null;
  private ServletEndpointContext context;
  private ObjectPool             itqlPool;

  /**
   * Creates a new WSTopazContext object.
   *
   * @param sessionNs The namespace to use for storing objects in HttpSession
   */
  public WSTopazContext(String sessionNs) {
    sessionKey = sessionNs + ".handle-cache";

    synchronized (poolMap) {
      itqlPool = (ObjectPool) poolMap.get(sessionNs + ".itql-pool");

      if (itqlPool == null) {
        itqlPool = new GenericObjectPool(new ItqlHelperFactory(), poolConfig);
        poolMap.put(sessionNs + ".itql-pool", itqlPool);
      }
    }
  }

  /*
   * @see org.topazproject.common.impl.TopazContext
   */
  public void init(Object object) {
    context = (ServletEndpointContext) object;
  }

  /*
   * @see org.topazproject.common.impl.TopazContext
   */
  public void destroy() {
    listeners.clear();
    context = null;
  }

  /*
   * @see org.topazproject.common.impl.TopazContext
   */
  public void activate() {
    active = true;
  }

  /*
   * @see org.topazproject.common.impl.TopazContext
   */
  public void passivate() {
    active = false;

    if (apim != null) {
      cache.returnObject(apim);
      apim = null;
    }

    if (upld != null) {
      cache.returnObject(upld);
      upld = null;
    }

    if (itqlWrapper != null) {
      try {
        itqlPool.returnObject(itqlWrapper);
      } catch (Exception e) {
        // xxx: ignore this for now 
      }

      itqlWrapper = null;
    }

    cache     = null;
    session   = null;
  }

  /*
   * @see org.topazproject.common.impl.TopazContext
   */
  public boolean isActive() {
    return active;
  }

  /*
   * @see org.topazproject.common.impl.TopazContext
   */
  public ServletContext getServletContext() {
    return context.getServletContext();
  }

  /*
   * @see org.topazproject.common.impl.TopazContext
   */
  public Principal getUserPrincipal() throws IllegalStateException {
    if (!active)
      throw new IllegalStateException("not active");

    return context.getUserPrincipal();
  }

  /*
   * @see org.topazproject.common.impl.TopazContext
   */
  public HttpSession getHttpSession() throws IllegalStateException {
    if (!active)
      throw new IllegalStateException("not active");

    return context.getHttpSession();
  }

  /*
   * @see org.topazproject.common.impl.TopazContext
   */
  public String getUserName() throws IllegalStateException {
    Principal principal = getUserPrincipal();

    return (principal == null) ? null : principal.getName();
  }

  /*
   * @see org.topazproject.common.impl.TopazContext
   */
  public String getServerName() {
    return serverName;
  }

  /*
   * @see org.topazproject.common.impl.TopazContext
   */
  public URI getObjectBaseUri() {
    return objectBaseUri;
  }

  /*
   * @see org.topazproject.common.impl.TopazContext
   */
  public URI getFedoraBaseUri() {
    return fedoraBaseUri;
  }

  /*
   * @see org.topazproject.common.impl.TopazContext
   */
  public ItqlHelper getItqlHelper() throws RemoteException, IllegalStateException {
    if (itqlWrapper == null) {
      try {
        itqlWrapper = (ItqlHelperWrapper) itqlPool.borrowObject();
      } catch (RemoteException e) {
        throw e;
      } catch (Exception e) {
        throw new RemoteException("", e);
      }

      if (itqlWrapper.isNewHandle()) {
        notifyHandleCreated(itqlWrapper.ref());
        itqlWrapper.setNewHandle(false);
      }
    }

    return itqlWrapper.ref();
  }

  /*
   * @see org.topazproject.common.impl.TopazContext
   */
  public FedoraAPIM getFedoraAPIM() throws RemoteException, IllegalStateException {
    if (apim != null)
      return apim;

    apim = (FedoraAPIM) getHandle(FedoraAPIM.class);

    if (apim != null)
      return apim;

    try {
      ProtectedService svc = ProtectedServiceFactory.createService(apimConfig, session);
      apim = APIMStubFactory.create(svc);
      notifyHandleCreated(apim);
    } catch (URISyntaxException e) {
      throw new Error(e); // already tested; so shouldn't happend
    } catch (MalformedURLException e) {
      throw new Error(e);
    } catch (ServiceException e) {
      throw new RemoteException("", e);
    } catch (IOException e) {
      throw new RemoteException("", e);
    }

    return apim;
  }

  /*
   * @see org.topazproject.common.impl.TopazContext
   */
  public Uploader getFedoraUploader() throws RemoteException, IllegalStateException {
    if (upld != null)
      return upld;

    upld = (Uploader) getHandle(Uploader.class);

    if (upld != null)
      return upld;

    try {
      ProtectedService svc = ProtectedServiceFactory.createService(upldConfig, session);
      upld = new Uploader(svc);
      notifyHandleCreated(upld);
    } catch (URISyntaxException e) {
      throw new Error(e); // already tested; so shouldn't happend
    } catch (IOException e) {
      throw new RemoteException("", e);
    }

    return upld;
  }

  /*
   * @see org.topazproject.common.impl.TopazContext
   */
  public FedoraAPIA getFedoraAPIA() throws RemoteException, IllegalStateException {
    throw new UnsupportedOperationException("not implemented");
  }

  /*
   * @see org.topazproject.common.impl.TopazContext
   */
  public void addListener(TopazContextListener listener) {
    if (!listeners.contains(listener))
      listeners.add(listener);
  }

  /*
   * @see org.topazproject.common.impl.TopazContext
   */
  public void removeListener(TopazContextListener listener) {
    listeners.remove(listener);
  }

  /**
   * Notify the listeners that a new handle has been created
   *
   * @param handle the handle that we are reporting (itql, apim etc.)
   */
  protected void notifyHandleCreated(Object handle) {
    Iterator it = listeners.iterator();

    while (it.hasNext()) {
      TopazContextListener listener = (TopazContextListener) it.next();
      listener.handleCreated(this, handle);
    }
  }

  private Object getHandle(Class clazz) throws IllegalStateException {
    if (cache != null)
      return cache.borrowObject(clazz);

    if (session == null)
      session = getHttpSession();

    cache = (HandleCache) session.getAttribute(sessionKey);

    if (cache != null)
      return cache.borrowObject(clazz);

    cache = new HandleCache();
    session.setAttribute(sessionKey, cache);

    return null;
  }

  /**
   * There is one cache per HttpSession. Usually the cache only contains the three handles (itql,
   * apim, upld). However if the client is multi-threaded and issues multiple calls to us, we have
   * to  create more handle objects since these are typically not multi-thread safe.
   * 
   * <p>
   * Using a SoftReference here since the handles are heavy wieght and so we should let the gc get
   * to it under heavy load.
   * </p>
   */
  private static class HandleCache {
    private SoftReference[] refs  = new SoftReference[3];
    int                     count = 0;

    public synchronized Object borrowObject(Class clazz) {
      for (int i = count - 1; i >= 0; i--) {
        Object o = refs[i].get();

        if (clazz.isInstance(o)) {
          if (--count != i)
            System.arraycopy(refs, i + 1, refs, i, count - i);

          return o;
        }
      }

      return null;
    }

    public synchronized void returnObject(Object borrowed) {
      if (count >= refs.length) {
        SoftReference[] newRefs = new SoftReference[refs.length * 2];
        System.arraycopy(refs, 0, newRefs, 0, refs.length);
        refs = newRefs;
      }

      refs[count++] = new SoftReference(borrowed);
    }
  }

  private static class ItqlHelperWrapper {
    private ItqlHelper itql;
    private boolean    newHandle = true;

    public ItqlHelperWrapper(ItqlHelper itql) {
      this.itql = itql;
    }

    public ItqlHelper ref() {
      return itql;
    }

    public boolean isNewHandle() {
      return newHandle;
    }

    public void setNewHandle(boolean newHandle) {
      this.newHandle = newHandle;
    }
  }

  private static class ItqlHelperFactory extends BasePoolableObjectFactory {
    public Object makeObject() throws Exception {
      //xxx: assume that we aren't using an auth scheme that requires HttpSession (eg. CAS)
      ProtectedService svc = ProtectedServiceFactory.createService(itqlConfig, (HttpSession) null);

      return new ItqlHelperWrapper(new ItqlHelper(svc));
    }

    public void destroyObject(Object obj) throws Exception {
      ItqlHelperWrapper wrapper = (ItqlHelperWrapper) obj;
      ItqlHelper        itql = wrapper.ref();
      itql.close();
    }
  }
}
