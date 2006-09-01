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


package xpointer.custom;

import java.util.*;
import java.text.SimpleDateFormat;

/**
 *
 * @author  root
 * @version 
 */
public class Date {

    protected GregorianCalendar gregorianCalendar;
    
    protected Date()
    {   
        gregorianCalendar = new GregorianCalendar();
    }
    
    /** Creates new Date */
    public Date(String s) throws java.text.ParseException{
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        
        gregorianCalendar = new GregorianCalendar();
        gregorianCalendar.setTime(sdf.parse(s));
       
    }

        
    private void init(int year,int month,int day,int hour,int minute,int second,int millisec,String timeZoneID)
    {
        gregorianCalendar = new GregorianCalendar(year,month,day,hour,minute,second);
        
        gregorianCalendar.set(Calendar.MILLISECOND,millisec);
        
        if(timeZoneID!=null)
        {
            TimeZone tz = TimeZone.getTimeZone(timeZoneID);
            gregorianCalendar.setTimeZone(tz);
        }
    }
    
    public Date(int year,int month,int day,int hour,int minute,int second,int millisec,String timeZoneID)
    {
        init(year,month,day,hour,minute,second,millisec,timeZoneID);
    }
    
    public Date(int year,int month,int day)
    {
        init(year,month,day,0,0,0,0,null);
    }
    
    public Date(int year,int month,int day,int hour,int minute,String timeZoneID)
    {
        init(year,month,day,hour,minute,0,0,timeZoneID);
    }
    
    public Date(int year,int month,int day,int hour,int minute,int second,String timeZoneID)
    {
        init(year,month,day,hour,minute,second,0,timeZoneID);
    }
    
    public String toString()
    {
        return gregorianCalendar.getTime().toString();
    }
    
    public void add(int field,int amount)
    {
        gregorianCalendar.add(field,amount);
    }
    
    public int subtractDate(Date subtDate)
    {
        GregorianCalendar subtGC = subtDate.gregorianCalendar;
        GregorianCalendar major,minor;
        int sign;
        int numdays = 0;
        
        if(subtGC.getTime().getTime()<gregorianCalendar.getTime().getTime())
        {
            major = gregorianCalendar;
            minor = subtGC;
            sign = 1;
        }
        else
        {
            major = subtGC;
            minor = gregorianCalendar;
            sign = -1;
        }
        
        while(equalDate(minor,major)==false)
        {
            minor.add(Calendar.DATE,1);
            numdays++;
        }
        
        return sign*numdays;
    }
    
    private boolean equalDate(GregorianCalendar gc1,GregorianCalendar gc2)
    {
        if(gc1.get(Calendar.DATE)!=gc2.get(Calendar.DATE))
            return false;
        
        if(gc1.get(Calendar.MONTH)!=gc2.get(Calendar.MONTH))
            return false;
        
        if(gc1.get(Calendar.YEAR)!=gc1.get(Calendar.YEAR))
            return false;
        
        return true;
    }
    
    public boolean greaterThan(Date date2)
    {
        return (gregorianCalendar.getTime().getTime()>date2.gregorianCalendar.getTime().getTime());
    }
    
    public boolean lessThan(Date date2)
    {
        return (gregorianCalendar.getTime().getTime()<date2.gregorianCalendar.getTime().getTime());
    }
    
    public boolean equal(Date date2)
    {
        return (gregorianCalendar.getTime().getTime()==date2.gregorianCalendar.getTime().getTime());
    }
        
    public boolean greaterThanOrEqual(Date date2)
    {
        return (gregorianCalendar.getTime().getTime()>=date2.gregorianCalendar.getTime().getTime());
    }
    
    public boolean lessThanOrEqual(Date date2)
    {
        return (gregorianCalendar.getTime().getTime()<=date2.gregorianCalendar.getTime().getTime());
    }
}
