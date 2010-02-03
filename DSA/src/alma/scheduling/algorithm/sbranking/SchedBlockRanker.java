package alma.scheduling.algorithm.sbranking;

import java.util.Collection;

import alma.scheduling.datamodel.obsproject.SchedBlock;

public interface SchedBlockRanker {

    /**
     * This method should clean the current ranks and calculate the
     * rank of each SchedBlock
     * 
     * @return the rank of each SchedBlock
     */
    public Collection<SBRank> rank();

    /**
     * 
     * @param ranks the ranks of the SchedBlocks
     * @return the best ranked SckedBlock
     */
    public SchedBlock getBestSB(Collection<SBRank> ranks);
}
