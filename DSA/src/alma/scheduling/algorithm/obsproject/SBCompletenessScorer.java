package alma.scheduling.algorithm.obsproject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import alma.scheduling.algorithm.sbranking.AbstractBaseRanker;
import alma.scheduling.algorithm.sbranking.SBRank;
import alma.scheduling.datamodel.observatory.ArrayConfiguration;
import alma.scheduling.datamodel.obsproject.SchedBlock;

public class SBCompletenessScorer extends AbstractBaseRanker {

	private ArrayList<SBRank> scores;
	
	public SBCompletenessScorer(String rankerName) {
		super(rankerName);
		scores = new ArrayList<SBRank>();
	}

	@Override
	public List<SBRank> rank(List<SchedBlock> sbs, ArrayConfiguration arrConf,
			Date ut, int nProjects) {
		scores.clear();
		for (SchedBlock sb : sbs) {
			SBRank score = new SBRank();
			score.setDetails(this.rankerName);
			score.setUid(sb.getUid());
//			score.setRank(Math.random());
			double s = (sb.getObsUnitControl().getMaximumTime() - 
					(sb.getObsUnitControl().getMaximumTime() - 
					 sb.getSchedBlockControl().getAccumulatedExecutionTime())) / 
					sb.getObsUnitControl().getMaximumTime();
			scores.add(score);
		}
		printVerboseInfo(scores, arrConf.getId(), ut);
		return scores;
	}

	@Override
	public SchedBlock getBestSB(List<SBRank> ranks) {
		// TODO Auto-generated method stub
		return null;
	}

}
