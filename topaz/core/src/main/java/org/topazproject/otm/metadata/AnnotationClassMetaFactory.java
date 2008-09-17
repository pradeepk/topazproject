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

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;

import java.lang.annotation.Annotation;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import java.net.URI;
import java.net.URL;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    Map<String, PropertyInfo> properties = new HashMap<String, PropertyInfo>();

    for (Method method : clazz.getDeclaredMethods()) {
      if (!isAnnotated(method))
        continue;

      PropertyDescriptor property = toProperty(method);

      if (property == null)
        throw new OtmException("'" + method.toGenericString() + "' is not a valid getter or setter");

      validate(property, def, method);
      properties.put(property.getName(), new PropertyInfo(def, property));
    }

    if (def instanceof EntityDefinition) {
      if (clazz.getGenericSuperclass() instanceof ParameterizedType)
        addGenericsSyntheticProps(def, clazz, properties);

      Map<String, String> supersedes = new HashMap<String, String>(); // localName --> superseded def
      buildSupersedes((EntityDefinition) def, supersedes);

      for (String name : supersedes.keySet()) {
        PropertyInfo pi = properties.get(name);

        if (pi != null)
          pi.setSupersedes(supersedes.get(name));
      }
    }

    for (PropertyInfo fi : properties.values()) {
      PropertyDefinition d = fi.getDefinition(sf, uriPrefix);

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

  private void addGenericsSyntheticProps(ClassDefinition def, Class clazz,
                                         Map<String, PropertyInfo> properties)
                                  throws OtmException {
    for (Method m : clazz.getSuperclass().getDeclaredMethods()) {
      if (!isAnnotated(m))
        continue;

      PropertyDescriptor property = toProperty(m);

      if ((property == null) || properties.containsKey(property.getName()))
        continue;

      PropertyInfo pi = resolve(def, clazz, property);

      if (pi != null)
        properties.put(property.getName(), pi);
    }
  }

  /**
   * Resolves the generics property in the parameterized sub-class.
   *
   * @param def the definition
   * @param clazz the parameterized sub-class
   * @param property property in super-class
   *
   * @return the synthetic property in the parameterized sub-class or null
   *
   * @throws OtmException on an error
   */
  private PropertyInfo resolve(ClassDefinition def, Class clazz, PropertyDescriptor property)
                        throws OtmException {
    Method       m  = property.getReadMethod();
    Type         t  = m.getGenericReturnType();
    PropertyInfo pi = null;

    if (t instanceof TypeVariable) {
      Class<?> ptype = resolve((TypeVariable) t, clazz);

      if (ptype == null)
        log.warn("Synthetic property not generated for '" + property.getName() + "' in " + clazz
                 + "' because the TypeVariable '" + t + "' is unresolved.");
      else {
        pi = new PropertyInfo(def, property.getName(), property.getReadMethod(),
                              property.getWriteMethod(), ptype, null);
      }
    } else if (t instanceof ParameterizedType) {
      Type[] args = ((ParameterizedType) t).getActualTypeArguments();

      if (!Collection.class.isAssignableFrom(property.getPropertyType()) || (args.length != 1)
           || !(args[0] instanceof TypeVariable))
        log.warn("Synthetic property not generated for '" + property.getName() + "' in " + clazz
                 + "' because it is not an instance of a Parameterized Collection.");
      else {
        Class<?> ptype = resolve((TypeVariable) args[0], clazz);

        if (ptype == null)
          log.warn("Synthetic property not generated for '" + property.getName() + "' in " + clazz
                   + "' because the TypeVariable '" + args[0]
                   + "' is unresolved in Parameterized Collection '" + t + "'.");
        else {
          pi = new PropertyInfo(def, property.getName(), property.getReadMethod(),
                                property.getWriteMethod(), property.getPropertyType(), ptype);
        }
      }
    } else if (t instanceof GenericArrayType) {
      Type     comp  = ((GenericArrayType) t).getGenericComponentType();
      Class<?> ptype;

      if (!property.getPropertyType().isArray() || !(comp instanceof TypeVariable)
           || ((ptype = resolve((TypeVariable) comp, clazz)) == null))
        log.warn("Synthetic property not generated for '" + property.getName() + "' in " + clazz
                 + "' because the GenericComponentType '" + comp
                 + "' is unresolved in the GenericArrayType '" + t + "'.");
      else {
        pi           = new PropertyInfo(def, property.getName(), property.getReadMethod(),
                                        property.getWriteMethod(), property.getPropertyType(), ptype);
      }
    }

    return pi;
  }

  private static Class<?> resolve(TypeVariable t, Class<?> clazz) {
    ParameterizedType pt    = (ParameterizedType) clazz.getGenericSuperclass();
    TypeVariable[]    types = clazz.getSuperclass().getTypeParameters();

    if (log.isDebugEnabled())
      log.debug("TypeParameters " + Arrays.toString(types));

    if (log.isDebugEnabled())
      log.debug("ActualTypeArgs " + Arrays.toString(pt.getActualTypeArguments()));

    for (int i = 0; i < types.length; i++) {
      if (t.equals(types[i])) {
        Type ptype = pt.getActualTypeArguments()[i];

        return (ptype instanceof Class) ? (Class<?>) ptype : null;
      }
    }

    return null;
  }

  private static boolean isAnnotated(Method method) {
    String ours = Id.class.getPackage().getName();

    for (Annotation a : method.getAnnotations())
      if (a.annotationType().getPackage().getName().equals(ours))
        return true;

    return false;
  }

  private static PropertyDescriptor toProperty(Method m)
                                        throws OtmException {
    String capitalized = getCapitalizedPropertyName(m);

    if (capitalized == null)
      return null;

    String propName = Introspector.decapitalize(capitalized);
    Method setter   = null;
    Method getter   = null;

    if (m.getName().startsWith("set")) {
      setter = m;

      try {
        getter = m.getDeclaringClass().getMethod("is" + capitalized);
      } catch (Throwable t) {
      }

      try {
        if ((getter == null) || !getter.getReturnType().equals(setter.getParameterTypes()[0]))
          getter = m.getDeclaringClass().getMethod("get" + capitalized);
      } catch (Throwable t) {
      }
    } else {
      getter = m;

      try {
        setter = m.getDeclaringClass().getMethod("set" + capitalized, getter.getReturnType());
      } catch (Throwable t) {
      }
    }

    try {
      // NOTE: PropertyDescriptor(propertyName, class) is buggy. 
      // Hence all the work above to figure out getter and setter.
      return new PropertyDescriptor(propName, getter, setter);
    } catch (IntrospectionException e) {
      throw new OtmException("Failed to create a PropertyDescriptor for '" + propName + "' from '"
                             + m.toGenericString() + "'", e);
    }
  }

  private static String getCapitalizedPropertyName(Method m) {
    if (Modifier.isStatic(m.getModifiers()))
      return null;

    if (m.getName().startsWith("set") && (m.getParameterTypes().length == 1))
      return m.getName().substring(3);

    if (m.getName().startsWith("get") && (m.getParameterTypes().length == 0))
      return m.getName().substring(3);

    if (m.getName().startsWith("is") && (m.getParameterTypes().length == 0))
      return m.getName().substring(2);

    return null;
  }

  private static void validate(PropertyDescriptor property, ClassDefinition def, Method m)
                        throws OtmException {
    Method setter = property.getWriteMethod();
    Method getter = property.getReadMethod();

    if (!m.equals(setter) && !m.equals(getter))
      throw new OtmException("'" + m.toGenericString() + "' is not a getter or setter for '"
                             + property.getName() + "'");

    if (setter == null)
      throw new OtmException("Missing setter for property '" + property.getName() + "' in "
                             + m.getDeclaringClass());

    if ((getter == null) && !(def instanceof ViewDefinition))
      throw new OtmException("Missing getter for property '" + property.getName() + "' in "
                             + m.getDeclaringClass());

    if (Modifier.isFinal(setter.getModifiers()))
      throw new OtmException("Setter can't be 'final' for  '" + property.getName() + "' in "
                             + m.getDeclaringClass());

    if ((getter != null) && Modifier.isFinal(setter.getModifiers()))
      throw new OtmException("Getter can't be 'final' for  '" + property.getName() + "' in "
                             + m.getDeclaringClass());
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
    final ClassDefinition cd;
    final String          name;
    final String          localName;
    final Method          getter;
    final Method          setter;
    final Class<?>        ptype;
    final Class<?>        type;
    final boolean         isArray;
    final boolean         isCollection;
    String                supersedes;

    public PropertyInfo(ClassDefinition cd, PropertyDescriptor property)
                 throws OtmException {
      this(cd, property.getName(), property.getReadMethod(), property.getWriteMethod(),
           property.getPropertyType(), null);
    }

    public PropertyInfo(ClassDefinition cd, String localName, Method getter, Method setter,
                        Class<?> ptype, Class<?> type)
                 throws OtmException {
      this.cd             = cd;
      this.localName      = localName;
      this.getter         = getter;
      this.setter         = setter;
      this.ptype          = ptype;

      this.name           = cd.getName() + ":" + localName;

      this.isArray        = ptype.isArray();
      this.isCollection   = Collection.class.isAssignableFrom(ptype);

      this.type           = (type != null) ? type
                            : ((isArray ? ptype.getComponentType()
                                : ((isCollection)
                                   ? collectionType(setter.getGenericParameterTypes()[0]) : ptype)));
    }

    public void setSupersedes(String supersedes) {
      this.supersedes = supersedes;
    }

    private Class<?> getDeclaringClass() {
      return setter.getDeclaringClass();
    }

    private static Class collectionType(Type type) {
      Class result = Object.class;

      if (type instanceof ParameterizedType) {
        ParameterizedType ptype = (ParameterizedType) type;
        Type[]            targs = ptype.getActualTypeArguments();

        if ((targs.length > 0) && (targs[0] instanceof Class))
          result = (Class) targs[0];

        if ((targs.length > 0) && (targs[0] instanceof TypeVariable)
             && ((TypeVariable) targs[0]).getBounds()[0] instanceof Class)
          result = (Class) ((TypeVariable) targs[0]).getBounds()[0];
      }

      return result;
    }

    public String getName() {
      return name;
    }

    public String toString() {
      return "'" + localName + "' in " + getDeclaringClass();
    }

    public PropertyDefinition getDefinition(SessionFactory sf, String uriPrefix)
                                     throws OtmException {
      GeneratedValue gv   = null;
      Annotation     ann  = null;
      String         ours = Id.class.getPackage().getName();

      for (Method m : new Method[] { setter, getter }) {
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
        pre   = Rdf.topaz + getDeclaringClass().getName() + '/' + localName + '#';

        //pre = pre + cd.getName() + '/' + localName + '/';
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
      String ref = ((rdf != null) && !"".equals(rdf.ref())) ? rdf.ref() : null;

      if (ref == null)
        ref = supersedes;

      String uri =
        ((rdf != null) && !"".equals(rdf.uri())) ? sf.expandAlias(rdf.uri())
        : (((ref == null) && (ns != null)) ? (ns + localName) : null);

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
      String     var        = "".equals(proj.value()) ? localName : proj.value();
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
      Type   type  = setter.getGenericParameterTypes()[0];

      if (Map.class.isAssignableFrom(getter.getReturnType()) && (type instanceof ParameterizedType)) {
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
        return new EmbeddedClassFieldBinder(getter, setter);

      Serializer serializer;

      if (pd instanceof BlobDefinition)
        serializer = null;
      else if (pd instanceof RdfDefinition) {
        RdfDefinition rd = (RdfDefinition) pd;
        serializer = (rd.isAssociation()) ? null
                     : sf.getSerializerFactory().getSerializer(type, rd.getDataType());

        if ((serializer == null) && sf.getSerializerFactory().mustSerialize(type))
          throw new OtmException("No serializer found for '" + type + "' with dataType '"
                                 + rd.getDataType() + "' for " + this);
      } else if (pd instanceof VarDefinition) {
        VarDefinition vd = (VarDefinition) pd;
        serializer = (vd.getAssociatedEntity() == null) ? null
                     : sf.getSerializerFactory().getSerializer(type, null);
      } else
        serializer = sf.getSerializerFactory().getSerializer(type, null);

      if (isArray)
        return new ArrayFieldBinder(getter, setter, serializer, type);

      if (isCollection)
        return new CollectionFieldBinder(getter, setter, serializer, type);

      return new ScalarFieldBinder(getter, setter, serializer);
    }
  }
}
