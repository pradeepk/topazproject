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

import org.apache.xpath.objects.*;
import org.w3c.dom.traversal.*;
import org.apache.xpath.*;
import xpointer.datatype.*;
import org.w3c.dom.*;
import org.w3c.dom.ranges.*;
import xpointer.parsing.*;
import xpointer.xmlns.*;
import javax.xml.transform.TransformerException;

public class XPointerAPI {

    /** Creates new XPointerAPI */
    public XPointerAPI() {
    }

    /**
     * Evaluates an XPointer expression.
     * If no locations are selected, an empty list is returned.
     *
     * @param contextLocation the location to start searching from
     * @param str the XPointer expression
     * @return a location list containing the selected locations 
     */
    public static LocationList selectLocationList(Node context,String str) throws javax.xml.transform.TransformerException
    {
        LocationListImpl locationListImpl = new LocationListImpl();
        
        
        XObject xobj = XPathAPI.eval(context,str);
        Location loc;
        
        if(xobj instanceof XLocationSet)
        {
            XLocationSet xls = (XLocationSet) xobj;
            xpointer.LocationIterator li = xls.locationSet();
            
            while((loc = li.nextLocation())!=null)
            {
                locationListImpl.addLocation(loc); 
            }
        }
        if(xobj instanceof XNodeSet)
        {
            XNodeSet xns = (XNodeSet)xobj;
            NodeIterator ni = xns.nodeset();
            Node node;
            while((node = ni.nextNode())!=null)
            {
                loc = new Location();
                loc.setType(Location.NODE);
                loc.setLocation(node);
                locationListImpl.addLocation(loc);
            }
        }
        
        return locationListImpl;
    }
    
    /**
     * Evaluates an XPointer expression and returns the first selected location.
     * If the expression does not select any locations, returns null.
     *
     * @param contextLocation the location to start searching from
     * @param str the XPointer expression
     * @return the selected location, null if no locations are selected
     */
    public static Location selectSinlgeLocation(Node context,String str) throws javax.xml.transform.TransformerException
    {
        Location loc = null;
        
        LocationList locationList = selectLocationList(context,str);
        
        if(locationList.getLength()>0)
            loc = locationList.item(0);
        
        return loc;
    }
   
    /**
     * Evaluates a full pointer which consists of one or more pointer parts.
     * Multiple pointers parts are evaluated from left to right, if the scheme identifies no resource 
     * the next is evaluated. The result of the first pointer part whose evaluation succeedes is reported as the 
     * subresource identified by the pointer as a whole.
     *
     * @param context the context node for the pointer evaluation
     * @param fullptr a full pointer
     * @return the list of location selected by the first pointer which succeedes
     */ 
   public static LocationList evalFullptr(Node context,String fullptr) throws javax.xml.transform.TransformerException
   {
       return evalFullptr(context,fullptr,null,null);
   }
    
    /**
     * Evaluates a full pointer which consists of one or more pointer parts.
     * Multiple pointers parts are evaluated from left to right, if the scheme identifies no resource 
     * the next is evaluated. The result of the first pointer part whose evaluation succeedes is reported as the 
     * subresource identified by the pointer as a whole.
     * An application may provide values to be used for here() and origin functions.
     *
     * @param context the context node for the pointer evaluation
     * @param fullptr a full pointer
     * @param here the location returned by the here() function,may be null
     * @param origin the location returned by the origin() function,may be null
     * @return the list of location selected by the first pointer which succeedes
     */
    public static LocationList evalFullptr(Node context,String fullptr,Location here,Location origin) throws javax.xml.transform.TransformerException
    {
        
        LocationList retLocation = null;
        
        SchemeParser sp = new SchemeParser(fullptr);
        
        SchemeList schemeList = sp.getSchemeList();
        
       
        
        for(int i=0;i<schemeList.getLength();i++)
        {           
            Scheme ptrpart = schemeList.item(i);
            
            XObject xobj = null;
            
            switch(ptrpart.getType())
            {
                case Scheme.XPOINTER_SCHEME:
                {
                    /*xmlns scheme handling*/
                    PrefixResolverImpl prefixResolverImpl = new PrefixResolverImpl();
                    SchemeList namespaceList = sp.getXmlNSSchemeList((XPointerScheme)ptrpart);           
                    for(int j=0; j<namespaceList.getLength();j++)
                    {
                        XmlNSScheme xmlNSScheme = (XmlNSScheme) namespaceList.item(j);
                        prefixResolverImpl.setNamespace(xmlNSScheme.getPrefix(),xmlNSScheme.getNamespaceURI());
                    }
                    
                    String schemedata = ptrpart.getValue();
                    xobj = XPathAPI.eval(context,schemedata,prefixResolverImpl,here,origin);
                    break;
                }
                case Scheme.ELEMENT_SCHEME:
                {
                    ElementScheme es = (ElementScheme)ptrpart;
                    xobj = XPathAPI.eval(context,es.getXPathExpression(),here,origin);
                    break;
                }
                case Scheme.SHORTHAND:
                {
                    ShortHand sh = (ShortHand)ptrpart;
                    xobj = XPathAPI.eval(context,sh.getXPathExpression());
                    break;
                }
            }
            
            if(xobj!=null)
            {
                retLocation = buildLocationList(xobj);
                if(retLocation.getLength()>0)
                    break;
            }
        }
        
        if(retLocation.getLength()==0)
            throw new TransformerException("Subresource Error: empty location-set");
        
        return retLocation;
    }
    
    private static LocationList buildLocationList(XObject xobj)
    {
        LocationListImpl locationListImpl = new LocationListImpl();
        Location loc;
        
        if(xobj instanceof XLocationSet)
        {
            XLocationSet xls = (XLocationSet) xobj;
            xpointer.LocationIterator li = xls.locationSet();
            
            while((loc = li.nextLocation())!=null)
            {
                locationListImpl.addLocation(loc); 
            }
        }
        if(xobj instanceof XNodeSet)
        {
            XNodeSet xns = (XNodeSet)xobj;
            NodeIterator ni = xns.nodeset();
            Node node;
            while((node = ni.nextNode())!=null)
            {
                loc = new Location();
                loc.setType(Location.NODE);
                loc.setLocation(node);
                locationListImpl.addLocation(loc);
            }
        }
        
        return locationListImpl;
    }
}
