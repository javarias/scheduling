package alma.scheduling.algorithm.weather;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import alma.scheduling.algorithm.sbranking.AbstractBaseRanker;
import alma.scheduling.algorithm.sbranking.SBRank;
import alma.scheduling.datamodel.observatory.ArrayConfiguration;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.utils.DSAErrorStruct;

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
		 ArrayList<DSAErrorStruct> errors =  new ArrayList<DSAErrorStruct>();
		for (SchedBlock sb: sbs) {
			try {
			SBRank rank = new SBRank();
			rank.setDetails(this.rankerName);
			rank.setUid(sb.getUid());
			double score = sb.getWeatherDependentVariables().getZenithTsys() / 
					sb.getWeatherDependentVariables().getTsys();
			rank.setRank(score);
			ranks.add(rank);
			} catch (RuntimeException ex) {
	        	errors.add(new DSAErrorStruct(this.getClass().getCanonicalName(), 
        				sb.getUid(), "SchedBlock", ex));
	            SBRank rank = new SBRank();
	            rank.setUid(sb.getUid());
	            rank.setRank(0.0);
	            ranks.add(rank);
			}
		}
		reportErrors(errors, sbs);
		printVerboseInfo(ranks, arrConf.getId(), ut);
		return ranks;
	}

	@Override
	public SchedBlock getBestSB(List<SBRank> ranks) {
		// TODO Auto-generated method stub
		return null;
	}

}
