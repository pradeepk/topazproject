/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007-2008 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.topazproject.otm.mapping.java;

import java.io.ObjectStreamException;
import java.io.Serializable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import java.util.Collections;

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.EntityMode;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.mapping.Binder;
import org.topazproject.otm.mapping.EntityBinder;
import org.topazproject.otm.mapping.EntityBinder.LazyLoaded;
import org.topazproject.otm.mapping.EntityBinder.LazyLoader;
import org.topazproject.otm.mapping.Mapper;

/**
 * Binds an entity to an {@link org.topazproject.otm.EntityMode#POJO} specific implementation.
 *
 * @author Pradeep Krishnan
 *
 * @param <T> The object type instantiated by this class
 */
public class ClassBinder<T> implements EntityBinder {
  private final Class<T>          clazz;
  private final Class<?extends T> proxy;
  private final boolean           instantiable;
  private static final Log        log = LogFactory.getLog(ClassBinder.class);

  /**
   * Creates a new ClassBinder object.
   *
   * @param clazz the java class to bind this entity to
   * @param ignore the methods to ignore when lazily loaded
   */
  public ClassBinder(Class<T> clazz, Method... ignore) {
    this.clazz = clazz;

    int mod = clazz.getModifiers();
    instantiable   = !Modifier.isAbstract(mod) && !Modifier.isInterface(mod)
                      && Modifier.isPublic(mod);
    proxy          = instantiable ? createProxy(clazz, ignore) : null;
  }

  /*
   * inherited javadoc
   */
  public T newInstance() throws OtmException {
    try {
      return clazz.newInstance();
    } catch (Exception t) {
      throw new OtmException("Failed to create a new instance of " + clazz, t);
    }
  }

  /*
   * inherited javadoc
   */
  public LazyLoaded newLazyLoadedInstance(LazyLoader ll)
                                   throws OtmException {
    try {
      Object        o  = proxy.newInstance();
      ClassMetadata cm = ll.getClassMetadata();

      if (cm.getIdField() != null)
        cm.getIdField().getBinder(EntityMode.POJO).set(o, Collections.singletonList(ll.getId()));

      ((ProxyObject) o).setHandler(new Handler(ll));

      return (LazyLoaded) o;
    } catch (Exception t) {
      throw new OtmException("Failed to create a new lazy-loaded instance of " + clazz, t);
    }
  }

  /*
   * inherited javadoc
   */
  public boolean isInstantiable() {
    return instantiable;
  }

  /*
   * inherited javadoc
   */
  public boolean isInstance(Object o) {
    return clazz.isInstance(o);
  }

  /*
   * inherited javadoc
   */
  public boolean isAssignableFrom(EntityBinder other) {
    return (other instanceof ClassBinder) && clazz.isAssignableFrom(((ClassBinder) other).clazz);
  }

  /**
   * Gets the java class that this binder binds to.
   *
   * @return the java class bound by this
   */
  public Class<T> getSourceClass() {
    return clazz;
  }

  public static <T> Class<?extends T> createProxy(Class<T> clazz, final Method[] ignoreList) {
    MethodFilter mf =
      new MethodFilter() {
        public boolean isHandled(Method m) {
          if (m.getName().equals("finalize"))
            return false;

          for (Method ignore : ignoreList)
            if (m.equals(ignore))
              return false;

          return true;
        }
      };

    ProxyFactory f  = new ProxyFactory();
    f.setSuperclass(clazz);

    if (Serializable.class.isAssignableFrom(clazz))
      f.setInterfaces(new Class[] { WriteReplace.class, LazyLoaded.class });
    else
      f.setInterfaces(new Class[] { LazyLoaded.class });

    f.setFilter(mf);

    Class<?extends T> c = f.createClass();

    return c;
  }

  /*
   * inherited javadoc
   */
  public String[] getNames() {
    return new String[] { clazz.getName() };
  }

  public static interface WriteReplace {
    public Object writeReplace() throws ObjectStreamException;
  }

  private class Handler implements MethodHandler {
    private final LazyLoader ll;

    public Handler(LazyLoader ll) {
      this.ll = ll;
    }

    public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args)
                  throws Throwable {
      ll.ensureDataLoad((LazyLoaded) self, thisMethod.getName());

      if ("writeReplace".equals(thisMethod.getName()) && (args.length == 0) 
          && (self instanceof Serializable))
        return getReplacement(self);

      try {
        return proceed.invoke(self, args);
      } catch (InvocationTargetException ite) {
        log.warn("Caught ite while invoking '" + proceed + "' on '" + self + "'", ite);
        throw ite.getCause();
      }
    }

    private Object getReplacement(Object o) throws Throwable {
      ClassMetadata cm  = ll.getClassMetadata();
      EntityMode    em  = ll.getSession().getEntityMode();
      Object        rep = cm.getEntityBinder(em).newInstance();

      for (Mapper m : new Mapper[] { cm.getIdField(), cm.getBlobField() })
        if (m != null) {
          Binder b = m.getBinder(em);
          b.setRawValue(rep, b.getRawValue(o, false));
        }

      for (Mapper m : cm.getRdfMappers()) {
        Binder b = m.getBinder(em);
        b.setRawValue(rep, b.getRawValue(o, false));
      }

      if (log.isDebugEnabled())
        log.debug("Serializable replacement created for " + o);

      return rep;
    }
  }
}
