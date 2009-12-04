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
 * A helper class to assist the setting of stuff (such as the greying
 * in and out of buttons) on the QueuedSchedTab. Keeps track of things
 * such as which SBs are in the queue and answers questions based upon
 * this information.
 * 
 * $Id: QueuedSchedQueueManager.java,v 1.1 2009/12/04 23:01:32 dclarke Exp $
 * 
 * Author: David Clarke
 */
package alma.scheduling.AlmaScheduling.GUI.OmcSchedulingPanel;

import alma.scheduling.Define.SB;
import alma.scheduling.utils.Bag;
import alma.scheduling.utils.HashBag;

/**
 * @author dclarke
 *
 */
public class QueuedSchedQueueManager {
	/*
	 * ================================================================
	 * Fields
	 * ================================================================
	 */
	private HashBag<String> queue;
	private int maxCount;
	/*
	 * end of Fields
	 * ----------------------------------------------------------------
	 */
	
	
	
	/*
	 * ================================================================
	 * Construction
	 * ================================================================
	 */
	QueuedSchedQueueManager() {
        this.queue = new HashBag<String>();
        this.maxCount = 0;
    }
	/*
	 * end of Construction
	 * ----------------------------------------------------------------
	 */
	
	
	
	/*
	 * ================================================================
	 * Bookkeeping
	 * ================================================================
	 */
	public void add(String id) {
        queue.add(id);
        if (queue.count(id) > maxCount) {
        	maxCount = queue.count(id);
        }
    }

	public void addAll(String[] ids) {
		for (final String id : ids) {
			add(id);
		}
	}
	
	private int findMaximumCount() {
		int max = 0;
		for (final String id : queue.toSet()) {
			final int thisCount = queue.count(id);
			if (thisCount > max) {
				max = thisCount;
			}
		}
		return max;
	}
	
	public void remove(String id) {
		final boolean isCurrentMax = (queue.count(id) == maxCount);
        queue.remove(id);
        if (isCurrentMax) {
        	maxCount = findMaximumCount();
        }
    }
	
	public void removeAll(String[] ids) {
		for (final String id : ids) {
			queue.remove(id);
		}
    	maxCount = findMaximumCount();
	}
	
	public boolean hasMultiples() {
		return maxCount > 1;
	}
	
	public boolean hasElements() {
		return queue.size() > 0;
	}
	/*
	 * end of Bookkeeping
	 * ----------------------------------------------------------------
	 */
}
