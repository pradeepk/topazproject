/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.action;

import com.opensymphony.xwork.ActionSupport;
import org.apache.commons.collections.map.ListOrderedMap;

import java.util.Map;

/**
 * A provider of all listings for select boxes in an html page.
 */
public class ListingAction extends ActionSupport {

  public String execute() throws Exception {
    return SUCCESS;
  }

  /** return a map of all Organization Types */
  public Map<String, String> getAllOrganizationTypes() {
    final Map<String, String> allOrgTypes = new ListOrderedMap();
    allOrgTypes.put("", "Choose One");
    allOrgTypes.put("University", "University");
    allOrgTypes.put("Hospital", "Hospital");
    allOrgTypes.put("Industry", "Industry (Research)");
    allOrgTypes.put("Industry Non-research", "Industry (Non-research)");
    allOrgTypes.put("Government Non-research", "Government (Non-Research)");
    allOrgTypes.put("Library", "Library");
    allOrgTypes.put("School", "School");
    allOrgTypes.put("Private Address", "Private Address");
    return allOrgTypes;
  }

  /** return a map of all titles */
  public Map<String, String> getAllTitles() {
    final Map<String, String> allTitles = new ListOrderedMap();
    allTitles.put("", "Choose One");
    allTitles.put("Professor", "Professor");
    allTitles.put("Dr", "Dr.");
    allTitles.put("Mr", "Mr.");
    allTitles.put("Mrs", "Mrs.");
    allTitles.put("Ms", "Ms.");
    allTitles.put("Other", "Other");
    return allTitles;
  }

  /** return a map of all position types */
  public Map<String, String> getAllPositionTypes() {
    final Map<String, String> allPositionTypes = new ListOrderedMap();
    allPositionTypes.put("", "Choose One");
    allPositionTypes.put("Head of Department/Director", "Head of Department/Director");
    allPositionTypes.put("Professor/Group Leader", "Professor/Group Leader");
    allPositionTypes.put("Physician", "Physician");
    allPositionTypes.put("Post-Doctoral researcher", "Post-Doctoral researcher");
    allPositionTypes.put("Post-Graduate student", "Post-Graduate student");
    allPositionTypes.put("Undergraduate student", "Undergraduate student");
    allPositionTypes.put("Other", "Other");
    return allPositionTypes;
  }

  /** return a map of all url descriptions */
  public Map<String, String> getAllUrlDescriptions() {
    final Map<String, String> allUrlDescriptions = new ListOrderedMap();
    allUrlDescriptions.put("", "Choose One");
    allUrlDescriptions.put("Personal", "Personal");
    allUrlDescriptions.put("Laboratory", "Laboratory");
    allUrlDescriptions.put("Departmental", "Departmental");
    allUrlDescriptions.put("Blog", "Blog");
    return allUrlDescriptions;
  }
}
