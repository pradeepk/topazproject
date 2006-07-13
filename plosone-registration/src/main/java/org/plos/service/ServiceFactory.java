package org.plos.service;

import java.util.Map;
import java.util.Hashtable;

/**
 * $HeadURL: $
 * @version: $Id: $
 */
public class ServiceFactory {
  /** All the service instances are cached in the map as only one instance should be sufficient for a single jvm */
  private volatile Map services = new Hashtable();
  private static final String USER_SERVICE = RegistrationService.class.getName();

  public RegistrationService getRegistrationService() {
    if (null == services.get(USER_SERVICE)) {
      synchronized (this) {
        if (null == services.get(USER_SERVICE)) {
          final PlosRegistrationService registrationService = new PlosRegistrationService();
          registrationService.setUserDAO(new UserDAO());

          services.put(USER_SERVICE, registrationService);
        }
      }
    }

    return (RegistrationService) services.get(USER_SERVICE);
  }
}
