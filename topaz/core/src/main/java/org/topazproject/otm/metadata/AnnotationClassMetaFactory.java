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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.otm.CascadeType;
import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.CollectionType;
import org.topazproject.otm.EntityMode;
import org.topazproject.otm.FetchType;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Rdf;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.annotations.Alias;
import org.topazproject.otm.annotations.Aliases;
import org.topazproject.otm.annotations.Blob;
import org.topazproject.otm.annotations.Embeddable;
import org.topazproject.otm.annotations.Embedded;
import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.GeneratedValue;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.Predicate;
import org.topazproject.otm.annotations.PredicateMap;
import org.topazproject.otm.annotations.Projection;
import org.topazproject.otm.annotations.SubView;
import org.topazproject.otm.annotations.UriPrefix;
import org.topazproject.otm.annotations.View;
import org.topazproject.otm.id.IdentifierGenerator;
import org.topazproject.otm.mapping.Binder;
import org.topazproject.otm.mapping.Mapper;
import org.topazproject.otm.mapping.MapperImpl;
import org.topazproject.otm.mapping.java.ArrayFieldBinder;
import org.topazproject.otm.mapping.java.ClassBinder;
import org.topazproject.otm.mapping.java.CollectionFieldBinder;
import org.topazproject.otm.mapping.java.EmbeddedClassFieldBinder;
import org.topazproject.otm.mapping.java.EmbeddedClassMemberFieldBinder;
import org.topazproject.otm.mapping.java.FieldBinder;
import org.topazproject.otm.mapping.java.ScalarFieldBinder;
import org.topazproject.otm.serializer.Serializer;

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
    this.sf = sf;
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
    if (clazz.getAnnotation(View.class) != null || clazz.getAnnotation(SubView.class) != null)
      return createView(clazz);
    else
      return create(clazz, clazz, null);
  }

  private ClassMetadata create(Class<?> clazz, Class<?> top,
                                      String uriPrefixOfContainingClass)
                        throws OtmException {
    Set<String>        types     = Collections.emptySet();
    String             type      = null;
    String             model     = null;
    String             uriPrefix = null;
    Mapper             idField   = null;
    Binder             blobField = null;
    String             superEntity = null;
    Collection<Mapper> fields    = new ArrayList<Mapper>();

    Class<?>           s         = clazz.getSuperclass();

    if (log.isDebugEnabled())
      log.debug("Creating class-meta for " + clazz);

    if (!Object.class.equals(s) && (s != null)) {
      ClassMetadata superMeta = create(s, top, uriPrefixOfContainingClass);
      model       = superMeta.getModel();
      type        = superMeta.getType();
      types       = superMeta.getTypes();
      idField     = superMeta.getIdField();
      blobField   = superMeta.getBlobField();
      fields.addAll(superMeta.getFields());
      superEntity = superMeta.getName();
    }

    Aliases aliases = clazz.getAnnotation(Aliases.class);
    if (aliases != null) {
      for (Alias a : aliases.value())
        sf.addAlias(a.alias(), a.value());
    }
    aliases = (clazz.getPackage() != null) ? clazz.getPackage().getAnnotation(Aliases.class) : null;
    if (aliases != null) {
      for (Alias a : aliases.value())
        sf.addAlias(a.alias(), a.value());
    }

    Entity entity = clazz.getAnnotation(Entity.class);

    if ((entity != null) && !"".equals(entity.model()))
      model = entity.model();

    UriPrefix uriPrefixAnn = clazz.getAnnotation(UriPrefix.class);

    if (uriPrefixAnn != null)
      uriPrefix = sf.expandAlias(uriPrefixAnn.value());

    if (uriPrefix == null)
      uriPrefix = uriPrefixOfContainingClass;

    if ((entity != null) && !"".equals(entity.type())) {
      type    = sf.expandAlias(entity.type());
      types   = new HashSet<String>(types);

      if (!types.add(type))
        throw new OtmException("Duplicate rdf:type in class hierarchy " + clazz);
    }

    String name = getEntityName(clazz);

    for (Field f : clazz.getDeclaredFields()) {
      Collection<?extends Mapper> mappers = createMapper(f, clazz, top, uriPrefix, false);

      if (mappers == null)
        continue;

      for (Mapper m : mappers) {
        FieldBinder l = (FieldBinder)m.getBinder(EntityMode.POJO);
        Field f2 = l.getField();
        Id id = f2.getAnnotation(Id.class);
        Blob blob = f2.getAnnotation(Blob.class);
        if ((id == null) && (blob == null))
          fields.add(m);
        else if (id != null) {
          if (idField != null)
            if (f.equals(f2))
              throw new OtmException("Duplicate @Id field " + toString(f));
             else
               throw new OtmException("Duplicate @Id field " + toString(f2) 
                   + " embedded from " + toString(f));
          idField = m;
        } else {
          if (blobField != null)
            if (f.equals(f2))
              throw new OtmException("Duplicate @Blob field " + toString(f));
             else
               throw new OtmException("Duplicate @Blob field " + toString(f2) 
                   + " embedded from " + toString(f));
          blobField = l;
        }
      }
    }

    ClassBinder binder = createBinder(clazz, idField);
    return new ClassMetadata(binder, name, type, types, model, idField, fields, blobField, superEntity);
  }

 private ClassBinder createBinder(Class<?> clazz, Mapper idField) {
   Method getter;

   if (idField == null)
     getter = null;
   else
     getter = ((FieldBinder)idField.getBinder(EntityMode.POJO)).getGetter();

   return new ClassBinder(clazz, (getter == null) ? new Method[0] : new Method[]{getter});
 }

  private  ClassMetadata createView(Class<?> clazz) throws OtmException {
    String name;
    String query = null;

    View view = clazz.getAnnotation(View.class);
    if (view != null) {
      name  = !"".equals(view.name()) ? view.name() : getName(clazz);
      query = view.query();
    } else {
      SubView sv = clazz.getAnnotation(SubView.class);
      name = !"".equals(sv.name()) ? sv.name() : getName(clazz);
    }

    Mapper             idField = null;
    Collection<Mapper> fields  = new ArrayList<Mapper>();

    for (Field f : clazz.getDeclaredFields()) {
      Collection<? extends Mapper> mappers = createMapper(f, clazz, clazz, null, true);
      if (mappers != null) {
        for (Mapper m : mappers) {
          String var = m.getProjectionVar();
          if (var != null)
            fields.add(m);
          else {
            if (idField != null)
              throw new OtmException("Duplicate @Id field " + toString(f));

            idField = m;
          }
        }
      }
    }

    if (view != null && idField == null)
      throw new OtmException("Missing @Id field in " + clazz.getName());

    ClassBinder binder = createBinder(clazz, idField);
    return new ClassMetadata(binder, name, query, idField, fields);
  }

  /**
   * Create an appropriate mapper for the given java field.
   *
   * @param f the field
   * @param clazz the declaring class
   * @param top the class where this field belongs (may not be the declaring class)
   * @param ns the rdf name space or null to use to build an rdf predicate uri
   *
   * @return a mapper or null if the field is static or transient
   *
   * @throws OtmException if a mapper can't be created
   */
  public Collection<?extends Mapper> createMapper(Field f, Class<?> clazz, Class<?> top, String ns,
                                                  boolean isView)
                                           throws OtmException {
    Class<?> type = f.getType();
    int      mod  = f.getModifiers();

    if (Modifier.isStatic(mod) || (!isView && Modifier.isTransient(mod))) {
      if (log.isDebugEnabled())
        log.debug(toString(f) + " will not be persisted.");

      return null;
    }

    if (Modifier.isFinal(mod))
      throw new OtmException("'final' field " + toString(f) + " can't be persisted.");

    String n = f.getName();
    n = n.substring(0, 1).toUpperCase() + n.substring(1);

    // polymorphic getter/setter is unusable for private fields
    Class<?> invokedOn =
      (Modifier.isProtected(mod) || Modifier.isPublic(mod)) ? top : f.getDeclaringClass();

    Method getMethod;
    Method setMethod;

    try {
      getMethod = invokedOn.getMethod("get" + n);
    } catch (NoSuchMethodException e) {
      getMethod = null;
    }

    setMethod = getSetter(invokedOn, "set" + n, type);

    if (((getMethod == null && !isView) || (setMethod == null)) && !Modifier.isPublic(mod))
      throw new OtmException("The field " + toString(f)
                             + " is inaccessible and can't be persisted.");

    // xxx: The above get/set determination is based on 'preload' polymorphic info.
    // xxx: Does not account for run time sub classing. 
    // xxx: But then again, that is 'bad coding style' anyway.  
    // xxx: So exception thrown above in those cases is somewhat justifiable.
    //
    Predicate  rdf      = f.getAnnotation(Predicate.class);
    Id         id       = f.getAnnotation(Id.class);
    Blob       blob     = f.getAnnotation(Blob.class);
    boolean    embedded =
      (f.getAnnotation(Embedded.class) != null) || (type.getAnnotation(Embeddable.class) != null);
    Projection proj     = f.getAnnotation(Projection.class);

    if (blob != null) {
      if (isView)
        throw new OtmException("@Blob not supported in views: " + toString(f));
      if (rdf != null)
        throw new OtmException("@Predicate and @Blob both cannot be applied to a field: " + toString(f));
      if (id != null)
        throw new OtmException("@Id and @Blob both cannot be applied to a field: " + toString(f));

      if (!type.isArray() || !type.getComponentType().equals(Byte.TYPE))
        throw new OtmException("@Blob may only be applied to a 'byte[]' field: " + toString(f));
      if (embedded)
        throw new OtmException("@Embedded and @Blob both cannot be applied to a field: " + toString(f));
      FieldBinder loader = new ArrayFieldBinder(f, getMethod, setMethod, null, Byte.TYPE);
      Mapper p = new MapperImpl(null, loader, null, null);

      return Collections.singletonList(p);
    }

    if (isView && proj == null && id == null)
      return null;
    if (isView && (rdf != null || embedded))
      throw new OtmException("Only @Projection and @Id are supported on fields in a View; class=" +
                             clazz.getName() + ", field=" + f.getName());
    if (!isView && proj != null)
      throw new OtmException("@Projection is only supported on fields in a View; class=" +
                             clazz.getName() + ", field=" + f.getName());

    String     uri      =
      ((rdf != null) && !"".equals(rdf.uri())) ? sf.expandAlias(rdf.uri()) :
                                                 ((ns != null) ? (ns + f.getName()) : null);
    String     var      =
      ((proj != null) && !"".equals(proj.value())) ? proj.value() : f.getName();

    // See if there is an @GeneratedValue annotation... create a generator and set parameter(s)
    IdentifierGenerator generator = null;
    GeneratedValue      gv        = f.getAnnotation(GeneratedValue.class);

    if (gv != null) {
      try {
        generator = (IdentifierGenerator) Thread.currentThread().getContextClassLoader()
                                                 .loadClass(gv.generatorClass()).newInstance();
      } catch (Throwable t) {
        // Between Class.forName() and newInstance() there are a half-dozen possible excps
        throw new OtmException("Unable to find implementation of '" + gv.generatorClass()
                               + "' generator for " + toString(f), t);
      }

      String uriPrefix = sf.expandAlias(gv.uriPrefix());

      if (uriPrefix.equals("")) {
        // Compute default uriPrefix: Rdf.topaz/clazz/generatorClass#
        uriPrefix = Rdf.topaz + clazz.getName() + '/' + f.getName() + '#';
      }

      try {
        // Validate that we have a valid uri
        URI.create(uriPrefix);
      } catch (IllegalArgumentException iae) {
        throw new OtmException("Illegal uriPrefix '" + gv.uriPrefix() + "' for "
                               + toString(f), iae);
      }

      generator.setUriPrefix(uriPrefix);
    }

    if (id != null) {
      if (!type.equals(String.class) && !type.equals(URI.class) && !type.equals(URL.class))
        throw new OtmException("@Id field '" + toString(f)
                               + "' must be a String, URI or URL.");

      Serializer serializer = sf.getSerializerFactory().getSerializer(type, null);

      Mapper     p;
      ScalarFieldBinder loader = new ScalarFieldBinder(f, getMethod, setMethod, serializer);
      if (isView)
        p = new MapperImpl(null, loader, null, null);
      else
        p = new MapperImpl(loader, generator);

      return Collections.singletonList(p);
    }

    if (f.getAnnotation(PredicateMap.class) != null)
      return Collections.singletonList(createPredicateMap(f, getMethod, setMethod));

    if (!embedded && (uri == null) && !isView)
      throw new OtmException("Missing @Predicate for field " + toString(f));

    boolean isArray      = type.isArray();
    boolean isCollection = Collection.class.isAssignableFrom(type);

    type                 = isArray ? type.getComponentType() : (isCollection ? collectionType(f)
                                                                : type);

    String dt            =
      ((rdf == null) || "".equals(rdf.dataType()))
      ? sf.getSerializerFactory().getDefaultDataType(type) : sf.expandAlias(rdf.dataType());

    if (Predicate.UNTYPED.equals(dt))
      dt = null;

    Serializer serializer = sf.getSerializerFactory().getSerializer(type, dt);

    if ((serializer == null) && sf.getSerializerFactory().mustSerialize(type))
      throw new OtmException("No serializer found for '" + type + "' with dataType '" 
          + dt + "' for field " + toString(f));

    if (!embedded) {
      boolean inverse = (rdf != null) && rdf.inverse();
      String  model   = ((rdf != null) && !"".equals(rdf.model())) ? rdf.model() : null;

      String  rt      = (serializer == null) ? getRdfType(type) : null;
      String  assoc   = (serializer == null) ? getEntityName(type) : null;

      if (inverse && (model == null) && (serializer == null))
        model = getModel(type);

      boolean           notOwned = (rdf != null) && rdf.notOwned();

      CollectionType mt       = getColType(f, rdf, isArray);
      CascadeType ct[] = (rdf != null) ? rdf.cascade()
                                   : new CascadeType[]{CascadeType.all};
      FetchType ft = (rdf != null) ? rdf.fetch() : FetchType.lazy;

      if (serializer != null)
        ft = null;
      else if (isView)
        ft = proj.fetch();

      FieldBinder loader;
      if (isArray)
        loader = new ArrayFieldBinder(f, getMethod, setMethod, serializer, type);
      else if (isCollection)
        loader = new CollectionFieldBinder(f, getMethod, setMethod, serializer, type);
      else
        loader = new ScalarFieldBinder(f, getMethod, setMethod, serializer);
      Mapper            p;
      if (isView)
        p = new MapperImpl(var, loader, ft, assoc);
      else
        p = new MapperImpl(uri, loader, dt, rt, inverse, model, mt, !notOwned, generator, ct, ft, assoc);

      return Collections.singletonList(p);
    }

    if (isArray || isCollection || (serializer != null))
      throw new OtmException("@Embedded class field " + toString(f)
                             + " can't be an array, collection or a simple field");

    ClassMetadata cm;
    try {
      cm = create(type, type, ns);
    } catch (OtmException e) {
      throw new OtmException("Could not generate metadata for @Embedded class field "
                             + toString(f), e);
    }

    // xxx: this is a restriction on this API. revisit to allow this case too.
    if (cm.getType() != null)
      throw new OtmException("@Embedded class '" + type + "' embedded at " + toString(f)
                             + " should not declare an rdf:type of its own. (fix me)");

    EmbeddedClassFieldBinder ecp     = new EmbeddedClassFieldBinder(f, getMethod, setMethod);

    Collection<Mapper>  mappers = new ArrayList<Mapper>();

    for (Mapper p : cm.getFields())
      mappers.add(new MapperImpl(p, new EmbeddedClassMemberFieldBinder(ecp, (FieldBinder)p.getBinder(EntityMode.POJO))));

    Mapper p = cm.getIdField();

    if (p != null)
      mappers.add(new MapperImpl(p, new EmbeddedClassMemberFieldBinder(ecp, (FieldBinder)p.getBinder(EntityMode.POJO))));

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

      if ((targs.length > 0) && (targs[0] instanceof Class))
        result = (Class) targs[0];
    }

    return result;
  }

  private CollectionType getColType(Field f, Predicate rdf, boolean isArray)
                                   throws OtmException {
    return (rdf == null) ? CollectionType.PREDICATE : rdf.collectionType();
  }

  private Mapper createPredicateMap(Field field, Method getter, Method setter)
                             throws OtmException {
    String model = null; // TODO: allow predicate maps from other models
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
            return new MapperImpl(new ScalarFieldBinder(field, getter, setter, null), model);
        }
      }
    }

    throw new OtmException("@PredicateMap can be applied to a Map<String, List<String>> field only."
                           + "It cannot be applied to " + toString(field));
  }

  private static String getName(Class<?> clazz) {
    String  name = clazz.getName();
    Package p    = clazz.getPackage();

    if (p != null)
      name = name.substring(p.getName().length() + 1);

    return name;
  }

  private static Method getSetter(Class<?> invokedOn, String name, Class<?> type) {
    for (Class<?> t = type; t != null; t = t.getSuperclass()) {
      try {
        return invokedOn.getMethod(name, t);
      } catch (NoSuchMethodException e) {
      }
    }

    for (Class<?> t : type.getInterfaces()) {
      try {
        return invokedOn.getMethod(name, t);
      } catch (NoSuchMethodException e) {
      }
    }

    return null;
  }

  private String getRdfType(Class<?> clazz) {
    if (clazz == null)
      return null;

    Entity entity = clazz.getAnnotation(Entity.class);

    if ((entity != null) && !"".equals(entity.type()))
      return sf.expandAlias(entity.type());

    return getRdfType(clazz.getSuperclass());
  }

  private static String getModel(Class<?> clazz) {
    if (clazz == null)
      return null;

    Entity entity = clazz.getAnnotation(Entity.class);

    if ((entity != null) && !"".equals(entity.model()))
      return entity.model();

    return getModel(clazz.getSuperclass());
  }

  private static String getEntityName(Class<?> clazz) {
    if (clazz == null)
      return null;

    Entity entity = clazz.getAnnotation(Entity.class);

    if ((entity != null) && !"".equals(entity.name()))
      return entity.name();

    return getName(clazz);
  }

  private static String toString(Field f) {
    return "'" + f.getName() + "' in " + f.getDeclaringClass();
  }
}
