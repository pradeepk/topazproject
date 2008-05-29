/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2007 by Topaz, Inc.
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
package org.plos.permission.service;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.plos.configuration.ConfigurationStore;

import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.transaction.annotation.Transactional;

import org.topazproject.otm.ModelConfig;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Rdf;
import org.topazproject.otm.RdfUtil;
import org.topazproject.otm.Session;
import org.topazproject.otm.TripleStore;
import org.topazproject.otm.query.Results;

import org.apache.struts2.ServletActionContext;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

/**
 * This provides the implementation of the permissions service.<p>Grants and Revokes are stored
 * in a seperate models with 1 triple per permission like this:<pre>
 * &lt;${resource}&gt; &lt;${permission}&gt; &lt;${principal}&gt;</pre></p>
 *
 * @author Pradeep Krishnan
 */
public class PermissionsService implements Permissions {
  private static final Log log = LogFactory.getLog(PermissionsService.class);

  //
  private static final Configuration CONF = ConfigurationStore.getInstance().getConfiguration();

  //
  private static final String GRANTS_MODEL       =
    "<" + CONF.getString("ambra.models.grants") + ">";
  private static final String REVOKES_MODEL      =
    "<" + CONF.getString("ambra.models.revokes") + ">";
  private static final String PP_MODEL           = "<" + CONF.getString("ambra.models.pp") + ">";
  private static final String GRANTS_MODEL_TYPE  =
    "<" + CONF.getString("ambra.models.grants[@type]", "mulgara:Model") + ">";
  private static final String REVOKES_MODEL_TYPE =
    "<" + CONF.getString("ambra.models.revokes[@type]", "mulgara:Model") + ">";
  private static final String PP_MODEL_TYPE      =
    "<" + CONF.getString("ambra.models.pp[@type]", "mulgara:Model") + ">";

  //
  private static final String IMPLIES    = Rdf.topaz + "implies";
  private static final String PROPAGATES = Rdf.topaz + "propagate-permissions-to";

  //
  private static final String ITQL_LIST                 =
    "select $p from ${MODEL} where <${resource}> $p <${principal}>;";
  private static final String ITQL_LIST_PP              =
    "select $o from ${MODEL} where <${s}> <${p}> $o;".replaceAll("\\Q${MODEL}", PP_MODEL);
  private static final String ITQL_LIST_PP_TRANS        =
    ("select $o from ${MODEL} where <${s}> <${p}> $o "
    + " or (trans($s <${p}> $o) and $s <mulgara:is> <${s}>);").replaceAll("\\Q${MODEL}", PP_MODEL);
  private static final String ITQL_INFER_PERMISSION     =
    ("select $s from ${PP_MODEL} where $s $p $o in ${MODEL} "
    + "and ($s <mulgara:is> <${resource}> or $s <mulgara:is> <${ALL}> "
    + "      or $s <${PP}> <${resource}> "
    + "      or (trans($s <${PP}> $res) and $res <mulgara:is> <${resource}>)) "
    + "and ($p <mulgara:is> <${permission}> or $p <mulgara:is> <${ALL}> "
    + "      or $p <${IMPLIES}> <${permission}> "
    + "      or (trans($p <${IMPLIES}> $perm) and $perm <mulgara:is> <${permission}>)) "
    + "and ($o <mulgara:is> <${principal}> or $o <mulgara:is> <${ALL}>);" //
    ).replaceAll("\\Q${PP_MODEL}", PP_MODEL).replaceAll("\\Q${PP}", PROPAGATES)
      .replaceAll("\\Q${IMPLIES}", IMPLIES).replaceAll("\\Q${ALL}", ALL);
  private static final String ITQL_RESOURCE_PERMISSIONS =
    ("select $p $o from ${PP_MODEL} where ($s $p $o in ${MODEL} " //
    + "   and ($s <mulgara:is> <${resource}> or $s <mulgara:is> <${ALL}> "
    + "      or $s <${PP}> <${resource}> "
    + "      or (trans($s <${PP}> $res) and $res <mulgara:is> <${resource}>))"
    + ") or ($s $impliedBy $o in ${MODEL} " //
    + "   and ($impliedBy <${IMPLIES}> $p " //
    + "      or trans($impliedBy <${IMPLIES}> $p)) " //
    + "   and ($s <mulgara:is> <${resource}> or $s <mulgara:is> <${ALL}> "
    + "      or $s <${PP}> <${resource}> "
    + "      or (trans($s <${PP}> $res) and $res <mulgara:is> <${resource}>))" + ");" //
    ).replaceAll("\\Q${PP_MODEL}", PP_MODEL).replaceAll("\\Q${PP}", PROPAGATES)
      .replaceAll("\\Q${IMPLIES}", IMPLIES).replaceAll("\\Q${ALL}", ALL);

