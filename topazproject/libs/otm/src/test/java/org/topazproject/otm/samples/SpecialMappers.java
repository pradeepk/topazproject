/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm.samples;

import java.util.List;
import java.util.ArrayList;

import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.Model;
import org.topazproject.otm.annotations.BaseUri;
import org.topazproject.otm.annotations.Rdf;
import org.topazproject.otm.annotations.RdfList;
import org.topazproject.otm.annotations.RdfBag;
import org.topazproject.otm.annotations.RdfAlt;
import org.topazproject.otm.annotations.RdfSeq;

@BaseUri(Rdf.topaz)
@Model("ri")
public class SpecialMappers {
  @Id
  public String id;
  @RdfList
  public List<String> list = new ArrayList<String>();
  @RdfBag
  public List<String> bag = new ArrayList<String>();
  @RdfAlt
  public List<String> alt = new ArrayList<String>();
  @RdfSeq
  public List<String> seq = new ArrayList<String>();

  public SpecialMappers() {
  }

  public SpecialMappers(String id) {
    this.id = id;
  }
}
