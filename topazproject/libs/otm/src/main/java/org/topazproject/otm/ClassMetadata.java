package org.topazproject.otm;

import java.lang.reflect.Field;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.otm.annotations.Model;
import org.topazproject.otm.annotations.Ns;
import org.topazproject.otm.annotations.Rdf;
import org.topazproject.otm.mapping.Mapper;
import org.topazproject.otm.mapping.MapperFactory;

/**
 * Meta information for mapping a class to a set of triples.
 *
 * @author Pradeep Krishnan
 */
public class ClassMetadata {
  private static final Log    log      = LogFactory.getLog(ClassMetadata.class);
  private Set<String>         types    = Collections.emptySet();
  private String              type     = null;
  private String              model    = null;
  private String              ns       = null;
  private Mapper              idField  = null;
  private Map<String, Mapper> fieldMap = new HashMap<String, Mapper>();
  private Map<String, Mapper> inverseMap = new HashMap<String, Mapper>();
  private Map<String, Mapper> nameMap  = new HashMap<String, Mapper>();
  private Class               clazz;
  private Collection<Mapper>  fields;

/**
   * Creates a new ClassMetadata object.
   *
   * @param clazz DOCUMENT ME!
   */
  public ClassMetadata(Class clazz) throws OtmException {
    this(clazz, null);
  }

/**
   * Creates a new ClassMetadata object.
   *
   * @param clazz DOCUMENT ME!
   * @param nsOfContainingClass DOCUMENT ME!
   */
  public ClassMetadata(Class clazz, String nsOfContainingClass) throws OtmException {
    this(clazz, clazz, nsOfContainingClass);
  }

  private ClassMetadata(Class clazz, Class top, String nsOfContainingClass) throws OtmException {
    this.clazz                         = clazz;

    Class         s                    = clazz.getSuperclass();
    ClassMetadata superMeta            = null;

    if (!Object.class.equals(s) && (s != null)) {
      try {
        superMeta   = new ClassMetadata(s, top, nsOfContainingClass);
        model       = superMeta.getModel();
        ns          = superMeta.getNs();
        type        = superMeta.getType();
        types       = superMeta.getTypes();
        idField     = superMeta.getIdField();

        for (Mapper m : superMeta.getFields()) {
          if (m.hasInverseUri())
            inverseMap.put(m.getUri(), m);
          else
            fieldMap.put(m.getUri(), m);
          nameMap.put(m.getName(), m);
        }
      } catch (OtmException e) {
        if (log.isDebugEnabled())
          log.debug("super class meta couldn't be created.", e);
      }
    }

    Model modelAnn = (Model) clazz.getAnnotation(Model.class);

    if (modelAnn != null)
      model = modelAnn.value();

    Ns nsAnn = (Ns) clazz.getAnnotation(Ns.class);

    if (nsAnn != null)
      ns = nsAnn.value();

    if (ns == null)
      ns = nsOfContainingClass;

    Rdf rdfAnn = (Rdf) clazz.getAnnotation(Rdf.class);

    if (rdfAnn != null) {
      type    = rdfAnn.value();
      types   = new HashSet<String>(types);

      if (!types.add(type))
        throw new OtmException("Duplicate rdf:type in class hierarchy " + clazz);

      types = Collections.unmodifiableSet(types);
    }

    for (Field f : clazz.getDeclaredFields()) {
      Collection<?extends Mapper> mappers = MapperFactory.create(f, top, ns);

      if (mappers == null)
        continue;

      for (Mapper m : mappers) {
        String uri = m.getUri();

        if (uri == null) {
          if (idField != null)
            throw new OtmException("Duplicate @Id field " + f.toGenericString());

          idField = m;
        } else {
          Map<String, Mapper> map = m.hasInverseUri() ? inverseMap : fieldMap;
          if (map.put(uri, m) != null)
            throw new OtmException("Duplicate @Rdf uri for " + f.toGenericString());

          nameMap.put(m.getName(), m);
        }
      }
    }

    fields   = Collections.unmodifiableCollection(nameMap.values());
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
   * Tests if this meta-data is for an entity class. Entity classes have an id field,
   * and graph/model
   *
   * @return true for entity type
   */
  public boolean isEntity() {
    return (idField != null) && (model != null);
  }
}
