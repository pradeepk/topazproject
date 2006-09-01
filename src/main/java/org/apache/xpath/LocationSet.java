/*

  XPointer API v 0.1 - an XPointer CR implementation
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
package org.apache.xpath;

import java.util.Vector;
import xpointer.Location;
import xpointer.LocationIterator;
import org.apache.xpath.axes.ContextLocationList;

/**
 * This class acts as a LocationIterator.
 *
 */
public class LocationSet implements LocationIterator,ContextLocationList,Cloneable{

    private Vector v;
    private int iterator;
    
    /** Creates new LocationSet */
    public LocationSet() {
        v = new Vector();
        iterator = 0;
    }

    public void addLocation(Location location)
    {
        v.addElement(location);
    }
    
    public int getLength()
    {
        return v.size();
    }
    
    public Location elem(int i)
    {
        return (Location)v.elementAt(i);
    }
    
    public Location nextLocation() {
        
        if(iterator<v.size())
            return (Location) v.elementAt(iterator++);
        else 
            return null;
    }
    
    public Object clone() throws CloneNotSupportedException
    {
        LocationSet clone = (LocationSet) super.clone();
        
        clone.v = new Vector();
        
        for(int i=0;i<v.size();i++)
            clone.v.addElement(((Location)v.elementAt(i)).clone());
        
        return clone;
    }
    
    public void reset()
    {
        iterator = 0;
    }
    
    public void setCurrentPos(int i) {
        iterator = i;
    }
    
    public int size() {
        return v.size();
    }
    
    public int getCurrentPos() {
        return iterator;
    }
    
    public Location getCurrentLocation() {
        
        return (Location)v.elementAt(iterator-1);
    }
    
}
