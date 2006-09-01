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

import org.w3c.dom.*;
import org.w3c.dom.ranges.*;

/**
 * This class represents the concept of location according to XPointer CR.
 * A location can be a node,a range or a point. We assume that a point is identical
 * to a collapsed range.
 */

public class Location implements Cloneable{

    public static final int RANGE = 0;
    
    public static final int NODE = 1;
    
    private int type;
    
    private Object location;
    
    public int getType()
    {
            return type;
    }
    
    public void setType(int param)
    {
        type=param;
    }
    
    public void setLocation(Object param)
    {
        location=param;
    }
    
    public Object getLocation()
    {
        return location;
    }
    
    public Object clone() throws CloneNotSupportedException
    {
        Location clone = (Location) super.clone();
        
        clone.type = type;
        
        if(type==NODE)
            clone.location = ((Node)location).cloneNode(true);
        else
            clone.location = ((Range)location).cloneRange();
        
        return clone;
    }
    
    /** Creates new Location */
    public Location() {
    }

    /**
     * Compares this location with another one.
     * 
     * @param loc the location to be compared
     * @return true if this location is equal to the location passed as an argument, false otherwise.
     */
    public boolean equals(Location loc)
    {
        if(type!=loc.getType())
            return false;
        
        if(loc.getType()==NODE)
        {
            Node node1 = (Node) loc.getLocation();
            Node node2 = (Node) location;
            return node1.equals(node2);
        }
        else
        {
            Range r1 = (Range) loc.getLocation();
            Range r2 = (Range) location;
            
            if(r1.getStartContainer()!=r2.getStartContainer())
                return false;
            else
                if(r1.getStartOffset()!=r2.getStartOffset())
                    return false;
                else
                    if(r1.getEndContainer()!=r2.getEndContainer())
                        return false;
                    else
                        if(r1.getEndOffset()!=r2.getEndOffset())
                            return false;
                        else
                            return true;
        }
    }
}
