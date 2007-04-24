package org.topazproject.otm.samples;

import java.net.URI;

import org.topazproject.otm.annotations.Rdf;

@Rdf(Annotea.NS + "Public")
public class PublicAnnotation extends Annotation {
  @Rdf(Rdf.topaz + "hasNote")
  public String note;

  public PublicAnnotation() {
  }

  public PublicAnnotation(URI id) {
    super(id);
  }
}
