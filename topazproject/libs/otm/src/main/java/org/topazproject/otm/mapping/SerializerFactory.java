package org.topazproject.otm.mapping;

import java.lang.reflect.Constructor;

import java.net.URI;
import java.net.URL;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.annotations.DataType;
import org.topazproject.otm.annotations.Rdf;

/**
 * A factory for creating serializers for basic java types.
 *
 * @author Pradeep Krishnan
 */
public class SerializerFactory {
  private Map<Class, Map<String, Serializer>> serializers;
  private SessionFactory                      sf;
  private static Map<Class, String>           typeMap = new HashMap<Class, String>();

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
   * Creates a new SerializerFactory object.
   *
   * @param sf DOCUMENT ME!
   */
  public SerializerFactory(SessionFactory sf) {
    this.sf       = sf;
    serializers   = new HashMap<Class, Map<String, Serializer>>();

    initDefaults();
  }

  private void initDefaults() {
    DateBuilder<Date> dateDateBuilder =
      new DateBuilder<Date>() {
        public Date toDate(Date o) {
          return o;
        }

        public Date fromDate(Date d) {
          return d;
        }
      };

    DateBuilder<Long> longDateBuilder =
      new DateBuilder<Long>() {
        public Date toDate(Long o) {
          return new Date(o.longValue());
        }

        public Long fromDate(Date d) {
          return new Long(d.getTime());
        }
      };

    setSerializer(String.class, new SimpleSerializer<String>(String.class));
    setSerializer(Boolean.class, new XsdBooleanSerializer());
    setSerializer(Boolean.TYPE, new XsdBooleanSerializer());
    setSerializer(Integer.class, new SimpleSerializer<Integer>(Integer.class));
    setSerializer(Integer.TYPE, new SimpleSerializer<Integer>(Integer.class));
    setSerializer(Long.class, new SimpleSerializer<Long>(Long.class));
    setSerializer(Long.TYPE, new SimpleSerializer<Long>(Long.class));
    setSerializer(Short.class, new SimpleSerializer<Short>(Short.class));
    setSerializer(Short.TYPE, new SimpleSerializer<Short>(Short.class));
    setSerializer(Float.class, new SimpleSerializer<Float>(Float.class));
    setSerializer(Float.TYPE, new SimpleSerializer<Float>(Float.class));
    setSerializer(Double.class, new SimpleSerializer<Double>(Double.class));
    setSerializer(Double.TYPE, new SimpleSerializer<Double>(Double.class));
    setSerializer(Byte.class, new SimpleSerializer<Byte>(Byte.class));
    setSerializer(Byte.TYPE, new SimpleSerializer<Byte>(Byte.class));
    setSerializer(URI.class, new SimpleSerializer<URI>(URI.class));
    setSerializer(URL.class, new SimpleSerializer<URL>(URL.class));
    setSerializer(Date.class, new XsdDateTimeSerializer<Date>(dateDateBuilder, Rdf.xsd + "dateTime"));

    setSerializer(Date.class, Rdf.xsd + "dateTime",
                  new XsdDateTimeSerializer<Date>(dateDateBuilder, Rdf.xsd + "dateTime"));
    setSerializer(Date.class, Rdf.xsd + "date",
                  new XsdDateTimeSerializer<Date>(dateDateBuilder, Rdf.xsd + "date"));
    setSerializer(Date.class, Rdf.xsd + "time",
                  new XsdDateTimeSerializer<Date>(dateDateBuilder, Rdf.xsd + "time"));

    setSerializer(Long.class, Rdf.xsd + "dateTime",
                  new XsdDateTimeSerializer<Long>(longDateBuilder, Rdf.xsd + "dateTime"));
    setSerializer(Long.class, Rdf.xsd + "date",
                  new XsdDateTimeSerializer<Long>(longDateBuilder, Rdf.xsd + "date"));
    setSerializer(Long.class, Rdf.xsd + "time",
                  new XsdDateTimeSerializer<Long>(longDateBuilder, Rdf.xsd + "time"));

    setSerializer(Long.TYPE, Rdf.xsd + "dateTime",
                  new XsdDateTimeSerializer<Long>(longDateBuilder, Rdf.xsd + "dateTime"));
    setSerializer(Long.TYPE, Rdf.xsd + "date",
                  new XsdDateTimeSerializer<Long>(longDateBuilder, Rdf.xsd + "date"));
    setSerializer(Long.TYPE, Rdf.xsd + "time",
                  new XsdDateTimeSerializer<Long>(longDateBuilder, Rdf.xsd + "time"));
  }

  /**
   * DOCUMENT ME!
   *
   * @param clazz DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public static String getDefaultDataType(Class clazz) {
    return typeMap.get(clazz);
  }

  /**
   * Get the serializer for a class.
   *
   * @param clazz the class
   * @param dataType DOCUMENT ME!
   *
   * @return a serializer or null
   */
  public Serializer getSerializer(Class clazz, String dataType) {
    if (dataType == null)
      dataType = DataType.UNTYPED;

    Map<String, Serializer> m = serializers.get(clazz);

    return (m != null) ? m.get(dataType) : null;
  }

