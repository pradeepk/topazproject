/*
 * TextPoint.java
 *
 * Created on 22 aprile 2002, 16.11
 */

package xpointer.custom;

import xpointer.Location;
import xpointer.StringRange;
import org.w3c.dom.ranges.Range;
import javax.xml.transform.TransformerException;

/**
 * This class represent the textpoint function, which is equivalent to string-range called with an empty string.
 */
public class TextPoint {

    private StringRange sr;
    
    /** Creates new TextPoint */
    public TextPoint(Location loc) {
        
        sr = new StringRange(loc);
    }

    public Range []getTextPoint() throws TransformerException
    {
        return sr.getStringRange("");
    }
}
