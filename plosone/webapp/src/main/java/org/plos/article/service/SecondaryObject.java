/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.article.service;

import org.topazproject.ws.article.ObjectInfo;
import org.topazproject.ws.article.RepresentationInfo;

/**
 * Wrapper around topaz's ObjectInfo.
 */
public class SecondaryObject {
  private final ObjectInfo objectInfo;
  private String repSmall;
  private String repMedium;
  private String repLarge;

  public SecondaryObject(final ObjectInfo objectInfo, 
                         final String repSmall, final String repMedium, final String repLarge)
  {
    this.objectInfo = objectInfo;
    this.repSmall = repSmall;
    this.repMedium = repMedium;
    this.repLarge = repLarge;

  }

  /**
   * @see org.topazproject.ws.article.ObjectInfo#getUri()
   */
  public String getUri() {
    return objectInfo.getUri();
  }

  /**
   * @see org.topazproject.ws.article.ObjectInfo#getTitle()
   */
  public String getTitle() {
    return objectInfo.getTitle();
  }

  /**
   * @see org.topazproject.ws.article.ObjectInfo#getDescription()
   */
  public String getDescription() {
    return objectInfo.getDescription();
  }

  /**
   * @see org.topazproject.ws.article.ObjectInfo#getRepresentations()
   */
  public RepresentationInfo[] getRepresentations() {
    return objectInfo.getRepresentations();
  }

  /**
   * @return the thumbnail representation for the images
   */
  public String getRepSmall() {
    return repSmall;
  }

  /**
   * @return the representation for medium size image
   */
  public String getRepMedium() {
    return repMedium;
  }

  /**
   * @return the representation for maximum size image
   */
  public String getRepLarge() {
    return repLarge;
  }
}
