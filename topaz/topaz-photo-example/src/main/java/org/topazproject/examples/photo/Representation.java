package org.topazproject.examples.photo;

import java.net.URI;
import java.util.Set;

import org.topazproject.otm.CascadeType;
import org.topazproject.otm.Blob;
import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.GeneratedValue;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.Predicate;

@Entity(graph="photo", types={"topaz:Representation"})
public class Representation {
  private URI id;
  private Set<String> tags;
  private Blob  image;
  private Photo photo;
  private String contentType;

  public URI getId() { return id; }
  @Id
  @GeneratedValue
  public void setId(URI id) { this.id = id; }

  public Set<String> getTags() { return tags; }
  @Predicate(uri = "topaz:tag")
  public void setTags(Set<String> tags) { this.tags = tags; }

  public Blob getImage() { return image; }
  @org.topazproject.otm.annotations.Blob
  public void setImage(Blob image) { this.image = image; }

  public Photo getPhoto() {return photo;}
  @Predicate(uri="topaz:representation", inverse=Predicate.BT.TRUE,
      notOwned=Predicate.BT.TRUE, cascade={CascadeType.peer})
  public void setPhoto(Photo photo) {this.photo = photo;}
  @Predicate(uri = "topaz:contentType")
  public String getContentType() {
    return contentType;
  }
  public void setContentType(String contentType) {
    this.contentType = contentType;
  }
}