  //
  private static Ehcache grantsCache  = initCache("permission-grants");
  private static Ehcache revokesCache = initCache("permission-revokes");

  private static Ehcache initCache(String name) {
    Ehcache cache = null;

    try {
      cache = CacheManager.getInstance().getEhcache(name);
    } catch (CacheException ce) {
      log.error("Error getting cache-manager", ce);
    } catch (IllegalStateException ise) {
      log.error("Error getting cache", ise);
    }

    if (cache == null)
      log.info("No cache configuration found for " + name + ".");
    else
      log.info("Cache configuration found for " + name + ".");

    return cache;
  }

  /**
   * Initialize the permissions ITQL model.
   *
   * @param s the session to use
   *
   * @throws OtmException on a failure
   */
  public static void initializeModel(Session s) throws OtmException {
    if (((grantsCache != null) && (grantsCache.getSize() != 0))
         || ((revokesCache != null) && (revokesCache.getSize() != 0)))
      return; // xxx: cache has entries perhaps from peers. so initialized is a good guess

    TripleStore ts = s.getSessionFactory().getTripleStore();
    ts.createModel(new ModelConfig("grants",  toURI(GRANTS_MODEL),  toURI(GRANTS_MODEL_TYPE)));
    ts.createModel(new ModelConfig("revokes", toURI(REVOKES_MODEL), toURI(REVOKES_MODEL_TYPE)));
    ts.createModel(new ModelConfig("pp",      toURI(PP_MODEL),      toURI(PP_MODEL_TYPE)));

    Configuration conf        = CONF.subset("ambra.permissions.impliedPermissions");

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

    String triples   = sb.toString();
    final String cmd = "insert " + triples + " into " + PP_MODEL + ";";

    if (permissions.size() > 0)
      s.doNativeUpdate(cmd);

    if (grantsCache != null)
      grantsCache.removeAll();

    if (revokesCache != null)
      revokesCache.removeAll();
  }

  private static URI toURI(String uri) {
    return URI.create(uri.substring(1, uri.length() - 1));
  }

  //
  private final PermissionsPEP pep;

  /**
   * Creates a new PermissionsService object.
   *
   * @throws Exception on an error
   */
  public PermissionsService() throws Exception {
    this(new PermissionsPEP());
  }

  /**
   * Create a new permission instance.
   *
   * @param pep the policy-enforcer to use for access-control
   */
  public PermissionsService(PermissionsPEP pep) {
    this.pep   = pep;
  }

  /*
   * @see org.plos.permission.service.Permissions#grant
   */
  @Transactional(rollbackFor = { Throwable.class })
  public void grant(String resource, String[] permissions, String[] principals)
             throws OtmException {
    updateModel(pep.GRANT, GRANTS_MODEL, grantsCache, resource, permissions, principals, true);
  }

  /*
   * @see org.plos.permission.service.Permissions#revoke
   */
  @Transactional(rollbackFor = { Throwable.class })
  public void revoke(String resource, String[] permissions, String[] principals)
              throws OtmException {
    updateModel(pep.REVOKE, REVOKES_MODEL, revokesCache, resource, permissions, principals, true);
  }

