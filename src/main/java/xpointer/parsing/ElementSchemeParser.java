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

package xpointer.parsing;

import java.util.*;

/**
 * This class parses the schemedata of the element() scheme.
 * The expression must conform to the following syntax:
 *
 * elementschemedata ::= (Name, childseq) | childseq
 * childseq ::= ('/' [1-9] [0-9]* )+
 *
 * A Token queue is created; the first token can be a String object (corresponding 
 * to the Name token) and the following tokens are Integer objects.
 */
public class ElementSchemeParser {

    private String globalExpr;
    private Object [] token_queue;
    
    /** Creates new ElementSchemeParser 
     * @param schemedata the schemedata of element scheme 
     */
    public ElementSchemeParser(String schemedata) {
        this.globalExpr = schemedata;
        
        if(schemedata.equals(""))
            throw new RuntimeException("Syntax Error");
        
        tokenize();
    }

    /**
     * Builds the token queue.
     */
    private void tokenize()
    {
        StringTokenizer st = new StringTokenizer(globalExpr,"/",true);
        String Name = null;
        /*true if the previous token is '/' */
        boolean previousSeparator = false;
        /* true only at the first iteration of the while loop*/
        boolean expectName = true;
        String token = null;
        Vector numbers = new Vector();
        
        while(st.hasMoreTokens())
        {
            token = st.nextToken();
            
            if(expectName && token.equals("/")==false)
            {
                Name = token;
            }   
            else
                /*this avoids two '/' in a row*/
                if(token.equals("/"))
                    if(previousSeparator==false)
                    {
                        previousSeparator = true;
                        expectName = false;
                        continue;
                    }
                    else
                        throw new RuntimeException("Syntax Error");
                else
                    if(isNum(token))
                    {
                        numbers.addElement(new Integer(token));
                        previousSeparator = false;
                    }
                    else
                        throw new RuntimeException("Syntax Error");
            
            expectName = false;
        }
        
        if(token.equals("/"))
            throw new RuntimeException("Syntax Error");
        
        if(Name!=null)
        {
            token_queue = new Object[1+numbers.size()];
            token_queue[0] = Name;
            
            for(int i=1;i<token_queue.length;i++)
                token_queue[i] = numbers.elementAt(i-1);

        }
        else
        {
            token_queue = new Object[numbers.size()];
            for(int i=0;i<token_queue.length;i++)
                token_queue[i] = numbers.elementAt(i);

        }
        
            }
    
            
    /**
     * Verifies if a string is numerical and the first digit is not zero.  
     *
     * @return true if the string passed as an argument has the form [1-9][0-9]*,false otherwise
     * @param num the string to be analysed
     */        
    private boolean isNum(String num)
    {
        if(num.charAt(0)=='0')
            return false;
        
        for(int i=0;i<num.length();i++)
            if(num.charAt(i)<'0' || num.charAt(i)>'9')
                return false;
        
        return true;
    }
    
    /**
     * Returns the token queue created after the parsing of the schemedata of the element() scheme.
     *
     * @return the token queue
     */
    public Object[] getTokenQueue()
    {
        return token_queue;
    }
    
    
}
