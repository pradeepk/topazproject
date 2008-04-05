/* $HeadURL::                                                                            $
 * $Id$
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
package org.topazproject.otm.serializer;

import java.lang.reflect.Constructor;

/**
 * A simple serializer that uses the toString() method to serialize and uses a constructor that
 * takes a String to deserialize. This works very well with basic java data-types.
 *
 * @author Pradeep Krishnan
 *
 * @param <T> the class that the serializer works on.
 */
public class SimpleSerializer<T> implements Serializer<T> {
  private final Constructor<T> constructor;
  private final String         name;

  /**
   * Creates a new SimpleSerializer object.
   *
   * @param clazz the class to serialize/deserialize
   */
  public SimpleSerializer(Class<T> clazz) {
    try {
      constructor = clazz.getConstructor(String.class);
    } catch (NoSuchMethodException t) {
      throw new IllegalArgumentException("Must have a constructor that takes a String", t);
    }

    String  n = getClass().getName();
    Package p = getClass().getPackage();

    if (p != null)
      n = n.substring(p.getName().length() + 1);

    name = n;
  }

  /*
   * inherited javadoc
   */
  public String serialize(T o) throws Exception {
    return (o == null) ? null : o.toString();
  }

  /*
   * inherited javadoc
   */
  public T deserialize(String o, Class<T> c) throws Exception {
    return constructor.newInstance(o);
  }

  /*
   * inherited javadoc
   */
  public String toString() {
    return name + "[" + constructor.getDeclaringClass().getName() + "]";
  }
}
