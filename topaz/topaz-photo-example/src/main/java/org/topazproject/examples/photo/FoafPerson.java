package org.topazproject.examples.photo;

import java.util.HashSet;
import java.util.Set;

import org.topazproject.otm.CascadeType;
import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.Predicate;
import org.topazproject.otm.annotations.Searchable;

@Entity(types={"foaf:Person"})
public class FoafPerson extends FoafAgent {
  private String givenname, surname;
  private Set<Photo> myPhotos = new HashSet<Photo>();
  private Set<Photo> depictedIn = new HashSet<Photo>();

  public String getGivenname() {return givenname;}
  @Predicate()
  @Searchable(index="lucene")
  public void setGivenname(String name) {this.givenname = name;}

  public String getSurname() {return surname;}
  @Predicate()
  @Searchable(index="lucene")
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

