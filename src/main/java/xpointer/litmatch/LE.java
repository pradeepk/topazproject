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

import java.util.Vector;

/**
 * This class represents a Literal Expression.
 * It is a much simpler implementation of Regular Expressions, in fact
 * strings are matched literally without special characters.
 * It is useful for string-range() function because it matches string literally.
 */
public class LE {

    private String match;
    
    /** Creates new LE 
     * @param match the string to be matched
     */
    public LE(String match) {
        this.match = match;
    }

    /**
     * Searches in the input string all the occurences which
     * match literally this expression.
     * 
     * @return an array of matches
     * @param input the string where matches are looked for
     */
    public LEMatch [] getAllMatches(String input) 
    {
        int index = 0;
        int searchIndex = 0;
        Vector matches = new Vector();
        
        while( (index=input.indexOf(match,searchIndex))!=-1)
        {
            LEMatch leMatch = new LEMatch(index,index+match.length());
            matches.addElement(leMatch);
            searchIndex = index + match.length() + 1;
        }
        
        LEMatch [] retval = new LEMatch[matches.size()];
        
        for(int i=0;i<retval.length;i++)
            retval[i] = (LEMatch)matches.elementAt(i);
        
        return retval;
    }
    
  
}
