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
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, * MA 02111-1307  USA
 * 
 * File OpenOT.java
 * 
 */
package alma.scheduling.GUI.InteractiveSchedGUI;


import alma.acs.container.ContainerServices;
/**
 * This Class starts the observing tool from inside the scheduling
 * system so that users can manipulate the scheduling blocks
 * associated with their interactive project.
 *
 * @author Sohaila Lucero
 */
public class OpenOT implements Runnable {
    private String id;
    private ContainerServices container;

    public OpenOT(String projectID, ContainerServices cs) {
        id = projectID;
        container = cs;
    }
    public void run() {
        
        /*
        try {
            System.out.println("Starting ALMA-OT");
            Runtime runtime = Runtime.getRuntime();
            //otProcess = runtime.exec("ALMA-OT");
            if(id == null) {
                otProcess = runtime.exec("java alma.obsprep.ot.gui.toplevel.ObservingTool");
            } else {
                otProcess = runtime.exec("java alma.obsprep.ot.gui.toplevel.ObservingTool \"-x\" \"-r\" "+id);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }*/
       //alma.obsprep.ot.gui.toplevel.ObservingTool.main(new String[] {"-x","-r",id});
       // AF: 2005-10-18 <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
       //alma.obsprep.ot.gui.toplevel.ObservingToolEmbedded.main(
       //        new String[] {"-x","-r",id}, container);
    }
    
}
