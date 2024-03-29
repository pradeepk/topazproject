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

import org.topazproject.otm.mapping.BlobMapper;
import org.topazproject.otm.mapping.EmbeddedMapper;
import org.topazproject.otm.mapping.IdMapper;
import org.topazproject.otm.mapping.Mapper;
import org.topazproject.otm.mapping.RdfMapper;
import org.topazproject.otm.mapping.VarMapper;
import org.topazproject.otm.mapping.EntityBinder;

/**
 * Meta information for mapping a class to a set of triples.
 *
 * @author Pradeep Krishnan
 */
public class ClassMetadata {
  private static final Log                log = LogFactory.getLog(ClassMetadata.class);

  private final Set<String>               allTypes;
  private final Set<String>               types;
  private final String                    name;
  private final Set<String>               superEntities;
  private final String                    graph;
  private final IdMapper                  idField;
  private final BlobMapper                blobField;
  private final Map<String, List<RdfMapper>> uriMap;
  private final Map<String, Mapper>       nameMap;
  private final Map<EntityMode, EntityBinder>              binders;
  private final Collection<RdfMapper>     rdfFields;
  private final Collection<VarMapper>     varFields;
  private final Collection<EmbeddedMapper> embeds;
  private final String                    query;
  private final Map<String, List<VarMapper>> varMap;

  /**
   * Creates a new ClassMetadata object for an Entity.
   *
   * @param binders the entity binders
   * @param name the entity name for use in queries
   * @param types the most specific rdf:type that identify this class
   * @param allTypes set of rdf:type values that identify this class
   * @param graph the graph where this class is persisted
   * @param idField the mapper for the id field
   * @param fields mappers for all persistable fields (includes embedded class fields)
   * @param blobField loader for the blob
   * @param superEntities super class entities for which this is a sub-class of
   * @param embeds fields that are embeds
   */
  public ClassMetadata(Map<EntityMode, EntityBinder> binders, String name, Set<String> types, Set<String> allTypes,
                       String graph, IdMapper idField, Collection<RdfMapper> fields, BlobMapper blobField,
                       Set<String> superEntities, Collection<EmbeddedMapper> embeds)
                throws OtmException {
    this.binders                              = binders;
    this.name                                 = name;
    this.query                                = null;
    this.graph                                = graph;
    this.idField                              = idField;
    this.blobField                            = blobField;

    this.superEntities                        = Collections.unmodifiableSet(new HashSet<String>(superEntities));
    this.types                                = Collections.unmodifiableSet(new HashSet<String>(types));
    this.allTypes                             = Collections.unmodifiableSet(new HashSet<String>(allTypes));
    this.embeds                               = Collections.unmodifiableCollection(new ArrayList<EmbeddedMapper>(embeds));
    this.rdfFields                            = Collections.unmodifiableCollection(new ArrayList<RdfMapper>(fields));
    this.varFields                            = null;

    Map<String, List<RdfMapper>> uriMap       = new HashMap<String, List<RdfMapper>>();
    Map<String, Mapper>       nameMap         = new HashMap<String, Mapper>();

    for (RdfMapper m : fields) {
      List<RdfMapper> rdfMappers = uriMap.get(m.getUri());

      // See ticket #840. The following enforces the current restrictions.
      if (rdfMappers == null)
        uriMap.put(m.getUri(), rdfMappers = new ArrayList<RdfMapper>());
      else if (isDuplicateMapping(rdfMappers, m))
        throw new OtmException("Duplicate predicate uri for " + m.getName() + " in " + name);
      else if (rdfMappers.get(0).getColType() != m.getColType())
        throw new OtmException("The colType for " + m.getName() + " in " + name +
            ", must be " + rdfMappers.get(0).getColType() + " as defined by "
            + rdfMappers.get(0).getName() + " since they both share the same predicate uri");
      else if (!sameGraph(rdfMappers.get(0), m, graph))
        throw new OtmException("The graph for " + m.getName() + " in " + name +
            ", must be " + rdfMappers.get(0).getGraph() + " as defined by "
            + rdfMappers.get(0).getName() + " since they both share the same predicate uri");

      rdfMappers.add(m);

      if (nameMap.put(m.getName(), m) != null)
        throw new OtmException("Duplicate field name " + m.getName() + " in " + name);
    }

    for (EmbeddedMapper m : embeds) {
      if (nameMap.put(m.getName(), m) != null)
        throw new OtmException("Duplicate field name " + m.getName() + " in " + name);
    }

    this.uriMap  = Collections.unmodifiableMap(uriMap);
    this.nameMap = Collections.unmodifiableMap(nameMap);
    this.varMap  = null;

    // Notify binders
    for (EntityBinder b : binders.values())
      b.bindComplete(this);
  }

