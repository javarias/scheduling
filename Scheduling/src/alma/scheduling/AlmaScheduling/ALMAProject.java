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

import alma.scheduling.Define.SB;
import alma.scheduling.Define.DateTime;
import alma.scheduling.Define.Project;
import alma.scheduling.Define.Program;

import alma.entity.xmlbinding.obsproject.*;
import alma.entity.xmlbinding.schedblock.*;

/**
 * Used when an observing project (ObsProject) is retrieved 
 * from the archvie. The information required by the 
 * scheduling subsystem in "Real Scheduling Mode" is extracted
 * from the obsproject and used to populate the contents of 
 * the Project. 
 *
 * @author Sohaila Lucero
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
        setTimeOfCreation(new DateTime(System.currentTimeMillis()));
        this.obsProject = obs;
        this.sbs = getSBRefs(obsProject.getObsProgram().getObsPlan().getObsUnitSetTChoice());
        //this.sbs = getSBRefs(obsProject.getObsProgram().getObsUnitSetTChoice());

    }

    private SchedBlockRefT[] getSBRefs(ObsUnitSetTChoice choice) {
        if(choice.getObsUnitSetCount() == 0) {
            return choice.getSchedBlockRef();
        } else {
            Vector tmpSBRefs = new Vector();
            ObsUnitSetT[] sets = choice.getObsUnitSet();
            for(int i=0; i < sets.length; i++) {
                tmpSBRefs.add(getSBRefs(sets[i].getObsUnitSetTChoice()));
            }
            Vector tmpsbs = new Vector();
            for(int j=0; j < tmpSBRefs.size(); j++){
                SchedBlockRefT[] refs = (SchedBlockRefT[])tmpSBRefs.elementAt(j);
                for(int k=0; k < refs.length; k++) {
                    tmpsbs.add(refs[k]);
                }
            }
            SchedBlockRefT[] sbRefs = new SchedBlockRefT[tmpsbs.size()];
            for(int l=0; l < tmpsbs.size(); l++){
                sbRefs[l] = (SchedBlockRefT)tmpsbs.elementAt(l);
            }
            return sbRefs;
        }
    }

    public String[] getSBIds() {
        String[] ids = new String[sbs.length];
        for(int i=0; i< sbs.length; i++) {
            ids[i] = sbs[i].getEntityId();
        }
        return ids;
    }

    /*
    private void getSbsInProject() {
        //Vector sbRefs = new Vector();
        obsProgram = obsProject.getObsProgram();
        Vector tmp_sbs=new Vector();
        
        obsUnitSets = obsProgram.getObsUnitSetTChoice().getObsUnitSet();
        System.out.println("Project has "+obsUnitSets.length+" ObsUnitSets");
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
            //System.out.println("tmp1 size = "+tmp1.length);
            SchedBlockRefT[] tmp2 = obsUnitSets[i].getObsUnitSetTChoice().getSchedBlockRef();
            //System.out.println("tmp2 size = "+tmp2.length);
            for(int x=0; x<tmp2.length; x++){
                tmp_sbs.add(tmp2[x]);
            }
            System.out.println("tmp_sbs size == "+tmp_sbs.size());
        }
        
    }
    */
}
