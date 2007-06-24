/* $HeadURL::                                                                            $
 * $Id$
 */
package org.plos.article.service;

import com.opensymphony.oscache.general.GeneralCacheAdministrator;

import com.sun.org.apache.xpath.internal.XPathAPI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.plos.ApplicationException;
import org.plos.annotation.service.AnnotationInfo;
import org.plos.annotation.service.AnnotationWebService;
import org.plos.annotation.service.Annotator;
import org.plos.article.util.NoSuchArticleIdException;
import org.plos.article.util.NoSuchObjectIdException;
import org.plos.models.ObjectInfo;
import org.plos.util.CacheAdminHelper;
import org.plos.util.FileUtils;
import org.plos.util.TextUtils;
import org.plos.util.ArticleXMLUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.activation.DataHandler;
import javax.activation.URLDataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;

import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;


/**
 * Fetch article service
 */
public class FetchArticleService {

  private String encodingCharset;
  private ArticleXMLUtils articleXmlUtils;

  private static final Log log = LogFactory.getLog(FetchArticleService.class);
  private AnnotationWebService annotationWebService;

  private GeneralCacheAdministrator articleCacheAdministrator;

  private static final String CACHE_KEY_ARTICLE_INFO = "CACHE_KEY_ARTICLE_INFO";

  private String getTransformedArticle(final String articleURI) throws ApplicationException {
    try {
      return articleXmlUtils.getTransformedDocument (getAnnotatedContentAsDocument(articleURI));
    } catch (Exception e) {
      if (log.isErrorEnabled()) {
        log.error ("Could not transform article: " + articleURI, e);
      }
      if (e instanceof ApplicationException) {
        throw (ApplicationException)e;
      } else {
        throw new ApplicationException (e);
      }
    }
  }

  /**
   * Get the URI transformed as HTML.
   * @param articleURI articleURI
   * @return String representing the annotated article as HTML
   * @throws org.plos.ApplicationException ApplicationException
   * @throws java.rmi.RemoteException RemoteException
   * @throws NoSuchArticleIdException NoSuchArticleIdException
   */
  public String getURIAsHTML(final String articleURI) throws ApplicationException,
                          RemoteException, NoSuchArticleIdException {
    String escapedURI = FileUtils.escapeURIAsPath(articleURI);

    Object res = CacheAdminHelper.getFromCache(articleCacheAdministrator,
                                               articleURI/* + topazUserId*/, -1,
                                               new String[] { escapedURI }, "transformed article",
                                               new CacheAdminHelper.CacheUpdater<Object>() {
        public Object lookup(boolean[] updated) {
          try {
            String a = getTransformedArticle(articleURI);
            updated[0] = true;
            return a;
          } catch (Exception e) {
            return e;
          }
        }
      }
    );

    if (res instanceof ApplicationException)
      throw (ApplicationException) res;
    if (res instanceof NoSuchArticleIdException)
      throw (NoSuchArticleIdException) res;
    if (res instanceof RemoteException)
      throw (RemoteException) res;
    if (res instanceof RuntimeException)
      throw (RuntimeException) res;
    return (String) res;
  }

  /**
   * Return the annotated content as a String
   * @param articleURI articleURI
   * @return an the annotated content as a String
   * @throws ParserConfigurationException ParserConfigurationException
   * @throws SAXException SAXException
   * @throws IOException IOException
   * @throws URISyntaxException URISyntaxException
   * @throws org.plos.ApplicationException ApplicationException
   * @throws NoSuchArticleIdException NoSuchArticleIdException
   * @throws javax.xml.transform.TransformerException TransformerException
   */
  public String getAnnotatedContent(final String articleURI) throws ParserConfigurationException,
                                    SAXException, IOException, URISyntaxException,
                                    ApplicationException, NoSuchArticleIdException,TransformerException{
    return TextUtils.getAsXMLString(getAnnotatedContentAsDocument(articleURI));
  }

  /**
   * Get the xmlFileURL as a DOMSource.
   * @param xmlFileURL xmlFileURL
   * @return an instance of DOMSource
   * @throws ParserConfigurationException ParserConfigurationException
   * @throws SAXException SAXException
   * @throws IOException IOException
   * @throws URISyntaxException URISyntaxException
   * @throws org.plos.ApplicationException ApplicationException
   * @throws NoSuchArticleIdException NoSuchArticleIdException
   */
  public Source getDOMSource(final String xmlFileURL) throws ParserConfigurationException, SAXException, IOException, URISyntaxException, ApplicationException, NoSuchArticleIdException {
    final DocumentBuilder builder = articleXmlUtils.createDocBuilder();

    Document doc;
    try {
      doc = builder.parse(articleXmlUtils.getAsFile(xmlFileURL));
    } catch (Exception e) {
      doc = builder.parse(xmlFileURL);
    }

    // Prepare the DOM source
    return new DOMSource(doc);
  }

  private Document getAnnotatedContentAsDocument(final String infoUri) throws IOException,
          NoSuchArticleIdException, ParserConfigurationException, SAXException, ApplicationException {

    final String contentUrl;
    try {
      contentUrl = articleXmlUtils.getArticleService().getObjectURL(infoUri, articleXmlUtils.getArticleRep());
    } catch (NoSuchObjectIdException ex) {
      throw new NoSuchArticleIdException(infoUri, "(representation=" + articleXmlUtils.getArticleRep() + ")", ex);
    }

    return getAnnotatedContentAsDocument(contentUrl, infoUri);
  }

