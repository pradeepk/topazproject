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

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.plos.configuration.ConfigurationStore;

public class ArticleType {
  private static HashMap<String, ArticleType> _knownArticleTypes = new HashMap<String, ArticleType>();
  private static List<ArticleType> _articleTypeOrder = new ArrayList<ArticleType>();
  private static HashMap<String, ArticleType> _newArticleTypes = new HashMap<String, ArticleType>();
  private static ArticleType theDefaultArticleType = null;
  static {
  	configureArticleTypes(ConfigurationStore.getInstance().getConfiguration());
  }
  
  private URI uri;
  private String heading;
	private boolean defaultArticleType = false;
  
  private ArticleType(URI articleTypeUri, String displayHeading) {
    uri = articleTypeUri;
    heading = displayHeading;
  }
  
  public static ArticleType getTypeForURI(URI uri) {
    ArticleType at = _knownArticleTypes.get(uri.toString());
    if (at == null) {
      at = _newArticleTypes.get(uri.toString());
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
          _newArticleTypes.put(uri.toString(), at);
        }
      }
    }
    return at;
  }
  
  public static boolean addArticleType(URI uri, String heading) {
    if (_knownArticleTypes.containsKey(uri.toString())) {
      return false;
    }
    ArticleType at = new ArticleType(uri, heading);
    _knownArticleTypes.put(uri.toString(), at);
    _articleTypeOrder.add(at);
    return true;
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
	 */
	public static void configureArticleTypes(Configuration myConfig) {
    int count = 0;
    String basePath = "pub.articleTypeList.articleType";
    String uriStr;
    String headingStr;
    String isDefault;
    do {
    	StringBuffer baseIndex = new StringBuffer(basePath).append("(").append(count).append(").");
    	uriStr = myConfig.getString(baseIndex.toString() + "typeUri");
    	headingStr = myConfig.getString(baseIndex.toString() + "typeHeading");
    	isDefault = myConfig.getString(baseIndex.toString() + "default");
    	if ((uriStr != null) && (headingStr != null)) {
    		addArticleType(URI.create(uriStr), headingStr);
    		if ("true".equalsIgnoreCase(isDefault)) {
    			theDefaultArticleType = getTypeForURI(URI.create(uriStr));
    		}
    	}
    	count++;
    } while (uriStr != null);
	}
	
	public static ArticleType getDefaultArticleType() {
		return theDefaultArticleType;
	}
}
