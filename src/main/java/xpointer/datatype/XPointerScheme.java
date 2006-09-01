/*
 * XPointerScheme.java
 *
 * Created on 13 febbraio 2002, 11.16
 */

package xpointer.datatype;

import java.util.Vector;

/**
 *
 * @author  root
 * @version 
 */
public class XPointerScheme extends Scheme {

    private SchemeListImpl sli = new SchemeListImpl();
    
    /** Creates new XPointerScheme */
    public XPointerScheme() {
    }

    public XPointerScheme(String val)
    {
        super(val,Scheme.XPOINTER_SCHEME);
    }
    
    /**
     * Adds a namespace for the evaluation of this pointer.
     */
    public void addXmlNSScheme(XmlNSScheme nsScheme)
    {
        sli.addScheme(nsScheme);
    }
    
    /**
     * Returns the xmlns schemes associated to this xpointer.
     */
    public SchemeList getXmlNSSchemes()
    {
        return sli;
    }
}
