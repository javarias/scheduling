/*
 * ALMA - Atacama Large Millimeter Array
 * (c) European Southern Observatory, 2002
 * (c) Associated Universities Inc., 2002
 * Copyright by ESO (in the framework of the ALMA collaboration),
 * Copyright by AUI (in the framework of the ALMA collaboration),
 * All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY, without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307  USA
 *
 * "@(#) $Id: FinalRanker.java,v 1.12 2011/05/04 23:21:21 javarias Exp $"
 */

package alma.scheduling.algorithm.sbranking;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alma.scheduling.datamodel.observatory.ArrayConfiguration;
import alma.scheduling.datamodel.obsproject.SchedBlock;

public class FinalRanker extends AbstractBaseRanker {

    public FinalRanker(String rankerName) {
        super(rankerName);
    }

    private static Logger logger = LoggerFactory.getLogger(FinalRanker.class);
    
    private List<Double> weights;
    public void setWeights(List<Double> weights) {
        this.weights = weights;
    }
    
    private List<SchedBlockRanker> rankers;
    public List<SchedBlockRanker> getRankers() {
        return rankers;
    }
    public void setRankers(List<SchedBlockRanker> rankers) {
        this.rankers = rankers;
    }    
    
    private HashMap<SBRank,SchedBlock> ranks = new HashMap<SBRank, SchedBlock>();
    

    @Override
    public SchedBlock getBestSB(List<SBRank> ranks) {
        SBRank best = Collections.max(ranks);
        System.out.println(best.getId() + "," + this.ranks.get(best).getId() + "," + best.getRank());
        return this.ranks.get(best);
    }

    @SuppressWarnings("unchecked")
	@Override
	public List<SBRank> rank(List<SchedBlock> sbs, ArrayConfiguration arrConf,
			Date ut, int nProjects) {
		ranks.clear();
		List<SBRank>[] results = new List[rankers.size()]; // for each ranker, a
															// list over the sbs
		int i = 0;
		for (SchedBlockRanker r : rankers) {
			results[i] = r.rank(sbs, arrConf, ut, nProjects);
			i++;
		}

		for (int sb = 0; sb < sbs.size(); sb++) {
			SBRank rank = new SBRank();
			rank.setUid(sbs.get(sb).getUid());
			double score = 0;
			for (i = 0; i < results.length; i++) {
				rank.addRank(results[i].get(sb));
				score += results[i].get(sb).getRank() * rankers.get(i).getFactor();
			}
			rank.setRank(score);
			ranks.put(rank, sbs.get(sb));
		}
		
		ArrayList<SBRank> retVal = new ArrayList<SBRank>(ranks.keySet());
		printVerboseInfo(retVal, arrConf.getId(), ut);
		return retVal;
	}

}
