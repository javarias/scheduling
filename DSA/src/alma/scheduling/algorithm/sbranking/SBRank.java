package alma.scheduling.algorithm.sbranking;


/**
 * This class will store the score obtained by SB in the Dynamic Scheduling Algorithm Ranker
 * 
 * @author javarias
 * @see SchedBlockRanker
 */
public class SBRank implements Comparable<SBRank>{

    /**
     * ID of SBRank
     */
    private long id;
    /**
     * Score obtained
     */
    private int rank;
    /**
     * SB uid reference
     */
    private String uid;
    
    public SBRank(long id, int rank, String uid) {
        super();
        this.id = id;
        this.rank = rank;
        this.uid = uid;
    }
    
    public SBRank(){
    }
    
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    @Override
    public String toString() {
        return "SBRank [id=" + id + ", rank=" + rank + ", uid=" + uid + "]";
    }

    @Override
    public int compareTo(SBRank o) {
        return this.getRank() - o.getRank();
    }
    
}
