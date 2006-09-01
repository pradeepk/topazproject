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

import org.w3c.dom.*;
import org.w3c.dom.ranges.*;
import xpointer.custom.Regexp;
import org.apache.xpath.*;
import org.apache.xpath.objects.*;
import org.w3c.dom.traversal.NodeIterator;
import xpointer.*;
import org.apache.xpath.LocationSet;
import java.util.Hashtable;

/**
 *
 * @author  root
 * @version 
 */
public class FuncRegexp extends Function2Args {

        
    private Hashtable groups = null;
    
    public XObject execute(XPathContext xctxt) throws javax.xml.transform.TransformerException
    {
        groups = new Hashtable();
        
        XLocationSet retval = new XLocationSet();
        LocationSet locset = retval.mutableLocationSet();
        
        XObject xobj = getArg0().execute(xctxt);
        String matchingString = getArg1().execute(xctxt).str();
        
        Regexp regexp;
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
                    regexp = new Regexp(current);
                    tempArray = regexp.getRegexp(matchingString);
                    for(int i=0;i<tempArray.length;i++)
                    {
                        loc = new Location();
                        loc.setLocation(tempArray[i]);
                        loc.setType(Location.RANGE);
                        locset.addLocation(loc);
                        
                        groups.put(loc,regexp.getGroups(i));
                    }
                }
                break;
        
            case XObject.CLASS_LOCATIONSET:
                XLocationSet xLocationSet = (XLocationSet) xobj;
                LocationIterator locIterator = xLocationSet.locationSet();
                Location current2,loc2;
                while((current2=locIterator.nextLocation())!=null)
                {
                    regexp = new Regexp(current2);
                    tempArray = regexp.getRegexp(matchingString);
                    for(int i=0;i<tempArray.length;i++)
                    {
                        loc2 = new Location();
                        loc2.setLocation(tempArray[i]);
                        loc2.setType(Location.RANGE);
                        locset.addLocation(loc2);
                        
                        groups.put(loc2,regexp.getGroups(i));
                    }
                }
        }
        
        return retval;
    }
    
    public Hashtable getGroupMap()
    {
        return groups;
    }
}
