/*******************************************************************************
 * ALMA - Atacama Large Millimeter Array
 * Copyright (c) AUI - Associated Universities Inc., 2011
 * (in the framework of the ALMA collaboration).
 * All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 *******************************************************************************/
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
			//set priority on higher bands
			score += sb.getRepresentativeBand() / 10;  
			rank.setRank(score);
			ranks.add(rank);
			} catch (RuntimeException ex) {
	        	errors.add(new DSAErrorStruct(this.getClass().getCanonicalName(), 
        				sb.getUid(), "SchedBlock", ex));
	            SBRank rank = new SBRank();
	            rank.setUid(sb.getUid());
	            rank.setRank(0.0);
	            rank.setDetails(this.rankerName);
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
