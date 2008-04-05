/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2008 by Topaz, Inc.
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

import java.io.IOException;

import java.util.Set;

import org.plos.xacml.AbstractSimplePEP;

import com.sun.xacml.PDP;
import com.sun.xacml.ParsingException;
import com.sun.xacml.UnknownIdentifierException;

/**
 * The XACML PEP for the permission accounts manager.
 *
 * @author Pradeep Krishnan
 */
public abstract class PermissionsPEP extends AbstractSimplePEP
  implements Permissions.ServicePermissions {
  /**
   * The list of all supported actions
   */
  protected static final String[] SUPPORTED_ACTIONS =
    new String[] {
                   GRANT, REVOKE, CANCEL_GRANTS, CANCEL_REVOKES, LIST_GRANTS, LIST_REVOKES,
                   IMPLY_PERMISSIONS, CANCEL_IMPLY_PERMISSIONS, LIST_IMPLIED_PERMISSIONS,
                   PROPAGATE_PERMISSIONS, CANCEL_PROPAGATE_PERMISSIONS, LIST_PERMISSION_PROPAGATIONS
    };

  /**
   * The list of all supported obligations
   */
  protected static final String[][] SUPPORTED_OBLIGATIONS =
    new String[][] { null, null, null, null, null, null, null, null, null, null, null, null };

  /*
   * inherited javadoc
   *
   */
  protected PermissionsPEP(PDP pdp, Set subjAttrs)
                    throws IOException, ParsingException, UnknownIdentifierException {
    super(pdp, subjAttrs);
  }

  /*
   * inherited javadoc
   *
   */
  protected PermissionsPEP(AbstractSimplePEP pep) {
    super(pep);
  }

  /**
   * A PermissionsPEP that inherits its pdp and subject attrs from another.
   */
  public static class Proxy extends PermissionsPEP {
    static {
      init(Proxy.class, SUPPORTED_ACTIONS, SUPPORTED_OBLIGATIONS);
    }

    public Proxy(AbstractSimplePEP pep) {
      super(pep);
    }
  }
}
