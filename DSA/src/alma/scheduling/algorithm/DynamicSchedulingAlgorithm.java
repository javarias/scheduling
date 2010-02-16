package alma.scheduling.algorithm;

import alma.scheduling.algorithm.sbselection.NoSbSelectedException;
import alma.scheduling.datamodel.obsproject.SchedBlock;

public interface DynamicSchedulingAlgorithm {

    
    public void rankSchedBlocks();

    /**
     * Clean the current candidate SBs and run again the selectors
     * 
     * @throws NoSbSelectedException if a selector cannot get SBs or if this method
     * cannot intersect a common group between al SBs returned by the selectors used
     */
    public void selectCandidateSB() throws NoSbSelectedException;

    public void updateModel();
    
    public SchedBlock getSelectedSchedBlock();

}