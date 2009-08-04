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
 * File ManualArrayTab.java
 */
package alma.scheduling.AlmaScheduling.GUI.OmcSchedulingPanel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.TitledBorder;

import alma.exec.extension.subsystemplugin.PluginContainerServices;
import alma.scheduling.ProjectLite;
import alma.scheduling.SBLite;


public class ManualArrayTab extends SchedulingPanelGeneralPanel implements SchedulerTab {
    //private String schedulerName;
    private String arrayName;
    private String type;
    private String title;
    private ManualArrayTabController controller;
    private JPanel mainPanel;
    private JPanel topPanel;
    private JPanel middlePanel;
    private JButton setupManualArrayB;
    private JButton destroyArrayB;
    private JLabel arrayStatusDisplay;
    private ArchiveSearchFieldsPanel archiveSearchPanel;
    private SBTable sbs;
    private ProjectTable projects; 
    private boolean searchingOnProject; 

    public ManualArrayTab(PluginContainerServices cs, String aName){
        super();
        super.onlineSetup(cs);
        searchingOnProject=true;
        arrayName = aName;
        controller = new ManualArrayTabController(cs, arrayName, this);
        controller.setArrayInUse(aName);
        type = "manual"; 
        title = arrayName+" (Manual)";
        setTitle(title);
        createLayout();
        archiveSearchPanel.setCS(container);
        projects.setCS(container);
        sbs.setCS(container);
        setEnable(true);
        doArchiveSearch();
    }
    
    protected void doArchiveSearch() {
    	//archiveSearchPanel.setProjectNamePrefix("*");
    	boolean manualMode = true;
        archiveSearchPanel.doSearch(manualMode);
    }
    
    protected void selectFirstResult(){
        projects.showFirstProject();
      
    }
    
