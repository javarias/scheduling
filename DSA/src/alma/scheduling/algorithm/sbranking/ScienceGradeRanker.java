package alma.scheduling.algorithm.sbranking;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

import alma.scheduling.datamodel.obsproject.SchedBlock;

public class ScienceGradeRanker implements SchedBlockRanker {

    private Collection<SchedBlock> sbs;
    private HashMap<SBRank,SchedBlock> ranks;
    private Random rand;
    
    /**
     * Create a new Science Grade Ranker
     * 
     * @param sbs The SchedBlocks to be ranked
     * @throws NullPointerException if sbs is null
     */
    public ScienceGradeRanker(Collection<SchedBlock> sbs){
        if (sbs == null)
            throw new NullPointerException();
        this.sbs = sbs;
        ranks = new HashMap<SBRank, SchedBlock>(sbs.size());
        rand = new Random();
    }
    
    @Override
    public SchedBlock getBestSB(Collection<SBRank> ranks) {
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
    public Collection<SBRank> rank() {
        ranks.clear();
        for(SchedBlock sb: sbs){
            SBRank rank = new SBRank();
            rank.setId(sb.getId());
            rank.setRank(rand.nextInt());
            ranks.put(rank, sb);
        }
        return ranks.keySet();
    }

}
