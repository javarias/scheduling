package alma.scheduling.algorithm.sbranking;

import java.util.Collection;

import alma.scheduling.datamodel.obsproject.SchedBlock;

public interface SchedBlockRanker {

    public SchedBlock getBestSB();
    
    public Collection<SBRank> rank();
}
