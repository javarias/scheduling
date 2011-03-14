package alma.scheduling.algorithm.sbranking;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import alma.scheduling.algorithm.VerboseLevel;
import alma.scheduling.algorithm.astro.TimeUtil;

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

}
