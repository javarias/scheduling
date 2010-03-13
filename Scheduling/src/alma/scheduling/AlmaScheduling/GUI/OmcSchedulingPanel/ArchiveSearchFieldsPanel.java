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
 * File ArchiveSearchFielesPanel.java
 *
 */
package alma.scheduling.AlmaScheduling.GUI.OmcSchedulingPanel;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import alma.entity.xmlbinding.obsproject.types.ControlBlockTArrayRequestedType;
import alma.exec.extension.subsystemplugin.PluginContainerServices;
import alma.scheduling.ProjectLite;
import alma.scheduling.SBLite;

public class ArchiveSearchFieldsPanel extends JPanel {
    private JButton searchB;
    private JLabel searchL;
    private JButton clearB;
    private JCheckBox projectCB;
    private JCheckBox sbCB;
    private JTextField piNameTF;
    private JTextField projNameTF;
    private JComboBox projTypeChoices;
    private JComboBox sbModeNameChoices;
    private JComboBox sbModeTypeChoices;
    private JComboBox arrayType;
    private JTextField expertQueryTF;
    private boolean connectedToALMA;
    private JPanel parent;
    private ArchiveSearchController controller;
    private boolean searchingOnProject;
    private JPanel northPanel;
    private String mode;
    private boolean manualMode;
    
    public ArchiveSearchFieldsPanel(){
        setLayout(new BorderLayout());
        northPanel = new JPanel(new BorderLayout());
        createTextFields();
        createCheckBoxes();//need to do this 2nd coz other fields will be null when used
        createArrayChoice();
        add(northPanel, BorderLayout.NORTH);
        controller = null;
        connectedToALMA= false;
        searchingOnProject=true;
        mode = "searchMode";
    }
    
    public ArchiveSearchFieldsPanel(String arrayMode,boolean manualMode){
        setLayout(new BorderLayout());
        northPanel = new JPanel(new BorderLayout());
        createTextFields();
        createCheckBoxes();//need to do this 2nd coz other fields will be null when used
        createArrayChoice();
        add(northPanel, BorderLayout.NORTH);
        controller = null;
        connectedToALMA= false;
        searchingOnProject=true;
        mode = arrayMode;
        this.manualMode =  manualMode;
    }
    /**
     * design for manual mode Scheduling
     * @param projectNamePrefix The prefix of the projectName for the purpose of search archive 
     * @return String projNameTF.getText() the prefix string of the porjectname 
     */
    public String setProjectNamePrefix(String projectNamePrefix) {
    	projNameTF.setText(projectNamePrefix);
    	return projNameTF.getText();
    }

    public void setCS(PluginContainerServices cs) {
        controller = new ArchiveSearchController(cs);
        setSearchMode(searchingOnProject);
    }
    public void connected(boolean x){
        connectedToALMA=x;
    }
    public void setOwner(JPanel p){
        parent = p;
    }
    private void setSearchMode(boolean m){
        if(parent.getClass().getName().contains("SearchArchiveOnlyPlugin")){
            ((SearchArchiveOnlyPlugin)parent).setSearchMode(m);
        } else if(parent.getClass().getName().contains("InteractiveSchedTab")){
            ((InteractiveSchedTab)parent).setSearchMode(m);
        } else if(parent.getClass().getName().contains("QueuedSchedTab")){
            ((QueuedSchedTab)parent).setSearchMode(m);
        } else if(parent.getClass().getName().contains("ManualArrayTab")){
            ((ManualArrayTab)parent).setSearchMode(m);
        }
    }

    public void setPanelEnabled(boolean b, String label){
        projectCB.setEnabled(b);
        sbCB.setEnabled(b);
        sbModeNameChoices.setEnabled(b);
        sbModeTypeChoices.setEnabled(b);
        projTypeChoices.setEnabled(b);
        projNameTF.setEnabled(b);
        piNameTF.setEnabled(b);
        searchB.setEnabled(b);
        clearB.setEnabled(b);
        searchL.setText(label);
    }

