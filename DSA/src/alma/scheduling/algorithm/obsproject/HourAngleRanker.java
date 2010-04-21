package alma.scheduling.algorithm.obsproject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import alma.scheduling.algorithm.astro.Constants;
import alma.scheduling.algorithm.astro.CoordinatesUtil;
import alma.scheduling.algorithm.sbranking.AbstractBaseRanker;
import alma.scheduling.algorithm.sbranking.SBRank;
import alma.scheduling.datamodel.observatory.ArrayConfiguration;
import alma.scheduling.datamodel.obsproject.SchedBlock;

public class HourAngleRanker extends AbstractBaseRanker {

	private ArrayList<SBRank> ranks;
	
	public HourAngleRanker(String rankerName) {
		super(rankerName);
		ranks = new ArrayList<SBRank>();
	}

	@Override
	public SchedBlock getBestSB(List<SBRank> ranks) {
		return null;
	}

	@Override
	public List<SBRank> rank(List<SchedBlock> sbs, ArrayConfiguration arrConf,
			Date ut) {
		ranks.clear();
		for(SchedBlock sb: sbs){
			SBRank rank = new SBRank();
			rank.setId(sb.getId());
			double ra = sb.getSchedulingConstraints().getRepresentativeTarget()
					.getSource().getCoordinates().getRA() / 15.0;
			double ha = CoordinatesUtil.getHourAngle(ut, ra, Constants.CHAJNANTOR_LONGITUDE);
			if (ha >= 12 && ha <= 16)
				rank.setRank(-0.25 * (ha - 12.0) + 1.0);
			else if (ha < 12  && ha >= 8) {
				rank.setRank((0.25 * (ha - 12.0) ) + 1.0);
			}
			else
				rank.setRank(0);
			ranks.add(rank);
		}
		printVerboseInfo(ranks, arrConf.getId(), ut);
		return ranks;
	}

}
