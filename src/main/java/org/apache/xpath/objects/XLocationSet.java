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

package org.apache.xpath.objects;

import org.apache.xpath.LocationSet;
import xpointer.Location;
import xpointer.LocationIterator;
import org.apache.xml.utils.StringVector;
import org.w3c.dom.ranges.Range;
import org.w3c.dom.*;
import org.apache.xpath.DOMHelper;
import org.w3c.dom.traversal.NodeIterator;

/**
 * This class represents an XPointer location-set object and
 * is capable of converting the location-set to other types.
 */
public class XLocationSet extends XObject {

    private XNodeSet nodeset = null;
    
    /** 
     * Creates an empty XLocationSet 
     */
    public XLocationSet() {
        super(new LocationSet());
        
    }

    /**
     * Constructs a XLocationSet for one location
     *
     * @param loc location to add to the new XLocationSet object 
     */
    public XLocationSet(Location loc)
    {
        super(new LocationSet());
        
        ((LocationSet)m_obj).addLocation(loc);
    }
    
    /**
     * Constructs a XLocationSet object
     *
     * @param li Value of the XLocationSet object
     */
    public XLocationSet(LocationIterator li)
    {
        super(li);
    }
    
    public int getType()
    {
        return CLASS_LOCATIONSET;
    }
    
    public LocationSet mutableLocationSet()
    {
        LocationSet mls;
        
        mls = (LocationSet) m_obj;
        
        return mls;
           
    }
    
    /**
     * 
     */
    public LocationIterator locationSet()
    {
        return (LocationIterator) m_obj;
    }
    
    /**
     * Given a request type, return the equivalent string.
     * For diagnostic purposes.
     *
     */
    public String getTypeString() {
        String retValue;
        
        retValue = "#LOCATIONSET";
        return retValue;
    }
    
    /**
     * Cast result object to a boolean
     */
    public boolean bool()
    {
        return (locationSet().nextLocation()!=null);
    }
    

    public String str()
    {
        Location loc = locationSet().nextLocation();
        
        if(loc==null)
            return "";
        
        if(loc.getType()==Location.NODE)
            return XNodeSet.getStringFromNode((Node)loc.getLocation());
        else
            return ((Range)loc.getLocation()).toString();
    }
    
  /**
   * Tell if one object is less than the other.
   *
   * @param obj2 Object to compare this locationset to
   * @param comparator Comparator to use
   *
   */    
  public boolean compare(XObject obj2, Comparator comparator) throws javax.xml.transform.TransformerException
  {
    boolean retval = false;
    int type = obj2.getType();

    if(type==XObject.CLASS_LOCATIONSET)
    {
        LocationIterator list1 = locationSet();
        LocationIterator list2 = ((XLocationSet)obj2).locationSet();
        Location loc1,loc2;
        StringVector loc2Strings = new StringVector();
        
        while(null!=(loc2=list2.nextLocation()))
            loc2Strings.addElement(getStringFromLocation(loc2));
        
        while(null!=(loc1=list1.nextLocation()))
        {
            String s1 = getStringFromLocation(loc1);
            
            for(int i=0;i<loc2Strings.size();i++)
                if(comparator.compareStrings(s1,loc2Strings.elementAt(i)))
                {
                    retval = true;
                    break;
                }
            
            if(retval==true)
                break;
        }
    }
    else
        if(type==XObject.CLASS_NODESET)
        {
            LocationIterator list1 = locationSet();
            NodeIterator list2 = ((XNodeSet)obj2).nodeset();
            Node node2;
            Location loc1;
            StringVector node2Strings = new StringVector();
            
            while(null!=(node2=list2.nextNode()))
                node2Strings.addElement(getStringFromNode(node2));
            
            while(null!=(loc1=list1.nextLocation()))
            {
                String s1 = getStringFromLocation(loc1);
                
                for(int i=0;i<node2Strings.size();i++)
                    if(comparator.compareStrings(s1,node2Strings.elementAt(i)))
                    {
                        retval = true;
                        break;
                    }
                
                if(retval==true)
                    break;
            }
        }
        else
            if(type==XObject.CLASS_BOOLEAN)
            {
                double num1 = bool() ? 1.0 : 0.0;
                double num2 = obj2.num();

                retval = comparator.compareNumbers(num1, num2);
            }
            else
                if(type==XObject.CLASS_NUMBER)
                {
                    LocationIterator list1 = locationSet();
                    double num2 = obj2.num();
                    Location loc1;
                    
                    while(null!=(loc1=list1.nextLocation()))
                    {
                        double num1 = getNumberFromLocation(loc1);
                        
                        if(comparator.compareNumbers(num1,num2));
                        {
                            retval = true;
                            break;
                        }
                    }
                    
                }
                else
                    if(type==XObject.CLASS_STRING)
                    {
                        String s2 = obj2.str();
                        LocationIterator list1 = locationSet();
                        Location loc1;
                        
                        while(null!=(loc1=list1.nextLocation()))
                        {
                            String s1 = getStringFromLocation(loc1);
                            
                            if(comparator.compareStrings(s1,s2))
                            {
                                retval = true;
                                break;
                            }
                        }
                        
                    }
                    else
                    {
                        retval = comparator.compareNumbers(this.num(),obj2.num());
                    }
    
    return retval;
  }
   
