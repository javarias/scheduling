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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.RowFilter.Entry;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.table.TableRowSorter;

import alma.JavaContainerError.wrappers.AcsJContainerServicesEx;
import alma.acs.gui.standards.StandardIcons;
import alma.scheduling.ArrayGUIOperation;
import alma.scheduling.SchedBlockQueueItem;
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
 * $Id: InteractivePanel.java,v 1.10 2011/02/04 17:19:36 javarias Exp $
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
    /** Title for the project part of the display. */
    private JLabel opTitle;
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
    
    /** Title for the schedblock part of the display. */
    private JLabel sbTitle;
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
    //	private FilterSetPanel sbFilterPanel;
    /** Used to convey information concerning SchedBlocks stuff */
    private JLabel sbMessage;
    /** sbTable pop-up menu */
    private JPopupMenu sbPopup;
    /** sbTable pop-up menu item for details of the SB under the mouse */
    private JMenuItem sbDetailsHere;
    /** sbTable pop-up menu item for details of the selected SB(s) */
    private JMenuItem sbDetailsSelected;
    /** sbTable pop-up menu item to queue the SB under the mouse */
    private JMenuItem sbQueueHere;
    /** sbTable pop-up menu item to queue the selected SB(s) */
    private JMenuItem sbQueueSelected;
    /** The SB under the mouse (only valid when sbPopup is active) */
    private String sbHere = null;
    /** The filter in use to select SBs by Project Id, null if none */
    private RowFilter<SchedBlockTableModel, Integer> sbFilterFromOPTable;
    
    /** Used to convey information to the user */
    private JLabel statusMessage;
    /** Button to initiate getting updates from the project store */
    private JButton update;
    
    /** Do we Queue or Configure? */
    private String processSB = "Queue";
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
		System.out.format("%s (InteractivePanel).InteractivePanel()%n",
				this.getClass().getSimpleName() );
		createWidgets();
		addWidgets();
		showConnectivity();
		showProjectCounts();
	}
	
	/**
	 * Basic constructor.
	 */
	public InteractivePanel(String arrayName) {
		super(arrayName);
		System.out.format("%s (InteractivePanel).InteractivePanel(%s)%n",
				this.getClass().getSimpleName(),
				arrayName);
		createWidgets();
		addWidgets();
		showConnectivity();
		showProjectCounts();
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
		opTitle = new JLabel(String.format(
				"<html><font color=%s>Projects</font></html>",
				this.TitleColour));
		opModel = new ObsProjectTableModel();
		opTable = new JTable(opModel);
		opSorter = new TableRowSorter<ObsProjectTableModel>(opModel);
		opTable.setRowSorter(opSorter);
		opFilters = new FilterSet(opModel);
		opFilterSummary = new JLabel(opFilters.toHTML(
				this.NormalColour, this.DetailColour));
		opFilterChange = newButton("Filter", "Edit the filters to control which Projects are displayed");
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
		
		sbTitle = new JLabel(String.format(
				"<html><font color=%s>SchedBlocks</font></html>",
				this.TitleColour));
		sbModel = new SchedBlockTableModel();
		sbTable = new JTable(sbModel);
		sbSorter = new TableRowSorter<SchedBlockTableModel>(sbModel);
		sbModel.addSpecificComparators(sbSorter);
		sbTable.setRowSorter(sbSorter);
		sbFilters = new FilterSet(sbModel);
		sbFilterSummary = new JLabel();
		sbFilterChange = newButton("Change", "Edit the filters to control which SchedBlocks are displayed");
		sbFilterReset = newButton("Reset", "Reset the filters to display all SchedBlocks");
//		sbFilterPanel = FilterSetPanel.createGUI(
//				"Filters for the SchedBlock Table",
//				sbFilters);
		sbMessage = new JLabel(BlankLabel);
		
		opTable.addMouseListener(this);
		sbTable.addMouseListener(this);
		
//		addFilterListeners(sbFilterChange,
//				   		   sbFilterReset,
//				   		   sbFilterPanel);
		sbFilters.addChangeListener(this);

		sbFilterFromOPTable = null;
		addLinkingListener(opTable, opSorter, sbTable);

		update = newButton("Update",
				"Get new project data from the repositories");
		update.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				update.setEnabled(false);
				try {
					getData();
				} finally {
					update.setEnabled(true);
				}
			}});
		update.setEnabled(false);
		statusMessage = new JLabel(BlankLabel);
		
		createPopups();
		
		makeSameWidth(opTitle, sbTitle, update, opFilterReset, opFilterChange);
	}
	
	/**
	 * Create the pop-up menus for the two tables.
	 */
	private void createPopups() {
		JMenuItem detailsSelected;
		JMenuItem changeFilters;
		JMenuItem resetFilters;
		JMenuItem clearSelection;
		
		// Project table menu
		opPopup = new JPopupMenu("Projects");
		detailsSelected = new JMenuItem(menuStringDetailsSelection(opTable));
		opDetailsHere = new JMenuItem("Details");
		resetFilters   = new JMenuItem("Reset Project Filters");
		changeFilters  = new JMenuItem("Change Project Filters");
		clearSelection  = new JMenuItem("Clear Selection");

		opTable.getSelectionModel().addListSelectionListener(detailsListener(detailsSelected, opTable));
		detailsSelected.addActionListener(detailsActionListener(opTable, opModel));
		detailsSelected.setEnabled(false);
		opDetailsHere.addActionListener(opDetailsHereListener());
		addFilterListeners(changeFilters, resetFilters, opFilterPanel);
		detailsSelected.setEnabled(false);
		clearSelection.addActionListener(opClearSelectionListener());
		clearSelection.setEnabled(true);

		opPopup.add(detailsSelected);
		opPopup.add(opDetailsHere);
		opPopup.addSeparator();
		opPopup.add(resetFilters);
		opPopup.add(changeFilters);
		opPopup.addSeparator();
		opPopup.add(clearSelection);
		
		// SchedBlock table menu
		sbPopup = new JPopupMenu("SchedBlocks");
		sbDetailsSelected = new JMenuItem(menuStringDetailsSelection(sbTable));
		sbDetailsHere     = new JMenuItem("Details");
		sbQueueSelected   = new JMenuItem(menuStringQueueSelection());
		sbQueueHere       = new JMenuItem("Queue");
		
		sbDetailsSelected.addActionListener(detailsActionListener(sbTable, sbModel));
		sbDetailsHere.addActionListener(sbDetailsHereListener());
		sbQueueHere.addActionListener(sbQueueHereListener());
		sbQueueSelected.addActionListener(queueActionListener());
		sbDetailsSelected.setEnabled(false);
		sbQueueSelected.setEnabled(false);
		
		sbPopup.add(sbDetailsSelected);
		sbPopup.add(sbDetailsHere);
		sbPopup.addSeparator();
		sbPopup.add(sbQueueSelected);
		sbPopup.add(sbQueueHere);
	}

	/**
	 * Add a "normal" widget to the display - i.e. one which takes up
	 * a single cell on the layout's grid. Note: because this uses the
	 * same GridBagConstraints object that other add*Widget() methods
	 * use, we need to be careful to set all the constraints that may
	 * have been set in them (and vice versa).
	 */
	private void addSingleWidget(JPanel             panel,
			                     GridBagLayout      l,
			                     GridBagConstraints c,
			                     JComponent widget,
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
		panel.add(widget);
	}
	
	/**
	 * Add a widget to the display which takes up the rest of a row on
	 * the layout's grid. Note: because this uses the same
	 * GridBagConstraints object that other add*Widget() methods use,
	 * we need to be careful to set all the constraints that may have
	 * been set in them (and vice versa).
	 */
	private void addFullWidthWidget(JPanel             panel,
                                    GridBagLayout      l,
                                    GridBagConstraints c,
                                    JComponent widget,
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
			panel.add(scroll);
		} else {
			l.setConstraints(widget, c);
			panel.add(widget);
		}
	}
	
	/**
	 * Add the OP widgets to the given panel.
	 */
	private void addOPWidgets(JPanel             panel,
                              GridBagLayout      l,
                              GridBagConstraints c) {
		int x = 0;
		int y = 0;
		
		addSingleWidget(panel, l, c, opTitle,         x++, y, 0.0, 0.0);
		addSingleWidget(panel, l, c, opFilterSummary, x++, y, 1.0, 0.0);
		addSingleWidget(panel, l, c, opFilterChange,  x++, y, 0.0, 0.0);
		addSingleWidget(panel, l, c, opFilterReset,   x++, y, 0.0, 0.0);
		
		x = 0 ; y ++; // New row
		addFullWidthWidget(panel, l, c, opTable, x++, y, 1.0, 1.0, true);
		
		x = 0 ; y ++; // New row
		addFullWidthWidget(panel, l, c, opMessage, x++, y, 1.0, 0.0, false);
	}
	
	/**
	 * Add the SB widgets to the given panel.
	 */
	private void addSBWidgets(JPanel             panel,
                              GridBagLayout      l,
                              GridBagConstraints c) {
		int x = 0;
		int y = 0;
		
		addSingleWidget(panel, l, c, sbTitle, x++, y, 0.0, 0.0);
//		addSingleWidget(panel, l, c, sbFilterSummary,           x++, y, 1.0, 0.0);
//		addSingleWidget(panel, l, c, sbFilterReset,             x++, y, 0.0, 0.0);
//		addSingleWidget(panel, l, c, sbFilterChange,            x++, y, 0.0, 0.0);
		
		x = 0 ; y ++; // New row
		addFullWidthWidget(panel, l, c, sbTable, x++, y, 1.0, 0.3, true);
		
		x = 0 ; y ++; // New row
		addFullWidthWidget(panel, l, c, sbMessage, x++, y, 1.0, 0.0, false);
		
		x = 0 ; y ++; // New row
		addSingleWidget(panel, l, c, update, x++, y, 0.0, 0.0);
		addFullWidthWidget(panel, l, c, statusMessage, x++, y, 1.0, 0.0, false);
	}
	
	/**
	 * Add the widgets to the display.
	 */
	private void addWidgets() {
		JPanel opPanel = new JPanel();
		JPanel sbPanel = new JPanel();
		GridBagLayout      l = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		c.ipadx = 2;
		c.ipady = 2;
		
		opPanel.setLayout(l);
		addOPWidgets(opPanel, l, c);
		
		l = new GridBagLayout();
		c = new GridBagConstraints();
		sbPanel.setLayout(l);
		addSBWidgets(sbPanel, l, c);
		
		final JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
		split.setTopComponent(opPanel);
		split.setBottomComponent(sbPanel);
		split.setDividerLocation(2.0/3.0);
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(split);
	}

    /* (non-Javadoc)
     * @see alma.scheduling.array.guis.AbstractArrayPanel#arrayAvailable()
     */
    @Override
    protected void arrayAvailable() {
		System.out.format("%s (InteractivePanel).arrayAvailable() - %s is %s @ %h%n",
				this.getClass().getSimpleName(),
				arrayName,
				array.getClass().getSimpleName(),
				array.hashCode());
		if (getModels() != null) {
			getData();
		}
		// Set the selection model for the SB table according to the array type
		if (isManual()) {
			sbTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			processSB = "Configure array for";
		} else {
			sbTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			processSB = "Queue";
		}
		sbTable.getSelectionModel().addListSelectionListener(detailsListener(sbDetailsSelected, sbTable));
		sbTable.getSelectionModel().addListSelectionListener(queueListener(sbQueueSelected, sbTable));
		showConnectivity();
    }

    /* (non-Javadoc)
     * @see alma.scheduling.array.guis.AbstractArrayPanel#arrayAvailable()
     */
    @Override
    protected void modelsAvailable() {
	if (getArray() != null) {
	    getData();
	}
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
		showProjectCounts();
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
				showProjectCounts();
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
				panel.restoreWindow();
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
	 * Create a listener to clear the selection in the opTable.
	 * 
	 * @return
	 */
	private ActionListener opClearSelectionListener() {
		final ActionListener result = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				opTable.getSelectionModel().clearSelection();
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
	    return String.format("%s Selected SchedBlock%s", processSB, singular);
	}
	
	/**
	 * Create the label for the pop-up menu item which queues the
	 * SchedBlock under the cursor.
	 * 
	 * @return
	 */
	private String menuStringQueueHere() {
	    return String.format("%s SchedBlock %s", processSB, sbHere);
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
						(table.getSelectedRows().length > 0) &&
						(spaceOnQueue(table.getSelectedRows().length)));
				item.setText(menuStringQueueSelection());
			}
		};
		return result;
	}
	
    /**
     * Deconfigure the previous SchedBlock Ideally this should involve
     * a wee bit of switching on and off of the execution queue so
     * that the previous doesn't get left marked as Running
     */
    private void deconfigurePrevious() {
	final ArrayAccessor array = getArray();
	final String name = getUserName();
	final String role = getUserRole();
	array.stopRunningSchedBlock(name, role);
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
				final List<String> pids = new ArrayList<String>();

				for (int viewRow : viewRows) {
					try {
						int modelRow = sbTable.convertRowIndexToModel(viewRow);
						pids.add(sbModel.getSchedBlockId(modelRow));
					} catch (ArrayIndexOutOfBoundsException ex) {
					}
				}
				if (isManual()) {
				    deconfigurePrevious();
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
			    if (isManual()) {
				deconfigurePrevious();
			    }
			    queueSchedBlock(sbHere);
			}
		    }
		};
	    return result;
	}
	
	private void queueSchedBlock(String entityId) {
		final long time = System.nanoTime();
		final SchedBlockQueueItem item =
			new SchedBlockQueueItem(time, entityId);
		getArray().push(item);
	}
	/* End Queue interactions
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * Miscellaneous operations kicked off by the GUI 
	 * ================================================================
	 */
	private class ProjectDataFetcher extends SwingWorker<Boolean, String> {

		private boolean wantManual;
		
		protected ProjectDataFetcher(boolean wantManual) {
			this.wantManual = wantManual;
		}
		@Override
		protected Boolean doInBackground() throws Exception {
			String opInfo = "Getting Project data";
			String sbInfo = "Getting SchedBlock data";
			safePublish(opInfo, sbInfo);
			
			final List<ObsProject> projects = getModels().getAllProjects(wantManual);
			opInfo = String.format("Got %d %sProject%s",
								   projects.size(),
								   wantManual? "Manual mode ": "",
								   projects.size()==1? "": "s");
			safePublish(opInfo, sbInfo);
			
			final List<SchedBlock> sbs = getModels().getAllSchedBlocks(wantManual);
			sbInfo = String.format("Got %d %sSchedBlock%s, hydrating them",
								   sbs.size(),
								   wantManual? "Manual mode ": "",
								   sbs.size()==1? "": "s");
			safePublish(opInfo, sbInfo);

			opModel.setData(projects);

			for (final SchedBlock sb : sbs) {
				getModels().hydrateSchedBlock(sb);
			}
			sbModel.setData(sbs);
			sbInfo = String.format("Got and hydrated %d %sSchedBlock%s",
					   sbs.size(),
					   wantManual? "Manual mode ": "",
					   sbs.size()==1? "": "s");
			safePublish(opInfo, sbInfo);
			return true;
		}

		private void safePublish(String op, String sb) {
			// Just makes sure there are two strings, as expected
			// by process().
			publish(op, sb);
		}
		
		@Override
		protected void done() {
			try { 
				showConnectivity();
				showProjectCounts();
				update.setEnabled(true);
			} catch (Exception ignore) {
			}
		}

	     @Override
	     protected void process(List<String> info) {
	    	 try {
	    		 setOPMessage(info.get(0));
	    		 setSBMessage(info.get(1));
	    	 } catch (Exception ignore) {}
	     }

	}
	
	/**
	 * Get the project and SB data from the ModelAccessor. Do the time
	 * consuming stuff in a separate thread like a good little Swing
	 * and OMC citizen.
	 */
	protected void getData() {
		if (getModels() != null) {
			update.setEnabled(false);
			final ProjectDataFetcher fetch = new ProjectDataFetcher(
					isManual());
			fetch.execute();
		} else {
			showConnectivity();
			showProjectCounts();
		}
	}
	/* End Miscellaneous operations kicked off by the GUI 
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
	
	private void showProjectCounts() {
		final int totalProjects = opModel.getRowCount();
		final int shownProjects = opTable.getRowCount();
		String l = String.format(
				"Showing %d project%s of %d",
				shownProjects,
				(shownProjects == 1)? "": "s",
				totalProjects);
		setOPMessage(l);
		final int totalSBs = sbModel.getRowCount();
		final int shownSBs = sbTable.getRowCount();
		l = String.format(
				"Showing %d SchedBlock%s of %d",
				shownSBs,
				(shownSBs == 1)? "": "s",
						totalSBs);
		setSBMessage(l);
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
			setOPFilterSummary(opFilters.toHTML(
					this.NormalColour, this.DetailColour));
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
				sbQueueHere.setEnabled(isControl() && spaceOnQueue(1));
				sbPopup.show(e.getComponent(), e.getX(), e.getY());
			}
		}
	}

    /**
     * Determine if there is space on the queue for the given number of
     * SchedBlocks.
     * 
     * @param numSBs
     * @return
     */
    private boolean spaceOnQueue(int numSBs) {
	if (isManual() && numSBs == 1) {
	    return true;
	}
	final int capacity = getArray().getQueueCapacity();
	final int numInQ   = getArray().getQueue().length;
	final int numRunning = getArray().hasRunningSchedBlock()? 1: 0;
	
	return (capacity >= numSBs + numRunning + numInQ);
    }

	@Override
	public void mouseReleased(MouseEvent e) { /* don't care */ }

	@Override
	protected void setArray(ArrayAccessor array) {
		super.setArray(array);
		System.out.format("%s (InteractivePanel).setArray(ArrayAccessor @ %h)%n",
				this.getClass().getSimpleName(),
				array.hashCode());
	
		PropertyChangeListener guiListener = new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				final String operation = evt.getPropertyName();
				final String[] item = (String[]) evt.getNewValue();
				if (operation.equals(ArrayGUIOperation.DESTROYED.toString())) {
					setStatusMessage(String.format(
							"<html>Array %s by %s</html>",
							operation.toString(),
							item[0]));
					deactivateAllButtons();
				}
			}
		};
		try {
			array.registerGUICallback(guiListener);
		} catch (AcsJContainerServicesEx e) {
			e.printStackTrace();
		}
	}
	
	protected void deactivateAllButtons() {
		opFilterChange.setEnabled(false);
		opFilterReset.setEnabled(false);
		opPopup.setEnabled(false);
		sbFilterChange.setEnabled(false);
		sbFilterReset.setEnabled(false);
		sbPopup.setEnabled(false);
		update.setEnabled(false);
		opFilterPanel.setEnabled(false);
//		sbFilterPanel.setEnabled(false);
	}
	/* End Listening to the filters (and anything else)
	 * ============================================================= */



	/*
	 * ================================================================
	 * Fake data
	 * ================================================================
	 */
	final static int numFakeOP = 5000;
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
				sb.setProjectUid(op.getUid());
				
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
        InteractivePanel panel = new InteractivePanel("testArray");
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
