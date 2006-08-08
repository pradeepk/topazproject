/* $HeadURL::                                                                            $
 * $Id$
 */
package org.plos.web;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.BasePlosoneTestCase;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.URISyntaxException;

public class XSLTransformationTest extends BasePlosoneTestCase {
  public static final Log log = LogFactory.getLog(XSLTransformationTest.class);

  private final String XML_SOURCE = "/pbio.0000001-embedded-math-dtd.xml";
  private final String XSL_SOURCE = "/viewnlm-v2.xsl";
  private final String OUTPUT_FILENAME = "foo.html";

  public void testXSLTransformation() throws TransformerException, FileNotFoundException, URISyntaxException {
    final Transformer transformer = getXSLTransformer(XSL_SOURCE);
    
    TimeIt.logTime();
    final File file = getAsURI(XML_SOURCE);
    transformXML(transformer, new StreamSource(file), OUTPUT_FILENAME);
    TimeIt.logTime();
  }

  private void transformXML(final Transformer transformer, final StreamSource xmlSource, final String outputFileName) throws TransformerException, FileNotFoundException {
    // 3. Use the Transformer to transform an XML_SOURCE Source and send the
    //    output to a Result object.
    transformer.transform(
            xmlSource,
            new StreamResult(new FileOutputStream(outputFileName)));
  }

  private Transformer getXSLTransformer(final String xslStyleSheet) throws TransformerConfigurationException, URISyntaxException {
    // 1. Instantiate a TransformerFactory.
    final TransformerFactory tFactory = TransformerFactory.newInstance();

    // 2. Use the TransformerFactory to process the stylesheet Source and
    //    generate a Transformer.
    final File file = getAsURI(xslStyleSheet);
    return tFactory.newTransformer(new StreamSource(file));
  }

  private File getAsURI(final String filename) throws URISyntaxException {
    return new File(getClass().getResource(filename).toURI());
  }

}

class TimeIt {
  public static void run(final Command command) {
    logTime();
    command.execute();
    logTime();
  }

  public static void logTime() {
    XSLTransformationTest.log.info(System.currentTimeMillis());
  }
}

interface Command {
  void execute();
}