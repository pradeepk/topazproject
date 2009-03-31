/*
 * $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006-2009 by Topaz, Inc.
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

package org.topazproject.ambra.annotation.service;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import static org.testng.Assert.assertTrue;
import org.w3c.dom.Document;
import org.topazproject.ambra.models.ArticleAnnotation;
import org.topazproject.ambra.models.FormalCorrection;
import org.topazproject.ambra.models.AnnotationBlob;
import org.topazproject.ambra.models.Retraction;
import org.topazproject.ambra.model.MockBlob;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.net.URISyntaxException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.io.IOException;
import java.util.TimeZone;

/**
 * @author Dragisa Krsmanovic
 */
public class AnnotatorTest {

  @Test
  public void testAnnotateAsDocument()
      throws ParseException, ParserConfigurationException, URISyntaxException,
      TransformerException, IOException, SAXException {

    SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy");

    String articleId = "info:doi/10.1371/journal.pone.0002250";

    ArticleAnnotation[] annoatations = new ArticleAnnotation[2];

    FormalCorrection fc = new FormalCorrection();
    fc.setId(URI.create("formalCorrection:1234"));
    fc.setAnnotates(URI.create(articleId));
    AnnotationBlob blob1 = new AnnotationBlob();
    blob1.setId("AnnotationBlob:1");
    blob1.setBody(new MockBlob("Blob:1", "Annotation One"));
    fc.setBody(blob1);
    // #xpointer(string-range(/article[1]/body[1]/sec[1]/p[1], '', 17, 3)[1])
    fc.setContext(articleId +
        "#xpointer(string-range(%2farticle%5b1%5d%2fbody%5b1%5d%2fsec%5b1%5d%2fp%5b1%5d%2c+''%2c+17%2c+3)%5b1%5d)");
    fc.setCreated(dateFormat.parse("03/22/09"));
    fc.setCreator("user:1");
    fc.setTitle("Formal Correction Title");
    fc.setState(0);

    annoatations[0] = fc;

    Retraction r = new Retraction();
    r.setId(URI.create("retrcation:3245"));
    r.setAnnotates(URI.create(articleId));
    AnnotationBlob blob2 = new AnnotationBlob();
    blob2.setId("AnnotationBlob:2");
    blob2.setBody(new MockBlob("Blob:2", "Annotation Two"));
    r.setBody(blob2);
    // #xpointer(string-range(/article[1]/front[1]/article-meta[1]/abstract[1]/p[1], '', 10, 4)[1])
    r.setContext(articleId +
        "#xpointer(string-range(%2Farticle%5B1%5D%2Ffront%5B1%5D%2Farticle-meta%5B1%5D%2Fabstract%5B1%5D%2Fp%5B1%5D%2C+''%2C+10%2C+4)%5B1%5D)");
    r.setCreated(dateFormat.parse("12/01/08"));
    r.setCreator("user:2");
    r.setTitle("Retraction Title");
    r.setState(0);

    annoatations[1] = r;

    Document doc = XMLUnit.buildTestDocument(new InputSource(getClass().getResourceAsStream("/annotation/document.xml")));
    Document expected = XMLUnit.buildControlDocument(new InputSource(getClass().getResourceAsStream("/annotation/result.xml")));

    Document result = Annotator.annotateAsDocument(doc, annoatations);

    Diff diff = new Diff(expected, result);
    assertTrue(diff.identical(), diff.toString());
  }

  @BeforeTest
  public void setUp() {

    System.setProperty("javax.xml.transform.TransformerFactory","org.apache.xalan.processor.TransformerFactoryImpl");
    System.setProperty("javax.xml.parsers.DocumentBuilderFactory","org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
    System.setProperty("javax.xml.parsers.SAXParserFactory","org.apache.xerces.jaxp.SAXParserFactoryImpl");

    TimeZone.setDefault(TimeZone.getTimeZone("GMT-8"));

    XMLUnit.setIgnoreAttributeOrder(true);
    XMLUnit.setIgnoreComments(true);
    XMLUnit.setIgnoreWhitespace(true);
  }

}
