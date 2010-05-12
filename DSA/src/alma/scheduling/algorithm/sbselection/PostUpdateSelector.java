package alma.scheduling.algorithm.sbselection;

import java.util.Collection;

import alma.scheduling.datamodel.obsproject.SchedBlock;

public interface PostUpdateSelector extends SchedBlockSelector{

    public void setSchedBlocksSubset(Collection<SchedBlock> sbs);
    
}