  private Document getAnnotatedContentAsDocument(final String contentUrl, final String infoUri)
          throws IOException, ParserConfigurationException, ApplicationException {
    final AnnotationInfo[] annotations = annotationWebService.listAnnotations(infoUri);
    return applyAnnotationsOnContentAsDocument (contentUrl, annotations);
  }

  private Document applyAnnotationsOnContentAsDocument (final String contentUrl,
                                                        final AnnotationInfo[] annotations)
          throws IOException, ParserConfigurationException, ApplicationException {
    final DataHandler content = new DataHandler(new URLDataSource(new URL(contentUrl)));
    final DocumentBuilder builder = articleXmlUtils.createDocBuilder();
    if (annotations.length != 0) {
      return Annotator.annotateAsDocument(content, annotations, builder);
    }
    try {
      return builder.parse(content.getInputStream());
    } catch (Exception e){
      if (log.isErrorEnabled()) {
        log.error("Could not apply annotations to article: " + contentUrl, e);
      }
      throw new ApplicationException("Applying annotations failed for resource:" + contentUrl, e);
    }
  }

  /**
   * Getter for AnnotatationWebService
   * 
   * @return the annotationWebService
   */
  public AnnotationWebService getAnnotationWebService() {
    return annotationWebService;
  }

  /**
   * Setter for annotationWebService
   * 
   * @param annotationWebService annotationWebService
   */
  public void setAnnotationWebService(final AnnotationWebService annotationWebService) {
    this.annotationWebService = annotationWebService;
  }

  /**
   * Get a list of all articles
   * @param startDate startDate
   * @param endDate endDate
   * @param state  array of matching state values
   * @return list of article uri's
   * @throws ApplicationException ApplicationException
   */
  public ArrayList<String> getArticles(final String startDate, final String endDate, final int[] state) throws ApplicationException {
    final ArrayList<String> articles = new ArrayList<String>();

    try {
      final String articlesDoc = articleXmlUtils.getArticleService().getArticles(startDate, endDate, state, true);

      // Create the builder and parse the file
      final Document articleDom = articleXmlUtils.getFactory().newDocumentBuilder().parse(new InputSource(new StringReader(articlesDoc)));

      // Get the matching elements
      final NodeList nodelist = XPathAPI.selectNodeList(articleDom, "/articles/article/uri");

      for (int i = 0; i < nodelist.getLength(); i++) {
        final Element elem = (Element) nodelist.item(i);
        final String uri = elem.getTextContent();
        final String decodedArticleUri = URLDecoder.decode(uri, encodingCharset);
        articles.add(decodedArticleUri);
      }

      return articles;
    } catch (Exception e) {
      throw new ApplicationException(e);
    }
  }

  /**
   * Get a list of all articles
   * @param startDate startDate
   * @param endDate endDate
   * @return list of article uri's
   * @throws ApplicationException ApplicationException
   */
  public Collection<String> getArticles(final String startDate, final String endDate) throws ApplicationException {
    return getArticles(startDate, endDate, null);
  }

  /**
   * Set the encoding charset
   * @param encodingCharset encodingCharset
   */
  public void setEncodingCharset(final String encodingCharset) {
    this.encodingCharset = encodingCharset;
  }

  /**
   * @return Returns the articleCacheAdministrator.
   */
  public GeneralCacheAdministrator getArticleCacheAdministrator() {
    return articleCacheAdministrator;
  }

  /**
   * @param articleCacheAdministrator The articleCacheAdministrator to set.
   */
  public void setArticleCacheAdministrator(GeneralCacheAdministrator articleCacheAdministrator) {
    this.articleCacheAdministrator = articleCacheAdministrator;
  }

  /**
   * @param articleXmlUtils The articleXmlUtils to set.
   */
  public void setArticleXmlUtils(ArticleXMLUtils articleXmlUtils) {
    this.articleXmlUtils = articleXmlUtils;
  }

  /**
   * @see ArticleOtmService#getObjectInfo(String)
   * @param articleURI articleURI
   * @return ObjectInfo
   * @throws ApplicationException ApplicationException
   */
  public ObjectInfo getArticleInfo(final String articleURI) throws ApplicationException {
    // do caching here rather than at articleOtmService level because we want the cache key
    // and group to be article specific
    ObjectInfo artInfo = CacheAdminHelper.getFromCache(articleCacheAdministrator,
                                             articleURI + CACHE_KEY_ARTICLE_INFO, -1,
                                             new String[] { FileUtils.escapeURIAsPath(articleURI) },
                                             "objectInfo",
                                             new CacheAdminHelper.CacheUpdater<ObjectInfo>() {
        public ObjectInfo lookup(boolean[] updated) {
          try {
            ObjectInfo artInfo = articleXmlUtils.getArticleService().getObjectInfo(articleURI);
            updated[0] = true;
            if (log.isDebugEnabled()) {
              log.debug("retrieved objectInfo from TOPAZ for article URI: " + articleURI);
            }
            return artInfo;
          } catch (NoSuchObjectIdException nsoie) {
            if (log.isErrorEnabled()) {
              log.error("Failed to get object info for article URI: " + articleURI, nsoie);
            }
            return null;
          }
        }
      }
    );

    if (artInfo == null)
      throw new ApplicationException("Failed to get object info " + articleURI);

    return artInfo;
  }
}
