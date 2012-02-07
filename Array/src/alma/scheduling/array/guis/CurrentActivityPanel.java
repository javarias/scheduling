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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

import alma.JavaContainerError.wrappers.AcsJContainerServicesEx;
import alma.scheduling.ArrayGUIOperation;
import alma.scheduling.Master;
import alma.scheduling.MasterHelper;
import alma.scheduling.SchedBlockExecutionItem;
import alma.scheduling.SchedBlockQueueItem;
import alma.scheduling.array.executor.AbortingExecutionState;
import alma.scheduling.array.executor.ArchivingExecutionState;
import alma.scheduling.array.executor.CompleteExecutionState;
import alma.scheduling.array.executor.FailedArchivingExecutionState;
import alma.scheduling.array.executor.FailedExecutionState;
import alma.scheduling.array.executor.ManualCompleteExecutionState;
import alma.scheduling.array.executor.ManualReadyExecutionState;
import alma.scheduling.array.executor.ManualRunningExecutionState;
import alma.scheduling.array.executor.ReadyExecutionState;
import alma.scheduling.array.executor.RunningExecutionState;
import alma.scheduling.array.executor.StartingExecutionState;
import alma.scheduling.array.executor.StoppingExecutionState;
import alma.scheduling.array.guis.SBExecutionTableModel.When;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.dao.ModelAccessor;
import alma.scheduling.policy.gui.PolicyChangeListener;
import alma.scheduling.policy.gui.PolicyManagementPanel;
import alma.scheduling.policy.gui.PolicySelectionListener;
import alma.scheduling.utils.ErrorHandling;

/**
 *
 * @author dclarke
 * $Id: CurrentActivityPanel.java,v 1.21 2012/02/07 00:06:39 dclarke Exp $
 */
