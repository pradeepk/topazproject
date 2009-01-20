package org.topazproject.examples.photo;

import java.net.URI;

import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.GeneratedValue;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.Predicate;
import org.topazproject.otm.annotations.UriPrefix;

@Entity(graph="foaf", types={"foaf:Agent"})
@UriPrefix("foaf:")
public class FoafAgent {
  private URI id;
  private String mbox;

  public URI getId() {return id;}
  @Id
  @GeneratedValue(uriPrefix="foaf:Agent/Id/")
  public void setId(URI id) {this.id = id;}

  public String getMbox() { return mbox; }
  @Predicate
  public void setMbox(String mbox) { this.mbox = mbox; }
}

