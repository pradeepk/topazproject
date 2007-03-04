package org.topazproject.otm.samples;

import org.topazproject.otm.annotations.Rdf;

@Rdf(Annotia.NS + "Private")
public class PrivateAnnotation extends Annotation {

  public PrivateAnnotation() {
  }

  public PrivateAnnotation(String id) {
    super(id);
  }
}
