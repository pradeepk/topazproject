/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.ws.permissions;

import java.io.IOException;

import java.net.URI;

import java.rmi.RemoteException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.xml.rpc.ServiceException;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.authentication.ProtectedService;
import org.topazproject.authentication.UnProtectedService;

import org.topazproject.configuration.ConfigurationStore;

import org.topazproject.mulgara.itql.AnswerException;
import org.topazproject.mulgara.itql.ItqlHelper;
import org.topazproject.mulgara.itql.StringAnswer;

/**
 * This provides the implementation of the permissions service.
 * 
 * <p>
 * Grants and Revokes are stored in a seperate models with 1 triple per permission like this:
 * <pre>
 * &lt;${resource}&gt; &lt;${permission}&gt; &lt;${principal}&gt;
 * </pre>
 * </p>
 *
 * @author Pradeep Krishnan
 */
public class PermissionsImpl implements Permissions {
  private static final Log log = LogFactory.getLog(PermissionsImpl.class);

  //
  private static final Configuration CONF = ConfigurationStore.getInstance().getConfiguration();

  //
  private static final String GRANTS_MODEL  = "<" + CONF.getString("topaz.models.grants") + ">";
  private static final String REVOKES_MODEL = "<" + CONF.getString("topaz.models.revokes") + ">";

  //
  private static final String ITQL_LIST =
    "select $p from ${MODEL} where <${resource}> $p <${principal}>;";

  //
  private final ItqlHelper     itql;
  private final PermissionsPEP pep;
  private final String         user;

  /**
   * Create a new permission instance.
   *
   * @param itql the mulgara itql-service
   * @param pep the policy-enforcer to use for access-control
   * @param user the user that is performing the operations
   *
   * @throws IOException if an error occurred initializing the itql service
   */
  public PermissionsImpl(ItqlHelper itql, PermissionsPEP pep, String user)
                  throws IOException {
    this.itql   = itql;
    this.pep    = pep;
    this.user   = user;

    // clear all since we want un-aliased uris always
    itql.getAliases().clear();
    itql.doUpdate("create " + GRANTS_MODEL + ";");
    itql.doUpdate("create " + REVOKES_MODEL + ";");
  }

  /**
   * Create a new permissions instance.
   *
   * @param mulgaraSvc the mulgara web-service
   * @param pep the policy-enforcer to use for access-control
   * @param user the user that is performing the operations
   *
   * @throws IOException if an error occurred talking to the mulgara service
   * @throws ServiceException if an error occurred locating the mulgara service
   * @throws ConfigurationException if any required config is missing
   */
  public PermissionsImpl(ProtectedService mulgaraSvc, PermissionsPEP pep, String user)
                  throws IOException, ServiceException, ConfigurationException {
    this(new ItqlHelper(mulgaraSvc), pep, user);
  }

  /**
   * Create a new permission accounts manager instance.
   *
   * @param mulgaraUri the uri of the mulgara server
   * @param pep the policy-enforcer to use for access-control
   * @param user the user that is performing the operations
   *
   * @throws IOException if an error occurred talking to the fedora service
   * @throws ServiceException if an error occurred locating the fedora service
   * @throws ConfigurationException DOCUMENT ME!
   */
  public PermissionsImpl(URI mulgaraUri, PermissionsPEP pep, String user)
                  throws IOException, ServiceException, ConfigurationException {
    this(new UnProtectedService(mulgaraUri.toString()), pep, user);
  }

  /*
   * @see org.topazproject.ws.permissions.Permission#grant
   */
  public void grant(String resource, String[] permissions, String[] principals)
             throws RemoteException {
    updateModel(pep.GRANT, GRANTS_MODEL, resource, permissions, principals, true);
  }

  /*
   * @see org.topazproject.ws.permissions.Permission#revoke
   */
  public void revoke(String resource, String[] permissions, String[] principals)
              throws RemoteException {
    updateModel(pep.REVOKE, REVOKES_MODEL, resource, permissions, principals, true);
  }

