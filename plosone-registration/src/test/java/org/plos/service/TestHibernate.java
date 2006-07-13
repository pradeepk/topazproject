package org.plos.service;

import junit.framework.TestCase;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.plos.registration.User;
import org.plos.registration.UserImpl;

/**
 * $HeadURL$
 * @version: $Id$
 */
public class TestHibernate extends TestCase {

  public void testHibernate() {
    final Session session = org.plos.service.HibernateUtil.getSession();

    final Transaction tx = session.beginTransaction();
    final User user = new UserImpl("steve@home.com", "stevec");

    try {
      session.save(user);
      tx.commit();
    } catch (final HibernateException e) {
      fail();
    } finally {
      session.close();
    }

  }

}
