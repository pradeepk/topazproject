/*

  XPointer API  - an XPointer CR implementation
  Copyright (C) 2002 Claudio Tasso

  This product includes software developed by the Apache Software
  Foundation (http://www.apache.org).
  The Apache Software Foundation is NOT involved in this project.
  
  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.

*/

package xpointer.parsing;

import xpointer.datatype.*;
import java.util.Vector;

/**
 * This class provides some methods for extracting the
 * schemes used in an XPointer expression.
 *  
 */
public class SchemeParser {

    private SchemeListImpl xmlnsList,schemeList;
    
    
    /** integers identifyng schemes correspond to positions in this array*/
    public static final String [] schemeNames = {"element","xmlns","xpointer"};
    
    
    /** Creates new SchemeParser 
     * @param str the XPointer expression
     */
    public SchemeParser(String str) {
        
        
        xmlnsList = new SchemeListImpl();
        schemeList = new SchemeListImpl();
       
        Lexer lexer = new Lexer(str);
        lexer.analyzeExpression();
        
        buildSchemes(lexer.getTokenQueue());
    }

    /**
     * Returns a list of ALL the schemes of any kind.
     * The ordering of the schemes in the list reflects the order of the schemes
     * in the full xpointer expression.
     */
    public SchemeList getSchemeList()
    {
        return schemeList;
    }
   
    /**
     * Returns xmlns schemes referring a given xpointer scheme
     */
    public SchemeList getXmlNSSchemeList(XPointerScheme xpointerScheme)
    {
        return xpointerScheme.getXmlNSSchemes();
    }
    
    /**
     * Builds the different lists of pointers and makes the association between
     * namespaces and xpointer.
     * @param token_queue 
     */
    private void buildSchemes(Object []token_queue)
    {
        
        for(int i=0;token_queue[i]!=null;i+=2)
        {
            int type = ((Integer)token_queue[i]).intValue();
            
            switch(type)
            {
                case Scheme.ELEMENT_SCHEME:
                {
                    schemeList.addScheme(new ElementScheme((String)token_queue[i+1]));
                    break;
                }
                case Scheme.XPOINTER_SCHEME:
                {
                    XPointerScheme xpointerScheme = new XPointerScheme((String)token_queue[i+1]);
                    for(int j=0;j<xmlnsList.getLength();j++)
                    {
                        XmlNSScheme tempXmlNSScheme = (XmlNSScheme)xmlnsList.item(j);
                        xpointerScheme.addXmlNSScheme(tempXmlNSScheme);
                    }    
                    
                    schemeList.addScheme(xpointerScheme);
                    break;
                }
                case Scheme.XMLNS_SCHEME:
                {
                    XmlNSScheme xmlNSScheme = new XmlNSScheme((String)token_queue[i+1]);
                    /*check to see if this prefix is already used*/
                    for(int j=0;j<xmlnsList.getLength();j++)
                    {
                        XmlNSScheme comparingScheme = (XmlNSScheme)xmlnsList.item(j);
                        if(xmlNSScheme.getPrefix().equals(comparingScheme.getPrefix()))
                        {
                            xmlnsList.removeScheme(comparingScheme);
                        }
                    }
                    xmlnsList.addScheme(xmlNSScheme);
                    schemeList.addScheme(xmlNSScheme);
                    break;
                }
                case Scheme.SHORTHAND:
                {
                    schemeList.addScheme(new ShortHand((String)token_queue[i+1]));
                    break;
                }
            }
        }
    }
    
}
