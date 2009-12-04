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
 * Implementation of SortedBag<E>
 * $Id: TreeBag.java,v 1.1 2009/12/04 23:01:56 dclarke Exp $
 * 
 * Author: David Clarke
 */
package alma.scheduling.utils;

import java.util.Collection;
import java.util.Comparator;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author dclarke
 *
 */
@SuppressWarnings("unchecked")
public class TreeBag<E> extends AbstractBag<E> implements SortedBag<E> {
	/*
	 * ================================================================
	 * Construction
	 * ================================================================
	 */
    /**
     * Constructs a new empty instance of TreeBag.
     */
    public TreeBag() {
        super(new TreeMap<E, Integer>());
    }

    /**
     * Constructs a new instance of TreeBag with the specified capacity.
     * 
     * @param capacity
     *            the initial capacity of this TreeBag
     */
    public TreeBag(Comparator<? super E> theComparator) {
    	super(new TreeMap<E, Integer>(theComparator));
    }

    /**
     * Constructs a new instance of TreeBag with the specified capacity and load
     * factor.
     * 
     * @param capacity
     *            the initial capacity
     * @param loadFactor
     *            the initial load factor
     */
    public TreeBag(TreeBag<E> bag) {
    	super(new TreeMap<E, Integer>(bag.comparator()));
    	addAll(bag);
    }

    /**
     * Constructs a new instance of TreeBag containing the elements in
     * the specified collection.
     * 
     * @param collection
     *            the collection of elements to add
     */
    public TreeBag(Collection<? extends E> collection) {
    	super(new TreeMap<E, Integer>());
        for (E e : collection) {
            add(e);
        }
    }
    
	public TreeBag(SortedMap<E, Integer> map) {
		super(map);
	}

	/*
	 * end of Construction
	 * ----------------------------------------------------------------
	 */
    
    
    
	/*
	 * ================================================================
	 * SortedBag
	 * ================================================================
	 */
	public Comparator<? super E> comparator() {
		SortedMap<E, Integer> d = (SortedMap<E, Integer>) getDelegate();
		return d.comparator();
	}

	public E first() {
		SortedMap<E, Integer> d = (SortedMap<E, Integer>) getDelegate();
		return d.firstKey();
	}

	@SuppressWarnings("unchecked")
	public SortedBag<E> headBag(E end) {
		SortedMap<E, Integer> d = (SortedMap<E, Integer>) getDelegate();
		return new TreeBag(d.headMap(end));
	}

	public E last() {
		SortedMap<E, Integer> d = (SortedMap<E, Integer>) getDelegate();
		return d.lastKey();
	}

	@SuppressWarnings("unchecked")
	public SortedBag<E> subBag(E start, E end) {
		SortedMap<E, Integer> d = (SortedMap<E, Integer>) getDelegate();
		return new TreeBag(d.subMap(start, end));
	}

	@SuppressWarnings("unchecked")
	public SortedBag<E> tailBag(E start) {
		SortedMap<E, Integer> d = (SortedMap<E, Integer>) getDelegate();
		return new TreeBag(d.tailMap(start));
	}
	/*
	 * end of SortedBag
	 * ----------------------------------------------------------------
	 */
    
    
    
    public static void main(String[] args) {
    	Bag<String> b = new TreeBag<String>();
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
    	
    	c = new TreeBag<String>(b);
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
