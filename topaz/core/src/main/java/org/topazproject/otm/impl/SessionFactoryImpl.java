/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm.impl;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.ProxyFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.otm.context.CurrentSessionContext;
import org.topazproject.otm.filter.FilterDefinition;
import org.topazproject.otm.metadata.AnnotationClassMetaFactory;
import org.topazproject.otm.query.DefaultQueryFunctionFactory;
import org.topazproject.otm.query.QueryFunctionFactory;
import org.topazproject.otm.serializer.SerializerFactory;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.TripleStore;
import org.topazproject.otm.Session;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.ModelConfig;

/**
 * A factory for otm sessions. It should be preloaded with the classes that would be persisted.
 * Also it holds the triple store and model/graph configurations. This class is multi-thread safe,
 * so long as the preload and configuration  operations are done at boot-strap time.
 *
 * @author Pradeep Krishnan
 */
public class SessionFactoryImpl implements SessionFactory {
  private static final Log log = LogFactory.getLog(SessionFactory.class);

  /**
   * rdf:type to Class mapping.
   */
  private final Map<String, Set<Class>> classmap = new HashMap<String, Set<Class>>();

  /**
   * Class to metadata mapping.
   */
  private final Map<Class<?>, ClassMetadata<?>> metadata = new HashMap<Class<?>, ClassMetadata<?>>();

  /**
   * Class name to metadata mapping.
   */
  private final Map<String, ClassMetadata<?>> cnamemap = new HashMap<String, ClassMetadata<?>>();

  /**
   * Entity name to metadata mapping.
   */
  private final Map<String, ClassMetadata<?>> entitymap = new HashMap<String, ClassMetadata<?>>();

  /**
   * Class to proxy class mapping.
   */
  private final Map<Class, Class> proxyClasses = new HashMap<Class, Class>();

  /**
   * Model to config mapping (uris, types etc.)
   */
  private final Map<String, ModelConfig> models = new HashMap<String, ModelConfig>();

  /**
   * Filter definitions by name.
   */
  private final Map<String, FilterDefinition> filterDefs = new HashMap<String, FilterDefinition>();

  /**
   * QueryFunction factories by function name.
   */
  private final Map<String, QueryFunctionFactory> qffMap =
                                                      new HashMap<String, QueryFunctionFactory>();

  private AnnotationClassMetaFactory cmf = new AnnotationClassMetaFactory(this);
  private SerializerFactory          serializerFactory = new SerializerFactory(this);
  private TripleStore                store;
  private CurrentSessionContext      currentSessionContext;

  {
    addQueryFunctionFactory(new DefaultQueryFunctionFactory());
  }

  /*
   * inherited javadoc
   */
  public Session openSession() {
    return new SessionImpl(this);
  }

  /*
   * inherited javadoc
   */
  public Session getCurrentSession() throws OtmException {
    if (currentSessionContext == null)
      throw new OtmException("CurrentSessionContext is not configured");

    return currentSessionContext.currentSession();
  }

  /*
   * inherited javadoc
   */
  public void preload(Class<?>[] classes) throws OtmException {
    for (Class<?> c : classes)
      preload(c);
  }

  /*
   * inherited javadoc
   */
  public void preload(Class<?> c) throws OtmException {
    if ((c == null) || Object.class.equals(c))
      return;

    try {
      preload(c.getSuperclass());
    } catch (Exception e) {
      if (log.isDebugEnabled())
        log.debug("Preload: skipped for " + c.getSuperclass(), e);
    }

    ClassMetadata<?> cm = cmf.create(c);

    setClassMetadata(cm);
  }

  /*
   * inherited javadoc
   */
  public Class mostSpecificSubClass(Class clazz, Collection<String> typeUris) {
    return mostSpecificSubClass(clazz, typeUris, false);
  }

  public Class mostSpecificSubClass(Class clazz, Collection<String> typeUris, boolean any) {
    if (typeUris.size() == 0)
      return clazz;

    ClassMetadata<?> solution  = null;

    for (String uri : typeUris) {
      Set<Class> classes = classmap.get(uri);

      if (classes == null)
        continue;

      Class candidate = clazz;

      //find the most specific class with the same rdf:type
      for (Class cl : classes) {
        if (candidate.isAssignableFrom(cl) && (any || isInstantiable(cl)))
          candidate = cl;
      }

      if (classes.contains(candidate)) {
        ClassMetadata<?> cm = metadata.get(candidate);
        if (solution == null)
          solution = cm;
        else if ((cm != null) && (solution.getTypes().size() < cm.getTypes().size()))
          solution = cm;
      }
    }

    return (solution != null) ? solution.getSourceClass() : null;
  }

