/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 *
 * Modified from code part of Fedora. It's license reads:
 * License and Copyright: The contents of this file will be subject to the
 * same open source license as the Fedora Repository System at www.fedora.info
 * It is expected to be released with Fedora version 2.2.
 * Copyright 2006 by The Technical University of Denmark.
 * All rights reserved.
 */
package org.topazproject.fedoragsearch.topazlucene;

import java.util.List;
import java.util.Arrays;
import java.io.File;
import java.io.IOException;
import org.topazproject.configuration.ConfigurationStore; // Wraps commons-config initialization
import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Package private class to initialize and hold shared configuration data.
 *
 * Both OperationsImpl and Statement needs some of the same shared configuration data. It
 * is read and saved here.
 *
 * @author Eric Brown
 * @version $Id$
 */
class TopazConfig {
  private static final Log   log        = LogFactory.getLog(TopazConfig.class);
  
  static final Configuration CONF       = ConfigurationStore.getInstance().getConfiguration();
  static final String FEDORAOBJ_PATH    = CONF.getString("topaz.search.fedoraobjpath",   null);
  static final String INDEX_PATH;         // see static section below
  static final String INDEX_NAME        = CONF.getString("topaz.search.indexname",       "topaz");
  static final long   CACHE_EXPIRATION  = CONF.getLong(  "topaz.search.cacheexpiration", 600000L);
  static final String FOXML2LUCENE_XSLT = CONF.getString("topaz.search.xslt.foxml2lucene",
                                                         "topazFoxmlToLucene");
  static final List   DEFAULT_FIELDS    = CONF.getList(  "topaz.search.defaultfields",
                                            Arrays.asList(new Object[]
                                              { "dc.description", "dc.title", "uva.access" }));
  
  static final String GFIND_XSLT =
    CONF.getString("topaz.search.xslt.gfind", "gfindObjectsToResultPage");
  static final String BROWSE_XSLT =
    CONF.getString("topaz.search.xslt.browse", "browseIndexTResultPage");
  static final String INDEXINFO_XSLT =
    CONF.getString("topaz.search.xslt.indexinfo", "copyXml");
  static final String UPDATE_XSLT    =
    CONF.getString("topaz.search.xslt.update", "updateIndexToResultPage");
  
  static final String INDEXINFO_XML =
    CONF.getString("topaz.search.xml.indexinfo", "indexInfo.xml");
  static final String ANALYZER_NAME =
    CONF.getString("topaz.search.analyzername",
                   "org.apache.lucene.analysis.standard.StandardAnalyzer");

  // Log some errors if necessary
  static {
    if (FEDORAOBJ_PATH == null) // may still work fine as long as don't re-index all of fedora
      log.warn("topaz.search.fedoraobjpath - location of fedora foxml files not configured");

    /* Try to configure INDEX_PATH from commons-config.
     * If this doesn't work, setup a temporary directory. Helpful for integration testing.
     */
    String indexPath = CONF.getString("topaz.search.indexpath", null);
    if (indexPath == null) {
      log.error("topaz.search.indexpath - location of lucene index not configured");
      try {
        // Create a temporary directory to stash DB
        File dir = File.createTempFile("topazlucene", "_db");
        if (!dir.delete()) // Delete the file as we want a directory
          log.error("Unable to delete temporary file " + dir);
        else {
          if (!dir.mkdir()) // Create the directory
            log.error("Unable to create temporary directory " + dir);
          else {
            indexPath = dir.toString();
            log.info("Putting topaz-lucene db in " + dir);
          }
        }
      } catch (IOException ioe) {
        log.error("Unable to create temporary directory", ioe);
      }
    }
    INDEX_PATH = indexPath;
  }
}
