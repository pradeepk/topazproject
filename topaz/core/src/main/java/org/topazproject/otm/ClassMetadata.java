/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.otm.mapping.Mapper;
import org.topazproject.otm.mapping.Binder;
import org.topazproject.otm.mapping.EntityBinder;
import org.topazproject.otm.mapping.java.ClassBinder;

/**
 * Meta information for mapping a class to a set of triples.
 *
 * @author Pradeep Krishnan
 */
public class ClassMetadata {
  private static final Log                log = LogFactory.getLog(ClassMetadata.class);

  private final Set<String>               types;
  private final String                    type;
  private final Set<String>               names;
  private final String                    name;
  private final String                    superEntity;
  private final String                    model;
  private final Mapper                    idField;
  private final Binder                    blobField;
  private final Map<String, List<Mapper>> uriMap;
  private final Map<String, Mapper>       nameMap;
  private final EntityBinder              binder;
  private final Collection<Mapper>        fields;
  private final String                    query;
  private final Map<String, List<Mapper>> varMap;

  /**
   * Creates a new ClassMetadata object for an Entity.
   *
   * @param binder the entity binder (XXX: temporary)
   * @param name the entity name for use in queries
   * @param type the most specific rdf:type that identify this class
   * @param types set of rdf:type values that identify this class
   * @param model the graph/model where this class is persisted
   * @param idField the mapper for the id field
   * @param fields mappers for all persistable fields (includes embedded class fields)
   * @param blobField loader for the blob
   * @param superEntity a super class entity for which this is a sub-class of
   */
  public ClassMetadata(EntityBinder binder, String name, String type, Set<String> types, String model,
                       Mapper idField, Collection<Mapper> fields, Binder blobField, String superEntity)
                throws OtmException {
    this.binder                               = binder;
    this.name                                 = name;
    this.query                                = null;
    this.type                                 = type;
    this.model                                = model;
    this.idField                              = idField;
    this.blobField                            = blobField;
    this.superEntity                          = superEntity;

    this.types                                = Collections.unmodifiableSet(new HashSet<String>(types));
    this.fields                               = Collections.unmodifiableCollection(new ArrayList<Mapper>(fields));
    this.names                                = buildNames(name, binder);

    Map<String, List<Mapper>> uriMap          = new HashMap<String, List<Mapper>>();
    Map<String, Mapper>       nameMap         = new HashMap<String, Mapper>();

    for (Mapper m : fields) {
      List<Mapper> mappers = uriMap.get(m.getUri());

      // See ticket #840. The following enforces the current restrictions.
      if (mappers == null)
        uriMap.put(m.getUri(), mappers = new ArrayList<Mapper>());
      else if (isDuplicateMapping(mappers, m))
        throw new OtmException("Duplicate predicate uri for " + m.getName() + " in " + name);
      else if (mappers.get(0).getColType() != m.getColType())
        throw new OtmException("The colType for " + m.getName() + " in " + name + 
            ", must be " + mappers.get(0).getColType() + " as defined by " 
            + mappers.get(0).getName() + " since they both share the same predicate uri");
      else if (!sameModel(mappers.get(0), m, model))
        throw new OtmException("The model for " + m.getName() + " in " + name + 
            ", must be " + mappers.get(0).getModel() + " as defined by " 
            + mappers.get(0).getName() + " since they both share the same predicate uri");

      mappers.add(m);

      if (nameMap.put(m.getName(), m) != null)
        throw new OtmException("Duplicate field name " + m.getName() + " in " + name);
    }

    this.uriMap  = Collections.unmodifiableMap(uriMap);
    this.nameMap = Collections.unmodifiableMap(nameMap);
    this.varMap  = null;
  }

