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

public class ManualArrayTab extends SchedulingPanelGeneralPanel implements SchedulerTab {
    //private String schedulerName;
    private String arrayName;
    private String type;
    private String title;
    private ManualArrayTabController controller;
    private JPanel middlePanel;
    private JButton createConsoleB;
    private JButton destroyB;
    private JLabel arrayStatusDisplay;

    public ManualArrayTab(PluginContainerServices cs, String aName){
        super();
        super.onlineSetup(cs);
        arrayName = aName;
        controller = new ManualArrayTabController(cs, arrayName, this);
        controller.setArrayInUse(aName);
        type = "manual"; 
        title = arrayName+" (Manual)";
        setTitle(title);
        createLayout();
        setEnable(true);
    }
    
    private void createLayout(){
        setBorder(new TitledBorder("Manual Array"));
        setLayout(new BorderLayout());
        createMiddlePanel();
        Dimension d = getPreferredSize();
        add(middlePanel,BorderLayout.CENTER);
    }
    public String getTitle() {
        return title;
    }

    /**
      * Middle panel contains he search text boxes and the buttons.
      */
    public void createMiddlePanel() {
        middlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        createConsoleB = new JButton("Create CCL Console");
        createConsoleB.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e){
                    doCreateConsoleButton();
                }
        });
        destroyB = new JButton("Destroy Array");
        destroyB.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e){
                    doDestroyButton();
                }
        });
        middlePanel.add(createConsoleB);
        middlePanel.add(destroyB);
        JLabel arrayStatusL = new JLabel("Array Status =");
        arrayStatusDisplay = new JLabel(controller.getArrayStatus());
    }

    private void doCreateConsoleButton(){
        CreateCCLConsoleThread c = new CreateCCLConsoleThread();
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
        destroyB.setEnabled(b);
        createConsoleB.setEnabled(b);
        repaint();
    }

    public String getSchedulerName(){
        return "No scheduler";
    }
    
    public String getArrayName() {
        return arrayName;
    }
    
    public String getSchedulerType(){
        return type;
    }
    
    public void exit(){
    }
  
    protected void updateArrayStatus() {
        arrayStatusDisplay.setText(controller.getArrayStatus());
        arrayStatusDisplay.validate();
    }
        
    public void stop() throws Exception {
        super.stop();
        exit();
    }

    class CreateCCLConsoleThread implements Runnable {
        public CreateCCLConsoleThread (){
        }
        public void run() {
            if( controller.createConsolePlugin()) {
                createConsoleB.setEnabled(false);
            } else {
                //inform error happened
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
                destroyB.setEnabled(false);
            } catch(Exception e) {
                e.printStackTrace();
                showErrorPopup(e.toString(), "destroy array");
            }
        }
    }
}
