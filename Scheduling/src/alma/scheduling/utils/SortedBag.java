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
 * An extension of Bag<E> which sorts its entries.
 * $Id: SortedBag.java,v 1.1 2009/12/04 23:01:56 dclarke Exp $
 * 
 * Author: David Clarke
 */
package alma.scheduling.utils;

import java.util.Comparator;
import java.util.NoSuchElementException;

public interface SortedBag<E> extends Bag<E> {

    /**
     * Answers the Comparator used to compare elements in this SortedBag.
     * 
     * @return a Comparator or null if the natural order is used
     */
    public Comparator<? super E> comparator();

    /**
     * Answer the first sorted element in this SortedBag.
     * 
     * @return the first sorted element
     * 
     * @exception NoSuchElementException
     *                when this SortedBag is empty
     */
    public E first();

    /**
     * Answers a SortedBag of the specified portion of this SortedBag which
     * contains elements less than the end element. The returned SortedBag is
     * backed by this SortedBag so changes to one are reflected by the other.
     * 
     * @param end
     *            the end element
     * @return a subset where the elements are less than <code>end</code>
     * 
     * @exception ClassCastException
     *                when the class of the end element is inappropriate for
     *                this SortedBag
     * @exception NullPointerException
     *                when the end element is null and this SortedBag does not
     *                support null elements
     */
    public SortedBag<E> headBag(E end);

    /**
     * Answer the last sorted element in this SortedBag.
     * 
     * @return the last sorted element
     * 
     * @exception NoSuchElementException
     *                when this SortedBag is empty
     */
    public E last();

    /**
     * Answers a SortedBag of the specified portion of this SortedBag which
     * contains elements greater or equal to the start element but less than the
     * end element. The returned SortedBag is backed by this SortedBag so
     * changes to one are reflected by the other.
     * 
     * @param start
     *            the start element
     * @param end
     *            the end element
     * @return a subset where the elements are greater or equal to
     *         <code>start</code> and less than <code>end</code>
     * 
     * @exception ClassCastException
     *                when the class of the start or end element is
     *                inappropriate for this SortedBag
     * @exception NullPointerException
     *                when the start or end element is null and this SortedBag
     *                does not support null elements
     * @exception IllegalArgumentException
     *                when the start element is greater than the end element
     */
    public SortedBag<E> subBag(E start, E end);

    /**
     * Answers a SortedBag of the specified portion of this SortedBag which
     * contains elements greater or equal to the start element. The returned
     * SortedBag is backed by this SortedBag so changes to one are reflected by
     * the other.
     * 
     * @param start
     *            the start element
     * @return a subset where the elements are greater or equal to
     *         <code>start</code>
     * 
     * @exception ClassCastException
     *                when the class of the start element is inappropriate for
     *                this SortedBag
     * @exception NullPointerException
     *                when the start element is null and this SortedBag does not
     *                support null elements
     */
    public SortedBag<E> tailBag(E start);
}
