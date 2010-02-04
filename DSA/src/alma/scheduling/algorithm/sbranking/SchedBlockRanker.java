package alma.scheduling.algorithm.sbranking;

import java.util.List;

import alma.scheduling.datamodel.obsproject.SchedBlock;

public interface SchedBlockRanker {

    /**
     * This method should clean the current ranks and calculate the
     * rank of each SchedBlock
     * 
     * @param the list of SchedBlock to be ranked
     * @return the rank of each SchedBlock. 
     */
    public List<SBRank> rank(List<SchedBlock> sbs);

    /**
     * 
     * @param ranks the ranks of the SchedBlocks
     * @return the best ranked SckedBlock
     */
    public SchedBlock getBestSB(List<SBRank> ranks);
}
