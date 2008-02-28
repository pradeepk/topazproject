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

import java.net.URI;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;
import javax.transaction.TransactionManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.objectweb.jotm.Jotm;

import org.topazproject.otm.context.CurrentSessionContext;
import org.topazproject.otm.filter.FilterDefinition;
import org.topazproject.otm.mapping.java.ClassBinder;
import org.topazproject.otm.metadata.AnnotationClassMetaFactory;
import org.topazproject.otm.query.DefaultQueryFunctionFactory;
import org.topazproject.otm.query.QueryFunctionFactory;
import org.topazproject.otm.serializer.SerializerFactory;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.EntityMode;
import org.topazproject.otm.ModelConfig;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Rdf;
import org.topazproject.otm.Rdfs;
import org.topazproject.otm.Session;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.TripleStore;
import org.topazproject.otm.BlobStore;

/**
 * A factory for otm sessions. It should be preloaded with the classes that would be persisted.
 * Also it holds the triple store and model/graph configurations. This class is multi-thread safe,
 * so long as the preload and configuration  operations are done at boot-strap time.
 *
 * <p>Instances are preloaded with the following aliases by default: <var>rdf</var>,
 * <var>rdfs</var>, <var>owl</var>, <var>xsd</var>, <var>dc</var>, <var>dc_terms</var>,
 * <var>mulgara</var>, <var>topaz</var>. Also, the {@link DefaultQueryFunctionFactory
 * DefaultQueryFunctionFactory} is always added.
 *
 * @author Pradeep Krishnan
 */
public class SessionFactoryImpl implements SessionFactory {
  private static final Log log = LogFactory.getLog(SessionFactory.class);

  /**
   * rdf:type to entity mapping
   */
  private final Map<String, Set<ClassMetadata>> typemap = new HashMap<String, Set<ClassMetadata>>();

  /**
   * Name to metadata mapping.
   */
  private final Map<String, ClassMetadata> entitymap = new HashMap<String, ClassMetadata>();

  /**
   * Entity name to sub-class entity name mapping. Note that 'null' key is used to indicate
   * root classes.
   */
  private final Map<String, Set<ClassMetadata>> subClasses = new HashMap<String, Set<ClassMetadata>>();

  /**
   * POJO mapping specific: Class to ClassMetadata mapping
   */
  private final Map<Class, ClassMetadata> classmap = new HashMap<Class, ClassMetadata>();

  /**
   * Model to config mapping (uris, types etc.)
   */
  private final Map<String, ModelConfig> modelsByName = new HashMap<String, ModelConfig>();

  /**
   * Model-type to config mapping (uris, types etc.)
   */
  private final Map<URI, List<ModelConfig>> modelsByType = new HashMap<URI, List<ModelConfig>>();

  /**
   * Filter definitions by name.
   */
  private final Map<String, FilterDefinition> filterDefs = new HashMap<String, FilterDefinition>();

  /**
   * QueryFunction factories by function name.
   */
  private final Map<String, QueryFunctionFactory> qffMap =
                                                      new HashMap<String, QueryFunctionFactory>();

  /**
   * Aliases
   */
  private final Map<String, String> aliases = new HashMap<String, String>();

  private AnnotationClassMetaFactory cmf = new AnnotationClassMetaFactory(this);
  private SerializerFactory          serializerFactory = new SerializerFactory(this);
  private TripleStore                tripleStore;
  private BlobStore                  blobStore;
  private Jotm                       jotm;
  private TransactionManager         txMgr;
  private CurrentSessionContext      currentSessionContext;

  {
    // set up defaults
    addQueryFunctionFactory(new DefaultQueryFunctionFactory());

    addAlias("rdf",      Rdf.rdf);
    addAlias("rdfs",     Rdfs.base);
    addAlias("xsd",      Rdf.xsd);
    addAlias("dc",       Rdf.dc);
    addAlias("dc_terms", Rdf.dc_terms);
    addAlias("mulgara",  Rdf.mulgara);
    addAlias("topaz",    Rdf.topaz);
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

    preload(c.getSuperclass());

    if (getClassMetadata(c) == null)
      setClassMetadata(cmf.create(c));
  }

  /*
   * inherited javadoc
   */
  public ClassMetadata getSubClassMetadata(ClassMetadata clazz, EntityMode mode, 
                                Collection<String> typeUris) {
    return getSubClassMetadata(clazz, mode, typeUris, true);
  }

  /*
   * inherited javadoc
   */
  public ClassMetadata getAnySubClassMetadata(ClassMetadata clazz, Collection<String> typeUris) {
    return getSubClassMetadata(clazz, null, typeUris, false);
  }

  private ClassMetadata getSubClassMetadata(ClassMetadata clazz, EntityMode mode, 
                                Collection<String> typeUris, boolean instantiable) {
    if ((typeUris == null) || (typeUris.size() == 0))
      return (clazz == null) ? null : ((clazz.getType() != null) ? null : clazz);

    ClassMetadata solution  = null;

    for (String uri : typeUris) {
      Set<ClassMetadata> classes = typemap.get(uri);

      if (classes == null)
        continue;

      ClassMetadata candidate = clazz;

      //find the most specific class with the same rdf:type
      for (ClassMetadata cl : classes) {
        if (instantiable && !cl.getEntityBinder(mode).isInstantiable())
          continue;

        if ((candidate == null) || candidate.isAssignableFrom(cl))
          candidate = cl;
      }

      // accept this candidate only if it corresponds to the type-uris
      if (classes.contains(candidate) 
          && ((solution == null) || solution.isAssignableFrom(candidate)))
            solution = candidate;
    }

    return solution;
  }

