package alma.scheduling.algorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import alma.scheduling.algorithm.sbranking.SBRank;
import alma.scheduling.algorithm.sbranking.SchedBlockRanker;
import alma.scheduling.algorithm.sbselection.NoSbSelectedException;
import alma.scheduling.algorithm.sbselection.SchedBlockSelector;
import alma.scheduling.datamodel.obsproject.SchedBlock;

public class DynamicSchedulingAlgorithmImpl implements DynamicSchedulingAlgorithm {
    
    private static Logger logger = LoggerFactory.getLogger(DynamicSchedulingAlgorithmImpl.class);

    private SchedBlockRanker ranker;
    private Collection<SchedBlockSelector> selectors;
    /**
     * Stores the current SBs selected from selectors
     */
    private HashMap<Long, SchedBlock> sbs;
    private List<SBRank> ranks;
    
    public DynamicSchedulingAlgorithmImpl(){
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

    /* (non-Javadoc)
     * @see alma.scheduling.algorithm.DynamicSchedulingAlgorithm#setSelectors(java.util.Collection)
     */
    public void setSelectors(Collection<SchedBlockSelector> selectors) {
        this.selectors = selectors;
    }

    public SchedBlock getSelectedSchedBlock(){
	    return ranker.getBestSB(ranks);
	}
	
	/* (non-Javadoc)
     * @see alma.scheduling.algorithm.DynamicSchedulingAlgorithm#rankSchedBlocks()
     */
	public void rankSchedBlocks(){
	    ranks = ranker.rank(new ArrayList<SchedBlock>(sbs.values()));
	}
	
	 /* (non-Javadoc)
     * @see alma.scheduling.algorithm.DynamicSchedulingAlgorithm#selectCandidateSB()
     */
	public void selectCandidateSB() throws NoSbSelectedException{
	    sbs.clear();
	    ArrayList<HashMap<Long, SchedBlock>> selectedSbs = 
	        new ArrayList<HashMap<Long,SchedBlock>>();
	    int i = 0;
        for(SchedBlockSelector s: selectors){
            selectedSbs.add(new HashMap<Long, SchedBlock>());
            try {
                for(SchedBlock sb: s.select())
                    selectedSbs.get(i).put(sb.getId(), sb);
            } catch (NoSbSelectedException e) {
                logger.warn("DSA cannot continue if a selector cannot get SBs");
                throw new NoSbSelectedException(e.getMessage());
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
            logger.warn("DSA cannot continue if it doesn't have SB to rank");
            String strCause = "Cannot get any SB valid to be ranked using ";
            for(SchedBlockSelector s: selectors)
                strCause += s.toString() + " ";
            throw new NoSbSelectedException(strCause);
        }
	}
	
	
	/* (non-Javadoc)
     * @see alma.scheduling.algorithm.DynamicSchedulingAlgorithm#updateModel()
     */
	@Transactional
	public void updateModel(){
	    
	}
	


	
}
