package org.topazproject.otm.samples;

import org.topazproject.otm.annotations.Rdf;

@Rdf(Annotia.NS + "Public")
public class PublicAnnotation extends Annotation {

  public PublicAnnotation() {
  }

  public PublicAnnotation(String id) {
    super(id);
  }
}
