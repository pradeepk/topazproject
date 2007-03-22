package org.topazproject.otm.mapping;

import java.lang.reflect.Constructor;

import java.net.URI;
import java.net.URL;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * A factory for creating serializers for basic java types.
 *
 * @author Pradeep Krishnan
 */
public class SerializerFactory {
  private static Map<Class, Serializer> serializers = new HashMap<Class, Serializer>();

  static {
    serializers.put(String.class, new SimpleSerializer<String>(String.class));
    serializers.put(Integer.class, new SimpleSerializer<Integer>(Integer.class));
    serializers.put(Integer.TYPE, new SimpleSerializer<Integer>(Integer.class));
    serializers.put(Long.class, new SimpleSerializer<Long>(Long.class));
    serializers.put(Long.TYPE, new SimpleSerializer<Long>(Long.class));
    serializers.put(Short.class, new SimpleSerializer<Short>(Short.class));
    serializers.put(Short.TYPE, new SimpleSerializer<Short>(Short.class));
    serializers.put(Float.class, new SimpleSerializer<Float>(Float.class));
    serializers.put(Float.TYPE, new SimpleSerializer<Float>(Float.class));
    serializers.put(Double.class, new SimpleSerializer<Double>(Double.class));
    serializers.put(Double.TYPE, new SimpleSerializer<Double>(Double.class));
    serializers.put(Byte.class, new SimpleSerializer<Byte>(Byte.class));
    serializers.put(Byte.TYPE, new SimpleSerializer<Byte>(Byte.class));
    serializers.put(URI.class, new SimpleSerializer<URI>(URI.class));
    serializers.put(URL.class, new SimpleSerializer<URL>(URL.class));
    serializers.put(Date.class, new SimpleSerializer<Date>(Date.class));
  }

  /**
   * Get the serializer for a class.
   *
   * @param clazz the class
   *
   * @return a serializer or null
   */
  public static Serializer getSerializer(Class clazz) {
    return serializers.get(clazz);
  }

  private static class SimpleSerializer<T> implements Serializer {
    private Constructor<T> constructor;

    public SimpleSerializer(Class<T> clazz) {
      try {
        constructor = clazz.getConstructor(String.class);
      } catch (NoSuchMethodException t) {
        throw new IllegalArgumentException("Must have a constructor that takes a String", t);
      }
    }

    public String serialize(Object o) throws Exception {
      return (o == null) ? null : o.toString();
    }

    public Object deserialize(String o) throws Exception {
      return constructor.newInstance(o);
    }

    public String toString() {
      return "SimpleSerializer[" + constructor.getDeclaringClass().getName() + "]";
    }
  }

  private SerializerFactory() {
  }
}
