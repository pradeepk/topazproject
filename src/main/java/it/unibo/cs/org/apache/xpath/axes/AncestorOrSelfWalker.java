/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights 
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer. 
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:  
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Xalan" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written 
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation and was
 * originally based on software copyright (c) 1999, Lotus
 * Development Corporation., http://www.lotus.com.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package it.unibo.cs.org.apache.xpath.axes;

import java.util.Stack;

import it.unibo.cs.org.apache.xpath.axes.LocPathIterator;
import it.unibo.cs.org.apache.xpath.XPath;
import it.unibo.cs.org.apache.xpath.XPathContext;
import it.unibo.cs.org.apache.xpath.DOMHelper;

import org.w3c.dom.Node;

/**
 * Walker for the 'ancestor-or-self' axes.
 * @see <a href="http://www.w3.org/TR/xpath#axes">XPath axes descriptions</a>
 */
public class AncestorOrSelfWalker extends AncestorWalker
{

  /**
   * Construct an AncestorOrSelfWalker using a LocPathWalker.
   *
   * @param locPathIterator The location path iterator that 'owns' this walker.
   */
  public AncestorOrSelfWalker(LocPathIterator locPathIterator)
  {
    super(locPathIterator);
  }

  /**
   * Push the ancestor nodes.
   *
   * @param n The context node.
   */
  protected void pushAncestors(Node n)
  {

    m_ancestors = new Stack();

    m_ancestors.push(n);

    DOMHelper dh = m_lpi.getDOMHelper();

    while (null != (n = dh.getParentOfNode(n)))
    {
      m_ancestors.push(n);
    }

    m_nextLevelAmount = m_ancestors.isEmpty() ? 0 : 1;
    m_ancestorsPos = m_ancestors.size() - 1;
  }

  /**
   * Tell what's the maximum level this axes can descend to.
   *
   * @return An estimation of the maximum level this axes can descend to.
   */
  protected int getLevelMax()
  {
    return m_lpi.getDOMHelper().getLevel(m_root);
  }
  
  /*vera se il punto contenuto nell'asse e' gia' stato ritornato*/
  private boolean m_pointReturned = false;
  
  /**
   * Questo asse contiene (nel caso la locazione contesto sia un punto)il nodo contenitore del punto, gli avi del nodo contenitore del punto
   * ed il punto stesso.
   * Per primo va restituito il punto,poi i nodi
  */
  public it.unibo.cs.xpointer.Location getNextLocation()
  {
    if(m_pointReturned==false && m_currentLoc!=null && m_currentLoc.getType()==it.unibo.cs.xpointer.Location.RANGE)
    {
        m_pointReturned = true;
        return m_currentLoc;
    }
    
    it.unibo.cs.xpointer.Location loc = null;
    Node node = super.getNextNode();
    
    if(node!=null)
    {
        loc = new it.unibo.cs.xpointer.Location();
        loc.setType(it.unibo.cs.xpointer.Location.NODE);
        loc.setLocation(node);
    }
    
    return loc;
  }
}
