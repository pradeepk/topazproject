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

import gnu.regexp.UncheckedRE;

/**
 * Given an expression, the lexer builds 
 * an array of tokens.
 * The array is made up of couples: the first element identifies the scheme type,
 * the second element contains the schemedata.
 */
public class Lexer {

    private String globalExpr;
    private Object [] token_queue;
    private final int TOKEN_QUEUE_LEN = 255;
    private int nextTokenPos = 0;
    
    /*true if a shorthand can be found*/
    private boolean possibleShortHand = true; 
    
    /** 
     * Creates new Lexer 
     * @param  the string to be analayzed
     */
    public Lexer(String expr) {
        globalExpr = expr;
        token_queue = new Object[TOKEN_QUEUE_LEN];
    }

    /**
     * Tokenize an expression.
     */
    public void analyzeExpression()
    {
        int index = 0;
        
        /*remove starting white spaces*/
        while(globalExpr.charAt(index)==' ')
            index++;
        
        while(index<globalExpr.length())
        {
            index = analyzeSinglePointer(index);
            
            if(index<globalExpr.length() &&  globalExpr.charAt(index)==' ')
                index++;
        }
    }
    
    /**
     * @param offset the starting index of a pointer
     * @return the position after the analyzed pointer 
     */
    private int analyzeSinglePointer(int offset)
    {
        int index = offset;
        String currentExpr = "";
        int startSchemedata,endSchemedata;
        int numpar = 1;
        
        while(globalExpr.charAt(index)!='(')
        {
            currentExpr = currentExpr + globalExpr.charAt(index);
            index++;
            
            if(index >= globalExpr.length())
                if(possibleShortHand)
                {
                    handleShortHand();
                    return globalExpr.length();
                }
                else
                    throw new RuntimeException("Syntax Error");
        }
        
        int i;
        
        for(i=0;i<SchemeParser.schemeNames.length;i++)
        {
            if(currentExpr.equals(SchemeParser.schemeNames[i]))
            {
                token_queue[nextTokenPos] = new Integer(i);
                
                possibleShortHand = false;
                
                nextTokenPos++;
                
                break;
            }
        }
        
        if(i==SchemeParser.schemeNames.length)
        {
            if(possibleShortHand==false)
                throw new RuntimeException("Syntax Error");
            
            handleShortHand();
            return globalExpr.length();
        }
        
        startSchemedata = index + 1;
        endSchemedata = startSchemedata;
        boolean escaping = false;
        
        
        while(numpar!=0 && endSchemedata<globalExpr.length())
        {
            if(globalExpr.charAt(endSchemedata)=='^')
            {
                if(escaping==false)
                    escaping = true;
                else  //we have ^^
                    escaping = false;
                          
            }
            else if(globalExpr.charAt(endSchemedata)=='(')
            {
                if(escaping==false)
                    numpar++;
                else
                    escaping=false;
            }
            else
            {
                if(globalExpr.charAt(endSchemedata)==')')
                {
                    if(escaping==false)
                        numpar--;
                    else
                        escaping = false;
                }
                else
                {
                    if(escaping)
                        throw new RuntimeException("Unescaped circumflex");
                }
            }
            
            endSchemedata++;
        }
        
        if(numpar!=0)
            throw new RuntimeException("Syntax Error: parenthesis unbalanced");
        
        token_queue[nextTokenPos] = new String(escapeCircumflex(globalExpr.substring(startSchemedata,endSchemedata-1)));
        nextTokenPos++;
        
        return endSchemedata;
    }
    
    Object [] getTokenQueue()
    {
        return token_queue;
    }
    
    /**
     * Makes escaping of unbalanced parenthesis and of double occurences of circumflex.
     * @param param the string where the substitution takes place
     *
     * @return a string where each occurrence of a circumflex used for escaping is erased
     */
    private String escapeCircumflex(String param)
    {
        UncheckedRE regexp = new UncheckedRE("\\^\\^");
        
        String retval = regexp.substituteAll(param,"^");
        
        regexp = new UncheckedRE("\\^\\(");
        retval = regexp.substituteAll(retval,"(");
        
        regexp = new UncheckedRE("\\^\\)");
        retval = regexp.substituteAll(retval,")");
        
        return retval;
    }
    
    private void handleShortHand()
    {
        token_queue[nextTokenPos++] = new Integer(xpointer.datatype.Scheme.SHORTHAND);
        token_queue[nextTokenPos++] = globalExpr;
    }
}
