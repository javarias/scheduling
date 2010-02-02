package alma.scheduling.algorithm;

import java.util.Collection;
import java.util.HashMap;

import alma.scheduling.algorithm.sbranking.SchedBlockRanker;
import alma.scheduling.algorithm.sbselection.SchedBlockSelector;
import alma.scheduling.datamodel.obsproject.SchedBlock;

public class DynamicSchedulingAlgorithm {

    private Collection<SchedBlockRanker> rankers;
    private Collection<SchedBlockSelector> selectors;
    /**
     * Stores the current SBs selected
     */
    private HashMap<Long, SchedBlock> sbs;
    
    public DynamicSchedulingAlgorithm(){
        sbs =  new HashMap<Long, SchedBlock>();
    }
    
	public Collection<SchedBlockRanker> getRankers() {
        return rankers;
    }

    public void setRankers(Collection<SchedBlockRanker> rankers) {
        this.rankers = rankers;
    }

    public Collection<SchedBlockSelector> getSelectors() {
        return selectors;
    }

    public void setSelectors(Collection<SchedBlockSelector> selectors) {
        this.selectors = selectors;
    }

    public SchedBlock getSelectedSchedBlock(){
	    return null;
	}
	
	public void rankSchedBlocks(){
	    
	}
	
	 /**
     * Clean the current candidate SBs and run again the selectors
     */
	public void selectCandidateSB(){
	    sbs.clear();
        for(SchedBlockSelector s: selectors){
            for(SchedBlock sb: s.select())
                sbs.put(sb.getId(), sb);
        }
	}
	
	public void updateModel(){
	    
	}
	


	
}
