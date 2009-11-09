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
 * File ExistingArraysTab.java
 */
package alma.scheduling.AlmaScheduling.GUI.OmcSchedulingPanel;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.TitledBorder;

import alma.exec.extension.subsystemplugin.PluginContainerServices;

public class ExistingArraysTab extends SchedulingPanelGeneralPanel {
    private ExistingArraysTabController controller;
    private JPanel topPanel, centerPanel, bottomPanel;
    private ArrayTable table;
    /**
      *Tester constructor
      */
    public ExistingArraysTab() {
        super();
        createLayout();
    }

    public void connectedSetup(PluginContainerServices cs){
        super.onlineSetup(cs);
        controller = new ExistingArraysTabController(cs, this);
        table.setCS(cs);
        checkCurrentExistingArrays();
    }

    private void checkCurrentExistingArrays() {
        CheckExistingArrays foo  = new CheckExistingArrays();
        Thread t = controller.getCS().getThreadFactory().newThread(foo);
        t.start();
    }

    
    private void createLayout() {
        setBorder(new TitledBorder("Existing Arrays"));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        createTopPanel();
        createCenterPanel();
        createBottomPanel();
    }

    private void createTopPanel(){
        topPanel = new JPanel();
        add(topPanel);//,BorderLayout.NORTH);
    }
    private void createCenterPanel(){
        table = new ArrayTable(new Dimension(300,200));
        table.setOwner(this);
        JScrollPane pane = new JScrollPane(table,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        centerPanel = new JPanel();
        centerPanel.add(pane);
        add(centerPanel, BorderLayout.CENTER);
    }
    
    private void createBottomPanel() {
        bottomPanel = new JPanel();
        //JLabel foo = new JLabel("NOTE: Manual Arrays are not currently displayed here!");
        //bottomPanel.add(foo);
        //add(bottomPanel, BorderLayout.SOUTH);
    }

    public void exit(){
    }

    public void setEnable(boolean b){
    }
    private String updateArrayName;
    private String updateArrayType;
        
    protected void addArray(String arrayname, String arraytype) {
        updateArrayName = arrayname;
        updateArrayType = arraytype;
        javax.swing.SwingUtilities.invokeLater(new Runnable(){
                public void run(){
                    table.addArray(updateArrayName, updateArrayType);
                }
        });
        repaint();
        validate();
    }

    protected void removeArray(String arrayname){
        table.removeArray(arrayname);
        repaint();
        validate();
    }

    protected void openSchedulerForArray(String array) {
        //do later when its ok to make an ICD change
    }


    class CheckExistingArrays implements Runnable{
        private String[] m_arrays;
        private String[] a_arrays;
        private int i;

        public CheckExistingArrays(){ }
        public void run() {
            a_arrays = controller.getCurrentAutomaticArrays();
            m_arrays = controller.getCurrentManualArrays();
            for(i=0; i < a_arrays.length; i++){
                javax.swing.SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            addArray(a_arrays[i], "automatic");
                        }
                });
            }
            for(i=0; i < m_arrays.length; i++){
                javax.swing.SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            addArray(m_arrays[i], "manual");
                        }
                });
                    
            }
        }
    }
}
