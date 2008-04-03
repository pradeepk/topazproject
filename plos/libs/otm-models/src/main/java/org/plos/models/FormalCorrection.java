package org.plos.models;

import org.topazproject.otm.annotations.Entity;

@Entity(type = FormalCorrection.RDF_TYPE)
public class FormalCorrection extends Correction implements ArticleAnnotation {
  private static final long serialVersionUID = 4949878990530615857L;
  
  public static final String RDF_TYPE = Annotea.TOPAZ_TYPE_NS + "FormalCorrection";
  public String getType() {
    return RDF_TYPE;
  }

  /**
   * Human friendly string for display and debugging.
   *
   * @return String for human consumption.
   */
  public String toString() {
    return "FormalCorrection: {"
            + "type: " + getType()
            + ", " + super.toString()
            + "}";
  }
}
