/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm.metadata;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import java.net.URI;
import java.net.URL;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.annotations.BaseUri;
import org.topazproject.otm.annotations.Embeddable;
import org.topazproject.otm.annotations.Embedded;
import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.Predicate;
import org.topazproject.otm.annotations.PredicateMap;
import org.topazproject.otm.annotations.Rdf;
import org.topazproject.otm.mapping.ArrayMapper;
import org.topazproject.otm.mapping.CollectionMapper;
import org.topazproject.otm.mapping.EmbeddedClassFieldMapper;
import org.topazproject.otm.mapping.EmbeddedClassMapper;
import org.topazproject.otm.mapping.FunctionalMapper;
import org.topazproject.otm.mapping.Mapper;
import org.topazproject.otm.mapping.PredicateMapMapper;
import org.topazproject.otm.mapping.Serializer;
import org.topazproject.otm.mapping.SerializerFactory;

/**
 * Meta information for mapping a class to a set of triples.
 *
 * @author Pradeep Krishnan
 */
public class AnnotationClassMetaFactory {
  private static final Log log = LogFactory.getLog(AnnotationClassMetaFactory.class);
  private SessionFactory   sf;

/**
   * Creates a new AnnotationClassMetaFactory object.
   *
   * @param sf the session factory
   */
  public AnnotationClassMetaFactory(SessionFactory sf) {
    this.sf                    = sf;
  }

  /**
   * Creates a new ClassMetadata object.
   *
   * @param clazz the class with annotations
   *
   * @return a newly created ClassMetadata object
   *
   * @throws OtmException on an error
   */
  public ClassMetadata create(Class clazz) throws OtmException {
    return create(clazz, clazz, null, new HashMap<Class, ClassMetadata>());
  }

  private ClassMetadata create(Class clazz, Class top, String baseUriOfContainingClass,
                               Map<Class, ClassMetadata> loopDetect)
                        throws OtmException {
    Set<String>        types   = Collections.emptySet();
    String             type    = null;
    String             model   = null;
    String             baseUri = null;
    Mapper             idField = null;
    Collection<Mapper> fields  = new ArrayList<Mapper>();

    Class              s       = clazz.getSuperclass();

    if (!Object.class.equals(s) && (s != null)) {
      ClassMetadata superMeta = create(s, top, baseUriOfContainingClass, loopDetect);
      model     = superMeta.getModel();
      baseUri   = superMeta.getBaseUri();
      type      = superMeta.getType();
      types     = superMeta.getTypes();
      idField   = superMeta.getIdField();
      fields.addAll(superMeta.getFields());
    }

    Entity entity = (Entity) clazz.getAnnotation(Entity.class);

    if ((entity != null) && !"".equals(entity.model()))
      model = entity.model();

    BaseUri baseUriAnn = (BaseUri) clazz.getAnnotation(BaseUri.class);

    if (baseUriAnn != null)
      baseUri = baseUriAnn.value();

    if (baseUri == null)
      baseUri = baseUriOfContainingClass;

    if ((entity != null) && !"".equals(entity.type())) {
      type    = entity.type();
      types   = new HashSet<String>(types);

      if (!types.add(type))
        throw new OtmException("Duplicate rdf:type in class hierarchy " + clazz);
    }

    ClassMetadata cm = new ClassMetadata(clazz, type, types, model, baseUri, idField, fields);
    loopDetect.put(clazz, cm);

    for (Field f : clazz.getDeclaredFields()) {
      Collection<?extends Mapper> mappers = createMapper(f, top, baseUri, loopDetect);

      if (mappers == null)
        continue;

      for (Mapper m : mappers) {
        String uri = m.getUri();

        if ((uri != null) || (m.getMapperType() == Mapper.MapperType.PREDICATE_MAP))
          fields.add(m);
        else {
          if (idField != null)
            throw new OtmException("Duplicate @Id field " + f.toGenericString());

          idField = m;
        }
      }
    }

    cm = new ClassMetadata(clazz, type, types, model, baseUri, idField, fields);
    loopDetect.put(clazz, cm);

    return cm;
  }

