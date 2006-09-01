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
import xpointer.*;
import org.apache.xpath.objects.XLocationSet;
import org.apache.xpath.LocationSet;
import org.apache.xpath.objects.XNodeSet;
import org.w3c.dom.traversal.NodeIterator;
import org.w3c.dom.*;
import org.w3c.dom.ranges.*;

/**
 *
 * @author  root
 * @version 
 */
public class FuncStringRange extends FunctionMultiArgs {

    
    private int numArgs = 2;
    private int offset,len;
    private String matchingString;
    
    public XObject execute(XPathContext xctxt) throws javax.xml.transform.TransformerException
    {
        XLocationSet retval = new XLocationSet();
        LocationSet locset = retval.mutableLocationSet();
        
        XObject xobj = getArg0().execute(xctxt);
        matchingString = getArg1().execute(xctxt).str();
        
        if(getArg2()!=null)
        {
            offset = (int) getArg2().execute(xctxt).num();
            numArgs++;
            if(m_args!=null)
            {
                len = (int) m_args[0].execute(xctxt).num();
                numArgs++;
            }
        }
        
        Range [] tempArray; 
        
        switch(xobj.getType())
        {
            case XObject.CLASS_NODESET:
                XNodeSet xNodeSet = (XNodeSet) xobj;
                NodeIterator nodeIterator = xNodeSet.nodeset();
                Node current;
                Location loc;
                while((current=nodeIterator.nextNode())!=null)
                {
                    tempArray = process(current);
                    for(int i=0;i<tempArray.length;i++)
                    {
                        loc = new Location();
                        loc.setLocation(tempArray[i]);
                        loc.setType(Location.RANGE);
                        locset.addLocation(loc);
                    }
                }
                break;
         
            case XObject.CLASS_LOCATIONSET:
                XLocationSet xLocationSet = (XLocationSet) xobj;
                LocationIterator locIterator = xLocationSet.locationSet();
                Location current2,loc2;
                while((current2=locIterator.nextLocation())!=null)
                {
                    tempArray = process(current2);
                    for(int i=0;i<tempArray.length;i++)
                    {
                        loc2 = new Location();
                        loc2.setLocation(tempArray[i]);
                        loc2.setType(Location.RANGE);
                        locset.addLocation(loc2);
                    }
                }
        }
        
        return retval;
    }
    
    
    private Range[] process(Object obj) throws javax.xml.transform.TransformerException
    {
        Location loc;
        Range []result = null;
        
        if(obj instanceof Node)
        {
            loc = new Location();
            loc.setType(Location.NODE);
            loc.setLocation(obj);
        }
        else
            loc = (Location) obj;
        
        StringRange stringRange = new StringRange(loc);    
        
        switch(numArgs)
        {
            case 2:
                result = stringRange.getStringRange(matchingString);
                break;
            case 3:
                result = stringRange.getStringRange(matchingString,offset);
                break;
            case 4:
                result = stringRange.getStringRange(matchingString,offset,len);
        }
        
        return result;
    }
}
