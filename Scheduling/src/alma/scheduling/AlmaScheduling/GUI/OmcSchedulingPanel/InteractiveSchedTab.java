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
 * File InteractiveSchedTab.java
 */
package alma.scheduling.AlmaScheduling.GUI.OmcSchedulingPanel;

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

import alma.scheduling.SBLite;
import alma.scheduling.ProjectLite;
import alma.exec.extension.subsystemplugin.PluginContainerServices;

public class InteractiveSchedTab extends SchedulingPanelGeneralPanel implements SchedulerTab {
    //private String schedulerName;
    //private String arrayName;
    private String type;
    
    private ArchiveSearchFieldsPanel archiveSearchPanel;
    private InteractiveSchedTabController controller;
    private JPanel topPanel;
    private JPanel middlePanel;
    private JPanel bottomPanel;
    private JButton destroyArrayB;
    private JButton execB;
    private JButton stopB;
    private SBTable sbs;
    private ProjectTable projects; 
    private JLabel arrayStatusDisplay;
  //  private boolean sessionStarted;
    private boolean searchingOnProject; 

    public InteractiveSchedTab(){
        super();
    }

    public InteractiveSchedTab(PluginContainerServices cs, String aName){
        super();
        super.onlineSetup(cs);
        doSetup(aName);
    }
    protected void doSetup(String aName){
        searchingOnProject=true;
        //arrayName = aName;
        controller = new InteractiveSchedTabController(container, aName, this);
        controller.setArrayInUse(aName);
        controller.getISRef();
        type = "interactive"; 
        setTitle(aName+" (Interactive)");
        createLayout();
        archiveSearchPanel.setCS(container);
        projects.setCS(container);
        sbs.setCS(container);
        setEnable(true);
        doInitialSearch();
    }
    
    private void doInitialSearch() {
        archiveSearchPanel.doSearch();
    }
    protected void selectFirstResult(){
        projects.showFirstProject();
      
    }
    /////////// SchedulerTab stuff /////
    public String getSchedulerName(){
        return controller.getSchedulerName();
    }
    public String getArrayName(){
        return controller.getArrayName();
    }
    public String getSchedulerType(){
        return type;
    }
    public void exit(){
        controller.stopInteractiveScheduling();
    }
    ////////////////////////////////////
    public void start() throws Exception {
        super.start();
        validate();
    }
    public void stop() throws Exception {
        super.stop();
        exit();
    }
    ////////////////////////////////////
    
    private void createLayout(){
        setBorder(new TitledBorder("Interactive Scheduling"));
        setLayout(new BorderLayout());
        createTopPanel();
        createMiddlePanel();
        Dimension d = getPreferredSize();
        add(topPanel,BorderLayout.NORTH);
        //add(archiveSearchPanel,BorderLayout.NORTH);
        add(middlePanel,BorderLayout.CENTER);
    }

