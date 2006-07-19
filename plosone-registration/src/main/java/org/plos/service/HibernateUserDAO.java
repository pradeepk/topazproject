package org.plos.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.plos.registration.User;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import java.sql.SQLException;
import java.util.List;

/**
 * Hibernate based implementation of the UserDAO.
 * $HeadURL: $
 * @version: $Id: $
 */
public class HibernateUserDAO extends HibernateDaoSupport implements UserDAO {
  private static final Log log = LogFactory.getLog(HibernateUserDAO.class);

  /**
   * Save or update the user
   * @param user User
   */
  public void saveOrUpdate(final User user) {
    getHibernateTemplate().execute(
      new HibernateCallback(){
        public Object doInHibernate(final Session session) throws HibernateException, SQLException {
          session.saveOrUpdate(user);
          return null;
        }
      });
  }

  /**
   * Delete user
   * @param user User
   */
  public void delete(final User user) {
     getHibernateTemplate().delete(user);
  }


  /**
   * Find the user for the given loginName. If more than one user is found it throws an Exception.
   *
   * @param loginName
   * @return the user for the given loginName
   * @throws DuplicateLoginNameException
   */
  public User findUserWithLoginName(final String loginName) {
    return (User) getHibernateTemplate().execute(
      new HibernateCallback(){
        public Object doInHibernate(final Session session) throws HibernateException, SQLException {
          final DetachedCriteria detachedCriteria = DetachedCriteria.forClass(User.class);
          detachedCriteria.add(Restrictions.eq("loginName", loginName));
          final List list = getHibernateTemplate().findByCriteria(detachedCriteria);

          if (list.size() > 1) {
            final DuplicateLoginNameException duplicateLoginNameException = new DuplicateLoginNameException(loginName);

            log.error("DuplicateLoginName:"+loginName, duplicateLoginNameException);
            throw duplicateLoginNameException;
          }

          if (list.isEmpty()) {
            return null;
          }
          return (User) list.get(0);
        }
    });
  }

}
