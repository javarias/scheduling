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
 * file SBTableController.java
 */
package alma.scheduling.AlmaScheduling.GUI.OmcSchedulingPanel;

import alma.exec.extension.subsystemplugin.PluginContainerServices;
import alma.scheduling.ProjectLite;
import alma.scheduling.SBLite;

public class SBTableController extends SchedulingPanelController {

    public SBTableController(PluginContainerServices cs){
        super(cs);
    }

    /**
      * Get SBLite for given sb id
      * @param id SB Id
      * @return SBLite 
      */
    public SBLite getSBLite(String id){
        getMSRef();
        String[] uid = new String[1];
        uid[0] = id;
        SBLite[] sb = masterScheduler.getSBLite(uid);
        releaseMSRef();
        return sb[0];
    }

    /**
      * Get ProjectLite for the project id 
      * @param id Project Id
      * @return ProjectLite[] Will only be one 
      */
    public ProjectLite[] getProjectLite(String id){
        getMSRef();
        String[] uid = new String[1];
        uid[0] = id;
        ProjectLite[] p = masterScheduler.getProjectLites(uid);
        releaseMSRef();
        return p;
    }
}