    public void setPanelEnabled(boolean b){
        setPanelEnabled(b, "");
    }

    private void createCheckBoxes(){ 
        JPanel p1 = new JPanel();
        JLabel l = new JLabel("Projects:");
        projectCB = new JCheckBox("",true);
        searchingOnProject = true;
        projectCB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                projectCB.setSelected(true);
                searchingOnProject = true;
                setSearchMode(searchingOnProject);
                if(sbCB.isSelected()){
                    sbCB.setSelected(false);
                } 
            }
        });
        p1.add(l); 
        p1.add(projectCB);
        JPanel p2 = new JPanel();
        l = new JLabel("SBs:");
        sbCB = new JCheckBox("",false);
        sbModeNameChoices.setEnabled(true);
        sbModeTypeChoices.setEnabled(true);
        sbCB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sbCB.setSelected(true);
                searchingOnProject = false;
                setSearchMode(searchingOnProject);
                if(projectCB.isSelected()){
                    projectCB.setSelected(false);
                } 
            }
        });
        p2.add(l);
        p2.add(sbCB);
        JPanel p = new JPanel();
        p.add(p1);
        p.add(p2);
        northPanel.add(p, BorderLayout.WEST);
    }

    private void createTextFields() {
        JLabel sep;
        JLabel l;
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0; c.weighty = 1.0;

        JPanel p = new JPanel(gridbag);
        l = new JLabel("PI Name");
        c.gridwidth =1;
        gridbag.setConstraints(l,c);
        p.add(l);
        piNameTF = new JTextField("*",2);
        c.gridwidth =1;
        gridbag.setConstraints(piNameTF,c);
        p.add(piNameTF);
        ////
        sep = new JLabel();
        c.gridwidth =1;
        gridbag.setConstraints(sep,c);
        p.add(sep);
        ////
        l = new JLabel("SB Mode Name");
        c.gridwidth =1;
        gridbag.setConstraints(l,c);
        p.add(l);
        String[] modeNameChoices = 
            {"All","Single Field Interferometry","Optical Pointing","Tower Holography", "Expert Mode"};
        sbModeNameChoices = new JComboBox(modeNameChoices);
        sbModeNameChoices.setSelectedIndex(0);
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(sbModeNameChoices,c);
        p.add(sbModeNameChoices);
        ////
        l = new JLabel("Project Name");
        c.gridwidth =1;
        gridbag.setConstraints(l,c);
        p.add(l);
        projNameTF = new JTextField("*",3);
        c.gridwidth =1;
        gridbag.setConstraints(projNameTF,c);
        p.add(projNameTF);
        ////
        sep = new JLabel();
        c.gridwidth =1;
        gridbag.setConstraints(sep,c);
        p.add(sep);
        ////
        l = new JLabel("SB Mode Type");
        c.gridwidth =1;
        gridbag.setConstraints(l,c);
        p.add(l);
        //TODO check if OT has this as something i can import
        String[] modeTypeChoices = 
            {"All","Observatory", "User", "Expert"}; 
        sbModeTypeChoices = new JComboBox(modeTypeChoices);
        sbModeTypeChoices.setSelectedIndex(0);
        c.gridwidth =GridBagConstraints.REMAINDER;
        gridbag.setConstraints(sbModeTypeChoices,c);
        p.add(sbModeTypeChoices);
        ////
        l = new JLabel("Project Type");
        c.gridwidth =1;
        gridbag.setConstraints(l,c);
        p.add(l);
        String[] projectTypes = {"All","Continuum","Polarization","Other"};
        projTypeChoices = new JComboBox(projectTypes);
        projTypeChoices.setSelectedIndex(0);
        projTypeChoices.setEnabled(true); 
        c.gridwidth =1;
        gridbag.setConstraints(projTypeChoices,c);
        p.add(projTypeChoices);
        ////
        sep = new JLabel();
        c.gridwidth =1;
        gridbag.setConstraints(sep,c);
        p.add(sep);
        JPanel bp = new JPanel(new GridLayout(1,3));
        searchL = new JLabel("");
        bp.add(searchL);//spacer
        searchB = new JButton("Search");
        searchB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                if(!connectedToALMA){
                    showConnectMessage();
                    return;
                }
                if(controller == null) {
                    showConnectMessage();
                    return;     
                }
            	setPanelEnabled(false, "Searching..."); // reenabled in the update after the result comes back from the archive

                doClearPreviousSearch();
                
                if(mode.equals("searchMode")) {
                	doSearch();
                }
                else if(mode.equals("arrayMode")) {
                	doSearch(manualMode);
                }
            }
        });
        searchB.setToolTipText("Click here to search archive.");
        bp.add(searchB);
        clearB = new JButton("Clear");
        clearB.setToolTipText("Click here to clear search fields.");
        clearB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                if(!connectedToALMA){
                    showConnectMessage();
                    return;
                }
                if(controller == null) {
                    showConnectMessage();
                    return;     
                }
                doClearSearchFields();
            }
        });
        bp.add(clearB);
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(bp,c);
        p.add(bp);
        add(p, BorderLayout.CENTER);
    }
    private void createArrayChoice() {
        JLabel arrayL = new JLabel("Array Type");
        String[] modeTypeChoices = {"All", 
            ControlBlockTArrayRequestedType.ACA.toString(),
            ControlBlockTArrayRequestedType.TWELVE_M.toString(),
            ControlBlockTArrayRequestedType.SEVEN_M.toString(),
            ControlBlockTArrayRequestedType.TP_ARRAY.toString()
        };
        arrayType = new JComboBox(modeTypeChoices);
        arrayType.setSelectedIndex(0);
        JPanel p = new JPanel();
        p.add(arrayL);
        p.add(arrayType);
        northPanel.add(p, BorderLayout.EAST);
    }

    private void doClearSearchFields(){
        piNameTF.setText("*");
        projNameTF.setText("*");
        projTypeChoices.setSelectedIndex(0);
        sbModeNameChoices.setSelectedIndex(0);
        sbModeTypeChoices.setSelectedIndex(0);
    }

    private void doClearPreviousSearch(){
        String name =parent.getClass().getName();
        if(name.contains("SearchArchiveOnlyPlugin")){
            ((SearchArchiveOnlyPlugin)parent).clearTables();
        } else if(name.contains("InteractiveSchedTab")){
            ((InteractiveSchedTab)parent).clearTables();
        } else if(name.contains("QueuedSchedTab")){
            ((QueuedSchedTab)parent).clearTables();
        } else if(name.contains("ManualArrayTab")){
            ((ManualArrayTab)parent).clearTables();
        }
    }

    private void showConnectMessage(){
        JOptionPane.showMessageDialog(this,"System not operational yet.",
                "Not Connected", JOptionPane.ERROR_MESSAGE);
    }
    //////////////////////////////////////
    /// SB Stuff Below
    //////////////////////////////////////
        
    public String makeSBQuery(){
        //access to pi name, sb type, sb mode
        String sbModeType =(String) sbModeTypeChoices.getSelectedItem();
        String sbModeName = (String)sbModeNameChoices.getSelectedItem(); //correspondstypename of mode
        String query;
        if(sbModeName.equals("All") && sbModeType.equals("All") )  {
            query="/*";
        } else if(sbModeType.equals("All") ) {
            //mode == all so serching by type only
            query="/sbl:SchedBlock[sbl:modeName=\""+sbModeName+"\"]";
        } else if(sbModeName.equals("All")) {
            //type == all so serching by mode only
            query="/sbl:SchedBlock[@modeType=\""+sbModeType+"\"]";
        } else {
            //searching with both!
            query = "/sbl:SchedBlock[@modeType=\""+
                        sbModeType+"\" and sbl:modeName=\""+sbModeName+"\"]";
        }
        return query;
    }

    public void displaySBResults(SBLite[] results){
        String name =parent.getClass().getName();
        if(name.contains("SearchArchiveOnlyPlugin")){
            ((SearchArchiveOnlyPlugin)parent).updateSBView(results);
        } else if(name.contains("InteractiveSchedTab")){
            ((InteractiveSchedTab)parent).updateSBView(results);
        } else if(name.contains("QueuedSchedTab")){
            ((QueuedSchedTab)parent).updateSBView(results);
        } else if(name.contains("ManualArrayTab")){
            ((ManualArrayTab)parent).updateSBView(results);
        }
    }
    
    //////////////////////////////////////
    /// Project Stuff Below
    //////////////////////////////////////
    
    public void displayProjectResults(ProjectLite[] results){
        String name =parent.getClass().getName();
        if(name.contains("SearchArchiveOnlyPlugin")){
            ((SearchArchiveOnlyPlugin)parent).updateProjectView(results);
        } else if(name.contains("InteractiveSchedTab")){
            ((InteractiveSchedTab)parent).updateProjectView(results);
        } else if(name.contains("QueuedSchedTab")){
            ((QueuedSchedTab)parent).updateProjectView(results);
        } else if(name.contains("ManualArrayTab")){
            ((ManualArrayTab)parent).updateProjectView(results);
        }
    }

    public void doSearch(boolean manualMode) {
        SPSearchArchiveThread foo = new SPSearchArchiveThread(manualMode);
        Thread t = controller.getCS().getThreadFactory().newThread(foo);
        t.start();
    }

    public void doSearch() {
        SPSearchArchiveThread foo = new SPSearchArchiveThread();
        Thread t = controller.getCS().getThreadFactory().newThread(foo);
        t.start();
    }

    //////////////////////////////////////
    // Search Thread
    //////////////////////////////////////
    class SPSearchArchiveThread implements Runnable {
    	boolean manualMode;
    	boolean searchAllProject = false;
        public SPSearchArchiveThread (boolean manualModeProject){
        	this.manualMode = manualModeProject;
        }
        
        public SPSearchArchiveThread (){
        	this.searchAllProject =true;
        }
        
        public void run(){
        	try {
        		String pi = piNameTF.getText();
        		String pName = projNameTF.getText();
        		String type = (String)projTypeChoices.getSelectedItem();
        		String array = (String)arrayType.getSelectedItem();
        		//if we know its for all SBs ignore it
        		String sbquery = makeSBQuery();
        		//returns a vector, first item will be matching projects
        		//second item will be matching sbs.
        		if(searchAllProject){
        			Vector res = controller.doQuery(sbquery, pName, pi, type, array);
        			javax.swing.SwingUtilities.invokeLater( new UpdateThread(res));
        		} else {
        			Vector res = controller.doQuery(sbquery, pName, pi, type, array,manualMode);
        			javax.swing.SwingUtilities.invokeLater( new UpdateThread(res));
        		}
        	} finally {
    			javax.swing.SwingUtilities.invokeLater(new ReenableThread());
        	}
        }
    }
    class UpdateThread implements Runnable {
    	private Vector res;
    	public UpdateThread (Vector v) {
    		res = v;
    	}
    	public void run() {
    		if(projectCB.isSelected() ){
    			displayProjectResults((ProjectLite[])res.elementAt(0));
    		} else if(sbCB.isSelected()){
    			displaySBResults((SBLite[])res.elementAt(1));
    		} 
    		if(parent.getClass().getName().contains("Interactive")){
    			((InteractiveSchedTab)parent).selectFirstResult();
    		} else if(parent.getClass().getName().contains("Queued")){
    			((QueuedSchedTab)parent).selectFirstResult();
    		}
    	}
    }
    // Thread which reenables the panel after some asynchronous work.
    class ReenableThread implements Runnable {
    	public void run() {
    		setPanelEnabled(true);
    	}
    }
}