@SuppressWarnings("serial")
public class CurrentActivityPanel extends AbstractArrayPanel
							 implements PolicyChangeListener {
	/*
	 * ================================================================
	 * Constants
	 * ================================================================
	 */
	private final static String BlankLabel = " ";
	
	/** SBs in any of these states should be shown on the current panel */
	private final static Set<String> CurrentStates = new HashSet<String>();
	
	/** SBs in any of these states should be shown on the past panel */
	private final static Set<String> PastStates = new HashSet<String>();
	
	/**
	 *  SBs in any of these states mean the Stop and Abort buttons
	 *  should be active
	 */
	private final static Set<String> RunningStates = new HashSet<String>();
	
	static {
		CurrentStates.add(AbortingExecutionState.class.getSimpleName());
		CurrentStates.add(ArchivingExecutionState.class.getSimpleName());
		CurrentStates.add(ReadyExecutionState.class.getSimpleName());
		CurrentStates.add(RunningExecutionState.class.getSimpleName());
		CurrentStates.add(StartingExecutionState.class.getSimpleName());
		CurrentStates.add(StoppingExecutionState.class.getSimpleName());
		CurrentStates.add(ManualReadyExecutionState.class.getSimpleName());
		CurrentStates.add(ManualRunningExecutionState.class.getSimpleName());
		
		PastStates.add(CompleteExecutionState.class.getSimpleName());
		PastStates.add(FailedExecutionState.class.getSimpleName());
		PastStates.add(ManualCompleteExecutionState.class.getSimpleName());
		PastStates.add(FailedArchivingExecutionState.class.getSimpleName());
		
		RunningStates.add(RunningExecutionState.class.getSimpleName());
		RunningStates.add(ManualRunningExecutionState.class.getSimpleName());
	}
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
	private JButton   stopSB;
	private JCheckBox fullAuto;
	private JCheckBox activeMode;
	private JButton   setPolicy;
	private JButton   startExec;
	private JButton   stopExec;
	private JButton   destroyArray;
	
	private SchedBlockQueueItem runningExecution = null;
	
	/** the things which sorts the SchedBlocks in our tables */
	// Note that there is no pending sorter - the queue is ordered
	private TableRowSorter<SBExecutionTableModel> currentSorter;
	private TableRowSorter<SBExecutionTableModel> pastSorter;

	/** Used to convey information to the user */
	private JLabel statusMessage;
	
	private JPanel pendingPanel;
	private JPanel currentPanel;
	private JPanel pastPanel;
	private JSplitPane bottomSplit;
	private JPanel commonButtons;
	private JPanel normalButtons;
	private JPanel manualButtons;
	private JPanel dynamicButtons;
	private JLabel currentPanelLabel;
	
	private PolicyManagementPanel policyPanel = null;

	private boolean deactivated = false;
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
		addWidgets();
		showConnectivity();
	}
	
	/**
	 * Basic constructor.
	 */
	public CurrentActivityPanel(String arrayName) {
		super(arrayName);
		createWidgets();
		addWidgets();
		showConnectivity();
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
		
		final int stateColumn = pastModel.stateColumn();
		if (stateColumn >= 0) {
			// -1 is used to denote "none", so if we get here there
			// should be a State column.
			final TableColumn tc = pastTable.getColumnModel().getColumn(stateColumn);
			if (tc != null) {
				tc.setCellRenderer(pastModel.getSchedBlockStateRenderer());
			}
		}

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
							System.out.format("pendingTable.getSelectionModel().setSelectionInterval(%d, %d)%n", r-1, r-1);
							pendingTable.getSelectionModel().setSelectionInterval(r-1, r-1);
						} catch (NullPointerException npe) {
						}
					}});
		moveDown = newButton("Demote",
				"Move the selected execution down the queue");
		moveDown.addActionListener(
				new ActionListener(){
					@Override
					public void actionPerformed(ActionEvent e) {
						try {
							final int r = pendingTable.getSelectedRow();
							final ManifestSchedBlockQueueItem mqi =
								pendingModel.getData(r);
							getArray().moveDown(mqi.getItem());
							pendingModel.fireTableRowsUpdated(r, r+1);
							pendingTable.getSelectionModel().setSelectionInterval(r+1, r+1);
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
							int r = pendingTable.getSelectedRow();
							final ManifestSchedBlockQueueItem mqi =
								pendingModel.getData(r);
							getArray().delete(mqi.getItem());
							pendingModel.fireTableRowsDeleted(r, r);
							if (r >= pendingTable.getRowCount()) {
								// Just removed the bottom row
								r --;
							}
							if (r >= 0) {
								pendingTable.getSelectionModel().setSelectionInterval(r, r);
							}
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
							getArray().stopRunningSchedBlock(
									getUserName(),
									getUserRole());
							//getArray().stop(
							//		getUserName(),
							//		getUserRole());
						} catch (NullPointerException npe) {
						}
					}});
		stopSB.setEnabled(true);
		
		fullAuto = new JCheckBox("Full Auto");
		fullAuto.setToolTipText("<html>" +
					"When checked, run the Scheduler in Full Auto" +
					" mode. Completed SchedBlocks will be made" +
					" available for re-execution if applicable." +
					"<br>" +
					"When not checked, run the Scheduler in Semi" +
					" Auto mode. Completed SchedBlocks will not be" +
					" made available for re-execution - they will" +
					" be marked as SUSPENDED (unless they are CSV" +
					" SBs)." +
				"</html>");
		fullAuto.addActionListener(
				new ActionListener(){
					@Override
					public void actionPerformed(ActionEvent e) {
						try {
							getArray().setFullAuto(
									fullAuto.isSelected(),
									getUserName(),
									getUserRole());
						} catch (NullPointerException npe) {
						}
					}});
		
		activeMode = new JCheckBox("Active");
		activeMode.setToolTipText("<html>" +
				"When checked, the Dynamic Scheduler scores and" +
				" ranks the SchedBlocks, it will automatically queue" +
				" the highest scoring SchedBlock for execution." +
				"<br>" +
				"When not checked, the Dynamic Scheduler scores and" +
				" ranks the SchedBlocks but does not automatically" +
				" queue any for execution." +
				"</html>");

		activeMode.addActionListener(
				new ActionListener(){
					@Override
					public void actionPerformed(ActionEvent e) {
						try {
							getArray().setActiveDynamic(
									activeMode.isSelected(),
									getUserName(),
									getUserRole());
						} catch (NullPointerException npe) {
						}
					}});
		
    	final PolicySelectionListener polly = new PolicySelectionListener(){

			@Override
			public void policySelected(String beanName) {
				logger.fine(
						String.format(
								"%n%nCurrentActivityPanel, policySelected: %s%n%n%n",
								beanName));
				getArray().setSchedulingPolicy(beanName);
//				ErrorHandling.printStackTrace();
			}
		};

		setPolicy = newButton("Set Policy",
				   "Set the current scheduling policy");
		setPolicy.addActionListener(
				new ActionListener(){
					
					@Override
					public void actionPerformed(ActionEvent e) {
						setPolicy.setEnabled(false);
						JFrame frame = new JFrame("Policy selection for " + array.getArrayName());
						policyPanel = new PolicyManagementPanel(getMaster());
						policyPanel.addListener(polly);
						frame.setSize(320, 400);
						frame.addWindowListener(new WindowListener(){

							@Override
							public void windowClosed(WindowEvent e) {
								policyPanel.removeListener(polly);
								policyPanel = null;
								setPolicy.setEnabled(true);
							}

							@Override public void windowOpened(WindowEvent e) {} // ignore
							@Override public void windowClosing(WindowEvent e) {} // ignore
							@Override public void windowIconified(WindowEvent e) {} // ignore
							@Override public void windowDeiconified(WindowEvent e) {} // ignore
							@Override public void windowActivated(WindowEvent e) {} // ignore
							@Override public void windowDeactivated(WindowEvent e) {} // ignore
						});
						frame.getContentPane().add(policyPanel);
						frame.pack();
						frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
						frame.setVisible(true);
					}
				});
		setPolicy.setEnabled(true);

		startExec = newButton("Start Queue",
							  "Start execution of SchedBlocks on this array");
		startExec.addActionListener(
				new ActionListener(){
					@Override
					public void actionPerformed(ActionEvent e) {
						try {
							getArray().start(getUserName(),
									         getUserRole());
						} catch (NullPointerException npe) {
							npe.printStackTrace();
						}
					}});
		startExec.setEnabled(true);
		stopExec = newButton("Stop Queue",
		  "Stop execution of SchedBlocks on this array");
		stopExec.addActionListener(
				new ActionListener(){
					@Override
					public void actionPerformed(ActionEvent e) {
						try {
							getArray().stop(getUserName(),
									getUserRole());
						} catch (NullPointerException npe) {
						}
					}});
		stopExec.setEnabled(true);
		
		destroyArray = newButton("Destroy Array",
			"Destroy the array");
		destroyArray.addActionListener(
				new ActionListener(){
					@Override
					public void actionPerformed(ActionEvent event) {
						DestroyArrayThread thread = new DestroyArrayThread();
						thread.start();
					}
					});
		destroyArray.setEnabled(true);

		currentSorter = new TableRowSorter<SBExecutionTableModel>(currentModel);
		currentTable.setRowSorter(currentSorter);
		pastSorter = new TableRowSorter<SBExecutionTableModel>(pastModel);
		pastTable.setRowSorter(pastSorter);

		statusMessage = new JLabel(BlankLabel);
		updateArrayButtons();
		updateSBButtons();
	}

	/**
	 * Create a Header styled label
	 * 
	 * @return
	 */
	private JLabel newLabel(final String label) {
		final JLabel result = new JLabel(label);
		
		result.setForeground(TitleColor);
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
//		final JLabel      label  = newLabel("Current Executions");
		final JScrollPane scroll = newScroller(currentTable);
		
		currentPanelLabel = newLabel("Current Executions");
		
		commonButtons = new JPanel();
		commonButtons.setLayout(new BoxLayout(commonButtons, BoxLayout.Y_AXIS));
		commonButtons.add(destroyArray);
		commonButtons.setAlignmentY(Component.TOP_ALIGNMENT);

		dynamicButtons = new JPanel();
		dynamicButtons.setLayout(new BoxLayout(dynamicButtons, BoxLayout.Y_AXIS));
		dynamicButtons.add(activeMode);
		dynamicButtons.add(setPolicy);
		dynamicButtons.setAlignmentY(Component.TOP_ALIGNMENT);

		normalButtons = new JPanel();
		normalButtons.setLayout(new BoxLayout(normalButtons, BoxLayout.Y_AXIS));
		normalButtons.add(stopSB);
		normalButtons.add(Box.createVerticalStrut(
				moveUp.getPreferredSize().height/2));
		normalButtons.add(fullAuto);
		normalButtons.add(dynamicButtons);
		normalButtons.add(Box.createVerticalStrut(
				moveUp.getPreferredSize().height/2));
		normalButtons.add(startExec);
		normalButtons.add(stopExec);
		normalButtons.setAlignmentY(Component.TOP_ALIGNMENT);

		manualButtons = new JPanel();
		manualButtons.setLayout(new BoxLayout(manualButtons, BoxLayout.Y_AXIS));
//		manualButtons.add(destroyArray);
		manualButtons.setAlignmentY(Component.TOP_ALIGNMENT);
		
		commonButtons.setVisible(false);
		normalButtons.setVisible(false);
		dynamicButtons.setVisible(false);
		manualButtons.setVisible(false);

		buttons.setLayout(new BoxLayout(buttons, BoxLayout.Y_AXIS));
		buttons.add(normalButtons);
		buttons.add(manualButtons);
		buttons.add(commonButtons);

		result.setLayout(new BorderLayout());
		result.add(currentPanelLabel, BorderLayout.NORTH);
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
		makeSameWidth(stopSB, startExec, stopExec,
			      moveUp, moveDown, delete, destroyArray,
			      setPolicy);
		forceToSize(stopSB.getPreferredSize(), fullAuto, activeMode);
		pendingPanel = createPendingPanel();
		currentPanel = createCurrentPanel();
		pastPanel    = createPastPanel();
		
		bottomSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
		
		bottomSplit.setTopComponent(currentPanel);
		bottomSplit.setBottomComponent(pastPanel);
		
		bottomSplit.setDividerLocation(0.5);
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(bottomSplit);
		add(statusMessage);
	}

	/**
	 * Manifest the given SchedBlockQueueItems
	 * 
	 * @param qItems
	 * @return
	 */
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

	/**
	 * Manifest a single SchedBlockQueueItem
	 * 
	 * @param item
	 * @return
	 */
	public ManifestSchedBlockQueueItem getSchedBlock(
			SchedBlockQueueItem item) {
		final ModelAccessor ma = getModels();
		final SchedBlock sb = ma.getSchedBlockFromEntityId(item.uid);
		final ManifestSchedBlockQueueItem result =
			new ManifestSchedBlockQueueItem(item, sb);

		return result;
	}

	/**
	 * Manifest a single SchedBlockExecutionItem
	 * 
	 * @param item
	 * @return
	 */
	public ManifestSchedBlockQueueItem getSchedBlock(
			SchedBlockExecutionItem item) {
		final ModelAccessor ma = getModels();
		final SchedBlock sb = ma.getSchedBlockFromEntityId(item.uid);
		final SchedBlockQueueItem fake = new SchedBlockQueueItem(
				item.timestamp, item.uid);
		final ManifestSchedBlockQueueItem result =
			new ManifestSchedBlockQueueItem(fake, sb, item.executionState);
		
		return result;
	}

    /* (non-Javadoc)
     * @see alma.scheduling.array.guis.AbstractArrayPanel#arrayAvailable()
     */
    @Override
    protected void arrayAvailable() {
    	if (getArray().isManual()) {
    		currentPanelLabel.setText("Configured SchedBlock");
    		bottomSplit.setDividerLocation(1.0/3.0);
    		normalButtons.setVisible(false);
    		manualButtons.setVisible(true);
    	} else {
    		final JSplitPane topSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
    		bottomSplit.setTopComponent(topSplit);
    		topSplit.setTopComponent(pendingPanel);
    		topSplit.setBottomComponent(currentPanel);
    		bottomSplit.setDividerLocation(2.0/3.0);
    		topSplit.setDividerLocation(1.0/2.0);
    		normalButtons.setVisible(true);
    		manualButtons.setVisible(false);
    		dynamicButtons.setVisible(getArray().isDynamic());
    	}
    	commonButtons.setVisible(true);
    	
    	SchedBlockQueueItem[] ids;
    	List<ManifestSchedBlockQueueItem> blocks;
    	
    	ids = getArray().getQueue();
    	blocks = getSchedBlocks(ids);
    	pendingModel.setData(blocks);
    	
		final SchedBlockExecutionItem[] items = array.getExecutions();
		for (final SchedBlockExecutionItem item : items) {
			addExecutionToModels(item);
		}

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
	 * GUI management
	 * ================================================================
	 */
	/**
	 * Determine if the given ManifestSchedBlockQueueItem is the same
	 * execution as we currently have running (if there's one running).
	 * 
	 * @param m
	 * @return
	 */
	private boolean isRunningExecution(ManifestSchedBlockQueueItem m) {
		try {
			SchedBlockQueueItem inner = m.getItem();
			return (inner.timestamp == runningExecution.timestamp) &&
					(inner.uid.equals(runningExecution.uid));
		} catch (NullPointerException e) {
			return false;
		}
	}
	
	/**
	 * Set the appropriate buttons according to whether we have a
	 * current execution.
	 */
	private void updateSBButtons() {
		if (!deactivated) {
			stopSB.setEnabled(runningExecution != null);
		}
	}
	
	/**
	 * Set the appropriate buttons according to the status of the Array
	 */
	private void updateArrayButtons() {
		if (!deactivated) {
			ArrayAccessor a = getArray();
			
			if (a == null) {
				startExec.setEnabled(false);
				stopExec.setEnabled(false);
				fullAuto.setSelected(false);
				activeMode.setSelected(false);
				setPolicy.setEnabled(false);
				destroyArray.setEnabled(false);
			} else {
				startExec.setEnabled(!a.isRunning());
				stopExec.setEnabled(a.isRunning());
				fullAuto.setSelected(a.isFullAuto());
				activeMode.setSelected(a.isActiveDynamic());
				setPolicy.setEnabled(true);
				destroyArray.setEnabled(true);
			}
		}
	}
	
	/**
	 * Set the appropriate buttons according to the status of the Array
	 */
	private void deactivateAllButtons() {
		deactivated = true;
		startExec.setEnabled(false);
		stopExec.setEnabled(false);
		fullAuto.setEnabled(false);
		activeMode.setEnabled(false);
		stopSB.setEnabled(false);
		moveUp.setEnabled(false);
		moveDown.setEnabled(false);
		delete.setEnabled(false);
		destroyArray.setEnabled(false);
		setPolicy.setEnabled(false);
	}
	/* End GUI management
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
		setStatusMessage(message, Color.black);
	}
	
	private void setStatusMessage(String message, Color colour) {
		statusMessage.setText(message);
		statusMessage.setForeground(colour);
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
	@Override
	protected void setArray(ArrayAccessor array) {
		super.setArray(array);

		PropertyChangeListener queueListener = new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				final SchedBlockQueueItem[] ids = getArray().getQueue();
				final List<ManifestSchedBlockQueueItem> blocks = getSchedBlocks(ids);
				final int selected = pendingTable.getSelectedRow();
				pendingModel.setData(blocks);
				if (selected >= 0) {
					pendingTable.getSelectionModel().setSelectionInterval(selected, selected);
				}
			}
		};
		try {
			array.registerQueueCallback(queueListener);
		} catch (AcsJContainerServicesEx e) {
			e.printStackTrace();
		}
	
		PropertyChangeListener execListener = new PropertyChangeListener() {
			@Override
			public synchronized void propertyChange(PropertyChangeEvent evt) {
				{ // Logging block
					PrintStream o = System.out;
					o.format("execListener.propertyChange(%s, ", evt.getPropertyName());
					try {
						SchedBlockQueueItem item = (SchedBlockQueueItem) evt.getNewValue();
						o.format("%s %s)",
								item.uid, item.timestamp);
					} catch (NullPointerException e) {
						o.format("Unexpected null)");
					} catch (ClassCastException e) {
						o.format("Unexpected class of object %s)",
								evt.getNewValue().getClass().getName());
					}
					if (CurrentStates.contains(evt.getPropertyName())) {
						o.print(" - Current");
					} else if (PastStates.contains(evt.getPropertyName())) {
						o.print(" - Past");
					} else {
						o.print(" - Unknown!");
					}
					o.println(" state");
				} // end Logging block

				SchedBlockQueueItem item = (SchedBlockQueueItem) evt.getNewValue();
				final String state = evt.getPropertyName();
				addExecutionToModels(item, state);
			}
		};

		try {
			array.registerExecutionCallback(execListener);
		} catch (AcsJContainerServicesEx e) {
			e.printStackTrace();
		}

		PropertyChangeListener guiListener = new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				{ // Logging block
					PrintStream o = System.out;
					o.format("guiListener.propertyChange(%s, ", evt.getPropertyName());
					try {
						String[] item = (String[]) evt.getNewValue();
						o.format("%s, %s)",
								item[0], item[1]);
					} catch (IndexOutOfBoundsException e) {
						o.format("Unexpected lack of values - expecting 2)");
					} catch (NullPointerException e) {
						o.format("Unexpected null)");
					} catch (ClassCastException e) {
						o.format("Unexpected class of object %s)",
								evt.getNewValue().getClass().getName());
					}
					o.println();
				} // end Logging block
				final String operation = evt.getPropertyName();
				final String[] item = (String[]) evt.getNewValue();
				if (operation.equals(ArrayGUIOperation.DESTROYED.toString())) {
					setStatusMessage(String.format(
							"<html>Array %s by %s at %TT</html>",
							operation.toString(),
							item[0],
							new Date()));
					deactivateAllButtons();
				} else {
					setStatusMessage(String.format(
							"<html>Array set to %s by %s (%s) at %TT</html>",
							operation.toString(),
							item[0],
							item[1],
							new Date()));
					updateArrayButtons();
				}
			}
		};
		try {
			array.registerGUICallback(guiListener);
		} catch (AcsJContainerServicesEx e) {
			e.printStackTrace();
		}
	
		updateArrayButtons();
		updateSBButtons();
	}
	
	private void addExecutionToModels(SchedBlockExecutionItem item) {	
		final ManifestSchedBlockQueueItem block = getSchedBlock(item);
		addExecutionToModels(block, item.executionState);
	}
	
	private void addExecutionToModels(SchedBlockQueueItem item, String state) {	
		final ManifestSchedBlockQueueItem block = getSchedBlock(item);
		addExecutionToModels(block, state);
	}
	
	private void addExecutionToModels(ManifestSchedBlockQueueItem block, String state) {
		if (!pastModel.contains(block)) {
			// Don't do anything if the execution is already in
			// a past (terminal) state. The notifications are
			// not guaranteed to arrive in the order sent!
			block.setExecutionState(state);
			if (CurrentStates.contains(state)) {
				currentModel.ensureIn(block);
			} else if (PastStates.contains(state)) {
				currentModel.ensureOut(block);
				pastModel.ensureIn(block);
			}
		}

		if (RunningStates.contains(state)) {
			// This execution is actively running.
			if (runningExecution == null) {
				// Nothing running, so use the new block
				runningExecution = block.getItem();
			} else if (!isRunningExecution(block)) {
				// Something else is currently running.
				// TODO: R8 something sensible.
			}
		} else {
			// This execution is not actively running
			if (isRunningExecution(block)) {
				// but it was, so clear it
				runningExecution = null;
			}
		}
		stopSB.setEnabled(runningExecution != null);
	}

	@Override
	public void refreshPolicyList() {
		if (policyPanel != null) {
			// Decided to pass on the notification from this parent GUI
			// rather than have the policyPanel listen for itself - it
			// seemed a bit of a hack to have policyPanel need to know
			// about the environment in which it operated (e.g. the
			// ContainerServices which it would need in order to set up
			// an offshoot callback thingy.
			policyPanel.refreshPolicyList();
		}
	}
	/* End Listening
	 * ============================================================= */

	
	

	/*
	 * ================================================================
	 * Interaction with components
	 * ================================================================
	 */
	private Master getMaster() {
		final String masterComponentName = "SCHEDULING_MASTERSCHEDULER";
		Master result = null;
		
		try {
			org.omg.CORBA.Object o = services.getComponentNonSticky(masterComponentName);
			result = MasterHelper.narrow(o);
		} catch (AcsJContainerServicesEx e) {
			ErrorHandling.severe(services.getLogger(),
					String.format("Cannot get reference to component %s - %s",
							masterComponentName, e.getMessage()),
					e);
		}
		return result;
	}
	/* End Interaction with components
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
        CurrentActivityPanel panel = new CurrentActivityPanel("testArray");
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
    
    private class DestroyArrayThread extends Thread {

		@Override
		public synchronized void start() {
			destroyArray.setEnabled(false);
			try {
				arrayName = getArray().getArrayName();
				setStatusMessage(
						String.format("Destroying array %s...",
						arrayName));
				Master m = getMaster();
				m.destroyArray(arrayName, getUserName(), getUserRole());
			} catch (NullPointerException npe) {
				npe.printStackTrace();
				setStatusMessage(String.format(
					"Cannot destroy array - cannot find master component! See logs for details."),
					Color.RED);
			} catch (Exception e) {
				e.printStackTrace();
				ErrorHandling.severe(services.getLogger(),
						String.format("Internal error (%s) whilst destroying array - %s",
								e.getClass().getSimpleName(), e.getMessage()),
						e);
				setStatusMessage(String.format(
					"Cannot destroy array - internal error! See logs for details."),
					Color.RED);
			}
			destroyArray.setEnabled(true);
		}
    }

	
	
	/*
	 * ================================================================
	 * IStateKeeping implementation
	 * ================================================================
	 */
	@Override
	public CurrentActivityPanelState getState() {
		safeInfo(String.format("%s.getState():%n%s",
				this.getClass().getSimpleName(),
				ErrorHandling.printedStackTrace(new Exception())));
		final CurrentActivityPanelState state = new CurrentActivityPanelState();
		
		if (getArray().isManual()) {
			// No top pane showing.
			state.setTopSplitDividerLocation(0.0);
			state.setBottomSplitDividerLocation(calculateSplitPaneDividerLocation(bottomSplit));
		} else {
			state.setTopSplitDividerLocation(calculateSplitPaneDividerLocation((JSplitPane)bottomSplit.getTopComponent()));
			state.setBottomSplitDividerLocation(calculateSplitPaneDividerLocation(bottomSplit));
		}

		return state;
	}

	@Override
	public void setState(Serializable inState) throws Exception {
		safeInfo(String.format("%s.setState(Serializable):%n%s",
				this.getClass().getSimpleName(),
				ErrorHandling.printedStackTrace(new Exception())));
		if (inState == null) {
			safeInfo("\tinState is null, returning");
			return;
		}
		final CurrentActivityPanelState state = (CurrentActivityPanelState) inState;
		
		if (!getArray().isManual()) {
			// Top pane showing, so restore it
			final JSplitPane topSplit = (JSplitPane) bottomSplit.getTopComponent();
			topSplit.setDividerLocation(state.getTopSplitDividerLocation());
		}
		bottomSplit.setDividerLocation(state.getBottomSplitDividerLocation());
	}
	/*
	 * End IStateKeeping implementation
	 * ============================================================= */
}
