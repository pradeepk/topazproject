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


import java.net.MalformedURLException;
import java.net.URI;

import java.rmi.RemoteException;

import java.util.ArrayList;
import java.util.List;

import javax.xml.rpc.ServiceException;

import org.topazproject.mulgara.itql.AnswerException;
import org.topazproject.mulgara.itql.AnswerSet;
import org.topazproject.mulgara.itql.ItqlHelper;
import org.topazproject.mulgara.itql.service.ItqlInterpreterException;

/**
 * Resolver for the rdf:type of a DOI-URI.
 *
 * @author Pradeep Krishnan
 */
public class DOITypeResolver {
  private static final String MODEL = "<local:///topazproject#filter:model=ri>";
  private static final String QUERY = "select $t from " + MODEL + " where <${doi}> <rdf:type> $t";

  
  //
  private ItqlHelper itql;

  /**
   * Creates a new DOITypeResolver object.
   *
   * @param mulgaraUri the mulgara service uri
   *
   * @throws MalformedURLException if service's uri is not a valid URL
   * @throws ServiceException if an error occurred locating the web-service
   * @throws RemoteException if an error occurred talking to the web-service
   */
  public DOITypeResolver(URI mulgaraUri)
                  throws MalformedURLException, ServiceException, RemoteException {
    itql = new ItqlHelper(mulgaraUri);
  }

  /**
   * Queries the ITQL database and returns all known rdf:type values of a URI.
   *
   * @param doi the doi uri
   *
   * @return returns an array of rdf:types
   *
   * @throws ItqlInterpreterException if an exception was encountered while processing the queries
   * @throws AnswerException if an exception occurred parsing the query response
   * @throws RemoteException if an exception occurred talking to the service
   */
  public String[] getRdfTypes(URI doi)
                       throws ItqlInterpreterException, AnswerException, RemoteException {
    String query = ItqlHelper.bindValues(QUERY, "doi", doi.toString());

    String results;

    synchronized (this) {
      results = itql.doQuery(query, null);
    }

    AnswerSet answer = new AnswerSet(results);
    answer.beforeFirst();
    if (!answer.next())
      return new String[0];

    List<String> types = new ArrayList<String>();
    AnswerSet.QueryAnswerSet qas = answer.getQueryResults();
    while (qas.next())
      types.add(qas.getString(0));

    return types.toArray(new String[types.size()]);
  }

  /**
   * Close the database handle.
   *
   * @throws Throwable on an error
   */
  protected void finalize() throws Throwable {
    try {
      itql.close();
    } catch (Throwable t) {
    }

    itql = null;

    super.finalize();
  }
}
