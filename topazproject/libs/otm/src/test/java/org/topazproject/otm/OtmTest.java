package org.topazproject.otm;

import java.net.URI;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.otm.criterion.Order;
import org.topazproject.otm.criterion.Restrictions;
import org.topazproject.otm.Criteria;
import org.topazproject.otm.samples.Annotation;
import org.topazproject.otm.samples.Article;
import org.topazproject.otm.samples.NoRdfType;
import org.topazproject.otm.samples.PrivateAnnotation;
import org.topazproject.otm.samples.PublicAnnotation;
import org.topazproject.otm.samples.Reply;
import org.topazproject.otm.samples.ReplyThread;
import org.topazproject.otm.samples.SampleEmbeddable;
import org.topazproject.otm.samples.SpecialMappers;
import org.topazproject.otm.stores.ItqlStore;
import org.topazproject.otm.stores.MemStore;

import junit.framework.TestCase;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision$
 */
public class OtmTest extends TestCase {
  private static final Log log = LogFactory.getLog(OtmTest.class);

  /**
   * DOCUMENT ME!
   */
  protected SessionFactory factory = new SessionFactory();

  /**
   * DOCUMENT ME!
   *
   * @throws OtmException DOCUMENT ME!
   */
  protected void setUp() throws OtmException {
    factory.setTripleStore(new ItqlStore(URI.create("http://localhost:9091/mulgara-service/services/ItqlBeanService")));

    //factory.setTripleStore(new MemStore());
    ModelConfig ri = new ModelConfig("ri", URI.create("local:///topazproject#otmtest1"), null);
    factory.addModel(ri);

    ModelConfig sc = new ModelConfig("str",
                                     URI.create("local:///topazproject#str"),
                                     URI.create("http://topazproject.org/models#StringCompare"));
    factory.addModel(sc);

    try {
      factory.getTripleStore().dropModel(ri);
    } catch (Throwable t) {
      if (log.isDebugEnabled())
        log.debug("Failed to drop model '" + ri.getId() + "'", t);
    }

    factory.getTripleStore().createModel(ri);
    factory.getTripleStore().createModel(sc);

    factory.preload(ReplyThread.class);
    factory.preload(PublicAnnotation.class);
    factory.preload(PrivateAnnotation.class);
    factory.preload(Article.class);
    factory.preload(NoRdfType.class);
    factory.preload(SpecialMappers.class);
  }