  /**
   * DOCUMENT ME!
   *
   * @param clazz DOCUMENT ME!
   * @param dataType DOCUMENT ME!
   * @param serializer DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public Serializer setSerializer(Class clazz, String dataType, Serializer serializer) {
    if (dataType == null)
      dataType = DataType.UNTYPED;

    Map<String, Serializer> m = serializers.get(clazz);

    if (m == null)
      serializers.put(clazz, m = new HashMap<String, Serializer>());

    return m.put(dataType, serializer);
  }

  /**
   * DOCUMENT ME!
   *
   * @param clazz DOCUMENT ME!
   * @param serializer DOCUMENT ME!
   */
  public void setSerializer(Class clazz, Serializer serializer) {
    String dataType = typeMap.get(clazz);

    if (dataType != null)
      setSerializer(clazz, dataType, serializer);

    setSerializer(clazz, null, serializer);
  }

  private class SimpleSerializer<T> implements Serializer<T> {
    private Constructor<T> constructor;

    public SimpleSerializer(Class<T> clazz) {
      try {
        constructor = clazz.getConstructor(String.class);
      } catch (NoSuchMethodException t) {
        throw new IllegalArgumentException("Must have a constructor that takes a String", t);
      }
    }

    public String serialize(T o) throws Exception {
      return (o == null) ? null : o.toString();
    }

    public T deserialize(String o) throws Exception {
      return constructor.newInstance(o);
    }

    public String toString() {
      return "SimpleSerializer[" + constructor.getDeclaringClass().getName() + "]";
    }
  }

  private static interface DateBuilder<T> {
    public Date toDate(T o);

    public T fromDate(Date d);
  }

  private static class XsdDateTimeSerializer<T> implements Serializer<T> {
    private SimpleDateFormat sparser;
    private SimpleDateFormat zparser;
    private SimpleDateFormat fmt;
    private DateBuilder<T>   dateBuilder;

    public XsdDateTimeSerializer(DateBuilder dateBuilder, String dataType) {
      this.dateBuilder = dateBuilder;

      if ((Rdf.xsd + "date").equals(dataType)) {
        zparser   = new SimpleDateFormat("yyyy-MM-ddZ");
        sparser   = new SimpleDateFormat("yyyy-MM-dd");
        fmt       = new SimpleDateFormat("yyyy-MM-dd'Z'");
      } else if ((Rdf.xsd + "dateTime").equals(dataType)) {
        zparser   = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.'SSSZ");
        sparser   = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.'SSS");
        fmt       = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.'SSS'Z'");
      } else if ((Rdf.xsd + "time").equals(dataType)) {
        zparser   = new SimpleDateFormat("HH:mm:ss'.'SSSZ");
        sparser   = new SimpleDateFormat("HH:mm:ss'.'SSS");
        fmt       = new SimpleDateFormat("HH:mm:ss'.'SSS'Z'");
      } else {
        throw new IllegalArgumentException("Data type must be an xsd:date, xsd:time or xsd:dateTime");
      }

      fmt.setTimeZone(new SimpleTimeZone(0, "UTC"));
      sparser.setLenient(false);
      zparser.setLenient(false);
    }

    public String serialize(T o) throws Exception {
      synchronized (fmt) {
        return (o == null) ? null : fmt.format(dateBuilder.toDate(o));
      }
    }

    public T deserialize(String o) throws Exception {
      if (o == null)
        return null;

      if (o.endsWith("Z"))
        o = o.substring(0, o.length() - 1) + "+00:00";

      int     len         = o.length();
      boolean hasTimeZone =
        ((o.charAt(len - 3) == ':') && ((o.charAt(len - 6) == '-') || (o.charAt(len - 6) == '+')));

      int     pos         = o.indexOf('.');
      String  mss;
      int     endPos;

      if (pos == -1) {
        mss               = ".000";
        pos               = hasTimeZone ? (len - 6) : len;
        endPos            = pos;
      } else {
        // convert fractional seconds to number of milliseconds
        endPos   = hasTimeZone ? (len - 6) : len;
        mss      = o.substring(pos, endPos);

        while (mss.length() < 4)
          mss += "0";

        if (mss.length() > 4)
          mss = mss.substring(0, 4);
      }

      o = o.substring(0, pos) + mss + o.substring(endPos, len);

      SimpleDateFormat parser = hasTimeZone ? zparser : sparser;

      synchronized (parser) {
        return dateBuilder.fromDate(parser.parse(o));
      }
    }

    public String toString() {
      return "XsdDateTimeSerializer";
    }
  }

  private static class XsdBooleanSerializer implements Serializer<Boolean> {
    public String serialize(Boolean o) throws Exception {
      return o.toString();
    }

    public Boolean deserialize(String o) throws Exception {
      if ("1".equals(o) || "true".equals(o))
        return Boolean.TRUE;

      if ("0".equals(o) || "false".equals(o))
        return Boolean.FALSE;

      throw new IllegalArgumentException("invalid xsd:boolean '" + o + "'");
    }
  }
}
