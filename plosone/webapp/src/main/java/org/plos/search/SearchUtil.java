/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.search;

import org.plos.search.service.SearchHit;
import org.plos.util.DateParser;
import org.plos.util.InvalidDateException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Util functions to be used for Flag related tasks like created and extracting flag attributes.
 */
public class SearchUtil {
  private static final String charsetEncoding = "UTF-8";
  private static final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

  /**
   * Return a collection of SearchHit's
   * @param searchResultXml searchResultXml
   * @return a Collection<SearchHit>
   * @throws IOException IOException
   * @throws ParserConfigurationException ParserConfigurationException
   * @throws SAXException SAXException
   * @throws org.plos.util.InvalidDateException InvalidDateException
   */
  public static Collection<SearchHit> convertSearchResultXml(final String searchResultXml) throws IOException, ParserConfigurationException, SAXException, InvalidDateException {
    final String nodeName = "hit";
    final NodeList hitList = getNodeList(searchResultXml, nodeName);
    final int noOfHits = hitList.getLength();

    final Collection<SearchHit> hits = new ArrayList<SearchHit>();
    for (int i = 0; i < noOfHits; i++) {
      final Node hitNode = hitList.item(i);
      final SearchHit searchHit = convertToSearchHit((Element) hitNode);
      hits.add(searchHit);
    }
    return hits;
  }

  private static Element getRootNode(final String xmlDocument) throws SAXException, IOException, ParserConfigurationException {
    final Document doc = documentBuilderFactory.newDocumentBuilder()
                            .parse(new ByteArrayInputStream(xmlDocument.getBytes(charsetEncoding)));
    return doc.getDocumentElement();
  }

  private static NodeList getNodeList(final String searchResultXml, final String nodeName) throws SAXException, IOException, ParserConfigurationException {
    final Element rootElement = getRootNode(searchResultXml);
    return rootElement.getElementsByTagName(nodeName);
  }

  private static SearchHit convertToSearchHit(final Element hitNode) throws InvalidDateException {
    final NamedNodeMap hitNodeMap = hitNode.getAttributes();
    final String hitNumber = hitNodeMap.getNamedItem("no").getTextContent();
    final String hitScore = hitNodeMap.getNamedItem("score").getTextContent();

    final Map<String, String> map = getFieldNodeNameAttributeValueMap(hitNode);

    final String pid = map.get("PID");
    final String type = map.get("property.type");
    final String state = map.get("property.state");
    final Date createdDate = DateParser.parse(map.get("property.createdDate"));
    final Date lastModifiedDate = DateParser.parse(map.get("property.lastModifiedDate"));
    final String contentModel = map.get("property.contentModel");
    final String description = map.get("dc.description");
    final String publisher = map.get("dc.publisher");
    final String repositoryName = map.get("repositoryName");
    return new SearchHit(hitNumber, hitScore, pid, type, state, createdDate, lastModifiedDate, contentModel, description, publisher, repositoryName);
  }

  private static Map<String, String> getFieldNodeNameAttributeValueMap(final Element hitNode) {
    final NodeList fieldNodes = hitNode.getElementsByTagName("field");

    final int noOfFields = fieldNodes.getLength();
    final Map<String, String> map = new HashMap<String, String>(noOfFields);
    for (int i = 0; i < noOfFields; i++) {
      final Node node = fieldNodes.item(i);
      final NamedNodeMap attributes = node.getAttributes();
      final Node item = attributes.getNamedItem("name");
      final String key = item.getTextContent();
      final String value = node.getTextContent();
      map.put(key, value);
    }
    return map;
  }

}
