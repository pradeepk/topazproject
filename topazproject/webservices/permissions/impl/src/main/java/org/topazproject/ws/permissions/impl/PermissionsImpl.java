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
  private static final String PP_MODEL          = "<" + CONF.getString("topaz.models.pp") + ">";
  private static final String GRANTS_MODEL_TYPE =
    "<" + CONF.getString("topaz.models.grants[@type]", "tucana:Model") + ">";
  private static final String REVOKES_MODEL_TYPE =
    "<" + CONF.getString("topaz.models.revokes[@type]", "tucana:Model") + ">";
  private static final String PP_MODEL_TYPE =
    "<" + CONF.getString("topaz.models.pp[@type]", "tucana:Model") + ">";

  //
  private static final String IMPLIES    = ItqlHelper.TOPAZ_URI + "implies";
  private static final String PROPAGATES = ItqlHelper.TOPAZ_URI + "propagate-permissions-to";

  //
  private static final String ITQL_LIST =
    "select $p from ${MODEL} where <${resource}> $p <${principal}>;";
  private static final String ITQL_LIST_PP =
    "select $o from ${MODEL} where <${s}> <${p}> $o".replaceAll("\\Q${MODEL}", PP_MODEL);
  private static final String ITQL_LIST_PP_TRANS =
    ("select $o from ${MODEL} where <${s}> <${p}> $o "
    + " or (trans($s <${p}> $o) and $s <tucana:is> <${s}>);").replaceAll("\\Q${MODEL}", PP_MODEL);
  private static final String ITQL_INFER_PERMISSION =
    ("select $s from ${PP_MODEL} where $s $p $o in ${MODEL} "
    + "and ($s <tucana:is> <${resource}> or $s <tucana:is> <${ALL}> "
    + "      or $s <${PP}> <${resource}> "
    + "      or (trans($s <${PP}> $res) and $res <tucana:is> <${resource}>)) "
    + "and ($p <tucana:is> <${permission}> or $p <tucana:is> <${ALL}> "
    + "      or $p <${IMPLIES}> <${permission}> "
    + "      or (trans($p <${IMPLIES}> $perm) and $perm <tucana:is> <${permission}>)) "
    + "and ($o <tucana:is> <${principal}> or $o <tucana:is> <${ALL}>)" //
    ).replaceAll("\\Q${PP_MODEL}", PP_MODEL).replaceAll("\\Q${PP}", PROPAGATES)
      .replaceAll("\\Q${IMPLIES}", IMPLIES).replaceAll("\\Q${ALL}", ALL);

  //
  private static boolean initialized = false;

  static private void initialize(ItqlHelper itql) {
    try {
      itql.doUpdate("create " + GRANTS_MODEL + " " + GRANTS_MODEL_TYPE + ";", null);
      itql.doUpdate("create " + REVOKES_MODEL + " " + REVOKES_MODEL_TYPE + ";", null);
      itql.doUpdate("create " + PP_MODEL + " " + PP_MODEL_TYPE + ";", null);
    } catch (IOException e) {
      log.warn("failed to create grants, revokes and pp models", e);
    }

    Configuration conf = CONF.subset("topaz.permissions.impliedPermissions");

    StringBuffer  sb          = new StringBuffer();
    List          permissions = conf.getList("permission[@uri]");
    int           c           = permissions.size();

    for (int i = 0; i < c; i++) {
      List implies = conf.getList("permission(" + i + ").implies[@uri]");
      log.info("config contains " + permissions.get(i) + " implies " + implies);

      for (int j = 0; j < implies.size(); j++) {
        sb.append("<").append(permissions.get(i)).append("> ");
        sb.append("<").append(IMPLIES).append("> ");
        sb.append("<").append(implies.get(j)).append("> ");
      }
    }

    String triples = sb.toString();
    String cmd =
      "delete " + triples + " from " + PP_MODEL + "; insert " + triples + " into " + PP_MODEL + ";";

    String txn = "load implied-permissions from config";

    try {
      itql.beginTxn(txn);
      itql.doUpdate(cmd, null);
      itql.commitTxn(txn);
      txn = null;
    } catch (IOException e) {
      log.warn("failed to store implied permissions loaded from config", e);
    } finally {
      try {
        if (txn != null)
          itql.rollbackTxn(txn);
      } catch (Throwable t) {
      }
    }

    initialized = true;
  }

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

            if (!initialized)
              initialize(itql);
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

  /*
   * @see org.topazproject.ws.permissions.Permission#implyPermission
   */
  public void implyPermissions(String permission, String[] implies)
                        throws RemoteException {
    updatePP(pep.IMPLY_PERMISSIONS, permission, IMPLIES, implies, true);
  }

  /*
   * @see org.topazproject.ws.permissions.Permission#cancelImplyPermission
   */
  public void cancelImplyPermissions(String permission, String[] implies)
                              throws RemoteException {
    updatePP(pep.CANCEL_IMPLY_PERMISSIONS, permission, IMPLIES, implies, false);
  }

  /*
   * @see org.topazproject.ws.permissions.Permission#listImpliedPermissions
   */
  public String[] listImpliedPermissions(String permission, boolean transitive)
                                  throws RemoteException {
    return listPP(pep.LIST_IMPLIED_PERMISSIONS, permission, IMPLIES, transitive);
  }

  /*
   * @see org.topazproject.ws.permissions.Permission#propagatePermissions
   */
  public void propagatePermissions(String resource, String[] to)
                            throws RemoteException {
    updatePP(pep.PROPAGATE_PERMISSIONS, resource, PROPAGATES, to, true);
  }

  /*
   * @see org.topazproject.ws.permissions.Permission#cancelPropagatePermissions
   */
  public void cancelPropagatePermissions(String resource, String[] to)
                                  throws RemoteException {
    updatePP(pep.CANCEL_PROPAGATE_PERMISSIONS, resource, PROPAGATES, to, false);
  }

  /*
   * @see org.topazproject.ws.permissions.Permission#listPermissionPropagations
   */
  public String[] listPermissionPropagations(String resource, boolean transitive)
                                      throws RemoteException {
    return listPP(pep.LIST_PERMISSION_PROPAGATIONS, resource, PROPAGATES, transitive);
  }

  /*
   * @see org.topazproject.ws.permissions.Permission#isGranted
   */
  public boolean isGranted(String resource, String permission, String principal)
                    throws RemoteException {
    return isInferred(GRANTS_MODEL, resource, permission, principal);
  }

  /*
   * @see org.topazproject.ws.permissions.Permission#isGranted
   */
  public boolean isRevoked(String resource, String permission, String principal)
                    throws RemoteException {
    return isInferred(REVOKES_MODEL, resource, permission, principal);
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

    ItqlHelper itql = ctx.getItqlHelper();
    String     txn = action + " on " + resource;

    try {
      itql.beginTxn(txn);
      itql.doUpdate(cmd, null);
      itql.commitTxn(txn);
      txn = null;
    } finally {
      try {
        if (txn != null)
          itql.rollbackTxn(txn);
      } catch (Throwable t) {
      }
    }

    if (log.isInfoEnabled()) {
      log.info(action + " succeeded for resource " + resource + "\npermissions:\n"
               + Arrays.asList(permissions) + "\nprincipals:\n" + Arrays.asList(principals));
    }
  }

  private void updatePP(String action, String subject, String predicate, String[] objects,
                        boolean insert) throws RemoteException {
    String sLabel;
    String oLabel;

    if (PROPAGATES.equals(predicate)) {
      sLabel   = "resource";
      oLabel   = "to[]";
    } else if (IMPLIES.equals(predicate)) {
      sLabel   = "permission";
      oLabel   = "implies[]";
    } else {
      sLabel   = "subject";
      oLabel   = "object[]";
    }

    objects = validateUriList(objects, oLabel, false);
    pep.checkAccess(action, ItqlHelper.validateUri(subject, sLabel));

    StringBuffer sb = new StringBuffer(512);

    for (int i = 0; i < objects.length; i++) {
      sb.append("<").append(subject).append("> ");
      sb.append("<").append(predicate).append("> ");
      sb.append("<").append(objects[i]).append("> ");
    }

    String triples = sb.toString();
    String cmd = "delete " + triples + " from " + PP_MODEL + ";";

    if (insert)
      cmd += ("insert " + triples + " into " + PP_MODEL + ";");

    ItqlHelper itql = ctx.getItqlHelper();
    String     txn = action + " on " + subject;

    try {
      itql.beginTxn(txn);
      itql.doUpdate(cmd, null);
      itql.commitTxn(txn);
      txn = null;
    } finally {
      try {
        if (txn != null)
          itql.rollbackTxn(txn);
      } catch (Throwable t) {
      }
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

      StringAnswer ans  = new StringAnswer(ctx.getItqlHelper().doQuery(query, null));
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

  private String[] listPP(String action, String subject, String predicate, boolean transitive)
                   throws RemoteException {
    String sLabel;
    String oLabel;

    if (PROPAGATES.equals(predicate)) {
      sLabel   = "resource";
      oLabel   = "permission-propagates";
    } else if (IMPLIES.equals(predicate)) {
      sLabel   = "permission";
      oLabel   = "implied-permissions";
    } else {
      sLabel   = "subject";
      oLabel   = "objects";
    }

    pep.checkAccess(action, ItqlHelper.validateUri(subject, sLabel));

    String query = transitive ? ITQL_LIST_PP_TRANS : ITQL_LIST_PP;

    query = ItqlHelper.bindValues(query, "s", subject, "p", predicate);

    try {
      StringAnswer ans  = new StringAnswer(ctx.getItqlHelper().doQuery(query, null));
      List         rows = ((StringAnswer.StringQueryAnswer) ans.getAnswers().get(0)).getRows();

      String[]     result = new String[rows.size()];

      for (int i = 0; i < result.length; i++)
        result[i] = ((String[]) rows.get(i))[0];

      return result;
    } catch (AnswerException ae) {
      throw new RemoteException("Error while loading " + oLabel + " for " + subject, ae);
    }
  }

  private boolean isInferred(String model, String resource, String permission, String principal)
                      throws RemoteException {
    if (principal == null)
      principal = ctx.getUserName();

    ItqlHelper.validateUri(resource, "resource");
    ItqlHelper.validateUri(permission, "permission");
    ItqlHelper.validateUri(principal, "principal");

    HashMap values = new HashMap();
    values.put("resource", resource);
    values.put("permission", permission);
    values.put("principal", principal);
    values.put("MODEL", model);

    String query = ItqlHelper.bindValues(ITQL_INFER_PERMISSION, values);

    try {
      StringAnswer ans  = new StringAnswer(ctx.getItqlHelper().doQuery(query, null));
      List         rows = ((StringAnswer.StringQueryAnswer) ans.getAnswers().get(0)).getRows();

      return rows.size() > 0;
    } catch (AnswerException ae) {
      throw new RemoteException("Error while querying inferred permissions", ae);
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