    private void createLayout(){
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new TitledBorder("Manual Array"));
        createTopPanel();
        createMiddlePanel();
        mainPanel.add(topPanel,BorderLayout.NORTH);
        mainPanel.add(middlePanel,BorderLayout.CENTER);
        Dimension d = mainPanel.getPreferredSize();
        mainPanel.setMaximumSize(d);
        mainPanel.setMinimumSize(d);
        add(mainPanel);
    }

    public String getTitle() {
        return title;
    }

    private void createTopPanel(){
        topPanel = new JPanel(new BorderLayout());
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JLabel arrayStatusL = new JLabel("Array Status =");
        arrayStatusDisplay = new JLabel(controller.getArrayStatus());
        createArchivePanel();
        p.add(arrayStatusL);
        p.add(arrayStatusDisplay);
        topPanel.add(p,BorderLayout.NORTH);
        topPanel.add(archiveSearchPanel,BorderLayout.CENTER);
    }
    
    /**
     * Top panel contains check boxes for determining if we
     * search by project or by sb.
     */
   private void createArchivePanel() {
       archiveSearchPanel = new ArchiveSearchFieldsPanel("arrayMode",true);
       //archiveSearchPanel.setProjectNamePrefix(ProjectNamePrefix_);
       archiveSearchPanel.setOwner(this);
       archiveSearchPanel.connected(true);
       
   }
   
    /**
      * Middle panel contains he search text boxes and the buttons.
      */
    private void createMiddlePanel() {
        middlePanel = new JPanel(new GridLayout(2,2));
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
        
        setupManualArrayB = new JButton("Setup Manual Mode");
        setupManualArrayB.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e){
                    //doCreateConsoleButton();
                    doSetupManualModeButton();
                }
        });
        
        destroyArrayB = new JButton("Destroy Array");
        destroyArrayB.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e){
                    doDestroyButton();
                }
        });
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER,0,0));   
        buttons.add(setupManualArrayB);
        buttons.add(destroyArrayB);
        sbPanel.add(buttons, BorderLayout.SOUTH);
        middlePanel.add(sbPanel);
        
        middlePanel.add(projects.getProjectInfoView());

        //second row: right hand cell = sb info textarea
         middlePanel.add(sbs.getSBInfoView());
    }

    private void doCreateConsoleButton(){
        CreateCCLConsoleThread c = new CreateCCLConsoleThread();
        Thread t  = container.getThreadFactory().newThread(c);
        t.start();
    }
    
    private void doSetupManualModeButton() {
    	SetupManualModeThread c = new SetupManualModeThread();
        Thread t  = container.getThreadFactory().newThread(c);
        t.start();
    }

    private void doDestroyButton(){
        DestroyArrayThread d = new DestroyArrayThread();
        Thread t  = container.getThreadFactory().newThread(d);
        t.start();
    }

    /**
      * If true then search fields & execute button are enabled and stop disabled
      */
    public void setEnable(boolean b) {
        destroyArrayB.setEnabled(b);
        setupManualArrayB.setEnabled(b);
        archiveSearchPanel.setPanelEnabled(b);
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
    
    protected void updateSBInfo(String id) {
        sbs.showSelectedSBDetails(id);
    }

    
    protected void clearTables() {
        sbs.clear();
        projects.clear();
    }
  
    /**
     * need to check later ....
     */
    public String getSchedulerName(){
        return "No scheduler";
        //return controller.getSchedulerName();
    }
    
    public String getArrayName() {
        return arrayName;
    }
    
    public String getSchedulerType(){
        return type;
    }
    
    public void exit(){
    }
    
    public void closeExecutionWaitingThing(){

        Component[] comps1 = middlePanel.getComponents();
        //sbPanel
        Component[] comps2 = ((JPanel)comps1[1]).getComponents();
        JPanel buttons = (JPanel)comps2[1];
        buttons.removeAll();
        buttons.add(setupManualArrayB);
        buttons.add(destroyArrayB);
        setEnabled(false);
        buttons.revalidate();
        validate();

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
    
    protected void updateProjectView(ProjectLite[] projectLites) {
    	/*
    	Vector<ProjectLite> tempProjectLiteVector = new Vector<ProjectLite>();
    	ProjectLite[] tempProjectLite;
    	ProjectLite temprow;
    	for(int i=0;i<projectLites.length;i++){
    		temprow = projectLites[i];
    		if(temprow.projectName.startsWith(ProjectNamePrefix_)){
    			tempProjectLiteVector.add(temprow);
    		}
    	}
    	tempProjectLite = new ProjectLite[tempProjectLiteVector.size()];
    	tempProjectLite = tempProjectLiteVector.toArray(tempProjectLite);
        //projects.setRowInfo(projectLites);
    	projects.setRowInfo(tempProjectLite);
    	archiveSearchPanel.setProjectNamePrefix(ProjectNamePrefix_);
    	*/
    	projects.setRowInfo(projectLites);
    }
    
    
////////////////////////////////////
    public void start() throws Exception {
     //   super.start();
        validate();
    }
        
    public void stop() throws Exception {
        super.stop();
        exit();
    }
    
    protected void setSBStatus(String sb, String status){
        sbs.setSBExecStatus(sb, status);
        if(status.equals("RUNNING")){
            setEnable(false);
        } else {
            setEnable(true);
        }
    }
    

    class CreateCCLConsoleThread implements Runnable {
        public CreateCCLConsoleThread (){
        }
        public void run() {
            if( controller.createConsolePlugin()) {
		//always keep createConsoleB enable 
                //createConsoleB.setEnabled(false);
            } else {
                //inform error happened
            }
        }
    }
    
    class SetupManualModeThread implements Runnable {
        public SetupManualModeThread (){
        }
        public void run() {
        	 try {
                 String sbId =sbs.returnSelectedSBId();
                 if(sbId.equals("You can only execute one at a time")){
                     showErrorPopup(sbId, "executeSB");
                     return;
                 } else if (sbId.equals( "You must selected one SB!")){
                     showErrorPopup(sbId, "executeSB");
                     return;
                 }

                 controller.setupManualArrayConfigure(sbId);
                 //check if a sb has been selected.
        	 }catch(Exception e){
                 e.printStackTrace();
                 logger.severe("SCHEDULING_PANEL: Error starting a SB");
                 showErrorPopup(e.toString()+", "+e.getMessage(), "executeSB");
                 
             }
		
        }
    }
    
    class DestroyArrayThread implements Runnable{
        public DestroyArrayThread() {
        }
        public void run() {
            try {
                controller.destroyArray();
                //so they can't press it twice!
                destroyArrayB.setEnabled(false);
            } catch(Exception e) {
                e.printStackTrace();
                showErrorPopup(e.toString(), "destroy array");
            }
        }
    }
}
