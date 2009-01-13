package org.topazproject.examples.photo;

import java.io.IOException;
import java.io.PrintWriter;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.SessionFactory;

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
    } finally {
      try {
        session.close();
      } catch (Throwable t) {
      }
    }
  }

  protected void process(HttpServletRequest req, HttpServletResponse resp,
      Session session) throws IOException, OtmException {
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
      } else {
        respond(session, resp, "unknown action '" + action + "'", "red");
      }
    }
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
    // Try an exact match first
    List<FoafPerson> people = personService.findPeople(session, givenname,
                                                       surname, false);
    FoafPerson creator;
    boolean wild = false;
    if (people.size() == 0) {
      // Try a wild match
      people = personService.findPeople(session, givenname, surname, true);
      wild = true;
    }

    switch(people.size()) {
      case 0:
        creator = personService.newPerson(givenname, surname);
        break;
      case 1:
        creator = people.get(0);
        break;
      default:
        creator = wild ? personService.newPerson(givenname, surname)
                       : people.get(0);
    }

    if ((creator != null) && (creator.getId() == null))
      log.info("About to create a new FoafPerson with name = " 
               + creator.getGivenname() + " " + creator.getSurname());

    log.info("About to " + action + " photo with id = " + id);
    photoService.createPhoto(session, uri, req.getParameter("title"), creator);
    respond(session, resp, action + "d photo with id : " + id, "green");
  }

  protected void respond(Session session, HttpServletResponse resp,
      String message, String color) throws IOException, OtmException {
    PrintWriter out = resp.getWriter();
    out.println("<html><head><title>Photo Manager</title></head><body>");

    if (message != null)
      out.println("<b><i><font color='" + color + "'>"  + message 
                  + "</font></i></b>");

    printPhotos(session, out);
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
}
