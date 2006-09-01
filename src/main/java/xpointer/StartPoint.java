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
 * This class represents the start-point() XPointer function.
 */

import org.w3c.dom.*;
import org.w3c.dom.ranges.*;
import javax.xml.transform.TransformerException;

public class StartPoint {

    /** Creates new StartPoint */
    public StartPoint() {
    }

     /**
     * Creates the start-point of the given generic location.
     *
     * @param location a generic location
     * @return a collapsed range corresponding to the start point of the given location  
     */
    public Range getStartPoint(Location location) throws TransformerException
    {
        if(location.getType()==Location.NODE)
            
            return getStartPoint((Node)location.getLocation());
        
        else
            
            return getStartPoint((Range)location.getLocation());
    }
    
     /**
     * Creates the start-point of the given range.
     * 
     * 
     * @param range a range
     * @return the collapsed range corresponding to the start point of the given range
     */
    private Range getStartPoint(Range range)
    {   
        Node node = range.getStartContainer();
        DocumentRange docRange;
        Range retval;
        
        if(node.getNodeType()==Node.DOCUMENT_NODE)
            docRange = (DocumentRange) node;
        else
            docRange = (DocumentRange) node.getOwnerDocument();
        
        retval = docRange.createRange();
        
        retval.setStart(range.getStartContainer(),range.getStartOffset());
        retval.setEnd(range.getStartContainer(),range.getStartOffset());
        
        return retval;
    }
    
    /**
     * Creates the start point of the given node.
     *
     * If the given node is of type attribute, the method returns null.
     * For any other kind of node, the container node of resulting point is the given node and the index
     * is zero.
     *    
     * @param node a node
     * @return the collapsed range corresponding to the start point of the given node, null if
     * the given node is of type attribute
     */
    public Range getStartPoint(Node node) throws TransformerException
    {
        Range range;
        if(node.getNodeType()==Node.DOCUMENT_NODE)
            range = ((DocumentRange)node).createRange();
        else    
            range = ((DocumentRange)node.getOwnerDocument()).createRange();
        
        switch(node.getNodeType())
        {
            case Node.ATTRIBUTE_NODE:
                throw new TransformerException("Subresource Error: start-point argument is attribute or namespace");
                
            default:    
                range.setStart(node,0);
                range.setEnd(node,0);
        }
        
        return range;
    }
}
