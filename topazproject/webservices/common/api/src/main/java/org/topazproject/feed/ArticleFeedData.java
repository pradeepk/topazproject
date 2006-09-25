/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.feed;

import java.util.List;
import java.util.Date;

/**
 * Article meta data.
 *
 * @author Eric Brown
 */
public class ArticleFeedData {
  String   doi;
  String   title;
  String   description;
  Date     date;
  List     authors;
  List     categories;
  
  public String toString() {
    return "ArticleData[" + this.doi + ":" + this.date + "]";
  }
}
