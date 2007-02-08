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
 * File ProjectTableController.java
 */
package alma.scheduling.AlmaScheduling.GUI.OmcSchedulingPanel;

import alma.exec.extension.subsystemplugin.PluginContainerServices;
import alma.scheduling.SBLite;
import alma.scheduling.ProjectLite;
import alma.scheduling.MasterSchedulerIF;
import java.util.logging.Logger;


public class ProjectTableController extends SchedulingPanelController {

    public ProjectTableController(PluginContainerServices cs){
        super(cs);
    }

    public SBLite[] getProjectSBs(String projectId){
        SBLite[] sbs=null;
        getMSRef();
        releaseMSRef();
        return sbs;
    }

    public ProjectLite getProjectLite(String uid) {
        getMSRef();
        String[] id = new String[1];
        id[0] = uid;
        ProjectLite[] p = masterScheduler.getProjectLites(id);
        releaseMSRef();
        return p[0];
    }
    public SBLite[] getSBLites(String[] uids){
        getMSRef();
        SBLite[] sbs = masterScheduler.getSBLite(uids);
        releaseMSRef();
        return sbs;
    }
}
