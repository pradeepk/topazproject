/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2006-2008 by Topaz, Inc.
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
package org.topazproject.mulgara.resolver.stringcompare;

import org.mulgara.store.stringpool.SPObject;

/**
 * Base class for string comparison functions.
 *
 * The important functions are getOp() and test(). They're pretty clearly
 * documented.<p>
 *
 * The lowValue(), highValue(), incLowValue() and incHighValue() kind of
 * go together and are used in the call to
 * L{org.mulgara.resolver.spi.ResolverSession}.findStringPoolRange.
 * It might also just be best to read the code in StringCompareResolver
 * to see how their used. High level: you need to specify all the tuples
 * that might be in a given range and specifying these 4 values does that.
 *
 * @author Eric Brown
 */
abstract class StringCompareImpl {
  /** The preallocated node (stashed here by StringCompareResolverFactory) */
  private long node;

  /** The name of the operation we're implementing. */
  abstract String  getOp();

  /**
   * The test to see if a value passed.
   *
   * @param spo  The new spo to check against
   * @param comp The value from the query to compare against
   */
  abstract boolean test(SPObject spo, String comp);

  /** Should we filter tuples returned by low/highValue stuff? */
  boolean doFilter() { return false; }

  /** Return start of range of valid values (null means lowest possible value). */
  String  lowValue(String comp)  { return null; }

  /** Return end of range of possible valid values (null means highest possible value). */
  String  highValue(String comp) { return null; }

  /** true if range includes lowValue() above */
  boolean incLowValue()  { return true; }

  /** true if range includes highValue() above */
  boolean incHighValue() { return true; }

  void    setNode(long node) { this.node = node; }
  long    getNode() { return node; }

  /** Return the implementation for reversed arguments */
  abstract StringCompareImpl getOpposite();
}
