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

package xpointer.datatype;

/**
 * This class represents a scheme with its schemedata.
 *
 */
public class Scheme {

    private int type;
    
    private String value;
    
    public static final int XPOINTER_SCHEME = 2;
    public static final int ELEMENT_SCHEME = 0;
    public static final int XMLNS_SCHEME = 1;
    public static final int SHORTHAND = 3;
    
    /** Creates new Scheme */
    public Scheme() {
    }

    /**
     * Creates new Scheme
     * @param the scheme value
     * @param the scheme type (xmlns,xpointer or element)
     */
    public Scheme(String value,int type)
    {
        this.value = value;
        this.type = type;
    }
    
    /** Getter for property type.
     * @return Value of property type.
     */
    public int getType() {
        return type;
    }    
    
    /** Setter for property type.
     * @param type New value of property type.
     */
    public void setType(int type) {
        this.type = type;
    }
    
    /** Getter for property value.
     * @return Value of property value.
     */
    public java.lang.String getValue() {
        return value;
    }
    
    /** Setter for property value.
     * @param value New value of property value.
     */
    public void setValue(java.lang.String value) {
        this.value = value;
    }
    
}
