package alma.scheduling.algorithm.sbranking;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import alma.scheduling.datamodel.obsproject.SchedBlock;

public class ScienceGradeRanker implements SchedBlockRanker {

    private HashMap<SBRank,SchedBlock> ranks;
    private Random rand;
    
    /**
     * Create a new Science Grade Ranker
     * 
     */
    public ScienceGradeRanker(){
        ranks = new HashMap<SBRank, SchedBlock>();
        rand = new Random();
    }

    @Override
    public SchedBlock getBestSB(List<SBRank> ranks) {
        ArrayList<SBRank> ranksCopy = new ArrayList<SBRank>(ranks);
        Collections.sort(ranksCopy);
        return this.ranks.get(ranksCopy.get(0));
    }

    /*
     * The current implementation generates a random number
     * (non-Javadoc)
     * @see alma.scheduling.algorithm.sbranking.SchedBlockRanker#rank()
     */
    @Override
    public List<SBRank> rank(List<SchedBlock> sbs) {
        ranks.clear();
        for(SchedBlock sb: sbs){
            SBRank rank = new SBRank();
            rank.setId(sb.getId());
            rank.setRank(rand.nextInt());
            ranks.put(rank, sb);
        }
        return new ArrayList<SBRank>(ranks.keySet());
    }

    @Override
    public String toString() {
        return "Science Grade Ranker";
    }


}
