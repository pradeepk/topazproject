/* $HeadURL::                                                                            $
 * $Rdf: ClassMetadata.java 4960 2008-03-12 17:13:54Z pradeep $
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
package org.topazproject.otm.metadata;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.topazproject.otm.CascadeType;
import org.topazproject.otm.CollectionType;
import org.topazproject.otm.FetchType;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.id.IdentifierGenerator;

/**
 * The definition for an Rdf property.
 *
 * @author Pradeep Krishnan
 */
public class RdfDefinition extends PropertyDefinition {
  /**
   * To specify un-typed literal fields in configuration only.
   */
  public static final String UNTYPED = "__untyped__";
  private String              uri;
  private Boolean             inverse;
  private Boolean             objectProperty;
  private String              model;
  private String              dataType;
  private CollectionType      colType;
  private Boolean             entityOwned;
  private Boolean             predicateMap;
  private Set<CascadeType>    cascade;
  private FetchType           fetchType;
  private String              associatedEntity;
  private IdentifierGenerator generator;

  /**
   * Creates a new RdfDefinition object.
   *
   * @param name   The name of this definition.
   * @param reference The definition to refer to resolve undefined attribiutes or null.
   * @param uri the rdf predicate. Null to resolve from reference
   * @param dataType of literals or null for un-typed or to resolve from reference
   * @param inverse if this field is persisted with an inverse predicate. Null to resolve from reference
   * @param model  the model where this field is persisted or null to resolve from reference
   * @param colType the collection type of this field or null to resolve from reference
   * @param entityOwned if the triples for this field is owned by the containing entity. 
   *                    Null to resolve from reference.
   * @param generator if there is a generator for this field. Null to resolve from reference (if any)
   * @param cascade cascade options for this field. Null to resolve from reference.
   * @param fetchType fetch type for associations. Null to resolve from reference.
   * @param associatedEntity the entity name for associations. Null to resolve from reference.
   * @param objectProperty if this is an object property. Null to resolve from reference.
   */
  public RdfDefinition(String name, String reference, String uri, String dataType, Boolean inverse,
                       String model, CollectionType colType, Boolean entityOwned,
                       IdentifierGenerator generator, CascadeType[] cascade, FetchType fetchType,
                       String associatedEntity, Boolean objectProperty) {
    super(name, reference);

    // Normalize undefines
    if (colType == CollectionType.UNDEFINED)
      colType = (reference == null) ? CollectionType.PREDICATE : null;

    if ((cascade != null) && (cascade.length == 1) && (cascade[0] == CascadeType.undefined))
      cascade = (reference == null) ? new CascadeType[] { CascadeType.peer } : null;

    if (fetchType == FetchType.undefined)
      fetchType = (reference == null) ? FetchType.lazy : null;

    this.uri                = uri;
    this.dataType           = dataType;
    this.inverse            = inverse;
    this.model              = model;
    this.colType            = colType;
    this.entityOwned        = entityOwned;
    this.generator          = generator;
    this.cascade            = (cascade == null) ? null
                              : Collections.unmodifiableSet(new HashSet<CascadeType>(Arrays.asList(cascade)));
    this.fetchType          = fetchType;
    this.associatedEntity   = associatedEntity;
    this.predicateMap       = false;
    this.objectProperty     = objectProperty;
  }

  /**
   * Creates a new RdfDefinition object for a predicate-map.
   *
   * @param name   The name of this definition.
   * @param model  the model where this field is persisted
   */
  public RdfDefinition(String name, String model) {
    super(name);
    this.uri                = null;
    this.dataType           = null;
    this.inverse            = false;
    this.model              = model;
    this.colType            = null;
    this.entityOwned        = true;
    this.generator          = null;
    this.cascade            = null;
    this.fetchType          = null;
    this.associatedEntity   = null;
    this.predicateMap       = true;
    this.objectProperty     = false;
  }

