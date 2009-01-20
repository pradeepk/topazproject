package org.topazproject.examples.photo;

import java.io.InputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.OutputStream;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.SessionFactory;

@SuppressWarnings("serial")
public class PhotoServlet extends HttpServlet {
  private static final Log    log  = LogFactory.getLog(PhotoServlet.class);
  private SessionFactory factory;
  private PhotoService   photoService;
  private PersonService  personService;

  @Override
  public void init() throws ServletException {
    TopazConfigurator conf = new TopazConfigurator();
    factory        = conf.getSessionFactory();
    photoService   = new PhotoService();
    personService  = new PersonService();
  }

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
             throws ServletException, IOException {
    process(req, resp);
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp)
              throws ServletException, IOException {
    process(req, resp);
  }

  protected void process(HttpServletRequest req, HttpServletResponse resp)
                  throws ServletException, IOException {
    Session session = null;

    try {
      session = factory.openSession();
      session.beginTransaction();
      process(req, resp, session);
      session.getTransaction().commit();
    } catch (OtmException e) {
      throw new ServletException("Processing error", e);
    } catch (FileUploadException e) {
      throw new ServletException("Processing error", e);
    } finally {
      try {
        session.close();
      } catch (Throwable t) {
      }
    }
  }

  protected void process(HttpServletRequest req, HttpServletResponse resp,
      Session session) throws IOException, OtmException, FileUploadException {

 // Check that we have a file upload request
    if (ServletFileUpload.isMultipartContent(req)) {
      processUpload(req, resp, session);
      return;
    }

    String action = req.getParameter("action");

    if ((action == null) || "list".equals(action)) {
      respond(session, resp, null, null);
    } else {
      String id = req.getParameter("id");
      if (id != null)
        id = id.trim();
      if (id.length() == 0)
        id = null;

      if (id == null) {
        respond(session, resp, "id must be specified", "red");
      } else if ("create".equals(action) || "update".equals(action)) {
        processCreate(id, action, req, resp, session);
      } else if ("delete".equals(action)) {
        log.info("About to " + action + " object with id = " + id);
        Photo photo = session.get(Photo.class, id);
        if (photo != null)
          session.delete(photo);

        FoafPerson person = session.get(FoafPerson.class, id);
        if (person != null)
          session.delete(person);

        respond(session, resp, action + "d object with id : " + id, "green");
      } else if ("create depiction".equals(action) || "delete depiction".equals(action))  {
        processDepiction(id, action, req, resp, session);
      } else if ("show image".equals(action) || "delete image".equals(action))  {
        processImage(id, action, req, resp, session);
      } else {
        respond(session, resp, "unknown action '" + action + "'", "red");
      }
    }
  }

  protected FoafPerson personFromName(Session session, String givenname, String surname) {
    // Try an exact match first
    List<FoafPerson> people = personService.findPeople(session, givenname,
                                                       surname, false);
    FoafPerson person;
    boolean wild = false;
    if (people.size() == 0) {
      // Try a wild match
      people = personService.findPeople(session, givenname, surname, true);
      wild = true;
    }

    switch(people.size()) {
      case 0:
        person = personService.newPerson(givenname, surname);
        break;
      case 1:
        person = people.get(0);
        break;
      default:
        person = wild ? personService.newPerson(givenname, surname)
                       : people.get(0);
    }

    if ((person != null) && (person.getId() == null))
      log.info("About to create a new FoafPerson with name = "
               + person.getGivenname() + " " + person.getSurname());

    return person;
  }

  protected void processCreate(String id, String action, HttpServletRequest req
                               , HttpServletResponse resp, Session session)
                               throws IOException, OtmException {
    URI uri;
    try {
      uri = new URI(id);
    } catch (URISyntaxException e) {
      respond(session, resp, "id must be a valid URI", "red");
      return;
    }

    if (!uri.isAbsolute()) {
      respond(session, resp, "id must be an absolute URI", "red");
      return;
    }

    String givenname = req.getParameter("givenname");
    String surname = req.getParameter("surname");
    FoafPerson creator = personFromName(session, givenname, surname);
    log.info("About to " + action + " photo with id = " + id);
    photoService.createPhoto(session, uri, req.getParameter("title"), creator);
    respond(session, resp, action + "d photo with id : " + id, "green");
  }