  /**
   * Tell if two objects are functionally equal.
   *
   * @param obj2 object to compare this nodeset to
   *
   * @return see this.compare(...) 
   *
   * @throws javax.xml.transform.TransformerException
   */
  public boolean equals(XObject obj2) throws javax.xml.transform.TransformerException
  {
    return compare(obj2, new EqualComparator());
  }

  /**
   * Tell if one object is less than the other.
   *
   * @param obj2 object to compare this locationset to
   *
   * @return see this.compare(...) 
   *
   * @throws javax.xml.transform.TransformerException
   */
  public boolean greaterThan(XObject obj2) throws javax.xml.transform.TransformerException
  {
    return compare(obj2, new GreaterThanComparator());
  }

   /**
   * Tell if one object is less than the other.
   *
   * @param obj2 object to compare this locationset to
   *
   * @return see this.compare(...) 
   *
   * @throws javax.xml.transform.TransformerException
   */
  public boolean greaterThanOrEqual(XObject obj2)
          throws javax.xml.transform.TransformerException
  {
    return compare(obj2, new GreaterThanOrEqualComparator());
  }
  
   /**
   * Tell if one object is less than the other.
   *
   * @param obj2 object to compare this locationset to
   *
   * @return see this.compare(...) 
   *
   * @throws javax.xml.transform.TransformerException
   */
  public boolean lessThan(XObject obj2) throws javax.xml.transform.TransformerException
  {
    return compare(obj2, new LessThanComparator());
  }

   /**
   * Tell if one object is less than or equal to the other.
   *
   * @param obj2 object to compare this locationset to
   *
   * @return see this.compare(...) 
   *
   * @throws javax.xml.transform.TransformerException
   */
  public boolean lessThanOrEqual(XObject obj2) throws javax.xml.transform.TransformerException
  {
    return compare(obj2, new LessThanOrEqualComparator());
  }
  
  /**
   * Cast result object to a number.
   *
   * @return numeric value of the string conversion from the 
   * next location in the LocationSet, or NAN if no node was found
   */
  public double num()
  {
    LocationIterator li = locationSet();
    Location loc = li.nextLocation();
    
    return (loc!=null) ? getNumberFromLocation(loc): Double.NaN ;
  }
  
  private String getStringFromLocation(Location loc)
  {
      if(loc.getType()==Location.NODE)
          return getStringFromNode((Node)loc.getLocation());
      else
      {
        return ((Range)loc.getLocation()).toString();
      }    
  }
  
  /**
   * Get the string conversion from a single node.
   *
   * @param n Node to convert
   *
   * @return the string conversion from a single node.
   */
  private String getStringFromNode(Node n)
  {

    switch (n.getNodeType())
    {
    case Node.ELEMENT_NODE :
    case Node.DOCUMENT_NODE :
      return DOMHelper.getNodeData(n);
    case Node.CDATA_SECTION_NODE :
    case Node.TEXT_NODE :
      return ((Text) n).getData();
    case Node.COMMENT_NODE :
    case Node.PROCESSING_INSTRUCTION_NODE :
    case Node.ATTRIBUTE_NODE :
      return n.getNodeValue();
    default :
      return DOMHelper.getNodeData(n);
    }
  }
  
   /**
   * Get numeric value of the string conversion from a single node.
   *
   * @param n Node to convert
   *
   * @return numeric value of the string conversion from a single node.
   */
  private double getNumberFromNode(Node n)
  {
    return XString.castToNum(getStringFromNode(n));
  }
  
  private double getNumberFromLocation(Location loc)
  {
    return XString.castToNum(getStringFromLocation(loc));
  }
  
/**
 * compares locations for various boolean operations.
 */
abstract class Comparator
{

