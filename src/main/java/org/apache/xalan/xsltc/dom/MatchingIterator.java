/*
 * @(#)$Id: MatchingIterator.java 557 2006-09-01 08:31:40Z pradeep $
 *
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
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
 * originally based on software copyright (c) 2001, Sun
 * Microsystems., http://www.sun.com.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 *
 */

package org.apache.xalan.xsltc.dom;

import org.apache.xalan.xsltc.NodeIterator;
import org.apache.xalan.xsltc.runtime.BasisLibrary;

public final class MatchingIterator extends NodeIteratorBase {
    private NodeIterator _source;
    private final int    _match;
    private int _matchPos, _matchLast = -1;
	
    public MatchingIterator(int match, NodeIterator source) {
	_source = source;
	_match = match;
    }

    public NodeIterator cloneIterator() {
	try {
	    final MatchingIterator clone = (MatchingIterator)super.clone();
	    clone._isRestartable = false;
	    clone._source = _source.cloneIterator();
	    return clone;
	}
	catch (CloneNotSupportedException e) {
	    BasisLibrary.runTimeError("Iterator clone not supported."); 
	    return null;
	}
    }
    
    public NodeIterator setStartNode(int node) {
	if (_isRestartable) {
	    // iterator is not a clone
	    _source.setStartNode(_startNode = node);

	    // Calculate the position of the node in the set
	    final int match = _match;
	    int i = 1;
	    while ((node = _source.next()) != END && node != match) {
		++i;
	    }
	    _matchPos = i;
	    _matchLast = -1;
	}
	return this;
    }

    public NodeIterator reset() {
	return this;	// should not be called
    }
    
    public int next() {
	return _source.next();
    }
	
    public int getLast() {
	if (_matchLast == -1) {
	    _source.reset();
	    int i = 1, node;
	    while ((node = _source.next()) != END) {
		++i;
	    }
	    _matchLast = i - 1;
	}
	return _matchLast;
    }

    public int getPosition() {
	return _matchPos;
    }

    public void setMark() {
	_source.setMark();
    }

    public void gotoMark() {
	_source.gotoMark();
    }
}
