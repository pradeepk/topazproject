package org.plos.util;

import com.opensymphony.util.GUID;

/**
 * $HeadURL: $
 *
 * @version: $Id: $
 */
public class UniqueTokenGenerator {

  public static String getUniqueToken() {
    return GUID.generateGUID();
  }
}
