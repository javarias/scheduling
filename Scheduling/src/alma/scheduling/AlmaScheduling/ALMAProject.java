/*
 * ALMA - Atacama Large Millimeter Array
 * (c) European Southern Observatory, 2002
 * (c) Associated Universities Inc., 2002
 * Copyright by ESO (in the framework of the ALMA collaboration),
 * Copyright by AUI (in the framework of the ALMA collaboration),
 * All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY, without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307  USA
 *
 * File ALMAProject.java
 */
package alma.scheduling.AlmaScheduling;

import java.util.ArrayList;
import java.util.Vector;

import alma.scheduling.Define.Project;

import alma.entity.xmlbinding.obsproject.*;
import alma.entity.xmlbinding.schedblock.*;

/**
 * Used when an observing project (ObsProject) is retrieved 
 * from the archvie. The information required by the 
 * scheduling subsystem in "Real Scheduling Mode" is extracted
 * from the obsproject and used to populate the contents of 
 * the Project. 
 *
 * @author Sohaila Roberts
 */
public class ALMAProject extends Project {
    //Copy of the obs project that came out of the archive.
    private ObsProject obsProject;
    private ObsProgramT obsProgram;
    private SchedBlockRefT[] sbs;
    private ObsUnitSetT[] obsUnitSets;

    
    public ALMAProject(ObsProject obs) { 
        super( obs.getObsProjectEntity().getEntityId(), 
            obs.getObsProposalRef().getEntityId(),
                obs.getProjectName(), 
                    obs.getVersion(), 
                        obs.getPI() );
        this.obsProject = obs;
        getSbsInProject();
        System.out.println("In ALMAProject constructor");
    }

    private void getSbsInProject() {
        obsProgram = obsProject.getObsProgram();
        
        sbs = obsProgram.getObsUnitSetTChoice().getSchedBlockRef();
        if(sbs == null ){
            System.out.println("sbs null");
        }
        if(sbs.length ==0) {
            System.out.println("sbs 0 length");
        }
        for(int i=0; i < sbs.length; i++ ) {
            String id = sbs[i].getEntityId();
            System.out.println("In ALMAProject: "+id);
            //get sbs from archive & create ALMASBs
        }

        Vector tmp_sbs=new Vector();
        
        obsUnitSets = obsProgram.getObsUnitSetTChoice().getObsUnitSet();
        if(obsUnitSets == null ){
            System.out.println("obsUnitSets null");
        }
        if(obsUnitSets.length ==0) {
            System.out.println("obsUnitSets 0 length");
        } else {
            System.out.println("obsUnitSets length="+ obsUnitSets.length);
        }
        for(int i=0; i < obsUnitSets.length; i++) {
            //check and see if it has more obsUnitSets or sbs
            ObsUnitSetT[] tmp1 = obsUnitSets[i].getObsUnitSetTChoice().getObsUnitSet();
            System.out.println("tmp1 size = "+tmp1.length);
            SchedBlockRefT[] tmp2 = obsUnitSets[i].getObsUnitSetTChoice().getSchedBlockRef();
            System.out.println("tmp2 size = "+tmp2.length);
            for(int x=0; x<tmp2.length; x++){
                tmp_sbs.add(tmp2[x]);
            }
            System.out.println(tmp_sbs.size());
        }
        
    }
}
