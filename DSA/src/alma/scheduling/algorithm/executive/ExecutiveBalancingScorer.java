package alma.scheduling.algorithm.executive;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alma.scheduling.algorithm.sbranking.AbstractBaseRanker;
import alma.scheduling.algorithm.sbranking.SBRank;
import alma.scheduling.datamodel.observatory.ArrayConfiguration;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.utils.DSAErrorStruct;

public class ExecutiveBalancingScorer extends AbstractBaseRanker {

	private static Logger logger = LoggerFactory.getLogger(ExecutiveBalancingScorer.class);
	
	private Map<String, Double> execBalance;
	private ArrayList<SBRank> scores;
	
	public ExecutiveBalancingScorer(String rankerName) {
		super(rankerName);
	}

	@Override
	public List<SBRank> rank(List<SchedBlock> sbs, ArrayConfiguration arrConf,
			Date ut, int nProjects) {
		scores = new ArrayList<SBRank>();
        double score;
        ArrayList<DSAErrorStruct> errors =  new ArrayList<DSAErrorStruct>();
		for(SchedBlock sb: sbs) {
			try {
				if (!execBalance.containsKey(sb.getExecutive().getName())){
					logger.warn("executive with name: " + sb.getExecutive().getName() 
							+ " is not part of the executive balancing map. Setting the score to default (0)");
					score = 0;
				} else
					score = execBalance.get(sb.getExecutive().getName());
	            SBRank rank = new SBRank();
	            rank.setUid(sb.getUid());
	            rank.setRank(score);
	            scores.add(rank);
	            logger.debug("rank: " + rank);
			} catch(RuntimeException ex) {
				errors.add(new DSAErrorStruct(this.getClass().getCanonicalName(), 
        				sb.getUid(), "SchedBlock", ex));
	            SBRank rank = new SBRank();
	            rank.setUid(sb.getUid());
	            rank.setRank(0.0);
	            scores.add(rank);
			}
		}
        reportErrors(errors, sbs);
        printVerboseInfo(scores, arrConf.getId(), ut);
		return scores;
	}

	@Override
	public SchedBlock getBestSB(List<SBRank> ranks) {
		throw new RuntimeException("Not Implemented");
		//return null;
	}

	public Map<String, Double> getExecBalance() {
		return execBalance;
	}

	public void setExecBalance(Map<String, Double> execBalance) {
		this.execBalance = execBalance;
	}

	@Override
    public String toString() {
        return "Executive Balancing Scorer";
    }
}