  /**
   * Compare two strings
   *
   *
   * @param s1 First string to compare
   * @param s2 Second String to compare 
   *
   * @return Whether the strings are equal or not
   */
  abstract boolean compareStrings(String s1, String s2);

  /**
   * Compare two numbers
   *
   *
   * @param n1 First number to compare
   * @param n2 Second number to compare
   *
   * @return Whether the numbers are equal or not
   */
  abstract boolean compareNumbers(double n1, double n2);
}

/**
 * Compare strings or numbers for less than.
 */
class LessThanComparator extends Comparator
{

  /**
   * Compare two strings for less than.
   *
   *
   * @param s1 First string to compare
   * @param s2 Second String to compare 
   *
   * @return True if s1 is less than s2
   */
  boolean compareStrings(String s1, String s2)
  {
    return s1.compareTo(s2) < 0;
  }

  /**
   * Compare two numbers for less than.
   *
   *
   * @param n1 First number to compare
   * @param n2 Second number to compare
   *
   * @return true if n1 is less than n2
   */
  boolean compareNumbers(double n1, double n2)
  {
    return n1 < n2;
  }
}

/**
 * Compare strings or numbers for less than or equal.
 */
class LessThanOrEqualComparator extends Comparator
{

  /**
   * Compare two strings for less than or equal.
   *
   *
   * @param s1 First string to compare
   * @param s2 Second String to compare
   *
   * @return true if s1 is less than or equal to s2
   */
  boolean compareStrings(String s1, String s2)
  {
    return s1.compareTo(s2) <= 0;
  }

  /**
   * Compare two numbers for less than or equal.
   *
   *
   * @param n1 First number to compare
   * @param n2 Second number to compare
   *
   * @return true if n1 is less than or equal to n2
   */
  boolean compareNumbers(double n1, double n2)
  {
    return n1 <= n2;
  }
}

/**
 * Compare strings or numbers for greater than.
 */
class GreaterThanComparator extends Comparator
{

  /**
   * Compare two strings for greater than.
   *
   *
   * @param s1 First string to compare
   * @param s2 Second String to compare
   *
   * @return true if s1 is greater than s2
   */
  boolean compareStrings(String s1, String s2)
  {
    return s1.compareTo(s2) > 0;
  }

  /**
   * Compare two numbers for greater than.
   *
   *
   * @param n1 First number to compare
   * @param n2 Second number to compare
   *
   * @return true if n1 is greater than n2
   */
  boolean compareNumbers(double n1, double n2)
  {
    return n1 > n2;
  }
}

/**
 * Compare strings or numbers for greater than or equal.
 */
class GreaterThanOrEqualComparator extends Comparator
{

  /**
   * Compare two strings for greater than or equal.
   *
   *
   * @param s1 First string to compare
   * @param s2 Second String to compare
   *
   * @return true if s1 is greater than or equal to s2
   */
  boolean compareStrings(String s1, String s2)
  {
    return s1.compareTo(s2) >= 0;
  }

  /**
   * Compare two numbers for greater than or equal.
   *
   *
   * @param n1 First number to compare
   * @param n2 Second number to compare
   *
   * @return true if n1 is greater than or equal to n2
   */
  boolean compareNumbers(double n1, double n2)
  {
    return n1 >= n2;
  }
}

/**
 * Compare strings or numbers for equality.
 */
class EqualComparator extends Comparator
{

  /**
   * Compare two strings for equality.
   *
   *
   * @param s1 First string to compare
   * @param s2 Second String to compare
   *
   * @return true if s1 is equal to s2
   */
  boolean compareStrings(String s1, String s2)
  {
    return s1.equals(s2);
  }

  /**
   * Compare two numbers for equality.
   *
   *
   * @param n1 First number to compare
   * @param n2 Second number to compare
   *
   * @return true if n1 is equal to n2
   */
  boolean compareNumbers(double n1, double n2)
  {
    return n1 == n2;
  }
}

/**
 * Compare strings or numbers for non-equality.
 */
class NotEqualComparator extends Comparator
{

  /**
   * Compare two strings for non-equality.
   *
   *
   * @param s1 First string to compare
   * @param s2 Second String to compare
   *
   * @return true if s1 is not equal to s2
   */
  boolean compareStrings(String s1, String s2)
  {
    return !s1.equals(s2);
  }

  /**
   * Compare two numbers for non-equality.
   *
   *
   * @param n1 First number to compare
   * @param n2 Second number to compare
   *
   * @return true if n1 is not equal to n2
   */
  boolean compareNumbers(double n1, double n2)
  {
    return n1 != n2;
  }
}
}
