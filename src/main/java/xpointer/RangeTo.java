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

package xpointer;

/**
 * This class represents the range-to() XPointer function.
 */

import org.w3c.dom.*;
import org.w3c.dom.ranges.*;
import javax.xml.transform.TransformerException;

public class RangeTo {

    /** Creates new RangeTo */
    public RangeTo() {
    }

    /**
     * Creates a range starting from the beginning of the context location 
     * to the end of the given location.
     * The start point of the the range is the start point of the context location and
     * the end point of the range is the end point of the given location, which is evaluated
     * with respect to the context location.  
     * 
     * @param contextLocation the context location
     * @param location the given location
     * @return a range starting from the beginning of the context location 
     * to the end of the given location.
     */
    public Range getRangeTo(Location contextLocation,Location location) throws TransformerException
    {
        StartPoint sp = new StartPoint();
        EndPoint ep = new EndPoint();
        
        Range startPoint = sp.getStartPoint(contextLocation);
        Range endPoint = ep.getEndPoint(location);
        
        Range range;
        Node temp;
        
        if(location.getType()==Location.NODE)
            temp = (Node) location.getLocation();
        else
            temp = ((Range)location.getLocation()).getStartContainer();
        
        if(temp.getNodeType()==Node.DOCUMENT_NODE)
            range = ((DocumentRange)temp).createRange();
        else
            range = ((DocumentRange)temp.getOwnerDocument()).createRange();
        
        range.setStart(startPoint.getStartContainer(),startPoint.getStartOffset());
        range.setEnd(endPoint.getEndContainer(),endPoint.getEndOffset());
        
        TaxDomHelper taxDomHelper = new TaxDomHelper(new org.apache.xpath.DOMHelper());
        Location startLoc = new Location();
        startLoc.setType(Location.RANGE);
        startLoc.setLocation(startPoint);
        Location endLoc = new Location();
        endLoc.setType(Location.RANGE);
        endLoc.setLocation(endPoint);
        if (isRangeAfter(startPoint,endPoint)==false)
            throw new TransformerException("Subresource Error: range start-point is after end-point");
        
        return range;
    }
    
    private boolean isRangeAfter(Range r1,Range r2)
    {
        switch(r1.compareBoundaryPoints(Range.START_TO_START,r2))
        {
            case -1: return true;
                
            case 1 : return false;
            
            default: //0
                switch(r1.compareBoundaryPoints(Range.END_TO_END,r2))
                {
                    case -1: 
                    case 0:    return true;
                    default: return false; //1
                }
        }
            
    }
}
