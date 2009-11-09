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
 *File SearchArchiveOnlyPlugin.java
 */
package alma.scheduling.AlmaScheduling.GUI.OmcSchedulingPanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.TitledBorder;

import alma.exec.extension.subsystemplugin.PluginContainerServices;
import alma.scheduling.ProjectLite;
import alma.scheduling.SBLite;

public class SearchArchiveOnlyPlugin extends SchedulingPanelGeneralPanel {
    private JPanel mainPanel;
    private ArchiveSearchFieldsPanel archiveSearchPanel;
    private JPanel middlePanel;
    private SBTable sbs;
    private ProjectTable projects;
    private boolean connectedToALMA;
    private boolean searchingOnProject;
    private SearchArchiveOnlyController controller;

    public SearchArchiveOnlyPlugin(){
        super();
       // add(bottomPanel,BorderLayout.SOUTH);
        createLayout();
        connectedToALMA=false;
        controller = new SearchArchiveOnlyController(this);
    }

    public void setServices(PluginContainerServices cs){
        super.setServices(cs);
        logger.fine("### setServices in SearchArchive Plugin ###");
        connectedSetup(cs);
    }

    public void start() {
    }

    public void connectedSetup(PluginContainerServices cs){
        super.onlineSetup(cs);
        controller.setup(cs);
        controller.checkOperationalState();
        archiveSearchPanel.setCS(cs);
        projects.setCS(cs);
        sbs.setCS(cs);
    }
    
    /**
      *
      */
    public void setSearchMode(boolean b) {
        searchingOnProject = b;
        projects.setSearchMode(b);
        sbs.setSearchMode(b);
    }

    public void connectToALMA(boolean x) {
        connectedToALMA=x;
        archiveSearchPanel.connected(connectedToALMA);
    }

    private void createLayout(){
        mainPanel = new JPanel(new GridBagLayout());
        //mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(new TitledBorder("Search Archive"));
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        
        createTopPanel();
        createMiddlePanel();
        gridBagConstraints.weightx = 0.2;
        gridBagConstraints.weighty = 0.2;
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
       
        mainPanel.add(archiveSearchPanel,gridBagConstraints );
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.anchor = GridBagConstraints.CENTER;
        mainPanel.add(middlePanel,gridBagConstraints );
        Dimension d = mainPanel.getPreferredSize();
        //mainPanel.setMaximumSize(d);
        this.setLayout(new GridBagLayout());
        mainPanel.setMinimumSize(d);
        add(mainPanel, gridBagConstraints);
    }
    /**
      * Top panel contains check boxes for determining if we
      * search by project or by sb.
      */
    private void createTopPanel() {
        archiveSearchPanel = new ArchiveSearchFieldsPanel();
        archiveSearchPanel.setOwner(this);
        archiveSearchPanel.connected(connectedToALMA);
    }

    /**
      * Middle panel contains he search text boxes and the buttons.
      */
    private void createMiddlePanel() {
        middlePanel = new JPanel(new GridLayout(2,2));
        //first row: left hand cell = project table
        Dimension size = new Dimension(200,150);
        projects = new ProjectTable(size);
        projects.setOwner(this);
        JScrollPane projectPane = new JScrollPane(projects,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, 
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        projectPane.setBorder(new TitledBorder("Projects Found"));
        projectPane.setPreferredSize(size);
        middlePanel.add(projectPane);

        //first row: right hand cell = sb table
        sbs = new SBTable(false, size);
        sbs.setOwner(this);
        JScrollPane sbPane = new JScrollPane(sbs,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        sbPane.setBorder(new TitledBorder("SBs Found"));
        sbPane.setPreferredSize(size);
                                   
        middlePanel.add(sbPane);
        
        //second row: left hand cell = project info
        middlePanel.add(projects.getProjectInfoView());
        //second row: right hand cell = sb info
        middlePanel.add(sbs.getSBInfoView());
        
    }

    public void updateSBView(SBLite[] sblites){
        sbs.setRowInfo(sblites, false);
    }

    public void updateProjectView(ProjectLite[] projectLites) {
        projects.setRowInfo(projectLites);
    }

    public void clearTables() {
        sbs.clear();
        projects.clear();
    }
    protected boolean areWeConnected(){
        return controller.areWeConnected();
    }
}