  protected void processDepiction(String id, String action,
                           HttpServletRequest req,HttpServletResponse resp,
                           Session session) throws IOException, OtmException {
    URI uri;
    try {
      uri = new URI(id);
    } catch (URISyntaxException e) {
      respond(session, resp, "id must be a valid URI", "red");
      return;
    }

    if (!uri.isAbsolute()) {
      respond(session, resp, "id must be an absolute URI", "red");
      return;
    }

    Photo photo = session.get(Photo.class, id);
    if (photo == null) {
      respond(session, resp, "no photo exists with id: " + id, "red");
      return;
    }

    String givenname = req.getParameter("givenname");
    String surname = req.getParameter("surname");
    FoafPerson depicted = personFromName(session, givenname, surname);

    if (depicted == null) {
      respond(session, resp, "must enter a name for the depicted person", "red");
      return;
    }

    log.info("About to " + action + " for photo with id = " + id);
    if (action.equals("create depiction"))
      photo.getDepictedPeople().add(depicted);
    else
      photo.getDepictedPeople().remove(depicted);

    respond(session, resp, action + " completed for photo with id : " + id, "green");
  }

  protected void processImage(String id, String action,
                                  HttpServletRequest req,HttpServletResponse resp,
                                  Session session) throws IOException, OtmException {
    Representation rep = session.get(Representation.class, id);
    if (rep == null) {
      respond(session, resp, "no representation exists with id: " + id, "red");
      return;
    }

    if ("delete image".equals(action)) {
      /**
       * Note that we only need to remove the reference to this representation
       * from Photo. Topaz translates that to a removal from the triple-store.
       * This is because it is tracking changes to all attached objects to the
       * Session.
       */
      rep.getPhoto().getRepresentations().remove(rep);
      respond(session, resp, action + " completed for representation with id : " + id, "green");
      return;
    }

    if (!rep.getImage().exists()) {
      respond(session, resp, "no image has been uploaded for representation with id: " + id, "red");
      return;
    }

    resp.setContentType(rep.getContentType());
    OutputStream out;
    IOUtils.copyLarge(rep.getImage().getInputStream(), out = resp.getOutputStream());
    out.flush();
  }

  protected void processUpload(HttpServletRequest req, HttpServletResponse resp,
                               Session session) throws IOException, OtmException,
                               FileUploadException {
    ServletFileUpload upload = new ServletFileUpload();
    FileItemIterator iter = upload.getItemIterator(req);

    String id = null;
    Set<String> tset = new HashSet<String>();
    Photo photo = null;
    Representation rep = null;
    OutputStream out = null;

    while (iter.hasNext()) {
      FileItemStream item = iter.next();
      String name = item.getFieldName();
      InputStream stream = item.openStream();
      if (!item.isFormField()) {
        if (photo == null) {
          respond(session, resp, "no photo id specified", "red");
          return;
        }
        if (rep == null) {
          respond(session, resp, "no tags specified", "red");
          return;
        }
        rep.setContentType(item.getContentType());
        // Topaz blobs are txn scoped and therefore are saved only on a commit.
        IOUtils.copyLarge(stream, out = rep.getImage().getOutputStream());
        out.close();
      } else {
        String val = Streams.asString(stream);
        if (name.equals("id")) {
          id = val;
          photo = session.get(Photo.class, id);
          if (photo == null)
            break;
        }
        else if (name.equals("tags")) {
          if (photo == null)
            break;
          String[] tags = val.split(",");
          for (String tag : tags) {
            tag = tag.trim();
            if (!tag.isEmpty())
              tset.add(tag);
          }
          if (tset.isEmpty())
            break;
          for (String tag : tset) {
            rep = photo.findRepresentation(tag);
            if (rep != null) {
              rep.getTags().addAll(tset);
              break;
            }
          }
          if (rep == null) {
            rep = new Representation();
            rep.setPhoto(photo);
            rep.setTags(tset);
            /*
             * Need to explicitly save here so that Topaz allocates the Blob.
             * For streaming Blobs, Topaz is the factory. If we had used a
             * byte[] for the blob, then there was no need to save here. The
             * addition to representations-set of the Photo object would have
             * been sufficient in that case. (See object state tracking in Topaz)
             */
            session.saveOrUpdate(rep);
            photo.getRepresentations().add(rep);
          }
        }
      }
    }

    if (out != null) {
      respond(session, resp, "successfully saved image representations "
          + rep.getTags() + " for photo " + id, "green");
    } else if (rep != null) {
      if (rep.getImage().exists())
       respond(session, resp, "successfully updated representation "
           + rep.getTags() + " for photo " + id, "green");
      else
        respond(session, resp, "representation " + rep.getTags() + " without an image "
            + " created/updated for photo " + id, "yellow");
    } else if (photo != null) {
        respond(session, resp, "missing tag for image. upload rejected.", "red");
    } else if (id != null) {
      respond(session, resp, "no such photo with id : " + id, "red");
    } else {
      respond(session, resp, "must specify a photo id", "red");
    }
  }

