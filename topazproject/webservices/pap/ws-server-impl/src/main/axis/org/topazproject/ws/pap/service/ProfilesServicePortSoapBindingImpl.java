/*
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.ws.pap.service;

import java.io.IOException;
import java.net.URI;
import java.rmi.RemoteException;
import javax.xml.rpc.ServiceException;
import javax.xml.rpc.server.ServiceLifecycle;
import javax.xml.rpc.server.ServletEndpointContext;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.configuration.ConfigurationStore;
import org.topazproject.ws.pap.ProfilesImpl;
import org.topazproject.ws.pap.ProfilesPEP;
import org.topazproject.xacml.Util;

/** 
 * Implements the server-side of the profiles webservice. This is really just a wrapper around
 * the actual implementation in core.
 * 
 * @author Ronald Tschalär
 */
public class ProfilesServicePortSoapBindingImpl implements Profiles, ServiceLifecycle {
  private static final Log log = LogFactory.getLog(ProfilesServicePortSoapBindingImpl.class);

  private ProfilesImpl impl;

  public void init(Object context) throws ServiceException {
    if (log.isTraceEnabled())
      log.trace("ServiceLifecycle#init");

    try {
      // get the pep
      final ProfilesPEP pep = new WSProfilesPEP((ServletEndpointContext) context);

      // get other config
      Configuration conf = ConfigurationStore.getInstance().getConfiguration();
      conf = conf.subset("topaz");

      if (!conf.containsKey("services.fedora.uri"))
        throw new ConfigurationException("missing key 'topaz.services.fedora.uri'");
      if (!conf.containsKey("services.itql.uri"))
        throw new ConfigurationException("missing key 'topaz.services.itql.uri'");

      final URI fedora = new URI(conf.getString("services.fedora.uri"));
      final String username = conf.getString("services.fedora.userName", null);
      final String password = conf.getString("services.fedora.password", null);
      final URI mulgara = new URI(conf.getString("services.itql.uri"));

      // create the impl
      impl = new ProfilesImpl(mulgara, fedora, username, password, pep);
    } catch (Exception e) {
      log.error("Failed to initialize ProfilesImpl.", e);
      throw new ServiceException(e);
    }
  }

  public void destroy() {
    if (log.isTraceEnabled())
      log.trace("ServiceLifecycle#destroy");

    impl = null;
  }

  /**
   * @see org.topazproject.ws.pap.Profiles#createProfile
   */
  public void createProfile(String userId, UserProfile profile)
      throws RemoteException, DuplicateIdException {
    try {
      impl.createProfile(userId, (profile != null) ? toProfile(profile) : null);
    } catch (org.topazproject.ws.pap.DuplicateIdException die) {
      log.info("", die);
      throw new DuplicateIdException(die.getId());
    } catch (RuntimeException re) {
      log.warn("", re);
      throw re;
    } catch (Error e) {
      log.error("", e);
      throw e;
    }
  }

  /**
   * @see org.topazproject.ws.pap.Profiles#getProfile
   */
  public UserProfile getProfile(String userId) throws RemoteException, NoSuchIdException {
    try {
      org.topazproject.ws.pap.UserProfile prof = impl.getProfile(userId);
      return new UserProfile(prof.getBiography(), prof.getBiographyReaders(), prof.getDisplayName(),
                             prof.getDisplayNameReaders(), prof.getEmail(), prof.getEmailReaders(),
                             prof.getGender(), prof.getGenderReaders(), prof.getHomePage(),
                             prof.getHomePageReaders(), prof.getInterests(),
                             prof.getInterestsReaders(), prof.getPublications(),
                             prof.getPublicationsReaders(), prof.getRealName(),
                             prof.getRealNameReaders(), prof.getTitle(), prof.getTitleReaders(),
                             prof.getWeblog(), prof.getWeblogReaders());
    } catch (org.topazproject.ws.pap.NoSuchIdException nsie) {
      log.info("", nsie);
      throw new NoSuchIdException(nsie.getId());
    } catch (RuntimeException re) {
      log.warn("", re);
      throw re;
    } catch (Error e) {
      log.error("", e);
      throw e;
    }
  }

  /**
   * @see org.topazproject.ws.pap.Profiles#updateProfile
   */
  public void updateProfile(String userId, UserProfile profile)
      throws RemoteException, NoSuchIdException {
    try {
      impl.updateProfile(userId, toProfile(profile));
    } catch (org.topazproject.ws.pap.NoSuchIdException nsie) {
      log.info("", nsie);
      throw new NoSuchIdException(nsie.getId());
    } catch (RuntimeException re) {
      log.warn("", re);
      throw re;
    } catch (Error e) {
      log.error("", e);
      throw e;
    }
  }

  /**
   * @see org.topazproject.ws.pap.Profiles#deleteProfile
   */
  public void deleteProfile(String userId) throws RemoteException, NoSuchIdException {
    try {
      impl.deleteProfile(userId);
    } catch (org.topazproject.ws.pap.NoSuchIdException nsie) {
      log.info("", nsie);
      throw new NoSuchIdException(nsie.getId());
    } catch (RuntimeException re) {
      log.warn("", re);
      throw re;
    } catch (Error e) {
      log.error("", e);
      throw e;
    }
  }

  private static final org.topazproject.ws.pap.UserProfile toProfile(UserProfile prof) {
    org.topazproject.ws.pap.UserProfile res = new org.topazproject.ws.pap.UserProfile();

    res.setBiography(prof.getBiography());
    res.setDisplayName(prof.getDisplayName());
    res.setEmail(prof.getEmail());
    res.setGender(prof.getGender());
    res.setHomePage(prof.getHomePage());
    res.setInterests(prof.getInterests());
    res.setPublications(prof.getPublications());
    res.setRealName(prof.getRealName());
    res.setTitle(prof.getTitle());
    res.setWeblog(prof.getWeblog());

    res.setBiographyReaders(prof.getBiographyReaders());
    res.setDisplayNameReaders(prof.getDisplayNameReaders());
    res.setEmailReaders(prof.getEmailReaders());
    res.setGenderReaders(prof.getGenderReaders());
    res.setHomePageReaders(prof.getHomePageReaders());
    res.setInterestsReaders(prof.getInterestsReaders());
    res.setPublicationsReaders(prof.getPublicationsReaders());
    res.setRealNameReaders(prof.getRealNameReaders());
    res.setTitleReaders(prof.getTitleReaders());
    res.setWeblogReaders(prof.getWeblogReaders());

    return res;
  }

  private static class WSProfilesPEP extends ProfilesPEP {
    static {
      init(WSProfilesPEP.class, SUPPORTED_ACTIONS, SUPPORTED_OBLIGATIONS);
    }

    public WSProfilesPEP(ServletEndpointContext context) throws Exception {
      super(Util.lookupPDP(context, "topaz.profiles.pdpName"), Util.createSubjAttrs(context));
    }
  }
}
