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

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.text.Document;
import javax.swing.border.*;
import javax.swing.table.*;

import alma.scheduling.SBLite;
import alma.scheduling.ProjectLite;
import alma.exec.extension.subsystemplugin.PluginContainerServices;

public class QueuedSchedTab extends SchedulingPanelGeneralPanel implements SchedulerTab {

    //private String schedulerName;
    private String arrayName;
    private String type;
    private QueuedSchedTabController controller;
    private ArchiveSearchFieldsPanel archiveSearchPanel;
    //private JPanel middlePanel;
    //private JPanel bottomPanel;
    private JPanel topPanel;
    private JPanel centerPanel;
    private boolean searchingOnProject;
    private SBTable sbs;
    private SBTable queueSBs;
    private ProjectTable projects;
    private JTextArea executionInfo;
    private JButton destroyArrayB;
    private JButton addB;
    private JButton removeB;
    private JButton executeB;
    private JButton stopB;
    private JButton stopQB;
    private int currentExecutionRow;
    
    
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
        archiveSearchPanel.doSearch();
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
        controller.stopQueuedScheduling();
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
        setBorder(new TitledBorder("Queued Scheduling"));
        setLayout(new BorderLayout());
        createTopPanel();
        Dimension d = getPreferredSize();
        add(topPanel,BorderLayout.NORTH);
        add(createCenterPanel(),BorderLayout.CENTER);
    }
    private void createTopPanel() {
        createArchivePanel();
        destroyArrayB = new JButton("Destroy Array");
        topPanel = new JPanel(new FlowLayout());
        destroyArrayB.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    //ask if they really want to do this!
                    controller.destroyArray();
                    setEnable(false);
                }
        });
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        p.add(destroyArrayB);
        topPanel = new JPanel(new BorderLayout());
        topPanel.add(p, BorderLayout.NORTH);
        topPanel.add(archiveSearchPanel, BorderLayout.CENTER);
    }

    private void createArchivePanel() {
        archiveSearchPanel = new ArchiveSearchFieldsPanel();
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
        sbs = new SBTable(false, new Dimension(150,60));
        sbs.setOwner(this);
        JScrollPane sbPane = new JScrollPane(sbs,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        sbPane.setBorder(new TitledBorder("SBs Found"));
        sbPanel.add(sbPane,BorderLayout.CENTER);
        addB = new JButton("Add to Queue");
        addB.setToolTipText("Will add SB to queue.");
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
        queueSBs = new SBTable(true, new Dimension(150,60));
        queueSBs.setOwner(this);
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
        executeB.setToolTipText("Will execute the queue.");
        executeB.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                executeSBs();
            }
        });
        stopB = new JButton ("Stop SB");
        //stopB = new JButton ("Stop");
        stopB.setToolTipText("Will stop the current SB and move to the next SB.");
        stopB.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                stopSB();
            }
        });
        stopQB = new JButton("Stop Queue");
        stopQB.setToolTipText("Will stop the entire queue");
        stopQB.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                stopQueue();
            }
        });
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel,BoxLayout.Y_AXIS));
        JPanel foo = new JPanel(new FlowLayout(FlowLayout.CENTER,0,0));
        foo.add(removeB);
        foo.add(executeB);
        buttonPanel.add(foo);
        foo = new JPanel(new FlowLayout(FlowLayout.CENTER,0,0));
        foo.add(stopQB);
        foo.add(stopB);
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
    private void executeSBs(){
        //get all ids from the queueSB table and send them to control
        currentExecutionRow =0;
        controller.runQueuedScheduling(queueSBs.getAllSBIds());
        //setStopButtonsEnabled(true);
    }
    public void updateExecutionRow(){
        currentExecutionRow++;
    }
    
    public void setSBStatus(String sbid, String status){
        queueSBs.setSBExecStatusForRow(currentExecutionRow, sbid, status);
    }
    
    private void stopSB(){
    }

    private void stopQueue(){
        //controller.stopWholeQueue();
    }
    
    private void addSBsToQueue(){
        //get selected SBs from sbTable
        String[] selectedSBs = sbs.getSelectedSBs();
        SBLite[] sbs = controller.getSBLites(selectedSBs);
        for(int i=0; i < sbs.length; i++){
            System.out.println("sb lites "+sbs[i].schedBlockRef);
        }
        //pass these to queuedSBTable
        queueSBs.setRowInfo(sbs, true);
        controller.addSBs(selectedSBs);
    }
    private void removeSBsFromQueue(){
        // this might cause problems if one sb is finished and we're waiting for the next
        //exec block started event to update the currentSB, gotta think of something better
        if(isSelectedSBRunning()){
            logger.info("Scheduler ("+controller.getSchedulerName() +"): cannot remove a running sb from the queue");
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

    }

    /**
      * returns true if one of the selected sbs is currently the running one.
      * doesn't say which, just does a general check
      */
    private boolean isSelectedSBRunning(){
        String[] ids = queueSBs.getSelectedSBs();
        for(int i=0;i < ids.length;i++){
            if(ids[i].equals(controller.getCurrentSB())){
                return true;
            }
        }
        return false;
    }
}
