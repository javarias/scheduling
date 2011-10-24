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
package alma.scheduling.algorithm.sbselection;

import java.util.Collection;
import java.util.Date;

import alma.scheduling.algorithm.VerboseLevel;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.utils.TimeUtil;


public abstract class AbstractBaseSelector implements SchedBlockSelector {
    
    protected static VerboseLevel verboseLvl;
    protected String selectorName = "";
    
    protected AbstractBaseSelector(String selectorName){
        this.selectorName = selectorName;
    }
    
    public static void setVerboseLevel(VerboseLevel lvl){
        verboseLvl = lvl;
    }
 
    public String getSelectorName(){
        return selectorName;
    }
    
    protected void printVerboseInfo(Collection<SchedBlock> sbs,
            Long arrId, Date ut) {
        if (verboseLvl != VerboseLevel.NONE){
            System.out.println(TimeUtil.getUTString(ut)
                    + getVerboseLine(sbs, arrId));
        }
    }
    
    /**
     * @param selectedSbs the SchedBlocks selected by the Selector
     * @param arrId the Id of the array that ran the selector
     */
    protected String getVerboseLine(Collection<SchedBlock> selectedSbs, Long arrId){
        String str = "";
        switch (verboseLvl) {
        case LOW:
            str = "Selector: " + selectorName + " executed.";
            break;
        case MEDIUM:
            str = "Selector: " + selectorName + " retrieved " + selectedSbs.size();
            str += " SchedBlocks.";
            break;
        case HIGH:
            str = "Selector: " + selectorName + " retrieved " + selectedSbs.size();
            str += " SchedBlocks.\n";
            str += "SchedBlock Ids: ";
            for(SchedBlock sb: selectedSbs)
                str += sb.getId() + " ";
            break;
        default:
            break;
        }
        return str;
    }

    @Override
    public boolean canBeSelected(SchedBlock sb) {
        return false;
    }
}

