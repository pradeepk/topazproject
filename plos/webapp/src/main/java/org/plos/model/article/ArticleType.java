/* $HeadURL$
 * $Id$ 
 *
 * Copyright (c) 2006-2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.model.article;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.configuration.ConfigurationStore;

public class ArticleType implements Serializable {
  private static final Log log = LogFactory.getLog(ArticleType.class);
  
  private static HashMap<String, ArticleType> _knownArticleTypes = new HashMap<String, ArticleType>();
  private static List<ArticleType> _articleTypeOrder = new ArrayList<ArticleType>();
  private static HashMap<String, ArticleType> _newArticleTypes = new HashMap<String, ArticleType>();
  private static ArticleType theDefaultArticleType = null;
  static {
  	configureArticleTypes(ConfigurationStore.getInstance().getConfiguration());
  }
  
  private URI uri;
  private String heading;
  private String imageConfigName;
  
  private ArticleType(URI articleTypeUri, String displayHeading) {
    uri = articleTypeUri;
    heading = displayHeading;
  }
  
  /**
   * Returns an ArticleType if configured in defaults.xml (etc) or null otherwise
   * @param uri
   * @return
   */
  public static ArticleType getKnownArticleTypeForURI(URI uri) {
    return _knownArticleTypes.get(uri.toString());
  }
  
  /**
   * Returns an ArticleType object for the given URI. If one does not exist for that URI and
   * createIfAbsent is true, a new ArticleType shall be created and added to a list of types 
   * (although shall not be recognized as an official ArticleType by getKnownArticleTypeForURI). 
   * If createIfAbsent is false, an ArticleType shall not be created and null shall be returned. 
   * @param uri
   * @param createIfAbsent
   * @return The ArticleType for the given URI
   */
  public static ArticleType getArticleTypeForURI(URI uri, boolean createIfAbsent) {
    ArticleType at = _knownArticleTypes.get(uri.toString());
    if (at == null) {
      at = _newArticleTypes.get(uri.toString());
      if ((at == null) && (uri != null)  && createIfAbsent) {
        String uriStr = uri.toString();
        if (uriStr.contains("/")) {
          uriStr = uriStr.substring(uriStr.indexOf('/'));
          try {
            uriStr = URLDecoder.decode(uriStr, "UTF-8");
          } catch (UnsupportedEncodingException e) {
            // ignore and just use encoded uriStr :(
          }
          at = new ArticleType(uri, uriStr);
          _newArticleTypes.put(uri.toString(), at);
        }
      }
    }
    return at;
  }
  
  public static ArticleType addArticleType(URI uri, String heading) {
    if (_knownArticleTypes.containsKey(uri.toString())) {
      return _knownArticleTypes.get(uri.toString());
    }
    ArticleType at = new ArticleType(uri, heading);
    _knownArticleTypes.put(uri.toString(), at);
    _articleTypeOrder.add(at);
    return at;
  }

  public URI getUri() {
    return uri;
  }

  public String getHeading() {
    return heading;
  }
  
  /**
   * Returns an unmodifiable ordered list of known ArticleTypes as read in order from the configuration
   * in configureArticleTypes(). 
   * 
   * @return Collection of ArticleType(s)
   */
  public static List<ArticleType> getOrderedListForDisplay() {
    return Collections.unmodifiableList(_articleTypeOrder);
  }
  
  /**
	 * Read in the ArticleTypes from the pubApp configuration (hint: normally defined in defauls.xml) 
	 * and add them to the list of known ArticleType(s). The order of article types found in the 
	 * configuration is significant and is returned in a Collection from getOrderedListForDisplay(). 
	 * The defaultArticleType is set to the first article type defined unless configured explicitly. 
	 */
	public static void configureArticleTypes(Configuration myConfig) {
    int count = 0;
    String basePath = "pub.articleTypeList.articleType";
    String uriStr;
    String headingStr;
    // Iterate through the defined article types. This is ugly since the index needs 
    // to be given in xpath format to access the element, so we calculate a base string
    // like: pub.articleTypeList.articleType(x) and check if it's non-null for typeUri
    do {
      String baseString = (new StringBuffer(basePath).append("(").append(count)
          .append(").")).toString();
      uriStr = myConfig.getString(baseString + "typeUri");
      headingStr = myConfig.getString(baseString + "typeHeading");
      if ((uriStr != null) && (headingStr != null)) {
        ArticleType at = addArticleType(URI.create(uriStr), headingStr);
        if (("true".equalsIgnoreCase(myConfig.getString(baseString + "default"))) || 
            (theDefaultArticleType == null)) {
          theDefaultArticleType = at;
        }
        at.setImageSetConfigName(myConfig.getString(baseString + "imageSetConfigName"));
      }
    	count++;
    } while (uriStr != null);
	}

	public void setImageSetConfigName(String imgConfigName) {
	  this.imageConfigName = imgConfigName;
  }
	
	public String getImageSetConfigName() {
	  return imageConfigName;
	}

  public static ArticleType getDefaultArticleType() {
		return theDefaultArticleType;
	}

}
