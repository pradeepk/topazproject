/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm.mapping;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.topazproject.otm.OtmException;

/**
 * Mapper for a mapper that allows a dynamic/run-time map of properties to values..
 *
 * @author Pradeep Krishnan
 */
public class PredicateMapMapper extends AbstractMapper {
/**
   * Creates a new PredicateMapMapper object.
   *
   * @param field the java class field
   * @param getter the field get method or null
   * @param setter the field set method or null
   */
  public PredicateMapMapper(Field field, Method getter, Method setter) {
    super(null, field, getter, setter, null, null, null, false, null, 
          Mapper.MapperType.PREDICATE_MAP, true);
  }

  /**
   * Retrieve elements from a collection field of an object.
   *
   * @param o the object
   *
   * @return the list of array elements (may be serialized)
   *
   * @throws OtmException if a field's value cannot be retrieved and serialized
   * @throws UnsupportedOperationException DOCUMENT ME!
   */
  public List get(Object o) throws OtmException {
    throw new UnsupportedOperationException("Only raw get/set allowed");
  }

  /**
   * Populate a collection field of an object.
   *
   * @param o the object
   * @param vals the values to be set (may be deserialized)
   *
   * @throws OtmException if a field's value cannot be de-serialized and set
   * @throws UnsupportedOperationException DOCUMENT ME!
   */
  public void set(Object o, List vals) throws OtmException {
    throw new UnsupportedOperationException("Only raw get/set allowed");
  }

  /**
   * DOCUMENT ME!
   *
   * @param o DOCUMENT ME!
   * @param vals DOCUMENT ME!
   *
   * @throws OtmException DOCUMENT ME!
   */
  public void set(Object o, Map vals) throws OtmException {
    Map m = (Map) getRawValue(o, true);
    m.clear();
    m.putAll(vals);
  }
}
