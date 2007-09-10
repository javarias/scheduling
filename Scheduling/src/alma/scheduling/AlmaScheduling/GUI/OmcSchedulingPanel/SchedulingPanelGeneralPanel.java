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
import alma.exec.extension.subsystemplugin.*;
import alma.scheduling.MasterSchedulerIF;
import alma.scheduling.ArrayModeEnum;
import alma.SchedulingExceptions.InvalidOperationEx;
import alma.scheduling.AlmaScheduling.ALMASchedLogger;

public class SchedulingPanelGeneralPanel extends JPanel implements SubsystemPlugin {
    protected PluginContainerServices container;
    protected ALMASchedLogger logger;
    protected String title;
    private MasterSchedulerIF ms;
    private String arrayname;
    private ArrayModeEnum mode;

    public SchedulingPanelGeneralPanel(){
        super();
        ms = null;
        arrayname = null;
        mode = null;
    }
    public void onlineSetup(PluginContainerServices cs){
        container = cs;
        logger = new ALMASchedLogger(cs.getLogger());
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String t){
        title = t;
    }

    public void setMaxSize(Dimension d){ 
        setMaximumSize(d);
    }

    public void showErrorPopup(String error,String method) {
        JOptionPane.showMessageDialog(this, error, method, JOptionPane.ERROR_MESSAGE);
    }
    public void showWarningPopup(String warning, String method) {
        JOptionPane.showMessageDialog(this, warning, method, JOptionPane.WARNING_MESSAGE);
    }

    //plugin stuff

    public void setServices(PluginContainerServices cs) {
        container = cs;
        logger = new ALMASchedLogger(cs.getLogger());
    }

    public void start() throws Exception {
         //Get Array name and start scheduler
        getArrayName();
        if(arrayname == null || arrayname.equals("") || arrayname.equals("Unassigned")) {
            createErrorPlugin();
        } else {
            createSchedulerPlugin();
        }
    }

    public void stop() throws Exception{
      //  exit();
    }
    public boolean runRestricted(boolean b) throws Exception {
        return b;
    }

    ///////////////////////
    /**
      * Get master scheduler component
      */
    private void getMSRef(){
        try {
            if(ms == null) {
                ms = alma.scheduling.MasterSchedulerIFHelper.narrow(
                    container.getComponentNonSticky("SCHEDULING_MASTERSCHEDULER"));
                logger.fine("SP: Got master scheduler reference");
            }
        } catch(Exception e){
            logger.severe("SP: Error getting master scheduler reference");
            e.printStackTrace();
        }
    }
    /**
      * Release master scheduler component
      */
    private void releaseMSRef(){
        try {
            if(ms != null){
                container.releaseComponent(ms.name());
                logger.fine("SP: Released MS.");
            }
        } catch(Exception e){
            logger.severe("SP: Error releasing MS: "+e.toString());
            e.printStackTrace();
        }
    }
    
    /**
      * Get array name from arraytab/container
      */
    private void getArrayName() {
        arrayname = container.getSessionProperties().getProperty("array.name");
        logger.fine("SP: ArrayName = "+arrayname);
    }
    /**
      * Gets the mode of the array name we got prior to calling this method
      * and opens the appropriate scheduler for it.
      */ 
    private void createSchedulerPlugin() {
        getMSRef();
        try {
            mode = ms.getSchedulerModeForArray(arrayname);
            OpenSchedulerTab o = new OpenSchedulerTab(mode, arrayname, container);
            Thread t = container.getThreadFactory().newThread(o);
            t.start();
        }catch(InvalidOperationEx e){
            logger.severe("SP: Unable to get scheduler mode for array "+arrayname);
            createErrorPlugin();
            e.printStackTrace();
        }
        releaseMSRef();
    }    

    /**
      * Creates a plugin that says you can't open a scheduler on a tab not associated 
      * with an array
      */
    private void createErrorPlugin(){
        String error = "Cannot open a scheduler for this tab, array ("+arrayname+") is invalid!";
        try {
            OpenErrorPlugin p = new OpenErrorPlugin(container, error);
            Thread t = container.getThreadFactory().newThread(p);
            t.start();
        }catch(Exception e){
            logger.severe("SP: Error opening error plugin! Error was "+error);
            e.printStackTrace();
        }
        
    }
    
    private void addSchedulerToView(SchedulerTab s){
        try {
            removeAll();
            add((JPanel)s);
            validate();
        }catch (Exception e){
            logger.severe("SP: Error changing view to scheduler mode "+mode.toString());
        }
    }

    private void addErrorMessageToView(SchedulingErrorPlugin p){
        try {
            removeAll();
            p.start();
            add((JPanel)p);
            p.validate();
            validate();
        }catch (Exception e){
            logger.severe("SP: Error opening error message which basically said "+
                    "Cannot open scheduler in this array tab");
        }
    }
    //////////////////////////

    /**
      * thread class to open the scheduler plugin
      */
    class OpenSchedulerTab implements Runnable {
        private PluginContainerServices container;
        private ArrayModeEnum mode;
        private String array;
        public OpenSchedulerTab(ArrayModeEnum m, String arrayName, PluginContainerServices cs) {
            container = cs;
            mode =m;
            array = arrayName;
        }
        public void run() {
            SchedulerTab tab=null;
            if(mode == ArrayModeEnum.DYNAMIC) {
                tab = new DynamicSchedTab(container,array);
            } else if(mode == ArrayModeEnum.INTERACTIVE){
                tab = new InteractiveSchedTab(container, array);
            } else if(mode == ArrayModeEnum.QUEUED){
                tab = new QueuedSchedTab(container, array);
            } else if(mode == ArrayModeEnum.MANUAL){
                tab = new ManualArrayTab(container,array);
            }
            try {
                //container.startChildPlugin(tab.getTitle(), (SubsystemPlugin)tab);
                addSchedulerToView(tab);
            }catch(Exception e){
                logger.severe("SP: unable to start schduler plugin for array "+array);
                e.printStackTrace();
            }
        }
    }
    /**
      * thread class to open the error plugin
      *
      */
    class OpenErrorPlugin implements Runnable {
        private PluginContainerServices container;
        private String errMsg;
        
        public OpenErrorPlugin(PluginContainerServices cs, String error){
            container = cs;
            errMsg = error;
        }

        public void run(){
            SchedulingErrorPlugin p = new SchedulingErrorPlugin(errMsg, container);
            try {
               // container.startChildPlugin("Cannot Open Scheduler", p);
                addErrorMessageToView(p);
            }catch(Exception e){
                logger.severe("SP: unable to start error plugin for error "+errMsg);
                e.printStackTrace();
            }
            
        }
    }
}
