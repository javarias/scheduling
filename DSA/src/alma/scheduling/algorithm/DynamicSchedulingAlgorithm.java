package alma.scheduling.algorithm;

import java.util.Collection;

import alma.scheduling.algorithm.sbselection.NoSbSelectedException;
import alma.scheduling.algorithm.sbselection.SchedBlockSelector;

public interface DynamicSchedulingAlgorithm {

    
    public abstract void rankSchedBlocks();

    /**
     * Clean the current candidate SBs and run again the selectors
     * @throws NoSbSelectedException if a selector cannot get SBs or if this method
     * cannot intersect a common group between al SBs returned by the selectors used
     */
    public abstract void selectCandidateSB() throws NoSbSelectedException;

    public abstract void updateModel();

}