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
import javax.xml.transform.TransformerException;

/**
 * This class represents the endpoint() XPointer function.
 */
public class EndPoint {

    /** Creates new EndPoint */
    public EndPoint() {
    }

    /**
     * Creates the end-point of the given generic location.
     *
     * @param location a generic location
     * @return a collapsed range corresponding to the end point of the given location  
     */
    public Range getEndPoint(Location location) throws TransformerException
    {
        if(location.getType()==Location.NODE)
            
            return getEndPoint((Node)location.getLocation());
        
        else
            
            return getEndPoint((Range)location.getLocation());
    }
    
    /**
     * Creates the end-point of the given range.
     * 
     * 
     * @param range a range
     * @return the collapsed range corresponding to the end point of the given range
     */
    public Range getEndPoint(Range range)
    {
        Node node = range.getStartContainer();
        DocumentRange docRange;
        Range retval;
        
        if(node.getNodeType()==Node.DOCUMENT_NODE)
            docRange = (DocumentRange) node;
        else
            docRange = (DocumentRange) node.getOwnerDocument();
        
        retval = docRange.createRange();
     
        retval.setStart(range.getEndContainer(),range.getEndOffset());
        retval.setEnd(range.getEndContainer(),range.getEndOffset());
        
        return retval;
    }
    
    /**
     * Creates the end-point of the given node.
     * 
     * If the given node is of type root or element, the container node of the resulting point is the given node and
     * the index is the number of children of the given node.
     * If the given node is of type text, comment or processing istruction, the container node of the resulting 
     * point is the given node and the index is the length of the string-value of the given node.
     * If the given node is of type attribute or namespace, the method returns null.
     * 
     * @param node a node
     * @return the collapsed range corresponding to the end point of the given node, null if
     * the given node is of type attribute
     */
    public Range getEndPoint(Node node) throws TransformerException
    {
        Range range;
        
        if(node.getNodeType()==Node.DOCUMENT_NODE)
            range = ((DocumentRange)node).createRange();
        else        
            range = ((DocumentRange)node.getOwnerDocument()).createRange();
        
        switch(node.getNodeType())
        {
            case Node.ELEMENT_NODE:
            case Node.DOCUMENT_NODE:
                range.setStart(node,node.getChildNodes().getLength());
                range.setEnd(node,node.getChildNodes().getLength());
                break;
                
            case Node.ATTRIBUTE_NODE:
                throw new TransformerException("Subresource Error: endpoint argument is of type attribute or namespace");
                
                
            default:
                range.setStart(node,node.getNodeValue().length());
                range.setEnd(node,node.getNodeValue().length());
        }
        
        return range;
    }
}
