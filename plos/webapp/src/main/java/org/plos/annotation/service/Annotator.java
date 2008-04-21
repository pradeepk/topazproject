/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2008 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.plos.annotation.service;

import it.unibo.cs.xpointer.Location;
import it.unibo.cs.xpointer.XPointerAPI;
import it.unibo.cs.xpointer.datatype.LocationList;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.topazproject.dom.ranges.SelectionRange;
import org.topazproject.dom.ranges.SelectionRangeList;
import org.plos.models.ArticleAnnotation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ranges.DocumentRange;
import org.w3c.dom.ranges.Range;
import org.xml.sax.SAXException;


/**
 * Creates an annotated version of the content.
 *
 * @author Pradeep Krishnan
 */
public class Annotator {
  private static final Log log    = LogFactory.getLog(Annotator.class);
  private static String    AML_NS = "http://topazproject.org/aml/";

  /**
   * Creates an annotated document as a DataHandler.
   *
   * @param content the source document
   * @param annotations the list of annotations to apply
   * @param documentBuilder documentBuilder that provides it's own entity resolver
   * @return the annotated document as DataHandler
   *
   * @throws RemoteException on a failure
   */
  public static DataHandler annotate(DataHandler content, ArticleAnnotation[] annotations,
                                    final DocumentBuilder documentBuilder)
                              throws RemoteException {
    try {
      return serialize(annotate(parse(content, documentBuilder), annotations));
    } catch (Exception e) {
      throw new RemoteException("", e);
    }
  }

  /**
   * Creates an annotated document and returns it as Document.
   *
   * @param content the source document
   * @param annotations the list of annotations to apply
   * @param documentBuilder documentBuilder that provides it's own entity resolver
   * @return the annotated document
   *
   * @throws RemoteException on a failure
   */
  public static Document annotateAsDocument(DataHandler content, ArticleAnnotation[] annotations,
                                            final DocumentBuilder documentBuilder)
                              throws RemoteException {
    try {
      return annotate(parse(content, documentBuilder), annotations);
    } catch (Exception e) {
      throw new RemoteException("", e);
    }
  }

  /**
   * Creates an annotated document.
   *
   * @param document the source document
   * @param annotations the list of annotations to apply
   *
   * @return the annotated document
   *
   * @throws URISyntaxException if at least one annotation context is an invalid URI
   * @throws TransformerException if at least one annotation context is an invalid xpointer
   *         expression
   */
  private static Document annotate(Document document, ArticleAnnotation[] annotations)
                           throws URISyntaxException, TransformerException {
    LocationList[] lists = evaluate(document, annotations);

    Regions        regions = new Regions(document);

    for (int i = 0; i < lists.length; i++) {
      if (lists[i] != null){
        regions.addRegion(lists[i], annotations[i]);
      }
    }
    regions.surroundContents(AML_NS, "aml:annotated", "aml:id", "aml:first");

    Element rRoot = regions.createElement(AML_NS, "aml:region", "aml:annotation", "aml:id");

    Element aRoot = document.createElementNS(AML_NS, "aml:annotations");
    AnnotationModel.appendNSAttr(aRoot);

    ArticleAnnotation annotation;

    for (int i = 0; i < annotations.length; i++) {
      annotation = annotations[i];
      if ((lists[i] != null) && (annotation.getContext() != null)) {
        Element a = document.createElementNS(AML_NS, "aml:annotation");
        a.setAttributeNS(AML_NS, "aml:id", annotation.getId().toString());
        aRoot.appendChild(a);
        AnnotationModel.appendToNode(a, annotation);
      }
    }
    return assembleResultDoc(document, rRoot, aRoot);
  }

  private static Document assembleResultDoc(Document document, Element regions, Element annotations) {
    String  xmlns = "http://www.w3.org/2000/xmlns/";

    Element source = document.getDocumentElement();
    source.setAttributeNS(xmlns, "xmlns:aml", AML_NS);

    source.appendChild(regions);
    source.appendChild(annotations);

    return document;
  }

  /**
   * Parses the content into a DOM document.
   *
   * @param content the content to parse
   * @param builder documentBuilder
   * @return the dom document
   *
   * @throws SAXException on an error in parse
   * @throws ParserConfigurationException if a suitable parser could not be found
   * @throws IOException on an error in reading the content
   */
  private static Document parse(final DataHandler content, final DocumentBuilder builder)
                        throws SAXException, ParserConfigurationException, IOException {
    return builder.parse(content.getInputStream());
  }

