package alma.scheduling.algorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import alma.scheduling.algorithm.sbranking.SchedBlockRanker;
import alma.scheduling.algorithm.sbselection.SchedBlockSelector;
import alma.scheduling.datamodel.obsproject.SchedBlock;

public class DynamicSchedulingAlgorithm {

    private SchedBlockRanker ranker;
    private Collection<SchedBlockSelector> selectors;
    /**
     * Stores the current SBs selected from selectors
     */
    private HashMap<Long, SchedBlock> sbs;
    
    public DynamicSchedulingAlgorithm(){
        sbs =  new HashMap<Long, SchedBlock>();
    }

    public SchedBlockRanker getRanker() {
        return ranker;
    }


    public void setRanker(SchedBlockRanker ranker) {
        this.ranker = ranker;
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
	    ranker.rank(new ArrayList<SchedBlock>(sbs.values()));
	}
	
	 /**
     * Clean the current candidate SBs and run again the selectors
     */
	@SuppressWarnings("unchecked")
    public void selectCandidateSB(){
	    sbs.clear();
	    HashMap<Long, SchedBlock>[] selectedSbs =
	        new HashMap[selectors.size()];
	    int i = 0;
        for(SchedBlockSelector s: selectors){
            for(SchedBlock sb: s.select())
                selectedSbs[i].put(sb.getId(), sb);
            i++;
        }
        HashMap<Long, SchedBlock> smallestSet = selectedSbs[0]; 
        for(i = 1; i<selectedSbs.length; i++){
            if (smallestSet.size() > selectedSbs[i].size())
                smallestSet = selectedSbs[i];
        }
        // Check if the SchedBlock was selected by the others selectors
        for(SchedBlock sb: smallestSet.values()){
            boolean verified = true;
            for(i = 0; i < selectedSbs.length; i++){
                verified = true;
                if (selectedSbs[i] == smallestSet)
                    continue;
                if(selectedSbs[i].get(sb.getId()) == null){
                    verified = false;
                    break;
                }
            }
            // Add to the selected sb if that sb was selected by all the others selectors
            if(verified)
                sbs.put(sb.getId(), sb);
        }
	}
	
	public void updateModel(){
	    
	}
	


	
}
