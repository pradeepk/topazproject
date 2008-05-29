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
package org.plos.search.service;

import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Iterator wrapper that caches results such that when position is reset (via <code>goto</code>)
 * cache is used.
 *
 * @author Eric Brown
 * @version $Id$
 */
public class CachingIterator implements Iterator {
  private static final Log log      = LogFactory.getLog(CachingIterator.class);
  private Iterator         delegate;
  private List             cache    = new LinkedList();
  private ListIterator     iter;
  private Object           element  = null;

  /**
   * Create a new CachingIterator based on the underlying delegate.
   */
  public CachingIterator(Iterator delegate) {
    this.delegate = delegate;
    this.iter = cache.listIterator();
  }

  /**
   * Rewind (or fast-forward) to the postion in the delegate iterator indicated by the index.
   *
   * If rewinding, data is read from the cache until there is no more cache. Then data is
   * read from the delegate.
   *
   * If fast-forwarding, the data is read (and cached) from the delegate until the desired
   * index is reached.
   *
   * @param index The position in the cache.
   * @throws IndexOutOfBoundsException if the index is past the end of the delegate.
   */
  public void gotoRecord(int index) {
    if (index > cache.size() && delegate.hasNext() && log.isDebugEnabled())
      log.debug("Skipping from " + cache.size() + " to index " + index);

    while (index > cache.size() && delegate.hasNext()) {
      cache.add(delegate.next());
    }

    if (index > cache.size())
      throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + cache.size());

    if (iter.nextIndex() != index) {
      iter = cache.listIterator(index);
      element = null;
      if (log.isDebugEnabled())
        log.debug("Reading from cache of " + cache.size() + " starting at index " + index);
    }
  }

  /**
   * Return data from the cache first and then from the delegate (until there is no more
   * entries in the delegate).
   *
   * @return true if there are more elements.
   */
  public boolean hasNext() {
    if (element != null)
      return true;

    if (iter.hasNext()) {
      element = iter.next();
      return true;
    }

    if (delegate.hasNext()) {
      element = delegate.next();
      iter.add(element);
      return true;
    }

    return false;
  }

  public Object next() {
    if (hasNext()) {
      try {
        return element;
      } finally {
        element = null;
      }
    } else
      throw new NoSuchElementException();
  }

  public void remove() {
    throw new UnsupportedOperationException();
  }

  /**
   * Get the number of items currently in the cache. This can be useful if hasNext()
   * is returning false.
   *
   * @return the number of items currently in the cache
   */
  public int getCurrentSize() {
    return cache.size();
  }
}