  /*
   * inherited javadoc
   */
  @Override
  protected void resolve(SessionFactory sf, Definition def)
                  throws OtmException {
    if (def != null) {
      if (!(def instanceof RdfDefinition))
        throw new OtmException("Reference '" + def.getName() + "' in '" + getName()
                               + "' is not an RDF property definition");

      RdfDefinition ref = (RdfDefinition) def;

      if (ref.predicateMap != predicateMap)
        throw new OtmException("Reference '" + ref.getName() + "' in '" + getName()
                               + "' cannot be used since there is a mismatch in the predicate-map attribute");
      assert ref.getName().equals(getReference()); // this should be right. But just in case

      uri                = (uri == null) ? ref.uri : uri;
      inverse            = (inverse == null) ? ref.inverse : inverse;
      model              = (model == null) ? ref.model : model;
      dataType           = (dataType == null) ? ref.dataType : dataType;
      colType            = (colType == null) ? ref.colType : colType;
      associatedEntity   = (associatedEntity == null) ? ref.associatedEntity : associatedEntity;
      objectProperty     = (objectProperty == null) ? ref.objectProperty : objectProperty;
      entityOwned        = (entityOwned == null) ? ref.entityOwned : entityOwned;
      generator          = (generator == null) ? ref.generator : generator;
      cascade            = (cascade == null) ? ref.cascade : cascade;
      fetchType          = (fetchType == null) ? ref.fetchType : fetchType;
    }

    if (objectProperty == null)
      objectProperty = (associatedEntity != null) || inverse;

    if (objectProperty)
      dataType = null;

    // validate stuff

    /* Disabled till we split the resolve() as a second pass
    if ((associatedEntity != null)
         && !(sf.getDefinition(associatedEntity) instanceof EntityDefinition))
      throw new OtmException("Undefined association '" + associatedEntity + "' in '" + getName()
                             + "'");
    */
  }

  /**
   * Tests if this definition refers to the same set of graph nodes as another.
   *
   * @param o the defenition to compare
   *
   * @return true if the predicate-uri, the graph and the inverse mapping is the same.
   */
  public boolean refersSameGraphNodes(RdfDefinition o) {
    return same(uri, o.uri) && same(model, o.model) && same(inverse, o.inverse);
  }

  /**
   * Tests if this definition has the same rdfs:range as another.
   *
   * @param o the definition to compare
   *
   * @return true if the rdfs:range matches
   */
  public boolean refersSameRange(RdfDefinition o) {
    return same(dataType, o.dataType) && same(colType, o.colType)
            && same(objectProperty, o.objectProperty);
     // TODO: Add equivalence comparison on associated entities rather than name compare
     //       && same(associatedEntity, o.associatedEntity);
  }

  private boolean same(Object o1, Object o2) {
    return (o1 == null) ? (o2 == null) : o1.equals(o2);
  }

  /*
   * inherited javadoc
   */
  public String getUri() {
    return uri;
  }

  /*
   * inherited javadoc
   */
  public boolean typeIsUri() {
    return objectProperty;
  }

  /*
   * inherited javadoc
   */
  public String getDataType() {
    return dataType;
  }

  /*
   * inherited javadoc
   */
  public boolean isAssociation() {
    return associatedEntity != null;
  }

  /*
   * inherited javadoc
   */
  public boolean isPredicateMap() {
    return predicateMap;
  }

  /*
   * inherited javadoc
   */
  public boolean hasInverseUri() {
    return inverse;
  }

  /*
   * inherited javadoc
   */
  public String getModel() {
    return model;
  }

  /*
   * inherited javadoc
   */
  public CollectionType getColType() {
    return colType;
  }

  /*
   * inherited javadoc
   */
  public boolean isEntityOwned() {
    return entityOwned;
  }

  /*
   * inherited javadoc
   */
  public Set<CascadeType> getCascade() {
    return cascade;
  }

  /*
   * inherited javadoc
   */
  public boolean isCascadable(CascadeType op) {
    for (CascadeType ct : cascade)
      if (ct.implies(op))
        return true;

    return false;
  }

  /*
   * inherited javadoc
   */
  public FetchType getFetchType() {
    return fetchType;
  }

  /*
   * inherited javadoc
   */
  public IdentifierGenerator getGenerator() {
    return generator;
  }

  /*
   * inherited javadoc
   */
  public String getAssociatedEntity() {
    return associatedEntity;
  }

  /*
   * inherited javadoc
   */
  public String toString() {
    return getClass().getName() + "[pred=" + uri + ", dataType=" + dataType + ", colType="
           + colType + ", inverse=" + inverse + ", fetchType=" + fetchType + ", cascade=" + cascade
           + ", generator=" + ((generator != null) ? generator.getClass() : "-null-")
           + ", association=" + associatedEntity + ", objectProperty=" + objectProperty
           + ", model=" + model + ",entityOwned=" + entityOwned + "]";
  }
}