  /*
   * @see org.topazproject.ws.permissions.Permission#cancleGrants
   */
  public void cancelGrants(String resource, String[] permissions, String[] principals)
                    throws RemoteException {
    updateModel(pep.CANCEL_GRANTS, GRANTS_MODEL, resource, permissions, principals, false);
  }

  /*
   * @see org.topazproject.ws.permissions.Permission#cancelRevokes
   */
  public void cancelRevokes(String resource, String[] permissions, String[] principals)
                     throws RemoteException {
    updateModel(pep.CANCEL_REVOKES, REVOKES_MODEL, resource, permissions, principals, false);
  }

  /*
   * @see org.topazproject.ws.permissions.Permission#listGrants
   */
  public String[] listGrants(String resource, String principal)
                      throws RemoteException {
    return listPermissions(pep.LIST_GRANTS, GRANTS_MODEL, resource, principal);
  }

  /*
   * @see org.topazproject.ws.permissions.Permission#listRevokes
   */
  public String[] listRevokes(String resource, String principal)
                       throws RemoteException {
    return listPermissions(pep.LIST_REVOKES, REVOKES_MODEL, resource, principal);
  }

  private void updateModel(String action, String model, String resource, String[] permissions,
                           String[] principals, boolean insert)
                    throws RemoteException {
    permissions = validateUriList(permissions, "permissions", false);

    if ((principals == null) || (principals.length == 0))
      principals = new String[] { user };
    else
      principals = validateUriList(principals, "principals", true);

    pep.checkAccess(action, itql.validateUri(resource, "resource"));

    StringBuffer sb = new StringBuffer(512);

    for (int i = 0; i < principals.length; i++) {
      String principal = principals[i];

      if (principal == null)
        principal = user;

      for (int j = 0; j < permissions.length; j++) {
        sb.append("<").append(resource).append("> ");
        sb.append("<").append(permissions[j]).append("> ");
        sb.append("<").append(principal).append("> ");
      }
    }

    String triples = sb.toString();

    String cmd = "delete " + triples + " from " + model + ";";

    if (insert)
      cmd += ("insert " + triples + " into " + model + ";");

    itql.doUpdate(cmd);

    if (log.isInfoEnabled()) {
      log.info(action + " succeeded for resource " + resource + "\npermissions:\n"
               + Arrays.asList(permissions) + "\nprincipals:\n" + Arrays.asList(principals));
    }
  }

  private String[] listPermissions(String action, String model, String resource, String principal)
                            throws RemoteException {
    if (principal == null)
      principal = user;
    else
      itql.validateUri(principal, "principal");

    pep.checkAccess(action, itql.validateUri(resource, "resource"));

    try {
      HashMap map = new HashMap(3);
      map.put("resource", resource);
      map.put("principal", principal);
      map.put("MODEL", model);

      String       query = itql.bindValues(ITQL_LIST, map);

      StringAnswer ans  = new StringAnswer(itql.doQuery(query));
      List         rows = ((StringAnswer.StringQueryAnswer) ans.getAnswers().get(0)).getRows();

      String[]     result = new String[rows.size()];

      for (int i = 0; i < result.length; i++)
        result[i] = ((String[]) rows.get(i))[0];

      return result;
    } catch (AnswerException ae) {
      throw new RemoteException("Error listing permissions for resource '" + resource
                                + "' and principal '" + principal + "'", ae);
    }
  }

  private String[] validateUriList(String[] list, String name, boolean nullOk) {
    if (list == null)
      throw new NullPointerException(name + " list can't be null");

    if (list.length == 0)
      throw new IllegalArgumentException(name + " list can't be empty");

    // eliminate duplicates
    list   = (String[]) (new HashSet(Arrays.asList(list))).toArray(new String[0]);

    name = name + " list item";

    for (int i = 0; i < list.length; i++) {
      if (list[i] != null)
        itql.validateUri(list[i], name);
      else if (!nullOk)
        throw new NullPointerException(name + " can't be null");
    }

    return list;
  }
}
