/*******************************************************************************
 * ALMA - Atacama Large Millimeter Array
 * Copyright (c) AUI - Associated Universities Inc., 2011
 * (in the framework of the ALMA collaboration).
 * All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 *******************************************************************************/
package alma.scheduling.master.gui;

//import alma.scheduling.AlmaScheduling.ALMASchedLogger;
import java.util.logging.Logger;

import javax.swing.JLabel;
import javax.swing.JPanel;

import alma.exec.extension.subsystemplugin.PluginContainerServices;
import alma.exec.extension.subsystemplugin.SubsystemPlugin;

/**
  * A very simple plugin which is launched from the invisible scheduler
  * starter plugin when someone tries to open a scheduler on a tab which
  * has no array associated to it. However this is a generic plugin so it
  * can be used with any error message.
  *
  * @author sslucero
  */
public class SchedulingErrorPlugin extends JPanel implements SubsystemPlugin{

    private PluginContainerServices container;
    //private ALMASchedLogger logger;
    private Logger logger;
    private String error;

    public SchedulingErrorPlugin(String e){
        super();
        error = e;
    }
    public SchedulingErrorPlugin(String e, PluginContainerServices cs){
        this(e);
        container = cs;
        //logger = new ALMASchedLogger(cs.getLogger());
        logger = cs.getLogger();
    }

    public void setServices(PluginContainerServices cs){
        container = cs;
        //logger = new ALMASchedLogger(cs.getLogger());
        logger = cs.getLogger();
    }
    
    public void start(){
        displayError();
    }
    
    public void stop(){
    }
    
    public boolean runRestricted(boolean b){
        return b;
    }
    
    private void displayError(){
        JLabel l = new JLabel(error);
        add(l);
        validate();
    }
}
