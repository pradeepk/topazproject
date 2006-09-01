/*
 * Sentence.java
 *
 * Created on 16 febbraio 2002, 8.54
 */

package xpointer.custom;

import xpointer.Location;
import org.w3c.dom.Node;
import org.w3c.dom.ranges.Range;
import java.util.Vector;

/**
 *
 * @author  root
 * @version 
 */
public class Sentence {

    private String PATTERN = "(\\w.*?)";
    
    private String []boundaries;
    private int boundSize;
    private boolean customBoundaries = true;
    private Regexp regexp;
    private int INDEX = 1;
   

    public Sentence(Location loc)
    {
        regexp = new Regexp(loc);
        init();
    }
    
    public Sentence(Node node)
    {
        regexp = new Regexp(node);
        init();
    }
    
    private String buildPattern()
    {
        String pattern = PATTERN + "(\\" + boundaries[0];
        
        for(int i=1;i<=boundSize;i++)
            pattern = pattern + "|\\" +boundaries[i];
        
        pattern = pattern + ")+";
        
        return pattern;
    }
    
    private void init() {
        
        regexp.setFlag(gnu.regexp.RE.REG_DOT_NEWLINE);
        boundSize = 2;
        boundaries = new String[3];
        
        boundaries[0]=".";
        boundaries[1]="!";
        boundaries[2]="?";
        
    }

    public void setBoundaryOutput(boolean flag)
    {
        if(flag==true)
            INDEX = 0;
        else
            INDEX = 1;
    }
    
    public void setBoundary(String s)
    {
        if(customBoundaries==false)
        {
            customBoundaries = true;
            boundSize = -1;
            boundaries = new String[255];
        }
        
        boundSize++;
        boundaries[boundSize] = s;
    }
    
    public Range [] getSentences()
    {
        //DEBUG
        System.out.println("PATTERN:"+buildPattern());
        int len = regexp.getRegexp(buildPattern()).length;
        
        Range [] retval;
        Vector temp = new Vector();
        
        for(int i=0;i<len;i++)
        {
            temp.addElement(regexp.getGroups(i)[INDEX]);
        }
        
        retval = new Range[temp.size()];
        
        for(int i=0;i<retval.length;i++)
        {
            retval[i] = (Range) temp.elementAt(i);
        }
        
        return retval;
    }
}
