package org.plos.service;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.plos.ApplicationException;
import org.plos.registration.User;

import java.util.List;

/**
 * $HeadURL$
 * @version: $Id$
 */
 public class UserDAO {

  public void saveOrUpdate(final User user) {
    DBUtil.execute(new DBCommand() {
      public Object execute(final Session session) {
        session.saveOrUpdate(user);
        return null;
      }
    });
  }

  public User findUserWithEmailAddress(final String emailAddress) {
    return (User) DBUtil.execute(new DBCommand() {
      public Object execute(final Session session) {
        final Criteria criteria = session.createCriteria(User.class);
        criteria.add(Restrictions.eq("emailAddress", emailAddress));
        final List list = criteria.list();
        if (list.size() > 1) {
          throw new ApplicationException("Data error: More than one user account found with the same email address.");
        }

        if (list.isEmpty()) {
          return null;
        }
        return (User) list.get(0);
      }
    });
  }

}

class DBUtil {
  public static Object execute(final DBCommand dbCommand) {
    final Session session = HibernateUtil.getSession();
    final Transaction tx = session.beginTransaction();

    final Object result;
    try {
      result = dbCommand.execute(session);
      tx.commit();
    } catch (final HibernateException e) {
      if (tx!=null) tx.rollback();
      throw e;
    } finally {
      session.close();
    }

    return result;

  }

}

interface DBCommand {
  Object execute(final Session session);
}