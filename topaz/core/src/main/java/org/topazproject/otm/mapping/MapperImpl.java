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

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import java.net.URI;
import java.net.URL;

import java.util.List;

import org.topazproject.otm.OtmException;
import org.topazproject.otm.Rdf;
import org.topazproject.otm.id.IdentifierGenerator;
import org.topazproject.otm.serializer.Serializer;

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
  private final MapperType          mapperType;
  private final boolean             entityOwned;
  private final IdentifierGenerator generator;
  private final CascadeType[]       cascade;
  private final FetchType           fetchType;
  private final Loader              loader;

  /**
   * Creates a new MapperImpl object for a regular class.
   *
   * @param uri the rdf predicate
   * @param loader the loader for the field
   * @param dataType of literals or null for un-typed
   * @param rdfType of associations or null for un-typed
   * @param inverse if this field is persisted with an inverse predicate
   * @param model  the model where this field is persisted
   * @param mapperType the mapper type of this field
   * @param entityOwned if the triples for this field is owned by the containing entity
   * @param generator if there is a generator for this field
   * @param cascade cascade options for this field
   * @param fetchType fetch type for associations. Must be null otherwise
   */
  public MapperImpl(String uri, Loader loader, String dataType,
                        String rdfType, boolean inverse, String model, MapperType mapperType,
                        boolean entityOwned, IdentifierGenerator generator, 
                        CascadeType[] cascade, FetchType fetchType) {
    this.uri             = uri;
    this.var             = null;
    this.loader          = loader;
    this.dataType        = dataType;
    this.rdfType         = rdfType;
    this.inverse         = inverse;
    this.model           = model;
    this.mapperType      = mapperType;
    this.entityOwned     = entityOwned;
    this.generator       = generator;
    this.cascade         = cascade;
    this.fetchType       = fetchType;
  }

  /**
   * Creates a new MapperImpl object for a view.
   *
   * @param var           the projection variable
   * @param loader        the loader for the field
   * @param fetchType     fetch type for associations. Must be null otherwise
   */
  public MapperImpl(String var, Loader loader, FetchType fetchType) {
    this.uri             = null;
    this.var             = var;
    this.loader          = loader;
    this.dataType        = null;
    this.rdfType         = null;
    this.inverse         = false;
    this.model           = null;
    this.mapperType      = null;
    this.entityOwned     = false;
    this.generator       = null;
    this.cascade         = null;
    this.fetchType       = fetchType;
  }

  /**
   * Creates a new MapperImpl with a different loader.
   *
   * @param other         the MapperImpl to copy from
   * @param loader        the loader for the field
   */
  public MapperImpl(Mapper other, Loader loader) {
    this.uri             = other.getUri();
    this.var             = other.getProjectionVar();
    this.loader          = loader;
    this.dataType        = other.getDataType();
    this.rdfType         = other.getRdfType();
    this.inverse         = other.hasInverseUri();
    this.model           = other.getModel();
    this.mapperType      = other.getMapperType();
    this.entityOwned     = other.isEntityOwned();
    this.generator       = other.getGenerator();
    this.cascade         = other.getCascade();
    this.fetchType       = other.getFetchType();
  }

  /*
   * inherited javadoc
   */
  public Loader getLoader() {
    return loader;
  }

  /*
   * inherited javadoc
   */
  public String getName() {
    return loader.getName();
  }

  /*
   * inherited javadoc
   */
  public List get(Object o) throws OtmException {
    return loader.get(o);
  }

  /*
   * inherited javadoc
   */
  public void set(Object o, List vals) throws OtmException {
    loader.set(o, vals);
  }

  /*
   * inherited javadoc
   */
  public Object getRawValue(Object o, boolean create) throws OtmException {
    return loader.getRawValue(o, create);
  }

  /*
   * inherited javadoc
   */
  public void setRawValue(Object o, Object value) throws OtmException {
    loader.setRawValue(o, value);
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
    return loader.typeIsUri(this);
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
    return fetchType != null;
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
  public MapperType getMapperType() {
    return mapperType;
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
           + ", mapperType=" + mapperType + ", inverse=" + inverse
           + ", loader=" + loader + ", fetchType=" + fetchType
           + ", cascade=" + cascade
           + ", generator=" + ((generator != null) ? generator.getClass() : "-null-") + "]";
  }

  public Class getComponentType() {
    return ((org.topazproject.otm.mapping.java.FieldLoader)loader).getComponentType();
  }
}
