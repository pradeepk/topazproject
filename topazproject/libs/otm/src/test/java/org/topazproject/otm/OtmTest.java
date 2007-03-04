package org.topazproject.otm;

import java.net.URI;

import java.util.List;

import org.topazproject.otm.samples.Annotation;
import org.topazproject.otm.samples.Article;
import org.topazproject.otm.samples.PrivateAnnotation;
import org.topazproject.otm.samples.PublicAnnotation;
import org.topazproject.otm.samples.Reply;
import org.topazproject.otm.samples.ReplyThread;
import org.topazproject.otm.samples.SampleEmbeddable;
import org.topazproject.otm.stores.MemStore;

import junit.framework.TestCase;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision$
 */
public class OtmTest extends TestCase {
  /**
   * DOCUMENT ME!
   */
  protected SessionFactory factory = new SessionFactory();

  /**
   * DOCUMENT ME!
   */
  protected void setUp() {
    MemStore store = new MemStore();
    store.setInverseUri(Reply.NS + "hasReply", Reply.NS + "inReplyTo");
    store.setInverseUri(Annotation.NS + "hasAnnotation", Reply.NS + "annotates");

    factory.setTripleStore(store);
    factory.addModel(new ModelConfig("ri", URI.create("local:///topazproject#ri"), null));
    factory.preload(ReplyThread.class);
    factory.preload(PublicAnnotation.class);
    factory.preload(PrivateAnnotation.class);
    factory.preload(Article.class);
  }

  /**
   * DOCUMENT ME!
   */
  public void test01() {
    Session     session = factory.openSession();
    Transaction tx      = null;

    try {
      tx = session.beginTransaction();

      session.saveOrUpdate(new PublicAnnotation("http://localhost/annotation/1"));

      tx.commit(); // Flush happens automatically
    } catch (RuntimeException e) {
      if (tx != null)
        tx.rollback();

      throw e; // or display error message
    } finally {
      session.close();
    }

    session   = factory.openSession();
    tx        = null;

    try {
      tx = session.beginTransaction();

      Annotation a = session.get(Annotation.class, "http://localhost/annotation/1");

      assertNotNull(a);

      a.setCreator("Pradeep");
      a.setState(42);
      a.setType(Annotation.NS + "Comment");

      if (a.foobar == null)
        a.foobar = new SampleEmbeddable();

      a.foobar.foo   = "FOO";
      a.foobar.bar   = "BAR";

      session.saveOrUpdate(a);

      tx.commit();
    } catch (RuntimeException e) {
      if (tx != null)
        tx.rollback();

      throw e; // or display error message
    } finally {
      session.close();
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
    } catch (RuntimeException e) {
      if (tx != null)
        tx.rollback();

      throw e; // or display error message
    } finally {
      session.close();
    }

    session   = factory.openSession();
    tx        = null;

    try {
      tx = session.beginTransaction();

      Annotation a = session.get(Annotation.class, "http://localhost/annotation/1");

      assertNull(a);

      tx.commit();
    } catch (RuntimeException e) {
      if (tx != null)
        tx.rollback();

      throw e; // or display error message
    } finally {
      session.close();
    }
  }

  /**
   * DOCUMENT ME!
   */
  public void test02() {
    Session     session = factory.openSession();
    Transaction tx      = null;

    try {
      tx = session.beginTransaction();

      Annotation a = new PublicAnnotation("http://localhost/annotation/1");
      a.setAnnotates("http://www.plosone.org");

      Annotation sa = new PublicAnnotation("http://localhost/annotation/1/1");
      sa.setAnnotates("http://www.plosone.org");

      a.setSupersededBy(sa);
      sa.setSupersedes(a);

      session.saveOrUpdate(a);

      tx.commit(); // Flush happens automatically
    } catch (RuntimeException e) {
      if (tx != null)
        tx.rollback();

      throw e; // or display error message
    } finally {
      session.close();
    }

    session   = factory.openSession();
    tx        = null;

    try {
      tx = session.beginTransaction();

      Annotation a = session.get(Annotation.class, "http://localhost/annotation/1/1");

      assertNotNull(a);

      Annotation old = a.getSupersedes();
      assertNotNull(old);

      assertEquals("http://localhost/annotation/1", old.getId());
      assertEquals("http://www.plosone.org", old.getAnnotates());
      assertEquals("http://www.plosone.org", a.getAnnotates());

      tx.commit(); // Flush happens automatically
    } catch (RuntimeException e) {
      if (tx != null)
        tx.rollback();

      throw e; // or display error message
    } finally {
      session.close();
    }
  }

  /**
   * DOCUMENT ME!
   */
  public void test03() {
    Session     session = factory.openSession();
    Transaction tx      = null;

    try {
      tx = session.beginTransaction();

      Annotation  a  = new PublicAnnotation("http://localhost/annotation/1");
      ReplyThread r  = new ReplyThread("http://localhost/reply/1");
      ReplyThread rr = new ReplyThread("http://localhost/reply/1/1");

      a.addReply(r);
      r.addReply(rr);

      session.saveOrUpdate(a);

      tx.commit(); // Flush happens automatically
    } catch (RuntimeException e) {
      if (tx != null)
        tx.rollback();

      throw e; // or display error message
    } finally {
      session.close();
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
      assertEquals("http://localhost/reply/1", r.getId());

      replies = r.getReplies();
      assertNotNull(replies);
      assertEquals(1, replies.size());

      r = replies.get(0);
      assertNotNull(r);
      assertEquals("http://localhost/reply/1/1", r.getId());

      tx.commit(); // Flush happens automatically
    } catch (RuntimeException e) {
      if (tx != null)
        tx.rollback();

      throw e; // or display error message
    } finally {
      session.close();
    }
  }
}
