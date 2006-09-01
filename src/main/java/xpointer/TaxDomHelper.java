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

import org.apache.xpath.DOMHelper;
import org.w3c.dom.Node;
import org.w3c.dom.ranges.Range;
import xpointer.XPRange;

/**
 * The DOMHelper of the xpath package provides some useful operations
 * that act on nodes.
 * The purpose of this class is providing the same operations for
 * locations.
 * A better name of this class should be ExtDomHelper.
 */
public class TaxDomHelper {

    private DOMHelper domHelper;
    
    /** Creates new TaxDomHelper 
     * @param domHelper the standard DOMHelper
     */
    public TaxDomHelper(DOMHelper domHelper) {
        this.domHelper = domHelper;
    }

    /**
     * Figures out whether loc2 should be considered as being later 
     * in the document than loc1, in Document Order ad defined 
     * in XPointer CR.
     *
     * @param loc1 the location to perform position comparison on
     * @param loc2 the location to perform position comparison on
     * @return false if loc2 comes before loc1,otherwise returns true
     */
    public boolean isLocationAfter(Location loc1,Location loc2)
    {
        XPRange xpRange1,xpRange2;
        Range range1,range2;
        Node startNode1,startNode2,endNode1,endNode2;
        int startIndex1,startIndex2,endIndex1,endIndex2;
        
        xpRange1 = new XPRange();
        xpRange2 = new XPRange();
        range1 = xpRange1.getRange(loc1);
        range2 = xpRange2.getRange(loc2);
        startNode1 = range1.getStartContainer();
        startNode2 = range2.getStartContainer();
        endNode1 = range1.getEndContainer();
        endNode2 = range2.getEndContainer();
        startIndex1 = range1.getStartOffset();
        startIndex2 = range2.getStartOffset();
        endIndex1 = range1.getEndOffset();
        endIndex2 = range2.getEndOffset();   
     
        if(startNode1!=startNode2)
            return domHelper.isNodeAfter(startNode1,startNode2);
        else
        {
            if(startIndex1>startIndex2)
                return false;
            else
            {
                if(startIndex1<startIndex2)
                    return true;
                else
                {
                    if(endNode1!=endNode2)
                        return domHelper.isNodeAfter(endNode1,endNode2);
                    else
                        if(endIndex1>endIndex2)
                            return false;
                        else
                            return true;
                }
            }           
        }
    }
}
