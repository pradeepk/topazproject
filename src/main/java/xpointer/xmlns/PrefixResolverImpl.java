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

package xpointer.xmlns;

import java.util.Hashtable;

/**
 * 
 */
public class PrefixResolverImpl implements org.apache.xml.utils.PrefixResolver {

    private Hashtable table = new Hashtable();
    
    public void setNamespace(String prefix,String URI)
    {
        table.put(prefix,URI);
    }
    
    /** Creates new PrefixResolverImpl */
    public PrefixResolverImpl() {
    }

    /**
     * Return the base identifier.
     *
     * @return The base identifier from where relative URIs should be absolutized, or null
     * if the base ID is unknown.
     */
    public String getBaseIdentifier() {
        return null;
    }
    
    /**
     * Given a namespace, get the corrisponding prefix.  This assumes that
     * the PrevixResolver hold's it's own namespace context, or is a namespace
     * context itself.
     *
     * @param prefix The prefix to look up, which may be an empty string ("") for the default Namespace.
     *
     * @return The associated Namespace URI, or null if the prefix
     *        is undeclared in this context.
     */
    public String getNamespaceForPrefix(String prefix) {
        
        String namespace = (String) table.get(prefix);
        
        if(prefix.equals("xml"))
            namespace = "http://www.w3.org/XML/1998/namespace";
        
        return namespace;
    }
    
    /**
     * Given a namespace, get the corrisponding prefix, based on the node context.
     *
     * @param prefix The prefix to look up, which may be an empty string ("") for the default Namespace.
     * @param context The node context from which to look up the URI.
     *
     * @return The associated Namespace URI, or null if the prefix
     *        is undeclared in this context.
     */
    public String getNamespaceForPrefix(String prefix, org.w3c.dom.Node context) {
        
        return getNamespaceForPrefix(prefix);
    }
    
}
