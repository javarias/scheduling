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
 *File SearchArchiveOnlyTab.java
 */
package alma.scheduling.AlmaScheduling.GUI.OmcSchedulingPanel;

import java.awt.*;
import java.util.EventListener;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.util.logging.Logger;

import alma.scheduling.SBLite;
import alma.scheduling.ProjectLite;
import alma.exec.extension.subsystemplugin.PluginContainerServices;

public class SearchArchiveOnlyTab extends SchedulingPanelGeneralPanel {
    //private PluginContainerServices container;
    //private Logger logger;
    private ArchiveSearchFieldsPanel archiveSearchPanel;
    private JPanel middlePanel;
    //private JPanel bottomPanel;
    private SBTable sbs;
    private ProjectTable projects;
    private boolean connectedToALMA;
    private boolean searchingOnProject;

    public SearchArchiveOnlyTab(){
        super();
        setBorder(new TitledBorder("Search Archive"));
        setLayout(new BorderLayout());
        createTopPanel();
        createMiddlePanel();
        Dimension d = getPreferredSize();
        add(archiveSearchPanel,BorderLayout.NORTH);
        add(middlePanel,BorderLayout.CENTER);
       // add(bottomPanel,BorderLayout.SOUTH);
        connectedToALMA=false;
    }

    public void connectedSetup(PluginContainerServices cs){
     //   container = cs;
     //   logger = cs.getLogger();
        super.onlineSetup(cs);
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

    /**
      * Top panel contains check boxes for determining if we
      * search by project or by sb.
      */
    public void createTopPanel() {
        archiveSearchPanel = new ArchiveSearchFieldsPanel();
        archiveSearchPanel.setOwner(this);
        archiveSearchPanel.connected(connectedToALMA);
    }

    /**
      * Middle panel contains he search text boxes and the buttons.
      */
    public void createMiddlePanel() {
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
}
