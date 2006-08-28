/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.ws.annotation.impl;

import java.net.URI;
import java.net.URISyntaxException;

import java.rmi.RemoteException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.jrdf.graph.Literal;
import org.jrdf.graph.URIReference;

import org.topazproject.configuration.ConfigurationStore;

import org.topazproject.fedora.client.FedoraAPIM;
import org.topazproject.fedora.client.Uploader;

import org.topazproject.mulgara.itql.Answer;
import org.topazproject.mulgara.itql.AnswerException;
import org.topazproject.mulgara.itql.ItqlHelper;

import org.topazproject.ws.annotation.AnnotationInfo;
import org.topazproject.ws.annotation.Annotations;
import org.topazproject.ws.annotation.NoSuchIdException;

/**
 * The implementation of the annotation service.
 */
public class AnnotationsImpl implements Annotations {
  private static final Log    log               = LogFactory.getLog(AnnotationsImpl.class);
  private static final Map    aliases           = ItqlHelper.getDefaultAliases();
  private static final String ANNOTATION_PID_NS = "annotation";

  //
  private static final Configuration CONF = ConfigurationStore.getInstance().getConfiguration();

  //
  private static final String MODEL = "<" + CONF.getString("topaz.models.annotations") + ">";

  //
  private static final String CREATE_ITQL =
    ("insert <${id}> <r:type> <a:Annotation> <${id}> <r:type> <${type}>"
    + " <${id}> <topaz:state> '0' <${id}> <a:annotates> <${annotates}>"
    + " <${id}> <a:created> '${created}' <${id}> <a:context> '${context}'"
    + " <${id}> <a:body> <${body}> <${id}> <${creator}> '${user}'"
    + " <${id}> <dt:isReplacedBy> <r:nil> into ${MODEL};").replaceAll("\\Q${MODEL}", MODEL);
  private static final String SUPERSEDE_ITQL =
    ("insert <${supersedes}> <dt:isReplacedBy> <${id}> into ${MODEL};"
    + "delete <${supersedes}> <dt:isReplacedBy> <r:nil> from ${MODEL};"
    + "insert <${id}> <dt:replaces> <${supersedes}> into ${MODEL};").replaceAll("\\Q${MODEL}", MODEL);
  private static final String INSERT_TITLE_ITQL =
    ("insert <${id}> <d:title> '${title}' into ${MODEL};").replaceAll("\\Q${MODEL}", MODEL);
  private static final String INSERT_MEDIATOR_ITQL =
    ("insert <${id}> <dt:mediator> '${mediator}' into ${MODEL};").replaceAll("\\Q${MODEL}", MODEL);
  private static final String DELETE_ITQL =
    ("insert select $a <dt:isReplacedBy> $c from ${MODEL}"
    + " where $a <dt:isReplacedBy> <${id}> and <${id}> <dt:isReplacedBy> $c into ${MODEL};"
    + " delete select $a <dt:isReplacedBy> <${id}> from ${MODEL} where $a <dt:isReplacedBy> <${id}>"
    + " from ${MODEL}; delete select <${id}> $p $o from ${MODEL} where <${id}> $p $o"
    + " from ${MODEL}; delete select $a <dt:isReplacedBy> <r:nil>"
    + " from ${MODEL} where $a <dt:isReplacedBy> $c and $c <r:type> <a:Annotation> from ${MODEL};"
    + "delete select $a <dt:replaces> <${id}> from ${MODEL} where $a <dt:replaces> <${id}> "
    + " from ${MODEL};").replaceAll("\\Q${MODEL}", MODEL);
  private static final String GET_ITQL =
    ("select $p $o from ${MODEL} where <${id}> $p $o;").replaceAll("\\Q${MODEL}", MODEL);
  private static final String LIST_ITQL =
    ("select $s subquery(select $p $o from ${MODEL} where $s $p $o) from ${MODEL} "
    + " where $s <a:annotates> <${annotates}>            "
    + "    and $s <dt:isReplacedBy> <r:nil>              "
    + "    and $s <r:type> <${type}>;                    ").replaceAll("\\Q${MODEL}", MODEL);
  private static final String LIST_BY_MEDIATOR_ITQL =
    ("select $s subquery(select $p $o from ${MODEL} where $s $p $o) from ${MODEL} "
    + " where $s <a:annotates> <${annotates}>            "
    + "    and $s <dt:isReplacedBy> <r:nil>              "
    + "    and $s <dt:mediator> '${mediator}'            "
    + "    and $s <r:type> <${type}>;                    ").replaceAll("\\Q${MODEL}", MODEL);
  private static final String LATEST_ITQL =
    ("select $s subquery(select $p $o from ${MODEL} where $s $p $o) from ${MODEL} "
    + " where ( (walk(<${id}> <dt:isReplacedBy> $c and $c <dt:isReplacedBy> $s)"
    + "          and $s <dt:isReplacedBy> <r:nil>)      "
    + "          or                                     "
    + "         ($s <dt:isReplacedBy> <r:nil> and $s <tucana:is> <${id}>) )"
    + " and $s <r:type> <a:Annotation>;").replaceAll("\\Q${MODEL}", MODEL);
  private static final String PRECEDING_ITQL =
    ("select $s subquery(select $p $o from ${MODEL} where $s $p $o) from ${MODEL} "
    + " where walk($s <dt:isReplacedBy> <${id}> and $s <dt:isReplacedBy> $c)"
    + " and $s <r:type> <a:Annotation>;").replaceAll("\\Q${MODEL}", MODEL);
  private static final String PRECEDING_ALL_ITQL =
    ("select $s from ${MODEL} where"
    + " walk($c <dt:isReplacedBy> <${id}> and $s <dt:isReplacedBy> $c) "
    + " and $s <r:type> <a:Annotation>;").replaceAll("\\Q${MODEL}", MODEL);
  private static final String SET_STATE_ITQL =
    ("delete select <${id}> <topaz:state> $o from ${MODEL}"
    + " where <${id}> <topaz:state> $o from ${MODEL};"
    + " insert <${id}> <topaz:state> '${state}' into ${MODEL};").replaceAll("\\Q${MODEL}", MODEL);
  private static final String CHECK_ID_ITQL =
    ("select $s from ${MODEL} where $s <r:type> <a:Annotation> and   $s <tucana:is> <${id}>;")
     .replaceAll("\\Q${MODEL}", MODEL);

