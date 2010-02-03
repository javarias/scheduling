package alma.scheduling.algorithm.sbranking;

import java.util.Collection;
import java.util.List;

import alma.scheduling.datamodel.obsproject.SchedBlock;

public class FinalRanker implements SchedBlockRanker {

    private List<SchedBlockRanker> rankers;
    
    public List<SchedBlockRanker> getRankers() {
        return rankers;
    }

    public void setRankers(List<SchedBlockRanker> rankers) {
        this.rankers = rankers;
    }

    @Override
    public SchedBlock getBestSB(Collection<SBRank> ranks) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<SBRank> rank() {
        // TODO Auto-generated method stub
        return null;
    }

}
