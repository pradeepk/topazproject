/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.fedora.otm;

import org.topazproject.otm.BlobStore;
import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Transaction;
import org.topazproject.otm.id.IdentifierGenerator;

/**
 * An id generator that allocates ids from Fedora.
 *
 * @author Pradeep Krishnan
 */
public class FedoraIdGenerator implements IdentifierGenerator {
  private String prefix;

  /*
   * inherited javadoc
   */
  public String generate(ClassMetadata cm, Transaction txn) throws OtmException {
    BlobStore bs = txn.getSession().getSessionFactory().getBlobStore();

    if (!(bs instanceof FedoraBlobStore))
      throw new OtmException(getClass().getName() + " requires a "
                             + FedoraBlobStore.class.getName() + " to be setup as the BlobStore");

    FedoraBlobStore store = (FedoraBlobStore) bs;

    return store.generateId(cm, prefix, txn);
  }

  /*
   * inherited javadoc
   */
  public void setUriPrefix(String prefix) {
    this.prefix = prefix;
  }
}
