/*
 * -----------------------------------------------------------------------------
 *
 * <p><b>License and Copyright: </b>The contents of this file are subject to the
 * Educational Community License (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License
 * at <a href="http://www.opensource.org/licenses/ecl1.txt">
 * http://www.opensource.org/licenses/ecl1.txt.</a></p>
 *
 * <p>Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.</p>
 *
 * <p>The entire file consists of original code.  Copyright &copy; 2002-2006 by
 * The Rector and Visitors of the University of Virginia and Cornell University.
 * All rights reserved.</p>
 *
 * -----------------------------------------------------------------------------
 */

package fedora.server.management;

import java.io.*;

import java.text.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.*;

import org.apache.xml.serialize.*;

import org.w3c.dom.*;

import fedora.common.Constants;

import fedora.server.*;

import fedora.server.errors.*;
import fedora.server.errors.authorization.AuthzException;

import fedora.server.security.Authorization;

import fedora.server.storage.*;
import fedora.server.storage.types.*;

import fedora.server.utilities.*;

import fedora.server.validation.*;

/**
 * Implements API-M without regard to the transport/messaging protocol.
 *
 * @author cwilper-cs.cornell.edu
 * @version $Id$
 */
public class DefaultManagement extends Module implements Management {
  private DOManager m_manager;

  //private String m_fedoraServerHost; 2005.3.31 not used, so why have?
  //private String m_fedoraServerPort; 2005.3.31 not used, so why have?
  private int                    m_uploadStorageMinutes;
  private int                    m_lastId;
  private File                   m_tempDir;
  private Hashtable              m_uploadStartTime;
  private ExternalContentManager m_contentManager;
  private Authorization          m_fedoraXACMLModule;

  /**
   * Creates and initializes the Management Module.
   *
   * <p></p>
   * When the server is starting up, this is invoked as part of the initialization process.
   *
   * @param moduleParameters A pre-loaded Map of name-value pairs comprising the intended
   *        configuration of this Module.
   * @param server The <code>Server</code> instance.
   * @param role The role this module fulfills, a java class name.
   *
   * @throws ModuleInitializationException If initilization values are invalid or initialization
   *         fails for some other reason.
   */
  public DefaultManagement(Map moduleParameters, Server server, String role)
                    throws ModuleInitializationException {
    super(moduleParameters, server, role);
  }