  /**
   * Creates a new ClassMetadata object for a View.
   *
   * @param binder  the entity binder (XXX: temporary) 
   * @param name    the view name (used to look up class-metadata)
   * @param query   the query, or null for SubView's
   * @param idField the mapper for the id field
   * @param fields  mappers for all persistable fields (includes embedded class fields)
   */
  public ClassMetadata(EntityBinder binder, String name, String query, Mapper idField,
                       Collection<Mapper> fields)
                throws OtmException {
    this.binder      = binder;
    this.name        = name;
    this.query       = query;
    this.type        = null;
    this.model       = null;
    this.idField     = idField;
    this.blobField   = null;
    this.superEntity = null;

    this.types       = Collections.emptySet();
    this.fields      = Collections.unmodifiableCollection(new ArrayList<Mapper>(fields));
    this.names       = buildNames(name, binder);

    Map<String, List<Mapper>> varMap  = new HashMap<String, List<Mapper>>();
    Map<String, Mapper>       nameMap = new HashMap<String, Mapper>();
    for (Mapper m : fields) {
      if (nameMap.put(m.getName(), m) != null)
        throw new OtmException("Duplicate field name " + m.getName() + " in view " + name);

      List<Mapper> mappers = varMap.get(m.getProjectionVar());
      if (mappers == null)
        varMap.put(m.getProjectionVar(), mappers = new ArrayList<Mapper>());
      mappers.add(m);
    }

    this.uriMap  = null;
    this.nameMap = Collections.unmodifiableMap(nameMap);
    this.varMap  = Collections.unmodifiableMap(varMap);
  }

  private static Set<String> buildNames(String name, EntityBinder binder) {
    Set<String>               names           = new HashSet<String>();

    names.add(name);
    Collections.addAll(names, binder.getNames());
    return Collections.unmodifiableSet(names);
  }

  /**
   * Gets the binder corresponding to the entity mode.
   *
   * @param mode the entity mode
   *
   * @return the entity binder
   */
  public EntityBinder getEntityBinder(EntityMode mode) {
    return binder;
  }

  /**
   * Gets the binder corresponding to the entity mode.
   *
   * @param session the otm session implementation
   *
   * @return the entity binder
   */
  public EntityBinder getEntityBinder(Session session) {
    return binder;
  }

  /**
   * Gets the graph/model where this class is persisted.
   *
   * @return the model identifier
   */
  public String getModel() {
    return model;
  }

  /**
   * Gets the entity name for this ClassMetadata
   *
   * @return the entity name
   */
  public String getName() {
    return name;
  }

  /**
   * Gets the set of all unique names by which this entity is known.
   *
   * @return set of unique names for this entity
   */
  public Set<String> getNames() {
    return names;
  }

  /**
   * Gets the most specific rdf:type that describes this class.
   *
   * @return the rdf:type uri or null
   */
  public String getType() {
    return type;
  }

  /**
   * Gets the set of rdf:type values that describe this class.
   *
   * @return set of rdf:type uri values or an empty set; null for views
   */
  public Set<String> getTypes() {
    return types;
  }

  /**
   * Gets the entity for which this is a subclass-of.
   *
   * @return the super entity or null
   */
  public String getSuperEntity() {
    return superEntity;
  }

  /**
   * Gets the mappers for all predicates that this ClassMetadata maps. All
   * embedded class fields are flattened and made available as part of this
   * list for convinience.
   *
   * @return collection of field mappers.
   */
  public Collection<Mapper> getMappers() {
    return fields;
  }

  /**
   * Gets the mapper for the id/subject-uri field.
   *
   * @return the id field or null for embeddable classes and views
   */
  public Mapper getIdField() {
    return idField;
  }

  /**
   * Gets the Binder for the blob field.
   *
   * @return the blob field or null
   */
  public Binder getBlobField() {
    return blobField;
  }

  /**
   * Gets a field mapper by its predicate uri.
   *
   * @param uri the predicate uri
   * @param inverse if the mapping is reverse (ie. o,p,s instead of s,p,o))
   * @param type for associations the rdf:type or null
   *
   * @return the mapper or null
   */
  public Mapper getMapperByUri(String uri, boolean inverse, String type) {
    List<Mapper> mappers = uriMap.get(uri);

    if (mappers == null)
      return null;

    Mapper candidate = null;

    for (Mapper m : mappers) {
      if (m.hasInverseUri() != inverse)
        continue;

      String rt = m.getRdfType();

      if ((type == null) || type.equals(rt))
        return m;

      if ((rt == null) && (candidate == null))
        candidate = m;
    }

    return candidate;
  }

