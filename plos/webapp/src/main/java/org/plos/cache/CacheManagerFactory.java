/* $HeadURL::                                                                            $
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
package org.plos.cache;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A factory class for CacheManagers that all synchronize using a shared lock.
 *
 * @author Pradeep Krishnan
 */
public class CacheManagerFactory {
  private final Lock updateLock = new ReentrantLock();

  /**
   * Creates a new CacheManager
   *
   * @param lockWaitSeconds number of seconds to wait to acquire a lock
   *
   * @return the newly created CacheManager instance
   */
  public CacheManager createCacheManager(long lockWaitSeconds) {
    return new CacheManager(updateLock, lockWaitSeconds);
  }
}
