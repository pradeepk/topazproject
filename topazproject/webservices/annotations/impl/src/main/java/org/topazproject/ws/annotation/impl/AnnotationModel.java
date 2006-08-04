package org.topazproject.ws.annotation.impl;

import java.net.URI;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.jrdf.graph.Literal;
import org.jrdf.graph.URIReference;

import org.topazproject.mulgara.itql.ItqlHelper;

import org.topazproject.ws.annotation.AnnotationInfo;

/**
 * Annotation meta-data.
 *
 * @author Pradeep Krishnan
 */
public class AnnotationModel extends AnnotationInfo {
  private static final Log log = LogFactory.getLog(AnnotationModel.class);

  //
  static final URI a            = URI.create("http://www.w3.org/2000/10/annotation-ns#");
  static final URI r            = URI.create(ItqlHelper.RDF_URI);
  static final URI d            = URI.create(ItqlHelper.DC_URI);
  static final URI nil          = URI.create(ItqlHelper.RDF_URI + "nil");
  static final URI a_Annotation = a.resolve("#Annotation");
  static final URI r_type       = r.resolve("#type");
  static final URI a_annotates  = a.resolve("#annotates");
  static final URI a_context    = a.resolve("#context");
  static final URI d_creator    = d.resolve("creator");
  static final URI a_created    = a.resolve("#created");
  static final URI a_body       = a.resolve("#body");
  static final URI a_supersedes = a.resolve("#supersedes");

  /**
   * Creates a new AnnotationModel object.
   *
   * @param id the annotation id;
   * @param map meta-data as a map of name value pairs
   */
  public AnnotationModel(String id, Map map) {
    setId(id);
    setType((String) map.get(r_type));
    setAnnotates((String) map.get(a_annotates));
    setContext((String) map.get(a_context));
    setCreator((String) map.get(d_creator));
    setCreated((String) map.get(a_created));
    setBody((String) map.get(a_body));
    setSupersedes((String) map.get(a_supersedes));
  }

  /**
   * Creates an AnnotationInfo object from an ITQL query result.
   *
   * @param id the annotation-id
   * @param rows a list of name value pairs (predicate, object)
   *
   * @return returns the newly created Annotation Info
   */
  public static AnnotationInfo create(String id, List rows) {
    Map map = new HashMap();

    for (Iterator it = rows.iterator(); it.hasNext();) {
      Object[] cols      = (Object[]) it.next();
      URI      predicate = ((URIReference) (cols[0])).getURI();

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

        if (r_type.equals(predicate) && a_Annotation.equals((Object) v))
          continue;

        value = v.toString();
      }

      String prev = (String) map.put(predicate, value);

      if (prev != null) {
        log.warn("Unexpected duplicate triple found. Ignoring <" + id + "> <" + predicate + "> <"
                 + prev + ">");
      }
    }

    return new AnnotationModel(id, map);
  }
}
