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

/**
 * This class represents the xmlns() scheme.
 */
public class XmlNSScheme extends Scheme {

    
    /**
     * @param value the schemedata 
     */
    public XmlNSScheme(String value)
    {
        super(value,Scheme.XMLNS_SCHEME);
    }
    
    /** Creates new XmlNSScheme */
    public XmlNSScheme() {
    }

   
    
    /**
     * Returns the namespace defined in the schemedata.
     */
    public String getNamespaceURI()
    {
        String schemedata = getValue();
        
        int index = schemedata.indexOf('=') + 1;
        
        if(schemedata.charAt(index)==' ')
            index++;
        
        String namespaceURI = schemedata.substring(index);
        
        return namespaceURI;
    }
    
    /**
     * Returns the namespace prefix associated with the XML namespace
     */
    public String getPrefix()
    {
        String schemedata = getValue();
        
        String prefix = "";
        int index = 0;
        
        while(schemedata.charAt(index)!=' ' && schemedata.charAt(index)!='=' )
        {
            prefix = prefix + schemedata.charAt(index);
            index++;
        }
        
        return prefix;
    }
}
