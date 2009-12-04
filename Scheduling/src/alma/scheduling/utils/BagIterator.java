/*
 * ALMA - Atacama Large Millimiter Array
 * (c) European Southern Observatory, 2009
 * (c) Associated Universities Inc., 2009
 * Copyright by AUI (in the framework of the ALMA collaboration),
 * All rights reserved
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307  USA
 */
/**
 * Support class to allow iteration over Bags
 * $Id: BagIterator.java,v 1.1 2009/12/04 23:01:56 dclarke Exp $
 * 
 * Author: David Clarke
 */
package alma.scheduling.utils;

import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;

public class BagIterator<E> implements Iterator<E> {
	
	private Iterator<Entry<E, Integer>> delegateIterator;
	private E current;
	private int remaining = 0;

	public BagIterator(Set<Entry<E, Integer>> entries) {
		this.delegateIterator = entries.iterator();
	}

	/* (non-Javadoc)
	 * @see java.util.Iterator#hasNext()
	 */
	public boolean hasNext() {
		return delegateIterator.hasNext() || (remaining > 0);
	}

	/* (non-Javadoc)
	 * @see java.util.Iterator#next()
	 */
    @SuppressWarnings("unchecked")
	public E next() {
		while (remaining == 0) {
			Entry entry = delegateIterator.next();
			current = (E)entry.getKey();
			remaining = (Integer)entry.getValue();
		}

		remaining --;
		return current;
	}

	/* (non-Javadoc)
	 * @see java.util.Iterator#remove()
	 */
	public void remove() {
		throw new UnsupportedOperationException();
	}

}
