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
 * file SchedulingPanelGeneralPanel.java
 */
package alma.scheduling.AlmaScheduling.GUI.OmcSchedulingPanel;

import javax.swing.*;
import java.awt.Dimension;
import java.util.logging.Logger;
import alma.exec.extension.subsystemplugin.PluginContainerServices;

public class SchedulingPanelGeneralPanel extends JPanel{
    protected PluginContainerServices container;
    protected Logger logger;

    public SchedulingPanelGeneralPanel(){
        super();
    }
    public void onlineSetup(PluginContainerServices cs){
        container = cs;
        logger = cs.getLogger();
    }

    public void setMaxSize(Dimension d){ 
//        System.out.println("MAX Size should be: "+d.toString());
        setMaximumSize(d);
    }

    public void showErrorPopup(String error,String method) {
        JOptionPane.showMessageDialog(this, error, method, JOptionPane.ERROR_MESSAGE);
    }
    public void showWarningPopup(String warning, String method) {
        JOptionPane.showMessageDialog(this, warning, method, JOptionPane.WARNING_MESSAGE);
    }

}
