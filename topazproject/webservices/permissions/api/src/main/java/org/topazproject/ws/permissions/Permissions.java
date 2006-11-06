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

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * This defines the permissions service.
 *
 * @author Pradeep Krishnan
 */
public interface Permissions extends Remote {
  /**
   * A URI to indicate all resources, permissions or subjects. eg. to grant all permissions to a
   * principal on a resource, the permissions URI can be set to this URI. To grant a specific
   * permission to all subjects on all resources, the resource and subject URIs can be set to this
   * URI.
   */
  public static final String ALL = "http://rdf.topazproject.org/RDF/permissions#all";

  /**
   * Permissions associated with permissions service.
   */
  public static interface ServicePermissions {
    /**
     * The action that represents a permission grant operation in XACML policies.
     */
    public static final String GRANT = "permissions:grant";

    /**
     * The action that represents a permission revoke operation in XACML policies.
     */
    public static final String REVOKE = "permissions:revoke";

    /**
     * The action that represents canceling a permission grant operation in XACML policies.
     */
    public static final String CANCEL_GRANTS = "permissions:cancelGrants";

    /**
     * The action that represents cancelling a permission revoke operation in XACML policies.
     */
    public static final String CANCEL_REVOKES = "permissions:cancelRevokes";

    /**
     * The action that represents a list permission grants operation in XACML policies.
     */
    public static final String LIST_GRANTS = "permissions:listGrants";

    /**
     * The action that represents a list permission revokes operation in XACML policies.
     */
    public static final String LIST_REVOKES = "permissions:listRevokes";
  }

  /**
   * Grants permissions. Adds these to a grants list. A permit biased PEP may allow access to any
   * request if a match is found in the grants list. Similarly a deny biased PEP may deny access
   * to any request if no matching permissions are found in the grants list.
   *
   * @param resource the resource uri for which permissions are granted.
   * @param permissions the list of permissions being granted. (as a list of uris)
   * @param principals the list ofprincipals to whom this grant applies to. (as a list of uris) May
   *        be <code>null</code>, empty or may contain <code>null</code> to indicate the user that
   *        is making this call.
   *
   * @throws RemoteException if some other error occured
   */
  public void grant(String resource, String[] permissions, String[] principals)
             throws RemoteException;

  /**
   * Revokes permissions. Adds these to a revokes list. A deny biased PEP will deny access to any
   * request if a match is found in the revokes list. Similarly a permit biased PEP will allow
   * access to any request if no matching permissions are found in the revokes list.
   *
   * @param resource the resource uri for which permissions are revoked
   * @param permissions the list of permissions being revoked. (as a list of uris)
   * @param principals the list ofprincipals to whom this revoke applies to. (as a list of uris)
   *        May be <code>null</code>, empty or may contain <code>null</code> to indicate the user
   *        that is making this call.
   *
   * @throws RemoteException if some other error occured
   */
  public void revoke(String resource, String[] permissions, String[] principals)
              throws RemoteException;

  /**
   * Cancel earlier grants. Deletes permissions from a grants list.
   *
   * @param resource the resource uri for which permission grants are to be cancelled
   * @param permissions the list of permission grants being cancelled.
   * @param principals the list of principals to whom this cancellation applies to
   *
   * @throws RemoteException if some other error occured
   */
  public void cancelGrants(String resource, String[] permissions, String[] principals)
                    throws RemoteException;

  /**
   * Cancel earlier revokes. Deletes permissions from a revokes list.
   *
   * @param resource the resource uri for which permission revokes are to be cancelled
   * @param permissions the list of permission grants being cancelled.
   * @param principals the list of principals to whom this cancellation applies to
   *
   * @throws RemoteException if some other error occured
   */
  public void cancelRevokes(String resource, String[] permissions, String[] principals)
                     throws RemoteException;

  /**
   * List explicit permission grants for a resource. (Note: does not imply permits).
   *
   * @param resource the resource uri for which permissions are to be listed
   * @param principal the principal for whom the query is perfromed or <code>null</code> for the
   *        user that is making this call.
   *
   * @return Returns the list of grants
   *
   * @throws RemoteException if some other error occured
   */
  public String[] listGrants(String resource, String principal)
                      throws RemoteException;

  /**
   * List explicit permission grants for a resource. (Note: does not imply denials).
   *
   * @param resource the resource uri for which permissions are to be listed
   * @param principal the principal for whom the query is perfromed or <code>null</code> for the
   *        user that is making this call.
   *
   * @return Returns the list of revokes
   *
   * @throws RemoteException if some other error occured
   */
  public String[] listRevokes(String resource, String principal)
                       throws RemoteException;
}
