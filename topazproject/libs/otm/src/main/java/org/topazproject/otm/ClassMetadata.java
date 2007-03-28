package org.topazproject.otm;

import java.lang.reflect.Field;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.otm.mapping.Mapper;

/**
 * Meta information for mapping a class to a set of triples.
 *
 * @author Pradeep Krishnan
 */
public class ClassMetadata {
  private static final Log    log        = LogFactory.getLog(ClassMetadata.class);
  private Set<String>         types;
  private String              type;
  private String              model;
  private String              ns;
  private Mapper              idField;
  private Map<String, Mapper> fieldMap;
  private Map<String, Mapper> inverseMap;
  private Map<String, Mapper> nameMap;
  private Class               clazz;
  private Collection<Mapper>  fields;

/**
   * Creates a new ClassMetadata object.
   *
   * @param clazz DOCUMENT ME!
   * @param types DOCUMENT ME!
   * @param type DOCUMENT ME!
   * @param model DOCUMENT ME!
   * @param ns DOCUMENT ME!
   * @param idField DOCUMENT ME!
   * @param fields DOCUMENT ME!
   */
  public ClassMetadata(Class clazz, String type, Set<String> types, String model, String ns,
                       Mapper idField, Collection<Mapper> fields)
                throws OtmException {
    this.clazz                           = clazz;
    this.type                            = type;
    this.model                           = model;
    this.ns                              = ns;
    this.idField                         = idField;

    this.types                           = Collections.unmodifiableSet(new HashSet<String>(types));
    this.fields                          = Collections.unmodifiableCollection(new ArrayList<Mapper>(fields));

    fieldMap                             = new HashMap<String, Mapper>();
    inverseMap                           = new HashMap<String, Mapper>();
    nameMap                              = new HashMap<String, Mapper>();

    for (Mapper m : fields) {
      Map<String, Mapper> map = m.hasInverseUri() ? inverseMap : fieldMap;

      if (map.put(m.getUri(), m) != null)
        throw new OtmException("Duplicate Rdf uri for " + m.getField().toGenericString());

      if (nameMap.put(m.getName(), m) != null)
        throw new OtmException("Duplicate field name for " + m.getField().toGenericString());
    }

    fieldMap     = Collections.unmodifiableMap(fieldMap);
    inverseMap   = Collections.unmodifiableMap(inverseMap);
    nameMap      = Collections.unmodifiableMap(nameMap);
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public Class getSourceClass() {
    return clazz;
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public String getModel() {
    return model;
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public String getNs() {
    return ns;
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public String getType() {
    return type;
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public Set<String> getTypes() {
    return types;
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public Collection<Mapper> getFields() {
    return fields;
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public Mapper getIdField() {
    return idField;
  }

  /**
   * Gets a field mapper by its predicate uri.
   *
   * @param uri the predicate uri
   * @param inverse DOCUMENT ME!
   *
   * @return the mapper or null
   */
  public Mapper getMapperByUri(String uri, boolean inverse) {
    return inverse ? inverseMap.get(uri) : fieldMap.get(uri);
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
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public String toString() {
    return clazz.toString();
  }

  /**
   * Tests if this meta-data is for an entity class. Entity classes have an id field, and
   * graph/model
   *
   * @return true for entity type
   */
  public boolean isEntity() {
    return (idField != null) && (model != null);
  }
}