  // xxx : can't find the fedora stuff. 
  private static final String FEDORA_LIST_ITQL =
    ("select $f from ${MODEL} where" // + " $f <fedora:fedora-system:def/model#contentModel> 'Annotation' and"
    + " $s <a:body> $f and ($s <tucana:is> <${id}>"
    + " or (walk($c <dt:isReplacedBy> <${id}> and $s <dt:isReplacedBy> $c)"
    + " and $s <r:type> <a:Annotation>));").replaceAll("\\Q${MODEL}", MODEL);
  private static final String FEDORA_ID_ITQL =
    ("select $f from ${MODEL} where" //  + " $f <fedora:fedora-system:def/model#contentModel> 'Annotation' and"
    + " $s <a:body> $f and $s <tucana:is> <${id}>;").replaceAll("\\Q${MODEL}", MODEL);

  static {
    aliases.put("a", AnnotationModel.a.toString());
    aliases.put("r", AnnotationModel.r.toString());
    aliases.put("d", AnnotationModel.d.toString());
    aliases.put("dt", AnnotationModel.dt.toString());
  }

  private final AnnotationsPEP pep;
  private final ItqlHelper     itql;
  private final FedoraHelper   fedora;
  private final String         user;
  private final String         baseURI;

  /**
   * Creates a new AnnotationsImpl object.
   *
   * @param pep The xacml pep
   * @param itql The itql service
   * @param fedoraServer The fedora server uri
   * @param apim Fedora API-M stub
   * @param uploader Fedora uploader stub
   * @param user The authenticated user
   * @param baseURI The base uri for annotation ids
   */
  public AnnotationsImpl(AnnotationsPEP pep, ItqlHelper itql, URI fedoraServer, FedoraAPIM apim,
                         Uploader uploader, String user, String baseURI) {
    this.pep       = pep;
    this.itql      = itql;
    this.fedora    = new FedoraHelper(fedoraServer, apim, uploader);
    this.user      = (user == null) ? "anonymous" : user;
    this.baseURI   = baseURI;

    itql.setAliases(aliases);
  }

  /*
   * @see org.topazproject.ws.annotation.Annotations#createAnnotation
   */
  public String createAnnotation(String mediator, String type, String annotates, String context,
                                 String supersedes, boolean anonymize, String title, String body)
                          throws NoSuchIdException, RemoteException {
    itql.validateUri(body, "body");

    return createAnnotation(mediator, type, annotates, context, supersedes, anonymize, title, body,
                            null, null);
  }

