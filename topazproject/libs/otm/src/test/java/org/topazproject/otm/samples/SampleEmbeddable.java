package org.topazproject.otm.samples;

import org.topazproject.otm.annotations.Embeddable;
import org.topazproject.otm.annotations.BaseUri;
import org.topazproject.otm.annotations.Rdf;

@Embeddable
@BaseUri(Rdf.topaz)
public class SampleEmbeddable {

  public String foo;
  public String bar;

}
