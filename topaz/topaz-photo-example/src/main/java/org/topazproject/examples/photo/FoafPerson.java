package org.topazproject.examples.photo;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.topazproject.otm.CascadeType;
import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.GeneratedValue;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.Predicate;
import org.topazproject.otm.annotations.UriPrefix;

@Entity(graph="foaf", types={"foaf:Person", "foaf:Agent"})
@UriPrefix("foaf:")
public class FoafPerson {
  private URI id;
  private String givenname, surname;
  private Set<Photo> myPhotos = new HashSet<Photo>();
  private Set<Photo> depictedIn = new HashSet<Photo>();

  public URI getId() {return id;}
  @Id
  @GeneratedValue(uriPrefix="foaf:Person/Id/")
  public void setId(URI id) {this.id = id;}

  public String getGivenname() {return givenname;}
  @Predicate()
  public void setGivenname(String name) {this.givenname = name;}

  public String getSurname() {return surname;}
  @Predicate()
  public void setSurname(String name) {this.surname = name;}

  public Set<Photo> getMyPhotos() {return myPhotos;}
  @Predicate(uri="dc:creator", inverse=Predicate.BT.TRUE,
             cascade = {CascadeType.child})
  public void setMyPhotos(Set<Photo> myPhotos) {this.myPhotos = myPhotos;}

  public Set<Photo> getDepictedIn() {return depictedIn;}
  @Predicate(uri="foaf:depicts", inverse=Predicate.BT.TRUE,
             cascade = {CascadeType.child})
  public void setDepictedIn(Set<Photo> depictedIn) {this.depictedIn = depictedIn;}
}

