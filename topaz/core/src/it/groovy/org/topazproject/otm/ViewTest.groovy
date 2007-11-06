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

import java.net.URI;
import java.util.Date;

import org.topazproject.otm.View;
import org.topazproject.otm.annotations.Projection;
import org.topazproject.otm.annotations.SubView;
import org.topazproject.otm.annotations.View;

import org.topazproject.otm.samples.Article;
import org.topazproject.otm.samples.ObjectInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Integration tests for views.
 */
public class ViewTest extends AbstractTest {
  private static final Log log = LogFactory.getLog(ViewTest.class);

  void setUp() {
    super.setUp();

    rdf.sessFactory.preload(Article.class);
    rdf.sessFactory.preload(ViewOne.class);
    rdf.sessFactory.preload(ViewTwo.class);
    rdf.sessFactory.preload(ViewThree.class);
    rdf.sessFactory.preload(ViewThreePart.class);
  }

  void testGet() {
    def o1 = new Article([uri: "http://foo.com/bar/baz".toURI(), title: "The sum of things",
                          date: new Date("02 Nov 2007"), categories:["Fruits", "Fish"],
                          authors:["James Kirchner", "Sandra Hollister"],
                          parts:[
                            new ObjectInfo([uri: "http://foo.com/bar/baz/p1".toURI(),
                                            identifier: "info:doi/10.1371/baz/part1",
                                            representations: ["PDF", "PNG"]]),
                          ]])
    def o2 = new Article([uri: "http://foo.com/bar/duh".toURI(), title: "Omega-3",
                          date: new Date("23 Jul 2007"), categories:["Veggies", "Fruits"],
                          authors:["Peter Bellum", "Beth Dirnhum"],
                          parts:[
                            new ObjectInfo([uri: "http://foo.com/bar/duh/p1".toURI(),
                                            identifier: "info:doi/10.1371/duh/part1",
                                            representations: ["PNG"]]),
                            new ObjectInfo([uri: "http://foo.com/bar/duh/p2".toURI(),
                                            identifier: "info:doi/10.1371/duh/part2",
                                            representations: ["XML", "HTML"]])
                          ]])

    doInTx { s ->
      s.saveOrUpdate(o1);
      s.saveOrUpdate(o2);
    }

    doInTx { s ->
      // basic test
      List<ViewOne> res1 = s.createView(ViewOne.class).setParameter("cat", "Fish").list()
      assert res1.size() == 1
      assertEquals(o1.uri,  res1.get(0).uri)
      assertEquals(o1.date, res1.get(0).date)

      // subqueries, field-list != projection-list
      List<ViewTwo> res2 = s.createView(ViewTwo.class).setParameter("cat", "Fruits").list()
      assert res2.size() == 2

      assertEquals(o2.title,  res2.get(0).title)
      assertEquals(o2.authors.toList().sort(), res2.get(0).authors.toList().sort())
      assertEquals(o2.authors.toList().sort(), res2.get(0).authorsList.sort())
      assertEquals(o2.authors.toList().sort(), res2.get(0).authorsSet.toList().sort())

      assertEquals(o1.title,  res2.get(1).title)
      assertEquals(o1.authors.toList().sort(), res2.get(1).authors.toList().sort())
      assertEquals(o1.authors.toList().sort(), res2.get(1).authorsList.sort())
      assertEquals(o1.authors.toList().sort(), res2.get(1).authorsSet.toList().sort())

      // sub-views, count
      List<ViewThree> res3 = s.createView(ViewThree.class).setParameter("cat", "Fruits").list()
      assert res3.size() == 2

      assertEquals(o1.uri.toString(), res3.get(0).id)
      assertEquals(o1.authors.size(), res3.get(0).authors)
      assertEquals(o1.parts.size(),   res3.get(0).parts.size())
      def parts = o1.parts.toList().sort({ it.identifier })
      assertEquals(parts[0].identifier,      res3.get(0).parts[0].identifier)
      assertEquals(parts[0].representations, res3.get(0).parts[0].representations)

      assertEquals(o2.uri.toString(), res3.get(1).id)
      assertEquals(o2.authors.size(), res3.get(1).authors)
      assertEquals(o2.parts.size(),   res3.get(1).parts.size())
      parts = o2.parts.toList().sort({ it.identifier })
      assertEquals(parts[0].identifier,      res3.get(1).parts[0].identifier)
      assertEquals(parts[0].representations, res3.get(1).parts[0].representations)
      assertEquals(parts[1].identifier,      res3.get(1).parts[1].identifier)
      assertEquals(parts[1].representations, res3.get(1).parts[1].representations)

      // cleanup
      s.delete(o1)
      s.delete(o2)
    }
  }
}

/* basic view test */
@View(query = "select a.uri id, a.date date from Article a where a.categories = :cat ;")
class ViewOne {
  @Projection("id")
  URI uri;

  @Projection("date")
  Date date;
}

/* testing projection not used (uri), projection used multiple times (authors), and subqueries */
@View(query = """select a.uri, a.title title, (select a.authors from Article aa) authors
                 from Article a where a.categories = :cat order by title;""")
class ViewTwo {
  @Projection("title")
  String title;

  @Projection("authors")
  String[] authors;

  @Projection("authors")
  List<String> authorsList;

  @Projection("authors")
  Set<String> authorsSet;
}

/* testing projection not used (uri), projection used multiple times (authors), and subqueries */
@View(query = """select a.uri id, count(a.authors) numAuth,
                  (select oi.uri, oi.identifier ident,
                   (select oi.representations from ObjectInfo oi2) reps
                   from ObjectInfo oi where oi = a.parts order by ident) parts
                 from Article a where a.categories = :cat order by id;""")
class ViewThree {
  @Projection("id")
  String id;

  @Projection("numAuth")
  int authors;

  @Projection("parts")
  List<ViewThreePart> parts;
}

@SubView
class ViewThreePart {
  @Projection("ident")
  String identifier;

  @Projection("reps")
  Set<String> representations;
}
