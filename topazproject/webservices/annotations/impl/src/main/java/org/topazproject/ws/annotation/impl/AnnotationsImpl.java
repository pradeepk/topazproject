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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.jrdf.graph.Literal;
import org.jrdf.graph.URIReference;

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
  private static final String MODEL             = "<rmi://localhost/fedora#ri>";
  private static final String ANNOTATION_PID_NS = "annotation";

  //
  private static final String CREATE_ITQL =
    ("insert <${id}> <r:type> <a:Annotation> <${id}> <r:type> <${type}>"
    + " <${id}> <a:state> '0' <${id}> <a:annotates> <${annotates}> <${id}> <a:created> '${created}'"
    + " <${id}> <a:context> '${context}' <${id}> <a:body> <${body}> <${id}> <d:creator> '${user}'"
    + " <${id}> <a:supersededBy> <r:nil> into ${MODEL};").replaceAll("\\Q${MODEL}", MODEL);
  private static final String SUPERSEDE_ITQL =
    ("insert <${supersedes}> <a:supersededBy> <${id}> into ${MODEL};"
    + "delete <${supersedes}> <a:supersededBy> <r:nil> from ${MODEL};                     ")
     .replaceAll("\\Q${MODEL}", MODEL);
  private static final String INSERT_TITLE_ITQL =
    ("insert <${id}> <d:title> '${title}' into ${MODEL};").replaceAll("\\Q${MODEL}", MODEL);
  private static final String DELETE_ITQL =
    ("insert select $a <a:supersededBy> $c from ${MODEL}"
    + " where $a <a:supersededBy> <${id}> and <${id}> <a:supersededBy> $c into ${MODEL};"
    + " delete select $a <a:supersededBy> <${id}> from ${MODEL} where $a <a:supersededBy> <${id}>"
    + " from ${MODEL}; delete select <${id}> $p $o from ${MODEL} where <${id}> $p $o"
    + " from ${MODEL}; delete select $a <a:supersededBy> <r:nil>"
    + " from ${MODEL} where $a <a:supersededBy> $c and $c <r:type> <a:Annotation> from ${MODEL};")
     .replaceAll("\\Q${MODEL}", MODEL);
  private static final String GET_ITQL =
    ("select $p $o from ${MODEL} where <${id}> $p $o;").replaceAll("\\Q${MODEL}", MODEL);
  private static final String LIST_ITQL =
    ("select $s subquery(select $p $o from ${MODEL} where $s $p $o) from ${MODEL} "
    + " where $s <a:annotates> <${annotates}>            "
    + "    and $s <a:state> '0'                          "
    + "    and $s <a:supersededBy> <r:nil>               "
    + "    and $s <r:type> <${type}>;                    ").replaceAll("\\Q${MODEL}", MODEL);
  private static final String LATEST_ITQL =
    ("select $s subquery(select $p $o from ${MODEL} where $s $p $o) from ${MODEL} "
    + " where ( (walk(<${id}> <a:supersededBy> $c and $c <a:supersededBy> $s)"
    + "          and $s <a:supersededBy> <r:nil>)       "
    + "          or                                     "
    + "         ($s <a:supersededBy> <r:nil> and $s <tucana:is> <${id}>) )"
    + " and $s <a:state> '0' and $s <r:type> <a:Annotation>;").replaceAll("\\Q${MODEL}", MODEL);
  private static final String PRECEDING_ITQL =
    ("select $s subquery(select $p $o from ${MODEL} where $s $p $o) from ${MODEL} "
    + " where walk($s <a:supersededBy> <${id}> and $s <a:supersededBy> $c)"
    + " and $s <a:state> '0' and $s <r:type> <a:Annotation>;").replaceAll("\\Q${MODEL}", MODEL);
  private static final String PRECEDING_ALL_ITQL =
    ("select $s from ${MODEL} where"
    + " walk($c <a:supersededBy> <${id}> and $s <a:supersededBy> $c) "
    + " and $s <r:type> <a:Annotation>;").replaceAll("\\Q${MODEL}", MODEL);
  private static final String SET_STATE_ITQL =
    ("delete select <${id}> <a:state> $o from ${MODEL}"
    + " where <${id}> <a:state> $o from ${MODEL};"
    + " insert <${id}> <a:state> '${state}' into ${MODEL};").replaceAll("\\Q${MODEL}", MODEL);
  private static final String LIST_STATE_ITQL =
    ("select $a from ${MODEL} where $a <r:type> <a:Annotation> and $a <a:state> '${state}';")
     .replaceAll("\\Q${MODEL}", MODEL);
  private static final String CHECK_ID_ITQL =
    ("select $s from ${MODEL} where $s <r:type> <a:Annotation> and   $s <tucana:is> <${id}>")
     .replaceAll("\\Q${MODEL}", MODEL);

  // xxx : can't find the fedora stuff. 
  private static final String FEDORA_LIST_ITQL =
    ("select $f from ${MODEL} where" // + " $f <fedora:fedora-system:def/model#contentModel> 'Annotation' and"
    + " $s <a:body> $f and ($s <tucana:is> <${id}>"
    + " or (walk($c <a:supersededBy> <${id}> and $s <a:supersededBy> $c)"
    + " and $s <r:type> <a:Annotation>))").replaceAll("\\Q${MODEL}", MODEL);
  private static final String FEDORA_ID_ITQL =
    ("select $f from ${MODEL} where" //  + " $f <fedora:fedora-system:def/model#contentModel> 'Annotation' and"
    + " $s <a:body> $f and $s <tucana:is> <${id}>").replaceAll("\\Q${MODEL}", MODEL);

  static {
    aliases.put("a", AnnotationModel.a.toString());
    aliases.put("r", AnnotationModel.r.toString());
    aliases.put("d", AnnotationModel.d.toString());
  }

  private final AnnotationsPEP pep;
  private final ItqlHelper     itql;
  private final FedoraHelper   fedora;
  private final String         user;

  /**
   * Creates a new AnnotationsImpl object.
   *
   * @param pep The xacml pep
   * @param itql The itql service
   * @param fedoraServer The fedora server uri
   * @param apim Fedora API-M stub
   * @param uploader Fedora uploader stub
   * @param user The authenticated user
   */
  public AnnotationsImpl(AnnotationsPEP pep, ItqlHelper itql, URI fedoraServer, FedoraAPIM apim,
                         Uploader uploader, String user) {
    this.pep      = pep;
    this.itql     = itql;
    this.fedora   = new FedoraHelper(fedoraServer, apim, uploader);
    this.user     = (user == null) ? "anonymous" : user;

    itql.setAliases(aliases);
  }

  /**
   * @see org.topazproject.ws.annotation.Annotation#createAnnotation
   */
  public String createAnnotation(String type, String annotates, String context, String supersedes,
                                 String title, String body)
                          throws NoSuchIdException, RemoteException {
    itql.validateUri(body, "body");

    return createAnnotation(type, annotates, context, supersedes, title, body, null, null);
  }

  /**
   * @see org.topazproject.ws.annotation.Annotation#createAnnotation
   */
  public String createAnnotation(String type, String annotates, String context, String supersedes,
                                 String title, String contentType, byte[] content)
                          throws NoSuchIdException, RemoteException {
    if (contentType == null)
      throw new NullPointerException("'contentType' cannot be null");

    if (content == null)
      throw new NullPointerException("'content' cannot be null");

    return createAnnotation(type, annotates, context, supersedes, title, null, contentType, content);
  }

  private String createAnnotation(String type, String annotates, String context, String supersedes,
                                  String title, String body, String contentType, byte[] content)
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
      pep.checkAccess(pep.SET_ANNOTATION_INFO, itql.validateUri(supersedes, "supersedes"));
      checkId(supersedes);
    }

    String id = fedora.getNextId(ANNOTATION_PID_NS);

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

    if (supersedes != null) {
      create += SUPERSEDE_ITQL;
      values.put("supersedes", supersedes);
    }

    if (title != null) {
      create += INSERT_TITLE_ITQL;
      values.put("title", itql.escapeLiteral(title));
    }

    itql.doUpdate(itql.bindValues(create, values));

    if (log.isDebugEnabled())
      log.debug("created annotaion " + id + " for " + annotates + " annotated by " + body);

    return id;
  }

  /**
   * @see org.topazproject.ws.annotation.Annotation#deleteAnnotation
   */
  public void deleteAnnotation(String id, boolean deletePreceding)
                        throws NoSuchIdException, RemoteException {
    pep.checkAccess(pep.DELETE_ANNOTATION, itql.validateUri(id, "annotation-id"));
    checkId(id);

    String[] purgeList = getFedoraObjects(id, deletePreceding);

    String[] preceding = deletePreceding ? getPrecedingAll(id) : new String[0];

    String   delete = DELETE_ITQL.replaceAll("\\Q${id}", id);

    String   txn = "delete " + id;

    try {
      itql.beginTxn(txn);
      itql.doUpdate(delete);

      if (log.isDebugEnabled())
        log.debug("deleted " + id);

      for (int i = 0; i < preceding.length; i++) {
        delete = DELETE_ITQL.replaceAll("\\Q${id}", preceding[i]);
        itql.doUpdate(delete);

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

  /**
   * @see org.topazproject.ws.annotation.Annotation#getAnnotationInfo
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

  /**
   * @see org.topazproject.ws.annotation.Annotation#listAnnotations
   */
  public AnnotationInfo[] listAnnotations(String annotates, String type)
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

      String query = itql.bindValues(LIST_ITQL, values);

      Answer ans = new Answer(itql.doQuery(query));

      List   rows = ((Answer.QueryAnswer) ans.getAnswers().get(0)).getRows();

      return buildAnnotationInfoList(rows);
    } catch (AnswerException e) {
      throw new RemoteException("Error querying RDF", e);
    }
  }

  /**
   * @see org.topazproject.ws.annotation.Annotation#getLatestAnnotations
   */
  public AnnotationInfo[] getLatestAnnotations(String id)
                                        throws NoSuchIdException, RemoteException {
    pep.checkAccess(pep.GET_ANNOTATION_INFO, itql.validateUri(id, "annotation-id"));

    try {
      String query = LATEST_ITQL.replaceAll("\\Q${id}", id);

      Answer ans = new Answer(itql.doQuery(query));

      List   rows = ((Answer.QueryAnswer) ans.getAnswers().get(0)).getRows();

      if (rows.size() == 0)
        throw new NoSuchIdException(id);

      return buildAnnotationInfoList(rows);
    } catch (AnswerException e) {
      throw new RemoteException("Error querying RDF", e);
    }
  }

  /**
   * @see org.topazproject.ws.annotation.Annotation#getPrecedingAnnotations
   */
  public AnnotationInfo[] getPrecedingAnnotations(String id)
                                           throws NoSuchIdException, RemoteException {
    pep.checkAccess(pep.GET_ANNOTATION_INFO, itql.validateUri(id, "annotation-id"));
    checkId(id);

    try {
      String query = PRECEDING_ITQL.replaceAll("\\Q${id}", id);

      Answer ans = new Answer(itql.doQuery(query));

      List   rows = ((Answer.QueryAnswer) ans.getAnswers().get(0)).getRows();

      return buildAnnotationInfoList(rows);
    } catch (AnswerException e) {
      throw new RemoteException("Error querying RDF", e);
    }
  }

  /**
   * @see org.topazproject.ws.annotation.Annotation#setAnnotationState
   */
  public void setAnnotationState(String id, int state)
                          throws RemoteException, NoSuchIdException {
    pep.checkAccess(pep.SET_ANNOTATION_STATE, itql.validateUri(id, "annotation-id"));
    checkId(id);

    String set = SET_STATE_ITQL.replaceAll("\\Q${id}", id).replaceAll("\\Q$state", "" + state);

    itql.doUpdate(set);
  }

  /**
   * @see org.topazproject.ws.annotation.Annotation#listAnnotations
   */
  public String[] listAnnotations(int state) throws RemoteException {
    pep.checkAccess(pep.LIST_ANNOTATIONS_IN_STATE, URI.create("" + state));

    try {
      String query = LIST_STATE_ITQL.replaceAll("\\Q$state", "" + state);

      Answer ans  = new Answer(itql.doQuery(query));
      List   rows = ((Answer.QueryAnswer) ans.getAnswers().get(0)).getRows();

      return buildAnnotationIdList(rows);
    } catch (AnswerException e) {
      throw new RemoteException("Error querying RDF", e);
    }
  }

  private void checkId(String id) throws RemoteException, NoSuchIdException {
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
}
