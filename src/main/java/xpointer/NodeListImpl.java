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
import java.util.Vector;

/**
 * This is just the implementation of the NodeList interface.
 */

public class NodeListImpl implements org.w3c.dom.NodeList {

    Vector v = new Vector();
    
    /**
     * Adds a node to the list. 
     */
    protected void addNode(Node node)
    {
        v.addElement(node);
    }
    
    public NodeListImpl() {
    }

    public int getLength() {
        return v.size();
    }
    
    public org.w3c.dom.Node item(int param) {
        return (Node)v.elementAt(param);
    }
    
}