  /**
   * DOCUMENT ME!
   *
   * @throws ModuleInitializationException DOCUMENT ME!
   */
  public void initModule() throws ModuleInitializationException {
    // how many minutes should we hold on to uploaded files? default=5
    String min = getParameter("uploadStorageMinutes");

    if (min == null)
      min = "5";

    try {
      m_uploadStorageMinutes = Integer.parseInt(min);

      if (m_uploadStorageMinutes < 1) {
        throw new ModuleInitializationException("uploadStorageMinutes "
                                                + "must be 1 or more, if specified.", getRole());
      }
    } catch (NumberFormatException nfe) {
      throw new ModuleInitializationException("uploadStorageMinutes must "
                                              + "be an integer, if specified.", getRole());
    }

    // initialize storage area by 1) ensuring the directory is there
    // and 2) reading in the existing files, if any, and setting their
    // startTime to the current time.
    try {
      m_tempDir = new File(getServer().getHomeDir(), "management/upload");

      if (!m_tempDir.isDirectory()) {
        m_tempDir.mkdirs();
      }

      // put leftovers in hash, while saving highest id as m_lastId
      m_uploadStartTime = new Hashtable();

      String[] fNames            = m_tempDir.list();
      Long     leftoverStartTime = new Long(System.currentTimeMillis());
      m_lastId = 0;

      for (int i = 0; i < fNames.length; i++) {
        try {
          int id = Integer.parseInt(fNames[i]);

          if (id > m_lastId)
            m_lastId = id;

          m_uploadStartTime.put(fNames[i], leftoverStartTime);
        } catch (NumberFormatException nfe) {
          // skip files that aren't named numerically
        }
      }

      Thread t = new Thread() {
        public void run() {
           while (true) {
             try {
               /*
                * Deletion of large files take a while too.
                * So throttle it. eg. 5 files per minute.
                */
               sleep(60 * 1000);
               cleanUpTmpFiles(5);
             } catch (InterruptedException e) {
             }
           }
        }
      };

      t.setDaemon(true);
      t.setPriority(Thread.MIN_PRIORITY);
      t.start();

    } catch (Exception e) {
      e.printStackTrace();
      throw new ModuleInitializationException("Error while initializing "
                                              + "temporary storage area: " + e.getClass().getName()
                                              + ": " + e.getMessage(), getRole());
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @throws ModuleInitializationException DOCUMENT ME!
   */
  public void postInitModule() throws ModuleInitializationException {
    m_manager = (DOManager) getServer().getModule("fedora.server.storage.DOManager");

    if (m_manager == null) {
      throw new ModuleInitializationException("Can't get a DOManager " + "from Server.getModule",
                                              getRole());
    }

    m_contentManager =
      (ExternalContentManager) getServer().getModule("fedora.server.storage.ExternalContentManager");

    if (m_contentManager == null) {
      throw new ModuleInitializationException("Can't get an ExternalContentManager "
                                              + "from Server.getModule", getRole());
    }

    //m_fedoraServerHost=getServer().getParameter("fedoraServerHost"); 2005.3.31 not used, so why have?
    //m_fedoraServerPort=getServer().getParameter("fedoraServerPort"); 2005.3.31 not used, so why have?
    m_fedoraXACMLModule =
      (Authorization) getServer().getModule("fedora.server.security.Authorization");

    if (m_fedoraXACMLModule == null) {
      throw new ModuleInitializationException("Can't get Authorization module (in default management) from Server.getModule",
                                              getRole());
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param context DOCUMENT ME!
   * @param serialization DOCUMENT ME!
   * @param logMessage DOCUMENT ME!
   * @param format DOCUMENT ME!
   * @param encoding DOCUMENT ME!
   * @param newPid DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   *
   * @throws ServerException DOCUMENT ME!
   */
  public String ingestObject(Context context, InputStream serialization, String logMessage,
                             String format, String encoding, boolean newPid)
                      throws ServerException {
    DOWriter w = null;

    try {
      getServer().logFinest("Entered DefaultManagement.ingestObject");
      w = m_manager.getIngestWriter(Server.USE_DEFINITIVE_STORE, context, serialization, format,
                                    encoding, newPid);

      String pid = w.GetObjectPID();

      m_fedoraXACMLModule.enforceIngestObject(context, pid, format, encoding);

      w.commit(logMessage);

      return pid;
    } finally {
      if (w != null) {
        m_manager.releaseWriter(w);
      }

      Runtime r = Runtime.getRuntime();
      getServer().logFinest("Memory: " + r.freeMemory() + " bytes free of " + r.totalMemory()
                            + " available.");
      getServer().logFinest("Exiting DefaultManagement.ingestObject");
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param context DOCUMENT ME!
   * @param pid DOCUMENT ME!
   * @param state DOCUMENT ME!
   * @param label DOCUMENT ME!
   * @param logMessage DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   *
   * @throws ServerException DOCUMENT ME!
   * @throws InvalidStateException DOCUMENT ME!
   */
  public Date modifyObject(Context context, String pid, String state, String label,
                           String logMessage) throws ServerException {
    DOWriter w = null;

    try {
      logFinest("Entered DefaultManagement.modifyObject");

      m_fedoraXACMLModule.enforceModifyObject(context, pid, state);

      checkObjectLabel(label);

      w = m_manager.getWriter(Server.USE_DEFINITIVE_STORE, context, pid);

      if ((state != null) && !state.equals("")) {
        if (!state.equals("A") && !state.equals("D") && !state.equals("I")) {
          throw new InvalidStateException("The object state of \"" + state
                                          + "\" is invalid. The allowed values for state are: "
                                          + " A (active), D (deleted), and I (inactive).");
        }

        w.setState(state);
      }

      //if (label!=null && !label.equals(""))
      if (label != null) {
        w.setLabel(label);
      }

      w.commit(logMessage);

      return w.getLastModDate();
    } finally {
      if (w != null) {
        m_manager.releaseWriter(w);
      }

      Runtime r = Runtime.getRuntime();
      getServer().logFinest("Memory: " + r.freeMemory() + " bytes free of " + r.totalMemory()
                            + " available.");
      getServer().logFinest("Exiting DefaultManagement.modifyObject");
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param context DOCUMENT ME!
   * @param pid DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   *
   * @throws ServerException DOCUMENT ME!
   */
  public Property[] getObjectProperties(Context context, String pid)
                                 throws ServerException {
    try {
      logFinest("Entered DefaultManagement.getObjectProperties");

      m_fedoraXACMLModule.enforceGetObjectProperties(context, pid);

      ArrayList props  = new ArrayList();
      DOReader  reader = m_manager.getReader(Server.USE_CACHE, context, pid);

      props.add(new Property("info:fedora/fedora-system:def/fType", reader.getFedoraObjectType()));

      props.add(new Property("info:fedora/fedora-system:def/cModel", reader.getContentModelId()));

      props.add(new Property("info:fedora/fedora-system:def/label", reader.GetObjectLabel()));

      props.add(new Property("info:fedora/fedora-system:def/state", reader.GetObjectState()));

      props.add(new Property("info:fedora/fedora-system:def/owner", reader.getOwnerId()));

      props.add(new Property("info:fedora/fedora-system:def/cDate",
                             DateUtility.convertDateToString(reader.getCreateDate())));

      props.add(new Property("info:fedora/fedora-system:def/mDate",
                             DateUtility.convertDateToString(reader.getLastModDate())));

      //Property[] extProps=reader.getExtProperties();
      return (Property[]) props.toArray(new Property[0]);
    } finally {
      logFinest("Exiting DefaultManagement.getObjectProperties");
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param context DOCUMENT ME!
   * @param pid DOCUMENT ME!
   * @param encoding DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   *
   * @throws ServerException DOCUMENT ME!
   */
  public InputStream getObjectXML(Context context, String pid, String encoding)
                           throws ServerException {
    try {
      logFinest("Entered DefaultManagement.getObjectXML");

      m_fedoraXACMLModule.enforceGetObjectXML(context, pid, encoding);

      DOReader    reader   = m_manager.getReader(Server.USE_CACHE, context, pid);
      InputStream instream = reader.GetObjectXML();

      return instream;
    } finally {
      logFinest("Exiting DefaultManagement.getObjectXML");
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param context DOCUMENT ME!
   * @param pid DOCUMENT ME!
   * @param format DOCUMENT ME!
   * @param exportContext DOCUMENT ME!
   * @param encoding DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   *
   * @throws ServerException DOCUMENT ME!
   */
  public InputStream exportObject(Context context, String pid, String format, String exportContext,
                                  String encoding) throws ServerException {
    try {
      logFinest("Entered DefaultManagement.exportObject");

      m_fedoraXACMLModule.enforceExportObject(context, pid, format, exportContext, encoding);

      DOReader    reader   = m_manager.getReader(Server.USE_DEFINITIVE_STORE, context, pid);
      InputStream instream = reader.ExportObject(format, exportContext);

      return instream;
    } finally {
      logFinest("Exiting DefaultManagement.exportObject");
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param context DOCUMENT ME!
   * @param pid DOCUMENT ME!
   * @param logMessage DOCUMENT ME!
   * @param force DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   *
   * @throws ServerException DOCUMENT ME!
   * @throws GeneralException DOCUMENT ME!
   */
  public Date purgeObject(Context context, String pid, String logMessage, boolean force)
                   throws ServerException {
    if (force) {
      throw new GeneralException("Forced object removal is not " + "yet supported.");
    }

    DOWriter w = null;

    try {
      logFinest("Entered DefaultManagement.purgeObject");

      m_fedoraXACMLModule.enforcePurgeObject(context, pid);

      w = m_manager.getWriter(Server.USE_DEFINITIVE_STORE, context, pid);
      w.remove();
      w.commit(logMessage);

      return new Date();
    } finally {
      if (w != null)
        m_manager.releaseWriter(w);

      logFinest("Exiting DefaultManagement.purgeObject");
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param context DOCUMENT ME!
   * @param pid DOCUMENT ME!
   * @param dsID DOCUMENT ME!
   * @param altIDs DOCUMENT ME!
   * @param dsLabel DOCUMENT ME!
   * @param versionable DOCUMENT ME!
   * @param MIMEType DOCUMENT ME!
   * @param formatURI DOCUMENT ME!
   * @param dsLocation DOCUMENT ME!
   * @param controlGroup DOCUMENT ME!
   * @param dsState DOCUMENT ME!
   * @param logMessage DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   *
   * @throws ServerException DOCUMENT ME!
   * @throws GeneralException DOCUMENT ME!
   * @throws InvalidStateException DOCUMENT ME!
   */
  public String addDatastream(Context context, String pid, String dsID, String[] altIDs,
                              String dsLabel, boolean versionable, String MIMEType,
                              String formatURI, String dsLocation, String controlGroup,
                              String dsState, String logMessage)
                       throws ServerException {
    // empty MIME types are allowed.  assume they meant "" if they provide it as null.
    if (MIMEType == null)
      MIMEType = "";

    if ((dsID != null) && (dsID.equals("AUDIT") || dsID.equals("FEDORA-AUDITTRAIL"))) {
      throw new GeneralException("Creation of a datastream with an"
                                 + " identifier of 'AUDIT' or 'FEDORA-AUDITTRAIL' is not permitted.");
    }

    DOWriter w = null;

    try {
      getServer().logFinest("Entered DefaultManagement.addDatastream");

      m_fedoraXACMLModule.enforceAddDatastream(context, pid, dsID, altIDs, MIMEType, formatURI,
                                               dsLocation, controlGroup, dsState);

      checkDatastreamID(dsID);
      checkDatastreamLabel(dsLabel);

      w = m_manager.getWriter(Server.USE_DEFINITIVE_STORE, context, pid);

      Datastream ds;

      if (controlGroup.equals("X")) {
        ds              = new DatastreamXMLMetadata();
        ds.DSInfoType   = ""; // field is now deprecated

        try {
          InputStream in;

          if (dsLocation.startsWith("uploaded://")) {
            in = getTempStream(dsLocation);
          } else {
            in = m_contentManager.getExternalContent(dsLocation, context).getStream();
          }

          ((DatastreamXMLMetadata) ds).xmlContent = getEmbeddableXML(in);

          // If it's a RELS-EXT datastream, do validation
          if ((dsID != null) && dsID.equals("RELS-EXT")) {
            validateRelsExt(pid, new ByteArrayInputStream(((DatastreamXMLMetadata) ds).xmlContent));
          }
        } catch (Exception e) {
          String extraInfo;

          if (e.getMessage() == null)
            extraInfo = "";
          else
            extraInfo = " : " + e.getMessage();

          throw new GeneralException("Error with " + dsLocation + extraInfo);
        }
      } else if (controlGroup.equals("M")) {
        ds              = new DatastreamManagedContent();
        ds.DSInfoType   = "DATA";
      } else if (controlGroup.equals("R") || controlGroup.equals("E")) {
        ds              = new DatastreamReferencedContent();
        ds.DSInfoType   = "DATA";
      } else {
        throw new GeneralException("Invalid control group: " + controlGroup);
      }

      ds.isNew           = true;
      ds.DSControlGrp    = controlGroup;
      ds.DSVersionable   = versionable;

      if (!dsState.equals("A") && !dsState.equals("D") && !dsState.equals("I")) {
        throw new InvalidStateException("The datastream state of \"" + dsState
                                        + "\" is invalid. The allowed values for state are: "
                                        + " A (active), D (deleted), and I (inactive).");
      }

      ds.DSState = dsState;

      // set new datastream id if not provided...
      if ((dsID == null) || (dsID.length() == 0)) {
        ds.DatastreamID = w.newDatastreamID();
      } else {
        if (dsID.indexOf(" ") != -1) {
          throw new GeneralException("Datastream ids cannot contain spaces.");
        }

        if (dsID.indexOf("+") != -1) {
          throw new GeneralException("Datastream ids cannot contain plusses.");
        }

        if (dsID.indexOf(":") != -1) {
          throw new GeneralException("Datastream ids cannot contain colons.");
        }

        if (w.GetDatastream(dsID, null) != null) {
          throw new GeneralException("A datastream already exists with ID: " + dsID);
        } else {
          ds.DatastreamID = dsID;
        }
      }

      // add version level attributes and
      // create new ds version id ...
      ds.DSVersionID         = ds.DatastreamID + ".0";
      ds.DSLabel             = dsLabel;
      ds.DSLocation          = dsLocation;

      if (dsLocation != null) {
        ValidationUtility.validateURL(dsLocation, false);
      }

      ds.DSFormatURI         = formatURI;
      ds.DatastreamAltIDs    = altIDs;
      ds.DSMIME              = MIMEType;

      Date nowUTC            = new Date();
      ds.DSCreateDT = nowUTC;

      AuditRecord audit = new fedora.server.storage.types.AuditRecord();
      audit.id               = w.newAuditRecordID();
      audit.processType      = "Fedora API-M";
      audit.action           = "addDatastream";
      audit.componentID      = ds.DatastreamID;
      audit.responsibility   = context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri);
      audit.date             = nowUTC;
      audit.justification    = logMessage;
      w.getAuditRecords().add(audit);
      w.addDatastream(ds);
      w.commit("Added a new datastream");

      return ds.DatastreamID;
    } finally {
      if (w != null) {
        m_manager.releaseWriter(w);
      }

      getServer().logFinest("Exiting DefaultManagement.addDatastream");
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param context DOCUMENT ME!
   * @param pid DOCUMENT ME!
   * @param bDefPid DOCUMENT ME!
   * @param bMechPid DOCUMENT ME!
   * @param dissLabel DOCUMENT ME!
   * @param bindingMap DOCUMENT ME!
   * @param dissState DOCUMENT ME!
   * @param logMessage DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   *
   * @throws ServerException DOCUMENT ME!
   * @throws InvalidStateException DOCUMENT ME!
   */
  public String addDisseminator(Context context, String pid, String bDefPid, String bMechPid,
                                String dissLabel, DSBindingMap bindingMap, String dissState,
                                String logMessage) throws ServerException {
    DOWriter w = null;

    try {
      getServer().logFinest("Entered DefaultManagement.addDisseminator");

      m_fedoraXACMLModule.enforceAddDisseminator(context, pid, bDefPid, bMechPid, dissState);

      checkDisseminatorLabel(dissLabel);

      w = m_manager.getWriter(Server.USE_DEFINITIVE_STORE, context, pid);

      Disseminator diss = new Disseminator();
      diss.isNew           = true;
      diss.parentPID       = pid;

      if (!dissState.equals("A") && !dissState.equals("D") && !dissState.equals("I")) {
        throw new InvalidStateException("The disseminator state of \"" + dissState
                                        + "\" is invalid. The allowed values for state are: "
                                        + " A (active), D (deleted), and I (inactive).");
      }

      diss.dissState       = dissState;
      diss.dissLabel       = dissLabel;
      diss.bMechID         = bMechPid;
      diss.bDefID          = bDefPid;

      Date nowUTC          = new Date();
      diss.dissCreateDT    = nowUTC;
      diss.dissID          = w.newDisseminatorID();
      diss.dissVersionID   = diss.dissID + ".0";

      // Generate the binding map ID here - ignore the value passed in
      // and set the field on both the disseminator and the binding map,
      // then set the disseminator's binding map to the one passed in.
      diss.dsBindMapID         = w.newDatastreamBindingMapID();
      bindingMap.dsBindMapID   = diss.dsBindMapID;
      diss.dsBindMap           = bindingMap;

      AuditRecord audit        = new fedora.server.storage.types.AuditRecord();
      audit.id               = w.newAuditRecordID();
      audit.processType      = "Fedora API-M";
      audit.action           = "addDisseminator";
      audit.componentID      = diss.dissID;
      audit.responsibility   = context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri);
      audit.date             = nowUTC;
      audit.justification    = logMessage;
      w.getAuditRecords().add(audit);
      w.addDisseminator(diss);
      w.commit("Added a new disseminator");

      return diss.dissID;
    } finally {
      if (w != null) {
        m_manager.releaseWriter(w);
      }

      getServer().logFinest("Exiting DefaultManagement.addDisseminator");
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param context DOCUMENT ME!
   * @param pid DOCUMENT ME!
   * @param datastreamId DOCUMENT ME!
   * @param altIDs DOCUMENT ME!
   * @param dsLabel DOCUMENT ME!
   * @param versionable DOCUMENT ME!
   * @param mimeType DOCUMENT ME!
   * @param formatURI DOCUMENT ME!
   * @param dsLocation DOCUMENT ME!
   * @param dsState DOCUMENT ME!
   * @param logMessage DOCUMENT ME!
   * @param force DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   *
   * @throws ServerException DOCUMENT ME!
   * @throws GeneralException DOCUMENT ME!
   * @throws InvalidStateException DOCUMENT ME!
   */
  public Date modifyDatastreamByReference(Context context, String pid, String datastreamId,
                                          String[] altIDs, String dsLabel, boolean versionable,
                                          String mimeType, String formatURI, String dsLocation,
                                          String dsState, String logMessage, boolean force)
                                   throws ServerException {
    if (datastreamId.equals("AUDIT") || datastreamId.equals("FEDORA-AUDITTRAIL")) {
      throw new GeneralException("Modification of the system-controlled AUDIT"
                                 + " datastream is not permitted.");
    }

    DOWriter w = null;

    try {
      getServer().logFinest("Entered DefaultManagement.modifyDatastreamByReference");
      m_fedoraXACMLModule.enforceModifyDatastreamByReference(context, pid, datastreamId, altIDs,
                                                             mimeType, formatURI, dsLocation,
                                                             dsState);
      checkDatastreamLabel(dsLabel);
      w = m_manager.getWriter(Server.USE_DEFINITIVE_STORE, context, pid);

      fedora.server.storage.types.Datastream orig   = w.GetDatastream(datastreamId, null);
      Date                                   nowUTC; // variable for ds modified date

      // some forbidden scenarios...
      if (orig.DSControlGrp.equals("X")) {
        throw new GeneralException("Inline XML datastreams must be modified by value, not by reference.");
      }

      if (orig.DSState.equals("D")) {
        throw new GeneralException("Changing attributes on deleted datastreams is forbidden.");
      }

      // A NULL INPUT PARM MEANS NO CHANGE TO DS ATTRIBUTE...
      // if input parms are null, the ds attribute should not be changed,
      // so set the parm values to the existing values in the datastream.
      if (dsLabel == null)
        dsLabel = orig.DSLabel;

      if (mimeType == null)
        mimeType = orig.DSMIME;

      if (formatURI == null)
        formatURI = orig.DSFormatURI;

      if (altIDs == null)
        altIDs = orig.DatastreamAltIDs;

      // In cases where an empty attribute value is not allowed, then
      // NULL or EMPTY PARM means no change to ds attribute...
      if ((dsLocation == null) || dsLocation.equals("")) {
        if (orig.DSControlGrp.equals("M")) {
          // if managed content location is unspecified,
          // cause a copy of the prior content to be made at commit-time
          dsLocation = "copy://" + orig.DSLocation;
        } else {
          dsLocation = orig.DSLocation;
        }
      } else {
        ValidationUtility.validateURL(dsLocation, false);
      }

      if ((dsState == null) || dsState.equals("")) {
        // If state unspecified leave state unchanged
        dsState = orig.DSState;
      }

      // just as a double check, make sure we have a valid ds state
      if (!dsState.equals("A") && !dsState.equals("D") && !dsState.equals("I")) {
        throw new InvalidStateException("The datastream state of \"" + dsState
                                        + "\" is invalid. The allowed values for state are: "
                                        + " A (active), D (deleted), and I (inactive).");
      }

      // if "force" is false and the mime type changed, validate the
      // original datastream with respect to any disseminators it is
      // involved in, and keep a record of that information for later
      // (so we can determine whether the mime type change would cause
      // data contract invalidation)
      Map oldValidationReports = null;

      if (!mimeType.equals(orig.DSMIME) && !force) {
        oldValidationReports = getAllBindingMapValidationReports(context, w, datastreamId);
      }

      // instantiate the right class of datastream
      // (inline xml "X" datastreams have already been rejected)
      Datastream newds;

      if (orig.DSControlGrp.equals("M")) {
        newds = new DatastreamManagedContent();
      } else {
        newds = new DatastreamReferencedContent();
      }

      // update ds attributes that are common to all versions...
      // first, those that cannot be changed by client...
      newds.DatastreamID   = orig.DatastreamID;
      newds.DSControlGrp   = orig.DSControlGrp;
      newds.DSInfoType     = orig.DSInfoType;

      // next, those that can be changed by client...
      newds.DSVersionable   = versionable;
      newds.DSState         = dsState;

      // update ds version-level attributes, and
      // make sure ds gets a new version id
      newds.DSVersionID        = w.newDatastreamID(datastreamId);
      newds.DSLabel            = dsLabel;
      newds.DSMIME             = mimeType;
      newds.DSFormatURI        = formatURI;
      newds.DatastreamAltIDs   = altIDs;
      nowUTC                   = new Date();
      newds.DSCreateDT         = nowUTC;

      //newds.DSSize will be computed later
      if (dsLocation != null) {
        ValidationUtility.validateURL(dsLocation, false);
      }

      newds.DSLocation = dsLocation;

      // next, add the datastream via the object writer
      w.addDatastream(newds);

      // if state was changed, set new state across all versions
      if (!orig.DSState.equals(newds.DSState)) {
        w.setDatastreamState(datastreamId, newds.DSState);
      }

      // if versionable was changed, set new versionable across all versions
      if (orig.DSVersionable != newds.DSVersionable) {
        w.setDatastreamVersionable(datastreamId, newds.DSVersionable);
      }

      // add the audit record
      fedora.server.storage.types.AuditRecord audit = new fedora.server.storage.types.AuditRecord();
      audit.id               = w.newAuditRecordID();
      audit.processType      = "Fedora API-M";
      audit.action           = "modifyDatastreamByReference";
      audit.componentID      = newds.DatastreamID;
      audit.responsibility   = context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri);
      audit.date             = nowUTC;
      audit.justification    = logMessage;
      w.getAuditRecords().add(audit);

      // if all went ok, check if we need to validate, then commit.
      if (oldValidationReports != null) { // mime changed and force=false
        rejectMimeChangeIfCausedInvalidation(oldValidationReports,
                                             getAllBindingMapValidationReports(context, w,
                                                                               datastreamId));
      }

      w.commit(logMessage);

      return nowUTC;
    } finally {
      if (w != null) {
        m_manager.releaseWriter(w);
      }

      getServer().logFinest("Exiting DefaultManagement.modifyDatastreamByReference");
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param context DOCUMENT ME!
   * @param pid DOCUMENT ME!
   * @param datastreamId DOCUMENT ME!
   * @param altIDs DOCUMENT ME!
   * @param dsLabel DOCUMENT ME!
   * @param versionable DOCUMENT ME!
   * @param mimeType DOCUMENT ME!
   * @param formatURI DOCUMENT ME!
   * @param dsContent DOCUMENT ME!
   * @param dsState DOCUMENT ME!
   * @param logMessage DOCUMENT ME!
   * @param force DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   *
   * @throws ServerException DOCUMENT ME!
   * @throws GeneralException DOCUMENT ME!
   * @throws InvalidStateException DOCUMENT ME!
   */
  public Date modifyDatastreamByValue(Context context, String pid, String datastreamId,
                                      String[] altIDs, String dsLabel, boolean versionable,
                                      String mimeType, String formatURI, InputStream dsContent,
                                      String dsState, String logMessage, boolean force)
                               throws ServerException {
    if (datastreamId.equals("AUDIT") || datastreamId.equals("FEDORA-AUDITTRAIL")) {
      throw new GeneralException("Modification of the system-controlled AUDIT"
                                 + " datastream is not permitted.");
    }

    DOWriter w           = null;
    boolean  mimeChanged = false;

    try {
      getServer().logFinest("Entered DefaultManagement.modifyDatastreamByValue");

      m_fedoraXACMLModule.enforceModifyDatastreamByValue(context, pid, datastreamId, altIDs,
                                                         mimeType, formatURI, dsState);

      checkDatastreamLabel(dsLabel);
      w = m_manager.getWriter(Server.USE_DEFINITIVE_STORE, context, pid);

      fedora.server.storage.types.Datastream orig = w.GetDatastream(datastreamId, null);

      // some forbidden scenarios...
      if (orig.DSState.equals("D")) {
        throw new GeneralException("Changing attributes on deleted datastreams is forbidden.");
      }

      if (!orig.DSControlGrp.equals("X")) {
        throw new GeneralException("Only content of inline XML datastreams may"
                                   + " be modified by value.\n"
                                   + "Use modifyDatastreamByReference instead.");
      }

      if (orig.DatastreamID.equals("METHODMAP") || orig.DatastreamID.equals("DSINPUTSPEC")
           || orig.DatastreamID.equals("WSDL")) {
        throw new GeneralException("METHODMAP, DSINPUTSPEC, and WSDL datastreams cannot be modified.");
      }

      // A NULL INPUT PARM MEANS NO CHANGE TO DS ATTRIBUTE...
      // if input parms are null, the ds attribute should not be changed,
      // so set the parm values to the existing values in the datastream.
      if (dsLabel == null)
        dsLabel = orig.DSLabel;

      if (mimeType == null)
        mimeType = orig.DSMIME;

      if (formatURI == null)
        formatURI = orig.DSFormatURI;

      if (altIDs == null)
        altIDs = orig.DatastreamAltIDs;

      // In cases where an empty attribute value is not allowed, then
      // NULL or EMPTY PARM means no change to ds attribute...
      if ((dsState == null) || dsState.equals("")) {
        // If state unspecified leave state unchanged
        dsState = orig.DSState;
      }

      // just as a double check, make sure we have a valid ds state
      if (!dsState.equals("A") && !dsState.equals("D") && !dsState.equals("I")) {
        throw new InvalidStateException("The datastream state of \"" + dsState
                                        + "\" is invalid. The allowed values for state are: "
                                        + " A (active), D (deleted), and I (inactive).");
      }

      // If "force" is false and the mime type changed, validate the
      // original datastream with respect to any disseminators it is
      // involved in, and keep a record of that information for later
      // (so we can determine whether the mime type change would cause
      // data contract invalidation)
      Map oldValidationReports = null;

      if (!mimeType.equals(orig.DSMIME) && !force) {
        oldValidationReports = getAllBindingMapValidationReports(context, w, datastreamId);
      }

      DatastreamXMLMetadata newds = new DatastreamXMLMetadata();
      newds.DSMDClass = ((DatastreamXMLMetadata) orig).DSMDClass;

      if (dsContent == null) {
        // If the dsContent input stream parm is null,
        // that means "do not change the content".
        // Accordingly, here we just make a copy of the old content.
        newds.xmlContent = ((DatastreamXMLMetadata) orig).xmlContent;
      } else {
        // If it's not null, use it
        newds.xmlContent = getEmbeddableXML(dsContent);

        // If it's a RELS-EXT datastream, do validation
        if (orig.DatastreamID.equals("RELS-EXT")) {
          validateRelsExt(pid, new ByteArrayInputStream(((DatastreamXMLMetadata) newds).xmlContent));
        }
      }

      // update ds attributes that are common to all versions...
      // first, those that cannot be changed by client...
      newds.DatastreamID   = orig.DatastreamID;
      newds.DSControlGrp   = orig.DSControlGrp;
      newds.DSInfoType     = orig.DSInfoType;

      // next, those that can be changed by client...
      newds.DSVersionable   = versionable;
      newds.DSState         = dsState;

      // update ds version level attributes, and
      // make sure ds gets a new version id
      newds.DSVersionID        = w.newDatastreamID(datastreamId);
      newds.DSLabel            = dsLabel;
      newds.DatastreamAltIDs   = altIDs;
      newds.DSMIME             = mimeType;
      newds.DSFormatURI        = formatURI;

      Date nowUTC              = new Date();
      newds.DSCreateDT = nowUTC;

      // next, add the datastream via the object writer
      w.addDatastream(newds);

      // if state was changed, set new state
      if (!orig.DSState.equals(newds.DSState)) {
        w.setDatastreamState(datastreamId, newds.DSState);
      }

      // if versionable was changed, set new versionable across all versions
      if (orig.DSVersionable != newds.DSVersionable) {
        w.setDatastreamVersionable(datastreamId, newds.DSVersionable);
      }

      // add the audit record
      fedora.server.storage.types.AuditRecord audit = new fedora.server.storage.types.AuditRecord();
      audit.id               = w.newAuditRecordID();
      audit.processType      = "Fedora API-M";
      audit.action           = "modifyDatastreamByValue";
      audit.componentID      = newds.DatastreamID;
      audit.responsibility   = context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri);
      audit.date             = nowUTC;
      audit.justification    = logMessage;
      w.getAuditRecords().add(audit);

      // if all went ok, check if we need to validate, then commit.
      if (oldValidationReports != null) { // mime changed and force=false
        rejectMimeChangeIfCausedInvalidation(oldValidationReports,
                                             getAllBindingMapValidationReports(context, w,
                                                                               datastreamId));
      }

      w.commit(logMessage);

      return nowUTC;
    } finally {
      if (w != null) {
        m_manager.releaseWriter(w);
      }

      getServer().logFinest("Exiting DefaultManagement.modifyDatastreamByValue");
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param context DOCUMENT ME!
   * @param pid DOCUMENT ME!
   * @param disseminatorId DOCUMENT ME!
   * @param bMechPid DOCUMENT ME!
   * @param dissLabel DOCUMENT ME!
   * @param dsBindingMap DOCUMENT ME!
   * @param dissState DOCUMENT ME!
   * @param logMessage DOCUMENT ME!
   * @param force DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   *
   * @throws ServerException DOCUMENT ME!
   * @throws InvalidStateException DOCUMENT ME!
   * @throws GeneralException DOCUMENT ME!
   */
  public Date modifyDisseminator(Context context, String pid, String disseminatorId,
                                 String bMechPid, String dissLabel, DSBindingMap dsBindingMap,
                                 String dissState, String logMessage, boolean force)
                          throws ServerException {
    DOWriter w = null;
    DOReader r = null;

    try {
      getServer().logFinest("Entered DefaultManagement.modifyDisseminator");
      m_fedoraXACMLModule.enforceModifyDisseminator(context, pid, disseminatorId, bMechPid,
                                                    dissState);

      checkDisseminatorLabel(dissLabel);
      w = m_manager.getWriter(Server.USE_DEFINITIVE_STORE, context, pid);

      fedora.server.storage.types.Disseminator orig = w.GetDisseminator(disseminatorId, null);

      String                                   oldValidationReport = null;

      if (!force) {
        oldValidationReport = getBindingMapValidationReport(context, w, orig.bMechID);
      }

      r = m_manager.getReader(Server.USE_DEFINITIVE_STORE, context, pid); // FIXME: Unnecessary?  Is

      // there a reason "w" isn't
      // used for the call below?
      Date[]       d = r.getDisseminatorVersions(disseminatorId);

      Disseminator newdiss = new Disseminator();

      // use original diss values for attributes that can't be changed by client
      newdiss.dissID      = orig.dissID;
      newdiss.bDefID      = orig.bDefID;
      newdiss.parentPID   = orig.parentPID;

      // make sure disseminator has a new version id
      newdiss.dissVersionID = w.newDisseminatorID(disseminatorId);

      // make sure disseminator has a new version date
      Date nowUTC = new Date();
      newdiss.dissCreateDT = nowUTC;

      // for testing; null indicates a new (uninitialized) instance
      // of dsBindingMap was passed in which is what you get if
      // you pass null in for dsBindingMap using MangementConsole
      if (dsBindingMap.dsBindMapID != null) {
        newdiss.dsBindMap = dsBindingMap;
      } else {
        newdiss.dsBindMap = orig.dsBindMap;
      }

      // make sure dsBindMapID has a different id
      newdiss.dsBindMapID             = w.newDatastreamBindingMapID();
      newdiss.dsBindMap.dsBindMapID   = w.newDatastreamBindingMapID();

      // NULL INPUT PARMS MEANS NO CHANGE in these cases:
      // set any diss attributes whose input parms value
      // is NULL to the original attribute value on the disseminator
      if (dissLabel == null) {
        //if (dissLabel==null || dissLabel.equals("")) {
        newdiss.dissLabel = orig.dissLabel;
      } else {
        newdiss.dissLabel = dissLabel;
      }

      // NULL OR "" INPUT PARM MEANS NO CHANGE:
      // for diss attributes whose values MUST NOT be empty,
      // either NULL or "" on the input parm indicates no change
      // (keep original value)
      if ((bMechPid == null) || bMechPid.equals("")) {
        newdiss.bMechID = orig.bMechID;
      } else {
        newdiss.bMechID = bMechPid;
      }

      if ((dissState == null) || dissState.equals("")) {
        // If reference unspecified leave state unchanged
        newdiss.dissState = orig.dissState;
      } else {
        // Check that supplied value for state is one of the allowable values
        if (!dissState.equals("A") && !dissState.equals("D") && !dissState.equals("I")) {
          throw new InvalidStateException("The disseminator state of \"" + dissState
                                          + "\" is invalid. The allowed values for state are: "
                                          + " A (active), D (deleted), and I (inactive).");
        }

        newdiss.dissState = dissState;
      }

      // just add the disseminator
      w.addDisseminator(newdiss);

      if (!orig.dissState.equals(newdiss.dissState)) {
        w.setDisseminatorState(disseminatorId, newdiss.dissState);
      }

      // add the audit record
      fedora.server.storage.types.AuditRecord audit = new fedora.server.storage.types.AuditRecord();
      audit.id               = w.newAuditRecordID();
      audit.processType      = "Fedora API-M";
      audit.action           = "modifyDisseminator";
      audit.componentID      = newdiss.dissID;
      audit.responsibility   = context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri);
      audit.date             = nowUTC;
      audit.justification    = logMessage;
      w.getAuditRecords().add(audit);

      // if all went ok, check if we need to validate, then commit.
      if (!force && (oldValidationReport == null)) {
        String cause = getBindingMapValidationReport(context, w, newdiss.bMechID);

        if (cause != null) {
          throw new GeneralException("That change would invalidate " + "the disseminator: " + cause);
        }
      }

      w.commit(logMessage);

      return nowUTC;
    } finally {
      if (w != null) {
        m_manager.releaseWriter(w);
      }

      getServer().logFinest("Exiting DefaultManagement.modifyDisseminator");
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param context DOCUMENT ME!
   * @param pid DOCUMENT ME!
   * @param datastreamID DOCUMENT ME!
   * @param endDT DOCUMENT ME!
   * @param logMessage DOCUMENT ME!
   * @param force DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   *
   * @throws ServerException DOCUMENT ME!
   * @throws GeneralException DOCUMENT ME!
   */
  public Date[] purgeDatastream(Context context, String pid, String datastreamID, Date endDT,
                                String logMessage, boolean force)
                         throws ServerException {
    if (force) {
      throw new GeneralException("Forced datastream removal is not " + "yet supported.");
    }

    DOWriter w = null;

    try {
      getServer().logFinest("Entered DefaultManagement.purgeDatastream");

      m_fedoraXACMLModule.enforcePurgeDatastream(context, pid, datastreamID, endDT);

      w = m_manager.getWriter(Server.USE_DEFINITIVE_STORE, context, pid);

      Date   start        = null;
      Date[] deletedDates = w.removeDatastream(datastreamID, start, endDT);

      // check if there's at least one version with this id...
      if (w.GetDatastream(datastreamID, null) == null) {
        // if deleting would result in no versions remaining,
        // only continue if there are no disseminators that use
        // this datastream.
        // to do this, we must look through all versions of every
        // disseminator, regardless of state
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        ArrayList        usedList = new ArrayList();

        if (datastreamID.equals("DC")) {
          usedList.add("The default disseminator");
        }

        // ...for each disseminator
        Disseminator[] disses = w.GetDisseminators(null, null);

        for (int i = 0; i < disses.length; i++) {
          Date[] dates = w.getDisseminatorVersions(disses[i].dissID);

          // ...for each of its versions
          for (int j = 0; j < dates.length; j++) {
            Disseminator diss       = w.GetDisseminator(disses[i].dissID, dates[j]);
            DSBinding[]  dsBindings = diss.dsBindMap.dsBindings;

            // ...for each of its datastream bindings
            for (int k = 0; k < dsBindings.length; k++) {
              // ...is the datastream id referenced?
              if (dsBindings[k].datastreamID.equals(datastreamID)) {
                usedList.add(diss.dissID + " (" + formatter.format(diss.dissCreateDT) + ")");
              }
            }
          }
        }

        if (usedList.size() > 0) {
          StringBuilder msg = new StringBuilder();
          msg.append("Cannot purge entire datastream because it\n");
          msg.append("is used by the following disseminators:");

          for (int i = 0; i < usedList.size(); i++) {
            msg.append("\n - " + (String) usedList.get(i));
          }

          throw new GeneralException(msg.toString());
        }
      }

      // add an explanation of what happened to the user-supplied message.
      if (logMessage == null) {
        logMessage = "";
      } else {
        logMessage += " . . . ";
      }

      logMessage += getPurgeLogMessage("datastream", datastreamID, start, endDT, deletedDates);

      Date                                    nowUTC = new Date();
      fedora.server.storage.types.AuditRecord audit = new fedora.server.storage.types.AuditRecord();
      audit.id               = w.newAuditRecordID();
      audit.processType      = "Fedora API-M";
      audit.action           = "purgeDatastream";
      audit.componentID      = datastreamID;
      audit.responsibility   = context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri);
      audit.date             = nowUTC;
      audit.justification    = logMessage;

      // Normally we associate an audit record with a specific version
      // of a datastream, but in this case we are talking about a range
      // of versions.  So we'll just add it to the object, but not associate
      // it with anything.
      w.getAuditRecords().add(audit);

      // It looks like all went ok, so commit
      w.commit(logMessage);

      // ... then give the response
      return deletedDates;
    } finally {
      if (w != null) {
        m_manager.releaseWriter(w);
      }

      getServer().logFinest("Exiting DefaultManagement.purgeDatastream");
    }
  }

  private String getPurgeLogMessage(String kindaThing, String id, Date start, Date end,
                                    Date[] deletedDates) {
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    StringBuilder     buf = new StringBuilder();
    buf.append("Purged ");
    buf.append(kindaThing);
    buf.append(" (ID=");
    buf.append(id);
    buf.append("), versions ranging from ");

    if (start == null) {
      buf.append("the beginning of time");
    } else {
      buf.append(formatter.format(start));
    }

    buf.append(" to ");

    if (end == null) {
      buf.append("the end of time");
    } else {
      buf.append(formatter.format(end));
    }

    buf.append(".  This resulted in the permanent removal of ");
    buf.append(deletedDates.length + " ");
    buf.append(kindaThing);
    buf.append(" version(s) (");

    for (int i = 0; i < deletedDates.length; i++) {
      if (i > 0) {
        buf.append(", ");
      }

      buf.append(formatter.format(deletedDates[i]));
    }

    buf.append(") and all associated audit records.");

    return buf.toString();
  }

  /**
   * DOCUMENT ME!
   *
   * @param context DOCUMENT ME!
   * @param pid DOCUMENT ME!
   * @param datastreamID DOCUMENT ME!
   * @param asOfDateTime DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   *
   * @throws ServerException DOCUMENT ME!
   */
  public Datastream getDatastream(Context context, String pid, String datastreamID,
                                  Date asOfDateTime) throws ServerException {
    try {
      getServer().logFinest("Entered DefaultManagement.getDatastream");

      m_fedoraXACMLModule.enforceGetDatastream(context, pid, datastreamID, asOfDateTime);

      DOReader r = m_manager.getReader(Server.GLOBAL_CHOICE, context, pid);

      return r.GetDatastream(datastreamID, asOfDateTime);
    } finally {
      getServer().logFinest("Exiting DefaultManagement.getDatastream");
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param context DOCUMENT ME!
   * @param pid DOCUMENT ME!
   * @param asOfDateTime DOCUMENT ME!
   * @param state DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   *
   * @throws ServerException DOCUMENT ME!
   */
  public Datastream[] getDatastreams(Context context, String pid, Date asOfDateTime, String state)
                              throws ServerException {
    try {
      getServer().logFinest("Entered DefaultManagement.getDatastreams");

      if (fedora.server.Debug.DEBUG)
        System.err.println("in getDatastreams, before call to enforce");

      m_fedoraXACMLModule.enforceGetDatastreams(context, pid, asOfDateTime, state);

      if (fedora.server.Debug.DEBUG)
        System.err.println("in getDatastreams, after call to enforce");

      DOReader r = m_manager.getReader(Server.GLOBAL_CHOICE, context, pid);

      return r.GetDatastreams(asOfDateTime, state);
    } finally {
      getServer().logFinest("Exiting DefaultManagement.getDatastream");
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param context DOCUMENT ME!
   * @param pid DOCUMENT ME!
   * @param datastreamID DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   *
   * @throws ServerException DOCUMENT ME!
   */
  public Datastream[] getDatastreamHistory(Context context, String pid, String datastreamID)
                                    throws ServerException {
    try {
      getServer().logFinest("Entered DefaultManagement.getDatastreamHistory");

      m_fedoraXACMLModule.enforceGetDatastreamHistory(context, pid, datastreamID);

      DOReader     r            = m_manager.getReader(Server.GLOBAL_CHOICE, context, pid);
      Date[]       versionDates = r.getDatastreamVersions(datastreamID);
      Datastream[] versions     = new Datastream[versionDates.length];

      for (int i = 0; i < versionDates.length; i++) {
        versions[i] = r.GetDatastream(datastreamID, versionDates[i]);
      }

      // sort, ascending
      Arrays.sort(versions, new DatastreamDateComparator());

      // reverse it (make it descend, so most recent date is element 0)
      Datastream[] out = new Datastream[versions.length];

      for (int i = 0; i < versions.length; i++) {
        out[i] = versions[versions.length - 1 - i];
      }

      return out;
    } finally {
      getServer().logFinest("Exiting DefaultManagement.getDatastreamHistory");
    }
  }

  public class DatastreamDateComparator implements Comparator {
    public int compare(Object o1, Object o2) {
      long ms1 = ((Datastream) o1).DSCreateDT.getTime();
      long ms2 = ((Datastream) o1).DSCreateDT.getTime();

      if (ms1 < ms2)
        return -1;

      if (ms1 > ms2)
        return 1;

      return 0;
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param context DOCUMENT ME!
   * @param pid DOCUMENT ME!
   * @param disseminatorID DOCUMENT ME!
   * @param endDT DOCUMENT ME!
   * @param logMessage DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   *
   * @throws ServerException DOCUMENT ME!
   */
  public Date[] purgeDisseminator(Context context, String pid, String disseminatorID, Date endDT,
                                  String logMessage) throws ServerException {
    DOWriter w = null;

    try {
      getServer().logFinest("Entered DefaultManagement.purgeDisseminator");

      m_fedoraXACMLModule.enforcePurgeDisseminator(context, pid, disseminatorID, endDT);

      w = m_manager.getWriter(Server.GLOBAL_CHOICE, context, pid);

      Date   start        = null;
      Date[] deletedDates = w.removeDisseminator(disseminatorID, start, endDT);

      // add an explanation of what happened to the user-supplied message.
      if (logMessage == null) {
        logMessage = "";
      } else {
        logMessage += " . . . ";
      }

      logMessage += getPurgeLogMessage("disseminator", disseminatorID, start, endDT, deletedDates);

      Date                                    nowUTC = new Date();
      fedora.server.storage.types.AuditRecord audit = new fedora.server.storage.types.AuditRecord();
      audit.id               = w.newAuditRecordID();
      audit.processType      = "Fedora API-M";
      audit.action           = "purgeDisseminator";
      audit.componentID      = disseminatorID;
      audit.responsibility   = context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri);
      audit.date             = nowUTC;
      audit.justification    = logMessage;

      // Normally we associate an audit record with a specific version
      // of a disseminator, but in this case we are talking about a range
      // of versions.  So we'll just add it to the object, but not associate
      // it with anything.
      w.getAuditRecords().add(audit);

      // It looks like all went ok, so commit
      // ... then give the response
      w.commit(logMessage);

      return deletedDates;
    } finally {
      if (w != null) {
        m_manager.releaseWriter(w);
      }

      getServer().logFinest("Exiting DefaultManagement.purgeDisseminator");
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param context DOCUMENT ME!
   * @param pid DOCUMENT ME!
   * @param disseminatorId DOCUMENT ME!
   * @param asOfDateTime DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   *
   * @throws ServerException DOCUMENT ME!
   */
  public Disseminator getDisseminator(Context context, String pid, String disseminatorId,
                                      Date asOfDateTime)
                               throws ServerException {
    try {
      getServer().logFinest("Entered DefaultManagement.getDisseminator");

      m_fedoraXACMLModule.enforceGetDisseminator(context, pid, disseminatorId, asOfDateTime);

      DOReader r = m_manager.getReader(Server.GLOBAL_CHOICE, context, pid);

      return r.GetDisseminator(disseminatorId, asOfDateTime);
    } finally {
      getServer().logFinest("Exiting DefaultManagement.getDisseminator");
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param context DOCUMENT ME!
   * @param pid DOCUMENT ME!
   * @param asOfDateTime DOCUMENT ME!
   * @param dissState DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   *
   * @throws ServerException DOCUMENT ME!
   */
  public Disseminator[] getDisseminators(Context context, String pid, Date asOfDateTime,
                                         String dissState)
                                  throws ServerException {
    try {
      getServer().logFinest("Entered DefaultManagement.getDisseminators");

      if (fedora.server.Debug.DEBUG)
        System.err.println("in getDisseminators, before call to enforce");

      m_fedoraXACMLModule.enforceGetDisseminators(context, pid, asOfDateTime, dissState);

      if (fedora.server.Debug.DEBUG)
        System.err.println("in getDisseminators, after call to enforce");

      DOReader r = m_manager.getReader(Server.GLOBAL_CHOICE, context, pid);

      return r.GetDisseminators(asOfDateTime, dissState);
    } finally {
      getServer().logFinest("Exiting DefaultManagement.getDisseminators");
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param context DOCUMENT ME!
   * @param pid DOCUMENT ME!
   * @param disseminatorID DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   *
   * @throws ServerException DOCUMENT ME!
   */
  public Disseminator[] getDisseminatorHistory(Context context, String pid, String disseminatorID)
                                        throws ServerException {
    try {
      m_fedoraXACMLModule.enforceGetDisseminatorHistory(context, pid, disseminatorID);

      DOReader       r            = m_manager.getReader(Server.USE_DEFINITIVE_STORE, context, pid);
      Date[]         versionDates = r.getDisseminatorVersions(disseminatorID);
      Disseminator[] versions     = new Disseminator[versionDates.length];

      for (int i = 0; i < versionDates.length; i++) {
        versions[i] = r.GetDisseminator(disseminatorID, versionDates[i]);
      }

      // sort, ascending
      Arrays.sort(versions, new DisseminatorDateComparator());

      // reverse it (make it descend, so most recent date is element 0)
      Disseminator[] out = new Disseminator[versions.length];

      for (int i = 0; i < versions.length; i++) {
        out[i] = versions[versions.length - 1 - i];
      }

      return out;
    } finally {
      getServer().logFinest("Exiting DefaultManagement.getDisseminatorHistory");
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param context DOCUMENT ME!
   * @param numPIDs DOCUMENT ME!
   * @param namespace DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   *
   * @throws ServerException DOCUMENT ME!
   */
  public String[] getNextPID(Context context, int numPIDs, String namespace)
                      throws ServerException {
    try {
      getServer().logFinest("Entered DefaultManagement.getNextPID");
      m_fedoraXACMLModule.enforceGetNextPid(context, namespace, numPIDs);

      return m_manager.getNextPID(numPIDs, namespace);
    } finally {
      getServer().logFinest("Exiting DefaultManagement.getNextPID");
    }
  }

  public class DisseminatorDateComparator implements Comparator {
    public int compare(Object o1, Object o2) {
      long ms1 = ((Disseminator) o1).dissCreateDT.getTime();
      long ms2 = ((Disseminator) o2).dissCreateDT.getTime();

      if (ms1 < ms2)
        return -1;

      if (ms1 > ms2)
        return 1;

      return 0;
    }
  }

  private void cleanUpTmpFiles(int max) {
    long      minStartTime = System.currentTimeMillis() - (60 * 1000 * m_uploadStorageMinutes);
    ArrayList removeList = new ArrayList();

    synchronized (m_uploadStartTime) { // patched 11/23/06
      Iterator iter = m_uploadStartTime.keySet().iterator();

      while (iter.hasNext()) {
        String id        = (String) iter.next();
        Long   startTime = (Long) m_uploadStartTime.get(id);

        if (startTime.longValue() < minStartTime) {
          // remove from filesystem and hash
          File f = new File(m_tempDir, id);

          if (f.delete()) {
            logInfo("Removed uploaded file '" + id + "' because it expired.");
          } else {
            logWarning("Could not remove expired uploaded file '" + id
                       + "'.  Check existence/permissions in management/upload/ directory.");
          }

          removeList.add(id);
          if (removeList.size() >= max)
            break;
        }
      }

      for (int i = 0; i < removeList.size(); i++) {
        String id = (String) removeList.get(i);
        m_uploadStartTime.remove(id);
      }
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param context DOCUMENT ME!
   * @param in DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   *
   * @throws StreamWriteException DOCUMENT ME!
   * @throws AuthzException DOCUMENT ME!
   */
  public String putTempStream(Context context, InputStream in)
                       throws StreamWriteException, AuthzException {
    m_fedoraXACMLModule.enforceUpload(context);

    int id = getNextTempId();

    // and attempt to save the stream
    File             outFile = new File(m_tempDir, "" + id);
    FileOutputStream out = null;

    try {
      out = new FileOutputStream(outFile);
      StreamUtility.pipeStream(in, out, 32768);
    } catch (Exception e) {
      if (out != null) {
        try {
          out.close();
        } catch (Exception ex) {
        }

        outFile.delete();
      }

      throw new StreamWriteException(e.getMessage());
    }

    // if we got this far w/o an exception, add to hash with current time
    // and return the identifier-that-looks-like-a-url
    long now = System.currentTimeMillis();
    m_uploadStartTime.put("" + id, new Long(now));

    return "uploaded://" + id;
  }

  private synchronized int getNextTempId() {
    m_lastId++;

    return m_lastId;
  }

  /**
   * DOCUMENT ME!
   *
   * @param id DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   *
   * @throws StreamReadException DOCUMENT ME!
   */
  public InputStream getTempStream(String id) throws StreamReadException {
    // it should come in starting with "uploaded://"
    if (id.startsWith("uploaded://") || (id.length() < 12)) {
      String internalId = id.substring(11);

      if (m_uploadStartTime.get(internalId) != null) {
        // found... return inputstream
        try {
          return new FileInputStream(new File(m_tempDir, internalId));
        } catch (Exception e) {
          throw new StreamReadException(e.getMessage());
        }
      } else {
        throw new StreamReadException("Id specified, '" + id
                                      + "', does not match an existing file.");
      }
    } else {
      throw new StreamReadException("Invalid id syntax '" + id + "'.");
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param context DOCUMENT ME!
   * @param pid DOCUMENT ME!
   * @param datastreamID DOCUMENT ME!
   * @param dsState DOCUMENT ME!
   * @param logMessage DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   *
   * @throws ServerException DOCUMENT ME!
   * @throws InvalidStateException DOCUMENT ME!
   */
  public Date setDatastreamState(Context context, String pid, String datastreamID, String dsState,
                                 String logMessage) throws ServerException {
    DOWriter w = null;

    try {
      getServer().logFinest("Entered DefaultManagement.setDatastreamState");

      m_fedoraXACMLModule.enforceSetDatastreamState(context, pid, datastreamID, dsState);

      w = m_manager.getWriter(Server.USE_DEFINITIVE_STORE, context, pid);

      if (!dsState.equals("A") && !dsState.equals("D") && !dsState.equals("I")) {
        throw new InvalidStateException("The datastream state of \"" + dsState
                                        + "\" is invalid. The allowed values for state are: "
                                        + " A (active), D (deleted), and I (inactive).");
      }

      fedora.server.storage.types.Datastream ds = w.GetDatastream(datastreamID, null);
      w.setDatastreamState(datastreamID, dsState);

      // add the audit record
      fedora.server.storage.types.AuditRecord audit = new fedora.server.storage.types.AuditRecord();
      audit.id               = w.newAuditRecordID();
      audit.processType      = "Fedora API-M";
      audit.action           = "setDatastreamState";
      audit.componentID      = datastreamID;
      audit.responsibility   = context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri);

      Date nowUTC            = new Date();
      audit.date            = nowUTC;
      audit.justification   = logMessage;
      w.getAuditRecords().add(audit);

      // if all went ok, commit
      w.commit(logMessage);

      return nowUTC;
    } finally {
      if (w != null) {
        m_manager.releaseWriter(w);
      }

      getServer().logFinest("Exiting DefaultManagement.setDatastreamState");
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param context DOCUMENT ME!
   * @param pid DOCUMENT ME!
   * @param disseminatorID DOCUMENT ME!
   * @param dissState DOCUMENT ME!
   * @param logMessage DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   *
   * @throws ServerException DOCUMENT ME!
   * @throws InvalidStateException DOCUMENT ME!
   */
  public Date setDisseminatorState(Context context, String pid, String disseminatorID,
                                   String dissState, String logMessage)
                            throws ServerException {
    DOWriter w = null;

    try {
      getServer().logFinest("Entered DefaultManagement.setDisseminatorState");
      m_fedoraXACMLModule.enforceSetDisseminatorState(context, pid, disseminatorID, dissState);

      w = m_manager.getWriter(Server.USE_DEFINITIVE_STORE, context, pid);

      if (!dissState.equals("A") && !dissState.equals("D") && !dissState.equals("I")) {
        throw new InvalidStateException("The disseminator state of \"" + dissState
                                        + "\" is invalid. The allowed values for state are: "
                                        + " A (active), D (deleted), and I (inactive).");
      }

      fedora.server.storage.types.Disseminator diss = w.GetDisseminator(disseminatorID, null);
      w.setDisseminatorState(disseminatorID, dissState);

      // add the audit record
      fedora.server.storage.types.AuditRecord audit = new fedora.server.storage.types.AuditRecord();
      audit.id               = w.newAuditRecordID();
      audit.processType      = "Fedora API-M";
      audit.action           = "setDisseminatorState";
      audit.componentID      = disseminatorID;
      audit.responsibility   = context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri);

      Date nowUTC            = new Date();
      audit.date            = nowUTC;
      audit.justification   = logMessage;
      w.getAuditRecords().add(audit);

      // if all went ok, commit
      w.commit(logMessage);

      return nowUTC;
    } finally {
      if (w != null) {
        m_manager.releaseWriter(w);
      }

      getServer().logFinest("Exiting DefaultManagement.setDisseminatorState");
    }
  }

  /**
   * Get a byte array containing an xml chunk that is safe to embed in  another UTF-8 xml document.
   *
   * <p>
   * This will ensure that the xml is:
   *
   * <ul>
   * <li>
   * well-formed. If not, an exception will be raised.
   * </li>
   * <li>
   * encoded in UTF-8. It will be converted otherwise.
   * </li>
   * <li>
   * devoid of processing instructions. These will be stripped if present.
   * </li>
   * <li>
   * devoid of DOCTYPE declarations. These will be stripped if present.
   * </li>
   * <li>
   * devoid of internal entity references.  These will be expanded if present.
   * </li>
   * </ul>
   * </p>
   *
   * @param in DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   *
   * @throws GeneralException DOCUMENT ME!
   */
  private byte[] getEmbeddableXML(InputStream in) throws GeneralException {
    // parse with xerces and re-serialize the fixed xml to a byte array
    try {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      OutputFormat          fmt = new OutputFormat("XML", "UTF-8", true);
      fmt.setIndent(2);
      fmt.setLineWidth(120);
      fmt.setPreserveSpace(false);
      fmt.setOmitXMLDeclaration(true);
      fmt.setOmitDocumentType(true);

      XMLSerializer          ser     = new XMLSerializer(out, fmt);
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);

      DocumentBuilder builder = factory.newDocumentBuilder();
      Document        doc = builder.parse(in);
      ser.serialize(doc);

      return out.toByteArray();
    } catch (Exception e) {
      String message = e.getMessage();

      if (message == null)
        message = "";

      throw new GeneralException("XML was not well-formed. " + message, e);
    }
  }

  /**
   * Get a string indicating whether the associated binding map (or an empty binding map, if none
   * is found) is valid or invalid according to the  data contract defined by the indicated
   * behavior mechanism. Returns null if valid, otherwise returns a String explaining why not.
   * This assumes the indicated bMech actually exists, and the binding map, if it exists and
   * specifies any datastreams, refers to existing  datastreams within the object.  If these
   * conditions are not met, an  exception is thrown.
   *
   * @param context DOCUMENT ME!
   * @param doReader DOCUMENT ME!
   * @param bMechPID DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   *
   * @throws ServerException DOCUMENT ME!
   */
  private String getBindingMapValidationReport(Context context, DOReader doReader, String bMechPID)
                                        throws ServerException {
    // find the associated datastream binding map, else use an empty one.
    DSBindingMapAugmented   augMap  = new DSBindingMapAugmented();
    DSBindingMapAugmented[] augMaps = doReader.GetDSBindingMaps(null);

    for (int i = 0; i < augMaps.length; i++) {
      if (augMaps[i].dsBindMechanismPID.equals(bMechPID)) {
        augMap = augMaps[i];
      }
    }

    // load the bmech, then validate the bindings
    BMechReader     mReader = m_manager.getBMechReader(Server.USE_CACHE, context, bMechPID);
    BMechDSBindSpec spec = mReader.getServiceDSInputSpec(null);

    return spec.validate(augMap.dsBindingsAugmented);
  }

  /**
   * Get a combined report indicating failure or success of data contract  validation for every
   * disseminator in the given object that the indicated  datastream is bound to. The returned
   * map's keys will be Disseminator objects. The values will be null in the case of successful
   * validation, or Strings (explaining why) in the case of failure. This assumes that all bMechs
   * specified in the binding maps of the disseminators that use the indicated datastream actually
   * exist, and  the binding map, if it exists and specifies any datastreams, refers to  existing
   * datastreams within the object.  If these conditions are not  met, an exception is thrown.
   *
   * @param context DOCUMENT ME!
   * @param doReader DOCUMENT ME!
   * @param dsID DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   *
   * @throws ServerException DOCUMENT ME!
   */
  private Map getAllBindingMapValidationReports(Context context, DOReader doReader, String dsID)
                                         throws ServerException {
    HashMap map = new HashMap();

    // for all disseminators in the object,
    Disseminator[] disses = doReader.GetDisseminators(null, null);

    for (int i = 0; i < disses.length; i++) {
      DSBinding[] bindings = disses[i].dsBindMap.dsBindings;
      boolean     isUsed = false;

      // check each binding to see if it's the indicated datastream
      for (int j = 0; (j < bindings.length) && !isUsed; j++) {
        if (bindings[j].datastreamID.equals(dsID))
          isUsed = true;
      }

      if (isUsed) {
        // if it's used, add it's validation information to the map.
        map.put(disses[i], getBindingMapValidationReport(context, doReader, disses[i].bMechID));
      }
    }

    return map;
  }

  private Map getNewFailedValidationReports(Map oldReport, Map newReport) {
    HashMap  map     = new HashMap();
    Iterator newIter = newReport.keySet().iterator();

    // For each disseminator in the new report:
    while (newIter.hasNext()) {
      Disseminator diss          = (Disseminator) newIter.next();
      String       failedMessage = (String) newReport.get(diss);

      // Did it fail in the new report . . .
      if (failedMessage != null) {
        // . . . but not in the old one?
        if (oldReport.get(diss) == null) {
          map.put(diss, failedMessage);
        }
      }
    }

    return map;
  }

  private void rejectMimeChangeIfCausedInvalidation(Map oldReports, Map newReports)
                                             throws ServerException {
    Map causedFailures = getNewFailedValidationReports(oldReports, newReports);
    int numFailures = causedFailures.keySet().size();

    if (numFailures > 0) {
      StringBuilder buf = new StringBuilder();
      buf.append("This mime type change would invalidate " + numFailures + " disseminator(s):");

      Iterator iter = causedFailures.keySet().iterator();

      while (iter.hasNext()) {
        Disseminator diss   = (Disseminator) iter.next();
        String       reason = (String) causedFailures.get(diss);
        buf.append("\n" + diss.dissID + ": " + reason);
      }

      throw new GeneralException(buf.toString());
    }
  }

  private void validateRelsExt(String pid, InputStream relsext)
                        throws ServerException {
    // RELATIONSHIP METADATA VALIDATION:
    try {
      RelsExtValidator deser = new RelsExtValidator("UTF-8", false);

      if (relsext != null) {
        logFinest("API-M: Validating RELS-EXT datastream...");
        deser.deserialize(relsext, "info:fedora/" + pid);

        if (fedora.server.Debug.DEBUG)
          System.out.println("Done validating RELS-EXT.");

        logFinest("API-M: RELS-EXT datastream passed validation.");
      }
    } catch (Exception e) {
      String message = e.getMessage();

      if (message == null)
        message = e.getClass().getName();

      throw new GeneralException("RELS-EXT validation failed: " + message);
    }
  }

  private void checkDatastreamID(String id) throws ValidationException {
    checkString(id, "Datastream id", ValidationConstants.DATASTREAM_ID_MAXLEN,
                ValidationConstants.DATASTREAM_ID_BADCHARS);
  }

  private void checkDatastreamLabel(String label) throws ValidationException {
    checkString(label, "Datastream label", ValidationConstants.DATASTREAM_LABEL_MAXLEN, null);
  }

  private void checkDisseminatorID(String id) throws ValidationException {
    checkString(id, "Disseminator id", ValidationConstants.DISSEMINATOR_ID_MAXLEN,
                ValidationConstants.DISSEMINATOR_ID_BADCHARS);
  }

  private void checkDisseminatorLabel(String label) throws ValidationException {
    checkString(label, "Disseminator label", ValidationConstants.DISSEMINATOR_LABEL_MAXLEN, null);
  }

  private void checkObjectLabel(String label) throws ValidationException {
    checkString(label, "Object label", ValidationConstants.OBJECT_LABEL_MAXLEN, null);
  }

  private void checkString(String string, String kind, int maxLen, char[] badChars)
                    throws ValidationException {
    if (string != null) {
      if (string.length() > maxLen) {
        throw new ValidationException(kind + " is too long. Maximum " + "length is " + maxLen
                                      + " characters.");
      } else if (badChars != null) {
        for (int i = 0; i < badChars.length; i++) {
          char c = badChars[i];

          if (string.indexOf(c) != -1) {
            throw new ValidationException(kind + " contains a " + "'" + c
                                          + "', but that character is not " + "allowed.");
          }
        }
      }
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param context DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   *
   * @throws ServerException DOCUMENT ME!
   */
  public boolean adminPing(Context context) throws ServerException {
    m_fedoraXACMLModule.enforceAdminPing(context);

    return true;
  }
}
