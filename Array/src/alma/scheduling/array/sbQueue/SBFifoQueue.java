/* ALMA - Atacama Large Millimiter Array
 * (c) Associated Universities Inc., 2010
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
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 */

package alma.scheduling.array.sbQueue;

import java.util.PriorityQueue;

import alma.scheduling.SchedBlockQueueManagerOperations;

/**
 * Simple FIFO implementation of a SchedBlock queue (though it does
 * support promotion & demotion of SchedBlocks up & down the queue).
 * 
 * @author dclarke
 * $Id: SBFifoQueue.java,v 1.1 2010/07/19 21:08:29 dclarke Exp $
 */
public class SBFifoQueue implements SchedBlockQueueManagerOperations {

	/* (non-Javadoc)
	 * @see alma.scheduling.SchedBlockQueueManagerOperations#moveDown(java.lang.String)
	 */
	@Override
	public void moveDown(String arg0) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see alma.scheduling.SchedBlockQueueManagerOperations#moveUp(java.lang.String)
	 */
	@Override
	public void moveUp(String arg0) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see alma.scheduling.SchedBlockQueueManagerOperations#pull()
	 */
	@Override
	public String pull() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.SchedBlockQueueManagerOperations#push(java.lang.String)
	 */
	@Override
	public void push(String arg0) {
		// TODO Auto-generated method stub

	}

	private static class PriorityPair implements Comparable<PriorityPair>{
		public PriorityPair(String sbId, int priority) {
			super();
			this.sbId = sbId;
			this.priority = priority;
		}
		String sbId;
		int    priority;
		@Override
		public int compareTo(PriorityPair that) {
			return this.priority - that.priority;
		}
		
		public String toString() {
			return String.format("@ %h (%s, %d)",
					hashCode(), sbId, priority);
		}
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		final String[] labels = {"Alpha", "Bravo", "Charlie", "Delta"};
		final PriorityQueue<PriorityPair> pq = new PriorityQueue<PriorityPair>();
		
		for (int i = labels.length-1; i >= 0; i--) {
			PriorityPair pp = new PriorityPair(labels[i], i);
			pq.add(pp);
			System.out.format("inserted  %s%n", pp);
		}
		
		PriorityPair[] array = pq.toArray(new PriorityPair[0]);
		for (PriorityPair pp : array) {
			System.out.format("\tin array %s%n", pp);
		}
		promote(pq, "Alpha");
		promote(pq, "Charlie");
		promote(pq, "Zulu");
		while (!pq.isEmpty()) {
			PriorityPair pp = pq.poll();
			System.out.format("extracted %s%n", pp);
		}
	}

	private static void promote(PriorityPair[] array, int i) {
		System.out.format("promoting item no. %d (%s)%n", i, array[i]);
		if (i > 0) {
			int nextP = array[i-1].priority;
			array[i-1].priority = array[i].priority;
			array[i].priority = nextP;
		}
	}

	private static void promote(PriorityPair[] array, String label) {
		if (array[0].sbId.equals(label)) {
			System.out.format("cannot promote %s - it's already the highest priority%n",
					label);
			return;
		}
		for (int i = 1; i < array.length; i++) {
			if (array[i].sbId.equals(label)) {
				promote(array, i);
				return;
			}
		}
		System.out.format("cannot promote %s - it's not in the queue%n",
				label);
	}
	
	private static void promote(PriorityQueue<PriorityPair> pq, String label) {
		synchronized (pq) {
			PriorityPair[] array = pq.toArray(new PriorityPair[0]);
			promote(array, label);
		}
	}
}
