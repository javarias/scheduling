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

package alma.scheduling.array.sbSelection;

import java.util.Observable;

import alma.scheduling.QueueOperation;
import alma.scheduling.array.compimpl.ArrayImpl;
import alma.scheduling.array.guis.ArrayGUINotification;
import alma.scheduling.array.sbQueue.QueueNotification;

/**
 *
 * @author dclarke
 * $Id: AbstractSelector.java,v 1.4 2011/03/18 00:20:23 dclarke Exp $
 */
public abstract class AbstractSelector extends Observable
			implements Selector {

	/*
	 * ================================================================
	 * Array configuration
	 * ================================================================
	 */
	/** The array for which we're configured */
	protected ArrayImpl array;
	
	/** The source for our triggering events */
	protected Observable source;
	
	private void subscribeToNewSource(Observable source) {
		this.source = source;
		source.addObserver(this);
	}
	
	private void unsubscribeFromPreviousSource() {
		if (source != null) {
			source.deleteObserver(this);
			source = null;
		}
	}
	
	/* (non-Javadoc)
	 * @see alma.scheduling.array.sbSelection.Selector#configureArray(alma.scheduling.ArrayOperations)
	 */
	@Override
	public void configureArray(ArrayImpl array, Observable source) {
		this.array = array;
		unsubscribeFromPreviousSource();
		subscribeToNewSource(source);
		
	}
	/* End Array configuration
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * Listening
	 * ================================================================
	 */
    @Override
    public void update(Observable o, Object arg) {
    	try {
    		QueueNotification notification = (QueueNotification) arg;

    		if (notification.getOperation() == QueueOperation.WAITING) {
    			SelectionThread t = new SelectionThread();
    			t.start();
    		}
    	} catch (ClassCastException e) {
    	}
    }
    
    private class SelectionThread extends Thread {
    	@Override
    	public void run() {
    		selectNextSB();
    	}
    }

	/* End Listening
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * Notifying
	 * ================================================================
	 */
    protected void notify(Object change) {
        setChanged();
        notifyObservers(change);
    }
	/* End Notifying
	 * ============================================================= */
}
