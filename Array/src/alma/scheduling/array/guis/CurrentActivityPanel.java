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
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableRowSorter;

import alma.scheduling.SchedBlockQueueItem;
import alma.scheduling.array.guis.SBExecutionTableModel.When;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.dao.ModelAccessor;
/**
 *
 * @author dclarke
 * $Id: CurrentActivityPanel.java,v 1.1 2010/08/23 23:07:36 dclarke Exp $
 */
@SuppressWarnings("serial")
public class CurrentActivityPanel extends AbstractArrayPanel {
//							 implements ChangeListener, MouseListener {
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
	/** The widgets in which we show the SchedBlocks */
	private JTable pendingTable;
	private JTable currentTable;
	private JTable pastTable;
	
	/** The models behind the SchedBlock tables */
	private SBExecutionTableModel pendingModel;
	private SBExecutionTableModel currentModel;
	private SBExecutionTableModel pastModel;
	
	/** The buttons for manipulating the pending queue */
	private JButton moveUp;
	private JButton moveDown;
	private JButton delete;
	
	/** The buttons for manipulating the executor */
	private JButton abortSB;
	private JButton stopSB;
	private JButton startExec;
	private JButton stopExec;
	
	/** the things which sorts the SchedBlocks in our tables */
	// Note that there is no pending sorter - the queue is ordered
	private TableRowSorter<SBExecutionTableModel> currentSorter;
	private TableRowSorter<SBExecutionTableModel> pastSorter;

	/** Used to convey information to the user */
	private JLabel statusMessage;
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
	public CurrentActivityPanel() {
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
		result.setEnabled(false);
		return result;
	}
	
	private void makeSameWidth(JButton... buttons) {
		double maxWidth = -1;
		System.out.println("-- makeSameWidth() --");
		for (final JButton b : buttons) {
			final double w = b.getPreferredSize().getWidth();
			System.out.format("%s:\t%f%n",
					b.getText(),
					w);
			if (w > maxWidth) {
				maxWidth = w;
			}
		}
		for (final JButton b : buttons) {
			final Dimension d = b.getPreferredSize();
			d.setSize(maxWidth, d.getHeight());
			b.setMinimumSize(d);
			b.setPreferredSize(d);
			b.setMaximumSize(d);
		}
		System.out.println("---------------------");
	}
	/**
	 * Create those widgets which we want to keep track of.
	 */
	private void createWidgets() {
		pendingModel = new SBExecutionTableModel(When.Pending);
		currentModel = new SBExecutionTableModel(When.Current);
		pastModel    = new SBExecutionTableModel(When.Past);
		
		pendingTable = new JTable(pendingModel);
		pendingTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		pendingTable.getSelectionModel().addListSelectionListener(
			new ListSelectionListener(){
				@Override
				public void valueChanged(ListSelectionEvent e) {
					final int row = pendingTable.getSelectedRow();
					moveUp.setEnabled(
							(getArray() != null)
								&& (row != 0));
					moveDown.setEnabled(
							(getArray() != null)
								&& (row != pendingTable.getRowCount()-1));
					delete.setEnabled(
							(getArray() != null)
								&& (row >= 0));
				}});
		currentTable = new JTable(currentModel);
		currentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		pastTable = new JTable(pastModel);
		pastTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		moveUp   = newButton("Promote",
				"Move the selected execution up the queue");
		moveUp.addActionListener(
				new ActionListener(){
					@Override
					public void actionPerformed(ActionEvent e) {
						try {
							final int r = pendingTable.getSelectedRow();
							final ManifestSchedBlockQueueItem mqi =
								pendingModel.getData(r);
							getArray().moveUp(mqi.getItem());
							pendingModel.fireTableRowsUpdated(r-1, r);
						} catch (NullPointerException npe) {
						}
					}});
		moveDown = newButton("Demote",
				"Move the selected execution down the queue");
		moveUp.addActionListener(
				new ActionListener(){
					@Override
					public void actionPerformed(ActionEvent e) {
						try {
							final int r = pendingTable.getSelectedRow();
							final ManifestSchedBlockQueueItem mqi =
								pendingModel.getData(r);
							getArray().moveDown(mqi.getItem());
							pendingModel.fireTableRowsUpdated(r, r+1);
						} catch (NullPointerException npe) {
						}
					}});
		delete   = newButton("Remove",
				"Remove the selected execution from the queue");
		delete.addActionListener(
				new ActionListener(){
					@Override
					public void actionPerformed(ActionEvent e) {
						try {
							final int r = pendingTable.getSelectedRow();
							final ManifestSchedBlockQueueItem mqi =
								pendingModel.getData(r);
							getArray().delete(mqi.getItem());
							pendingModel.fireTableRowsDeleted(r, r);
						} catch (NullPointerException npe) {
						}
					}});
		
		abortSB = newButton("Abort SB",
							"Abort the execution of the current SchedBlock as quickly as possible");
		abortSB.setForeground(Color.RED);
		abortSB.addActionListener(
				new ActionListener(){
					@Override
					public void actionPerformed(ActionEvent e) {
						try {
							getArray().abortRunningSchedBlock();
							getArray().stop();
						} catch (NullPointerException npe) {
						}
					}});
		stopSB = newButton("Stop SB",
						   "Stop the execution of the current SchedBlock at the end of the current scan");
		stopSB.addActionListener(
				new ActionListener(){
					@Override
					public void actionPerformed(ActionEvent e) {
						try {
							getArray().stopRunningSchedBlock();
							getArray().stop();
						} catch (NullPointerException npe) {
						}
					}});
		startExec = newButton("Start",
							  "Start execution of SchedBlocks on this array");
		startExec.addActionListener(
				new ActionListener(){
					@Override
					public void actionPerformed(ActionEvent e) {
						try {
							getArray().start();
						} catch (NullPointerException npe) {
						}
					}});
		stopExec = newButton("Stop",
							  "Stop execution of SchedBlocks on this array");
		stopExec.addActionListener(
				new ActionListener(){
					@Override
					public void actionPerformed(ActionEvent e) {
						try {
							getArray().stop();
						} catch (NullPointerException npe) {
						}
					}});

		currentSorter = new TableRowSorter<SBExecutionTableModel>(currentModel);
		currentTable.setRowSorter(currentSorter);
		pastSorter = new TableRowSorter<SBExecutionTableModel>(pastModel);
		pastTable.setRowSorter(pastSorter);

		statusMessage = new JLabel(BlankLabel);
	}

