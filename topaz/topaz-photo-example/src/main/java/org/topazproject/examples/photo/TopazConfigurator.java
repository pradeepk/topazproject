package org.topazproject.examples.photo;

import java.net.URI;
import org.topazproject.mulgara.itql.DefaultItqlClientFactory;

import org.topazproject.otm.GraphConfig;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.impl.SessionFactoryImpl;
import org.topazproject.otm.stores.ItqlStore;
import org.topazproject.otm.stores.SimpleBlobStore;

public class TopazConfigurator {
  private SessionFactory factory = new SessionFactoryImpl();

  public TopazConfigurator() throws OtmException {
    DefaultItqlClientFactory tqlFactory = new DefaultItqlClientFactory();
    tqlFactory.setDbDir(System.getProperty("java.io.tmpdir") + "/triple-db");
    ItqlStore tripleStore = new ItqlStore(URI.create("local:///topazproject"), tqlFactory);
    factory.setTripleStore(tripleStore);

    SimpleBlobStore blobStore = new SimpleBlobStore(System.getProperty("java.io.tmpdir") + "/blob-db");
    factory.setBlobStore(blobStore);

    factory.preloadFromClasspath();
    factory.validate();
    createGraphs();
  }

  public SessionFactory getSessionFactory() {
    return factory;
  }

  private void createGraphs() throws OtmException {
    Session session = factory.openSession();
    try {
      session.beginTransaction();
      for (GraphConfig gc : factory.listGraphs())
        session.createGraph(gc.getId());
      session.getTransaction().commit();
    } finally {
      try { session.close(); } catch (Throwable t) {}
    }
  }
}
