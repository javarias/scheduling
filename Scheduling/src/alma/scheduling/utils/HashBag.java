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
 * Implementation of Bag<E>
 * $Id: HashBag.java,v 1.1 2009/12/04 23:01:56 dclarke Exp $
 * 
 * Author: David Clarke
 */
package alma.scheduling.utils;

import java.util.Collection;
import java.util.HashMap;

@SuppressWarnings("unchecked")
public class HashBag<E> extends AbstractBag<E> implements Bag<E> {
	/*
	 * ================================================================
	 * Construction
	 * ================================================================
	 */
    /**
     * Constructs a new empty instance of HashBag.
     */
    public HashBag() {
        super(new HashMap<E, Integer>());
    }

    /**
     * Constructs a new instance of HashBag with the specified capacity.
     * 
     * @param capacity
     *            the initial capacity of this HashBag
     */
    public HashBag(int capacity) {
    	super(new HashMap<E, Integer>(capacity));
    }

    /**
     * Constructs a new instance of HashBag with the specified capacity and load
     * factor.
     * 
     * @param capacity
     *            the initial capacity
     * @param loadFactor
     *            the initial load factor
     */
    public HashBag(int capacity, float loadFactor) {
    	super(new HashMap<E, Integer>(capacity, loadFactor));
    }

    /**
     * Constructs a new instance of HashBag containing the unique elements in
     * the specified collection.
     * 
     * @param collection
     *            the collection of elements to add
     */
    public HashBag(Collection<? extends E> collection) {
    	super(new HashMap<E, Integer>(collection.size() < 6 ? 11 : collection
                .size() * 2));
        for (E e : collection) {
            add(e);
        }
    }
	/*
	 * end of Construction
	 * ----------------------------------------------------------------
	 */
    
    
    
    public static void main(String[] args) {
    	Bag<String> b = new HashBag<String>();
    	Bag<String> c;
    	
    	b.add("Peter");
    	b.add("Piper");
    	b.add("picked");
    	b.add("a");
    	b.add("peck");
    	b.add("of");
    	b.add("pickled");
    	b.add("peppers");
    	b.add("If");
    	b.add("Peter");
    	b.add("Piper");
    	b.add("picked");
    	b.add("a");
    	b.add("peck");
    	b.add("of");
    	b.add("pickled");
    	b.add("peppers");
    	b.add("where's");
    	b.add("the");
    	b.add("peck");
    	b.add("of");
    	b.add("pickled");
    	b.add("peppers");
    	b.add("Peter");
    	b.add("Piper");
    	b.add("picked");
    	
    	c = new HashBag<String>(b);
    	c.add("zombie");
    	
    	System.out.format("b.size() = %d%n", b.size());
    	int i = 0;
    	for (String str : b) {
        	System.out.format("%5d:%s (%d)%n", ++i, str, b.count(str));
    	}
    	
    	System.out.format("b.containsAll(b) = %s%n", b.containsAll(b));
    	System.out.format("b.containsAll(c) = %s%n", b.containsAll(c));
    	System.out.format("c.containsAll(b) = %s%n", c.containsAll(b));
    	System.out.format("c.containsAll(c) = %s%n", c.containsAll(c));
    }
}
