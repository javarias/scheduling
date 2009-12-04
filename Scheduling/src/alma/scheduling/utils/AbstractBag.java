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
 * Abstract implementation of most Bag functionality
 * $Id: AbstractBag.java,v 1.1 2009/12/04 23:01:56 dclarke Exp $
 * 
 * Author: David Clarke
 */
package alma.scheduling.utils;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public abstract class AbstractBag<E> implements Bag<E> {
	/*
	 * ================================================================
	 * Fields
	 * ================================================================
	 */
	private Map<E, Integer> delegate;
	
	private int elements = 0;
	/*
	 * end of Fields
	 * ----------------------------------------------------------------
	 */
	
	
	
	/*
	 * ================================================================
	 * Construction
	 * ================================================================
	 */
    AbstractBag(Map<E, Integer> backingMap) {
        this.delegate = backingMap;
    }
	/*
	 * end of Construction
	 * ----------------------------------------------------------------
	 */
	
	
	
	/*
	 * ================================================================
	 * Abstract support for subclasses
	 * ================================================================
	 */
    protected Map<E, Integer> getDelegate() {
    	return delegate;
    }
	/*
	 * end of Abstract support for subclasses
	 * ----------------------------------------------------------------
	 */
	
	
	
	/*
	 * ================================================================
	 * Bag inteface
	 * ================================================================
	 */
	/* (non-Javadoc)
	 * @see alma.scheduling.utils.Bag#add(java.lang.Object)
	 */
	public boolean add(E object) {
		if (delegate.containsKey(object)) {
			delegate.put(object, delegate.get(object) + 1);
		} else {
			delegate.put(object, 1);
		}

		elements ++;
		return true;
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.utils.Bag#addAll(java.util.Collection)
	 */
    public boolean addAll(Collection<? extends E> collection) {
        boolean result = false;
        Iterator<? extends E> it = collection.iterator();
        while (it.hasNext()) {
            if (add(it.next())) {
                result = true;
            }
        }
        return result;
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.utils.Bag#clear()
	 */
	public void clear() {
		delegate.clear();
		elements = 0;
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.utils.Bag#contains(java.lang.Object)
	 */
    public boolean contains(Object object) {
        return delegate.containsKey(object);
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.utils.Bag#contains(java.lang.Object)
	 */
    public int count(Object object) {
    	int result = 0;
    	if (delegate.containsKey(object)) {
    		result = delegate.get(object);
    	}
        return result;
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.utils.Bag#containsAll(java.util.Collection)
	 */
    public boolean containsAll(Collection<?> collection) {
    	HashBag<Object> that = new HashBag<Object>(collection);
    	Iterator<Object> it = that.getDelegate().keySet().iterator();
    	while (it.hasNext()) {
    		Object what = it.next();
    		if (this.count(what) < that.count(what)) {
    			return false;
    		}
    	}
    	return true;
    }

	/* (non-Javadoc)
	 * @see alma.scheduling.utils.Bag#isEmpty()
	 */
	public boolean isEmpty() {
		return delegate.isEmpty();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.utils.Bag#iterator()
	 */
    @SuppressWarnings("unchecked")
	public Iterator iterator() {
		return new BagIterator<E>(delegate.entrySet());
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.utils.Bag#remove(java.lang.Object)
	 */
    @SuppressWarnings("unchecked")
	public boolean remove(Object object) {
		boolean result = false;
		if (delegate.containsKey(object)) {
			int count = delegate.get(object);
			if (--count == 0) {
				delegate.remove(object);
			} else {
				delegate.put((E)object, count);
			}
			elements --;
		}

		return result;
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.utils.Bag#removeAll(java.util.Collection)
	 */
    public boolean removeAll(Collection<?> collection) {
        boolean result = false;
        if (size() <= collection.size()) {
            Iterator<?> it = iterator();
            while (it.hasNext()) {
                if (collection.contains(it.next())) {
                    it.remove();
                    result = true;
                }
            }
        } else {
            Iterator<?> it = collection.iterator();
            while (it.hasNext()) {
                result = remove(it.next()) || result;
            }
        }
        return result;
    }

	/* (non-Javadoc)
	 * @see alma.scheduling.utils.Bag#retainAll(java.util.Collection)
	 */
    public boolean retainAll(Collection<?> collection) {
        boolean result = false;
        Iterator<?> it = iterator();
        while (it.hasNext()) {
            if (!collection.contains(it.next())) {
                it.remove();
                result = true;
            }
        }
        return result;
    }

	/* (non-Javadoc)
	 * @see alma.scheduling.utils.Bag#size()
	 */
	public int size() {
		return elements;
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.utils.Bag#toArray()
	 */
    public Object[] toArray() {
        int size = size(), index = 0;
        Iterator<?> it = iterator();
        Object[] array = new Object[size];
        while (index < size) {
            array[index++] = it.next();
        }
        return array;
    }

	/* (non-Javadoc)
	 * @see alma.scheduling.utils.Bag#toArray(T[])
	 */
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] contents) {
        int size = size(), index = 0;
        if (size > contents.length) {
            Class<?> ct = contents.getClass().getComponentType();
            contents = (T[]) Array.newInstance(ct, size);
        }
        for (E entry : this) {
            contents[index++] = (T) entry;
        }
        if (index < contents.length) {
            contents[index] = null;
        }
        return contents;
    }


    /* (non-Javadoc)
     * @see alma.scheduling.utils.Bag#toSet()
     */
    public Set<E> toSet() {
    	return delegate.keySet();
    }
}
