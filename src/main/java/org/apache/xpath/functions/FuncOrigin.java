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

package org.apache.xpath.functions;

import org.apache.xpath.XPathContext;
import org.apache.xpath.objects.XObject;
import org.apache.xpath.objects.XLocationSet;
import org.apache.xpath.LocationSet;
import javax.xml.transform.TransformerException;

/**
 *
 * @author  root
 * @version 
 */
public class FuncOrigin extends Function {

   public XObject execute(XPathContext xctxt) throws javax.xml.transform.TransformerException
    {
        XLocationSet retval = new XLocationSet();
        LocationSet locset = retval.mutableLocationSet();
        
        xpointer.Location origin = xctxt.getOrigin();
        
        if(origin==null)
            throw new TransformerException("Error: oring not specified");
        
        locset.addLocation(origin);
        
        return retval;
    }

}
