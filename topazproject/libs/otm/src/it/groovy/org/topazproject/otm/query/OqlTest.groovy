/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.otm;

import org.topazproject.otm.metadata.RdfBuilder;
import org.topazproject.otm.query.Results;
import org.topazproject.otm.stores.ItqlStore;
import org.topazproject.otm.samples.Annotation;
import org.topazproject.otm.samples.Article;
import org.topazproject.otm.samples.PublicAnnotation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Integration tests for OQL.
 */
public class OqlTest extends GroovyTestCase {
  def rdf;

  void setUp() {
    def store =
        new ItqlStore("http://localhost:9091/mulgara-service/services/ItqlBeanService".toURI())
    rdf = new RdfBuilder(
        sessFactory:new SessionFactory(tripleStore:store), defModel:'ri', defBaseUri:'topaz:')

    def ri = new ModelConfig("ri", "local:///topazproject#otmtest1".toURI(), null)
    rdf.sessFactory.addModel(ri);

    rdf.sessFactory.preload(Annotation.class);
    rdf.sessFactory.preload(Article.class);
    rdf.sessFactory.preload(PublicAnnotation.class);

    try {
      store.dropModel(ri);
    } catch (Throwable t) {
    }
    store.createModel(ri)
  }

  void testOqlBasic() {
    def checker = new ResultChecker(test:this)

    URI id1 = "http://localhost/annotation/1".toURI()
    URI id2 = "http://localhost/annotation/2".toURI()
    URI id3 = "http://localhost/annotation/3".toURI()

    URI id4 = "foo:1".toURI()
    URI id5 = "bar:1".toURI()

    doInTx { s ->
      Article art  = new Article(uri:id4, title:"Yo ho ho", description:"A bottle of Rum")
      s.saveOrUpdate(art)
      s.flush()

      Annotation a1 = new PublicAnnotation(id:id1, annotates:id4)
      Annotation a2 = new PublicAnnotation(id:id2, annotates:id4, supersedes:a1)
      Annotation a3 = new PublicAnnotation(id:id3, annotates:id5, supersedes:a2,
                                           foobar:[foo:'one', bar:'two'])
      a1.supersededBy = a2
      a2.supersededBy = a3

      s.saveOrUpdate(a1)
      s.saveOrUpdate(a2)
      s.saveOrUpdate(a3)
    }

    doInTx { s ->
      // single class, simple condition
      Results r = s.doQuery("select a from Article a where a.title = 'Yo ho ho';")
      checker.verify(r) {
        row { obj (class:Article.class, uri:id4) }
      }

      // two classes, simple condition
      r = s.doQuery("""
          select art a, ann n from Article art, Annotation ann where ann.annotates = art order by n;
          """)
      checker.verify(r) {
        row { obj (class:Article.class, uri:id4); obj (class:PublicAnnotation.class, id:id1) }
        row { obj (class:Article.class, uri:id4); obj (class:PublicAnnotation.class, id:id2) }
        row { obj (class:Article.class, uri:id5); obj (class:PublicAnnotation.class, id:id3) }
      }

      // no results
      r = s.doQuery("""
            select art.publicAnnotations.note n from Article art
            where art.title = 'Yo ho ho' order by n;
            """)
      checker.verify(r) {
      }

      // no results, cast, !=
      r = s.doQuery("""
            select ann n from Annotation ann
            where cast(ann.annotates, Article).title != 'Yo ho ho' 
            order by n;
            """)
      checker.verify(r) {
      }

      // !=
      r = s.doQuery("select ann n from Annotation ann where ann.annotates != <foo:1> order by n;")
      checker.verify(r) {
        row { obj (class:PublicAnnotation.class, id:id3) }
      }

      // subquery
      r = s.doQuery("""
            select art a,
              (select pa pa from Article a2 where pa := art.publicAnnotations order by pa)
            from Article art 
            where p := art.publicAnnotations order by a;
            """)
      checker.verify(r) {
        row {
          obj (class:Article.class, uri:id5)
          subq {
            row { obj (class:PublicAnnotation.class, id:id3) }
          }
        }
        row {
          obj (class:Article.class, uri:id4)
          subq {
            row { obj (class:PublicAnnotation.class, id:id1) }
            row { obj (class:PublicAnnotation.class, id:id2) }
          }
        }
      }

      // count
      r = s.doQuery("""
            select art a, count(art.publicAnnotations) from Article art 
            where p := art.publicAnnotations order by a;
            """)
      checker.verify(r) {
        row { obj (class:Article.class, uri:id5); string ("1.0") }
        row { obj (class:Article.class, uri:id4); string ("2.0") }
      }

      // multiple orders, one by a constant
      r = s.doQuery("""
            select art a, foo f from Article art 
            where art.<rdf:type> = <topaz:Article> and foo := 'yellow' order by a, f;
            """)
      checker.verify(r) {
        row { obj (class:Article.class, uri:id4); string ('yellow') }
      }
    }
  }

  private def doInTx(Closure c) {
    Session s = rdf.sessFactory.openSession()
    s.beginTransaction()
    try {
      def r = c(s)
      s.transaction.commit()
      return r
    } catch (OtmException e) {
      try {
        s.transaction.rollback()
      } catch (OtmException oe) {
        log.warn("rollback failed", oe);
      }
      log.error("error: ${e}", e)
      throw e
    } finally {
      try {
        s.close();
      } catch (OtmException oe) {
        log.warn("close failed", oe);
      }
    }
  }
}

class ResultChecker extends BuilderSupport {
  private Stack resHist = new Stack()
  private Stack colHist = new Stack()

  private Results res;
  private int     col = 0;

  GroovyTestCase test;

  protected Object createNode(Object name) {
    return createNode(name, null, null)
  }

  protected Object createNode(Object name, Object value) {
    return createNode(name, null, value)
  }

  protected Object createNode(Object name, Map attributes) {
    return createNode(name, attributes, null)
  }

  protected Object createNode(Object name, Map attributes, Object value) {
    switch (name) {
      case 'verify':
        res = value;
        if (res.warnings)
          test.log.error "Got warnings: " + res.warnings.join(System.getProperty("line.separator"))
        test.assertNull(res.warnings);
        break;

      case 'row':
        test.assertTrue(res.next())
        col = 0;
        break;

      case 'obj':
        def o = res.get(col++);
        for (a in attributes) {
          if (a.key == 'class')
            test.assertTrue(a.value.isAssignableFrom(o."${a.key}"))
          else
            test.assertEquals(a.value, o."${a.key}")
        }
        break;

      case 'string':
        test.assertEquals(res.getString(col++), value);
        break;

      case 'uri':
        test.assertEquals(res.getURI(col++), value.toURI());
        break;

      case 'subq':
        def q = res.getSubQueryResults(col++);
        resHist.push(res)
        colHist.push(col)
        res = q
        break;

      default:
        throw new Exception("unsupported item '${name}'");
    }

    return name
  }

  protected void setParent(Object parent, Object child) {
  }

  protected void nodeCompleted(Object parent, Object node) {
    if (parent == null || node == 'subq')
      test.assertFalse(res.next())

    if (node == 'subq') {
      res = resHist.pop()
      col = colHist.pop()
    }
  }
}
