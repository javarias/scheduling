package alma.scheduling.algorithm.sbranking;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import alma.scheduling.datamodel.obsproject.SchedBlock;

public class FinalRanker implements SchedBlockRanker {

    private List<SchedBlockRanker> rankers;
    private HashMap<SBRank,SchedBlock> ranks;
    
    public List<SchedBlockRanker> getRankers() {
        return rankers;
    }

    public void setRankers(List<SchedBlockRanker> rankers) {
        this.rankers = rankers;
    }

    @Override
    public SchedBlock getBestSB(List<SBRank> ranks) {
        ArrayList<SBRank> ranksCopy = new ArrayList<SBRank>(ranks);
        Collections.sort(ranksCopy);
        return this.ranks.get(ranksCopy.get(0));
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SBRank> rank(List<SchedBlock> sbs) {
        ranks.clear();
        List<SBRank>[] res = new List[rankers.size()];
        int i = 0;
        for(SchedBlockRanker r: rankers){
            res[i] = r.rank(sbs);
            i++;
        }
        for(i = 0; i < sbs.size(); i++){
            SBRank rank =  new SBRank();
            rank.setId(res[0].get(i).getId());
            rank.setUid(res[0].get(i).getUid());
            int score = 0;
            for(int j = 0; j < res.length; j++)
                score += res[j].get(i).getRank();
            rank.setRank(score);
            ranks.put(rank, sbs.get(i));
        }
        return new ArrayList<SBRank>(ranks.keySet());
    }

}
