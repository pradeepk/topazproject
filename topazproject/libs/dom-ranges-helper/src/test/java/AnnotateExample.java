/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.transform.TransformerException;

import org.topazproject.dom.ranges.SelectionRange;
import org.topazproject.dom.ranges.SelectionRangeList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ranges.DocumentRange;
import org.w3c.dom.ranges.Range;

import it.unibo.cs.xpointer.Location;
import it.unibo.cs.xpointer.XPointerAPI;
import it.unibo.cs.xpointer.datatype.LocationList;

/**
 * A sample implementation for creating an annotated document.
 *
 * @author PradeepKrishnan
 */
public class AnnotateExample {
  /**
   * Insert annotation markers in a document.
   *
   * @param document the document to annotate
   * @param annotations the list of annotations on the document
   *
   * @throws URISyntaxException if the annotation context is not valid
   * @throws TransformerException on error in xpointer evaluation
   */
  public static void annotate(Document document, AnnotationInfo[] annotations)
                       throws URISyntaxException, TransformerException {
    // Step 1: build non overlapping ranges
    SelectionRangeList srl = new SelectionRangeList();

    for (int i = 0; i < annotations.length; i++) {
      URI          context    = new URI(annotations[i].getContext());
      String       expression = context.getFragment();
      LocationList list       = XPointerAPI.evalFullptr(document, expression);
      int          length     = list.getLength();

      for (int j = 0; j < length; j++) {
        Location location = list.item(j);
        Range    range;

        if (location.getType() == Location.RANGE)
          range = (Range) location.getLocation();
        else {
          range = ((DocumentRange) document).createRange();
          range.selectNode((Node) location.getLocation());
        }

        // Ignore it if this range is collapsed (ie. start == end)
        if (!range.getCollapsed())
          srl.insert(new SelectionRange(range, annotations[i]));
      }
    }

    // Step 2: get a list of 'surroundable' ranges.
    int       length = srl.size();

    Range[][] ranges = new Range[length][0];

    for (int i = 0; i < length; i++)
      ranges[i] = srl.get(i).getSurroundableRanges();

    // Step 3: surround each surroundable region with annotation markers
    for (int i = 0; i < length; i++) {
      AnnotationInfo[] a =
        (AnnotationInfo[]) srl.get(i).getUserDataList().toArray(new AnnotationInfo[0]);

      for (int j = 0; j < ranges[i].length; j++) {
        Element rNode = document.createElementNS("http://topaz.org/aml", "aml:annotated");
        ranges[i][j].surroundContents(rNode);

        for (int k = 0; k < a.length; k++) {
          Element meta = document.createElementNS("http://topaz.org/aml", "aml:annotation");
          meta.setAttributeNS("http://purl.org/dc/elements/1.1/", "dc:creator", a[k].getCreator());

          // and similarly for other meta info
          rNode.appendChild(meta);
        }
      }
    }
  }

  private class AnnotationInfo {
    public String getCreator() {
      return "FSM";
    }

    public String getContext() {
      return "#xpointer(string-range('Hello world'))";
    }
  }
}
