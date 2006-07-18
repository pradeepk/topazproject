#set( $service = $artifactId )
#set( $Svc = "${service.substring(0, 1).toUpperCase()}${service.substring(1)}" )
/*
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package ${package};

import java.rmi.RemoteException;

/** 
 * This provides the implementation of the ${service} service.
 * 
 * @author foo
 */
public class ${Svc}Impl implements ${Svc} {
}