  /**
   * DOCUMENT ME!
   *
   * @throws OtmException DOCUMENT ME!
   */
  public void test01() throws OtmException {
    Session     session = factory.openSession();
    Transaction tx      = null;

    try {
      tx = session.beginTransaction();

      session.saveOrUpdate(new PublicAnnotation(URI.create("http://localhost/annotation/1")));
      session.saveOrUpdate(new NoRdfType("http://localhost/noRdfType/1"));

      tx.commit(); // Flush happens automatically
    } catch (OtmException e) {
      try {
        if (tx != null)
          tx.rollback();
      } catch (OtmException re) {
        log.warn("rollback failed", re);
      }

      throw e; // or display error message
    } finally {
      try {
        session.close();
      } catch (OtmException ce) {
        log.warn("close failed", ce);
      }
    }

    session   = factory.openSession();
    tx        = null;

    try {
      tx = session.beginTransaction();

      Annotation a = session.get(Annotation.class, "http://localhost/annotation/1");
      assertNotNull(a);

      NoRdfType n = session.get(NoRdfType.class, "http://localhost/noRdfType/1");
      assertNotNull(n);

      n = session.get(NoRdfType.class, "http://localhost/noRdfType/2");
      assertNull(n);

      a.setCreator("Pradeep");
      a.setState(42);
      a.setType(Annotation.NS + "Comment");

      if (a.foobar == null)
        a.foobar = new SampleEmbeddable();

      a.foobar.foo   = "FOO";
      a.foobar.bar   = "BAR";

      session.saveOrUpdate(a);

      tx.commit();
    } catch (OtmException e) {
      try {
        if (tx != null)
          tx.rollback();
      } catch (OtmException re) {
        log.warn("rollback failed", re);
      }

      throw e; // or display error message
    } finally {
      try {
        session.close();
      } catch (OtmException ce) {
        log.warn("close failed", ce);
      }
    }

    session   = factory.openSession();
    tx        = null;

    try {
      tx = session.beginTransaction();

      Annotation a = session.get(Annotation.class, "http://localhost/annotation/1");

      assertNotNull(a);
      assertNotNull(a.foobar);

      assertTrue(a instanceof PublicAnnotation);

      assertEquals(42, a.getState());
      assertEquals("Pradeep", a.getCreator());
      assertEquals(Annotation.NS + "Comment", a.getType());
      assertEquals("FOO", a.foobar.foo);
      assertEquals("BAR", a.foobar.bar);

      session.delete(a);

      a = session.get(Annotation.class, "http://localhost/annotation/1");

      assertNull(a);

      tx.commit();
    } catch (OtmException e) {
      try {
        if (tx != null)
          tx.rollback();
      } catch (OtmException re) {
        log.warn("rollback failed", re);
      }

      throw e; // or display error message
    } finally {
      try {
        session.close();
      } catch (OtmException ce) {
        log.warn("close failed", ce);
      }
    }

    session   = factory.openSession();
    tx        = null;

    try {
      tx = session.beginTransaction();

      Annotation a = session.get(Annotation.class, "http://localhost/annotation/1");

      assertNull(a);

      tx.commit();
    } catch (OtmException e) {
      try {
        if (tx != null)
          tx.rollback();
      } catch (OtmException re) {
        log.warn("rollback failed", re);
      }

      throw e; // or display error message
    } finally {
      try {
        session.close();
      } catch (OtmException ce) {
        log.warn("close failed", ce);
      }
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @throws OtmException DOCUMENT ME!
   */
  public void test02() throws OtmException {
    Session     session = factory.openSession();
    Transaction tx      = null;

    try {
      tx = session.beginTransaction();

      Annotation a = new PublicAnnotation(URI.create("http://localhost/annotation/1"));
      a.setAnnotates(URI.create("http://www.plosone.org"));

      Annotation sa = new PublicAnnotation(URI.create("http://localhost/annotation/1/1"));
      sa.setAnnotates(URI.create("http://www.plosone.org"));

      a.setSupersededBy(sa);
      sa.setSupersedes(a);

      session.saveOrUpdate(a);

      tx.commit(); // Flush happens automatically
    } catch (OtmException e) {
      try {
        if (tx != null)
          tx.rollback();
      } catch (OtmException re) {
        log.warn("rollback failed", re);
      }

      throw e; // or display error message
    } finally {
      try {
        session.close();
      } catch (OtmException ce) {
        log.warn("close failed", ce);
      }
    }

    session   = factory.openSession();
    tx        = null;

    try {
      tx = session.beginTransaction();

      Annotation a = session.get(Annotation.class, "http://localhost/annotation/1/1");

      assertNotNull(a);

      Annotation old = a.getSupersedes();
      assertNotNull(old);

      assertEquals(URI.create("http://localhost/annotation/1"), old.getId());
      assertEquals(URI.create("http://www.plosone.org"), old.getAnnotates());
      assertEquals(URI.create("http://www.plosone.org"), a.getAnnotates());

      tx.commit(); // Flush happens automatically
    } catch (OtmException e) {
      try {
        if (tx != null)
          tx.rollback();
      } catch (OtmException re) {
        log.warn("rollback failed", re);
      }

      throw e; // or display error message
    } finally {
      try {
        session.close();
      } catch (OtmException ce) {
        log.warn("close failed", ce);
      }
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @throws OtmException DOCUMENT ME!
   */
  public void test03() throws OtmException {
    Session     session = factory.openSession();
    Transaction tx      = null;

    try {
      tx = session.beginTransaction();

      Annotation  a  = new PublicAnnotation(URI.create("http://localhost/annotation/1"));
      ReplyThread r  = new ReplyThread(URI.create("http://localhost/reply/1"));
      ReplyThread rr = new ReplyThread(URI.create("http://localhost/reply/1/1"));

      a.addReply(r);
      r.addReply(rr);

      session.saveOrUpdate(a);

      tx.commit(); // Flush happens automatically
    } catch (OtmException e) {
      try {
        if (tx != null)
          tx.rollback();
      } catch (OtmException re) {
        log.warn("rollback failed", re);
      }

      throw e; // or display error message
    } finally {
      try {
        session.close();
      } catch (OtmException ce) {
        log.warn("close failed", ce);
      }
    }

    session   = factory.openSession();
    tx        = null;

    try {
      tx = session.beginTransaction();

      Annotation a = session.get(Annotation.class, "http://localhost/annotation/1");

      assertNotNull(a);

      List<ReplyThread> replies = a.getReplies();
      assertNotNull(replies);
      assertEquals(1, replies.size());

      ReplyThread r = replies.get(0);
      assertNotNull(r);
      assertEquals(URI.create("http://localhost/reply/1"), r.getId());

      replies = r.getReplies();
      assertNotNull(replies);
      assertEquals(1, replies.size());

      r = replies.get(0);
      assertNotNull(r);
      assertEquals(URI.create("http://localhost/reply/1/1"), r.getId());

      tx.commit(); // Flush happens automatically
    } catch (OtmException e) {
      try {
        if (tx != null)
          tx.rollback();
      } catch (OtmException re) {
        log.warn("rollback failed", re);
      }

      throw e; // or display error message
    } finally {
      try {
        session.close();
      } catch (OtmException ce) {
        log.warn("close failed", ce);
      }
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @throws OtmException DOCUMENT ME!
   */
  public void test04() throws OtmException {
    Session     session = factory.openSession();
    Transaction tx      = null;

    URI         id1     = URI.create("http://localhost/annotation/1");
    URI         id2     = URI.create("http://localhost/annotation/2");
    URI         id3     = URI.create("http://localhost/annotation/3");

    try {
      tx = session.beginTransaction();

      Annotation a1 = new PublicAnnotation(id1);
      Annotation a2 = new PublicAnnotation(id2);
      Annotation a3 = new PublicAnnotation(id3);

      a1.setAnnotates(URI.create("foo:1"));
      a2.setAnnotates(URI.create("foo:1"));
      a3.setAnnotates(URI.create("bar:1"));
      a1.setCreator("Eric");

      a1.setSupersededBy(a2);
      a2.setSupersedes(a1);
      a2.setSupersededBy(a3);
      a3.setSupersedes(a2);

      session.saveOrUpdate(a1);
      session.saveOrUpdate(a2);
      session.saveOrUpdate(a3);

      tx.commit(); // Flush happens automatically
    } catch (OtmException e) {
      try {
        if (tx != null)
          tx.rollback();
      } catch (OtmException re) {
        log.warn("rollback failed", re);
      }

      throw e; // or display error message
    } finally {
      try {
        session.close();
      } catch (OtmException ce) {
        log.warn("close failed", ce);
      }
    }

    session   = factory.openSession();
    tx        = null;

    try {
      tx = session.beginTransaction();

      List l =
        session.createCriteria(Annotation.class).add(Restrictions.eq("annotates", "foo:1")).list();

      assertEquals(2, l.size());

      Annotation a1 = (Annotation) l.get(0);
      Annotation a2 = (Annotation) l.get(1);

      assertEquals(URI.create("foo:1"), a1.getAnnotates());
      assertEquals(URI.create("foo:1"), a2.getAnnotates());

      assertTrue(id1.equals(a1.getId()) || id1.equals(a2.getId()));
      assertTrue(id2.equals(a1.getId()) || id2.equals(a2.getId()));

      l = session.createCriteria(Annotation.class).add(Restrictions.id(id3.toString())).list();

      assertEquals(1, l.size());

      a1 = (Annotation) l.get(0);
      assertEquals(URI.create("bar:1"), a1.getAnnotates());
      assertTrue(id3.equals(a1.getId()));

      l = session.createCriteria(Annotation.class).add(Restrictions.eq("annotates", "foo:1"))
                  .add(Restrictions.id(id3.toString())).list();

      assertEquals(0, l.size());

      l = session.createCriteria(Annotation.class).add(Restrictions.eq("annotates", "foo:1"))
                  .add(Restrictions.id(id1.toString())).list();

      assertEquals(1, l.size());
      a1 = (Annotation) l.get(0);
      assertEquals(URI.create("foo:1"), a1.getAnnotates());
      assertTrue(id1.equals(a1.getId()));

      l = session.createCriteria(Annotation.class)
                  .add(Restrictions.conjunction().add(Restrictions.eq("annotates", "foo:1"))
                                    .add(Restrictions.id(id1.toString()))).list();

      assertEquals(1, l.size());
      a1 = (Annotation) l.get(0);
      assertEquals(URI.create("foo:1"), a1.getAnnotates());
      assertTrue(id1.equals(a1.getId()));

      l = session.createCriteria(Annotation.class)
                  .add(Restrictions.disjunction().add(Restrictions.eq("annotates", "foo:1"))
                                    .add(Restrictions.id(id1.toString()))).list();

      assertEquals(2, l.size());

      a1   = (Annotation) l.get(0);
      a2   = (Annotation) l.get(1);

      assertEquals(URI.create("foo:1"), a1.getAnnotates());
      assertEquals(URI.create("foo:1"), a2.getAnnotates());

      assertTrue(id1.equals(a1.getId()) || id1.equals(a2.getId()));
      assertTrue(id2.equals(a1.getId()) || id2.equals(a2.getId()));

      l = session.createCriteria(Annotation.class)
                  .add(Restrictions.walk("supersededBy", id3.toString())).list();

      assertEquals(2, l.size());

      a1   = (Annotation) l.get(0);
      a2   = (Annotation) l.get(1);

      assertTrue(id1.equals(a1.getId()) || id1.equals(a2.getId()));
      assertTrue(id2.equals(a1.getId()) || id2.equals(a2.getId()));

      l = session.createCriteria(Annotation.class)
                  .add(Restrictions.walk("supersedes", id2.toString())).list();

      assertEquals(1, l.size());

      Annotation a3 = (Annotation) l.get(0);

      assertTrue(id3.equals(a3.getId()));

      l = session.createCriteria(Annotation.class)
                  .add(Restrictions.trans("supersededBy", id3.toString())).list();

      assertEquals(2, l.size());

      a1   = (Annotation) l.get(0);
      a2   = (Annotation) l.get(1);

      assertTrue(id1.equals(a1.getId()) || id1.equals(a2.getId()));
      assertTrue(id2.equals(a1.getId()) || id2.equals(a2.getId()));

      l = session.createCriteria(Annotation.class)
                  .add(Restrictions.trans("supersedes", id2.toString())).list();

      assertEquals(1, l.size());

      a3 = (Annotation) l.get(0);

      assertTrue(id3.equals(a3.getId()));

      l = session.createCriteria(Annotation.class).add(Restrictions.ne("annotates", "foo:1")).list();

      assertEquals(1, l.size());

      a3 = (Annotation) l.get(0);

      assertTrue(id3.equals(a3.getId()));

      l = session.createCriteria(Annotation.class).add(Restrictions.ne("annotates", "bar:1"))
                  .setFirstResult(0).setMaxResults(1).list();

      assertEquals(1, l.size());

      a1 = (Annotation) l.get(0);

      assertTrue(id1.equals(a1.getId()) || id2.equals(a1.getId()));

      l = session.createCriteria(Annotation.class).add(Restrictions.ne("annotates", "bar:1"))
                  .setFirstResult(1).setMaxResults(1).addOrder(Order.asc("annotates")).list();

      assertEquals(1, l.size());

      a1 = (Annotation) l.get(0);

      assertTrue(id1.equals(a1.getId()) || id2.equals(a1.getId()));

      // Eric:
      Criteria c = session.createCriteria(Annotation.class);
      System.out.println(Restrictions.eq("annotates", "foo:1").toItql(c, "x", "eric"));
      c = session.createCriteria(Annotation.class);
      System.out.println(Restrictions.eq("creator", "Fudge").toItql(c, "$x", "eric"));

      l = session.createCriteria(Annotation.class).add(Restrictions.eq("creator", "Eric")).list();
      assertEquals(1, l.size());

      l = session.createCriteria(Annotation.class).add(Restrictions.gt("creator", "Dave")).list();
      assertEquals(1, l.size());

      l = session.createCriteria(Annotation.class).add(Restrictions.gt("creator", "Fred")).list();
      assertEquals(0, l.size());
      //

      tx.commit(); // Flush happens automatically
    } catch (OtmException e) {
      try {
        if (tx != null)
          tx.rollback();
      } catch (OtmException re) {
        log.warn("rollback failed", re);
      }

      throw e; // or display error message
    } finally {
      try {
        session.close();
      } catch (OtmException ce) {
        log.warn("close failed", ce);
      }
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @throws OtmException DOCUMENT ME!
   */
  public void test05() throws OtmException {
    Session     session = factory.openSession();
    Transaction tx      = null;

    try {
      tx = session.beginTransaction();

      SpecialMappers m = new SpecialMappers("http://localhost/sm/1");
      m.list.add("l1");
      m.list.add("l2");

      m.bag.add("b1");
      m.bag.add("b2");

      m.seq.add("s1");
      m.seq.add("s2");
      m.seq.add("s3");
      m.seq.add("s4");
      m.seq.add("s5");
      m.seq.add("s6");
      m.seq.add("s7");
      m.seq.add("s8");
      m.seq.add("s9");
      m.seq.add("s10");
      m.seq.add("s11");

      m.alt.add("a1");
      m.alt.add("a2");

      session.saveOrUpdate(m);

      tx.commit(); // Flush happens automatically
    } catch (OtmException e) {
      try {
        if (tx != null)
          tx.rollback();
      } catch (OtmException re) {
        log.warn("rollback failed", re);
      }

      throw e; // or display error message
    } finally {
      try {
        session.close();
      } catch (OtmException ce) {
        log.warn("close failed", ce);
      }
    }

    session   = factory.openSession();
    tx        = null;

    try {
      tx = session.beginTransaction();

      SpecialMappers m = session.get(SpecialMappers.class, "http://localhost/sm/1");
      assertNotNull(m);

      assertEquals(2, m.list.size());
      assertTrue(m.list.contains("l1") && m.list.contains("l2"));

      assertEquals(2, m.bag.size());
      assertTrue(m.bag.contains("b1") && m.bag.contains("b2"));

      assertEquals(11, m.seq.size());
      assertTrue(m.seq.contains("s1") && m.seq.contains("s2"));

      assertEquals(2, m.alt.size());
      assertTrue(m.alt.contains("a1") && m.alt.contains("a2"));

      assertTrue(m.alt.get(0).equals("a1") && m.alt.get(1).equals("a2"));
      assertTrue(m.seq.get(0).equals("s1"));
      assertTrue(m.seq.get(1).equals("s2"));
      assertTrue(m.seq.get(2).equals("s3"));
      assertTrue(m.seq.get(3).equals("s4"));
      assertTrue(m.seq.get(4).equals("s5"));
      assertTrue(m.seq.get(5).equals("s6"));
      assertTrue(m.seq.get(6).equals("s7"));
      assertTrue(m.seq.get(7).equals("s8"));
      assertTrue(m.seq.get(8).equals("s9"));
      assertTrue(m.seq.get(9).equals("s10"));
      assertTrue(m.seq.get(10).equals("s11"));

      tx.commit();
    } catch (OtmException e) {
      try {
        if (tx != null)
          tx.rollback();
      } catch (OtmException re) {
        log.warn("rollback failed", re);
      }

      throw e; // or display error message
    } finally {
      try {
        session.close();
      } catch (OtmException ce) {
        log.warn("close failed", ce);
      }
    }
  }
}