  /*
   * @see org.plos.permission.service.Permissions#cancleGrants
   */
  @Transactional(rollbackFor = { Throwable.class })
  public void cancelGrants(String resource, String[] permissions, String[] principals)
                    throws OtmException {
    updateModel(pep.CANCEL_GRANTS, GRANTS_MODEL, grantsCache, resource, permissions, principals,
                false);
  }

  /*
   * @see org.plos.permission.service.Permissions#cancelRevokes
   */
  @Transactional(rollbackFor = { Throwable.class })
  public void cancelRevokes(String resource, String[] permissions, String[] principals)
                     throws OtmException {
    updateModel(pep.CANCEL_REVOKES, REVOKES_MODEL, revokesCache, resource, permissions, principals,
                false);
  }

  /*
   * @see org.plos.permission.service.Permissions#listGrants
   */
  @Transactional(readOnly = true)
  public String[] listGrants(String resource, String principal)
                      throws OtmException {
    return listPermissions(pep.LIST_GRANTS, GRANTS_MODEL, resource, principal);
  }

  /*
   * @see org.plos.permission.service.Permissions#listRevokes
   */
  @Transactional(readOnly = true)
  public String[] listRevokes(String resource, String principal)
                       throws OtmException {
    return listPermissions(pep.LIST_REVOKES, REVOKES_MODEL, resource, principal);
  }

  /*
   * @see org.plos.permission.service.Permissions#implyPermission
   */
  @Transactional(rollbackFor = { Throwable.class })
  public void implyPermissions(String permission, String[] implies)
                        throws OtmException {
    updatePP(pep.IMPLY_PERMISSIONS, permission, IMPLIES, implies, true);
  }

  /*
   * @see org.plos.permission.service.Permissions#cancelImplyPermission
   */
  @Transactional(rollbackFor = { Throwable.class })
  public void cancelImplyPermissions(String permission, String[] implies)
                              throws OtmException {
    updatePP(pep.CANCEL_IMPLY_PERMISSIONS, permission, IMPLIES, implies, false);
  }

  /*
   * @see org.plos.permission.service.Permissions#listImpliedPermissions
   */
  @Transactional(readOnly = true)
  public String[] listImpliedPermissions(String permission, boolean transitive)
                                  throws OtmException {
    return listPP(pep.LIST_IMPLIED_PERMISSIONS, permission, IMPLIES, transitive);
  }

  /*
   * @see org.plos.permission.service.Permissions#propagatePermissions
   */
  @Transactional(rollbackFor = { Throwable.class })
  public void propagatePermissions(String resource, String[] to)
                            throws OtmException {
    updatePP(pep.PROPAGATE_PERMISSIONS, resource, PROPAGATES, to, true);
  }

  /*
   * @see org.plos.permission.service.Permissions#cancelPropagatePermissions
   */
  @Transactional(rollbackFor = { Throwable.class })
  public void cancelPropagatePermissions(String resource, String[] to)
                                  throws OtmException {
    updatePP(pep.CANCEL_PROPAGATE_PERMISSIONS, resource, PROPAGATES, to, false);
  }

  /*
   * @see org.plos.permission.service.Permissions#listPermissionPropagations
   */
  @Transactional(readOnly = true)
  public String[] listPermissionPropagations(String resource, boolean transitive)
                                      throws OtmException {
    return listPP(pep.LIST_PERMISSION_PROPAGATIONS, resource, PROPAGATES, transitive);
  }

  /*
   * @see org.plos.permission.service.Permissions#isGranted
   */
  @Transactional(readOnly = true)
  public boolean isGranted(String resource, String permission, String principal)
                    throws OtmException {
    if (principal == null)
      throw new NullPointerException("principal");

    if (grantsCache == null)
      return isInferred(GRANTS_MODEL, resource, permission, principal);

    Map<String, List<String>> map;
    Element element = grantsCache.get(resource);

    if (element != null) {
      map = (Map<String, List<String>>) element.getValue();

      if (log.isDebugEnabled())
        log.debug("grants-cache: cache hit for " + resource);
    } else {
      map = createPermissionMap(resource, GRANTS_MODEL);
      grantsCache.put(new Element(resource, map));

      if (log.isDebugEnabled())
        log.debug("grants-cache: cache miss for " + resource);
    }

    List<String> list = map.get(permission);

    return (list != null) && (list.contains(principal) || list.contains(ALL));
  }

