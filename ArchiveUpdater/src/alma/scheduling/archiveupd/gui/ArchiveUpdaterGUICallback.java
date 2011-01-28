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
 * $Id: ArchiveUpdaterGUICallback.java,v 1.2 2011/01/28 00:35:30 javarias Exp $
 */

package alma.scheduling.archiveupd.gui;

import java.util.Date;

import alma.scheduling.ArchiveImportEvent;
import alma.scheduling.ArchiveUpdaterCallbackPOA;

public class ArchiveUpdaterGUICallback extends ArchiveUpdaterCallbackPOA{

	private ArchiveUpdaterStatusTableModel tableModel;
	
	/**
	 * 
	 * @param tableModel
	 */
	ArchiveUpdaterGUICallback(ArchiveUpdaterStatusTableModel tableModel){
		this.tableModel = tableModel;
	}
	/**
	 * Prepare the notification events received from {@code APDMtoSchedulingConverter} 
	 * to be readable by the Archive Updater status GUI
	 * (convert it to {@code ArchiveUpdaterImportEvent}) and passes it to the table model
	 * of the GUI.  
	 */
	@Override
	public void report(ArchiveImportEvent evt) {
		ArchiveUpdaterImportEvent event = new ArchiveUpdaterImportEvent();
		event.setTimestamp(new Date(alma.acs.util.UTCUtility.utcOmgToJava(evt.timestamp)));
		event.setEntityId(evt.entityId);
		event.setEntityType(evt.entityType);
		event.setStatus(ArchiveUpdaterImportEvent.ImportStatus.values()[evt.status.value()]);
		event.setDetails(evt.details);
		
		System.out.println("Received ArchiveImportEvent, Time: " + event.getTimestamp() + " Entity ID: " + event.getEntityId());
		
		tableModel.addDataRow(event);
	}

}
