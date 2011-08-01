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

