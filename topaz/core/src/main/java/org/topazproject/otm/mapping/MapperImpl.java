/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm.mapping;

import org.topazproject.otm.CascadeType;
import org.topazproject.otm.CollectionType;
import org.topazproject.otm.EntityMode;
import org.topazproject.otm.FetchType;
import org.topazproject.otm.Session;
import org.topazproject.otm.id.IdentifierGenerator;

/**
 * A convenient base class for all mappers.
 *
 * @author Pradeep krishnan
 */
public class MapperImpl implements Mapper {
  private final String              uri;
  private final String              var;
  private final boolean             inverse;
  private final String              model;
  private final String              dataType;
  private final String              rdfType;
  private final CollectionType      colType;
  private final boolean             entityOwned;
  private final boolean             predicateMap;
  private final IdentifierGenerator generator;
  private final CascadeType[]       cascade;
  private final FetchType           fetchType;
  private final Binder              binder;
  private final String              associatedEntity;


  /**
   * Creates a new MapperImpl object for a regular class field.
   *
   * @param uri the rdf predicate
   * @param binder the binder for the field
   * @param dataType of literals or null for un-typed
   * @param rdfType of associations or null for un-typed
   * @param inverse if this field is persisted with an inverse predicate
   * @param model  the model where this field is persisted
   * @param colType the collection type of this field
   * @param entityOwned if the triples for this field is owned by the containing entity
   * @param generator if there is a generator for this field
   * @param cascade cascade options for this field
   * @param fetchType fetch type for associations. Must be null otherwise
   * @param associatedEntity the entity name for associations
   */
  public MapperImpl(String uri, Binder binder, String dataType,
                        String rdfType, boolean inverse, String model, CollectionType colType,
                        boolean entityOwned, IdentifierGenerator generator, 
                        CascadeType[] cascade, FetchType fetchType, String associatedEntity) {
    this.uri             = uri;
    this.var             = null;
    this.binder          = binder;
    this.dataType        = dataType;
    this.rdfType         = rdfType;
    this.inverse         = inverse;
    this.model           = model;
    this.colType         = colType;
    this.entityOwned     = entityOwned;
    this.generator       = generator;
    this.cascade         = cascade;
    this.fetchType       = fetchType;
    this.associatedEntity = associatedEntity;
    this.predicateMap    = false;
  }

  /**
   * Creates a new MapperImpl object for a predicat-map field.
   *
   * @param binder the binder for the field
   * @param model  the model where this field is persisted
   */
  public MapperImpl(Binder binder, String model) {
    this.uri             = null;
    this.var             = null;
    this.binder          = binder;
    this.dataType        = null;
    this.rdfType         = null;
    this.inverse         = false;
    this.model           = model;
    this.colType         = null;
    this.entityOwned     = true;
    this.generator       = null;
    this.cascade         = null;
    this.fetchType       = null;
    this.associatedEntity = null;
    this.predicateMap    = true;
  }

  /**
   * Creates a new MapperImpl object for an id field.
   *
   * @param binder the binder for the field
   * @param generator if there is a generator for this field
   */
  public MapperImpl(Binder binder, IdentifierGenerator generator) {
    this.uri             = null;
    this.var             = null;
    this.binder          = binder;
    this.dataType        = null;
    this.rdfType         = null;
    this.inverse         = false;
    this.model           = null;
    this.colType         = null;
    this.entityOwned     = true;
    this.generator       = generator;
    this.cascade         = null;
    this.fetchType       = null;
    this.associatedEntity = null;
    this.predicateMap    = false;
  }

  /**
   * Creates a new MapperImpl object for a view.
   *
   * @param var           the projection variable
   * @param binder        the binder for the field
   * @param fetchType     fetch type for associations. Must be null otherwise
   * @param associatedEntity the entity name for associations
   */
  public MapperImpl(String var, Binder binder, FetchType fetchType, String associatedEntity) {
    this.uri             = null;
    this.var             = var;
    this.binder          = binder;
    this.dataType        = null;
    this.rdfType         = null;
    this.inverse         = false;
    this.model           = null;
    this.colType         = null;
    this.entityOwned     = false;
    this.generator       = null;
    this.cascade         = null;
    this.fetchType       = fetchType;
    this.associatedEntity = associatedEntity;
    this.predicateMap    = false;
  }

  /**
   * Creates a new MapperImpl with a different binder.
   *
   * @param other         the MapperImpl to copy from
   * @param binder        the binder for the field
   */
  public MapperImpl(Mapper other, Binder binder) {
    this.uri             = other.getUri();
    this.var             = other.getProjectionVar();
    this.binder          = binder;
    this.dataType        = other.getDataType();
    this.rdfType         = other.getRdfType();
    this.inverse         = other.hasInverseUri();
    this.model           = other.getModel();
    this.colType         = other.getColType();
    this.entityOwned     = other.isEntityOwned();
    this.generator       = other.getGenerator();
    this.cascade         = other.getCascade();
    this.fetchType       = other.getFetchType();
    this.associatedEntity = other.getAssociatedEntity();
    this.predicateMap    = other.isPredicateMap();
  }

  /*
   * inherited javadoc
   */
  public Binder getBinder(Session session) {
    return binder;
  }

  /*
   * inherited javadoc
   */
  public Binder getBinder(EntityMode mode) {
    return binder;
  }

  /*
   * inherited javadoc
   */
  public String getName() {
    return binder.getName();
  }

  /*
   * inherited javadoc
   */
  public String getUri() {
    return uri;
  }

  public String getProjectionVar() {
    return var;
  }

  /*
   * inherited javadoc
   */
  public boolean typeIsUri() {
    return binder.typeIsUri(this);
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
  public String getRdfType() {
    return rdfType;
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
  public CascadeType[] getCascade() {
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
  public String toString() {
    return getClass().getName() + "[pred=" + uri + ", var=" + var
           + ", dataType=" + dataType + ", rdfType=" + rdfType
           + ", colType=" + colType + ", inverse=" + inverse
           + ", binder=" + binder + ", fetchType=" + fetchType
           + ", cascade=" + cascade
           + ", generator=" + ((generator != null) ? generator.getClass() : "-null-") + "]";
  }

  public String getAssociatedEntity() {
    return associatedEntity;
  }
}
