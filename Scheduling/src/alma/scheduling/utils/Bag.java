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
 * A Bag is a collection which allows duplicate elements
 * $Id: Bag.java,v 1.1 2009/12/04 23:01:56 dclarke Exp $
 * 
 * Author: David Clarke
 */
package alma.scheduling.utils;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * Bag is a collection which allows duplicate elements.
 */
public interface Bag<E> extends Collection<E> {

    /**
     * Adds the specified object to this Bag.
     * 
     * @param object
     *            the object to add
     * @return true if this Bag is modified, false otherwise
     * 
     * @exception UnsupportedOperationException
     *                when adding to this Bag is not supported
     * @exception ClassCastException
     *                when the class of the object is inappropriate for this Bag
     * @exception IllegalArgumentException
     *                when the object cannot be added to this Bag
     */
    public boolean add(E object);

    /**
     * Adds the objects in the specified Collection to this Bag.
     * 
     * @param collection
     *            the Collection of objects
     * @return true if this Bag is modified, false otherwise
     * 
     * @exception UnsupportedOperationException
     *                when adding to this Bag is not supported
     * @exception ClassCastException
     *                when the class of an object is inappropriate for this Bag
     * @exception IllegalArgumentException
     *                when an object cannot be added to this Bag
     */
    public boolean addAll(Collection<? extends E> collection);

    /**
     * Removes all elements from this Bag, leaving it empty.
     * 
     * @exception UnsupportedOperationException
     *                when removing from this Bag is not supported
     * 
     * @see #isEmpty
     * @see #size
     */
    public void clear();

    /**
     * Searches this Bag for the specified object.
     * 
     * @param object
     *            the object to search for
     * @return true if object is an element of this Bag, false otherwise
     */
    public boolean contains(Object object);

    /**
     * Searches this Bag for the specified object.
     * 
     * @param object
     *            the object to search for
     * @return the number of times object is found in this Bag
     */
    public int count(Object object);

    /**
     * Searches this Bag for all objects in the specified Collection.
     * 
     * @param collection
     *            the Collection of objects
     * @return true if all objects in the specified Collection are elements of
     *         this Bag, false otherwise
     */
    public boolean containsAll(Collection<?> collection);

    /**
     * Compares the argument to the receiver, and answers true if they represent
     * the <em>same</em> object using a class specific comparison.
     * 
     * @param object
     *            Object the object to compare with this object.
     * @return boolean <code>true</code> if the object is the same as this
     *         object <code>false</code> if it is different from this object.
     * @see #hashCode
     */
    public boolean equals(Object object);

    /**
     * Answers an integer hash code for the receiver. Objects which are equal
     * answer the same value for this method.
     * 
     * @return the receiver's hash
     * 
     * @see #equals
     */
    public int hashCode();

    /**
     * Answers if this Bag has no elements, a size of zero.
     * 
     * @return true if this Bag has no elements, false otherwise
     * 
     * @see #size
     */
    public boolean isEmpty();

    /**
     * Answers an Iterator on the elements of this Bag.
     * 
     * @return an Iterator on the elements of this Bag
     * 
     * @see Iterator
     */
    public Iterator<E> iterator();

    /**
     * Removes an occurrence of the specified object from this Bag.
     * 
     * @param object
     *            the object to remove
     * @return true if this Bag is modified, false otherwise
     * 
     * @exception UnsupportedOperationException
     *                when removing from this Bag is not supported
     */
    public boolean remove(Object object);

    /**
     * Removes all objects in the specified Collection from this Bag.
     * 
     * @param collection
     *            the Collection of objects to remove
     * @return true if this Bag is modified, false otherwise
     * 
     * @exception UnsupportedOperationException
     *                when removing from this Bag is not supported
     */
    public boolean removeAll(Collection<?> collection);

    /**
     * Removes all objects from this Bag that are not contained in the specified
     * Collection.
     * 
     * @param collection
     *            the Collection of objects to retain
     * @return true if this Bag is modified, false otherwise
     * 
     * @exception UnsupportedOperationException
     *                when removing from this Bag is not supported
     */
    public boolean retainAll(Collection<?> collection);

    /**
     * Answers the number of elements in this Bag.
     * 
     * @return the number of elements in this Bag
     */
    public int size();

    /**
     * Answers an array containing all elements contained in this Bag.
     * 
     * @return an array of the elements from this Bag
     */
    public Object[] toArray();

    /**
     * Answers a Set<E> containing all unique elements contained in
     * this Bag. The set is backed by this Bag so changes to one are
     * reflected by the other. The set does not support adding.
     * 
     * @return a Set<E> of all unique elements from this Bag
     */
    public Set<E> toSet();

    /**
     * Answers an array containing all elements contained in this Bag. If the
     * specified array is large enough to hold the elements, the specified array
     * is used, otherwise an array of the same type is created. If the specified
     * array is used and is larger than this Bag, the array element following
     * the collection elements is set to null.
     * 
     * @param array
     *            the array
     * @return an array of the elements from this Bag
     * 
     * @exception ArrayStoreException
     *                when the type of an element in this Bag cannot be stored
     *                in the type of the specified array
     */
    public <T> T[] toArray(T[] array);
}