  /*
   * inherited javadoc
   */
  public <T> void setClassMetadata(ClassMetadata<T> cm) throws OtmException {
    if (entitymap.containsKey(cm.getName())
         && !entitymap.get(cm.getName()).getSourceClass().equals(cm.getSourceClass()))
      throw new OtmException("An entity with name '" + cm.getName() + "' already exists.");

    entitymap.put(cm.getName(), cm);

    Class<T> c = cm.getSourceClass();
    metadata.put(c, cm);
    cnamemap.put(c.getName(), cm);
    if (cm.isEntity() || cm.isView())
      createProxy(c, cm);

    String type = cm.getType();

    if (type != null) {
      Set<Class> set = classmap.get(type);

      if (set == null) {
        set = new HashSet<Class>();
        classmap.put(type, set);
      }

      set.add(c);
    }

    if (log.isDebugEnabled())
      log.debug("setClassMetadata: type(" + cm.getType() + ") ==> " + cm);
  }

  /*
   * inherited javadoc
   */
  public <T> ClassMetadata<T> getClassMetadata(Class<? extends T> clazz) {
    ClassMetadata<T> cm = (ClassMetadata<T>) metadata.get(clazz);

    if (cm != null)
      return cm;

    clazz = getProxyMapping(clazz);

    if (clazz != null)
      cm = (ClassMetadata<T>) metadata.get(clazz);

    return cm;
  }

  /*
   * inherited javadoc
   */
  public ClassMetadata<?> getClassMetadata(String entity) {
    ClassMetadata<?> res = entitymap.get(entity);
    if (res == null)
      res = cnamemap.get(entity);

    return res;
  }

  /*
   * inherited javadoc
   */
  public Collection<ClassMetadata<?>> listClassMetadata() {
    return new ArrayList<ClassMetadata<?>>(metadata.values());
  }

  /*
   * inherited javadoc
   */
  public <T> Class<? extends T> getProxyMapping(Class<? extends T> clazz) {
    return proxyClasses.get(clazz);
  }

  /*
   * inherited javadoc
   */
  public ModelConfig getModel(String modelId) {
    return models.get(modelId);
  }

  /*
   * inherited javadoc
   */
  public void addModel(ModelConfig model) {
    models.put(model.getId(), model);
  }

  /*
   * inherited javadoc
   */
  public TripleStore getTripleStore() {
    return store;
  }

  /*
   * inherited javadoc
   */
  public void setTripleStore(TripleStore store) {
    this.store = store;
  }

  /*
   * inherited javadoc
   */
  public CurrentSessionContext getCurrentSessionContext() {
    return currentSessionContext;
  }

  /*
   * inherited javadoc
   */
  public void setCurrentSessionContext(CurrentSessionContext currentSessionContext) {
    this.currentSessionContext = currentSessionContext;
  }

  /*
   * inherited javadoc
   */
  public SerializerFactory getSerializerFactory() {
    return serializerFactory;
  }

  /*
   * inherited javadoc
   */
  public void addFilterDefinition(FilterDefinition fd) {
    filterDefs.put(fd.getFilterName(), fd);
  }

  /*
   * inherited javadoc
   */
  public void removeFilterDefinition(String filterName) {
    filterDefs.remove(filterName);
  }

  /*
   * inherited javadoc
   */
  public Collection<FilterDefinition> listFilterDefinitions() {
    return new ArrayList<FilterDefinition>(filterDefs.values());
  }

  /** 
   * Get the filter definition for the named filter. 
   * 
   * @param name the name of the filter
   * @return the filter definition, or null
   */
  FilterDefinition getFilterDefinition(String name) {
    return filterDefs.get(name);
  }

  public void addQueryFunctionFactory(QueryFunctionFactory qff) {
    for (String name : qff.getNames())
      qffMap.put(name, qff);
  }

  public void removeQueryFunctionFactory(QueryFunctionFactory qff) {
    for (String name : qff.getNames())
      qffMap.remove(name);
  }

  public Set<QueryFunctionFactory> listQueryFunctionFactories() {
    return new HashSet(qffMap.values());
  }

  public QueryFunctionFactory getQueryFunctionFactory(String funcName) {
    return qffMap.get(funcName);
  }

  private boolean isInstantiable(Class clazz) {
    int mod = clazz.getModifiers();

    return !Modifier.isAbstract(mod) && !Modifier.isInterface(mod) && Modifier.isPublic(mod);
  }

  private <T> void createProxy(Class<T> clazz, ClassMetadata<T> cm) {
    final Method getter = cm.getIdField().getGetter();

    MethodFilter mf     =
      new MethodFilter() {
        public boolean isHandled(Method m) {
          return !m.getName().equals("finalize") && !m.equals(getter);
        }
      };

    ProxyFactory f      = new ProxyFactory();
    f.setSuperclass(clazz);
    f.setFilter(mf);

    Class<? extends T> c = f.createClass();

    proxyClasses.put(clazz, c);
    proxyClasses.put(c, clazz);
  }
}
