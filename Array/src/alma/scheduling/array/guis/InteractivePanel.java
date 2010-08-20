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

package alma.scheduling.array.guis;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.RowFilter.Entry;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.table.TableRowSorter;

import alma.acs.gui.standards.StandardIcons;
import alma.scheduling.array.util.FilterSet;
import alma.scheduling.array.util.FilterSetPanel;
import alma.scheduling.datamodel.executive.Executive;
import alma.scheduling.datamodel.obsproject.ObsProject;
import alma.scheduling.datamodel.obsproject.ObsUnitSet;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.SchedBlockControl;
import alma.scheduling.datamodel.obsproject.SchedBlockState;
import alma.scheduling.datamodel.obsproject.ScienceGrade;
import alma.scheduling.swingx.CallbackFilter;

/**
 *
 * @author dclarke
 * $Id: InteractivePanel.java,v 1.6 2010/08/20 19:27:31 rhiriart Exp $
 */
@SuppressWarnings("serial")
public class InteractivePanel extends AbstractArrayPanel
											implements ChangeListener, MouseListener {
	/*
	 * ================================================================
	 * Constants
	 * ================================================================
	 */
	private final static String BlankLabel = " ";
	private final static int popupLimit = 10;
	/* End Constants
	 * ============================================================= */
	
	
	
	/*
	 * ================================================================
	 * Fields for widgets &c
	 * ================================================================
	 */
	/** The LayoutManager we're using. */
	private GridBagLayout l;
	private GridBagConstraints c;
	
	/** Used to show a summary of the ObsProject filters */
	private JLabel opFilterSummary;
	/** Used to invoke a change to the ObsProject filters */
	private JButton opFilterChange;
	/** Used to reset the ObsProject filters to their default */
	private JButton opFilterReset;
	/** The widget in which we show the ObsProjects */
	private JTable opTable;
	/** The model behind the ObsProjects' table */
	private ObsProjectTableModel opModel;
	/** the thing which sorts the ObsProjects in our table */
	private TableRowSorter<ObsProjectTableModel> opSorter;
	/** The filters we use to select which ObsProjects to show */
	private FilterSet opFilters;
	/** The panel for editing the ObsProject filters */
	private FilterSetPanel opFilterPanel;
	/** Used to convey information concerning ObsProjects stuff */
	private JLabel opMessage;
	/** opTable pop-up menu */
	private JPopupMenu opPopup;
	/** opTable pop-up menu item for details of the OP under the mouse */
	private JMenuItem opDetailsHere;
	/** The OP under the mouse (only valid when opPopup is active) */
	private String opHere = null;
	
	/** Used to show a summary of the SchedBlock filters */
	private JLabel sbFilterSummary;
	/** Used to invoke a change to the SchedBlock filters */
	private JButton sbFilterChange;
	/** Used to reset the SchedBlock filters to their default */
	private JButton sbFilterReset;
	/** The widget in which we show the SchedBlocks */
	private JTable sbTable;
	/** The model behind the SchedBlocks' table */
	private SchedBlockTableModel sbModel;
	/** the thing which sorts the SchedBlocks in our table */
	private TableRowSorter<SchedBlockTableModel> sbSorter;
	/** The filters we use to select which SchedBlocks to show */
	private FilterSet sbFilters;
	/** The panel for editing the SchedBlock filters */
	private FilterSetPanel sbFilterPanel;
	/** Used to convey information concerning SchedBlocks stuff */
	private JLabel sbMessage;
	/** sbTable pop-up menu */
	private JPopupMenu sbPopup;
	/** sbTable pop-up menu item for details of the SB under the mouse */
	private JMenuItem sbDetailsHere;
	/** sbTable pop-up menu item to queue the SB under the mouse */
	private JMenuItem sbQueueHere;
	/** The SB under the mouse (only valid when sbPopup is active) */
	private String sbHere = null;
	/** The filter in use to select SBs by Project Id, null if none */
	private RowFilter<SchedBlockTableModel, Integer> sbFilterFromOPTable;

	/** Used to convey information to the user */
	private JLabel statusMessage;
	/** Button to initiate getting updates from the project store */
	private JButton update;
	/* End Fields for widgets &c
	 * ============================================================= */
	
	
	
	/*
	 * ================================================================
	 * Constructors and GUI building
	 * ================================================================
	 */
	/**
	 * Basic constructor.
	 */
	public InteractivePanel() {
		super();
		createWidgets();
		createLayoutManager();
		addWidgets();
		showConnectivity();
	}
	
	/**
	 * Build the LayoutManager we plan to use.
	 */
	private void createLayoutManager() {
		l = new GridBagLayout();
		c = new GridBagConstraints();
		c.ipadx = 2;
		c.ipady = 2;
		this.setLayout(l);
	}
	
	/**
	 * Create a button with the given label and tooltip.
	 * 
	 * @param label
	 * @param tooltip
	 * @return
	 */
	private JButton newButton(String label, String tooltip) {
		final JButton result = new JButton(label);
		result.setToolTipText(tooltip);
		return result;
	}
	
	/**
	 * Create those widgets which we want to keep track of.
	 */
	private void createWidgets() {
		opModel = new ObsProjectTableModel();
		opTable = new JTable(opModel);
		opSorter = new TableRowSorter<ObsProjectTableModel>(opModel);
		opTable.setRowSorter(opSorter);
		opFilters = new FilterSet(opModel);
		opFilterSummary = new JLabel(opFilters.toHTML());
		opFilterChange = newButton("Change", "Edit the filters to control which Projects are displayed");
		opFilterReset = newButton("Reset", "Reset the filters to display all Projects");
		opFilterPanel = FilterSetPanel.createGUI(
				"Filters for the Project Table",
				opFilters);
		opMessage = new JLabel(BlankLabel);
		
		addFilterListeners(opFilterChange,
				           opFilterReset,
				           opFilterPanel);
		opFilters.addChangeListener(this);
		opTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		
		sbModel = new SchedBlockTableModel();
		sbTable = new JTable(sbModel);
		sbSorter = new TableRowSorter<SchedBlockTableModel>(sbModel);
		sbTable.setRowSorter(sbSorter);
		sbFilters = new FilterSet(sbModel);
		sbFilterSummary = new JLabel();
		sbFilterChange = newButton("Change", "Edit the filters to control which SchedBlocks are displayed");
		sbFilterReset = newButton("Reset", "Reset the filters to display all SchedBlocks");
		sbFilterPanel = FilterSetPanel.createGUI(
				"Filters for the SchedBlock Table",
				sbFilters);
		sbMessage = new JLabel(BlankLabel);
		
		opTable.addMouseListener(this);
		sbTable.addMouseListener(this);
		
		addFilterListeners(sbFilterChange,
				   		   sbFilterReset,
				   		   sbFilterPanel);
		sbFilters.addChangeListener(this);

		sbFilterFromOPTable = null;
		addLinkingListener(opTable, opSorter, sbTable);

		update = newButton("Update",
				"Get new project data from the repositories");
		update.setEnabled(false);
		statusMessage = new JLabel(BlankLabel);
		
		createPopups();
	}
	
	/**
	 * Create the pop-up menus for the two tables.
	 */
	private void createPopups() {
		JMenuItem detailsSelected;
		JMenuItem change;
		JMenuItem reset;
		JMenuItem queueSelected;
		
		// Project table menu
		opPopup = new JPopupMenu("Projects");
		detailsSelected = new JMenuItem(menuStringDetailsSelection(opTable));
		opDetailsHere = new JMenuItem("Details");
		reset   = new JMenuItem("Reset Project Filters");
		change  = new JMenuItem("Change Project Filters");

		opTable.getSelectionModel().addListSelectionListener(detailsListener(detailsSelected, opTable));
		detailsSelected.addActionListener(detailsActionListener(opTable, opModel));
		detailsSelected.setEnabled(false);
		opDetailsHere.addActionListener(opDetailsHereListener());
		addFilterListeners(change, reset, opFilterPanel);
		detailsSelected.setEnabled(false);

		opPopup.add(detailsSelected);
		opPopup.add(opDetailsHere);
		opPopup.addSeparator();
		opPopup.add(reset);
		opPopup.add(change);
		
		// SchedBlock table menu
		sbPopup = new JPopupMenu("SchedBlocks");
		detailsSelected = new JMenuItem(menuStringDetailsSelection(sbTable));
		sbDetailsHere = new JMenuItem("Details");
		queueSelected = new JMenuItem(menuStringQueueSelection());
		sbQueueHere   = new JMenuItem("Queue");
		
		sbTable.getSelectionModel().addListSelectionListener(detailsListener(detailsSelected, sbTable));
		sbTable.getSelectionModel().addListSelectionListener(queueListener(queueSelected, sbTable));
		detailsSelected.addActionListener(detailsActionListener(sbTable, sbModel));
		sbDetailsHere.addActionListener(sbDetailsHereListener());
		sbQueueHere.addActionListener(sbQueueHereListener());
		queueSelected.addActionListener(queueActionListener());
		detailsSelected.setEnabled(false);
		queueSelected.setEnabled(false);
		
		sbPopup.add(detailsSelected);
		sbPopup.add(sbDetailsHere);
		sbPopup.addSeparator();
		sbPopup.add(queueSelected);
		sbPopup.add(sbQueueHere);
	}

	/**
	 * Add a "normal" widget to the display - i.e. one which takes up
	 * a single cell on the layout's grid. Note: because this uses the
	 * same GridBagConstraints object that other add*Widget() methods
	 * use, we need to be careful to set all the constraints that may
	 * have been set in them (and vice versa).
	 */
	private void addSingleWidget(JComponent widget,
								 int        x,
								 int        y,
								 double     wx,
								 double     wy) {
		c.gridx = x;
		c.gridy = y;
		c.gridwidth  = 1;
		c.gridheight = 1;
		c.weightx = wx;
		c.weighty = wy;
		if (x == 0) {
			c.anchor = GridBagConstraints.WEST;
		} else if (x == 1) {
			c.anchor = GridBagConstraints.CENTER;
		} else {
			c.anchor = GridBagConstraints.EAST;
		}
		c.fill = GridBagConstraints.BOTH;
		l.setConstraints(widget, c);
		add(widget);
	}
	
	/**
	 * Add a widget to the display which takes up the rest of a row on
	 * the layout's grid. Note: because this uses the same
	 * GridBagConstraints object that other add*Widget() methods use,
	 * we need to be careful to set all the constraints that may have
	 * been set in them (and vice versa).
	 */
	private void addFullWidthWidget(JComponent widget,
			                        int        x,
			                        int        y,
			                        double     wx,
			                        double     wy,
			                        boolean    scrollRequired) {
		c.gridx = x;
		c.gridy = y;
		c.gridwidth  = GridBagConstraints.REMAINDER;
		c.gridheight = 1;
		c.weightx = wx;
		c.weighty = wy;
		c.anchor = GridBagConstraints.CENTER;
		c.fill = GridBagConstraints.BOTH;
		
		if (scrollRequired) {
			JScrollPane scroll = new JScrollPane(widget);
			l.setConstraints(scroll, c);
			add(scroll);
		} else {
			l.setConstraints(widget, c);
			add(widget);
		}
	}
	
	/**
	 * Add blank row to the display. Note: because this uses the same
	 * GridBagConstraints object that other add*Widget() methods use,
	 * we need to be careful to set all the constraints that may have
	 * been set in them (and vice versa).
	 */
	private void addVerticalSpacer(int y) {
		c.gridx = 0;
		c.gridy = y;
		c.gridwidth  = GridBagConstraints.REMAINDER;
		c.gridheight = 1;
		c.weightx = 1.0;
		c.weighty = 0.0;
		c.anchor = GridBagConstraints.CENTER;
		c.fill = GridBagConstraints.BOTH;
		
		final JLabel spacer = new JLabel(BlankLabel);
		l.setConstraints(spacer, c);
		add(spacer);
	}
	
	/**
	 * Add the widgets to the display.
	 */
	private void addWidgets() {
		int x = 0;
		int y = 0;
		
		addSingleWidget(new JLabel("Projects"), x++, y, 0.0, 0.0);
		addSingleWidget(opFilterSummary,        x++, y, 1.0, 0.0);
		addSingleWidget(opFilterReset,          x++, y, 0.0, 0.0);
		addSingleWidget(opFilterChange,         x++, y, 0.0, 0.0);
		
		x = 0 ; y ++; // New row
		addFullWidthWidget(opTable, x++, y, 1.0, 1.0, true);
		
		x = 0 ; y ++; // New row
		addFullWidthWidget(opMessage, x++, y, 1.0, 0.0, false);
		
		x = 0 ; y ++; // New row
		addVerticalSpacer(y);
		
		x = 0 ; y ++; // New row
		addSingleWidget(new JLabel("SchedBlocks"), x++, y, 0.0, 0.0);
		addSingleWidget(sbFilterSummary,           x++, y, 1.0, 0.0);
//		addSingleWidget(sbFilterReset,             x++, y, 0.0, 0.0);
//		addSingleWidget(sbFilterChange,            x++, y, 0.0, 0.0);
		
		x = 0 ; y ++; // New row
		addFullWidthWidget(sbTable, x++, y, 1.0, 0.3, true);
		
		x = 0 ; y ++; // New row
		addFullWidthWidget(sbMessage, x++, y, 1.0, 0.0, false);
		
		x = 0 ; y ++; // New row
		addSingleWidget(update, x++, y, 0.0, 0.0);
		addFullWidthWidget(statusMessage, x++, y, 1.0, 0.0, false);
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.array.guis.AbstractArrayPanel#arrayAvailable()
	 */
	@Override
	protected void arrayAvailable() {
		showConnectivity();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.array.guis.AbstractArrayPanel#arrayAvailable()
	 */
	@Override
	protected void modelsAvailable() {
		fakeData(opModel, sbModel);
		showConnectivity();
	}
	/* End Constructors and GUI building
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * Filters
	 * ================================================================
	 */
	/**
	 * Create a RowFilter<> for the SchedBlock table which will include
	 * any SchedBlock which belongs to one of the projects whose Entity
	 * Id is in the set <code>pids</code>.
	 * 
	 * @param pids
	 * @return
	 */
	private RowFilter<SchedBlockTableModel, Integer> rowFilterForSBs(
			final Set<String> pids) {
		final CallbackFilter.Callee callee =
			new CallbackFilter.Callee() {
			@Override
			public boolean include(
					Entry<? extends Object, ? extends Object> value,
					int index) {
				final String pid =
					value.getValue(SchedBlockTableModel.
							projectIdColumn()).toString();
				return pids.contains(pid);
			}
		};

		return CallbackFilter.callbackFilter(
				callee,
				SchedBlockTableModel.projectIdColumn());
	}

	/**
	 * Create the a filter for the SchedBlock table based upon the
	 * state of the ProjectTable.
	 * <ul>
	 * <li>if their are items selected in the project table, then
	 *     the filter should include all the SchedBlocks for those
	 *     Projects.
	 * <li>if their is no selection in the project table, then the
	 *     filter should include all the SchedBlocks for all the
	 *     Projects shown in the project table (which is filtered, so
	 *     be careful to start from the view rows).
	 * </ul>
	 * 
	 * @param opTable
	 */
	private void setFilterForSBs(JTable opTable) {
		final Set<Integer> viewRows = new HashSet<Integer>();
		final Set<String> pids = new HashSet<String>();
		final ObsProjectTableModel model
						= (ObsProjectTableModel)opTable.getModel();
		
		if (opTable.getSelectedRows().length != 0) {
			// There is a selection in the table, use the selected rows
			int[] vr = opTable.getSelectedRows();
			for (int viewRow : vr) {
				viewRows.add(viewRow);
			}
		} else {
			// No selection, used all the view rows.
			final int numViewRows = opSorter.getViewRowCount();
			for (int viewRow = 0; viewRow < numViewRows; viewRow++) {
				viewRows.add(viewRow);
			}
		}
		
		for (int viewRow : viewRows) {
			try {
				int modelRow = opTable.convertRowIndexToModel(viewRow);
				pids.add(model.getProjectId(modelRow));
			} catch (ArrayIndexOutOfBoundsException e) {
			}
		}
		sbFilterFromOPTable = rowFilterForSBs(pids);
		sbSorter.setRowFilter(sbFilterFromOPTable);
	}
	
	/**
	 * Add listeners to the opTable which will maintain the filters on
	 * the sbTable.
	 * 
	 * @param opTable
	 * @param opSorter
	 * @param sbTable
	 */
	private void addLinkingListener(
			final JTable                               opTable,
			final TableRowSorter<ObsProjectTableModel> opSorter,
			final JTable                               sbTable) {
		opTable.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {
					@Override
					public void valueChanged(ListSelectionEvent e) {
						setFilterForSBs(opTable);
					}});
		opSorter.addRowSorterListener(new RowSorterListener() {
			@Override
			public void sorterChanged(RowSorterEvent e) {
				setFilterForSBs(opTable);
			}
		});
	}

	/**
	 * Add listeners to the given buttons to allow them to control the
	 * given filter panel accordingly. We use <code>AbstractButton
	 * </code> as the type of the button arguments so that we can also
	 * use this for <code>JMenuItem</code>s.
	 *
	 * @param change - the button to pop-up the panel
	 * @param reset  - the button to reset the panel
	 * @param panel  - the control panel for the filters
	 */
	private void addFilterListeners(final AbstractButton change,
			                        final AbstractButton reset,
			                        final FilterSetPanel panel) {
		
		change.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				panel.setVisible(true);
			}});
		reset.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				panel.reset();
			}});
	}
	/* End Filters
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * Detail dialogues
	 * ================================================================
	 */
	/**
	 * Create the label for the pop-up menu item which gives the
	 * details of the selected elements in <code>table</code>.
	 * 
	 * @param table
	 * @return
	 */
	private String menuStringDetailsSelection(JTable table) {
		final String singular = (table.getSelectedRowCount() == 1)? "": "s";
		String result;
		
		if (table == opTable) {
			result = String.format("Details of Selected Project%s", singular);
		} else {
			result = String.format("Details of Selected SchedBlock%s", singular);
		}
		return result;
	}
	
	/**
	 * Create the label for the pop-up menu item which gives the
	 * details of the given element in <code>table</code>.
	 * 
	 * @param table
	 * @return
	 */
	private String menuStringDetailsHere(JTable table) {
		String result;
		
		if (table == opTable) {
			result = String.format("Details of Project %s", opHere);
		} else {
			result = String.format("Details of SchedBlock %s", sbHere);
		}
		return result;
	}
	
	/**
	 * Create a listener to control the availability of the given
	 * JMenuItem based on there being something selected in the
	 * given JTable.
	 * 
	 * @param item
	 * @param table
	 * @return
	 */
	private ListSelectionListener detailsListener(final JMenuItem item,
												  final JTable table) {
		final ListSelectionListener result = new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				item.setEnabled(table.getSelectedRowCount() > 0);
				item.setText(menuStringDetailsSelection(table));
			}
		};
		return result;
	}

	/**
	 * Work out if it's OK to pop up a number of windows. It is if
	 * either there are less than the <code>popupLimit</code> or if the
	 * user says it's OK.
	 * 
	 * @param number
	 * @param whats
	 * @return
	 */
	private boolean okToDo(int number, String whats) {
		if (number < popupLimit) {
			return true;
		}
		String question = String.format(
				"There are %d %s selected, %s%n%s",
				number, whats,
				"each of which will be displayed separately",
				"Do you really want to open that many windows?");
		return JOptionPane.showConfirmDialog(
				this,
				question,
				"Confirmation Required",
				JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
	}
	
	/**
	 * Create a listener to pop-up details of the projects selected in
	 * the given table.
	 * 
	 * @param table
	 * @param model
	 * @return
	 */
	private ActionListener detailsActionListener(final JTable table,
											     final ObsProjectTableModel model) {
		final ActionListener result = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int[] viewRows = table.getSelectedRows();
				
				if (okToDo(viewRows.length, "Projects")) {
					final Set<String> ids = new HashSet<String>();

					for (int viewRow : viewRows) {
						try {
							int modelRow = table.convertRowIndexToModel(viewRow);
							ids.add(model.getProjectId(modelRow));
						} catch (ArrayIndexOutOfBoundsException ex) {
						}
					}
					for (final String id : ids) {
						showObsProjectDetails(id);
					}
				}
			}
		};
		return result;
	}
	
	/**
	 * Create a listener to pop-up details of the SchedBlocks selected
	 * in the given table.
	 * 
	 * @param table
	 * @param model
	 * @return
	 */
	private ActionListener detailsActionListener(final JTable table,
											     final SchedBlockTableModel model) {
		final ActionListener result = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int[] viewRows = table.getSelectedRows();

				if (okToDo(viewRows.length, "SchedBlocks")) {
					final Set<String> ids = new HashSet<String>();

					for (int viewRow : viewRows) {
						try {
							int modelRow = table.convertRowIndexToModel(viewRow);
							ids.add(model.getSchedBlockId(modelRow));
						} catch (ArrayIndexOutOfBoundsException ex) {
						}
					}
					for (final String id : ids) {
						showSchedBlockDetails(id);
					}
				}
			}
		};
		return result;
	}
	
	/**
	 * Create a listener to show details of the ObsProject over which
	 * the pop-up menu was popped up. Which ObsProject that is will be
	 * worked out in the handling of the mouse event which triggers the
	 * menu.
	 * 
	 * @return
	 */
	private ActionListener opDetailsHereListener() {
		final ActionListener result = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (opHere != null) {
					showObsProjectDetails(opHere);
				}
			}
		};
		return result;
	}
	
	/**
	 * Create a listener to show details of the SchedBlock over which
	 * the pop-up menu was popped up. Which SchedBlock that is will be
	 * worked out in the handling of the mouse event which triggers the
	 * menu.
	 * 
	 * @return
	 */
	private ActionListener sbDetailsHereListener() {
		final ActionListener result = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (sbHere != null) {
					showSchedBlockDetails(sbHere);
				}
			}
		};
		return result;
	}
	
	private void showSchedBlockDetails(String entityId) {
		System.out.format("Show details of SchedBlock %s%n", entityId);
	}
	
	private void showObsProjectDetails(String entityId) {
		System.out.format("Show details of ObsProject %s%n", entityId);
	}
	/* End Detail dialogues
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * Queue interactions
	 * ================================================================
	 */
	/**
	 * Create the label for the pop-up menu item which queues the
	 * selected SchedBlocks.
	 * 
	 * @return
	 */
	private String menuStringQueueSelection() {
		final String singular = (sbTable.getSelectedRowCount() == 1)? "": "s";
		return String.format("Queue Selected SchedBlock%s", singular);
	}
	
	/**
	 * Create the label for the pop-up menu item which queues the
	 * SchedBlock under the cursor.
	 * 
	 * @return
	 */
	private String menuStringQueueHere() {
		return String.format("Queue SchedBlock %s", sbHere);
	}

	/**
	 * Create a listener to control the availability of the given
	 * JMenuItem based on there being something selected in the
	 * given JTable, but not too many things.
	 * 
	 * @param item
	 * @param table
	 * @return
	 */
	private ListSelectionListener queueListener(final JMenuItem item, final JTable table) {
		final ListSelectionListener result = new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				item.setEnabled(isControl() &&
						(table.getSelectedRows().length > 0));
				// TODO: also have to disable the item if there are
				//       more items selected than there is capacity
				//       available in the SB queue.
			}
		};
		return result;
	}
	
	/**
	 * Create a listener to queue the SchedBlocks selected in the given
	 * table.
	 * 
	 * @param table
	 * @param model
	 * @return
	 */
	private ActionListener queueActionListener() {
		final ActionListener result = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int[] viewRows = sbTable.getSelectedRows();
				final Set<String> pids = new HashSet<String>();

				for (int viewRow : viewRows) {
					try {
						int modelRow = sbTable.convertRowIndexToModel(viewRow);
						pids.add(sbModel.getSchedBlockId(modelRow));
					} catch (ArrayIndexOutOfBoundsException ex) {
					}
				}
				for (final String pid : pids) {
					queueSchedBlock(pid);
				}
			}
		};
		return result;
	}
	
	/**
	 * Create a listener to queue the SchedBlock over which the pop-up
	 * menu was popped up. Which SchedBlock that is will be worked out
	 * in the handling of the mouse event which triggers the menu.
	 * 
	 * @param table
	 * @param model
	 * @return
	 */
	private ActionListener sbQueueHereListener() {
		final ActionListener result = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (sbHere != null) {
					queueSchedBlock(sbHere);
				}
			}
		};
		return result;
	}
	
	private void queueSchedBlock(String entityId) {
		System.out.format("Queue SchedBlock %s%n", entityId);
	}
	/* End Queue interactions
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * Messages to the user
	 * ================================================================
	 */
	private void setOPFilterSummary(String message) {
		opFilterSummary.setText(message);
	}
	
	private void setOPMessage(String message) {
		opMessage.setText(message);
	}
	
	private void setSBFilterSummary(String message) {
		sbFilterSummary.setText(message);
	}
	
	private void setSBMessage(String message) {
		sbMessage.setText(message);
	}
	
	private void setStatusMessage(String message) {
		statusMessage.setText(message);
	}
	
	private void showConnectivity() {
		StringBuilder b = new StringBuilder();
		String sep = "";
		
		b.append("<html>");
		if (getArray() == null) {
			b.append("Waiting for connection to array");
			sep = "<br>";
		}
		if (getModels() == null) {
			b.append(sep);
			b.append("Waiting for connection to project data");
		}
		b.append("</html>");
		setStatusMessage(b.toString());
	}
	/* End Messages to the user
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * Listening to the filters (and anything else)
	 * ================================================================
	 */
	/*
	 * Listening to the FilterSets.
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == opFilters) {
			opSorter.setRowFilter(opFilters.rowFilter());
			setOPFilterSummary(opFilters.toHTML());
		} else if (e.getSource() == sbFilters) {
//			setSBFilterSummary(sbFilters.toHTML());
		}
	}
	
	/**
	 * Hypothetical at the moment, but listening to the project store
	 * @param e
	 */
	public void archiveChanged(ChangeEvent e) {
		statusMessage.setIcon(StandardIcons.IDEA.icon);
		setStatusMessage("New project information available in the repositories.");
	}
	
	/**
	 * Hypothetical at the moment, but listening to the project store
	 * @param e
	 */
	public void archiveUpToDate() {
		update.setIcon(null);
		setStatusMessage("");
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		final JTable table = (JTable) e.getSource();
		final int viewRow = table.rowAtPoint(e.getPoint());
		final int modelRow = table.convertRowIndexToModel(viewRow);
		if (e.getClickCount() == 2 &&
				SwingUtilities.isLeftMouseButton(e)) {
			// It's a double click of the left button.
			try {
				String id;
				if (table == opTable) {
					ObsProjectTableModel model = (ObsProjectTableModel) table.getModel();
					id = model.getProjectId(modelRow);
					showObsProjectDetails(id);
				} else if (table == sbTable) {
					SchedBlockTableModel model = (SchedBlockTableModel) table.getModel();
					id = model.getSchedBlockId(modelRow);
					showSchedBlockDetails(id);
				}
			} catch (ArrayIndexOutOfBoundsException ex) {
			}
		}
	}
	@Override
	public void mouseEntered(MouseEvent e) { /* don't care */ }

	@Override
	public void mouseExited(MouseEvent e) { /* don't care */ }

	@Override
	public void mousePressed(MouseEvent e) {
		if (e.isPopupTrigger()) {
			final JTable table = (JTable) e.getSource();
			final int viewRow = table.rowAtPoint(e.getPoint());
			final int modelRow = table.convertRowIndexToModel(viewRow);
			
			if (table == opTable) {
				final ObsProjectTableModel model = (ObsProjectTableModel)table.getModel();
				opHere = model.getProjectId(modelRow);
				opDetailsHere.setText(menuStringDetailsHere(table));
				opPopup.show(e.getComponent(), e.getX(), e.getY());
			} else if (table == sbTable) {
				final SchedBlockTableModel model = (SchedBlockTableModel)table.getModel();
				sbHere = model.getSchedBlockId(modelRow);
				sbDetailsHere.setText(menuStringDetailsHere(table));
				sbQueueHere.setText(menuStringQueueHere());
				sbQueueHere.setEnabled(isControl());
				sbPopup.show(e.getComponent(), e.getX(), e.getY());
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) { /* don't care */ }
	/* End Listening to the filters (and anything else)
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * Fake data
	 * ================================================================
	 */
	final static int numFakeOP = 2500;
	private static final String pi[] = {
		"David", "David", "David", "Andrew", "Andrew",
		"Simon", "Simon", "Andrew", "Simon", "Mike",
		"Mike", "Mike"
	};
	
	private static final String ex[] = {
		"North America", "North America", "Chile", "Japan", "Japan",
		"Europe", "Europe", "Chile", "Japan", "Europe",
		"North America", "Japan"
	};
	
	private static final int sbPerOP[] = {
		1, 1, 2, 1, 1, 1, 1, 4, 1, 6, 1, 1, 21, 1, 1, 1
	};
	
	private void fakeData(ObsProjectTableModel opModel,
			              SchedBlockTableModel sbModel) {
		final Collection<ObsProject> ops = new ArrayList<ObsProject>();
		final Collection<SchedBlock> sbs = new ArrayList<SchedBlock>();
		
		final Map<String, Executive> exs = new HashMap<String, Executive>();
		for (String x : ex) {
			if (!exs.containsKey(x)) {
				final Executive e = new Executive();
				e.setName(x);
				e.setDefaultPercentage((float)100);
				exs.put(x, e);
			}
		}
		
		int sc = 0;
		
		for (int p = 0; p < numFakeOP; p++, sc++) {
			final ObsProject op = new ObsProject();
			op.setUid(String.format("uid://X007/X%04x/X01", p));
			op.setPrincipalInvestigator(pi[p % pi.length]);
			op.setScienceScore((float)Math.sqrt(p));
			op.setScienceRank(numFakeOP - p);
			op.setLetterGrade(ScienceGrade.values()[p % ScienceGrade.values().length]);
			op.setStatus("READY");
			op.setTotalExecutionTime(Math.sqrt(p) * p * p);
			ops.add(op);
			
			final ObsUnitSet ous = new ObsUnitSet();
			ous.setUid(String.format("ous%04X", sc));
			
			final int numSBs = sbPerOP[p % sbPerOP.length];
			for (int s = 0; s < numSBs; s++) {
				final SchedBlock sb = new SchedBlock();
				sb.setUid(String.format("uid://X007/X%04x/X%02x", p, s+2));
				sb.setPiName(pi[p % pi.length]);
				sb.setExecutive(exs.get(ex[sc % ex.length]));
				
				final SchedBlockControl sbc = new SchedBlockControl();
				sbc.setAccumulatedExecutionTime(op.getTotalExecutionTime()/numSBs);
				sbc.setState(SchedBlockState.READY);
				
				sb.setSchedBlockControl(sbc);
				ous.addObsUnit(sb);
				sbs.add(sb);
			}
			
			op.setObsUnit(ous);
			ous.setProject(op);
		}
		opModel.setData(ops);
		sbModel.setData(sbs);
	}
	/* End Fake data
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * Running stand-alone
	 * ================================================================
	 */
    private static InteractivePanel createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("Interactive Panel");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Add the ubiquitous "Hello World" label.
        InteractivePanel panel = new InteractivePanel();
        frame.getContentPane().add(panel);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
        return panel;
    }

	/**
	 * @param args
	 */
    public static void main(String[] args) {
    	//Schedule a job for the event-dispatching thread:
    	//creating and showing this application's GUI.
    	javax.swing.SwingUtilities.invokeLater(new Runnable() {
    		public void run() {
    			InteractivePanel p = createAndShowGUI();
    			try {
    				p.runRestricted(false);
    			} catch (Exception e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
    			p.arrayAvailable();
    			p.modelsAvailable();
    		}
    	});
    }
	/*
	 * End Running stand-alone
	 * ============================================================= */
}
