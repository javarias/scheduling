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

import java.awt.BorderLayout;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
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

import org.springframework.context.support.AbstractApplicationContext;

import alma.JavaContainerError.wrappers.AcsJContainerServicesEx;
import alma.acs.gui.standards.StandardIcons;
import alma.entity.xmlbinding.projectstatus.StatusBaseT;
import alma.entity.xmlbinding.sbstatus.SBStatus;
import alma.lifecycle.persistence.StateArchive;
import alma.scheduling.ArrayGUIOperation;
import alma.scheduling.SchedBlockQueueItem;
import alma.scheduling.SchedulingException;
import alma.scheduling.algorithm.results.Result;
import alma.scheduling.algorithm.results.dao.ResultsDao;
import alma.scheduling.algorithm.sbranking.SBRank;
import alma.scheduling.array.util.FilterSet;
import alma.scheduling.array.util.FilterSetPanel;
import alma.scheduling.array.util.StatusCollection;
import alma.scheduling.datamodel.executive.Executive;
import alma.scheduling.datamodel.obsproject.ObsProject;
import alma.scheduling.datamodel.obsproject.ObsUnitSet;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.SchedBlockControl;
import alma.scheduling.datamodel.obsproject.SchedBlockState;
import alma.scheduling.datamodel.obsproject.ScienceGrade;
import alma.scheduling.swingx.CallbackFilter;
import alma.scheduling.utils.DSAContextFactory;
import alma.statearchiveexceptions.wrappers.AcsJEntitySerializationFailedEx;
import alma.statearchiveexceptions.wrappers.AcsJInappropriateEntityTypeEx;
import alma.statearchiveexceptions.wrappers.AcsJNoSuchEntityEx;
import alma.statearchiveexceptions.wrappers.AcsJNullEntityIdEx;

