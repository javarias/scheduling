/*******************************************************************************
 * ALMA - Atacama Large Millimeter Array
 * Copyright (c) AUI - Associated Universities Inc., 2011
 * (in the framework of the ALMA collaboration).
 * All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 *******************************************************************************/
package alma.scheduling.array.compimpl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import alma.acs.logging.AcsLogger;
import alma.scheduling.QueueOperation;
import alma.scheduling.SchedBlockQueueCallbackPOA;
import alma.scheduling.array.guis.SBExecutionTableModel;

public class SchedBlockQueueCallbackImpl extends SchedBlockQueueCallbackPOA {

	AcsLogger logger;
	private PropertyChangeListener pcl;
	
	public SchedBlockQueueCallbackImpl(AcsLogger acsLogger, PropertyChangeListener listener){
		logger = acsLogger;
		this.pcl = listener;
	}
	
	@Override
	public void report(long timestamp, QueueOperation operation, 
			String[] uids, String newState) {
		logger.info("received report SBQC: " + timestamp + ", " + operation
				+ ", " + uids.length + ", " + newState);
		PropertyChangeEvent event = new PropertyChangeEvent(this, newState, null, operation);
		pcl.propertyChange(event);
	}

}
