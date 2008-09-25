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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import java.net.URI;
import java.net.URL;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.otm.CascadeType;
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
import org.topazproject.otm.mapping.java.ClassBinder;
import org.topazproject.otm.mapping.java.EmbeddedClassFieldBinder;
import org.topazproject.otm.mapping.java.Property;
import org.topazproject.otm.mapping.java.PropertyBinderFactory;
import org.topazproject.otm.serializer.Serializer;

/**
 * A factory that processes annotations on a class.
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
   * @throws OtmException on an error
   */
  public void create(Class clazz) throws OtmException {
    addAliases(clazz);

    if ((clazz.getAnnotation(View.class) != null) || (clazz.getAnnotation(SubView.class) != null))
      createView(clazz);
    else
      createEntity(clazz);
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

  private void createEntity(Class<?> clazz) throws OtmException {
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

    createMeta(ed, clazz, uriPrefix);
  }

  private void createView(Class<?> clazz) throws OtmException {
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

    createMeta(vd, clazz, null);
  }

  private void createMeta(ClassDefinition def, Class<?> clazz, String uriPrefix)
                   throws OtmException {
    sf.addDefinition(def);

    ClassBindings bin = sf.getClassBindings(def.getName());
    bin.bind(EntityMode.POJO, new ClassBinder(clazz));

    Map<String, PropertyDefFactory> factories = new HashMap<String, PropertyDefFactory>();

    for (Method method : clazz.getDeclaredMethods()) {
      if (!isAnnotated(method))
        continue;

      Property property = Property.toProperty(method);

      if (property == null)
        throw new OtmException("'" + method.toGenericString() + "' is not a valid getter or setter");

      PropertyDefFactory pi = factories.get(property.getName());

      if (pi != null) {
        if (method.equals(pi.property.getReadMethod())
             || method.equals(pi.property.getWriteMethod()))
          continue;

        throw new OtmException("Duplicate property " + property);
      }

      validate(property, def);
      factories.put(property.getName(), new PropertyDefFactory(def, property));
    }

    if (def instanceof EntityDefinition) {
      if (clazz.getGenericSuperclass() instanceof ParameterizedType)
        addGenericsSyntheticProps(def, clazz, factories);

      Map<String, String> supersedes = new HashMap<String, String>();
      buildSupersedes((EntityDefinition) def, supersedes);

      for (String name : supersedes.keySet()) {
        PropertyDefFactory pi = factories.get(name);

        if (pi != null)
          pi.setSupersedes(supersedes.get(name));
      }
    }

    for (PropertyDefFactory fi : factories.values()) {
      PropertyDefinition d = fi.getDefinition(sf, uriPrefix);

      if (d == null) {
        log.info("Skipped (WTF) " + fi);

        continue;
      }

      sf.addDefinition(d);
      bin.addBinderFactory(new PropertyBinderFactory(fi.name, fi.property));
    }
  }

  private void buildSupersedes(EntityDefinition def, Map<String, String> supersedes) {
    if (def.getSuper() != null) {
      EntityDefinition sdef = (EntityDefinition) sf.getDefinition(def.getSuper());

      if (sdef != null)
        buildSupersedes(sdef, supersedes);
    }

    ClassBindings b = sf.getClassBindings(def.getName());

    if (b != null) {
      for (String prop : b.getProperties()) {
        PropertyDefinition pd = (PropertyDefinition) sf.getDefinition(prop);

        if ((pd != null) && def.getName().equals(pd.getNamespace()))
          supersedes.put(pd.getLocalName(), prop); // overwrite super-class
      }
    }
  }

  private void addGenericsSyntheticProps(ClassDefinition def, Class clazz,
                                         Map<String, PropertyDefFactory> factories)
                                  throws OtmException {
    for (Method m : clazz.getSuperclass().getDeclaredMethods()) {
      if (!isAnnotated(m))
        continue;

      Property property = Property.toProperty(m);

      if ((property == null) || factories.containsKey(property.getName()))
        continue;

      property = property.resolveGenericsType(clazz);

      if (property != null)
        factories.put(property.getName(), new PropertyDefFactory(def, property));
    }
  }

  private static boolean isAnnotated(Method method) {
    String ours = Id.class.getPackage().getName();

    for (Annotation a : method.getAnnotations())
      if (a.annotationType().getPackage().getName().equals(ours))
        return true;

    return false;
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

  /**
   * Gets the entity name for a class.
   *
   * @param clazz the class to look-up
   *
   * @return name from {@link org.topazproject.otm.annotations.Entity @Entity} annotation or the
   *         default short-name of the clazz.
   */
  public static String getEntityName(Class<?> clazz) {
    if (clazz == null)
      return null;

    Entity entity = clazz.getAnnotation(Entity.class);

    if ((entity != null) && !"".equals(entity.name()))
      return entity.name();

    return getName(clazz);
  }

  private static void validate(Property property, ClassDefinition def)
                        throws OtmException {
    Method setter = property.getWriteMethod();
    Method getter = property.getReadMethod();

    if (setter == null)
      throw new OtmException("Missing setter for property " + property);

    if ((getter == null) && !(def instanceof ViewDefinition))
      throw new OtmException("Missing getter for property " + property);

    if (Modifier.isFinal(setter.getModifiers()))
      throw new OtmException("Setter can't be 'final' for " + property);

    if ((getter != null) && Modifier.isFinal(getter.getModifiers()))
      throw new OtmException("Getter can't be 'final' for " + property);
  }

  private static class PropertyDefFactory {
    final ClassDefinition cd;
    final Property        property;
    final String          name;
    String                supersedes;

    public PropertyDefFactory(ClassDefinition cd, Property property)
                       throws OtmException {
      this.cd         = cd;
      this.property   = property;
      this.name       = cd.getName() + ":" + property.getName();
    }

    public void setSupersedes(String supersedes) {
      this.supersedes = supersedes;
    }

    public String getName() {
      return name;
    }

    public String toString() {
      return property.toString();
    }

    public PropertyDefinition getDefinition(SessionFactory sf, String uriPrefix)
                                     throws OtmException {
      GeneratedValue gv   = null;
      Annotation     ann  = null;
      Class<?>       decl = null;
      String         ours = Id.class.getPackage().getName();

      for (Method m : new Method[] { property.getWriteMethod(), property.getReadMethod() }) {
        if (m == null)
          continue;

        for (Annotation a : m.getAnnotations()) {
          if (a instanceof GeneratedValue)
            gv = (GeneratedValue) a;
          else if (a.annotationType().getPackage().getName().equals(ours)) {
            if (ann != null) {
              if (!ann.getClass().equals(a.getClass())
                   || !decl.isAssignableFrom(m.getDeclaringClass())
                   || (decl == m.getDeclaringClass()))
                throw new OtmException("Only one of @Id, @Predicate, @Blob, @Projection, @PredicateMap"
                                       + " or @Embedded can be applied to " + this);
            }

            ann    = a;
            decl   = m.getDeclaringClass();
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
        pre   = Rdf.topaz + property.getContainingClass().getName() + '/' + property.getName()
                + '#';

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
      Class<?> type = property.getComponentType();
      String   ref  = ((rdf != null) && !"".equals(rdf.ref())) ? rdf.ref() : null;

      if (ref == null)
        ref = supersedes;

      String uri =
        ((rdf != null) && !"".equals(rdf.uri())) ? sf.expandAlias(rdf.uri())
        : (((ref == null) && (ns != null)) ? (ns + property.getName()) : null);

      if ((uri == null) && (ref == null))
        throw new OtmException("Missing attribute 'uri' in @Predicate for " + this);

      Boolean inverse        =
        getBooleanProperty(((rdf != null) ? rdf.inverse() : null), ref, Boolean.FALSE);
      Boolean notOwned       =
        getBooleanProperty(((rdf != null) ? rdf.notOwned() : null), ref, Boolean.FALSE);
      Boolean owned          = (notOwned == null) ? null : (!notOwned);

      String  dt             =
        ((rdf != null) && !"".equals(rdf.dataType())) ? sf.expandAlias(rdf.dataType())
        : ((ref != null) ? null : sf.getSerializerFactory().getDefaultDataType(type));

      String  assoc          =
        sf.getSerializerFactory().mustSerialize(type) ? null : getEntityName(type);
      Boolean objectProperty = null;

      if ((rdf == null) || PropType.UNDEFINED.equals(rdf.type())) {
        if (URI.class.isAssignableFrom(type) || URL.class.isAssignableFrom(type)) {
          if (ref == null)
            objectProperty = Boolean.TRUE;
        } else
          objectProperty = assoc != null;
      } else if (PropType.OBJECT.equals(rdf.type())) {
        if (!"".equals(rdf.dataType()))
          throw new OtmException("Datatype cannot be specified for an object-Property " + this);

        objectProperty = Boolean.TRUE;
      } else if (PropType.DATA.equals(rdf.type())) {
        assoc = null;

        if ((inverse != null) && (inverse == Boolean.TRUE))
          throw new OtmException("Inverse mapping cannot be specified for a data-property " + this);

        objectProperty   = Boolean.FALSE;
        inverse          = Boolean.FALSE;
      }

      String model = ((rdf != null) && !"".equals(rdf.model())) ? rdf.model() : null;

      if ((inverse != null) && inverse && (model == null) && (assoc != null))
        model = getModel(type);

      CollectionType mt = (rdf == null) ? CollectionType.UNDEFINED : rdf.collectionType();

      if (mt == CollectionType.UNDEFINED)
        mt = (ref == null) ? CollectionType.PREDICATE : null;

      CascadeType[] ct =
        (rdf != null) ? rdf.cascade() : new CascadeType[] { CascadeType.undefined };

      if ((ct.length == 1) && (ct[0] == CascadeType.undefined))
        ct = (ref == null) ? new CascadeType[] { CascadeType.peer } : null;

      FetchType ft = (rdf != null) ? rdf.fetch() : FetchType.undefined;

      if (ft == FetchType.undefined)
        ft = (ref == null) ? FetchType.lazy : null;

      return new RdfDefinition(getName(), ref, supersedes, uri, dt, inverse, model, mt, owned,
                               generator, ct, ft, assoc, objectProperty);
    }

    private Boolean getBooleanProperty(Predicate.BT raw, String ref, Boolean dflt) {
      if (raw == Predicate.BT.TRUE)
        return Boolean.TRUE;

      if (raw == Predicate.BT.FALSE)
        return Boolean.FALSE;

      if (ref == null)
        return dflt;

      return null;
    }

    public VarDefinition getVarDefinition(SessionFactory sf, Projection proj)
                                   throws OtmException {
      Class<?>   type       = property.getComponentType();
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
      Class<?> type = property.getComponentType();

      if (!type.equals(String.class) && !type.equals(URI.class) && !type.equals(URL.class))
        throw new OtmException("@Id property '" + this + "' must be a String, URI or URL.");

      return new IdDefinition(getName(), generator);
    }

    public BlobDefinition getBlobDefinition(SessionFactory sf, Blob blob)
                                     throws OtmException {
      if (!property.isArray() || !property.getComponentType().equals(Byte.TYPE))
        throw new OtmException("@Blob may only be applied to a 'byte[]' property : " + this);

      return new BlobDefinition(getName());
    }

    public RdfDefinition getPredicateMapDefinition(SessionFactory sf, PredicateMap pmap)
                                            throws OtmException {
      String model = null; // TODO: allow predicate maps from other models
      Type   type  = property.getGenericType();

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
      boolean simpleType = sf.getSerializerFactory().mustSerialize(property.getComponentType());

      if (property.isArray() || property.isCollection() || simpleType)
        throw new OtmException("@Embedded class property " + this
                               + " can't be an array, collection or a simple data type");

      sf.preload(property.getComponentType());

      return new EmbeddedDefinition(getName(), getEntityName(property.getComponentType()));
    }
  }
}
