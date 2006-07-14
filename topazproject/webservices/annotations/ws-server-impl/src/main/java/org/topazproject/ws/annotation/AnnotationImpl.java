package org.topazproject.ws.annotation;

import java.io.ByteArrayInputStream;

import java.net.URI;
import java.net.URISyntaxException;

import java.rmi.RemoteException;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.jrdf.graph.Literal;
import org.jrdf.graph.URIReference;

import org.topazproject.mulgara.itql.Answer;
import org.topazproject.mulgara.itql.AnswerException;
import org.topazproject.mulgara.itql.ItqlHelper;

import fedora.client.Uploader;

import fedora.server.management.FedoraAPIM;

/**
 * The implementation of the annotation service.
 */
public class AnnotationImpl implements Annotation {
  private static final Log              log               = LogFactory.getLog(AnnotationImpl.class);
  private static final Map              aliases           = ItqlHelper.getDefaultAliases();
  private static final String           MODEL             = "<rmi://localhost/fedora#ri>";
  private static final String           ANNOTATION_PID_NS = "annotation";
  private static final SimpleDateFormat XSD_DATE_TIME_FMT =
    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");

  //
  private static final URI a            = URI.create("http://www.w3.org/2000/10/annotation-ns#");
  private static final URI r            = URI.create("http://www.w3.org/1999/02/22-rdf-syntax-ns#");
  private static final URI d            = URI.create("http://purl.org/dc/elements/1.1/");
  private static final URI nil          =
    URI.create("http://www.w3.org/1999/02/22-rdf-syntax-ns#nil");
  private static final URI a_Annotation =
    URI.create("http://www.w3.org/2000/10/annotation-ns#Annotation");

  //
  private static final String FOXML =
    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
    + "<foxml:digitalObject xmlns:foxml=\"info:fedora/fedora-system:def/foxml#\">"
    + "<foxml:objectProperties>"
    + "<foxml:property NAME=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#type\" VALUE=\"FedoraObject\"/>"
    + "<foxml:property NAME=\"info:fedora/fedora-system:def/model#state\" VALUE=\"A\"/>"
    + "<foxml:property NAME=\"info:fedora/fedora-system:def/model#label\" VALUE=\"Annotation Body\"/>"
    + "<foxml:property NAME=\"info:fedora/fedora-system:def/model#contentModel\" VALUE=\"Annotation\"/>"
    + "</foxml:objectProperties>"
    + "<foxml:datastream CONTROL_GROUP=\"M\" ID=\"BODY\" STATE=\"A\">"
    + "<foxml:datastreamVersion ID=\"BODY1.0\" MIMETYPE=\"$CONTENTTYPE\" LABEL=\"Annotation Body\">"
    + "<foxml:contentLocation REF=\"$CONTENT\" TYPE=\"URL\"/>" + "</foxml:datastreamVersion>"
    + "</foxml:datastream>" + "</foxml:digitalObject>";

