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
import xpointer.custom.Now;
import org.apache.xpath.objects.*;

/**
 *
 * @author  root
 * @version 
 */
public class FuncNow extends Function {

    /** Creates new FuncNow */
    public FuncNow() {
    }

    public XObject execute(XPathContext xctxt) throws javax.xml.transform.TransformerException
    {
        return new XDate(new Now());
    }
}
