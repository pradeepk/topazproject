
package xpointer.custom;

import xpointer.Location;
import org.w3c.dom.Node;
import org.w3c.dom.ranges.Range;

/**
 *
 * @author  root
 * @version 
 */
public class Word {

    private Regexp regexp;
    private final String PATTERN = "\\w+";
    
    
    /** Creates new Word */
    public Word(Location loc) 
    {
        regexp = new Regexp(loc);
    }
    
    public Word(Node node)
    {
        regexp = new Regexp(node);
    }

    public Range[] getWords()
    {
        return regexp.getRegexp(PATTERN);
    }
}
