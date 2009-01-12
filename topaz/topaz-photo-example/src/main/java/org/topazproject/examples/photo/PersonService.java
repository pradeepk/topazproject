package org.topazproject.examples.photo;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.query.Results;

public class PersonService {

  public List<FoafPerson> findPeople(Session session, String givenname, 
		  String surname, boolean wildmatch) throws OtmException {
    givenname = (givenname != null) ? givenname.trim() : "";
    surname = (surname != null) ? surname.trim() : "";

    String query = "select p from FoafPerson p";
    String next = " where ";
    if (givenname.length() > 0) {
      query = query + next + " p.givenname = '" + givenname + "'";
      next = " and ";
    }
    if (surname.length() > 0)
      query = query + next + " p.surname = '" + surname + "'";

    Results results = session.createQuery(query + ";").execute();

    List<FoafPerson> people = new ArrayList<FoafPerson>();
    while(results.next()) {
      FoafPerson person = (FoafPerson) results.get(0);
      if (person == null)
	continue;  // can happen when filters are enabled
      if (!wildmatch) {
        String gn = (person.getGivenname() != null) ? person.getGivenname() : "";
        String sn = (person.getSurname() != null) ? person.getSurname() : "";
	// exact match required
        if (!gn.equals(givenname) || !sn.equals(surname))
	  continue;
      }
      people.add(person);
    }

    return people;
  }

  public FoafPerson newPerson(String givenname, String surname) {
    givenname = (givenname != null) ? givenname.trim() : "";
    givenname = (givenname.length() == 0) ? null : givenname;
    surname = (surname != null) ? surname.trim() : "";
    surname = (surname.length() == 0) ? null : surname;

    // If both names are empty, then don't create
    if ((givenname == null) && (surname == null))
      return null;

    // Create a new person
    FoafPerson person = new FoafPerson();
    person.setGivenname(givenname);
    person.setSurname(surname);
    return person;
  }
}
