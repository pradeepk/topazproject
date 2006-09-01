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
package xpointer.litmatch;

/**
 * An instance of this class represents a match completed by a LE matching function.
 * It can be used to obtaion relevant information about the location of a match. 
 * 
 */
public class LEMatch {

    private int startIndex;
    private int endIndex;
    
    /** Creates new LEMatch */
    LEMatch() {
    }

    /**
     * Creates new LEMatch
     * @param startIndex the start index of the matched string in the original expression
     * @param endIndex the end index of the matched string in the original expression
     */
    LEMatch(int startIndex,int endIndex)
    {
        this.startIndex = startIndex;
        this.endIndex = endIndex;
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
    void setEndIndex(int endIndex) {
        this.endIndex = endIndex;
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
    void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }
    
}
