package org.topazproject.examples.photo;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.query.Results;

public class PhotoService {

  public void createPhoto(Session session, URI id, String title, 
      FoafPerson creator) throws OtmException {
    Photo photo = session.get(Photo.class, id.toString());
    if (photo == null) {
      photo = new Photo();
      photo.setId(id);
    }
    photo.setTitle(title);
    photo.setDate(new Date());
    photo.setCreator(creator);
    if (!session.contains(photo))
      session.saveOrUpdate(photo);
  }

  public List<Photo> listPhotos(Session session) throws OtmException {
    Results results = session.createQuery("select p from Photo p;").execute();
    List<Photo> photos = new ArrayList<Photo>();
    while(results.next())
      photos.add((Photo) results.get(0));

    return photos;
  }
}