  protected void respond(Session session, HttpServletResponse resp,
      String message, String color) throws IOException, OtmException {
    PrintWriter out = resp.getWriter();
    out.println("<html><head><title>Photo Manager</title></head><body>");

    if (message != null)
      out.println("<b><i><font color='" + color + "'>"  + message
                  + "</font></i></b>");

    printPhotos(session, out);
    printImages(session, out);
    printDepictions(session, out);
    printPeople(session, out);

    out.println("</body></html>");
    out.flush();
  }

  protected void printPhotos(Session session, PrintWriter out)
    throws IOException, OtmException {
    out.println("<h2>Photo Manager</h2>");
    out.println("<table border='2'>");
    out.println("<tr><th>id</th><th>title</th><th>givenname</th>");
    out.println("<th>surname</th><th>action</th></tr>");
    out.println("<tr><form method='post'>");
    out.println("<td><input name='id'></td><td><input name='title'></td>");
    out.println("<td><input name='givenname'></td>");
    out.println("<td><input name='surname'></td>");
    out.println("<td><input type='submit' name='action' value='create'></td>");
    out.println("</form></tr>");

    for (Photo photo : photoService.listPhotos(session)) {
      FoafPerson creator = photo.getCreator();
      String gn = (creator == null) ? "" : creator.getGivenname();
      String sn = (creator == null) ? "" : creator.getSurname();
      if (gn == null)
        gn = "";
      if (sn == null)
        sn = "";
      out.println("<tr><form method='post'>");
      out.println("<td><input name='id' type='hidden' value='" + photo.getId() + "'>"
                  + photo.getId() + "</td>");
      out.println("<td><input name='title' value='" + photo.getTitle() + "'></td>");
      out.println("<td><input name='givenname' value='" + gn + "'></td>");
      out.println("<td><input name='surname' value='" + sn + "'></td>");
      out.println("<td><input type='submit' name='action' value='update'>");
      out.println("<input type='submit' name='action' value='delete'></td>");
      out.println("</form></tr>");
    }

    out.println("</table>");
  }

