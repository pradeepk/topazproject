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

import org.topazproject.otm.annotations.Rdf;

/**
 * A factory for creating serializers for basic java types.
 *
 * @author Pradeep Krishnan
 */
public class SerializerFactory {
  private static Map<Class, Serializer>              serializers      =
    new HashMap<Class, Serializer>();
  private static Map<Class, Map<String, Serializer>> typedSerializers =
    new HashMap<Class, Map<String, Serializer>>();

  static {
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

    serializers.put(String.class, new SimpleSerializer<String>(String.class));
    serializers.put(Boolean.class, new XsdBooleanSerializer());
    serializers.put(Boolean.TYPE, new XsdBooleanSerializer());
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
    serializers.put(Date.class,
                    new XsdDateTimeSerializer<Date>(dateDateBuilder, Rdf.xsd + "dateTime"));

    Map<String, Serializer> m = new HashMap<String, Serializer>();
    m.put(Rdf.xsd + "dateTime",
          new XsdDateTimeSerializer<Date>(dateDateBuilder, Rdf.xsd + "dateTime"));
    m.put(Rdf.xsd + "date", new XsdDateTimeSerializer<Date>(dateDateBuilder, Rdf.xsd + "date"));
    m.put(Rdf.xsd + "time", new XsdDateTimeSerializer<Date>(dateDateBuilder, Rdf.xsd + "time"));

    typedSerializers.put(Date.class, m);

    m = new HashMap<String, Serializer>();
    m.put(Rdf.xsd + "dateTime",
          new XsdDateTimeSerializer<Long>(longDateBuilder, Rdf.xsd + "dateTime"));
    m.put(Rdf.xsd + "date", new XsdDateTimeSerializer<Long>(longDateBuilder, Rdf.xsd + "date"));
    m.put(Rdf.xsd + "time", new XsdDateTimeSerializer<Long>(longDateBuilder, Rdf.xsd + "time"));

    typedSerializers.put(Long.class, m);
    typedSerializers.put(Long.TYPE, m);
  }

  /**
   * Get the serializer for a class.
   *
   * @param clazz the class
   * @param dataType DOCUMENT ME!
   *
   * @return a serializer or null
   */
  public static Serializer getSerializer(Class clazz, String dataType) {
    Serializer              s = null;
    Map<String, Serializer> m = (dataType == null) ? null : typedSerializers.get(clazz);

    if (m != null)
      s = m.get(dataType);

    if (s == null)
      s = serializers.get(clazz);

    return s;
  }

  private static class SimpleSerializer<T> implements Serializer<T> {
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

  private SerializerFactory() {
  }
}