  /*
   * @see org.plos.permission.service.Permissions#isGranted
   */
  @Transactional(readOnly = true)
  public boolean isRevoked(String resource, String permission, String principal)
                    throws OtmException {
    if (principal == null)
      throw new NullPointerException("principal");

    if (revokesCache == null)
      return isInferred(REVOKES_MODEL, resource, permission, principal);

    Map<String, List<String>> map;
    Element element = revokesCache.get(resource);

    if (element != null) {
      map = (Map<String, List<String>>) element.getValue();

      if (log.isDebugEnabled())
        log.debug("revokes-cache: cache hit for " + resource);
    } else {
      map = createPermissionMap(resource, REVOKES_MODEL);
      revokesCache.put(new Element(resource, map));

      if (log.isDebugEnabled())
        log.debug("grants-cache: cache miss for " + resource);
    }

    List<String> list = map.get(permission);

    return (list != null) && (list.contains(principal) || list.contains(ALL));
  }

  private void updateModel(String action, String model, Ehcache cache, String resource,
                           String[] permissions, String[] principals, boolean insert)
                    throws OtmException {
    permissions = validateUriList(permissions, "permissions", false);

    if (permissions.length == 0)
      return;

    if ((principals == null) || (principals.length == 0))
      throw new NullPointerException("principal");

    principals = validateUriList(principals, "principals", false);

    pep.checkAccess(action, RdfUtil.validateUri(resource, "resource"));

    StringBuffer sb = new StringBuffer(512);

    for (int i = 0; i < principals.length; i++) {
      String principal = principals[i];
      for (int j = 0; j < permissions.length; j++) {
        sb.append("<").append(resource).append("> ");
        sb.append("<").append(permissions[j]).append("> ");
        sb.append("<").append(principal).append("> ");
      }
    }

    String triples = sb.toString();

    String cmd;

    if (insert)
      cmd = "insert " + triples + " into " + model + ";";
    else
      cmd = "delete " + triples + " from " + model + ";";

    getCurrentSession().doNativeUpdate(cmd);

    if (cache != null)
      cache.remove(resource);

    if (log.isInfoEnabled()) {
      log.info(action + " succeeded for resource " + resource + "\npermissions:\n"
               + Arrays.asList(permissions) + "\nprincipals:\n" + Arrays.asList(principals));
    }
  }

  private void updatePP(String action, final String subject, final String predicate,
                        String[] objects, boolean insert)
                 throws OtmException {
    String sLabel;
    String oLabel;

    if (PROPAGATES.equals(predicate)) {
      sLabel       = "resource";
      oLabel       = "to[]";
    } else if (IMPLIES.equals(predicate)) {
      sLabel   = "permission";
      oLabel   = "implies[]";
    } else {
      sLabel   = "subject";
      oLabel   = "object[]";
    }

    objects = validateUriList(objects, oLabel, false);

    if (objects.length == 0)
      return;

    pep.checkAccess(action, RdfUtil.validateUri(subject, sLabel));

    StringBuffer sb = new StringBuffer(512);

    for (int i = 0; i < objects.length; i++) {
      sb.append("<").append(subject).append("> ");
      sb.append("<").append(predicate).append("> ");
      sb.append("<").append(objects[i]).append("> ");
    }

    String       triples = sb.toString();
    final String cmd;

    if (insert)
      cmd = "insert " + triples + " into " + PP_MODEL + ";";
    else
      cmd = "delete " + triples + " from " + PP_MODEL + ";";

    getCurrentSession().doNativeUpdate(cmd);

    if ((grantsCache == null) && (revokesCache == null))
      return;

    if (!PROPAGATES.equals(predicate)) {
      // implied permissions changed.
      if (grantsCache != null)
        grantsCache.removeAll();

      if (revokesCache != null)
        revokesCache.removeAll();
    } else {
      Results ans = getCurrentSession().doNativeQuery(
                        RdfUtil.bindValues(ITQL_LIST_PP_TRANS, "s", subject, "p", predicate));
      while (ans.next()) {
        String res = ans.getString(0);

        if (grantsCache != null)
          grantsCache.remove(res);

        if (revokesCache != null)
          revokesCache.remove(res);
      }
    }
  }

