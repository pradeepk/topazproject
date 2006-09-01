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

import java.util.Calendar;

/**
 *
 * @author  root
 * @version 
 */
public class Today extends Date {

    /** Creates new Today */
    public Today() {
        
        gregorianCalendar.set(Calendar.HOUR_OF_DAY,0);
        gregorianCalendar.set(Calendar.MINUTE,0);
        gregorianCalendar.set(Calendar.SECOND,0);
        gregorianCalendar.set(Calendar.MILLISECOND,0);
        
    }

}
