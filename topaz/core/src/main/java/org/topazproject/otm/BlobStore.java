/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm;

/**
 * An abstraction to represent blob stores.
 *
 * @author Pradeep Krishnan
 */
public interface BlobStore extends Store {
  /**
   * Persists an object in the blob store.
   *
   * @param id the id for the blob
   * @param blob the blob to store
   * @param txn the transaction context
   *
   * @throws OtmException on an error
   */
  public void insert(String id, byte[] blob, Transaction txn) throws OtmException;

  /**
   * Removes an object from the blob store.
   *
   * @param id the id for the blob
   * @param txn the transaction context
   *
   * @throws OtmException on an error
   */
  public void delete(String id, Transaction txn) throws OtmException;

  /**
   * Gets a blob from the blob store.
   *
   * @param id the id for the blob
   * @param txn the transaction context
   *
   * @return the blob or null if it does not exist
   *
   * @throws OtmException on an error
   */
  public byte[] get(String id, Transaction txn) throws OtmException;

}
