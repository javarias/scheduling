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
 * File QueuedSchedTab.java
 */

package alma.scheduling.AlmaScheduling.GUI.OmcSchedulingPanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.TitledBorder;
import javax.swing.text.Document;

import alma.exec.extension.subsystemplugin.PluginContainerServices;
import alma.scheduling.ProjectLite;
import alma.scheduling.SBLite;
import alma.scheduling.utils.Bag;
import alma.scheduling.utils.HashBag;

public class QueuedSchedTab extends SchedulingPanelGeneralPanel implements SchedulerTab {

    //private String schedulerName;
    private String arrayName;
    private JLabel arrayStatusDisplay;
    private String type;
    private QueuedSchedTabController controller;
    private ArchiveSearchFieldsPanel archiveSearchPanel;
    //private JPanel middlePanel;
    private JPanel mainPanel;
    private JPanel topPanel;
    private JPanel centerPanel;
    private boolean searchingOnProject;
    private SBTable sbs;
    private SBTable queueSBs;
    private QueuedSchedQueueManager sbHelper;
    private ProjectTable projects;
    private JTextArea executionInfo;
    private JButton destroyArrayB;
    private JButton addB;
    private JButton removeB;
    private JButton executeB;
    private JButton stopB;
    private JButton stopQB;
    private JButton abortQB;
    private JButton abortB;
    private JCheckBox fullAutoButton;
    private int currentExecutionRow=-1;
    private int archivingRow;
    
    
    public QueuedSchedTab(String aName){
        type = "queued";
        arrayName = aName;
        searchingOnProject = true;
        createLayout();
    }
    public QueuedSchedTab(PluginContainerServices cs, String aName){
        super();
        super.onlineSetup(cs);
        type = "queued";
        arrayName = aName;
        searchingOnProject = true;
        controller = new QueuedSchedTabController(cs, this, aName);
        //controller.setSchedulerName(title);
        setTitle(controller.getSchedulerName());
        createLayout();
        archiveSearchPanel.setCS(cs);
        projects.setCS(cs);
        sbs.setCS(cs);
        queueSBs.setCS(cs);
        doInitialSearch();
    }
    private void doInitialSearch(){
    	boolean manualMode = false;
        archiveSearchPanel.doSearch(manualMode);

    }
    public void selectFirstResult(){
        projects.showFirstProject();
    }

///////////////////////////////
    public void stop() throws Exception {
        super.stop();
        exit();
    }
///////////////////////////////
    public void exit(){
        try {
            controller.stopQueuedScheduling();
        } catch(Exception e){}
    }
    public String getSchedulerType(){
        return type;
    }
    public String getArrayName() {
        return arrayName;
    }
    public String getSchedulerName() {
        return controller.getSchedulerName();
    }
///////////////////////////////
    private void createLayout(){
    	mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        
        
        mainPanel.setBorder(new TitledBorder("Queued Scheduling"));
        gridBagConstraints.weightx = 0.2;
        gridBagConstraints.weighty = 0.2;
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        createTopPanel();
        mainPanel.add(topPanel,gridBagConstraints);
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.CENTER;
        mainPanel.add(createCenterPanel(),gridBagConstraints);
        //setVisible(Boolean.TRUE);
        //mainPanel.setPreferredSize(new Dimension(180,250));
        Dimension d = getPreferredSize();
        //mainPanel.setMaximumSize(d);
        //mainPanel.setMinimumSize(new Dimension(480,600));
        this.setLayout(new GridBagLayout());
        add(mainPanel,gridBagConstraints);
        
        setButtons();
    }
    private void createTopPanel() {
        createArchivePanel();
        
        fullAutoButton = new JCheckBox("Full Automatic Mode");
        fullAutoButton.setSelected(false);
        fullAutoButton.addItemListener(new ItemListener(){
        	public void itemStateChanged(ItemEvent e) {
        		if(fullAutoButton.isSelected()) {
        			controller.setRunMode(true);
        		} else {
        			controller.setRunMode(false);
        		}
        		setButtons();
        	}
        });

        destroyArrayB = new JButton("Destroy Array");
        JLabel arrayStatusL = new JLabel("Array Status =");
        arrayStatusDisplay = new JLabel(controller.getArrayStatus());
        topPanel = new JPanel(new FlowLayout());
        destroyArrayB.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    //ask if they really want to do this!
                    controller.destroyArray();
                    setEnable(false);
                }
        });

        Box box = Box.createHorizontalBox();
        box.add(fullAutoButton);
        box.add(Box.createHorizontalStrut(30));
        box.add(arrayStatusL);
        box.add(arrayStatusDisplay);
        box.add(Box.createHorizontalStrut(30));
        box.add(destroyArrayB);

        topPanel = new JPanel(new BorderLayout());
        topPanel.add(box, BorderLayout.NORTH);
        topPanel.add(archiveSearchPanel, BorderLayout.CENTER);
    }

    private void createArchivePanel() {
        archiveSearchPanel = new ArchiveSearchFieldsPanel("arrayMode",false);
        archiveSearchPanel.setOwner(this);
        archiveSearchPanel.connected(true);
    }

    public JPanel createCenterPanel() {
        centerPanel = new JPanel(new GridLayout(2,2));
        //first row: left hand cell == Project Table
        projects = new ProjectTable(new Dimension(150,75));
        projects.setOwner(this);
        JScrollPane projectPane = new JScrollPane(projects,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        projectPane.setBorder(new TitledBorder("Projects Found"));

        centerPanel.add(projectPane);

        //first row: right hand cell == sbTable + button
        JPanel sbPanel = new JPanel(new BorderLayout());
        sbs = new SBTable(false, new Dimension(150,75));
        sbs.setOwner(this);
        JScrollPane sbPane = new JScrollPane(sbs,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        sbPane.setBorder(new TitledBorder("SBs Found"));
        sbPanel.add(sbPane,BorderLayout.CENTER);
        addB = new JButton("Add to Queue");
        addB.setToolTipText("Will add SB to end of the queue.");
        addB.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                addSBsToQueue();
            }
        });
        JPanel button1 = new JPanel(new FlowLayout(FlowLayout.CENTER,0,0));
        button1.add(addB);
        sbPanel.add(button1, BorderLayout.SOUTH);

        centerPanel.add(sbPanel);
        //second row: left hand cell == sbTable + buttons (PROBLEMATIC ONE!)
        JPanel queuePanel = new JPanel(new BorderLayout());
        queueSBs = new SBTable(true, new Dimension(150,75));
        queueSBs.setOwner(this);
        sbHelper = new QueuedSchedQueueManager();
        JScrollPane queueSbPane = new JScrollPane(queueSBs,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        //queueSbPane.setMaximumSize( new Dimension(130,60));
        queueSbPane.setBorder(new TitledBorder("SB Queue"));
        

        removeB = new JButton("Remove");
        removeB.setToolTipText("Will remove SB from queue.");
        removeB.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                removeSBsFromQueue();
            }
        });

        executeB = new JButton("Run Queue");
        //executeB = new JButton("Run");
        // The tooltip for executeB is set in setButtons(), not here
        executeB.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                executeSBs();
            }
        });
        stopB = new JButton ("Stop SB");
        //stopB = new JButton ("Stop");
        stopB.setToolTipText("Will stop the SB at next subscan and move to the next SB.");
        stopB.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                stopSB();
            }
        });
        stopQB = new JButton("Stop Queue");
        stopQB.setToolTipText("Will stop the entire queue at end of current SB's subscan.");
        stopQB.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                stopQueue();
            }
        });

        abortB = new JButton("Abort SB");
        abortB.setToolTipText("Will abort the current SB right away.");
        abortB.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                abortCurrentSB();
            }
        });
        abortQB = new JButton("Abort Queue");
        abortQB.setToolTipText("Will abort the entire queue right away.");
        abortQB.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                abortQueue();
            }
        });
	
        JPanel buttonPanel = new JPanel();
	buttonPanel.setLayout(new BoxLayout(buttonPanel,BoxLayout.Y_AXIS));
        JPanel foo = new JPanel();
        foo.setLayout(new BoxLayout(foo,BoxLayout.X_AXIS));
	removeB.setSize(abortQB.getSize());
	executeB.setSize(abortQB.getSize());
        foo.add(removeB);
        foo.add(executeB);
        buttonPanel.add(foo);
        foo = new JPanel();
        foo.setLayout(new BoxLayout(foo,BoxLayout.X_AXIS));
	stopQB.setSize(abortQB.getSize());
	stopB.setSize(abortQB.getSize());
        foo.add(stopQB);
        foo.add(stopB);
        buttonPanel.add(foo);
        foo = new JPanel();
        foo.setLayout(new BoxLayout(foo,BoxLayout.X_AXIS));
	abortB.setSize(abortQB.getSize());
	//iabortQB.setSize(abortQB.getSize());
        foo.add(abortB);
        foo.add(abortQB);
        buttonPanel.add(foo);

        queuePanel.add(queueSbPane, BorderLayout.CENTER);
        queuePanel.add(buttonPanel, BorderLayout.SOUTH);

        centerPanel.add(queuePanel);
    //    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0,0));
     //   buttonPanel.add(removeB);
      //  buttonPanel.add(executeB);
       // buttonPanel.add(stopB);

        //queuePanel.add(queueSbPane, BorderLayout.CENTER);
        //queuePanel.add(buttonPanel, BorderLayout.SOUTH);

       // centerPanel.add(queuePanel);
        
        //second row: right hand cell == execution info text area
        executionInfo = new JTextArea();
        executionInfo.setEditable(false);
        executionInfo.setMaximumSize(new Dimension(150,75));
        executionInfo.setPreferredSize(new Dimension(150,75));
        JScrollPane taPane = new JScrollPane(executionInfo,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        taPane.setBorder(new TitledBorder("Execution Info"));

        centerPanel.add(taPane);

        return centerPanel;
    }
    

    public void setEnable(boolean b){
        destroyArrayB.setEnabled(b);
        stopB.setEnabled(b);
        executeB.setEnabled(b);
        removeB.setEnabled(b);
        addB.setEnabled(b);
        queueSBs.setEnabled(b);
    }

    public void setSearchMode(boolean b) {
        searchingOnProject =b;
        projects.setSearchMode(b);
        sbs.setSearchMode(b);
    }

    public void clearTables() {
        sbs.clear();
        //queueSBs.clear();
        projects.clear();
    }
    
    public void updateProjectView(ProjectLite[] p){
        projects.setRowInfo(p);
    }
    
    public void updateSBView(SBLite[] sb){
        sbs.setRowInfo(sb, false);
    }
    
    public void updateExecutionInfo(String info){
        try {
            Document doc = executionInfo.getDocument();
            doc.insertString (doc.getLength(), info, null);
            int newCaretPosition = doc.getLength();
            executionInfo.setCaretPosition (newCaretPosition);
        } catch(Exception e){
            e.printStackTrace();
        }
        //executionInfo.append(info);
    }
    protected void updateArrayStatus() {
        String stat = controller.getArrayStatus();
        if(stat.equals("Destroyed")){
            destroyArrayB.setEnabled(false);        
        }
        arrayStatusDisplay.setText(stat);
        arrayStatusDisplay.validate();
        revalidate();
    }
    private void executeSBs(){
        //TODO: check if queue is currently running
        queueSBs.resetStatusColumns();
        executionInfo.setText("");
        //get all ids from the queueSB table and send them to control
        currentExecutionRow =0;
        archivingRow=0;

	if(queueSBs.getAllSBIds().length!=0){
        controller.runQueuedScheduling(queueSBs.getAllSBIds());
        }
        else {
        	JOptionPane.showMessageDialog(null,
        		    "there is no any SB in Queue. Queue Scheduling can not run anything", 
        		    "Message",JOptionPane.PLAIN_MESSAGE);
        };

        //setStopButtonsEnabled(true);
    }
    public void updateExecutionRow(){
        currentExecutionRow++;
    }
    
    protected void updateArchivingRow(){
        archivingRow++;
    }
    public void setSBStatus(String sbid, String status){
        if(status.equals("ARCHIVED")){
            queueSBs.setSBExecStatusForRow(archivingRow, sbid, status);
        }else{
            queueSBs.setSBExecStatusForRow(currentExecutionRow, sbid, status);
        }
    }
    
    private void stopSB(){
        logger.fine("stopSB called in QS plugin");
        try {
            controller.stopSB();
        }catch(Exception e){
            showErrorPopup(e.toString(), "stopSB");
        }
        showInfoPopup("SB will stop after current subscan.","Stop message");
    }

    private void stopQueue(){
        try {
            controller.stopWholeQueue();
        }catch(Exception e){
            showErrorPopup(e.toString(), "stopQueue");
        }
        showInfoPopup("Queue will stop after current subscan.","Stop message");
    }

    private void abortCurrentSB(){
        try {
            controller.abortSB();
        }catch(Exception e){
            showErrorPopup(e.toString(), "abortSB");
        }
    }
    private void abortQueue(){
        try {
            controller.abortQueue();
        }catch(Exception e){
            showErrorPopup(e.toString(), "abortQueue");
        }
    }
    
    private void addSBsToQueue(){
        //get selected SBs from sbTable
        String[] selectedSBs = sbs.getSelectedSBs();
        SBLite[] sbs = controller.getSBLites(selectedSBs);
        //pass these to queuedSBTable
        queueSBs.setRowInfo(sbs, true);
        // add SBs into QueueSchedTabController deferred to execution
        controller.addSBs(selectedSBs);
        sbHelper.addAll(selectedSBs);
        setButtons();
    }
    private void removeSBsFromQueue(){
        // this might cause problems if one sb is finished and we're waiting for the next
        //exec block started event to update the currentSB, gotta think of something better
        if(isSelectedSBRunning()){
            logger.warning("Scheduler ("+controller.getSchedulerName() +"): cannot remove a running sb from the queue");
            //force de-selection
            queueSBs.clearSelectedItems();
            return;
        }
        //remove selected sb from QueuedSbTable
        String[] selectedSBs = queueSBs.getSelectedSBs();
        int[] indices = queueSBs.getIndicesOfSBsToRemove();
        controller.removeSBs(selectedSBs, indices);
        queueSBs.removeRowsFromQueue();
        //and update view/scheduler/etc
        sbHelper.removeAll(selectedSBs);
        setButtons();
    }
    
	/**
      * returns true if one of the selected sbs is currently the running one.
      * doesn't say which, just does a general check
      */
    private boolean isSelectedSBRunning(){
        String[] ids = queueSBs.getSelectedSBs();
        int[] indices = queueSBs.getIndicesOfSBsToRemove();
        for(int i=0;i < ids.length;i++){
            if(ids[i].equals(controller.getCurrentSB()) && (indices[i]==currentExecutionRow)  ){
                return true;
            }
        }
        return false;
    }

	public int getCurrentExecutionRow() {
		return currentExecutionRow;
	}

	/*
	 * ================================================================
	 * Setting of controls
	 * ================================================================
	 */
    private void setExecuteButton() {
    	// Assume runnable unless proven otherwise
    	boolean enabled = true;
    	String tooltip = "Will execute all SBs in the queue.";

    	
    	if (sbHelper.hasElements()) {
    		if (!fullAutoButton.isSelected()) {
    			// Semi-auto, no duplicates allowed
    			if (sbHelper.hasMultiples()) {
    				enabled = false;
    				tooltip = "The queue cannot contain multiple instances of<br>" + 
    				          "the same SB except in Full Automatic Mode.<br>" +
    				          "Either set Full Automatic Mode or remove duplicate<br>" +
    				          "SBs from the queue.";
    			}
    		}
    	} else {
    		// No elements
			enabled = false;
			tooltip = "Cannot run an empty queue. Please add SBs<br>" +
			          "in order to run the queue.";
    	}
    	
    	executeB.setEnabled(enabled);
    	executeB.setToolTipText(String.format(
    			"<html>%s</html>", tooltip));
	}

    private void setButtons() {
    	setExecuteButton();
	}
	/*
	 * End of Setting of controls
	 * ----------------------------------------------------------------
	 */
}
