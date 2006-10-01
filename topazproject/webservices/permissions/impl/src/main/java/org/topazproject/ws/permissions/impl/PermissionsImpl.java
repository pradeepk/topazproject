/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.ws.permissions.impl;

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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.common.impl.TopazContext;
import org.topazproject.common.impl.TopazContextListener;

import org.topazproject.configuration.ConfigurationStore;

import org.topazproject.mulgara.itql.AnswerException;
import org.topazproject.mulgara.itql.ItqlHelper;
import org.topazproject.mulgara.itql.StringAnswer;

import org.topazproject.ws.permissions.Permissions;

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
  private static final String GRANTS_MODEL_TYPE =
    "<" + CONF.getString("topaz.models.grants[@type]", "http://tucana.org/tucana#Model") + ">";
  private static final String REVOKES_MODEL_TYPE =
    "<" + CONF.getString("topaz.models.revokes[@type]", "http://tucana.org/tucana#Model") + ">";

  //
  private static final String ITQL_LIST =
    "select $p from ${MODEL} where <${resource}> $p <${principal}>;";

  //
  private final TopazContext   ctx;
  private final PermissionsPEP pep;

  /**
   * Create a new permission instance.
   *
   * @param pep the policy-enforcer to use for access-control
   * @param ctx the topaz context
   */
  public PermissionsImpl(PermissionsPEP pep, TopazContext ctx) {
    this.ctx   = ctx;
    this.pep   = pep;

    ctx.addListener(new TopazContextListener() {
        public void handleCreated(TopazContext ctx, Object handle) {
          if (handle instanceof ItqlHelper) {
            ItqlHelper itql = (ItqlHelper) handle;

            // clear all since we want un-aliased uris always
            itql.getAliases().clear();

            try {
              itql.doUpdate("create " + GRANTS_MODEL + " " + GRANTS_MODEL_TYPE + ";");
              itql.doUpdate("create " + REVOKES_MODEL + " " + REVOKES_MODEL_TYPE + ";");
            } catch (IOException e) {
              log.warn("failed to create grants and revokes models", e);
            }
          }
        }
      });
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
    String user = ctx.getUserName();
    permissions = validateUriList(permissions, "permissions", false);

    if ((principals == null) || (principals.length == 0))
      principals = new String[] { user };
    else
      principals = validateUriList(principals, "principals", true);

    pep.checkAccess(action, ItqlHelper.validateUri(resource, "resource"));

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

    ctx.getItqlHelper().doUpdate(cmd);

    if (log.isInfoEnabled()) {
      log.info(action + " succeeded for resource " + resource + "\npermissions:\n"
               + Arrays.asList(permissions) + "\nprincipals:\n" + Arrays.asList(principals));
    }
  }

  private String[] listPermissions(String action, String model, String resource, String principal)
                            throws RemoteException {
    if (principal == null)
      principal = ctx.getUserName();
    else
      ItqlHelper.validateUri(principal, "principal");

    pep.checkAccess(action, ItqlHelper.validateUri(resource, "resource"));

    try {
      HashMap map = new HashMap(3);
      map.put("resource", resource);
      map.put("principal", principal);
      map.put("MODEL", model);

      String       query = ItqlHelper.bindValues(ITQL_LIST, map);

      StringAnswer ans  = new StringAnswer(ctx.getItqlHelper().doQuery(query));
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
        ItqlHelper.validateUri(list[i], name);
      else if (!nullOk)
        throw new NullPointerException(name + " can't be null");
    }

    return list;
  }
}
