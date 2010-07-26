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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import alma.scheduling.datamodel.executive.Executive;
import alma.scheduling.datamodel.obsproject.ObsProject;
import alma.scheduling.datamodel.obsproject.ObsUnitSet;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.ScienceGrade;

/**
 *
 * @author dclarke
 * $Id: InteractivePanel.java,v 1.2 2010/07/26 23:37:23 dclarke Exp $
 */
@SuppressWarnings("serial")
public class InteractivePanel extends AbstractArrayPanel
											implements ChangeListener {
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
	/** Used to convey information concerning ObsProjects stuff */
	private JLabel opMessage;
	
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
	/** Used to convey information concerning SchedBlocks stuff */
	private JLabel sbMessage;

	private JButton search;
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
	 * Create those widgets which we want to keep track of.
	 */
	private void createWidgets() {
		opModel = new ObsProjectTableModel();
		opTable = new JTable(opModel);
		opSorter = new TableRowSorter<ObsProjectTableModel>(opModel);
		opTable.setRowSorter(opSorter);
		opFilters = new FilterSet(opModel);
		opFilterSummary = new JLabel(opFilters.toString());
		opFilterChange = new JButton("Change");
		opFilterReset = new JButton("Reset");
		opMessage = new JLabel(BlankLabel);
		
		addActionListeners(opModel,
				           opFilterChange,
				           opFilterReset,
				           opFilters);
		opFilters.addChangeListener(this);
		opTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		
		sbModel = new SchedBlockTableModel();
		sbTable = new JTable(sbModel);
		sbSorter = new TableRowSorter<SchedBlockTableModel>(sbModel);
		sbTable.setRowSorter(sbSorter);
		sbFilters = new FilterSet(sbModel);
		sbFilterSummary = new JLabel(sbFilters.toString());
		sbFilterChange = new JButton("Change");
		sbFilterReset = new JButton("Reset");
		sbMessage = new JLabel(BlankLabel);
		
		addActionListeners(sbModel,
		           sbFilterChange,
		           sbFilterReset,
		           sbFilters);
		sbFilters.addChangeListener(this);

		addLinkingListener(opTable, sbTable);
		fakeData(opModel, sbModel);

		search  = new JButton("Search");
	}

	private int[] convertToModel(int[] viewRows, JTable table) {
		final int[] modelRows = new int[viewRows.length];
	
		for (int i = 0; i < viewRows.length; i++) {
			modelRows[i] = table.convertRowIndexToModel(viewRows[i]);
		}
		return modelRows;
	}

	private String[] getProjectIDs(int[]                modelRows,
								   ObsProjectTableModel table) {
		final String[] projectIDs = new String[modelRows.length];
	
		for (int i = 0; i < modelRows.length; i++) {
			projectIDs[i] = table.getProjectId(modelRows[i]);
		}
		return projectIDs;
	}

	private String format(int[] ints) {
		StringBuilder b = new StringBuilder();
		String        sep = "[";
		
		for (int i : ints) {
			b.append(sep);
			b.append(i);
			sep = ". ";
		}
		b.append("]");
		return b.toString();
	}

	private String regexp(String[] patterns) {
		StringBuilder b = new StringBuilder();
		String        sep = "";
		
		for (String p : patterns) {
			b.append(sep);
			b.append(p);
			sep = "|";
		}
		return b.toString();
	}

	private RowFilter<SchedBlockTableModel, Integer> regexpForSB(String[] patterns) {
		final String re = regexp(patterns);
		setSBMessage(re);
		return RowFilter.regexFilter(re, SchedBlockTableModel.projectIdColumn());
	}
	
	private void addLinkingListener(final JTable opTable,
									final JTable sbTable) {
		opTable.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {
					@Override
					public void valueChanged(ListSelectionEvent e) {
						int[] vRows = opTable.getSelectedRows();
						int[] mRows = convertToModel(vRows,
													 opTable);
						String[] pids = getProjectIDs(mRows,
								(ObsProjectTableModel)opTable.getModel());
						String re = regexp(pids);
						setOPMessage(String.format("v: %s, m: %s, p: %s",
												   format(vRows),
												   format(mRows),
												   re));
						sbSorter.setRowFilter(regexpForSB(pids));
						
					}});
		sbTable.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {
					@Override
					public void valueChanged(ListSelectionEvent e) {
						final int[] vRows = sbTable.getSelectedRows();
						final int[] mRows = convertToModel(vRows,
														   sbTable);
						setSBMessage(String.format("v: %s, m: %s",
												   format(vRows),
												   format(mRows)));
					}});
	}

	/**
	 * 
	 */
	private void addActionListeners(final TableModel model,
									final JButton    change,
			                        final JButton    reset,
			                        final FilterSet  filters) {
		
		change.addActionListener(new ActionListener(){
			int filterCount = 0;
			@Override
			public void actionPerformed(ActionEvent e) {
				filters.set(filterCount, ".*[aeiou].*");
				filterCount ++;
				if (filterCount >= model.getColumnCount()) {
					filterCount = 0;
				}
			}});
		reset.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				filters.reset();
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
		addSingleWidget(sbFilterReset,             x++, y, 0.0, 0.0);
		addSingleWidget(sbFilterChange,            x++, y, 0.0, 0.0);
		
		x = 0 ; y ++; // New row
		addFullWidthWidget(sbTable, y, 1.0, 0.3, true);
		
		x = 0 ; y ++; // New row
		addFullWidthWidget(sbMessage, y, 1.0, 0.0, false);
		
		x = 0 ; y ++; // New row
		addSingleWidget(search, numColumns-1, y, 0.0, 0.0);
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
	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == opFilters) {
			setOPFilterSummary(opFilters.toHTML());
			setOPMessage(opFilters.toHTML());
		} else if (e.getSource() == sbFilters) {
			setSBFilterSummary(sbFilters.toHTML());
			setSBMessage(sbFilters.toHTML());
		}
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
		1, 1, 1, 2, 1, 1, 1, 4, 1, 1, 1, 6, 1, 1, 1, 21
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
