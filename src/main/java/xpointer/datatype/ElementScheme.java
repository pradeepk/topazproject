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

package xpointer.datatype;

import xpointer.parsing.ElementSchemeParser;

/**
 *
 * @author  root
 * @version 
 */
public class ElementScheme extends Scheme {

    /** Creates new ElementScheme */
    public ElementScheme(String schemedata) {
        super(schemedata,Scheme.ELEMENT_SCHEME);
    }

    /**
     * Converts an XPointer expression (conforming to the element() scheme) to 
     * the equivalent XPath expression.
     * @return an XPath expression
     */
    public String getXPathExpression() 
    {    
        ElementSchemeParser esp = new ElementSchemeParser(getValue());
        
        Object [] token_queue = esp.getTokenQueue();
        
        String retval="";
        int i=0;
        
        if(token_queue[0] instanceof String)
        {
            retval = "id('"+token_queue[0]+"')";
            i = 1;
        }
        
        for(;i<token_queue.length;i++)
        {
            retval = retval + "/*[" + token_queue[i]+"]";
        }
        
        return retval;
    }
    
    
    
}