	/**
	 * Create a Header styled label
	 * 
	 * @return
	 */
	private JLabel newLabel(final String label) {
		final JLabel result = new JLabel(label);
		
		result.setForeground(new Color(127, 127, 255));
		result.setAlignmentX(Component.LEFT_ALIGNMENT);
		result.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));
		return result;
	}

	/**
	 * Create a scrolling panel around the given component
	 * 
	 * @return
	 */
	private JScrollPane newScroller(final Component comp) {
		final JScrollPane result = new JScrollPane(comp);
		
		result.setAlignmentX(Component.LEFT_ALIGNMENT);
		result.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));
		return result;
	}

	/**
	 * Create the sub-panel which shows the pending queue
	 * 
	 * @return
	 */
	private JPanel createPendingPanel() {
		final JPanel      result  = new JPanel();
		final JPanel      buttons = new JPanel();
		final JLabel      label  = newLabel("Pending Executions");
		final JScrollPane scroll = newScroller(pendingTable);

		buttons.setLayout(new BoxLayout(buttons, BoxLayout.Y_AXIS));
		buttons.add(moveUp);
		buttons.add(moveDown);
		buttons.add(Box.createVerticalStrut(
				moveUp.getPreferredSize().height/2));
		buttons.add(delete);
		buttons.setAlignmentY(Component.TOP_ALIGNMENT);

		result.setLayout(new BorderLayout());
		result.add(label, BorderLayout.NORTH);
		result.add(scroll, BorderLayout.CENTER);
		result.add(buttons, BorderLayout.EAST);
		
		return result;
	}

	/**
	 * Create the sub-panel which shows the current executions
	 * 
	 * @return
	 */
	private JPanel createCurrentPanel() {
		final JPanel      result  = new JPanel();
		final JPanel      buttons = new JPanel();
		final JLabel      label  = newLabel("Current Executions");
		final JScrollPane scroll = newScroller(currentTable);

		buttons.setLayout(new BoxLayout(buttons, BoxLayout.Y_AXIS));
		buttons.add(abortSB);
		buttons.add(stopSB);
		buttons.add(Box.createVerticalStrut(
				moveUp.getPreferredSize().height/2));
		buttons.add(startExec);
		buttons.add(stopExec);
		buttons.setAlignmentY(Component.TOP_ALIGNMENT);

		result.setLayout(new BorderLayout());
		result.add(label, BorderLayout.NORTH);
		result.add(scroll, BorderLayout.CENTER);
		result.add(buttons, BorderLayout.EAST);
		
		return result;
	}

	/**
	 * Create the sub-panel which shows the completed executions
	 * 
	 * @return
	 */
	private JPanel createPastPanel() {
		final JPanel      result = new JPanel();
		final JLabel      label  = newLabel("Completed Executions");
		final JScrollPane scroll = newScroller(pastTable);

		result.setLayout(new BoxLayout(result, BoxLayout.Y_AXIS));
		result.add(label);
		result.add(scroll);
		return result;
	}
	
	/**
	 * Add the widgets to the display.
	 */
	private void addWidgets() {
		makeSameWidth(abortSB, stopSB, startExec, stopExec,
			      moveUp, moveDown, delete);

		final JPanel pendingPanel = createPendingPanel();
		final JPanel currentPanel = createCurrentPanel();
		final JPanel pastPanel    = createPastPanel();
		
		final JSplitPane topSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
		final JSplitPane bottomSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
		
		topSplit.setTopComponent(pendingPanel);
		topSplit.setBottomComponent(bottomSplit);
		bottomSplit.setTopComponent(currentPanel);
		bottomSplit.setBottomComponent(pastPanel);
		
		topSplit.setDividerLocation(1.0/3.0);
		bottomSplit.setDividerLocation(0.5);
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(topSplit);
		add(statusMessage);
	}

	public List<ManifestSchedBlockQueueItem> getSchedBlocks(
			SchedBlockQueueItem[] qItems) {
		final List<ManifestSchedBlockQueueItem> result =
			new Vector<ManifestSchedBlockQueueItem>();
		
		final ModelAccessor ma = getModels();
		final Map<String, SchedBlock> cache =
			new HashMap<String, SchedBlock>();
		
		for (final SchedBlockQueueItem qItem : qItems) {
			final String id = qItem.uid;
			final SchedBlock sb;
			if (cache.containsKey(id)) {
				// Already got this SchedBlock, so use it again
				sb = cache.get(id);
			} else {
				// 1st time this SchedBlock has appeared in this queue
				sb = ma.getSchedBlockFromEntityId(id);
			}
			result.add(new ManifestSchedBlockQueueItem(qItem, sb));
		}
		

		return result;
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.array.guis.AbstractArrayPanel#arrayAvailable()
	 */
	@Override
	protected void arrayAvailable() {
		final SchedBlockQueueItem[] ids = getArray().getQueue();
		final List<ManifestSchedBlockQueueItem> blocks = getSchedBlocks(ids);
		pendingModel.setData(blocks);
		showConnectivity();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.array.guis.AbstractArrayPanel#arrayAvailable()
	 */
	@Override
	protected void modelsAvailable() {
//		final List<SchedBlock> sbs = getModels().getAllSchedBlocks();
//		final List<List<ManifestSchedBlockQueueItem>> qs =
//			new Vector<List<ManifestSchedBlockQueueItem>>(); 
//
//		for (int i = 0; i < 2; i++) {
//			qs.add(new Vector<ManifestSchedBlockQueueItem>());
//		}
//		int i = 0;
//		while ((i++ < 20) && !sbs.isEmpty()) {
//			sbs.remove(0);
//		}
//		for (SchedBlock sb : sbs) {
//			qs.get(i%qs.size()).add(sb);
//			i ++;
//		}
//		currentModel.setData(qs.get(0));
//		pastModel.setData(qs.get(1));
		showConnectivity();
	}
	/* End Constructors and GUI building
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * Detail dialogues
	 * ================================================================
	 */
	/* End Detail dialogues
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * Messages to the user
	 * ================================================================
	 */
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
	 * Listening
	 * ================================================================
	 */
	/* End Listening
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * Running stand-alone
	 * ================================================================
	 */
    private static CurrentActivityPanel createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("Current Activity Panel");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Add the ubiquitous "Hello World" label.
        CurrentActivityPanel panel = new CurrentActivityPanel();
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
    			CurrentActivityPanel p = createAndShowGUI();
    			try {
    				p.runRestricted(false);
    			} catch (Exception e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
//    			p.arrayAvailable();
//    			p.modelsAvailable();
    		}
    	});
    }
	/*
	 * End Running stand-alone
	 * ============================================================= */
}
