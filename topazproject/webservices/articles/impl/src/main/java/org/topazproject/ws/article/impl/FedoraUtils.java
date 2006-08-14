/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.ws.article.impl;

import java.rmi.RemoteException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.ws.article.DuplicateIdException;
import org.topazproject.ws.article.NoSuchIdException;

/** 
 * Some utilities for dealing with Fedora.
 * 
 * @author Ronald Tschalär
 */
class FedoraUtil {
  private static final Log log = LogFactory.getLog(FedoraUtil.class);

  /** 
   * Not mean to be instantiated. 
   */
  private FedoraUtil() {
  }

  /** 
   * See if the given exception indicates that the fedora object does not exist, and if so generate
   * a NoSuchIdException; otherwise just rethrow the original exception. This method never
   * returns normally.
   * 
   * @param re  the RemoteException to analyze
   * @param id  the id to put in the NoSuchIdException
   * @throws NoSuchIdException if <var>re</var> indicates that the fedora object doesn't exist
   * @throws RemoteException for all other exceptions; this is just <var>re</var>
   */
  public static void detectNoSuchIdException(RemoteException re, String id)
      throws NoSuchIdException, RemoteException {
    if (re.getMessage().startsWith("fedora.server.errors.ObjectNotInLowlevelStorageException")) {
      if (log.isDebugEnabled())
        log.debug("tried to modify non-existing object", re);
      throw new NoSuchIdException(id);
    }

    throw re;
  }

  /** 
   * See if the given exception indicates that the fedora object already exists, and if so generate
   * a DuplicateIdException; otherwise just rethrow the original exception. This method never
   * returns normally.
   * 
   * @param re  the RemoteException to analyze
   * @param id  the id to put in the DuplicateIdException
   * @throws DuplicateIdException if <var>re</var> indicates that the fedora object already exists
   * @throws RemoteException for all other exceptions; this is just <var>re</var>
   */
  public static void detectDuplicateIdException(RemoteException re, String id)
      throws DuplicateIdException, RemoteException {
    if (re.getMessage().startsWith("fedora.server.errors.ObjectExistsException")) {
      if (log.isDebugEnabled())
        log.debug("tried to create duplicate object", re);
      throw new DuplicateIdException(id);
    }

    throw re;
  }
}