  protected void printPeople(Session session, PrintWriter out)
    throws IOException, OtmException {
    out.println("<h2>People Manager</h2>");
    out.println("<table border='2'>");
    out.println("<tr><th>givenname</th>");
    out.println("<th>surname</th>");
    out.println("<th>my photos</th>");
    out.println("<th>depicted in</th>");
    out.println("<th>action</th></tr>");

    for (FoafPerson person : personService.findPeople(session, null, null, true)) {
      /*
       * Since we didn't update the myPhotos list on create/delete, we'll ask
       * Topaz to refresh. Note that all changes are automatically flush()ed
       * before executing a query. (like the query executed by findPeople()
       * above for example)
       */
      session.refresh(person);
      String gn = person.getGivenname();
      String sn = person.getSurname();
      if (gn == null)
        gn = "";
      if (sn == null)
        sn = "";
      out.println("<tr><form method='post'>");
      out.println("<td>" + gn + "</td>");
      out.println("<td>" + sn + "</td>");
      out.println("<td>" + myPhotoList(person.getMyPhotos()) + "</td>");
      out.println("<td>" + myPhotoList(person.getDepictedIn()) + "</td>");
      out.println("<td><input type='hidden' name='id' value='" + person.getId() +"'>");
      out.println("<input type='submit' name='action' value='delete'></td>");
      out.println("</form></tr>");
    }

    out.println("</table>");
  }

  protected String myPhotoList(Set<Photo> photos) {
    if ((photos == null) || (photos.size() == 0))
      return "";

    StringBuilder s = new StringBuilder();
    for (Photo p : photos)
      s.append(p.getId()).append("<br/>");

    s.setLength(s.length() - 5);

    return s.toString();
  }

  protected void printDepictions(Session session, PrintWriter out)
    throws IOException, OtmException {
    out.println("<h2>Photo Depictions</h2>");
    out.println("<table border='2'>");
    out.println("<tr><th rowspan='2'>photo id</th>");
    out.println("<th colspan='2'>Depicted person</th>");
    out.println("<th rowspan='2'>action</th></tr>");
    out.println("<tr><th>givenname</th>");
    out.println("<th>surname</th></tr>");
    out.println("<tr><form method='post'>");
    out.println("<td><input name='id'></td>");
    out.println("<td><input name='givenname'></td>");
    out.println("<td><input name='surname'></td>");
    out.println("<td><input type='submit' name='action' value='create depiction'></td>");
    out.println("</form></tr>");

    for (Photo photo : photoService.listPhotos(session)) {
      for (FoafPerson person : photo.getDepictedPeople()) {
        String gn = person.getGivenname();
        String sn = person.getSurname();
        if (gn == null)
          gn = "";
        if (sn == null)
          sn = "";
        out.println("<tr><form method='post'>");
        out.println("<td><input name='id' type='hidden' value='"
                  + photo.getId() + "'>"
                  + photo.getId() + "</td>");
        out.println("<td><input name='givenname' value='" + gn + "'></td>");
        out.println("<td><input name='surname' value='" + sn + "'></td>");
        out.println("<td><input type='submit' name='action' "
            + "value='delete depiction'></td>");
        out.println("</form></tr>");
      }
    }

    out.println("</table>");
  }

  void printImages(Session session, PrintWriter out)
    throws IOException, OtmException {
    out.println("<h2>Images</h2>");
    out.println("<table border='2'>");
    out.println("<tr><th>photo id</th>");
    out.println("<th>tags</th>");
    out.println("<th>image</th>");
    out.println("<th>action</th></tr>");
    out.println("<tr><form method='post' enctype='multipart/form-data'>");
    out.println("<td><input name='id'></td>");
    out.println("<td><input name='tags'></td>");
    out.println("<td><input name='image' type='file'></td>");
    out.println("<td><input type='submit' name='action' value='upload image'></td>");
    out.println("</form></tr>");

    for (Photo photo : photoService.listPhotos(session)) {
      for (Representation rep : photo.getRepresentations()) {
        out.println("<tr><form method='post'>");
        out.println("<td><input name='id' type='hidden' value='"
                  + rep.getId() + "'>"
                  + photo.getId() + "</td>");
        out.println("<td>");
        String sep = "";
        for (String tag : rep.getTags()) {
          out.print(sep + tag);
          sep = ",";
        }
        out.println("</td>");
        out.println("<td><input type='submit' name='action' "
            + "value='show image'></td><td><input type='submit' name='action' "
            + "value='delete image'></td>");
        out.println("</form></tr>");
      }
    }
    out.println("</table>");
  }
}