  /**
   * Creates a new ClassMetadata object for a View.
   *
   * @param binders the entity binders
   * @param name    the view name (used to look up class-metadata)
   * @param query   the query, or null for SubView's
   * @param idField the mapper for the id field
   * @param fields  mappers for all persistable fields (includes embedded class fields)
   */
  public ClassMetadata(Map<EntityMode, EntityBinder> binders, String name, String query, IdMapper idField,
                       Collection<VarMapper> fields)
                throws OtmException {
    this.binders       = binders;
    this.name          = name;
    this.query         = query;
    this.graph         = null;
    this.idField       = idField;
    this.blobField     = null;

    this.superEntities = Collections.emptySet();
    this.types         = Collections.emptySet();
    this.allTypes      = Collections.emptySet();
    this.varFields     = Collections.unmodifiableCollection(new ArrayList<VarMapper>(fields));
    this.rdfFields     = null;
    this.embeds        = null;

    Map<String, List<VarMapper>> varMap  = new HashMap<String, List<VarMapper>>();
    Map<String, Mapper>       nameMap = new HashMap<String, Mapper>();
    for (VarMapper m : fields) {
      if (nameMap.put(m.getName(), m) != null)
        throw new OtmException("Duplicate field name " + m.getName() + " in view " + name);

      List<VarMapper> varMappers = varMap.get(m.getProjectionVar());
      if (varMappers == null)
        varMap.put(m.getProjectionVar(), varMappers = new ArrayList<VarMapper>());
      varMappers.add(m);
    }

    this.uriMap  = null;
    this.nameMap = Collections.unmodifiableMap(nameMap);
    this.varMap  = Collections.unmodifiableMap(varMap);

    // Notify binders
    for (EntityBinder b : binders.values())
      b.bindComplete(this);
  }

  private static Set<String> buildNames(String name, Collection<EntityBinder> binders) {
    Set<String>               names           = new HashSet<String>();

    names.add(name);

    for (EntityBinder binder : binders)
      Collections.addAll(names, binder.getNames());

    return names;
  }

  /**
   * Gets the binder corresponding to the entity mode.
   *
   * @param mode the entity mode
   *
   * @return the entity binder
   */
  public EntityBinder getEntityBinder(EntityMode mode) {
    return binders.get(mode);
  }

  /**
   * Gets the binder corresponding to the entity mode.
   *
   * @param session the otm session implementation
   *
   * @return the entity binder
   */
  public EntityBinder getEntityBinder(Session session) {
    return binders.get(session.getEntityMode());
  }

  /**
   * Gets the graph where this class is persisted.
   *
   * @return the graph identifier
   */
  public String getGraph() {
    return graph;
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
    return buildNames(name, binders.values());
  }

  /**
   * Gets the most specific rdf:type that describes this class.
   *
   * @return the set of rdf:type values. Could be an empty set.
   *
   * @see #getAllTypes
   */
  public Set<String> getTypes() {
    return types;
  }

  /**
   * Gets the set of all rdf:type values that describe this class including all types
   * that are inherited from the super-classes.
   *
   * @return set of rdf:type values. Could be an empty set.
   *
   * @see #getTypes
   * @see #getSuperEntities
   */
  public Set<String> getAllTypes() {
    return allTypes;
  }

  /**
   * Gets the set of entities that are declared as super-classes for this entity.
   *
   * @return the super entity or null
   */
  public Set<String> getSuperEntities() {
    return superEntities;
  }

  /**
   * Gets the mappers for all predicates that this ClassMetadata maps. All
   * embedded class fields are flattened and made available as part of this
   * list for convenience.
   *
   * @return collection of field mappers.
   */
  public Collection<RdfMapper> getRdfMappers() {
    return rdfFields;
  }

  /**
   * Gets the mappers for all projection variables in a view.
   *
   * @return collection of field mappers.
   */
  public Collection<VarMapper> getVarMappers() {
    return varFields;
  }

  /**
   * Gets the mappers for all embedded variables in a view.
   *
   * @return collection of embedded mappers.
   */
  public Collection<EmbeddedMapper> getEmbeddedMappers() {
    return embeds;
  }

