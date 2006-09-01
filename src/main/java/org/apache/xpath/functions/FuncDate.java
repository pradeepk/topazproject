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

package org.apache.xpath.functions;

import org.apache.xpath.XPathContext;
import org.apache.xpath.objects.*;

/**
 *
 * @author  root
 * @version 
 */
public class FuncDate extends FunctionMultiArgs {

    private XPathContext xctxt = null;
    
    /** Creates new FuncDate */
    public FuncDate() {
    }

    public XObject execute(XPathContext xctxt) throws javax.xml.transform.TransformerException
    {
        XDate retval = null;
        this.xctxt = xctxt;
        
        switch(calcArgNum())
        {
            case 1:
                try
                {
                    retval = new XDate(getArg0().execute(xctxt).str());
                }
                catch(java.text.ParseException pe)
                {
                    throw new javax.xml.transform.TransformerException(pe.getMessage());
                }
                break;
                
            case 3:
                retval = new XDate((int)getArg0().execute(xctxt).num(),(int)getArg1().execute(xctxt).num()-1,(int)getArg2().execute(xctxt).num());
                break;
                
            case 6:
                retval = new XDate((int)getArg0().execute(xctxt).num(),(int)getArg1().execute(xctxt).num()-1,(int)getArg2().execute(xctxt).num(),
                                    (int)m_args[0].execute(xctxt).num(),(int)m_args[1].execute(xctxt).num(),m_args[2].execute(xctxt).str());
                break;                    
                                    
            case 7:
                retval = new XDate((int)getArg0().execute(xctxt).num(),(int)getArg1().execute(xctxt).num()-1,(int)getArg2().execute(xctxt).num(),
                                    (int)m_args[0].execute(xctxt).num(),(int)m_args[1].execute(xctxt).num(),(int)m_args[2].execute(xctxt).num(),m_args[3].execute(xctxt).str());
                break;
            
            case 8:
                retval = new XDate((int)getArg0().execute(xctxt).num(),(int)getArg1().execute(xctxt).num()-1,(int)getArg2().execute(xctxt).num(),
                                    (int)m_args[0].execute(xctxt).num(),(int)m_args[1].execute(xctxt).num(),(int)m_args[2].execute(xctxt).num(),(int)m_args[3].execute(xctxt).num(),m_args[4].execute(xctxt).str());
                break;
                
        }
        
        return retval;
    }
    
    private int calcArgNum()
    {
        int numArgs = 0;
        
        if(getArg0()!=null)
            numArgs++;
        if(getArg1()!=null)
            numArgs++;
        if(getArg2()!=null)
            numArgs++;
        
        if(m_args!=null)
        {
            numArgs += m_args.length;
        }
        
        return numArgs;
    }
    
    
}