  /**
   * Create an appropriate mapper for the given java field.
   *
   * @param f the field
   * @param top the class where this field belongs (may not be the declaring class)
   * @param ns the rdf name space or null to use to build an rdf predicate uri
   * @param loopDetect to avoid loops in following associations
   *
   * @return a mapper or null if the field is static or transient
   *
   * @throws OtmException if a mapper can't be created
   */
  public Collection<?extends Mapper> createMapper(Field f, Class top, String ns,
                                                  Map<Class, ClassMetadata> loopDetect)
                                           throws OtmException {
    Class type = f.getType();
    int   mod  = f.getModifiers();

    if (Modifier.isStatic(mod) || Modifier.isTransient(mod)) {
      if (log.isDebugEnabled())
        log.debug(f.toGenericString() + " will not be persisted.");

      return null;
    }

    if (Modifier.isFinal(mod))
      throw new OtmException("'final' field '" + f.toGenericString() + "' can't be persisted.");

    String n = f.getName();
    n = n.substring(0, 1).toUpperCase() + n.substring(1);

    // polymorphic getter/setter is unusable for private fields
    Class  invokedOn =
      (Modifier.isProtected(mod) || Modifier.isPublic(mod)) ? top : f.getDeclaringClass();

    Method getMethod;
    Method setMethod;

    try {
      getMethod      = invokedOn.getMethod("get" + n);
    } catch (NoSuchMethodException e) {
      getMethod = null;
    }

    try {
      setMethod = invokedOn.getMethod("set" + n, type);
    } catch (NoSuchMethodException e) {
      setMethod = null;
    }

    if (((getMethod == null) || (setMethod == null)) && !Modifier.isPublic(mod))
      throw new OtmException("The field '" + f.toGenericString()
                             + "' is inaccessible and can't be persisted.");

    // xxx: The above get/set determination is based on 'preload' polymorphic info.
    // xxx: Does not account for run time sub classing. 
    // xxx: But then again, that is 'bad coding style' anyway.  
    // xxx: So exception thrown above in those cases is somewhat justifiable.
    //
    Predicate rdf      = (Predicate) f.getAnnotation(Predicate.class);
    Id        id       = (Id) f.getAnnotation(Id.class);
    boolean   embedded =
      (f.getAnnotation(Embedded.class) != null) || (type.getAnnotation(Embeddable.class) != null);

    String    uri      =
      ((rdf != null) && !"".equals(rdf.uri())) ? rdf.uri() : ((ns != null) ? (ns + f.getName()) : null);

    if (id != null) {
      if (!type.equals(String.class) && !type.equals(URI.class) && !type.equals(URL.class))
        throw new OtmException("@Id field '" + f.toGenericString()
                               + "' must be a String, URI or URL.");

      Serializer serializer = sf.getSerializerFactory().getSerializer(type, null);

      return Collections.singletonList(new FunctionalMapper(null, f, getMethod, setMethod,
                                                            serializer, null, false, null,
                                                            Mapper.MapperType.PREDICATE));
    }

    if (f.getAnnotation(PredicateMap.class) != null)
      return Collections.singletonList(createPredicateMap(f, getMethod, setMethod));

    if (!embedded && (uri == null))
      throw new OtmException("Missing @Rdf for field '" + f.toGenericString() + "' in "
                             + f.getDeclaringClass());

    boolean isArray      = type.isArray();
    boolean isCollection = Collection.class.isAssignableFrom(type);

    type                 = isArray ? type.getComponentType() : (isCollection ? collectionType(f)
                                                                : type);

    String dt            =
      ((rdf == null) || "".equals(rdf.dataType()))
      ? sf.getSerializerFactory().getDefaultDataType(type) : rdf.dataType();

    if (Predicate.UNTYPED.equals(dt))
      dt = null;

    Serializer serializer = sf.getSerializerFactory().getSerializer(type, dt);

    if (log.isDebugEnabled() && (serializer == null))
      log.debug("No serializer found for " + type);

    if (!embedded) {
      boolean inverse      = (rdf != null) && rdf.inverse();
      String  inverseModel = null;

      if (inverse) {
        if (serializer != null) {
          if (!"".equals(rdf.model()))
            inverseModel = rdf.model();
        } else {
          ClassMetadata cm = loopDetect.get(type);

          if (cm == null)
            cm = create(type, type, null, loopDetect);

          inverseModel = cm.getModel();
        }
      }

      Mapper.MapperType mt = getMapperType(f, rdf, isArray);
      Mapper            p;

      if (isArray)
        p = new ArrayMapper(uri, f, getMethod, setMethod, serializer, type, dt, inverse,
                            inverseModel, mt);
      else if (isCollection)
        p = new CollectionMapper(uri, f, getMethod, setMethod, serializer, type, dt, inverse,
                                 inverseModel, mt);
      else
        p = new FunctionalMapper(uri, f, getMethod, setMethod, serializer, dt, inverse,
                                 inverseModel, mt);

      return Collections.singletonList(p);
    }

    if (isArray || isCollection || (serializer != null))
      throw new OtmException("@Embedded class field '" + f.toGenericString()
                             + "' can't be an array, collection or a simple field");

    ClassMetadata cm;

    try {
      cm                   = create(type, type, ns, loopDetect);
    } catch (OtmException e) {
      throw new OtmException("Could not generate metadata for @Embedded class field '"
                             + f.toGenericString() + "'", e);
    }

    // xxx: this is a restriction on this API. revisit to allow this case too.
    if (cm.getType() != null)
      throw new OtmException("@Embedded class '" + type + "' embedded at '" + f.toGenericString()
                             + "' should not declare an rdf:type of its own. (fix me)");

    EmbeddedClassMapper ecp     = new EmbeddedClassMapper(f, getMethod, setMethod);

    Collection<Mapper>  mappers = new ArrayList<Mapper>();

    for (Mapper p : cm.getFields())
      mappers.add(new EmbeddedClassFieldMapper(ecp, p));

    Mapper p = cm.getIdField();

    if (p != null)
      mappers.add(new EmbeddedClassFieldMapper(ecp, p));

    return mappers;
  }

