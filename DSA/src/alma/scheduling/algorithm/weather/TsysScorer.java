package alma.scheduling.algorithm.weather;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import alma.scheduling.algorithm.sbranking.AbstractBaseRanker;
import alma.scheduling.algorithm.sbranking.SBRank;
import alma.scheduling.datamodel.observatory.ArrayConfiguration;
import alma.scheduling.datamodel.obsproject.SchedBlock;

public class TsysScorer extends AbstractBaseRanker {

	private ArrayList<SBRank> ranks;
	
	public TsysScorer(String rankerName) {
		super(rankerName);
		ranks = new ArrayList<SBRank>();
	}

	@Override
	public List<SBRank> rank(List<SchedBlock> sbs, ArrayConfiguration arrConf,
			Date ut, int nProjects) {
		ranks.clear();
		for (SchedBlock sb: sbs) {
			SBRank rank = new SBRank();
			rank.setDetails(this.rankerName);
			rank.setUid(sb.getUid());
			double score = sb.getWeatherDependentVariables().getZenithTsys() / 
					sb.getWeatherDependentVariables().getTsys();
			rank.setRank(score);
			ranks.add(rank);
		}
		return ranks;
	}

	@Override
	public SchedBlock getBestSB(List<SBRank> ranks) {
		// TODO Auto-generated method stub
		return null;
	}

}