  /*
   * @see org.topazproject.ws.annotation.Annotations#createAnnotation
   */
  public String createAnnotation(String mediator, String type, String annotates, String context,
                                 String supersedes, boolean anonymize, String title,
                                 String contentType, byte[] content)
                          throws NoSuchIdException, RemoteException {
    if (contentType == null)
      throw new NullPointerException("'contentType' cannot be null");

    if (content == null)
      throw new NullPointerException("'content' cannot be null");

    return createAnnotation(mediator, type, annotates, context, supersedes, anonymize, title, null,
                            contentType, content);
  }

  private String createAnnotation(String mediator, String type, String annotates, String context,
                                  String supersedes, boolean anonymize, String title, String body,
                                  String contentType, byte[] content)
                           throws NoSuchIdException, RemoteException {
    if (context == null)
      context = annotates;
    else
      context = itql.escapeLiteral(context);

    if (type == null)
      type = "http://www.w3.org/2000/10/annotationType#Annotation";
    else
      itql.validateUri(type, "type");

    pep.checkAccess(pep.CREATE_ANNOTATION, itql.validateUri(annotates, "annotates"));

    if (supersedes != null) {
      URI supersedesUri = itql.validateUri(supersedes, "supersedes");
      pep.checkAccess(pep.SUPERSEDE, supersedesUri);
      checkId(supersedesUri);
    }

    String id = getNextId();

    if (body == null) {
      body = fedora.createBody(contentType, content, "Annotation", "Annotation Body");

      if (log.isDebugEnabled())
        log.debug("created fedora object " + body + " for annotation " + id);
    }

    String create = CREATE_ITQL;
    Map    values = new HashMap();

    values.put("id", id);
    values.put("type", type);
    values.put("annotates", annotates);
    values.put("context", context);
    values.put("body", body);
    values.put("user", user);
    values.put("created", itql.getUTCTime());

    values.put("creator", (anonymize ? "topaz:anonymousCreator" : "dc:creator"));

    if (supersedes != null) {
      create += SUPERSEDE_ITQL;
      values.put("supersedes", supersedes);
    }

    if (title != null) {
      create += INSERT_TITLE_ITQL;
      values.put("title", itql.escapeLiteral(title));
    }

    if (mediator != null) {
      create += INSERT_MEDIATOR_ITQL;
      values.put("mediator", itql.escapeLiteral(mediator));
    }

    itql.doUpdate(itql.bindValues(create, values));

    if (log.isDebugEnabled())
      log.debug("created annotaion " + id + " for " + annotates + " annotated by " + body);

    return id;
  }

  /*
   * @see org.topazproject.ws.annotation.Annotations#deleteAnnotation
   */
  public void deleteAnnotation(String id, boolean deletePreceding)
                        throws NoSuchIdException, RemoteException {
    URI thisUri = itql.validateUri(id, "annotation-id");
    pep.checkAccess(pep.DELETE_ANNOTATION, thisUri);
    checkId(thisUri);

    String[] preceding = deletePreceding ? getPrecedingAll(id) : new String[0];

    for (int i = 0; i < preceding.length; i++)
      pep.checkAccess(pep.DELETE_ANNOTATION, URI.create(preceding[i]));

    String[] purgeList = getFedoraObjects(id, deletePreceding);

    String   del = DELETE_ITQL.replaceAll("\\Q${id}", id);

    String   txn = "delete " + id;

    try {
      itql.beginTxn(txn);
      itql.doUpdate(del);

      if (log.isDebugEnabled())
        log.debug("deleted " + id);

      for (int i = 0; i < preceding.length; i++) {
        del = DELETE_ITQL.replaceAll("\\Q${id}", preceding[i]);
        itql.doUpdate(del);

        if (log.isDebugEnabled())
          log.debug("deleted preceding " + preceding[i]);
      }

      itql.commitTxn(txn);
      txn = null;
    } finally {
      try {
        if (txn != null)
          itql.rollbackTxn(txn);
      } catch (Throwable t) {
      }
    }

    fedora.purgeObjects(purgeList);
  }

  /*
   * @see org.topazproject.ws.annotation.Annotations#getAnnotationInfo
   */
  public AnnotationInfo getAnnotationInfo(String id) throws NoSuchIdException, RemoteException {
    pep.checkAccess(pep.GET_ANNOTATION_INFO, itql.validateUri(id, "annotation-id"));

    try {
      String query = GET_ITQL.replaceAll("\\Q${id}", id);

      Answer ans  = new Answer(itql.doQuery(query));
      List   rows = ((Answer.QueryAnswer) ans.getAnswers().get(0)).getRows();

      if (rows.size() == 0)
        throw new NoSuchIdException(id);

      return buildAnnotationInfo(id, rows);
    } catch (AnswerException e) {
      throw new RemoteException("Error querying RDF", e);
    }
  }

