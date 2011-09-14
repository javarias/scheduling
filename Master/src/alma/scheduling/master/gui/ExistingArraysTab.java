/*
 * ALMA - Atacama Large Millimiter Array
 * (c) European Southern Observatory, 2002
 * (c) Associated Universities Inc., 2002
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
 *
 * File ExistingArraysTab.java
 */
package alma.scheduling.master.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import alma.exec.extension.subsystemplugin.PluginContainerServices;

@SuppressWarnings("serial")
public class ExistingArraysTab extends SchedulingPanelGeneralPanel {
    private ExistingArraysTabController controller;
    private JPanel tablePanel, reportagePanel;
    private JTextPane arrayDetails;
    private ArrayTable table;

	private boolean filledExistingArrays = false;
    /**
      *Tester constructor
      */
    public ExistingArraysTab() {
        super();
        createLayout();
    }

    public void connectedSetup(PluginContainerServices cs){
        super.onlineSetup(cs);
        controller = new ExistingArraysTabController(cs, this);
        controller.secondSetup(cs);
        table.setCS(cs);
        checkCurrentExistingArrays();
   }

    private synchronized void checkCurrentExistingArrays() {
    		//HACK: To avoid to show duplicated entries in the current arrays table
    		if (filledExistingArrays )
    			return;
    		filledExistingArrays = true;
    		CheckExistingArrays foo  = new CheckExistingArrays();
    		Thread t = controller.getCS().getThreadFactory().newThread(foo);
    		t.start();
    }

    
    private void createLayout() {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        createTablePanel();
        createReportagePanel();
    }

    private void createTablePanel(){
        table = new ArrayTable(new Dimension(300,200));
        table.setOwner(this);
        table.getSelectionModel().addListSelectionListener(tableListener());
        JScrollPane pane = new JScrollPane(table,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        tablePanel = new JPanel();
        tablePanel.setLayout(new BorderLayout());
        tablePanel.setBorder(new TitledBorder("Existing Arrays"));
        tablePanel.add(pane);
        add(tablePanel);
    }
    
    private ListSelectionListener tableListener() {
		return new ListSelectionListener(){

			@Override
			public void valueChanged(ListSelectionEvent e) {
				final String array = table.getCurrentArray();
				if (array != null) {
					// TODO: display the array details:
					//	Antennas
					//	Photonic (if there is one)
					//	Correlator (if there is one)
					//	Scheduling Mode (manual, dynamic, interactive)
					//	Policy Name (if a dynamic array
					// Actually, the scheduling mode is displayed in
					// the table
					arrayDetails.setText("Current array = " +
							table.getCurrentArray());
				} else {
					arrayDetails.setText("no array selected");
				}
//				System.out.println("Current array = " + table.getCurrentArray());
			}};
	}

	private void createReportagePanel() {
    	reportagePanel = new JPanel();
    	reportagePanel.setBorder(new TitledBorder("Resources In Array"));
    	reportagePanel.setLayout(new BorderLayout());
    	arrayDetails = new JTextPane();
    	reportagePanel.add(arrayDetails);
    	// TODO: add this when we have something useful to say in it
//    	add(reportagePanel);
    }

    public void exit(){
    }

    public void setEnable(boolean b){
    }
    private String updateArrayName;
    private String updateArrayType;
        
    protected void addArray(String arrayname, String arraytype) {
        updateArrayName = arrayname;
        updateArrayType = arraytype;
        table.addArray(updateArrayName, updateArrayType);
        repaint();
        validate();
    }

    protected void removeArray(String arrayname){
        table.removeArray(arrayname);
        repaint();
        validate();
    }

    protected void openSchedulerForArray(String array) {
        //do later when its ok to make an ICD change
    }


    class CheckExistingArrays implements Runnable{
    	public CheckExistingArrays(){ }
    	
    	private String formatArray(final String label, final String[] strings) {
    		StringBuilder sb = new StringBuilder();
    		String sep = "";
    		sb.append(label);
    		sb.append(": ");
    		for (final String s : strings) {
    			sb.append(sep);
    			sb.append(s);
    			sep = ", ";
    		}
    		if (sep.length() == 0) {
    			// no strings
    			sb.append("<none>");
    		}
    		return sb.toString();
    	}
    	
		public void run() {
			String[] a_arrays = controller.getCurrentAutomaticArrays();
			String[] m_arrays = controller.getCurrentManualArrays();

			System.out.println(formatArray("Automatic Arrays", a_arrays));
			System.out.println(formatArray("Manual    Arrays", m_arrays));

			for (String s : a_arrays) {
				addArray(s, "automatic");
			}

			for (String s : m_arrays) {
				addArray(s, "manual");
			}
    	}
    }
}
