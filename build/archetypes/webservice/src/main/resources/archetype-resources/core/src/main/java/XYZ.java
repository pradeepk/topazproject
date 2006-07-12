#set( $service = $artifactId )
#set( $Svc = "${service.substring(0, 1).toUpperCase()}${service.substring(1)}" )

package ${package};

import java.rmi.RemoteException;

/** 
 * This defines the ${service} service.
 * 
 * @author foo
 */
public interface ${Svc} {
}
