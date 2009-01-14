package org.topazproject.examples.photo;

import java.net.URI;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.topazproject.otm.CascadeType;
import org.topazproject.otm.FetchType;
import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.Predicate;

@Entity(graph="photo", types={"topaz:Photo"})
public class Photo {
  private URI id;
  private String title;
  private Date date;
  private FoafPerson creator;
  private Set<FoafPerson> depictedPeople = new HashSet<FoafPerson>();

  public URI getId() {return id;}
  @Id
  public void setId(URI id) {this.id = id;}

  public String getTitle() {return title;}
  @Predicate(uri="dc:title")
  public void setTitle(String title) {this.title = title;}

  public Date getDate(){return date;}
  @Predicate(uri="dc:date")
  public void setDate(Date date) {this.date = date;}

  public FoafPerson getCreator(){return creator;}
  @Predicate(uri="dc:creator", cascade={CascadeType.peer}, fetch=FetchType.lazy)
  public void setCreator(FoafPerson creator) {this.creator = creator;}

  public Set<FoafPerson> getDepictedPeople(){
    return depictedPeople;
  }
  @Predicate(uri="foaf:depicts", cascade={CascadeType.peer}, fetch=FetchType.lazy)
  public void setDepictedPeople(Set<FoafPerson> depictedPeople) {
    this.depictedPeople = depictedPeople;
  }
}
