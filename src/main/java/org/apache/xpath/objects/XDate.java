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

package org.apache.xpath.objects;

import xpointer.custom.Date;
import java.util.Calendar;

/**
 *
 * @author  root
 * @version 
 */
public class XDate extends XObject {

    protected Date date;
    
    public XDate(Date date)
    {
        this.date = date;
    }
    
    /** Creates new XDate */
    public XDate(int year,int month,int day) {
        date = new Date(year,month,day);
    }

    public XDate(int year,int month,int day,int hour,int minute,String timeZoneID)
    {
        date = new Date(year,month,day,hour,minute,timeZoneID);
    }
    
    public XDate(int year,int month,int day,int hour,int minute,int second,String timeZoneID)
    {
        date = new Date(year,month,day,hour,minute,second,timeZoneID);
    }
    
    public XDate(int year,int month,int day,int hour,int minute,int second,int millisecond,String timeZoneID)
    {
        date = new Date(year,month,day,hour,minute,second,millisecond,timeZoneID);
    }
    
    
    public XDate(String s) throws java.text.ParseException
    {
        date = new Date(s);
    }
    
    public void addDays(int n)
    {
        date.add(java.util.Calendar.DATE,n);
    }
    
    public int subtractDate(XDate xDate)
    {
        return date.subtractDate(xDate.date);
    }
    
    public String str()
    {
        return date.toString();
    }
    
   /**
   * Given a request type, return the equivalent string.
   * For diagnostic purposes.
   *
   * @return type string "#DATE"
   */
    public String getTypeString()
    {
        return "#DATE";
    }
    
    public int getType()
    {
        return CLASS_DATE;
    }

    public boolean greaterThan(XObject obj2) throws javax.xml.transform.TransformerException
    {
        if((obj2 instanceof XDate)==false)
            throw new javax.xml.transform.TransformerException("XObject not comparable to XDate.");
        
        return date.greaterThan(((XDate)obj2).date);
    }
    
    public boolean equals(XObject obj2) throws javax.xml.transform.TransformerException
    {
        if((obj2 instanceof XDate)==false)
            throw new javax.xml.transform.TransformerException("XObject not comparable to XDate.");
        
        return date.equal(((XDate)obj2).date);
    }
    
    public boolean lessThan(XObject obj2) throws javax.xml.transform.TransformerException
    {
        if((obj2 instanceof XDate)==false)
            throw new javax.xml.transform.TransformerException("XObject not comparable to XDate.");
        
        return date.lessThan(((XDate)obj2).date);
 
    }
    
    public boolean greaterThanOrEqual(XObject obj2) throws javax.xml.transform.TransformerException
    {
        if((obj2 instanceof XDate)==false)
            throw new javax.xml.transform.TransformerException("XObject not comparable to XDate.");
        
        return date.greaterThanOrEqual(((XDate)obj2).date);

    }
    
    public boolean lessThanOrEqual(XObject obj2) throws javax.xml.transform.TransformerException
    {
        if((obj2 instanceof XDate)==false)
            throw new javax.xml.transform.TransformerException("XObject not comparable to XDate.");
        
        return date.lessThanOrEqual(((XDate)obj2).date);
 
    }
    
    public void addDuration(XDuration duration)
    {
        date.add(Calendar.YEAR,duration.getYear());
        date.add(Calendar.MONTH,duration.getMonth());
        date.add(Calendar.DATE,duration.getDay());
        date.add(Calendar.HOUR,duration.getHour());
        date.add(Calendar.MINUTE,duration.getMinute());
        date.add(Calendar.SECOND,duration.getSecond());
        date.add(Calendar.MILLISECOND,duration.getMillisecond());
    }
    
    public void subtractDuration(XDuration duration)
    {
        date.add(Calendar.YEAR,-1*duration.getYear());
        date.add(Calendar.MONTH,-1*duration.getMonth());
        date.add(Calendar.DATE,-1*duration.getDay());
        date.add(Calendar.HOUR,-1*duration.getHour());
        date.add(Calendar.MINUTE,-1*duration.getMinute());
        date.add(Calendar.SECOND,-1*duration.getSecond());
        date.add(Calendar.MILLISECOND,-1*duration.getMillisecond());
    }
}
