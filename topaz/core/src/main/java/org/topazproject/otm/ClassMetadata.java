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

/**
 * Meta information for mapping a class to a set of triples.
 *
 * @author Pradeep Krishnan
 */
public class ClassMetadata<T> {
  private static final Log                log = LogFactory.getLog(ClassMetadata.class);

  private final Set<String>               types;
  private final String                    type;
  private final String                    name;
  private final String                    model;
  private final String                    uriPrefix;
  private final Mapper                    idField;
  private final Binder                    blobField;
  private final Map<String, List<Mapper>> uriMap;
  private final Map<String, Mapper>       nameMap;
  private final Class<T>                  clazz;
  private final Collection<Mapper>        fields;
  private final String                    query;
  private final Map<String, List<Mapper>> varMap;

  /**
   * Creates a new ClassMetadata object for an Entity.
   *
   * @param clazz the class 
   * @param name the entity name for use in queries
   * @param type the most specific rdf:type that identify this class
   * @param types set of rdf:type values that identify this class
   * @param model the graph/model where this class is persisted
   * @param uriPrefix the uri-prefix for constructing predicate-uris for fields from their names 
   * @param idField the mapper for the id field
   * @param fields mappers for all persistable fields (includes embedded class fields)
   * @param blobField loader for the blob
   */
  public ClassMetadata(Class<T> clazz, String name, String type, Set<String> types, String model,
                       String uriPrefix, Mapper idField, Collection<Mapper> fields, 
                       Binder blobField)
                throws OtmException {
    this.clazz                                = clazz;
    this.name                                 = name;
    this.query                                = null;
    this.type                                 = type;
    this.model                                = model;
    this.uriPrefix                            = uriPrefix;
    this.idField                              = idField;
    this.blobField                            = blobField;

    this.types                                = Collections.unmodifiableSet(new HashSet<String>(types));
    this.fields                               = Collections.unmodifiableCollection(new ArrayList<Mapper>(fields));

    Map<String, List<Mapper>> uriMap          = new HashMap<String, List<Mapper>>();
    Map<String, Mapper>       nameMap         = new HashMap<String, Mapper>();

    for (Mapper m : fields) {
      List<Mapper> mappers = uriMap.get(m.getUri());

      if (mappers == null)
        uriMap.put(m.getUri(), mappers = new ArrayList<Mapper>());
      else if (isDuplicateMapping(mappers, m))
        throw new OtmException("Duplicate predicate uri for " + m.getName() + " in " + name);

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
   * @param clazz   the class 
   * @param name    the view name (used to look up class-metadata)
   * @param query   the query, or null for SubView's
   * @param idField the mapper for the id field
   * @param fields  mappers for all persistable fields (includes embedded class fields)
   */
  public ClassMetadata(Class<T> clazz, String name, String query, Mapper idField,
                       Collection<Mapper> fields)
                throws OtmException {
    this.clazz     = clazz;
    this.name      = name;
    this.query     = query;
    this.type      = null;
    this.model     = null;
    this.uriPrefix = null;
    this.idField   = idField;
    this.blobField = null;

    this.types     = Collections.emptySet();
    this.fields    = Collections.unmodifiableCollection(new ArrayList<Mapper>(fields));

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

  /**
   * Gets the class that this meta info pertains to.
   *
   * @return the class
   */
  public Class<T> getSourceClass() {
    return clazz;
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
   * Gets the uri-prefix used to build the predicate-uri for all fields in this class without
   * an explicit predicate uri defined.
   *
   * @return the uri prefix
   */
  public String getUriPrefix() {
    return uriPrefix;
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
   * Gets the persistable fields of this class. Includes embedded class fields.
   *
   * @return collection of field mappers.
   */
  public Collection<Mapper> getFields() {
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
    return clazz.toString();
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
}
