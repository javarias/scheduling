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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableModel;

import alma.scheduling.datamodel.executive.Executive;
import alma.scheduling.datamodel.obsproject.ObsProject;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.ScienceGrade;

/**
 *
 * @author dclarke
 * $Id: InteractivePanel.java,v 1.1 2010/07/26 16:36:19 dclarke Exp $
 */
@SuppressWarnings("serial")
public class InteractivePanel extends AbstractArrayPanel
											implements ChangeListener {
	/*
	 * ================================================================
	 * Fields for widgets &c
	 * ================================================================
	 */
	/** The LayoutManager we're using. */
	private GridBagLayout l;
	private GridBagConstraints c;
	
	private JLabel  opFilterSummary;
	private JButton opFilterChange;
	private JButton opFilterReset;
	private JTable  opTable;
	private ObsProjectTableModel opModel;
	private FilterSet opFilters;
	
	private JLabel  sbFilterSummary;
	private JButton sbFilterChange;
	private JButton sbFilterReset;
	private JTable  sbTable;
	private SchedBlockTableModel sbModel;
	private FilterSet sbFilters;

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
		opTable.setAutoCreateRowSorter(true);
		opFilters = new FilterSet(opModel);
		opFilterSummary = new JLabel(opFilters.toString());
		opFilterChange = new JButton("Change");
		opFilterReset = new JButton("Reset");
		
		fakeData(opModel);
		addActionListeners(opModel,
				           opFilterChange,
				           opFilterReset,
				           opFilters);
		opFilters.addChangeListener(this);
		
		sbModel = new SchedBlockTableModel();
		sbTable = new JTable(sbModel);
		sbTable.setAutoCreateRowSorter(true);
		sbFilters = new FilterSet(sbModel);
		sbFilterSummary = new JLabel(sbFilters.toString());
		sbFilterChange = new JButton("Change");
		sbFilterReset = new JButton("Reset");
		
		fakeData(sbModel);
		addActionListeners(sbModel,
		           sbFilterChange,
		           sbFilterReset,
		           sbFilters);
		sbFilters.addChangeListener(this);
		
		search  = new JButton("Search");
	}

	final static int numFakeOP = 12;
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
	
	private void fakeData(ObsProjectTableModel opModel) {
		final Collection<ObsProject> fake = new ArrayList<ObsProject>();
		for (int i = 0; i < numFakeOP; i++) {
			final ObsProject f = new ObsProject();
			f.setUid(String.format("uid://X007/Xdc/X%x", i));
			f.setPrincipalInvestigator(pi[i]);
			f.setScienceScore((float)Math.sqrt(i));
			f.setScienceRank(i);
			f.setLetterGrade(ScienceGrade.values()[i%ScienceGrade.values().length]);
			f.setStatus("READY");
			f.setTotalExecutionTime(Math.sqrt(i) * i * i);
			fake.add(f);
		}
		opModel.setData(fake);
	}


	final static int numFakeSB = 35;

	private void fakeData(SchedBlockTableModel sbModel) {
		final Collection<SchedBlock> fake = new ArrayList<SchedBlock>();
		final Map<String, Executive> exs = new HashMap<String, Executive>();
		for (String x : ex) {
			if (!exs.containsKey(x)) {
				final Executive e = new Executive();
				e.setName(x);
				e.setDefaultPercentage((float)100);
				exs.put(x, e);
			}
		}
		
		for (int i = 0; i < numFakeSB; i++) {
			final SchedBlock f = new SchedBlock();
			f.setUid(String.format("uid://X007/Xdc/X%x", i+numFakeOP));
			f.setPiName(pi[i % pi.length]);
			f.setExecutive(exs.get(ex[i % ex.length]));
			fake.add(f);
		}
		sbModel.setData(fake);
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
		c.fill = GridBagConstraints.NONE;
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
			                        double     wy) {
		c.gridx = 0;
		c.gridy = y;
		c.gridwidth  = GridBagConstraints.REMAINDER;
		c.gridheight = 1;
		c.weightx = wx;
		c.weighty = wy;
		c.anchor = GridBagConstraints.CENTER;
		c.fill = GridBagConstraints.BOTH;
		
		JScrollPane scroll = new JScrollPane(widget);
		l.setConstraints(scroll, c);
		add(scroll);
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
		addFullWidthWidget(opTable, y, 1.0, 1.0);
		
		x = 0 ; y ++; // New row
		addSingleWidget(new JLabel("SchedBlocks"), x++, y, 0.0, 0.0);
		addSingleWidget(sbFilterSummary,           x++, y, 1.0, 0.0);
		addSingleWidget(sbFilterReset,             x++, y, 0.0, 0.0);
		addSingleWidget(sbFilterChange,            x++, y, 0.0, 0.0);
		
		x = 0 ; y ++; // New row
		addFullWidthWidget(sbTable, y, 1.0, 0.3);
		
		x = 0 ; y ++; // New row
		addSingleWidget(search, numColumns-1, y, 0.0, 0.0);
	}
	/* End Constructors and GUI building
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * Listening to the filters (and anything else)
	 * ================================================================
	 */
	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == opFilters) {
			opFilterSummary.setText(opFilters.toString());
		} else if (e.getSource() == sbFilters) {
			sbFilterSummary.setText(sbFilters.toString());
		}
	}
	/* End Listening to the filters (and anything else)
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
