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

import org.w3c.dom.Node;

/**
 * This class is used when  we need something that is a range with the same start and end container.
 */
public class TextNodeFragment {

    private int startIndex;
    
    private int endIndex;
    
    private Node node;
    
    /** Creates new TextNodeFragment */
    public TextNodeFragment() {
    }

    /** Getter for property endIndex.
     * @return Value of property endIndex.
     */
    public int getEndIndex() {
        return endIndex;
    }
    
    /** Setter for property endIndex.
     * @param endIndex New value of property endIndex.
     */
    public void setEndIndex(int endIndex) {
        this.endIndex = endIndex;
    }
    
    /** Getter for property node.
     * @return Value of property node.
     */
    public org.w3c.dom.Node getNode() {
        return node;
    }
    
    /** Setter for property node.
     * @param node New value of property node.
     */
    public void setNode(org.w3c.dom.Node node) {
        this.node = node;
    }
    
    /** Getter for property startIndex.
     * @return Value of property startIndex.
     */
    public int getStartIndex() {
        return startIndex;
    }
    
    /** Setter for property startIndex.
     * @param startIndex New value of property startIndex.
     */
    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }
    
}