  /**
   * Gets the member type for a collection.
   *
   * @param field the filed
   *
   * @return the member type
   */
  public static Class collectionType(Field field) {
    Type  type   = field.getGenericType();
    Class result = Object.class;

    if (type instanceof ParameterizedType) {
      ParameterizedType ptype = (ParameterizedType) type;
      Type[]            targs = ptype.getActualTypeArguments();

      if (targs.length > 0)
        result = (Class) targs[0];
    }

    return result;
  }

  private Mapper.MapperType getMapperType(Field f, Predicate rdf, boolean isArray)
                                   throws OtmException {
    Predicate.StoreAs storeAs = (rdf == null) ? Predicate.StoreAs.undefined : rdf.storeAs();

    switch (storeAs) {
    case rdfList:
      return Mapper.MapperType.RDFLIST;

    case rdfBag:
      return Mapper.MapperType.RDFBAG;

    case rdfAlt:
      return Mapper.MapperType.RDFALT;

    case rdfSeq:
      return Mapper.MapperType.RDFSEQ;

    case predicate:
      return Mapper.MapperType.PREDICATE;

    case undefined:

      //if (isArray || List.class.isAssignableFrom(field.getType()))
      //  return Mapper.MapperType.RDFSEQ;
      return Mapper.MapperType.PREDICATE;
    }

    return Mapper.MapperType.PREDICATE;
  }

  private Mapper createPredicateMap(Field field, Method getter, Method setter)
                             throws OtmException {
    Type type = field.getGenericType();

    if (Map.class.isAssignableFrom(field.getType()) && (type instanceof ParameterizedType)) {
      ParameterizedType ptype = (ParameterizedType) type;
      Type[]            targs = ptype.getActualTypeArguments();

      if ((targs.length == 2) && (targs[0] instanceof Class)
           && String.class.isAssignableFrom((Class) targs[0])
           && (targs[1] instanceof ParameterizedType)) {
        ptype   = (ParameterizedType) targs[1];
        type    = ptype.getRawType();

        if ((type instanceof Class) && (List.class.isAssignableFrom((Class) type))) {
          targs = ptype.getActualTypeArguments();

          if ((targs.length == 1) && (targs[0] instanceof Class)
               && String.class.isAssignableFrom((Class) targs[0]))
            return new PredicateMapMapper(field, getter, setter);
        }
      }
    }

    throw new OtmException("@PredicateMap can be applied to a Map<String, List<String>> field only."
                           + "It cannot be applied to '" + field.toGenericString() + "'");
  }
}
