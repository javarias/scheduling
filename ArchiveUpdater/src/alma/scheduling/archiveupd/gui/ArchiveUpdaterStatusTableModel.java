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
 * 
 * $Id: ArchiveUpdaterStatusTableModel.java,v 1.2 2011/01/28 00:35:30 javarias Exp $
 */

package alma.scheduling.archiveupd.gui;

import java.util.LinkedList;

import javax.swing.ImageIcon;
import javax.swing.table.AbstractTableModel;

import alma.JavaContainerError.wrappers.AcsJContainerServicesEx;
import alma.exec.extension.subsystemplugin.PluginContainerServices;
import alma.scheduling.ArchiveUpdater;
import alma.scheduling.ArchiveUpdaterCallback;

/**
 * Table model for Archive Updater Import Status GUI table</br>
 * Check {@code ArchiveUpdaterPanel}
 * 
 * @author javarias
 * 
 */
public class ArchiveUpdaterStatusTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 7458671666183217394L;
	
	private static final String[] columnNames = {"Status", "Timestamp", "Entity ID", "Entity Type", "Details"};
	private static final Class[] columnClass = {ImageIcon.class, String.class, String.class, String.class, String.class};
	private LinkedList<Object[]> data;
	private ArchiveUpdaterGUICallback callback;
	private boolean isCallbackRegistered = false;
	
	private final static int MAX_ROWS= 10000;

	private String callbackName;


	public ArchiveUpdaterStatusTableModel(){
		data = new LinkedList<Object[]>();
//		ArrayList<Object>row = new ArrayList<Object>();
//		row.add(StandardIcons.STATUS_OKAY.icon); row.add("2"); row.add("3"); row.add("4"); row.add("5");
//		data.add(row);
		callback = new ArchiveUpdaterGUICallback(this);
	}
	
	@Override
	public int getRowCount() {
		return data.size();
	}

	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return data.get(rowIndex)[columnIndex];
	}

	@Override
	public String getColumnName(int column) {
		return columnNames[column];
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return columnClass[columnIndex];
	}
	
	public synchronized void addDataRow(ArchiveUpdaterImportEvent event){
		Object[] array = new Object[columnNames.length];
		array[0] = event.getIconStatus();
		array[1] = event.getTimestamp();
		array[2] = event.getEntityId();
		array[3] = event.getEntityType();
		array[4] = event.getDetails();
		if(data.size() > MAX_ROWS)
			data.pollLast();
		data.push(array);
		fireTableDataChanged();
	}	
	
	public void registerCallback(ArchiveUpdater upd, PluginContainerServices services){
		if	(!isCallbackRegistered) {
			callbackName = System.currentTimeMillis()+ "_" + this.toString();
			try{
				services.activateOffShoot(callback);
				upd.registerCallback(callbackName, callback._this());
				isCallbackRegistered = true;
			} catch (org.omg.CORBA.SystemException ex){
				System.out.println("Archive Updater Status GUI will continue without " +
						"connection to the Archive Updater Component" +
						" - Reason: " + ex.getMessage());
				ex.printStackTrace();
			} catch (AcsJContainerServicesEx ex) {
				System.out.println("Archive Updater Status GUI will continue without " +
						"connection to the Archive Updater Component" +
						" - Reason: " + ex.getMessage());
				ex.printStackTrace();
			}
		}
	}
	
	public void deregisterCallback(ArchiveUpdater upd, PluginContainerServices services){
		if (isCallbackRegistered) {
			try {
				services.deactivateOffShoot(callback);
				upd.deregisterCallback(callbackName);
			}catch (org.omg.CORBA.SystemException ex) {
				ex.printStackTrace();
				System.out.println("Continue the deregistration anyways");
			} catch (AcsJContainerServicesEx ex) {
				ex.printStackTrace();
				System.out.println("Continue the deregistration anyways");
			}
			isCallbackRegistered = false;
		}
	}

}
