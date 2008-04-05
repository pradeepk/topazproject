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
package org.plos.article.util;

import java.rmi.RemoteException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Some utilities for dealing with Fedora.
 *
 * @author Ronald Tschalär
 */
class FedoraUtil {
  private static final Log log = LogFactory.getLog(FedoraUtil.class);

  /**
   * Not meant to be instantiated.
   */
  private FedoraUtil() {
  }

  /**
   * See if the given exception indicates that the fedora object does not exist.
   *
   * @param re the RemoteException to analyze
   */
  public static boolean isNoSuchObjectException(RemoteException re) {
    return re.getMessage().startsWith("fedora.server.errors.ObjectNotInLowlevelStorageException");
  }

  /**
   * See if the given exception indicates that the fedora object already exists, and if so generate
   * a DuplicateArticleIdException; otherwise just rethrow the original exception. This method never
   * returns normally.
   *
   * @param re  the RemoteException to analyze
   * @param id  the id to put in the DuplicateArticleIdException
   * @throws DuplicateArticleIdException if <var>re</var> indicates that the fedora object already
   *         exists
   * @throws RemoteException for all other exceptions; this is just <var>re</var>
   */
  public static void detectDuplicateArticleIdException(RemoteException re, String id)
      throws DuplicateArticleIdException, RemoteException {
    if (re.getMessage().startsWith("fedora.server.errors.ObjectExistsException")) {
      if (log.isDebugEnabled())
        log.debug("tried to create duplicate object", re);
      throw new DuplicateArticleIdException(id);
    }

    throw re;
  }
}