  //
  private static final String CREATE_ITQL =
    ("insert <$id> <r:type> <a:Annotation> <$id> <r:type> <$type>"
    + " <$id> <a:state> '0' <$id> <a:annotates> <$annotates> <$id> <a:created> '$created'"
    + " <$id> <a:context> '$context' <$id> <a:body> <$body> <$id> <d:creator> '$user'"
    + " <$id> <a:supersededBy> <r:nil> into $MODEL;").replaceAll("\\$MODEL", MODEL);
  private static final String SUPERSEDE_ITQL =
    ("insert <$supersedes> <a:supersededBy> <$id> into $MODEL;"
    + " delete <$supersedes> <a:supersededBy> <r:nil> from $MODEL;").replaceAll("\\$MODEL", MODEL);
  private static final String DELETE_ITQL =
    ("insert select $a <a:supersededBy> $c from $MODEL"
    + " where $a <a:supersededBy> <$id> and <$id> <a:supersededBy> $c into $MODEL;"
    + " delete select $a <a:supersededBy> <$id> from $MODEL where $a <a:supersededBy> <$id>"
    + " from $MODEL; delete select <$id> $p $o from $MODEL where <$id> $p $o"
    + " from $MODEL; delete select $a <a:supersededBy> <r:nil>"
    + " from $MODEL where $a <a:supersededBy> $c and $c <r:type> <a:Annotation> from $MODEL;")
     .replaceAll("\\$MODEL", MODEL);
  private static final String GET_ITQL =
    ("select $p $o from $MODEL where <$id> $p $o;").replaceAll("\\$MODEL", MODEL);
  private static final String SUBQUERY =
    ("subquery(select $p $o from $MODEL where $s $p $o)").replaceAll("\\$MODEL", MODEL);
  private static final String LIST_ITQL =
    ("select $s $subquery from $MODEL where $s <a:annotates> <$annotates> and $s <a:state> '0'"
    + " and $s <a:supersededBy> <r:nil> and $s <r:type> <$type>;").replaceAll("\\$MODEL", MODEL);
  private static final String LATEST_ITQL =
    ("select $s $subquery from $MODEL where"
    + " ( (walk(<$id> <a:supersededBy> $c and $c <a:supersededBy> $s)"
    + "  and $s <a:supersededBy> <r:nil>)"
    + " or ($s <a:supersededBy> <r:nil> and $s <tucana:is> <$id>) ) and $s <a:state> '0'"
    + " and $s <r:type> <a:Annotation>" + ";").replaceAll("\\$MODEL", MODEL);
  private static final String PRECEDING_ITQL =
    ("select $s $subquery from $MODEL where"
    + " walk($s <a:supersededBy> <$id> and $s <a:supersededBy> $c) and $s <a:state> '0'"
    + " and $s <r:type> <a:Annotation>;").replaceAll("\\$MODEL", MODEL);
  private static final String PRECEDING_ALL_ITQL =
    ("select $s from $MODEL where" + " walk($c <a:supersededBy> <$id> and $s <a:supersededBy> $c) "
    + " and $s <r:type> <a:Annotation>;").replaceAll("\\$MODEL", MODEL);
  private static final String SET_STATE_ITQL =
    ("delete select <$id> <a:state> $o from $MODEL" + " where <$id> <a:state> $o from $MODEL;"
    + " insert <$id> <a:state> '$state' into $MODEL;").replaceAll("\\$MODEL", MODEL);
  private static final String LIST_STATE_ITQL =
    ("select $a from $MODEL where $a <a:state> '$state';").replaceAll("\\$MODEL", MODEL);
  private static final String CHECK_ID_ITQL =
    ("select $s from $MODEL where $s <r:type> <a:Annotation> and   $s <tucana:is> <$id>")
     .replaceAll("\\$MODEL", MODEL);

  // xxx : can't find the fedora stuff. 
  private static final String FEDORA_LIST_ITQL =
    ("select $f from $MODEL where" // + " $f <fedora:fedora-system:def/model#contentModel> 'Annotation' and"
    + " $s <a:body> $f and ($s <tucana:is> <$id>"
    + " or (walk($c <a:supersededBy> <$id> and $s <a:supersededBy> $c)"
    + " and $s <r:type> <a:Annotation>))").replaceAll("\\$MODEL", MODEL);
  private static final String FEDORA_ID_ITQL =
    ("select $f from $MODEL where" //  + " $f <fedora:fedora-system:def/model#contentModel> 'Annotation' and"
    + " $s <a:body> $f and $s <tucana:is> <$id>").replaceAll("\\$MODEL", MODEL);

  static {
    aliases.put("a", a.toString());
    aliases.put("r", r.toString());
    aliases.put("d", d.toString());
  }

  private final PEP        pep;
  private final ItqlHelper itql;
  private final URI        fedoraServer;
  private final FedoraAPIM apim;
  private final Uploader   uploader;
  private final String     user;

  /**
   * Creates a new AnnotationImpl object.
   *
   * @param pep The xacml pep
   * @param itql The itql service
   * @param fedoraServer The fedora server uri
   * @param apim Fedora API-M stub
   * @param uploader Fedora uploader stub
   * @param user The authenticated user
   */
  public AnnotationImpl(PEP pep, ItqlHelper itql, URI fedoraServer, FedoraAPIM apim,
                        Uploader uploader, String user) {
    this.pep            = pep;
    this.itql           = itql;
    this.fedoraServer   = fedoraServer;
    this.apim           = apim;
    this.uploader       = uploader;
    this.user           = (user == null) ? "anonymous" : user;

    itql.setAliases(aliases);
  }

  /**
   * @see org.topazproject.ws.annotation.Annotation#createAnnotation
   */
  public String createAnnotation(String type, String annotates, String context, String supersedes,
                                 String body) throws NoSuchIdException, RemoteException {
    validateUri(body, "body");

    return createAnnotation(type, annotates, context, supersedes, body, null, null);
  }

