package org.plos.service;

import org.plos.BasePlosoneRegistrationTest;
import org.plos.registration.UserImpl;

/**
 * $HeadURL$
 * @version: $Id$
 */
public class TestHibernate extends BasePlosoneRegistrationTest {
  private UserDAO userDao;

  public void testHibernate() {
    userDao.saveOrUpdate(new UserImpl("steve@home.com", "stevec"));
  }

  public void setUserDAO(final UserDAO userDao) {
      this.userDao = userDao;
  }

}
