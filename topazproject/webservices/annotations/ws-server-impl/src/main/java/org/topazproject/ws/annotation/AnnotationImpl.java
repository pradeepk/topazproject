package org.topazproject.ws.annotation;

import java.net.MalformedURLException;
import java.net.URI;

import java.rmi.RemoteException;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.jrdf.graph.Literal;
import org.jrdf.graph.URIReference;

import org.topazproject.mulgara.itql.Answer;
import org.topazproject.mulgara.itql.AnswerException;
import org.topazproject.mulgara.itql.ItqlHelper;

/**
 * The implementation of the annotation service.
 */
public class AnnotationImpl implements Annotation {
  private static final Log         log              = LogFactory.getLog(AnnotationImpl.class);
  private static final IdGenerator idGenerator      = new IdGenerator();
  private static final Map         aliases          = ItqlHelper.getDefaultAliases();
  private static final String      MODEL            = "<rmi://localhost/fedora#ri>";
  private static final String      ANNOTATIONS_BASE = "rmi://localhost/fedora#an.";
  private static final String      ANNOTATES        = " <a:annotates> ";
  private static final String      IN_STATE         = " <topaz:isInState> ";
  private static final String      XXX_VALUE        = " <topaz:hasValue> "; // xxx: temporary
  private PEP                      pep;
  private ItqlHelper               itql;

  static {
    aliases.put("a", "http://www.w3.org/2000/10/annotation-ns#");
  }

  /**
   * Creates a new AnnotationImpl object.
   *
   * @param itql The itql service
   * @param pep The xacml pep
   */
  public AnnotationImpl(ItqlHelper itql, PEP pep) {
    this.itql     = itql;
    this.pep      = pep;

    itql.setAliases(aliases);
  }

  /**
   * @see org.topazproject.ws.annotation.Annotation#createAnnotation
   */
  public String createAnnotation(String on, String annotationInfo)
                          throws RemoteException {
    checkAccess(PEP.CREATE_ANNOTATION, on);

    String seq = idGenerator.nextId();
    String id = ANNOTATIONS_BASE + seq;

    String annotation = "<" + id + ">";
    String annotates  = "<" + on + ">";
    String value      = "'" + annotationInfo + "'"; // xxx: encode?

    itql.doUpdate("insert " + annotation + ANNOTATES + annotates + " " + annotation + IN_STATE
                  + " '0' " + annotation + XXX_VALUE + value + " into" + MODEL + ";");

    return id;
  }

  /**
   * @see org.topazproject.ws.annotation.Annotation#deleteAnnotation
   */
  public void deleteAnnotation(String id) throws NoSuchIdException, RemoteException {
    checkAccess(PEP.DELETE_ANNOTATION, id);
    checkId(id);

    String annotation = "<" + id + ">";

    itql.doUpdate("delete select " + annotation + " $p $o from " + MODEL + " where " + annotation
                  + " $p $o from " + MODEL + ";");
  }

  /**
   * @see org.topazproject.ws.annotation.Annotation#setAnnotationInfo
   */
  public void setAnnotationInfo(String id, String annotationDef)
                         throws NoSuchIdException, RemoteException {
    checkAccess(PEP.SET_ANNOTATION_INFO, id);
    checkId(id);

    String annotation = "<" + id + ">";
    String value = "'" + annotationDef + "'"; // xxx: encode?

    itql.doUpdate("delete select " + annotation + XXX_VALUE + "$o from " + MODEL + " where "
                  + annotation + XXX_VALUE + " $o from " + MODEL + "; insert " + annotation
                  + XXX_VALUE + value + " into " + MODEL + ";");
  }

  /**
   * @see org.topazproject.ws.annotation.Annotation#getAnnotationInfo
   */
  public String getAnnotationInfo(String id) throws NoSuchIdException, RemoteException {
    checkAccess(PEP.GET_ANNOTATION_INFO, id);

    try {
      String annotation = "<" + id + ">";

      Answer ans =
        new Answer(itql.doQuery("select $o from " + MODEL + " where " + annotation + XXX_VALUE
                                + "$o;"));
      List rows = ((Answer.QueryAnswer) ans.getAnswers().get(0)).getRows();

      if (rows.size() == 0)
        throw new NoSuchIdException(id);

      String s = ((Literal) ((Object[]) rows.get(0))[0]).toString();

      return s.substring(1, s.length() - 1);// xxx: decode?
    } catch (AnswerException e) {
      throw new RemoteException("Error querying RDF", e);
    }
  }

