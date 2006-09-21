package org.plos;

import org.w3c.dom.Document;

/**
 * Placeholder for Article class
 * 
 * @author Stephen Cheng
 * 
 */
public class Article {
  private int version;

  private String doi;

  private Document xmlDoc;

  public Article() {

  }

  /**
   * Retrieves the article identified by the <code>doi</code> param from the store
   * 
   * @param doi
   * @param signOnId
   * @param authToken
   * @return <code>Article</code> given by <code>doi</code> param
   */
  public static Article getArticle(String doi, String signOnId, String authToken) {
    return new Article();
  }

  /**
   * Returns all articles in the store as an array.
   * 
   * @param signOnId
   * @param authToken
   * @return <code>Article[]</code>
   */
  public static Article[] getAllArticles(String signOnId, String authToken) {
    return new Article[0];
  }

  /**
   * @return Returns the doi.
   */
  public String getDoi() {
    return doi;
  }

  /**
   * @param doi
   *          The doi to set.
   */
  public void setDoi(String doi) {
    this.doi = doi;
  }

  /**
   * @return Returns the version.
   */
  public int getVersion() {
    return version;
  }

  /**
   * @param version
   *          The version to set.
   */
  public void setVersion(int version) {
    this.version = version;
  }

  /**
   * @return Returns the xmlDoc.
   */
  public Document getXmlDoc() {
    return xmlDoc;
  }

  /**
   * @param xmlDoc
   *          The xmlDoc to set.
   */
  public void setXmlDoc(Document xmlDoc) {
    this.xmlDoc = xmlDoc;
  }

}