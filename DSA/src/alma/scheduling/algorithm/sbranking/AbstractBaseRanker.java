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
package alma.scheduling.algorithm.sbranking;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import alma.scheduling.algorithm.VerboseLevel;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.utils.DSAErrorStruct;
import alma.scheduling.utils.ErrorHandling;
import alma.scheduling.utils.TimeUtil;

public abstract class AbstractBaseRanker implements SchedBlockRanker {

    protected static VerboseLevel verboseLvl = VerboseLevel.NONE;
    
    protected String rankerName = "" ;
    
    protected double factor = 1.0;
    
    protected AbstractBaseRanker(String rankerName){
        this.rankerName = rankerName;
    }
    
    public static void setVerboseLevel(VerboseLevel lvl){
        verboseLvl = lvl;
    }
    
    protected void printVerboseInfo(Collection<SBRank> ranks,
            Long arrId, Date ut) {
        if (verboseLvl != VerboseLevel.NONE){
            System.out.println(TimeUtil.getUTString(ut)
                    + getVerboseLine(ranks, arrId));
        }
    }

    /**
     * @param selectedSbs the SchedBlocks selected by the Selector
     * @param arrId the Id of the array that ran the selector
     */
    protected String getVerboseLine(Collection<SBRank> ranks, Long arrId){
        String str = "";
        switch (verboseLvl) {
        case LOW:
            str = "Ranker: " + rankerName + " for Array Id: " + arrId + " executed.";
            break;
        case MEDIUM:
            str = "Ranker: " + rankerName + " for Array Id: " + arrId + ". Best scored Sched Block ";
            SBRank r = Collections.max(ranks);
            str += r.toString();
            break;
        case HIGH:
            str = "Ranker: " + rankerName + " for Array Id: " + arrId + " executed\n";
            str += "Score obtained for each SchedBlock: ";
            ArrayList<SBRank> rankList = new ArrayList<SBRank>(ranks);
            Collections.sort(rankList);
            for(SBRank rank: rankList)
                str += rank.toString() + " ";
            break;
        default:
            break;
        }
        return str;
    }

	public double getFactor() {
		return factor;
	}

	public void setFactor(double factor) {
		this.factor = factor;
	}
	
	protected void reportErrors(List<DSAErrorStruct> errors, List<SchedBlock> sbs) {
        if (errors.size() > 0 && errors.size() != sbs.size()) {
        	for(DSAErrorStruct struct: errors)
        		ErrorHandling.getInstance().warning(
        				"Failed " + struct.getDSAPart() + 
        				" when scoring " + struct.getEntityType() + 
        				" Id: " + struct.getEntityId(), struct.getException());
        }
        else if (errors.size() == sbs.size()) {
        	ErrorHandling.getInstance().severe("Failed " + errors.get(0).getDSAPart()
        			+ ". No " + errors.get(0).getEntityType() + "s were scored"
        			, errors.get(0).getException());
        }
	}

}
