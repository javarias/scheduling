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
 * File SchedulingPanelMainFrame.java
 */
package alma.scheduling.master.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.logging.Logger;

import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;

import alma.exec.extension.subsystemplugin.PluginContainerServices;
import alma.exec.extension.subsystemplugin.SubsystemPlugin;

//import alma.scheduling.AlmaScheduling.ALMASchedLogger;

public class SchedulingPanelMainFrame extends JPanel implements SubsystemPlugin {
    private PluginContainerServices cs;
    private JPanel main;
    private JScrollPane pane;
    private MainSchedTabPane mainSchedPanel;
    private JProgressBar progressBar;
    private Logger logger;
 
    public SchedulingPanelMainFrame(){
        createMainSchedPanel();
        main = new JPanel();
        GridBagConstraints gridBagConstraints= new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
	    runInitialProgressMonitor();
	    main.setLayout(new GridBagLayout());
	    main.add(progressBar);
	    this.setLayout(new GridBagLayout());
        this.add(main);
        
    }

    public void setServices(PluginContainerServices ctrl) {
        cs = ctrl;
        logger = cs.getLogger();
        mainSchedPanel.secondSetup(cs);
        logger.fine("### setServices in CreateArray Plugin ###");
        main.removeAll();
        this.removeAll();
        GridBagConstraints gridBagConstraints= new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        main.add(mainSchedPanel,gridBagConstraints);
        this.add(main,gridBagConstraints);
	    this.revalidate();
	    this.repaint();
        setVisible(true);
    }

    public void start() throws Exception {
    }

    public void stop() throws Exception{
        exit();
    }
    public boolean runRestricted(boolean b) throws Exception {
        return b;
    }

    public void exit(){
        logger.fine("Calling exit in ExecFrameForSchedulingPanel");
        mainSchedPanel.exit();
    }

    private void createMainSchedPanel() {
        mainSchedPanel = new MainSchedTabPane(this);
    }
    
    private void runInitialProgressMonitor() {
	
    progressBar = new JProgressBar();
	progressBar.setIndeterminate(true);
	progressBar.setVisible(true);
	progressBar.setStringPainted(true);
	progressBar.setString("Reading antennas information");
    }
}