  /**
   * @see org.topazproject.ws.annotation.Annotation#listAnnotations
   */
  public String[] listAnnotations(String on) throws RemoteException {
    checkAccess(PEP.LIST_ANNOTATIONS, on);

    try {
      String annotates = "<" + on + ">";
      
      Answer ans =
        new Answer(itql.doQuery("select $a from " + MODEL + " where $a " + ANNOTATES + annotates
                                + "and $a" + IN_STATE + "'0';"));
      
      List     rows = ((Answer.QueryAnswer) ans.getAnswers().get(0)).getRows();
      
      String[] ids = new String[rows.size()];

      for (int i = 0; i < ids.length; i++) {
        URIReference ref = (URIReference) ((Object[]) rows.get(i))[0];
        ids[i] = ref.getURI().toString();
      }

      return ids;
    } catch (AnswerException e) {
      throw new RemoteException("Error querying RDF", e);
    }
  }

  /**
   * @see org.topazproject.ws.annotation.Annotation#setAnnotationState
   */
  public void setAnnotationState(String id, int state)
                          throws RemoteException, NoSuchIdException {
    checkAccess(PEP.SET_ANNOTATION_STATE, id);

    String s          = "'" + state + "'";
    String annotation = "<" + id + ">";

    itql.doUpdate("delete select " + annotation + IN_STATE + "$o from " + MODEL + " where "
                  + annotation + IN_STATE + "$o from " + MODEL + "; insert " + annotation
                  + IN_STATE + s + " into " + MODEL + ";");
  }

  /**
   * @see org.topazproject.ws.annotation.Annotation#listAnnotations
   */
  public String[] listAnnotations(int state) throws RemoteException {
    checkAccess(PEP.LIST_ANNOTATIONS_IN_STATE, "" + state);

    try {
      String   s = "'" + state + "'";

      Answer   ans =
        new Answer(itql.doQuery("select $a from " + MODEL + " where $a " + IN_STATE + s + ";"));
      List     rows = ((Answer.QueryAnswer) ans.getAnswers().get(0)).getRows();
      String[] ids  = new String[rows.size()];

      for (int i = 0; i < ids.length; i++) {
        URIReference ref = (URIReference) ((Object[]) rows.get(i))[0];
        ids[i] = ref.getURI().toString();
      }

      return ids;
    } catch (AnswerException e) {
      throw new RemoteException("Error querying RDF", e);
    }
  }

  private String checkId(String id) throws RemoteException, NoSuchIdException {
    try {
      String annotation = "<" + id + ">";
      Answer ans =
        new Answer(itql.doQuery("select $o from " + MODEL + " where " + annotation + IN_STATE
                                + "$o;"));
      List   rows = ((Answer.QueryAnswer) ans.getAnswers().get(0)).getRows();

      if (rows.size() == 0)
        throw new NoSuchIdException(id);

      return ((Literal) ((Object[]) rows.get(0))[0]).toString();
    } catch (AnswerException e) {
      throw new RemoteException("Error querying RDF", e);
    }
  }

  private Set checkAccess(String action, String resource) {
    try {
      if (log.isTraceEnabled())
        log.trace("checkAccess(" + action + ", " + resource + ")");

      Set s = pep.checkAccess(action, URI.create(resource));

      if (log.isDebugEnabled())
        log.debug("allowed access to " + action + "(" + resource + ")");

      return s;
    } catch (SecurityException e) {
      if (log.isDebugEnabled())
        log.debug("denied access to " + action + "(" + resource + ")", e);

      throw e;
    }
  }

  // xxx: temporary
  private static class IdGenerator {
    java.util.Random random = new java.util.Random(System.currentTimeMillis());

    String nextId() {
      return "" + System.currentTimeMillis() + "." + random.nextLong();
    }
  }
}
