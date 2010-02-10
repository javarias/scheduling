package alma.scheduling.algorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import alma.scheduling.algorithm.sbranking.SBRank;
import alma.scheduling.algorithm.sbranking.SchedBlockRanker;
import alma.scheduling.algorithm.sbselection.NoSbSelectedExecption;
import alma.scheduling.algorithm.sbselection.SchedBlockSelector;
import alma.scheduling.datamodel.obsproject.SchedBlock;

public class DynamicSchedulingAlgorithm {

    private SchedBlockRanker ranker;
    private Collection<SchedBlockSelector> selectors;
    /**
     * Stores the current SBs selected from selectors
     */
    private HashMap<Long, SchedBlock> sbs;
    private List<SBRank> ranks;
    
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
	    return ranker.getBestSB(ranks);
	}
	
	public void rankSchedBlocks(){
	    ranks = ranker.rank(new ArrayList<SchedBlock>(sbs.values()));
	}
	
	 /**
     * Clean the current candidate SBs and run again the selectors
	 * @throws NoSbSelectedExecption if a selector cannot get SBs or if this method
	 * cannot intersect a common group between al SBs returned by the selectors used
     */
	public void selectCandidateSB() throws NoSbSelectedExecption{
	    sbs.clear();
	    ArrayList<HashMap<Long, SchedBlock>> selectedSbs = 
	        new ArrayList<HashMap<Long,SchedBlock>>();
	    int i = 0;
        for(SchedBlockSelector s: selectors){
            selectedSbs.add(new HashMap<Long, SchedBlock>());
            try {
                for(SchedBlock sb: s.select())
                    selectedSbs.get(i).put(sb.getId(), sb);
            } catch (NoSbSelectedExecption e) {
                //log: DSA cannot continue if a selector cannot get SBs
                throw new NoSbSelectedExecption(e.getMessage());
            }
            i++;
        }
        HashMap<Long, SchedBlock> smallestSet = selectedSbs.get(0); 
        for(i = 1; i<selectedSbs.size(); i++){
            if (smallestSet.size() > selectedSbs.get(i).size())
                smallestSet = selectedSbs.get(i);
        }
        // Check if the SchedBlock was selected by the others selectors
        for(SchedBlock sb: smallestSet.values()){
            boolean verified = true;
            for(i = 0; i < selectedSbs.size(); i++){
                verified = true;
                if (selectedSbs.get(i) == smallestSet)
                    continue;
                if(selectedSbs.get(i).get(sb.getId()) == null){
                    verified = false;
                    break;
                }
            }
            // Add to the selected sb if that sb was selected by all the others selectors
            if(verified)
                sbs.put(sb.getId(), sb);
        }
        if (sbs.isEmpty()){
            String strCause = "Cannot get any SB valid to be ranked using ";
            for(SchedBlockSelector s: selectors)
                strCause += s.toString() + " ";
            throw new NoSbSelectedExecption(strCause);
        }
	}
	
	public void updateModel(){
	    
	}
	


	
}
