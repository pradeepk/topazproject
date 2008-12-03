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
package org.topazproject.ambra.permission.service;

import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;
import org.topazproject.ambra.configuration.ConfigurationStore;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.Transaction;

/**
 * Load the implied permissions to the Database.
 *
 * @author Pradeep Krishnan
 */
public class ImpliedPermissionsLoader {
  private static final Log log = LogFactory.getLog(ImpliedPermissionsLoader.class);
  private static final Configuration CONF = ConfigurationStore.getInstance().getConfiguration();

  private SessionFactory sf;

  public void load() throws OtmException {
    Session s = sf.openSession();
    Transaction txn = null;
    try {
      txn = s.beginTransaction();
      Configuration conf        = CONF.subset("ambra.permissions.impliedPermissions");

      StringBuilder sb          = new StringBuilder();
      List          permissions = conf.getList("permission[@uri]");
      int           c           = permissions.size();

      for (int i = 0; i < c; i++) {
        List implies = conf.getList("permission(" + i + ").implies[@uri]");
        log.info("config contains " + permissions.get(i) + " implies " + implies);

        for (int j = 0; j < implies.size(); j++) {
          sb.append("<").append(permissions.get(i)).append("> ");
          sb.append("<").append(PermissionsService.IMPLIES).append("> ");
          sb.append("<").append(implies.get(j)).append("> ");
        }
      }

      String triples   = sb.toString();
      final String cmd = "insert " + triples + " into " + PermissionsService.PP_GRAPH + ";";

      if (permissions.size() > 0)
        s.doNativeUpdate(cmd);

      txn.commit();
      txn = null;
    } finally {
      try {
        if (txn != null)
          txn.rollback();
      } catch (Exception e) {
        log.warn("Failed to rollback", e);
      }
      try {
        if (s != null)
          s.close();
      } catch (Exception e) {
        log.warn("Failed to close session", e);
      }
    }
  }

  @Required
  public void setOtmSessionFactory(SessionFactory sf) {
    this.sf = sf;
  }

}
