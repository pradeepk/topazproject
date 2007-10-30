package org.plos.model.article;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.plos.models.PLoS;

public class ArticleType {
  private static HashMap<URI, ArticleType> _knownArticleTypes = new HashMap<URI, ArticleType>();
  private static HashMap<URI, ArticleType> _newArticleTypes = new HashMap<URI, ArticleType>();
  static {
    addArticleType(URI.create(PLoS.PLOS_ArticleType + "research-article"), "Research Article");
    addArticleType(URI.create(PLoS.PLOS_ArticleType + "editorial"), "Editorial");
    addArticleType(URI.create(PLoS.PLOS_ArticleType + "correction"), "Correction");
    addArticleType(URI.create(PLoS.PLOS_ArticleType + "article-commentary"), "Expert Commentary");
  }
  
  private URI uri;
  private String heading;
  
  private ArticleType(URI articleTypeUri, String displayHeading) {
    uri = articleTypeUri;
    heading = displayHeading;
  }
  
  public static ArticleType getTypeForURI(URI uri) {
    ArticleType at = _knownArticleTypes.get(uri);
    if (at == null) {
      at = _newArticleTypes.get(uri);
      if ((at == null) && (uri != null)) {
        String uriStr = uri.toString();
        if (uriStr.contains("/")) {
          uriStr = uriStr.substring(uriStr.indexOf('/'));
          try {
            uriStr = URLDecoder.decode(uriStr, "UTF-8");
          } catch (UnsupportedEncodingException e) {
            // ignore and just use encoded uriStr :(
          }
          at = new ArticleType(uri, uriStr);
        }
      }
    }
    return at;
  }
  
  public static boolean addArticleType(URI uri, String heading) {
    if (_knownArticleTypes.containsKey(uri)) {
      return false;
    }
    ArticleType at = new ArticleType(uri, heading);
    _knownArticleTypes.put(uri, at);
    return true;
  }

  public URI getUri() {
    return uri;
  }

  public String getHeading() {
    return heading;
  }
  
  /**
   * Returns an ordered list of ArticleTypes that should be displayed by the JPS. 
   * @return
   */
  public static Collection<ArticleType> getOrderedListForDisplay() {
    // TODO - need to define a list of ArticleTypes that should be 
    // displayed in the JPS. These should be ordered. This could ultimately
    // be defined in mulgara. For now, just return all known ArticleTypes in
    // a list. 
    return new ArrayList<ArticleType>(_knownArticleTypes.values());
  }
}