  /**
   * Serializes a DOM document.
   *
   * @param document the document to serialize
   *
   * @return the serialized
   *
   * @throws TransformerException on an error in serialize
   * @throws IOException on an error in writing
   */
  private static DataHandler serialize(Document document)
                               throws TransformerException, IOException {
    TransformerFactory xformFactory = TransformerFactory.newInstance();
    Transformer        idTransform = xformFactory.newTransformer();
    Source             input       = new DOMSource(document);
    final File         tmp         = File.createTempFile("annotated", ".xml");
    tmp.deleteOnExit();

    Result output = new StreamResult(tmp);
    idTransform.transform(input, output);

    return new DataHandler(new FileDataSource(tmp) {
        @Override
        protected void finalize() throws Throwable {
          super.finalize();

          try {
            tmp.delete();
          } catch (Throwable t) {
            log.warn("Failed to delete temporary file " + tmp);
          }
        }
      });
  }

  private static LocationList[] evaluate(Document document, ArticleAnnotation[] annotations)
                                  throws URISyntaxException, TransformerException {
    ArrayList<LocationList> lists = new ArrayList<LocationList>(annotations.length);

    String annotationContext;

    for (int i = 0; i < annotations.length; i++) {
      annotationContext = annotations[i].getContext();
      if (annotationContext != null){
        URI    context    = new URI(annotationContext);
        String expression = context.getFragment();
        if (expression != null) {
          try {
            expression = URLDecoder.decode(expression, "UTF-8");
          } catch (UnsupportedEncodingException e) {
            throw new Error(e);
          }

          try {
            LocationList list = XPointerAPI.evalFullptr(document, expression);
            lists.add(list);
          } catch (Exception e) {
            StringBuilder errorMsg = new StringBuilder();
            log.error ("Could not evaluate xPointer");
            errorMsg.append("AnnotationID: ").append(annotations[i].getId());
            errorMsg.append(" Context: ").append(annotations[i].getContext());
            errorMsg.append(" Created: ").append(annotations[i].getCreated());
            errorMsg.append(" Creator: ").append(annotations[i].getCreator());
            errorMsg.append(" ID: ").append(annotations[i].getId());
            errorMsg.append(" Annotates: ").append(annotations[i].getAnnotates());
            errorMsg.append(" Title: ").append(annotations[i].getTitle());
            log.error(errorMsg, e);
            lists.add(null);
            // Trap the error here and continue.  One bad annotation shouldn't
            // cause the article to fail rendering.
            //throw new TransformerException(expression, e);
          }
        } else {
          lists.add(null);
        }
      } else {
        lists.add(null);
      }
    }
    LocationList[] theList = new LocationList[lists.size()];
    return lists.toArray(theList);
  }

  private static class Regions extends SelectionRangeList {
    private final Document document;

    public Regions(Document document) {
      this.document = document;
    }

    public void addRegion(LocationList list, ArticleAnnotation annotation) {
      int length = list.getLength();

      for (int i = 0; i < length; i++)
        addRegion(list.item(i), annotation);
    }

    public void addRegion(Location location, ArticleAnnotation annotation) {
      Range range;

      if (location.getType() == Location.RANGE)
        range = (Range) location.getLocation();
      else {
        range = ((DocumentRange) document).createRange();
        range.selectNode((Node) location.getLocation());
      }

      // Ignore it if this range is collapsed (ie. start == end)
      if (!range.getCollapsed())
        insert(new SelectionRange(range, annotation));
    }


    public Element createElement(String nsUri, String elemQName, String annotationsQName,
                                 String idAttrQName) {
      int     length = size();
      Element root = document.createElementNS(nsUri, elemQName + "s");
      
      for (int i = 0; i < length; i++) {
        Element rNode = document.createElementNS(nsUri, elemQName);
        rNode.setAttributeNS(nsUri, idAttrQName, "" + (i + 1));

        int numComments = 0;
        int numMinorCorrections = 0;
        int numFormalCorrections = 0;

        List annotations = get(i).getUserDataList();

        int  c = annotations.size();

        for (int j = 0; j < c; j++) {
          ArticleAnnotation a     = (ArticleAnnotation) annotations.get(j);
          Element        aNode = document.createElementNS(nsUri, annotationsQName);
          aNode.setAttributeNS(nsUri, idAttrQName, a.getId().toString());
          rNode.appendChild(aNode);
          
          assert a.getType() != null;
          String atype = a.getType().toLowerCase();
          if(atype.indexOf("comment") >= 0) {
            numComments++;
          }
          else if(atype.indexOf("minorcorrection") >= 0) {
            numMinorCorrections++;
          }
          else if(atype.indexOf("formalcorrection") >= 0) {
            numFormalCorrections++;
          }
        }
        
        rNode.setAttributeNS(nsUri, "aml:numComments", Integer.toString(numComments));
        rNode.setAttributeNS(nsUri, "aml:numMinorCorrections", Integer.toString(numMinorCorrections));
        rNode.setAttributeNS(nsUri, "aml:numFormalCorrections", Integer.toString(numFormalCorrections));
        
        root.appendChild(rNode);
      }

      return root;
    }
  }

}
