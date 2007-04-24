package org.topazproject.otm.samples;

import java.net.URI;

import org.topazproject.otm.annotations.Rdf;

@Rdf(Annotea.NS + "Private")
public class PrivateAnnotation extends Annotation {

  public PrivateAnnotation() {
  }

  public PrivateAnnotation(URI id) {
    super(id);
  }
}
