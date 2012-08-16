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
/**
 * 
 */
package alma.scheduling.projectmanager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableRowSorter;

/**
 * @author dclarke
 *
 */
@SuppressWarnings("serial")
public class ProjectDisplayGUI extends JPanel {
	
	private StateArchiveDAO sa;
	private JButton exitB;
	private JButton refreshB;
	private Set<JButton> stateBs;
	private JTable obsProjectTable;
	private JTable obsUnitSetTable;
	private JTable schedBlockTable;
	
	private PSTableModel  obsProjectTM;
	private OUSTableModel obsUnitSetTM;
	private SBTableModel  schedBlockTM;
	
	private EntityTableModel currentModel;
	private JTable           currentTable;

	public ProjectDisplayGUI(StateArchiveDAO sa) {
		this.sa = sa;
		createComponents();
	}

	private void createComponents() {
		final JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
		split.setTopComponent(createTablesPanel());
		split.setBottomComponent(createStateButtons());
		
		setLayout(new BorderLayout());
		add(split, BorderLayout.CENTER);
		add(createApplicationButtons(), BorderLayout.SOUTH);
		updateStateButtons();
	}

	private Component createTablesPanel() {
		final JTabbedPane result = new JTabbedPane();
		result.add("Projects",    createObsProjectTable());
		result.add("OUSs",        createObsUnitSetTable());
		result.add("SchedBlocks", createSchedBlockTable());
		result.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				switch (result.getSelectedIndex()) {
					case 0:
						currentModel = obsProjectTM;
						currentTable = obsProjectTable;
						break;
					case 1:
						currentModel = obsUnitSetTM;
						currentTable = obsUnitSetTable;
						break;
					case 2:
						currentModel = schedBlockTM;
						currentTable = schedBlockTable;
						break;
				}
				updateStateButtons();
			}
		});
		result.setSelectedIndex(0);
		currentModel = obsProjectTM;
		currentTable = obsProjectTable;
		result.setMinimumSize(new Dimension(0,0));
		return result;
	}
	
	private Component createObsProjectTable() {
		obsProjectTM    = new PSTableModel(sa);
		obsProjectTable = new JTable(obsProjectTM);
		obsProjectTable.setRowSorter(new TableRowSorter<PSTableModel>(obsProjectTM));
		obsProjectTable.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {
					
					@Override
					public void valueChanged(ListSelectionEvent e) {
						if (currentTable == obsProjectTable) {
							updateStateButtons();
						}
					}
				});
		final JScrollPane result = new JScrollPane(obsProjectTable);
		return result;
	}
	
	private Component createObsUnitSetTable() {
		obsUnitSetTM    = new OUSTableModel(sa);
		obsUnitSetTable = new JTable(obsUnitSetTM);
		obsUnitSetTable.setRowSorter(new TableRowSorter<OUSTableModel>(obsUnitSetTM));
		obsUnitSetTable.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {
					
					@Override
					public void valueChanged(ListSelectionEvent e) {
						if (currentTable == obsUnitSetTable) {
							updateStateButtons();
						}
					}
				});
		final JScrollPane result = new JScrollPane(obsUnitSetTable);
		return result;
	}
	
	private Component createSchedBlockTable() {
		schedBlockTM    = new SBTableModel(sa);
		schedBlockTable = new JTable(schedBlockTM);
		schedBlockTable.setRowSorter(new TableRowSorter<SBTableModel>(schedBlockTM));
		schedBlockTable.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {
					
					@Override
					public void valueChanged(ListSelectionEvent e) {
						if (currentTable == schedBlockTable) {
							updateStateButtons();
						}
					}
				});
		final JScrollPane result = new JScrollPane(schedBlockTable);
		return result;
	}
	
	private Component createStateButtons() {
		final JPanel result = new JPanel();
		result.setLayout(new GridLayout(0, 3));
		Set<String> allStates = new TreeSet<String>();
		allStates.addAll(sa.getObsProjectStates());
		allStates.addAll(sa.getSchedBlockStates());
		allStates.addAll(sa.getObsUnitSetStates());
		stateBs = new HashSet<JButton>();
		for (final String state : allStates) {
			final JButton b = new JButton(state);
//			final Dimension d = b.getPreferredSize();
//			b.setMinimumSize(d);
//			b.setMaximumSize(d);
			b.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					doStateChange(state);
				}});
			result.add(b);
			stateBs.add(b);
		}
		result.setMinimumSize(new Dimension(0,0));
		return result;
	}

	private Component createApplicationButtons() {
		final JPanel result = new JPanel();
		result.setLayout(new FlowLayout(FlowLayout.RIGHT));
		
		if (sa.isBuggered()) {
			JLabel label = new JLabel("<html><b>Problems during analysis of the state archive</b></html>");
			label.setForeground(Color.RED);
			result.add(label);
		}
		exitB = new JButton("Exit");
		refreshB = new JButton("Refresh");
		result.add(refreshB);
		result.add(exitB);
		
		exitB.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}});
		exitB.setToolTipText("Exit this application");
		
		refreshB.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				sa.refresh();
				obsProjectTM.refresh();
				obsUnitSetTM.refresh();
				schedBlockTM.refresh();
				obsProjectTM.fireTableDataChanged();
				obsUnitSetTM.fireTableDataChanged();
				schedBlockTM.fireTableDataChanged();
			}});
		refreshB.setToolTipText("Refresh the table contents from the archives");
		
		return result;
	}
	
	private boolean canAllTransition(String to) {
		final int[] viewRows = currentTable.getSelectedRows();
		for (final int viewRow : viewRows) {
			final int modelRow = currentTable.convertRowIndexToModel(viewRow);
			if (!currentModel.canTransition(modelRow, to)) {
				return false;
			}
		}
		return viewRows.length > 0;
	}

	private void updateStateButtons() {
		for (final JButton b : stateBs) {
			b.setEnabled(canAllTransition(b.getText()));
		}
	}

	private void doStateChange(String to) {
		final int[] viewRows = currentTable.getSelectedRows();
		final int total = viewRows.length;
		int done = 0;
		for (final int viewRow : viewRows) {
			final int modelRow = currentTable.convertRowIndexToModel(viewRow);
			currentModel.setStatus(modelRow, to);
			done ++;
			if (done % 100 == 0) {
				sa.logger.info(String.format(
						"Set %d / %d %ses to %s",
						done,
						total,
						currentModel.getEntityType(),
						to));
			}
		}
		if (done > 100) {
			sa.logger.info(String.format(
					"Set %d / %d %ses to %s",
					done,
					total,
					currentModel.getEntityType(),
					to));
		}
		currentModel.fireTableDataChanged();
		updateStateButtons();
	}
	
//	public ProjectDisplayGUI() throws Exception {
//		this(new StateArchiveDAO("Project Display"));
//	}
}
