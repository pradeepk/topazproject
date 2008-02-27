/* $HeadURL:: $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.doi;


import java.net.URI;

import java.util.ArrayList;
import java.util.List;

import org.topazproject.otm.OtmException;
import org.topazproject.otm.RdfUtil;
import org.topazproject.otm.Session;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.Transaction;
import org.topazproject.otm.impl.SessionFactoryImpl;
import org.topazproject.otm.stores.ItqlStore;
import org.topazproject.otm.query.Results;

/**
 * Resolver for the rdf:type of a DOI-URI.
 *
 * @author Pradeep Krishnan
 */
public class DOITypeResolver {
  private static final String MODEL = "<rmi://localhost/topazproject#filter:model=ri>";
  private static final String QUERY = "select $t from " + MODEL + " where <${doi}> <rdf:type> $t";


  //
  private final SessionFactory sf;

  /**
   * Creates a new DOITypeResolver object.
   *
   * @param mulgaraUri the mulgara service uri
   *
   * @throws OtmException if an error occurred talking to the web-service
   */
  public DOITypeResolver(URI mulgaraUri) throws OtmException {
    sf = new SessionFactoryImpl();
    sf.setTripleStore(new ItqlStore(mulgaraUri));
  }

  /**
   * Queries the ITQL database and returns all known rdf:type values of a URI.
   *
   * @param doi the doi uri
   *
   * @return returns an array of rdf:types
   *
   * @throws OtmException if an exception occurred talking to the service
   */
  public String[] getRdfTypes(URI doi) throws OtmException {
    String query = RdfUtil.bindValues(QUERY, "doi", doi.toString());

    List<String> types = new ArrayList<String>();
    Session sess = sf.openSession();
    try {
      Transaction tx = sess.beginTransaction();
      try {
        Results results = sess.doNativeQuery(query);
        results.beforeFirst();
        while (results.next())
          types.add(results.getString(0));
      } finally {
        tx.commit();
      }
    } finally {
      sess.close();
    }

    return types.toArray(new String[types.size()]);
  }
}