  /**
   * @see org.topazproject.ws.annotation.Annotation#createAnnotation
   */
  public String createAnnotation(String type, String annotates, String context, String supersedes,
                                 String contentType, byte[] content)
                          throws NoSuchIdException, RemoteException {
    if (contentType == null)
      throw new NullPointerException("'contentType' cannot be null");

    if (content == null)
      throw new NullPointerException("'content' cannot be null");

    return createAnnotation(type, annotates, context, supersedes, null, contentType, content);
  }

  private String createAnnotation(String type, String annotates, String context, String supersedes,
                                  String body, String contentType, byte[] content)
                           throws NoSuchIdException, RemoteException {
    if (context == null)
      context = annotates;
    else
      context = escapeLiteral(context);

    if (type == null)
      type = "http://www.w3.org/2000/10/annotationType#Annotation";
    else
      validateUri(type, "type");

    checkAccess(PEP.CREATE_ANNOTATION, validateUri(annotates, "annotates"));

    if (supersedes != null) {
      checkAccess(PEP.SET_ANNOTATION_INFO, validateUri(supersedes, "supersedes"));
      checkId(supersedes);
    }

    // xxx: cache a bunch of ids
    String id =
      apim.getNextPID(new org.apache.axis.types.NonNegativeInteger("1"), ANNOTATION_PID_NS)[0];

    if (body == null) {
      body = createBody(contentType, content);

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
    values.put("created", getUTCTime());

    if (supersedes != null) {
      create += SUPERSEDE_ITQL;
      values.put("supersedes", supersedes);
    }

    itql.doUpdate(replaceAll(create, values));

    if (log.isDebugEnabled())
      log.debug("created annotaion " + id + " for " + annotates + " annotated by " + body);

    return id;
  }

  private String createBody(String contentType, byte[] content)
                     throws RemoteException {
    try {
      String ref = uploader.upload(new ByteArrayInputStream(content));

      Map    values = new HashMap();
      values.put("CONTENTTYPE", contentType);
      values.put("CONTENT", ref);

      String foxml = replaceAll(FOXML, values);

      return pid2URI(apim.ingest(foxml.getBytes("UTF-8"), "foxml1.0", "created"));
    } catch (java.io.UnsupportedEncodingException e) {
      throw new Error(e);
    } catch (java.io.IOException e) {
      throw new RemoteException("Upload failed", e);
    }
  }

  /**
   * @see org.topazproject.ws.annotation.Annotation#deleteAnnotation
   */
  public void deleteAnnotation(String id, boolean deletePreceding)
                        throws NoSuchIdException, RemoteException {
    checkAccess(PEP.DELETE_ANNOTATION, validateUri(id, "annotation-id"));
    checkId(id);

    String[] purgeList = getFedoraObjects(id, deletePreceding);

    String[] preceding = deletePreceding ? getPrecedingAll(id) : new String[0];

    String   delete = DELETE_ITQL.replaceAll("\\$id", id);

    String   txn = "delete " + id;

    try {
      itql.beginTxn(txn);
      itql.doUpdate(delete);

      if (log.isDebugEnabled())
        log.debug("deleted " + id);

      for (int i = 0; i < preceding.length; i++) {
        delete = DELETE_ITQL.replaceAll("\\$id", preceding[i]);
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

    // xxx : txn?
    try {
      for (int i = 0; i < purgeList.length; i++) {
        if (log.isDebugEnabled())
          log.debug("purging " + purgeList[i]);

        // xxx: error says "fedora does not support forced removal yet".  
        // But don't suppose this really is a forced removal. So try with false. 
        //apim.purgeObject(purgeList[i], "deleted", true);
        apim.purgeObject(purgeList[i], "deleted", false);
      }

      if (log.isDebugEnabled())
        log.debug("purged " + purgeList.length + " fedora objects");
    } catch (Throwable t) {
      // No point reporting this back to the caller since the Annotation is deleted.
      // Admin needs to manually purge these later
      log.error("Failed to purge one or more fedora pids in " + Arrays.asList(purgeList), t);
    }
  }

  /**
   * @see org.topazproject.ws.annotation.Annotation#getAnnotation
   */
  public String getAnnotation(String id) throws NoSuchIdException, RemoteException {
    checkAccess(PEP.GET_ANNOTATION_INFO, validateUri(id, "annotation-id"));

    try {
      String query = GET_ITQL.replaceAll("\\$id", id);

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
  public String[] listAnnotations(String annotates, String type, boolean idsOnly)
                           throws RemoteException {
    checkAccess(PEP.LIST_ANNOTATIONS, validateUri(annotates, "annotates"));

    try {
      if (type == null)
        type = "a:Annotation";
      else
        validateUri(type, "type");

      String subquery = idsOnly ? "" : SUBQUERY;

      Map    values = new HashMap();
      values.put("subquery", subquery);
      values.put("annotates", annotates);
      values.put("type", type);

      String query = replaceAll(LIST_ITQL, values);

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
  public String[] getLatestAnnotations(String id, boolean idsOnly)
                                throws NoSuchIdException, RemoteException {
    checkAccess(PEP.GET_ANNOTATION_INFO, validateUri(id, "annotation-id"));

    try {
      String subquery = idsOnly ? "" : SUBQUERY;

      String query = LATEST_ITQL.replaceAll("\\$subquery", subquery).replaceAll("\\$id", id);

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
  public String[] getPrecedingAnnotations(String id, boolean idsOnly)
                                   throws NoSuchIdException, RemoteException {
    checkAccess(PEP.GET_ANNOTATION_INFO, validateUri(id, "annotation-id"));
    checkId(id);

    try {
      String subquery = idsOnly ? "" : SUBQUERY;

      String query = PRECEDING_ITQL.replaceAll("\\$subquery", subquery).replaceAll("\\$id", id);

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
    checkAccess(PEP.SET_ANNOTATION_STATE, validateUri(id, "annotation-id"));
    checkId(id);

    String set = SET_STATE_ITQL.replaceAll("\\$id", id).replaceAll("\\$state", "" + state);

    itql.doUpdate(set);
  }

  /**
   * @see org.topazproject.ws.annotation.Annotation#listAnnotations
   */
  public String[] listAnnotations(int state) throws RemoteException {
    checkAccess(PEP.LIST_ANNOTATIONS_IN_STATE, URI.create("" + state));

    try {
      String query = LIST_STATE_ITQL.replaceAll("\\$state", "" + state);

      Answer ans  = new Answer(itql.doQuery(query));
      List   rows = ((Answer.QueryAnswer) ans.getAnswers().get(0)).getRows();

      return buildAnnotationInfoList(rows);
    } catch (AnswerException e) {
      throw new RemoteException("Error querying RDF", e);
    }
  }

  private void checkId(String id) throws RemoteException, NoSuchIdException {
    try {
      String query = CHECK_ID_ITQL.replaceAll("\\$id", id);
      Answer ans  = new Answer(itql.doQuery(query));
      List   rows = ((Answer.QueryAnswer) ans.getAnswers().get(0)).getRows();

      if (rows.size() == 0)
        throw new NoSuchIdException(id);
    } catch (AnswerException e) {
      throw new RemoteException("Error querying RDF", e);
    }
  }

  private Set checkAccess(String action, URI resource) {
    try {
      if (log.isTraceEnabled())
        log.trace("checkAccess(" + action + ", " + resource + ")");

      Set s = pep.checkAccess(action, resource);

      if (log.isDebugEnabled())
        log.debug("allowed access to " + action + "(" + resource + ")");

      return s;
    } catch (SecurityException e) {
      if (log.isDebugEnabled())
        log.debug("denied access to " + action + "(" + resource + ")", e);

      throw e;
    }
  }

  private String buildAnnotationInfo(String id, List rows) {
    StringBuffer buf = new StringBuffer(512);

    buf.append("<annotation xmlns:a=\"").append(a.toString()).append("\" xmlns:d=\"")
        .append(d.toString()).append("\" xmlns:r=\"").append(r.toString()).append("\" id=\"")
        .append(id).append("\">");

    for (Iterator it = rows.iterator(); it.hasNext();) {
      Object[] cols      = (Object[]) it.next();
      URI      predicate = ((URIReference) (cols[0])).getURI();
      String   name      = null;
      String   attr      = null;

      if (a.getPath().equals(predicate.getPath()) && a.getHost().equals(predicate.getHost())
           && a.getScheme().equals(predicate.getScheme()))
        name = "a:" + predicate.getFragment();
      else if (r.getPath().equals(predicate.getPath()) && r.getHost().equals(predicate.getHost())
                && r.getScheme().equals(predicate.getScheme()))
        name = "r:" + predicate.getFragment();
      else if (d.getHost().equals(predicate.getHost())
                && d.getScheme().equals(predicate.getScheme())
                && predicate.getPath().startsWith(d.getPath()))
        name = "d:" + predicate.getPath().substring(d.getPath().length());
      else {
        attr   = predicate.toString();
        name   = "predicate";
      }

      Object o     = cols[1];
      String value;

      if (!(o instanceof URIReference)) {
        if (o instanceof Literal)
          value = ((Literal) o).getLexicalForm();
        else
          value = o.toString();
      } else {
        URI v = ((URIReference) o).getURI();

        if (nil.equals((Object) v))
          continue;

        if ("r:type".equals(name) && a_Annotation.equals((Object) v))
          continue;

        value = v.toString();

        if ("a:body".equals(name))
          value = getBodyURL(value);
      }

      if (attr == null)
        buf.append("<").append(name).append(">");
      else
        buf.append("<").append(name).append(" uri=\"").append(attr).append("\">");

      buf.append(value);
      buf.append("</").append(name).append(">");
    }

    buf.append("</annotation>");

    return buf.toString();
  }

  private String[] buildAnnotationInfoList(List rows) {
    String[] annotations = new String[rows.size()];

    for (int i = 0; i < annotations.length; i++) {
      Object[]     cols = (Object[]) rows.get(i);
      URIReference ref = (URIReference) cols[0];
      String       id  = ref.getURI().toString();

      if (cols.length < 2)
        annotations[i] = id;
      else {
        Answer.QueryAnswer subQueryAnswer = (Answer.QueryAnswer) cols[1];
        annotations[i] = buildAnnotationInfo(id, subQueryAnswer.getRows());
      }
    }

    return annotations;
  }

  private String[] getFedoraObjects(String id, boolean preceding)
                             throws RemoteException {
    String query = preceding ? FEDORA_LIST_ITQL : FEDORA_ID_ITQL;
    query = query.replaceAll("\\$id", id);

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
          pids.add(uri2PID(uri));
      }

      return (String[]) pids.toArray(new String[0]);
    } catch (AnswerException e) {
      throw new RemoteException("Error querying RDF", e);
    }
  }

  private String[] getPrecedingAll(String id) throws RemoteException {
    try {
      String query = PRECEDING_ALL_ITQL.replaceAll("\\$id", id);

      Answer ans = new Answer(itql.doQuery(query));

      List   rows = ((Answer.QueryAnswer) ans.getAnswers().get(0)).getRows();

      return buildAnnotationInfoList(rows);
    } catch (AnswerException e) {
      throw new RemoteException("Error querying RDF", e);
    }
  }

  private static String pid2URI(String pid) {
    return "info:fedora/" + pid;
  }

  private static String uri2PID(String uri) {
    return uri.substring(12);
  }

  private String getBodyURL(String uri) {
    if (!uri.startsWith("info:fedora"))
      return uri;

    String path = "/fedora/get/" + uri2PID(uri) + "/BODY";

    return fedoraServer.resolve(path).toString();
  }

  private String getUTCTime() {
    Calendar cal    = Calendar.getInstance();
    TimeZone tz     = cal.getTimeZone();
    Date     myDate = cal.getTime();

    long     rawOffset = tz.getRawOffset();
    long     myTime    = myDate.getTime();
    long     utcTime   = myTime - rawOffset;

    Date     utcDate = new Date(utcTime);

    return XSD_DATE_TIME_FMT.format(utcDate) + "Z";
  }

  private URI validateUri(String uri, String name) {
    if (uri == null)
      throw new NullPointerException("'" + name + "' cannot be null");

    try {
      URI u = new URI(uri);

      if (!u.isAbsolute())
        throw new URISyntaxException(uri, "missing scheme component", 0);

      return u;
    } catch (URISyntaxException e) {
      IllegalArgumentException iae =
        new IllegalArgumentException("'" + name + "' must be a valid absolute URI");
      iae.initCause(e);
      throw iae;
    }
  }

  private String replaceAll(String fmt, Map values) {
    Pattern      p   = Pattern.compile("\\$(\\w*)");
    Matcher      m   = p.matcher(fmt);
    StringBuffer sb  = new StringBuffer(fmt.length() * 2);
    int          pos = 0;

    while (m.find()) {
      int    ts    = m.start();
      int    te    = m.end();
      String token = fmt.substring(ts + 1, te);
      String val   = (String) values.get(token);

      if (val != null) {
        sb.append(fmt.substring(pos, ts));
        sb.append(val);
        pos = te;
      }
    }

    sb.append(fmt.substring(pos));

    return sb.toString();
  }

  private String escapeLiteral(String val) {
    return val.replaceAll("\\\\", "\\\\\\\\").replaceAll("'", "\\\\'");
  }
}
