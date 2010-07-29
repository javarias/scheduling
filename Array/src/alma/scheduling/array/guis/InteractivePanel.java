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

import java.awt.Color;
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
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.RowFilter.Entry;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.table.TableRowSorter;

import alma.scheduling.array.util.FilterSet;
import alma.scheduling.array.util.FilterSetPanel;
import alma.scheduling.datamodel.executive.Executive;
import alma.scheduling.datamodel.obsproject.ObsProject;
import alma.scheduling.datamodel.obsproject.ObsUnitSet;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.ScienceGrade;
import alma.scheduling.swingx.CallbackFilter;

/**
 *
 * @author dclarke
 * $Id: InteractivePanel.java,v 1.4 2010/07/29 15:55:39 dclarke Exp $
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
	/** The filter in use to select SBs by Project Id, null if none */
	private RowFilter<SchedBlockTableModel, Integer> sbFilterFromOPTable;

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
		fakeData(opModel, sbModel);

		update  = newButton("Update", "Get new project data from the repositories");
		
		createPopups();
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
	private ListSelectionListener detailsListener(final JMenuItem item, final JTable table) {
		final ListSelectionListener result = new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				item.setEnabled(table.getSelectedRows().length > 0);
			}
		};
		return result;
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
				item.setEnabled(table.getSelectedRows().length > 0);
				// TODO: also have to disable the item if there are
				//       more items selected than there is capacity
				//       available in the SB queue.
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
					final Set<String> pids = new HashSet<String>();

					for (int viewRow : viewRows) {
						try {
							int modelRow = table.convertRowIndexToModel(viewRow);
							pids.add(model.getProjectId(modelRow));
						} catch (ArrayIndexOutOfBoundsException ex) {
						}
					}
					for (final String pid : pids) {
						System.out.format("Show details of ObsProject %s%n", pid);
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
					final Set<String> pids = new HashSet<String>();

					for (int viewRow : viewRows) {
						try {
							int modelRow = table.convertRowIndexToModel(viewRow);
							pids.add(model.getSchedBlockId(modelRow));
						} catch (ArrayIndexOutOfBoundsException ex) {
						}
					}
					for (final String pid : pids) {
						System.out.format("Show details of SchedBlock %s%n", pid);
					}
				}
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
	private ActionListener queueActionListener(final JTable table,
											   final SchedBlockTableModel model) {
		final ActionListener result = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int[] viewRows = table.getSelectedRows();
				final Set<String> pids = new HashSet<String>();

				for (int viewRow : viewRows) {
					try {
						int modelRow = table.convertRowIndexToModel(viewRow);
						pids.add(model.getSchedBlockId(modelRow));
					} catch (ArrayIndexOutOfBoundsException ex) {
					}
				}
				for (final String pid : pids) {
					System.out.format("Queue SchedBlock %s%n", pid);
				}
			}
		};
		return result;
	}
	
	/**
	 * Create the pop-up menus for the two tables.
	 */
	private void createPopups() {
		JMenuItem details;
		JMenuItem change;
		JMenuItem reset;
		JMenuItem queue;
		
		// Project table menu
		opPopup = new JPopupMenu("Projects");
		details = new JMenuItem("Details");
		reset   = new JMenuItem("Reset Project Filters");
		change  = new JMenuItem("Change Project Filters");

		opTable.getSelectionModel().addListSelectionListener(detailsListener(details, opTable));
		details.addActionListener(detailsActionListener(opTable, opModel));
		details.setEnabled(false);
		addFilterListeners(change, reset, opFilterPanel);
		details.setEnabled(false);

		opPopup.add(details);
		opPopup.addSeparator();
		opPopup.add(reset);
		opPopup.add(change);
		
		// SchedBlock table menu
		sbPopup = new JPopupMenu("SchedBlocks");
		details = new JMenuItem("Details");
		queue   = new JMenuItem("Add SchedBlock(s) to Queue");
		
		sbTable.getSelectionModel().addListSelectionListener(detailsListener(details, sbTable));
		sbTable.getSelectionModel().addListSelectionListener(queueListener(queue, sbTable));
		details.addActionListener(detailsActionListener(sbTable, sbModel));
		queue.addActionListener(queueActionListener(sbTable, sbModel));
		details.setEnabled(false);
		queue.setEnabled(false);
		
		sbPopup.add(details);
		sbPopup.addSeparator();
		sbPopup.add(queue);
	}

	/**
	 * Create a RowFilter<> for the SchedBlock table which will include
	 * any SchedBlock which belongs to one of the projects whose Entity
	 * Id is in the set <code>pids</code>.
	 * 
	 * @param pids
	 * @return
	 */
	private RowFilter<SchedBlockTableModel, Integer> rowFilterForSBs(final Set<String> pids) {
		final CallbackFilter.Callee callee = new CallbackFilter.Callee() {

			@Override
			public boolean include(
					Entry<? extends Object, ? extends Object> value, int index) {
				final String pid =
					value.getValue(SchedBlockTableModel.projectIdColumn()).toString();
				return pids.contains(pid);
			}
		};

		return CallbackFilter.callbackFilter(callee, SchedBlockTableModel.projectIdColumn());
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
		final ObsProjectTableModel model = (ObsProjectTableModel)opTable.getModel();
		
		if (opTable.getSelectedRows().length != 0) {
			// There is a selection in the table, use the selected rows
			int[] vr = opTable.getSelectedRows();
			for (int viewRow : vr) {
				viewRows.add(viewRow);
			}
		} else {
			// No selection, used all the view rows.
			final int numViewRows = opTable.getRowSorter().getViewRowCount();
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
	 * Add a widget to the display which takes up a full row on the
	 * layout's grid. Note: because this uses the same
	 * GridBagConstraints object that other add*Widget() methods use,
	 * we need to be careful to set all the constraints that may have
	 * been set in them (and vice versa).
	 */
	private void addFullWidthWidget(JComponent widget,
			                        int        y,
			                        double     wx,
			                        double     wy,
			                        boolean    scrollRequired) {
		c.gridx = 0;
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
		int numColumns = 4;
		
		addSingleWidget(new JLabel("Projects"), x++, y, 0.0, 0.0);
		addSingleWidget(opFilterSummary,        x++, y, 1.0, 0.0);
		addSingleWidget(opFilterReset,          x++, y, 0.0, 0.0);
		addSingleWidget(opFilterChange,         x++, y, 0.0, 0.0);
		
		x = 0 ; y ++; // New row
		addFullWidthWidget(opTable, y, 1.0, 1.0, true);
		
		x = 0 ; y ++; // New row
		addFullWidthWidget(opMessage, y, 1.0, 0.0, false);
		
		x = 0 ; y ++; // New row
		addVerticalSpacer(y);
		
		x = 0 ; y ++; // New row
		addSingleWidget(new JLabel("SchedBlocks"), x++, y, 0.0, 0.0);
		addSingleWidget(sbFilterSummary,           x++, y, 1.0, 0.0);
//		addSingleWidget(sbFilterReset,             x++, y, 0.0, 0.0);
//		addSingleWidget(sbFilterChange,            x++, y, 0.0, 0.0);
		
		x = 0 ; y ++; // New row
		addFullWidthWidget(sbTable, y, 1.0, 0.3, true);
		
		x = 0 ; y ++; // New row
		addFullWidthWidget(sbMessage, y, 1.0, 0.0, false);
		
		x = 0 ; y ++; // New row
		addSingleWidget(update, numColumns-1, y, 0.0, 0.0);
	}
	/* End Constructors and GUI building
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
		} else if (e.getSource() == sbFilters) {
//			setSBFilterSummary(sbFilters.toHTML());
		}
	}
	
	/**
	 * Hypothetical at the moment, but listening to the project store
	 * @param e
	 */
	public void archiveChanged(ChangeEvent e) {
		update.setForeground(Color.green);
		update.setToolTipText("New project information is in the repositories. Click to fetch it.");
//		update.setIcon(defaultIcon);
	}
	
	/**
	 * Generic processing of any mouse event in which we might be
	 * interested.
	 * 
	 * @param e
	 */
	private void handleMouseEvent(MouseEvent e) {
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
					System.out.format("\tMore details in opTable, ObsProject Id %s (viewRow %d, modelRow %d)%n",
							id, viewRow, modelRow);
				} else if (table == sbTable) {
					SchedBlockTableModel model = (SchedBlockTableModel) table.getModel();
					id = model.getSchedBlockId(modelRow);
					System.out.format("\tMore details in sbTable, SchedBlock Id %s (viewRow %d, modelRow %d)%n",
							id, viewRow, modelRow);
				}
			} catch (ArrayIndexOutOfBoundsException ex) {
			}
		} else if (e.isPopupTrigger()) {
			if (table == opTable) {
				opPopup.show(e.getComponent(), e.getX(), e.getY());
			} else if (table == sbTable) {
				sbPopup.show(e.getComponent(), e.getX(), e.getY());
			}
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		handleMouseEvent(e);
	}
	@Override
	public void mouseEntered(MouseEvent e) { /* don't care */ }

	@Override
	public void mouseExited(MouseEvent e) { /* don't care */ }

	@Override
	public void mousePressed(MouseEvent e) {
		handleMouseEvent(e);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		handleMouseEvent(e);
	}
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
			
			for (int s = 0; s < sbPerOP[p % sbPerOP.length]; s++) {
				final SchedBlock sb = new SchedBlock();
				sb.setUid(String.format("uid://X007/X%04x/X%02x", p, s+2));
				sb.setPiName(pi[p % pi.length]);
				sb.setExecutive(exs.get(ex[sc % ex.length]));
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
    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("Interactive Panel");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Add the ubiquitous "Hello World" label.
        JPanel panel = new InteractivePanel();
        frame.getContentPane().add(panel);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

	/**
	 * @param args
	 */
   public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
	/*
	 * End Running stand-alone
	 * ============================================================= */
}
