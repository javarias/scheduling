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
package alma.scheduling.AlmaScheduling.GUI.OmcSchedulingPanel;

//java stuff
import java.awt.event.*;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.BorderLayout;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.util.logging.Logger;
//exec plugin stuff
import alma.exec.extension.subsystemplugin.*;

public class SchedulingPanelMainFrame extends JPanel implements SubsystemPlugin {
    private PluginContainerServices cs;
    private JPanel main;
    private JScrollPane pane;
    private MainSchedTabPane mainSchedPanel;
    private Logger logger;
    
    public SchedulingPanelMainFrame(){
        createMainSchedPanel();
        main = new JPanel();
        Dimension d = getPreferredSize();
        main.setSize(d.width + 5, d.height + 5);
        /*
        pane = new JScrollPane(mainSchedPanel);
        main.add(pane);
        */
        main.add(mainSchedPanel);
        add(main);
        setVisible(true);
    }

    public void setServices(PluginContainerServices ctrl) {
        cs = ctrl;
        logger = ctrl.getLogger();
        mainSchedPanel.secondSetup(cs);
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
        logger.info("Calling exit in ExecFrameForSchedulingPanel");
        mainSchedPanel.exit();
    }

    private void createMainSchedPanel() {
        mainSchedPanel = new MainSchedTabPane(this);
    }
}
