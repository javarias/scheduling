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

package alma.scheduling.archiveupd.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.omg.CORBA.Object;

import alma.exec.extension.subsystemplugin.PluginContainerServices;
import alma.exec.extension.subsystemplugin.SubsystemPlugin;
import alma.scheduling.ArchiveUpdater;
import alma.scheduling.ArchiveUpdaterHelper;

public class ArchiveUpdaterPanel extends JPanel implements SubsystemPlugin {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2015120775204419346L;
	
	private PluginContainerServices services;
	private ArchiveUpdater archiveUpdRef = null;
	
	private JButton updateButton;
	private JButton refreshButton;
	private JPanel southPanel;
	private JTable importStatusTable;
	
	@Override
	public void setServices(PluginContainerServices services) {
		this.services = services;
	}

	@Override
	public void start() throws Exception {
		if(archiveUpdRef == null) {
			Object o = services.getDefaultComponent("IDL:alma/scheduling/ArchiveUpdater:1.0");
			ArchiveUpdater au = alma.scheduling.ArchiveUpdaterHelper.narrow(o);
			String name = au.name();
			services.releaseComponent(name);
			archiveUpdRef = ArchiveUpdaterHelper.narrow(services.getComponentNonSticky(name));
		}
		
		initialize();

	}

	@Override
	public void stop() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean runRestricted(boolean restricted) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	private void initialize(){
		southPanel = new JPanel(new FlowLayout());
		initializeUpdateButton();
		initializeRefreshButton();
		southPanel.add(updateButton);
		southPanel.add(refreshButton);
		ArchiveUpdaterStatusTableModel tableModel = new ArchiveUpdaterStatusTableModel();
		tableModel.registerCallback(archiveUpdRef, services);
		importStatusTable = new JTable(tableModel);
		this.setLayout(new BorderLayout());
		this.add(new JScrollPane(importStatusTable), BorderLayout.CENTER);
		this.add(southPanel, BorderLayout.SOUTH);
		importStatusTable.getColumnModel().getColumn(0).setPreferredWidth(5);
		this.revalidate();
	}
	
	private void initializeUpdateButton(){
		updateButton = new JButton("Update");
		updateButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				updateButton.setEnabled(false);
				services.getThreadFactory().newThread(new UpdateCommand()).start();
			}
		});
	}
	
	private void initializeRefreshButton() {
		refreshButton = new JButton("Refresh");
		refreshButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				refreshButton.setEnabled(false);
				services.getThreadFactory().newThread(new RefreshCommand()).start();
			}
		});
	}
	
	public static void main(String[] args){
		JFrame frame = new JFrame();
		ArchiveUpdaterPanel panel = new ArchiveUpdaterPanel();
		panel.initialize();
		frame.getContentPane().add(panel);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
	
	private class RefreshCommand extends Thread{

		@Override
		public void run() {
			archiveUpdRef.refresh();
			refreshButton.setEnabled(true);
		}
	}
	
	private class UpdateCommand extends Thread{

		@Override
		public void run() {
			archiveUpdRef.update();
			updateButton.setEnabled(true);
		}
	}
	
	private static class CustomDateRenderer extends DefaultTableCellRenderer{
		
	}
}
