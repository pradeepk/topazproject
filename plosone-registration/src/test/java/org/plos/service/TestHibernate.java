package org.plos.service;

import org.plos.BasePlosoneRegistrationTest;
import org.plos.registration.User;
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

  public void testDeleteUser() {
    User user = new UserImpl("deleteUser@home.com", "delete");
    userDao.saveOrUpdate(user);
    user = userDao.findUserWithLoginName("deleteUser@home.com");
    assertNotNull(user);
    userDao.delete(user);
    user = userDao.findUserWithLoginName("deleteUser@home.com");
    assertNull(user);
  }

}