    private void createTopPanel() {
        createArchivePanel();
        JLabel arrayStatusL = new JLabel("Array Status =");
        arrayStatusDisplay = new JLabel(controller.getArrayStatus());
        destroyArrayB = new JButton("Destroy Array");
        destroyArrayB.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    //ask if they really want to do this!
                    controller.destroyArray();
                    destroyArrayB.setEnabled(false);
                    stopB.setEnabled(false);
                    execB.setEnabled(false);
                }
        });
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        p.add(arrayStatusL);
        p.add(arrayStatusDisplay);
        p.add(destroyArrayB);
        topPanel = new JPanel(new BorderLayout());
        topPanel.add(p, BorderLayout.NORTH);
        topPanel.add(archiveSearchPanel, BorderLayout.CENTER);
    }
    /**
      * Top panel contains check boxes for determining if we
      * search by project or by sb.
      */
    private void createArchivePanel() {
        archiveSearchPanel = new ArchiveSearchFieldsPanel();
        archiveSearchPanel.setOwner(this);
        archiveSearchPanel.connected(true);
    }

    /**
      * Middle panel contains he search text boxes and the buttons.
      */
    private void createMiddlePanel() {
        middlePanel = new JPanel(new GridLayout(2,2));

        //first row: lefthand cell = project table
        projects = new ProjectTable(new Dimension(150,75));
        projects.setOwner(this);
        JScrollPane projectPane = new JScrollPane(projects,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        projectPane.setBorder(new TitledBorder("Projects Found"));
        middlePanel.add(projectPane);

        //first row: right hand side cell: sb table & buttons
        JPanel sbPanel = new JPanel(new BorderLayout());
        sbs = new SBTable(true, new Dimension(150,60));
        sbs.setOwner(this);
        JScrollPane sbPane = new JScrollPane(sbs,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        sbPane.setBorder(new TitledBorder("SBs Found"));
        sbPanel.add(sbPane,BorderLayout.CENTER);
        execB = new JButton("Execute");
        execB.setToolTipText("Will execute the selected SB.");
        execB.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
            //disable searching while a SB is executing...
                executeSB();
            }
        });
        stopB = new JButton("Stop");
        stopB.setToolTipText("Will stop the currently running SB on this array");
        stopB.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                stopSB();
            }
        });
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER,0,0));
        buttons.add(execB);
        buttons.add(stopB);
        sbPanel.add(buttons, BorderLayout.SOUTH);
        middlePanel.add(sbPanel);
        
       //second row: left hand cell = project info textarea
        middlePanel.add(projects.getProjectInfoView());

       //second row: right hand cell = sb info textarea
        middlePanel.add(sbs.getSBInfoView());
    }

    /**
      * If true then search fields & execute button are enabled and stop disabled
      */
    protected void setEnable(boolean b) {
        archiveSearchPanel.setPanelEnabled(b);
        execB.setEnabled(b);
        stopB.setEnabled(!b);
        repaint();
    }
    /**
      *
      */
    protected void setSearchMode(boolean b) {
        searchingOnProject = b;
        projects.setSearchMode(b);
        sbs.setSearchMode(b);
    }
    
    protected void updateSBView(SBLite[] sblites){
        sbs.setRowInfo(sblites, false);
        sbs.selectFirstSB();
    }

    protected void updateProjectView(ProjectLite[] projectLites) {
        projects.setRowInfo(projectLites);
    }

    protected void clearTables() {
        sbs.clear();
        projects.clear();
    }

    protected void setSBStatus(String sb, String status){
        sbs.setSBExecStatus(sb, status);
        if(status.equals("RUNNING")){
            setEnable(false);
        } else {
            setEnable(true);
        }
    }

    protected void updateArrayStatus() {
        arrayStatusDisplay = new JLabel(controller.getArrayStatus());
        arrayStatusDisplay.validate();
    }
    
    private void executeSB(){
        ExecuteSBThread exec = new ExecuteSBThread();
        Thread t = controller.getCS().getThreadFactory().newThread(exec);
        t.start();
    }
    private void stopSB(){
        StopSBThread stop = new StopSBThread();
        Thread t = controller.getCS().getThreadFactory().newThread(stop);
        t.start();
    }

    class ExecuteSBThread implements Runnable {
        public ExecuteSBThread() {
        }
        public void run() {
            try {
                String sbId =sbs.returnSelectedSBId();
                if(!sbId.equals("")){
                    controller.executeSB(sbId);
                    //setEnable(false);
                    //setSBStatus(sbId, "RUNNING");//eventually do this with exec block started event
                }
                //check if a sb has been selected.
            }catch(Exception e){
                e.printStackTrace();
                showErrorPopup(e.toString(), "executeSB");
            }
        }
    }

    class StopSBThread implements Runnable{
        public StopSBThread() {
        }
        public void run() {
            try {
                controller.stopSB();
                //so they can't press stop twice!
                stopB.setEnabled(false);
            } catch(Exception e) {
                e.printStackTrace();
                showErrorPopup(e.toString(), "stopSB");
            }
        }
    }
}
