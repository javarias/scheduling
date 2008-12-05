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
 * File ArchiveSearchController.java
 */

package alma.scheduling.AlmaScheduling.GUI.OmcSchedulingPanel;

import java.util.Vector;
import java.util.logging.Logger;
import alma.exec.extension.subsystemplugin.PluginContainerServices;
import alma.scheduling.SBLite;
import alma.scheduling.ProjectLite;
import alma.scheduling.MasterSchedulerIF;

public class ArchiveSearchController extends SchedulingPanelController {
    
    public ArchiveSearchController(PluginContainerServices cs) {
        super(cs);
    }


    public Vector doQuery(String sbQuery, String pName, String piName, 
            String pType, String aType) {
        Vector tmp = new Vector();
        try {
            getMSRef();
            String[] sbs = masterScheduler.queryArchive(sbQuery,"SchedBlock");
            String[] projs = masterScheduler.queryForProject(pName, piName, pType, aType);
            //do union now
            String[] unionSB = masterScheduler.getSBProjectUnion(sbs,projs);
            SBLite[] unionSBLites = masterScheduler.getExistingSBLite(unionSB);
            String[] unionProj = masterScheduler.getProjectSBUnion(projs,sbs);
            ProjectLite[] unionProjectLites = masterScheduler.getProjectLites(unionProj);
            releaseMSRef();
            tmp.add(unionProjectLites);
            tmp.add(unionSBLites);
        }catch (Exception e) {
            logger.severe("SP_ARCHIVE_CONTROLLER: Error doing query: "+e.toString()); 
            e.printStackTrace();
        }
        return tmp;
    }
    
}
