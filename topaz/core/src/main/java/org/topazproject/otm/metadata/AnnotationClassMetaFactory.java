/* $HeadURL:: http://gandalf.topazproject.org/svn/head/topaz/core/src/main/java/org/topa#$
 * $Id: AnnotationClassMetaFactory.java 6329 2008-08-14 17:46:24Z pradeep $
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

import java.beans.BeanInfo;
import java.beans.IndexedPropertyDescriptor;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import java.net.URI;
import java.net.URL;

import java.util.Collection;
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
import org.topazproject.otm.annotations.Embedded;
import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.GeneratedValue;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.Predicate;
import org.topazproject.otm.annotations.Predicate.PropType;
import org.topazproject.otm.annotations.PredicateMap;
import org.topazproject.otm.annotations.Projection;
import org.topazproject.otm.annotations.SubView;
import org.topazproject.otm.annotations.UriPrefix;
import org.topazproject.otm.annotations.View;
import org.topazproject.otm.id.IdentifierGenerator;
import org.topazproject.otm.mapping.java.ArrayFieldBinder;
import org.topazproject.otm.mapping.java.ClassBinder;
import org.topazproject.otm.mapping.java.CollectionFieldBinder;
import org.topazproject.otm.mapping.java.EmbeddedClassFieldBinder;
import org.topazproject.otm.mapping.java.FieldBinder;
import org.topazproject.otm.mapping.java.ScalarFieldBinder;
import org.topazproject.otm.serializer.Serializer;

/**
 * A factory that processes annotations on a class and creates ClassMetadata for it.
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
    addAliases(clazz);

    if ((clazz.getAnnotation(View.class) != null) || (clazz.getAnnotation(SubView.class) != null))
      return createView(clazz);

    return createEntity(clazz);
  }

  private void addAliases(Class<?> clazz) throws OtmException {
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
  }

  private ClassMetadata createEntity(Class<?> clazz) throws OtmException {
    String type      = null;
    String model     = null;
    String uriPrefix = null;
    String sup       = null;

    if (log.isDebugEnabled())
      log.debug("Creating class-meta for " + clazz);

    Entity entity = clazz.getAnnotation(Entity.class);

    if (entity != null) {
      if (!"".equals(entity.model()))
        model = entity.model();

      if (!"".equals(entity.type()))
        type = sf.expandAlias(entity.type());
    }

    String   name = getEntityName(clazz);
    Class<?> s    = clazz.getSuperclass();

    if ((s != null) && !s.equals(Object.class))
      sup = getEntityName(s);

    EntityDefinition ed           = new EntityDefinition(name, type, model, sup);
    UriPrefix        uriPrefixAnn = clazz.getAnnotation(UriPrefix.class);

    if (uriPrefixAnn != null)
      uriPrefix = sf.expandAlias(uriPrefixAnn.value());

    return createMeta(ed, clazz, uriPrefix);
  }

  private ClassMetadata createView(Class<?> clazz) throws OtmException {
    String name;
    String query = null;

    View   view  = clazz.getAnnotation(View.class);

    if (view != null) {
      name    = (!"".equals(view.name())) ? view.name() : getName(clazz);
      query   = view.query();
    } else {
      SubView sv = clazz.getAnnotation(SubView.class);
      name = (!"".equals(sv.name())) ? sv.name() : getName(clazz);
    }

    ViewDefinition vd = new ViewDefinition(name, query);

    return createMeta(vd, clazz, null);
  }

  private ClassMetadata createMeta(ClassDefinition def, Class<?> clazz, String uriPrefix)
                            throws OtmException {
    sf.addDefinition(def);

    ClassBindings bin = sf.getClassBindings(def.getName());
    bin.bind(EntityMode.POJO, new ClassBinder(clazz));

    Set<Method> annotated = new HashSet<Method>();
    String      ours      = Id.class.getPackage().getName();

    for (Method method : clazz.getDeclaredMethods()) {
      // We only care about annotated methods. Ignore everything else.
      for (Annotation a : method.getAnnotations()) {
        if (a.annotationType().getPackage().getName().equals(ours)) {
          annotated.add(method);

          break;
        }
      }
    }

    BeanInfo                beanInfo;
    Set<PropertyDescriptor> properties = new HashSet<PropertyDescriptor>();

    try {
      beanInfo = Introspector.getBeanInfo(clazz);
    } catch (IntrospectionException e) {
      throw new OtmException("Failed to introspect " + clazz, e);
    }

    for (PropertyDescriptor property : beanInfo.getPropertyDescriptors()) {
      if (property instanceof IndexedPropertyDescriptor)
        continue;

      Method setter = property.getWriteMethod();
      Method getter = property.getReadMethod();

      if (!annotated.contains(setter) && !annotated.contains(getter))
        continue;

      if (setter == null)
        throw new OtmException("Missing setter for property '" + property.getName() + "' in "
                               + clazz);

      if ((getter == null) && !(def instanceof ViewDefinition))
        throw new OtmException("Missing getter for property '" + property.getName() + "' in "
                               + clazz);

      if (Modifier.isFinal(setter.getModifiers()))
        throw new OtmException("Setter can't be 'final' for  '" + property.getName() + "' in "
                               + clazz);

      if ((getter != null) && Modifier.isFinal(setter.getModifiers()))
        throw new OtmException("Getter can't be 'final' for  '" + property.getName() + "' in "
                               + clazz);

      annotated.remove(getter);
      annotated.remove(setter);
      properties.add(property);
    }

    if (annotated.size() > 0)
      throw new OtmException("Following annotated methods are not valid getters or setters: "
                             + annotated);

    for (PropertyDescriptor property : properties) {
      PropertyInfo       fi = new PropertyInfo(def, property);

      PropertyDefinition d  = fi.getDefinition(sf, uriPrefix);

      if (d == null) {
        log.info("Skipped (WTF) " + fi);

        continue;
      }

      sf.addDefinition(d);

      // XXX: This API hasn't changed. We should be doing two passes.
      //      One for definition and one for Binder. Till then
      //      references are resolved right here.

      d.resolveReference(sf);
      FieldBinder b = fi.getBinder(sf, d);
      bin.addAndBindProperty(d.getName(), EntityMode.POJO, b);
    }

    return def.buildClassMetadata(sf);
  }

  private static String getName(Class<?> clazz) {
    String  name = clazz.getName();
    Package p    = clazz.getPackage();

    if (p != null)
      name = name.substring(p.getName().length() + 1);

    return name;
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

  private static class PropertyInfo {
    final ClassDefinition    cd;
    final String             name;
    final PropertyDescriptor property;
    final Class<?>           type;
    final boolean            isArray;
    final boolean            isCollection;

    public PropertyInfo(ClassDefinition cd, PropertyDescriptor property)
                 throws OtmException {
      this.cd         = cd;
      this.property   = property;
      this.name       = cd.getName() + ":" + property.getName();

      Class<?> ptype  = property.getPropertyType();

      isArray         = ptype.isArray();
      isCollection    = Collection.class.isAssignableFrom(ptype);

      if (isArray)
        type = ptype.getComponentType();
      else if (isCollection)
        type = collectionType(property.getWriteMethod());
      else
        type = ptype;
    }

    private Class<?> getDeclaringClass() {
      return property.getWriteMethod().getDeclaringClass();
    }

    private static Class collectionType(Method setter) {
      Type  type   = setter.getGenericParameterTypes()[0];
      Class result = Object.class;

      if (type instanceof ParameterizedType) {
        ParameterizedType ptype = (ParameterizedType) type;
        Type[]            targs = ptype.getActualTypeArguments();

        if ((targs.length > 0) && (targs[0] instanceof Class))
          result = (Class) targs[0];
      }

      return result;
    }

    public String getName() {
      return name;
    }

    public String toString() {
      return "'" + property.getName() + "' in " + getDeclaringClass();
    }

    public PropertyDefinition getDefinition(SessionFactory sf, String uriPrefix)
                                     throws OtmException {
      GeneratedValue gv   = null;
      Annotation     ann  = null;
      String         ours = Id.class.getPackage().getName();

      for (Method m : new Method[] { property.getWriteMethod(), property.getReadMethod() }) {
        if (m == null)
          continue;

        for (Annotation a : m.getAnnotations()) {
          if (a instanceof GeneratedValue)
            gv = (GeneratedValue) a;
          else if (a.annotationType().getPackage().getName().equals(ours)) {
            if (ann != null)
              throw new OtmException("Only one of @Id, @Predicate, @Blob, @Projection, @PredicateMap"
                                     + " or @Embedded can be applied to " + this);

            ann = a;
          }
        }
      }

      if (((ann != null) && !(ann instanceof Id))
           && ((cd instanceof ViewDefinition) ^ (ann instanceof Projection)))
        throw new OtmException("@Projection can-only/must be applied to Views or Sub-Views : "
                               + this);

      if ((ann == null) && (cd instanceof ViewDefinition))
        return null;

      IdentifierGenerator generator;

      if (gv == null)
        generator = null;
      else if ((ann instanceof Id) || (ann instanceof Predicate) || (ann == null))
        generator = getGenerator(sf, uriPrefix, gv);
      else
        throw new OtmException("@GeneratedValue can only be specified for @Id and @Predicate : "
                               + this);

      if ((ann instanceof Predicate) || (ann == null))
        return getRdfDefinition(sf, uriPrefix, (Predicate) ann, generator);

      if (ann instanceof Projection)
        return getVarDefinition(sf, (Projection) ann);

      if (ann instanceof Id)
        return getIdDefinition(sf, (Id) ann, generator);

      if (ann instanceof Blob)
        return getBlobDefinition(sf, (Blob) ann);

      if (ann instanceof Embedded)
        return getEmbeddedDefinition(sf);

      if (ann instanceof PredicateMap)
        return getPredicateMapDefinition(sf, (PredicateMap) ann);

      throw new OtmException("Unexpected annotation " + ann.annotationType() + " on " + this);
    }

    public IdentifierGenerator getGenerator(SessionFactory sf, String uriPrefix, GeneratedValue gv)
                                     throws OtmException {
      IdentifierGenerator generator;

      try {
        generator = (IdentifierGenerator) Thread.currentThread().getContextClassLoader()
                                                 .loadClass(gv.generatorClass()).newInstance();
      } catch (Throwable t) {
        // Between Class.forName() and newInstance() there are a half-dozen possible excps
        throw new OtmException("Unable to find implementation of '" + gv.generatorClass()
                               + "' generator for " + this, t);
      }

      String pre = sf.expandAlias(gv.uriPrefix());

      if (pre.equals("")) {
        pre   = ((uriPrefix == null) || uriPrefix.equals("")) ? Rdf.topaz : uriPrefix;
        // Compute default uriPrefix: Rdf.topaz/clazz/generatorClass#
        pre   = Rdf.topaz + getDeclaringClass().getName() + '/' + property.getName() + '#';

        //pre = pre + cd.getName() + '/' + property.getName() + '/';
      } else {
        try {
          // Validate that we have a valid uri
          URI.create(pre);
        } catch (IllegalArgumentException iae) {
          throw new OtmException("Illegal uriPrefix '" + pre + "' in @GeneratedValue for " + this,
                                 iae);
        }
      }

      generator.setUriPrefix(pre);

      return generator;
    }

    public RdfDefinition getRdfDefinition(SessionFactory sf, String ns, Predicate rdf,
                                          IdentifierGenerator generator)
                                   throws OtmException {
      String uri =
        ((rdf != null) && !"".equals(rdf.uri())) ? sf.expandAlias(rdf.uri())
        : ((ns != null) ? (ns + property.getName()) : null);

      if (uri == null)
        throw new OtmException("Missing attribute 'uri' in @Predicate for " + this);

      String dt =
        ((rdf == null) || "".equals(rdf.dataType()))
        ? sf.getSerializerFactory().getDefaultDataType(type) : sf.expandAlias(rdf.dataType());

      if (Predicate.UNTYPED.equals(dt))
        dt = null;

      Serializer serializer = sf.getSerializerFactory().getSerializer(type, dt);

      if ((serializer == null) && sf.getSerializerFactory().mustSerialize(type))
        throw new OtmException("No serializer found for '" + type + "' with dataType '" + dt
                               + "' for " + this);

      boolean inverse = (rdf != null) && rdf.inverse();
      String  model   = ((rdf != null) && !"".equals(rdf.model())) ? rdf.model() : null;

      String  assoc   = (serializer == null) ? getEntityName(type) : null;

      if (inverse && (model == null) && (serializer == null))
        model = getModel(type);

      boolean        notOwned       = (rdf != null) && rdf.notOwned();

      CollectionType mt             = getColType(rdf);
      CascadeType[]  ct             =
        (rdf != null) ? rdf.cascade() : new CascadeType[] { CascadeType.peer };
      FetchType      ft             = (rdf != null) ? rdf.fetch() : FetchType.lazy;

      boolean        objectProperty = false;

      if ((rdf == null) || PropType.UNDEFINED.equals(rdf.type())) {
        boolean declaredAsUri =
          URI.class.isAssignableFrom(type) || URL.class.isAssignableFrom(type);
        objectProperty = (serializer == null) || inverse || declaredAsUri;
      } else if (PropType.OBJECT.equals(rdf.type())) {
        if (!"".equals(rdf.dataType()))
          throw new OtmException("Datatype cannot be specified for an object-Property " + this);

        objectProperty = true;
      } else if (PropType.DATA.equals(rdf.type())) {
        if (serializer == null)
          throw new OtmException("No serializer found for '" + type + "' with dataType '" + dt
                                 + "' for a data-property " + this);

        if (inverse)
          throw new OtmException("Inverse mapping cannot be specified for a data-property " + this);
      }

      if (serializer != null)
        ft = null;

      return new RdfDefinition(getName(), null, uri, dt, inverse, model, mt, !notOwned, generator, ct,
                               ft, assoc, objectProperty);
    }

    private CollectionType getColType(Predicate rdf) throws OtmException {
      return (rdf == null) ? CollectionType.PREDICATE : rdf.collectionType();
    }

    public VarDefinition getVarDefinition(SessionFactory sf, Projection proj)
                                   throws OtmException {
      String     var        = "".equals(proj.value()) ? property.getName() : proj.value();
      Serializer serializer = sf.getSerializerFactory().getSerializer(type, null);

      if ((serializer == null) && sf.getSerializerFactory().mustSerialize(type))
        throw new OtmException("No serializer found for '" + type + "' for " + this);

      // XXX: shouldn't we parse the query to figure this out?
      String assoc = (serializer == null) ? getEntityName(type) : null;

      return new VarDefinition(getName(), var, proj.fetch(), assoc);
    }

    public IdDefinition getIdDefinition(SessionFactory sf, Id id, IdentifierGenerator generator)
                                 throws OtmException {
      if (!type.equals(String.class) && !type.equals(URI.class) && !type.equals(URL.class))
        throw new OtmException("@Id property '" + this + "' must be a String, URI or URL.");

      return new IdDefinition(getName(), generator);
    }

    public BlobDefinition getBlobDefinition(SessionFactory sf, Blob blob)
                                     throws OtmException {
      if (!isArray || !type.equals(Byte.TYPE))
        throw new OtmException("@Blob may only be applied to a 'byte[]' property : " + this);

      return new BlobDefinition(getName());
    }

    public RdfDefinition getPredicateMapDefinition(SessionFactory sf, PredicateMap pmap)
                                            throws OtmException {
      String model = null; // TODO: allow predicate maps from other models
      Type   type  = property.getWriteMethod().getGenericParameterTypes()[0];

      if (Map.class.isAssignableFrom(property.getPropertyType())
           && (type instanceof ParameterizedType)) {
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
              return new RdfDefinition(getName(), model);
          }
        }
      }

      throw new OtmException("@PredicateMap can be applied to a Map<String, List<String>> "
                             + " property only. It cannot be applied to " + this);
    }

    public EmbeddedDefinition getEmbeddedDefinition(SessionFactory sf)
                                             throws OtmException {
      Serializer serializer = sf.getSerializerFactory().getSerializer(type, null);

      if (isArray || isCollection || (serializer != null))
        throw new OtmException("@Embedded class property " + this
                               + " can't be an array, collection or a simple data type");

      sf.preload(type);

      ClassMetadata cm = sf.getClassMetadata(type);

      return new EmbeddedDefinition(getName(), cm.getName());
    }

    public FieldBinder getBinder(SessionFactory sf, PropertyDefinition pd)
                          throws OtmException {
      if (pd instanceof EmbeddedDefinition)
        return new EmbeddedClassFieldBinder(property.getReadMethod(), property.getWriteMethod());

      Serializer serializer;

      if (pd instanceof BlobDefinition)
        serializer = null;
      else if (pd instanceof RdfDefinition) {
        RdfDefinition rd = (RdfDefinition) pd;
        serializer = (rd.isAssociation()) ? null
                     : sf.getSerializerFactory().getSerializer(type, rd.getDataType());
      } else if (pd instanceof VarDefinition) {
        VarDefinition vd = (VarDefinition) pd;
        serializer = (vd.getAssociatedEntity() == null) ? null
                     : sf.getSerializerFactory().getSerializer(type, null);
      } else
        serializer = sf.getSerializerFactory().getSerializer(type, null);

      if (isArray)
        return new ArrayFieldBinder(property.getReadMethod(), property.getWriteMethod(),
                                    serializer, type);

      if (isCollection)
        return new CollectionFieldBinder(property.getReadMethod(), property.getWriteMethod(),
                                         serializer, type);

      return new ScalarFieldBinder(property.getReadMethod(), property.getWriteMethod(), serializer);
    }
  }
}