  private String[] listPermissions(String action, String model, String resource, String principal)
                            throws OtmException {
    if (principal == null)
      throw new NullPointerException("principal");

    RdfUtil.validateUri(principal, "principal");

    pep.checkAccess(action, RdfUtil.validateUri(resource, "resource"));

    Map map = new HashMap(3);
    map.put("resource", resource);
    map.put("principal", principal);
    map.put("MODEL", model);

    String  query = RdfUtil.bindValues(ITQL_LIST, map);
    Results ans   = getCurrentSession().doNativeQuery(query);

    List<String> result = new ArrayList<String>();
    while (ans.next())
      result.add(ans.getString(0));

    return result.toArray(new String[result.size()]);
  }

  private String[] listPP(String action, String subject, String predicate, boolean transitive)
                   throws OtmException {
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

    pep.checkAccess(action, RdfUtil.validateUri(subject, sLabel));

    String query = transitive ? ITQL_LIST_PP_TRANS : ITQL_LIST_PP;
    query = RdfUtil.bindValues(query, "s", subject, "p", predicate);

    Results ans = getCurrentSession().doNativeQuery(query);

    List<String> result = new ArrayList<String>();
    while (ans.next())
      result.add(ans.getString(0));

    return result.toArray(new String[result.size()]);
  }

  private boolean isInferred(String model, String resource, String permission, String principal)
                      throws OtmException {
    if (principal == null)
      throw new NullPointerException("principal");

    RdfUtil.validateUri(resource, "resource");
    RdfUtil.validateUri(permission, "permission");
    RdfUtil.validateUri(principal, "principal");

    HashMap values = new HashMap();
    values.put("resource", resource);
    values.put("permission", permission);
    values.put("principal", principal);
    values.put("MODEL", model);

    String  query = RdfUtil.bindValues(ITQL_INFER_PERMISSION, values);
    Results ans   = getCurrentSession().doNativeQuery(query);

    return ans.next();
  }

  private String[] validateUriList(String[] list, String name, boolean nullOk) {
    if (list == null)
      throw new NullPointerException(name + " list can't be null");

    // eliminate duplicates
    list   = (String[]) (new HashSet(Arrays.asList(list))).toArray(new String[0]);

    name   = name + " list item";

    for (int i = 0; i < list.length; i++) {
      if (list[i] != null)
        RdfUtil.validateUri(list[i], name);
      else if (!nullOk)
        throw new NullPointerException(name + " can't be null");
    }

    return list;
  }

  private Map<String, List<String>> createPermissionMap(String resource, String model)
        throws OtmException {
    String query =
      RdfUtil.bindValues(ITQL_RESOURCE_PERMISSIONS, "resource", resource, "MODEL", model);

    Results                   ans = getCurrentSession().doNativeQuery(query);
    Map<String, List<String>> map = new HashMap<String, List<String>>();

    while (ans.next()) {
      List<String> list = map.get(ans.getString(0));

      if (list == null) {
        list = new ArrayList<String>();
        map.put(ans.getString(0), list);
      }

      list.add(ans.getString(1));
    }

    return map;
  }

  private Session getCurrentSession() {
    return (Session) WebApplicationContextUtils
      .getRequiredWebApplicationContext(ServletActionContext.getServletContext())
      .getBean("otmSession");
  }
}