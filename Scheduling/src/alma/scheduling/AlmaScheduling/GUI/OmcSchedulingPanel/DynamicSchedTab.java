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
 * File DynamicSchedTab.java
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
import alma.SchedulingExceptions.InvalidOperationEx;
import alma.exec.extension.subsystemplugin.PluginContainerServices;

public class DynamicSchedTab extends SchedulingPanelGeneralPanel implements SchedulerTab {
    private String arrayName;
    private String type;
    private DynamicSchedTabController controller;
    private SBTable sbs;
    private JPanel topPanel;
    private JPanel centerPanel;
    private JPanel bottomPanel;

    /**
      *Tester constructor
      */
    public DynamicSchedTab(String title, String aName){
        type = "dynamic";
        arrayName = aName;
        //schedulerName = title;
        createLayout();
    }

    public DynamicSchedTab(PluginContainerServices cs, String aName){
        super();
        super.onlineSetup(cs);
        arrayName = aName;
        controller = new DynamicSchedTabController(cs, arrayName, this);
        type = "dynamic"; 
       // sbs.setCS(cs);
        controller.setSchedulerName(arrayName+"_"+type);
        createLayout();
    }
    
    private void createLayout() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        createTopPanel();
        createCenterPanel();
        createAndStartDynamicScheduler();
    }

    private void createTopPanel(){
        topPanel = new JPanel();
        JLabel l = new JLabel("Dynamic Scheduling on Array "+arrayName);
        topPanel.add(l);
        add(topPanel);//,BorderLayout.NORTH);
    }
    private void createCenterPanel(){
        centerPanel = new JPanel();
        sbs = new SBTable(true, new Dimension(400,200));
        sbs.setCS(controller.getCS());
        JScrollPane sbPane = new JScrollPane(sbs,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton acceptB = new JButton("Accept");
        acceptB.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    //getSBId that is selected
                    String selectedSB = sbs.returnSelectedSBId();
                    System.out.println("Selected SB = "+selectedSB);
                    //Get SB selected from Table.
                    respondToDS(selectedSB);
                }
        });
        acceptB.setToolTipText("Accept the selected SBs for the dynamic scheduler");
        JButton modifyB = new JButton("Modify");
        modifyB.setToolTipText("Modify the selected list.");
        buttons.add(acceptB);
        buttons.add(modifyB);

        centerPanel.add(sbPane);
        add(centerPanel);
        add(buttons);
    }

    public String getSchedulerType(){
        return type;
    }
    public String getSchedulerName(){
        return controller.getSchedulerName();
    }
    public String getArrayName() {
        return arrayName;
    }
    public void exit(){
        controller.stopDynamicScheduling();
    }


    private void createAndStartDynamicScheduler() {
        try {
            controller.startDynamicScheduling();
        } catch(InvalidOperationEx e) {
            JOptionPane.showMessageDialog(this, "Dynamic Scheduling Didn't start",
                    "Problem starting dynamic scheduling", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void updateTableWithSBList(SBLite[] sblites){
        sbs.clearSelectedItems();
        sbs.setRowInfo(sblites, false);
        validate();
    }

   // public void setSBStatus(String sb, String status) {
     //   sbs.setSBExecStatus(sb,status);
    //}
    
    private void respondToDS(String selectedSB){
        controller.respondToDS(selectedSB);
    }

    public void setEnable(boolean b){
    }
}
