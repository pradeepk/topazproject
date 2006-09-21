/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.annotation.web;

import junit.framework.TestCase;
import org.plos.annotation.service.Annotation;

public class AnnotationPublicMaskingTest extends TestCase {
  final int PUBLIC_MASK = Annotation.PUBLIC_MASK;

  public void testShouldFindBitIntegerPublic() {
    assertEquals(1, PUBLIC_MASK & 0x001);
    assertEquals(1, PUBLIC_MASK & 0x07);
    assertEquals(1, PUBLIC_MASK & 0x06003);
    assertEquals(1, PUBLIC_MASK & 0x00d5);

  }
  
  public void testShouldFindBitIntegerNonPublic() {
    assertEquals(0, PUBLIC_MASK & 0x000);
    assertEquals(0, PUBLIC_MASK & 0x10);
    assertEquals(0, PUBLIC_MASK & 0x0b010);
    assertEquals(0, PUBLIC_MASK & 0x0996);

  }

}
