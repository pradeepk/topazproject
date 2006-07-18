package org.plos.service;

/**
 * Provides service implementations for all kinds of services.
 * 
 * $HeadURL$
 * @version: $Id$
 */
public class ServiceFactory {
  private PersistenceService persistenceService;
  private RegistrationService registrationService;

  public RegistrationService getRegistrationService() {
    return registrationService;
  }

  public void setRegistrationService(final RegistrationService registrationService) {
    this.registrationService = registrationService;
  }

  public PersistenceService getPersistenceService() {
    return persistenceService;
  }

  public void setPersistenceService(final PersistenceService persistenceService) {
    this.persistenceService = persistenceService;
  }

}