/**
 *
 * @author dclarke
 * $Id: InteractivePanel.java,v 1.20 2011/05/12 00:05:59 dclarke Exp $
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
    
    private JPanel details;
    private JTextPane opDetails;
    private boolean   showingOPDetails;
    private JTextPane sbDetails;
    
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
		initialiseScoresAndRanks();
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
		initialiseScoresAndRanks();
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
				TitleColour));
		opModel = new ObsProjectTableModel();
		opTable = new JTable(opModel);
		opSorter = new TableRowSorter<ObsProjectTableModel>(opModel);
		opTable.setRowSorter(opSorter);
		opFilters = new FilterSet(opModel);
		opFilterSummary = new JLabel(opFilters.toHTML(
				NormalColour, DetailColour));
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
				TitleColour));
		sbModel = new SchedBlockTableModel();
		sbTable = new JTable(sbModel);
//		initialiseSBSorting();
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
				getData();
			}});
		update.setEnabled(false);
		statusMessage = new JLabel(BlankLabel);
		
		createDetails();
		createPopups();
		
		makeSameWidth(opTitle, sbTitle, update, opFilterReset, opFilterChange);
	}

	/**
	 * 
	 */
	private void initialiseSBSorting() {
		sbSorter = new TableRowSorter<SchedBlockTableModel>(sbModel);
		sbModel.addSpecificComparators(sbSorter);
		sbTable.setRowSorter(sbSorter);
	}
	
	/**
	 * Create everything we need to show details of the selected
	 * SchedBlock and/or ObsProject.
	 */
	private void createDetails() {
		opDetails = new JTextPane();
		opDetails.setContentType("text/html");
		showingOPDetails = false;

		sbDetails = new JTextPane();
		sbDetails.setContentType("text/html");

		opTable.getSelectionModel().addListSelectionListener(new ListSelectionListener(){

			@Override
			public void valueChanged(ListSelectionEvent e) {
				int row = opTable.getSelectedRow();
				if (row >= 0) {
					// Something is selected
					row = opTable.convertRowIndexToModel(row);
					final String id = opModel.getProjectId(row);
					showObsProjectDetails(id);
					showingOPDetails = true;
				} else {
					// Nothing is selected
					clearObsProjectDetails();
					showingOPDetails = false;
				}
			}});
		sbTable.getSelectionModel().addListSelectionListener(new ListSelectionListener(){

			@Override
			public void valueChanged(ListSelectionEvent e) {
				int row = sbTable.getSelectedRow();
				if (row >= 0) {
					// Something is selected
					row = sbTable.convertRowIndexToModel(row);
					final String id = sbModel.getSchedBlockId(row);
					showSchedBlockDetails(id);
				} else {
					// Nothing is selected
					clearSchedBlockDetails();
				}
			}});
		
		final JPanel opDetailsPanel = new JPanel();
		final JPanel sbDetailsPanel = new JPanel();
		JScrollPane scroll;
		
		opDetailsPanel.setLayout(new BorderLayout());
		opDetailsPanel.add(new JLabel("Project Details"),
				           BorderLayout.NORTH);
		scroll = new JScrollPane(opDetails);
		opDetailsPanel.add(scroll, BorderLayout.CENTER);
		sbDetailsPanel.setLayout(new BorderLayout());
		sbDetailsPanel.add(new JLabel("SchedBlock Details"),
				           BorderLayout.NORTH);
		scroll = new JScrollPane(sbDetails);
		sbDetailsPanel.add(scroll, BorderLayout.CENTER);

		
		final JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
		split.setTopComponent(opDetailsPanel);
		split.setBottomComponent(sbDetailsPanel);
		split.setDividerLocation(1.0/2.0);
		split.setOneTouchExpandable(true);
		details = new JPanel();
		details.setLayout(new BorderLayout());
		details.add(split, BorderLayout.CENTER);

	}
	
	/**
	 * Create the pop-up menus for the two tables.
	 */
	private void createPopups() {
		JMenuItem changeFilters;
		JMenuItem resetFilters;
		JMenuItem clearSelection;
		
		// Project table menu
		opPopup = new JPopupMenu("Projects");
		resetFilters   = new JMenuItem("Reset Project Filters");
		changeFilters  = new JMenuItem("Change Project Filters");
		clearSelection  = new JMenuItem("Clear Selection");

		addFilterListeners(changeFilters, resetFilters, opFilterPanel);
		clearSelection.addActionListener(opClearSelectionListener());
		clearSelection.setEnabled(true);

		opPopup.addSeparator();
		opPopup.add(resetFilters);
		opPopup.add(changeFilters);
		opPopup.addSeparator();
		opPopup.add(clearSelection);
		
		// SchedBlock table menu
		sbPopup = new JPopupMenu("SchedBlocks");
		sbQueueSelected   = new JMenuItem(menuStringQueueSelection());
		sbQueueHere       = new JMenuItem("Queue");
		
		sbQueueHere.addActionListener(sbQueueHereListener());
		sbQueueSelected.addActionListener(queueActionListener());
		sbQueueSelected.setEnabled(false);
		
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
		final JSplitPane subSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
		subSplit.setTopComponent(opPanel);
		subSplit.setBottomComponent(sbPanel);
		subSplit.setDividerLocation(2.0/3.0);
		split.setTopComponent(subSplit);
		split.setBottomComponent(details);
		split.setDividerLocation(2.0/3.0);
		split.setOneTouchExpandable(true);
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(split);
	}

    /* (non-Javadoc)
     * @see alma.scheduling.array.guis.AbstractArrayPanel#arrayAvailable()
     */
    @Override
    protected void arrayAvailable() {
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
		sbTable.getSelectionModel().addListSelectionListener(queueListener(sbQueueSelected, sbTable));
		
		sbModel.configure(array.isDynamic());
		if (array.isDynamic()) {
			getFirstScoresAndRanks();
		}
		initialiseSBSorting();
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
			final SchedBlockTableModel sbtm,
			final Set<String> pids) {
		final CallbackFilter.Callee callee =
			new CallbackFilter.Callee() {
			@Override
			public boolean include(
					Entry<? extends Object, ? extends Object> value,
					int index) {
				final String pid =
					value.getValue(sbtm.projectIdColumn()).toString();
				return pids.contains(pid);
			}
		};

		return CallbackFilter.callbackFilter(
				callee,
				sbtm.projectIdColumn());
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
		sbFilterFromOPTable = rowFilterForSBs(sbModel, pids);
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
	 * Get the SBStatus for the given SchedBlock from our model accessor
	 * @param entityId
	 * @return
	 * @throws AcsJInappropriateEntityTypeEx 
	 * @throws AcsJNoSuchEntityEx 
	 * @throws AcsJNullEntityIdEx 
	 */
	private SBStatus getSBStatus(SchedBlock sb)
				throws AcsJNullEntityIdEx,
					   AcsJNoSuchEntityEx,
					   AcsJInappropriateEntityTypeEx {
		final StateArchive sa = getModels().getStateArchive();
		return sa.getSBStatus(sb.getStatusEntity());
	}
	
	/**
	 * Get the statuses for the given ObsProject from our model accessor
	 * @param entityId
	 * @return
	 * @throws AcsJInappropriateEntityTypeEx 
	 * @throws AcsJNoSuchEntityEx 
	 * @throws AcsJNullEntityIdEx 
	 * @throws AcsJEntitySerializationFailedEx 
	 * @throws SchedulingException 
	 */
	private StatusCollection getProjectStatus(ObsProject op)
					throws AcsJNullEntityIdEx,
					       AcsJNoSuchEntityEx,
					       AcsJInappropriateEntityTypeEx,
					       AcsJEntitySerializationFailedEx,
					       SchedulingException {
		final StateArchive sa = getModels().getStateArchive();
		final StatusBaseT[] statuses = sa.getProjectStatusList(op.getStatusEntity());
		return new StatusCollection(statuses);
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

	private void showSchedBlockDetails(String entityId) {
		final SchedBlock sb;
		String text;
		
		try {
			sb = getModels().getSchedBlockFromEntityId(entityId);
		} catch (Exception e) {
			StringBuffer buf = new StringBuffer();
			buf.append("<html>");
			buf.append("SchedBlock ");
			buf.append(entityId);
			buf.append(" is no longer in the Scheduler's database");
			buf.append(" (it's probably no longer in a state which allows it to be");
			buf.append(" scheduled - but you can see it in the Project Tracker)");
			buf.append("</html>");
			sbDetails.setText(buf.toString());
			return;
		}
		
		SBRank  currScore = null;
		Integer currRank  = null;
		SBRank  prevScore = null;
		Integer prevRank  = null;
		String  excuse;
		
		if (array.isDynamic()) {
			if (currentScores != null) {
				currScore = currentScores.get(entityId);
			}
			if (currentRanks != null) {
				currRank = currentRanks.get(entityId);
			}
			if (previousScores != null) {
				prevScore = previousScores.get(entityId);
			}
			if (previousRanks != null) {
				prevRank = previousRanks.get(entityId);
			}
		}
		
		try {
			final SBStatus sbs = getSBStatus(sb);
			if (array.isDynamic()) {
				text = SchedBlockFormatter.formatted(sb, sbs,
						currScore, currRank, prevScore, prevRank);
			} else {
				text = SchedBlockFormatter.formatted(sb, sbs);
			}
		} catch (AcsJNullEntityIdEx e) {
			excuse = "No reference to status object in SchedBlock";
			if (array.isDynamic()) {
				text = SchedBlockFormatter.formatted(sb, excuse,
						currScore, currRank, prevScore, prevRank);
			} else {
				text = SchedBlockFormatter.formatted(sb, excuse);
			}
		} catch (AcsJNoSuchEntityEx e) {
			excuse = String.format(
					"Reference to non-existant status object %s in SchedBlock",
					sb.getStatusEntity().getEntityId());
			if (array.isDynamic()) {
				text = SchedBlockFormatter.formatted(sb, excuse,
						currScore, currRank, prevScore, prevRank);
			} else {
				text = SchedBlockFormatter.formatted(sb, excuse);
			}
		} catch (AcsJInappropriateEntityTypeEx e) {
			excuse = String.format(
					"Status reference %s for SchedBlock is to wrong type of object",
					sb.getStatusEntity().getEntityId());
			if (array.isDynamic()) {
				text = SchedBlockFormatter.formatted(sb, excuse,
						currScore, currRank, prevScore, prevRank);
			} else {
				text = SchedBlockFormatter.formatted(sb, excuse);
			}
		}
		
		sbDetails.setText(text);

		if (!showingOPDetails) {
			// No operator selected project being show, so show this SB's one
			showObsProjectDetails(sb.getProjectUid());
		}
	}

	private void clearSchedBlockDetails() {
		final String text = "<html>No SchedBlock selected</html>";
		sbDetails.setText(text);

		if (!showingOPDetails) {
			// No operator selected project being show, so clear any shown
			clearObsProjectDetails();
		}
	}
	
	private void showObsProjectDetails(String entityId) {
		final ObsProject op;
		String text;
		
		try {
			op = getModels().getObsProjectFromEntityId(entityId);
		} catch (Exception e) {
			StringBuffer buf = new StringBuffer();
			buf.append("<html>");
			buf.append("Project ");
			buf.append(entityId);
			buf.append(" is no longer in the Scheduler's database");
			buf.append(" (it's probably no longer in a state which allows it to be");
			buf.append(" scheduled - but you can see it in the Project Tracker)");
			buf.append("</html>");
			opDetails.setText(buf.toString());
			return;
		}
		
		try {
			final StatusCollection statuses = getProjectStatus(op);
			text = ObsProjectFormatter.formatted(op, statuses);
		} catch (AcsJNullEntityIdEx e) {
			text = ObsProjectFormatter.formatted(op, 
					"No reference to status object in ObsProject");
		} catch (AcsJNoSuchEntityEx e) {
			text = ObsProjectFormatter.formatted(op, String.format(
					"Reference to non-existant status object %s in ObsProject",
					op.getStatusEntity().getEntityId()));
		} catch (AcsJInappropriateEntityTypeEx e) {
			text = ObsProjectFormatter.formatted(op, String.format(
					"Status reference %s for ObsProject is to wrong type of object",
					op.getStatusEntity().getEntityId()));
		} catch (AcsJEntitySerializationFailedEx e) {
			text = ObsProjectFormatter.formatted(op,
					"Cannot deserialize one or more status entities");
		} catch (SchedulingException e) {
			text = ObsProjectFormatter.formatted(op, String.format(
					"Problem with status entities for Project %s - %s",
					entityId, e.getMessage()));
		}
		
		opDetails.setText(text);
	}
	
	private void clearObsProjectDetails() {
		final String text = "<html>No Project selected</html>";
		opDetails.setText(text);
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
	
	@SuppressWarnings("unused")
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
					NormalColour, DetailColour));
		} else if (e.getSource() == sbFilters) {
//			setSBFilterSummary(sbFilters.toHTML());
		}
	}
	
	/**
	 * Hypothetical at the moment, but listening to the project store
	 * @param e
	 */
	public void archiveChanged(ChangeEvent e) {
		update.setEnabled(true);
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
					showingOPDetails = true;
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
				opPopup.show(e.getComponent(), e.getX(), e.getY());
			} else if (table == sbTable) {
				final SchedBlockTableModel model = (SchedBlockTableModel)table.getModel();
				sbHere = model.getSchedBlockId(modelRow);
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
				} else if (operation.equals(ArrayGUIOperation.CALCULATINGSCORES.toString())) {
					setStatusMessage(String.format(
							"<html>Ranking SchedBlocks by %s</html>",
							item[0]));
				} else if (operation.equals(ArrayGUIOperation.SCORESREADY.toString())) {
					setStatusMessage(String.format(
							"<html>Retrieving scores</html>"));
					try {
						getScoresAndRanks();
						setStatusMessage(String.format(
								"<html>Scores updated (%d found)</html>",
								currentScores.size()));
					} catch (NullPointerException npe) {
						setStatusMessage(String.format(
								"<html>Cannot access SchedBlock scores</html>"));
					}
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
	 * Scores and Ranks
	 * ================================================================
	 */
	private Map<String, SBRank>  currentScores;
	private Map<String, Integer> currentRanks;
	private Map<String, SBRank>  previousScores;
	private Map<String, Integer> previousRanks;
	private ResultsDao resultsDao;
	
	private void initialiseScoresAndRanks() {
		currentScores  = new HashMap<String, SBRank>();
		previousScores = new HashMap<String, SBRank>();
		currentRanks   = new HashMap<String, Integer>();
		previousRanks  = new HashMap<String, Integer>();
	}
	
	private void getFirstScoresAndRanks() {
		Result result;
		
		AbstractApplicationContext ctx = DSAContextFactory.getContext();
		resultsDao = (ResultsDao) ctx.getBean(
				DSAContextFactory.SCHEDULING_DSA_RESULTS_DAO_BEAN);
		
		result = resultsDao.getPreviousResult(getArray().getArrayName());
		decodeScoresAndRanks(result, previousScores, previousRanks);
		result = resultsDao.getCurrentResult(getArray().getArrayName());
		decodeScoresAndRanks(result, currentScores, currentRanks);
	}
	
	private void decodeScoresAndRanks(Result               result,
			                          Map<String, SBRank>  scores,
			                          Map<String, Integer> ranks) {

		final SortedSet<SBRank> sorted = new TreeSet<SBRank>(
				new Comparator<SBRank>(){

					@Override
					public int compare(SBRank o1, SBRank o2) {
						// We want higher scores first, so reverse
						// the natural ordering.
						final int first = o2.compareTo(o1);
						if (first != 0) {
							return first;
						}
						return o2.getUid().compareTo(o1.getUid());
					}});
		
		sorted.addAll(result.getScores());

		int r = 1;

		for (final SBRank sbRank : sorted) {
			ranks.put(sbRank.getUid(), r++);
			scores.put(sbRank.getUid(), sbRank);
		}
	}
	
	private void getScoresAndRanks() {
		previousScores = currentScores;
		previousRanks  = currentRanks;
		final Result result = resultsDao.getCurrentResult(getArray().getArrayName());

		currentScores = new HashMap<String, SBRank>();
		currentRanks  = new HashMap<String, Integer>();
		decodeScoresAndRanks(result, currentScores, currentRanks);
		
		sbModel.setScores(currentScores, currentRanks);
	}
	/* End Scores and Ranks
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
	
	@SuppressWarnings("unused")
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