  /**
   * Gets the mapper for the id/subject-uri field.
   *
   * @return the id field or null for embeddable classes and views
   */
  public IdMapper getIdField() {
    return idField;
  }

  /**
   * Gets the PropertyBinder for the blob field.
   *
   * @return the blob field or null
   */
  public BlobMapper getBlobField() {
    return blobField;
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
  public RdfMapper getMapperByUri(SessionFactory sf, String uri, boolean inverse,
                               Collection<String> typeUris) {
    List<RdfMapper> rdfMappers = uriMap.get(uri);

    if (rdfMappers == null)
      return null;

    Collection<String> uris = typeUris;
    RdfMapper   candidate = null;

    for (RdfMapper m : rdfMappers) {
      if (m.hasInverseUri() != inverse)
        continue;

      if (candidate == null)
        candidate = m;

      if ((uris == null) || uris.isEmpty())
        break;

      ClassMetadata assoc = !m.isAssociation() ? null
                            : sf.getClassMetadata(m.getAssociatedEntity());

      if ((assoc == null) || assoc.getTypes().isEmpty())
        continue;

      if (uris.containsAll(assoc.getTypes())) {
        if (uris == typeUris)
          uris = new HashSet<String>(typeUris); // lazy copy

        uris.removeAll(assoc.getAllTypes());
        candidate = m;
      }
    }

    return candidate;
  }

  /**
   * Gets the field mappers for a predicate-uri.
   *
   * @param uri the uri
   * @param inverse the inverse flag
   * @return the mappers or an empty list
   */
  public List<RdfMapper> getMappersByUri(String uri, boolean inverse) {
    List<RdfMapper> l       = new ArrayList<RdfMapper>();
    List<RdfMapper> mappers = uriMap.get(uri);

    if (mappers != null)
      for (RdfMapper m : mappers)
        if (m.hasInverseUri() == inverse)
          l.add(m);

    return l;
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
  public List<VarMapper> getMappersByVar(String var) {
    List<VarMapper> mappers = varMap.get(var);
    return (mappers != null) ? new ArrayList<VarMapper>(mappers) : null;
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
    return "ClassMetadata[name=" + getName() + ", type=" + getTypes() + "]";
  }

  /**
   * Tests if this metadata is for a persistable class. To be persistable, the class must
   * have an id field. It must also have a graph or if a graph is missing then there should not
   * be any persistable fields or rdf types and must have a blob field.
   *
   * @return true only if this class can be persisted
   */
  public boolean isPersistable() {
    if ((idField == null) || (rdfFields == null))
      return false;
    if (graph != null)
      return true;
    return ((allTypes.size() + rdfFields.size()) == 0) && (blobField != null);
  }

  /**
   * Tests if this meta-data is for a view class.
   *
   * @return true for view type
   */
  public boolean isView() {
    return (query != null);
  }

  private static boolean isDuplicateMapping(List<RdfMapper> rdfMappers, RdfMapper o) {
    for (RdfMapper m : rdfMappers) {
      if (m.hasInverseUri() != o.hasInverseUri())
        continue;

      if (!m.isAssociation() || !o.isAssociation())
        return true;

      if (m.getAssociatedEntity().equals(o.getAssociatedEntity()))
        return true;
    }

    return false;
  }

  private static boolean sameGraph(RdfMapper m1, RdfMapper m2, String graph) {
    String g1 = m1.getGraph();
    String g2 = m2.getGraph();

    if ((g1 == null) || "".equals(g1))
      g1 = graph;

    if ((g2 == null) || "".equals(g2))
      g2 = graph;

    return (g1 != null) ? g1.equals(g2) : (g2 == null);
  }

  /**
   * Tests to see if instances of this ClassMetadata is assignment compatible from
   * instances of another. If an EntityMode is passed in, assignment compatibility
   * in that mode is also checked.
   *
   * @param other the other ClassMetadata
   * @param mode the entity mode or null
   *
   * @return true if instances of this is assignable from instances of the other
   */
  public boolean isAssignableFrom(ClassMetadata other, EntityMode mode) {
    if (!other.allTypes.containsAll(this.allTypes))
      return false;

    if (mode != null) {
      EntityBinder thisBinder = getEntityBinder(mode);
      EntityBinder otherBinder = other.getEntityBinder(mode);
      if (!thisBinder.isAssignableFrom(otherBinder))
        return false;
    }
    return true;
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof ClassMetadata))
      return false;

    ClassMetadata o = (ClassMetadata) other;

    return name.equals(o.name);
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }
}