  /**
   * Gets a field mapper by its predicate uri.
   *
   * @param sf the session factory for looking up associations
   * @param uri the predicate uri
   * @param inverse if the mapping is reverse (ie. o,p,s instead of s,p,o))
   * @param typeUris Collection of rdf:Type values
   *
   * @return the mapper or null
   */
  public Mapper getMapperByUri(SessionFactory sf, String uri, boolean inverse,
                               Collection<String> typeUris) {
    if ((typeUris == null) || (typeUris.size() == 0))
      return getMapperByUri(uri, inverse, null);

    if (typeUris.size() == 1)
      return getMapperByUri(uri, inverse, typeUris.iterator().next());

    List<Mapper> mappers = uriMap.get(uri);

    if (mappers == null)
      return null;

    Set<String> uris      = new HashSet<String>(typeUris);
    Mapper      candidate = null;

    for (Mapper m : mappers) {
      if (m.hasInverseUri() != inverse)
        continue;

      String rt = m.getRdfType();

      if ((rt == null) && (candidate == null))
        candidate = m;

      if (uris.contains(rt)) {
        uris.removeAll(sf.getClassMetadata(m.getAssociatedEntity()).getTypes());
        candidate = m;
      }
    }

    return candidate;
  }

  /**
   * Gets a field mapper by its field name.
   *
   * @param name the field name.
   *
   * @return the mapper or null
   */
  public Mapper getMapperByName(String name) {
    return nameMap.get(name);
  }

  /**
   * Gets the field mappers for a projection variable (for Views).
   *
   * @param var the projection variable name.
   * @return the mappers or null
   */
  public List<Mapper> getMappersByVar(String var) {
    List<Mapper> mappers = varMap.get(var);
    return (mappers != null) ? new ArrayList<Mapper>(mappers) : null;
  }

  /** 
   * Get the OQL query string if this is for a View.
   * 
   * @return the OQL query string, or null if this is not a View
   */
  public String getQuery() {
    return query;
  }

  /*
   * inherited javadoc
   */
  public String toString() {
    return "ClassMetadata[name=" + getName() + ", type=" + getType() + "]";
  }

  /**
   * Tests if this metadata is for a persistable class. To be persistable, the class must
   * have an id field. It must also have a model or if a model is missing then there should not
   * be any persistable fields or rdf types and must have a blob field.
   *
   * @return true only if this class can be persisted
   */
  public boolean isPersistable() {
    if (idField == null)
      return false;
    if (model != null)
      return true;
    return ((types.size() + fields.size()) == 0) && (blobField != null);
  }

  /**
   * Tests if this meta-data is for a view class.
   *
   * @return true for view type
   */
  public boolean isView() {
    return (query != null);
  }

  private static boolean isDuplicateMapping(List<Mapper> mappers, Mapper o) {
    for (Mapper m : mappers) {
      if (m.hasInverseUri() != o.hasInverseUri())
        continue;

      if (m.getRdfType() != null) {
        if (m.getRdfType().equals(o.getRdfType()))
          return true;
      } else if (o.getRdfType() == null)
        return true;
    }

    return false;
  }

  private static boolean sameModel(Mapper m1, Mapper m2, String model) {
    String g1 = m1.getModel();
    String g2 = m2.getModel();

    if ((g1 == null) || "".equals(g1))
      g1 = model;

    if ((g2 == null) || "".equals(g2))
      g2 = model;

    return (g1 != null) ? g1.equals(g2) : (g2 == null);
  }

  public boolean isAssignableFrom(ClassMetadata other) {
    // XXX: temporary
    return ((ClassBinder)binder).getSourceClass().isAssignableFrom(((ClassBinder)other.binder).getSourceClass());
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof ClassMetadata))
      return false;

    ClassMetadata o = (ClassMetadata) other;

    return name.equals(name);
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }
}