  /*
   * @see org.topazproject.ws.annotation.Annotations#listAnnotations
   */
  public AnnotationInfo[] listAnnotations(String mediator, String annotates, String type)
                                   throws RemoteException {
    pep.checkAccess(pep.LIST_ANNOTATIONS, itql.validateUri(annotates, "annotates"));

    try {
      if (type == null)
        type = "a:Annotation";
      else
        itql.validateUri(type, "type");

      Map values = new HashMap();
      values.put("annotates", annotates);
      values.put("type", type);

      String query;

      if (mediator == null)
        query = LIST_ITQL;
      else {
        query = LIST_BY_MEDIATOR_ITQL;
        values.put("mediator", itql.escapeLiteral(mediator));
      }

      query = itql.bindValues(query, values);

      Answer ans = new Answer(itql.doQuery(query));

      List   rows = ((Answer.QueryAnswer) ans.getAnswers().get(0)).getRows();

      return allowedSubset(buildAnnotationInfoList(rows));
    } catch (AnswerException e) {
      throw new RemoteException("Error querying RDF", e);
    }
  }

  /*
   * @see org.topazproject.ws.annotation.Annotations#getLatestAnnotations
   */
  public AnnotationInfo[] getLatestAnnotations(String id)
                                        throws NoSuchIdException, RemoteException {
    URI thisUri = itql.validateUri(id, "annotation-id");
    pep.checkAccess(pep.GET_ANNOTATION_INFO, thisUri);

    try {
      String query = LATEST_ITQL.replaceAll("\\Q${id}", id);

      Answer ans = new Answer(itql.doQuery(query));

      List   rows = ((Answer.QueryAnswer) ans.getAnswers().get(0)).getRows();

      if (rows.size() == 0)
        throw new NoSuchIdException(id);

      return checkAccess(thisUri, buildAnnotationInfoList(rows));
    } catch (AnswerException e) {
      throw new RemoteException("Error querying RDF", e);
    }
  }

  /*
   * @see org.topazproject.ws.annotation.Annotations#getPrecedingAnnotations
   */
  public AnnotationInfo[] getPrecedingAnnotations(String id)
                                           throws NoSuchIdException, RemoteException {
    URI thisUri = itql.validateUri(id, "annotation-id");
    pep.checkAccess(pep.GET_ANNOTATION_INFO, thisUri);
    checkId(thisUri);

    try {
      String query = PRECEDING_ITQL.replaceAll("\\Q${id}", id);

      Answer ans = new Answer(itql.doQuery(query));

      List   rows = ((Answer.QueryAnswer) ans.getAnswers().get(0)).getRows();

      return checkAccess(thisUri, buildAnnotationInfoList(rows));
    } catch (AnswerException e) {
      throw new RemoteException("Error querying RDF", e);
    }
  }

  /*
   * @see org.topazproject.ws.annotation.Annotations#setAnnotationState
   */
  public void setAnnotationState(String id, int state)
                          throws RemoteException, NoSuchIdException {
    URI thisUri = itql.validateUri(id, "annotation-id");
    pep.checkAccess(pep.SET_ANNOTATION_STATE, thisUri);
    checkId(thisUri);

    String set = SET_STATE_ITQL.replaceAll("\\Q${id}", id).replaceAll("\\Q${state}", "" + state);

    itql.doUpdate(set);
  }

  /*
   * @see org.topazproject.ws.annotation.Annotations#listAnnotations
   */
  public String[] listAnnotations(String mediator, int state)
                           throws RemoteException {
    pep.checkAccess(pep.LIST_ANNOTATIONS_IN_STATE, URI.create(baseURI + ANNOTATION_PID_NS));

    try {
      String query =
        "select $a $o from " + MODEL + " where $a <r:type> <a:Annotation> and $a <topaz:state> $o";

      if (mediator != null)
        query += (" and $a <dt:mediator> '" + itql.escapeLiteral(mediator) + "'");

      if (state == 0)
        query += " and exclude($a <topaz:state> '0');";
      else
        query += (" and $a <topaz:state> '" + state + "';");

      Answer    ans  = new Answer(itql.doQuery(query));
      List      rows = ((Answer.QueryAnswer) ans.getAnswers().get(0)).getRows();

      int       c      = rows.size();
      ArrayList result = new ArrayList(c);

      for (int i = 0; i < c; i++) {
        Object[] cols = (Object[]) rows.get(i);

        // xxx: work around an exclude bug in ITQL
        if ((state == 0) && AnnotationModel.getColumnValue(cols[1]).toString().equals("0"))
          continue;

        result.add(AnnotationModel.getColumnValue(cols[0]).toString());
      }

      return (String[]) result.toArray(new String[0]);
    } catch (AnswerException e) {
      throw new RemoteException("Error querying RDF", e);
    }
  }

