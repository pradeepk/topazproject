package org.plos.service;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;

/**
 * $HeadURL$
 * @version: $Id$
 */
public class HibernateUtil {

  private static final SessionFactory sessionFactory;

  static {
    try {
      sessionFactory = new AnnotationConfiguration()
              .addAnnotatedClass(org.plos.registration.UserImpl.class)
//              .addPackage("org.plos") //the fully qualified package name
//              .setProperty("hibernate.show_sql", "true")
//              .setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect")
//              .setProperty("hibernate.connection.driver_class", "org.postgresql.Driver")
//              .setProperty("hibernate.connection.url", "jdbc:postgresql://localhost/postgres")
//              .setProperty("hibernate.connection.username", "postgres")
//              .setProperty("hibernate.connection.password", "postgres")
//              .setProperty(Environment.HBM2DDL_AUTO, "create")
//              .setProperty("hibernate.show_sql", "true");
              .configure()
              .buildSessionFactory();
    } catch (final Throwable ex) {
      throw new ExceptionInInitializerError(ex);
    }
  }

  public static Session getSession() throws HibernateException {
    return sessionFactory.openSession();
  }

}