  /*
   * inherited javadoc
   */
  public ClassMetadata getInstanceMetadata(ClassMetadata clazz, EntityMode mode, Object instance) {
    if ((clazz != null) && !clazz.getEntityBinder(mode).isInstance(instance))
      return null;

    ClassMetadata candidate = clazz;
    Collection<ClassMetadata> sub = subClasses.get((clazz == null) ? null : clazz.getName());

    if (sub != null) {
      for (ClassMetadata s : sub) {
        ClassMetadata cm = getInstanceMetadata(s, mode, instance);
        if ((cm != null) && ((candidate == null) || candidate.isAssignableFrom(cm)))
          candidate = cm;
      }
    }
    return candidate;
  }

  /*
   * inherited javadoc
   */
  public void setClassMetadata(ClassMetadata cm) throws OtmException {
    for (String name : cm.getNames()) {
      ClassMetadata other = entitymap.get(name);
      if (other != null)
        throw new OtmException("An entity with name or alias of '" + name + "' already exists.");
    }

    // XXX: temporary
    Class c = ((ClassBinder)cm.getEntityBinder(EntityMode.POJO)).getSourceClass();
    if (classmap.containsKey(c))
      throw new OtmException("An entity for class " + c + " already exists.");
    classmap.put(c, cm);

    for (String name : cm.getNames())
      entitymap.put(name, cm);

    String type = cm.getType();

    if (type != null) {
      Set<ClassMetadata> set = typemap.get(type);

      if (set == null) {
        set = new HashSet<ClassMetadata>();
        typemap.put(type, set);
      }

      set.add(cm);
    }

    String             sup = cm.getSuperEntity();
    Set<ClassMetadata> set = subClasses.get(sup);

    if (set == null)
      subClasses.put(sup, set = new HashSet<ClassMetadata>());

    set.add(cm);

    if (log.isDebugEnabled())
      log.debug("Registered " + cm);
  }

  /*
   * inherited javadoc
   */
  public ClassMetadata getClassMetadata(Class<?> clazz) {
    return classmap.get(clazz);
  }

  /*
   * inherited javadoc
   */
  public ClassMetadata getClassMetadata(String entity) {
    return entitymap.get(entity);
  }

  /*
   * inherited javadoc
   */
  public Collection<ClassMetadata> listClassMetadata() {
    return new HashSet<ClassMetadata>(entitymap.values());
  }

  /*
   * inherited javadoc
   */
  public ModelConfig getModel(String modelId) {
    return modelsByName.get(modelId);
  }

  /*
   * inherited javadoc
   */
  public List<ModelConfig> getModels(URI modelType) {
    return modelsByType.get(modelType);
  }

  /*
   * inherited javadoc
   */
  public void addModel(ModelConfig model) {
    modelsByName.put(model.getId(), model);

    List<ModelConfig> models = modelsByType.get(model.getType());
    if (models == null)
      modelsByType.put(model.getType(), models = new ArrayList<ModelConfig>());
    models.add(model);
  }

  /*
   * inherited javadoc
   */
  public void removeModel(ModelConfig model) {
    modelsByName.remove(model.getId());

    List<ModelConfig> models = modelsByType.get(model.getType());
    if (models != null) {
      models.remove(model);
      if (models.size() == 0)
        modelsByType.remove(model.getType());
    }
  }

  /*
   * inherited javadoc
   */
  public TripleStore getTripleStore() {
    return tripleStore;
  }

  /*
   * inherited javadoc
   */
  public void setTripleStore(TripleStore store) {
    this.tripleStore = store;
  }

  /*
   * inherited javadoc
   */
  public BlobStore getBlobStore() {
    return blobStore;
  }

  /*
   * inherited javadoc
   */
  public void setBlobStore(BlobStore store) {
    this.blobStore = store;
  }

  public void setTransactionManager(TransactionManager tm) {
    if (jotm != null) {
      jotm.stop();
      jotm = null;
    }

    this.txMgr = tm;
  }

  public TransactionManager getTransactionManager() throws OtmException {
    if (txMgr == null) {
      try {
        jotm  = new Jotm(true, false);
        txMgr = jotm.getTransactionManager();
      } catch (NamingException ne) {
        throw new OtmException("Failed to create default transaction-manager", ne);
      }
    }

    return txMgr;
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

  public void addAlias(String alias, String replacement) {
    aliases.put(alias, replacement);
  }

  public void removeAlias(String alias) {
    aliases.remove(alias);
  }

  public Map<String, String> listAliases() {
    return new HashMap<String, String>(aliases);
  }

  public String expandAlias(String uri) {
    for (String alias : aliases.keySet()) {
      if (uri.startsWith(alias + ":")) {
        uri = aliases.get(alias) + uri.substring(alias.length() + 1);
        break;
      }
    }
    return uri;
  }


  protected void finalize() throws Throwable {
    try {
      if (jotm != null) {
        jotm.stop();
        jotm = null;
      }
    } finally {
      super.finalize();
    }
  }
}
