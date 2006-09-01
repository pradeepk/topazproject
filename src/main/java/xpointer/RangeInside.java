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
 * This class represents the range-inside() XPointer function.
 */

import org.w3c.dom.*;
import org.w3c.dom.ranges.*;
import javax.xml.transform.TransformerException;


public class RangeInside {

    /** Creates new RangeInside */
    public RangeInside() {
    }

    /**
     * Creates a range that covers the contents of a generic location.
     *
     * @param location a location
     * @return a range covering the contents of the location
     */
    public Range getRangeInside(Location location) throws TransformerException
    {
        if(location.getType()==Location.RANGE)
            
            return getRangeInside((Range)location.getLocation());
        
        else
            
            return getRangeInside((Node)location.getLocation());
            
    }
    
    /**
     * Creates a range tha covers the contents of a given range.
     * @param range a range
     * @return the same range
     */
    public Range getRangeInside(Range range)
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
        retval.setEnd(range.getEndContainer(),range.getEndOffset());
        
        return retval;
    }
    
    /**
     * Creates a range tha covers the contents of a given node.
     *
     * The container node of the start point and of the end point of the resulting range 
     * is the node passed as an argument.
     * The index of the start point is zero. 
     * If the end point is a character point then its index is the length if the string-value of the node passed as 
     * an argument; otherwise its index is is the number of children of the given node. 
     *
     * @paran node a node
     * @return a range covering the contents of the node
     */
    public Range getRangeInside(Node node)
    {
        Range range;
        
        if(node.getNodeType()==node.DOCUMENT_NODE)
            range = ((DocumentRange)node).createRange();
        else
            range = ((DocumentRange)node.getOwnerDocument()).createRange();
        
        if(node.getNodeType()==Node.ATTRIBUTE_NODE)
            range.setStart(node.getFirstChild(),0);
        else
            range.setStart(node,0);
        
        switch(node.getNodeType())
        {
            case Node.TEXT_NODE:
            case Node.CDATA_SECTION_NODE:
            case Node.COMMENT_NODE:
            case Node.PROCESSING_INSTRUCTION_NODE:
                range.setEnd(node,node.getNodeValue().length());
                break;
            
            case Node.ATTRIBUTE_NODE:    
                range.setEnd(node.getFirstChild(),node.getNodeValue().length());
                break;
                    
            default:
                range.setEnd(node,node.getChildNodes().getLength());
        }
        
        return range;
    }
}
