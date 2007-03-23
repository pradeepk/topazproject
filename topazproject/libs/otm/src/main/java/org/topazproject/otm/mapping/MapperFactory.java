package org.topazproject.otm.mapping;

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
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.annotations.DataType;
import org.topazproject.otm.annotations.Embeddable;
import org.topazproject.otm.annotations.Embedded;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.Model;
import org.topazproject.otm.annotations.Ns;
import org.topazproject.otm.annotations.Rdf;

/**
 * A factory for creating mappers for java class fields.
 *
 * @author Pradeep Krishnan
 */
public class MapperFactory {
  private static final Log          log     = LogFactory.getLog(MapperFactory.class);
  private static Map<Class, String> typeMap = new HashMap<Class, String>();

  static {
    typeMap.put(String.class, null);
    typeMap.put(Boolean.class, Rdf.xsd + "boolean");
    typeMap.put(Boolean.TYPE, Rdf.xsd + "boolean");
    typeMap.put(Integer.class, Rdf.xsd + "int");
    typeMap.put(Integer.TYPE, Rdf.xsd + "int");
    typeMap.put(Long.class, Rdf.xsd + "long");
    typeMap.put(Long.TYPE, Rdf.xsd + "long");
    typeMap.put(Short.class, Rdf.xsd + "short");
    typeMap.put(Short.TYPE, Rdf.xsd + "short");
    typeMap.put(Float.class, Rdf.xsd + "float");
    typeMap.put(Float.TYPE, Rdf.xsd + "float");
    typeMap.put(Double.class, Rdf.xsd + "double");
    typeMap.put(Double.TYPE, Rdf.xsd + "double");
    typeMap.put(Byte.class, Rdf.xsd + "byte");
    typeMap.put(Byte.TYPE, Rdf.xsd + "byte");
    typeMap.put(URI.class, Rdf.xsd + "anyURI");
    typeMap.put(URL.class, Rdf.xsd + "anyURI");
    typeMap.put(Date.class, Rdf.xsd + "dateTime");
  }

  /**
   * Create an appropriate mapper for the given java field.
   *
   * @param f the field
   * @param top the class where this field belongs (may not be the declaring class)
   * @param ns the rdf name space or null to use to build an rdf predicate uri
   *
   * @return a mapper or null if the field is static or transient
   *
   * @throws OtmException if a mapper can't be created
   */
  public static Collection<?extends Mapper> create(Field f, Class top, String ns)
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
    Rdf     rdf      = (Rdf) f.getAnnotation(Rdf.class);
    Id      id       = (Id) f.getAnnotation(Id.class);
    boolean embedded =
      (f.getAnnotation(Embedded.class) != null) || (type.getAnnotation(Embeddable.class) != null);

    String  uri      = (rdf != null) ? rdf.value() : ((ns != null) ? (ns + f.getName()) : null);

    if (id != null) {
      if (!type.equals(String.class) && !type.equals(URI.class) && !type.equals(URL.class))
        throw new OtmException("@Id field '" + f.toGenericString()
                               + "' must be a String, URI or URL.");

      Serializer serializer = SerializerFactory.getSerializer(type, null);

      return Collections.singletonList(new FunctionalMapper(null, f, getMethod, setMethod,
                                                            serializer, null));
    }

    if (!embedded && (uri == null))
      throw new OtmException("Missing @Rdf for field '" + f.toGenericString() + "' in "
                             + f.getDeclaringClass());

    boolean isArray      = type.isArray();
    boolean isCollection = Collection.class.isAssignableFrom(type);

    type                 = isArray ? type.getComponentType() : (isCollection ? collectionType(f)
                                                                : type);

    DataType dta         = f.getAnnotation(DataType.class);
    String   dt          = (dta == null) ? typeMap.get(type) : dta.value();

    if (DataType.UNTYPED.equals(dt))
      dt = null;

    Serializer serializer = SerializerFactory.getSerializer(type, dt);

    if (log.isDebugEnabled() && (serializer == null))
      log.debug("No serializer found for " + type);

    if (!embedded) {
      Mapper p;

      if (isArray)
        p = new ArrayMapper(uri, f, getMethod, setMethod, serializer, type, dt);
      else if (isCollection)
        p = new CollectionMapper(uri, f, getMethod, setMethod, serializer, type, dt);
      else
        p = new FunctionalMapper(uri, f, getMethod, setMethod, serializer, dt);

      return Collections.singletonList(p);
    }

    if (isArray || isCollection || (serializer != null))
      throw new OtmException("@Embedded class field '" + f.toGenericString()
                             + "' can't be an array, collection or a simple field");

    ClassMetadata cm;

    try {
      cm = new ClassMetadata(type, ns);
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
}
