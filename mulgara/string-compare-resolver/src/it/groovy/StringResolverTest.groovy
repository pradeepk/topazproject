/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

import org.topazproject.mulgara.itql.DefaultItqlClientFactory
import org.topazproject.mulgara.itql.ItqlClient
import org.topazproject.mulgara.itql.ItqlClientFactory

class StringResolverTest extends GroovyTestCase {
  String MULGARA    = 'local:///topazproject'
  String TEST_MODEL = '<local:///topazproject#EqualIgnoreCaseTests>'
  String RSLV_MODEL = '<local:///topazproject#str>'
  String RSLV_TYPE  = '<http://topazproject.org/models#StringCompare>'
  ItqlClientFactory itqlFactory = new DefaultItqlClientFactory()
  ItqlClient itql

  void setUp() {
    itql = itqlFactory.createClient(MULGARA.toURI())
    itql.setAliases([topaz:'http://rdf.topazproject.org/RDF/'])
    itql.doUpdate("create ${RSLV_MODEL} ${RSLV_TYPE};")
    itql.doUpdate("create ${TEST_MODEL};")
    itql.doUpdate("insert <foo:1> <bar:is> 'a' into ${TEST_MODEL};")
    itql.doUpdate("insert <foo:2> <bar:is> 'b' into ${TEST_MODEL};")
    itql.doUpdate("insert <foo:X> <bar:is> 'c' into ${TEST_MODEL};")
  }

  void testEqualIgnoresCase() {
    def query = """select \$s from ${TEST_MODEL} 
                    where \$s <bar:is> \$o 
                      and \$o <topaz:equalsIgnoreCase> 'B' in ${RSLV_MODEL};"""
    def ans = itql.doQuery(query)[0]
    assert ans.next()
    assert ans.getString('s') == 'foo:2'
  }

  void testEqualIgnoreCaseSubject() {
    def query = """select \$s from ${TEST_MODEL}
                    where \$s <topaz:equalsIgnoreCase> <foo:x> in ${RSLV_MODEL};"""
    def ans = itql.doQuery(query)[0]
    assert ans.next()
    assert ans.getString('s') == 'foo:X'
    assert !ans.next()
  }

  void testLt1() {
    def query = """select \$s from ${TEST_MODEL}
                    where \$s <bar:is> \$o
                      and \$o <topaz:lt> 'b' in ${RSLV_MODEL};"""
    def ans = itql.doQuery(query)[0]
    assert ans.next()
    assert ans.getString('s') == 'foo:1'
    assert !ans.next()
  }

  void testLt2() {
    def query = """select \$s from ${TEST_MODEL}
                    where \$s <bar:is> \$o
                      and \$o <topaz:lt> 'c' in ${RSLV_MODEL};"""
    def ans = itql.doQuery(query)[0]
    assert ans.next()
    assert ans.getString('s') == 'foo:1'
    assert ans.next()
    assert ans.getString('s') == 'foo:2'
    assert !ans.next()
  }

  void testLe1() {
    def query = """select \$s from ${TEST_MODEL}
                    where \$s <bar:is> \$o
                      and \$o <topaz:le> 'b' in ${RSLV_MODEL};"""
    def ans = itql.doQuery(query)[0]
    assert ans.next()
    assert ans.getString('s') == 'foo:1'
    assert ans.next()
    assert ans.getString('s') == 'foo:2'
    assert !ans.next()
  }

  void testGt1() {
    def query = """select \$s from ${TEST_MODEL}
                    where \$s <bar:is> \$o
                      and \$o <topaz:gt> 'b' in ${RSLV_MODEL};"""
    def ans = itql.doQuery(query)[0]
    assert ans.next()
    assert ans.getString('s') == 'foo:X'
    assert !ans.next()
  }

  void testGe1() {
    def query = """select \$s from ${TEST_MODEL}
                    where \$s <bar:is> \$o
                      and \$o <topaz:ge> 'b' in ${RSLV_MODEL};"""
    def ans = itql.doQuery(query)[0]
    assert ans.next()
    assert ans.getString('s') == 'foo:2'
    assert ans.next()
    assert ans.getString('s') == 'foo:X'
    assert !ans.next()
  }
}
