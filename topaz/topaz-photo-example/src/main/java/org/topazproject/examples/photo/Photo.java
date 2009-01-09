package org.topazproject.examples.photo;

import java.net.URI;
import java.util.Date;

import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.Predicate;

@Entity(graph="photo", types={"topaz:Photo"})
public class Photo {
  private URI id;
  private String title;
  private Date date;

  public URI getId() {return id;}
  @Id
  public void setId(URI id) {this.id = id;}

  public String getTitle() {return title;}
  @Predicate(uri="dc:title")
  public void setTitle(String title) {this.title = title;}

  public Date getDate(){return date;}
  @Predicate(uri="dc:date")
  public void setDate(Date date) {this.date = date;}
}