  private void checkId(URI uri) throws RemoteException, NoSuchIdException {
    String id = uri.toString();

    try {
      String query = CHECK_ID_ITQL.replaceAll("\\Q${id}", id);
      Answer ans  = new Answer(itql.doQuery(query));
      List   rows = ((Answer.QueryAnswer) ans.getAnswers().get(0)).getRows();

      if (rows.size() == 0)
        throw new NoSuchIdException(id);
    } catch (AnswerException e) {
      throw new RemoteException("Error querying RDF", e);
    }
  }

  private AnnotationInfo buildAnnotationInfo(String id, List rows) {
    AnnotationInfo info = AnnotationModel.create(id, rows);
    info.setBody(fedora.getBodyURL(info.getBody()));

    return info;
  }

  private AnnotationInfo[] buildAnnotationInfoList(List rows) {
    AnnotationInfo[] annotations = new AnnotationInfo[rows.size()];

    for (int i = 0; i < annotations.length; i++) {
      Object[]           cols           = (Object[]) rows.get(i);
      URIReference       ref            = (URIReference) cols[0];
      String             id             = ref.getURI().toString();
      Answer.QueryAnswer subQueryAnswer = (Answer.QueryAnswer) cols[1];
      annotations[i] = buildAnnotationInfo(id, subQueryAnswer.getRows());
    }

    return annotations;
  }

  private AnnotationInfo[] allowedSubset(AnnotationInfo[] list) {
    ArrayList allowed = new ArrayList();

    for (int i = 0; i < list.length; i++) {
      AnnotationInfo info = list[i];

      try {
        pep.checkAccess(pep.GET_ANNOTATION_INFO, URI.create(info.getId()));
        allowed.add(info);
      } catch (SecurityException e) {
        if (log.isDebugEnabled())
          log.debug("no permission for " + info.getId() + " filtered out from result-set");
      }
    }

    return (AnnotationInfo[]) allowed.toArray(new AnnotationInfo[0]);
  }

  private AnnotationInfo[] checkAccess(URI id, AnnotationInfo[] list) {
    for (int i = 0; i < list.length; i++) {
      URI u = URI.create(list[i].getId());

      if (!u.equals(id))
        pep.checkAccess(pep.GET_ANNOTATION_INFO, u);
    }

    return list;
  }

  private String[] buildAnnotationIdList(List rows) {
    String[] annotations = new String[rows.size()];

    for (int i = 0; i < annotations.length; i++) {
      Object[]     cols = (Object[]) rows.get(i);
      URIReference ref = (URIReference) cols[0];
      String       id  = ref.getURI().toString();
      annotations[i] = id;
    }

    return annotations;
  }

  private String[] getFedoraObjects(String id, boolean preceding)
                             throws RemoteException {
    String query = preceding ? FEDORA_LIST_ITQL : FEDORA_ID_ITQL;
    query = query.replaceAll("\\Q${id}", id);

    try {
      Answer ans  = new Answer(itql.doQuery(query));
      List   rows = ((Answer.QueryAnswer) ans.getAnswers().get(0)).getRows();

      int    c    = rows.size();
      List   pids = new ArrayList(c);

      for (int i = 0; i < c; i++) {
        Object[]     cols = (Object[]) rows.get(i);
        URIReference ref = (URIReference) cols[0];
        String       uri = ref.getURI().toString();

        if (uri.startsWith("info:fedora"))
          pids.add(fedora.uri2PID(uri));
      }

      return (String[]) pids.toArray(new String[0]);
    } catch (AnswerException e) {
      throw new RemoteException("Error querying RDF", e);
    }
  }

  private String[] getPrecedingAll(String id) throws RemoteException {
    try {
      String query = PRECEDING_ALL_ITQL.replaceAll("\\Q${id}", id);

      Answer ans = new Answer(itql.doQuery(query));

      List   rows = ((Answer.QueryAnswer) ans.getAnswers().get(0)).getRows();

      return buildAnnotationIdList(rows);
    } catch (AnswerException e) {
      throw new RemoteException("Error querying RDF", e);
    }
  }

  private String getNextId() throws RemoteException {
    return baseURI + fedora.getNextId(ANNOTATION_PID_NS).replace(':', '/');
  }
}
