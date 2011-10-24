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
package alma.scheduling.algorithm.obsproject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static alma.scheduling.utils.Constants.CHAJNANTOR_LATITUDE;
import static alma.scheduling.utils.Constants.CHAJNANTOR_LONGITUDE;
import alma.scheduling.algorithm.sbranking.AbstractBaseRanker;
import alma.scheduling.algorithm.sbranking.SBRank;
import alma.scheduling.datamodel.observatory.ArrayConfiguration;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.utils.CoordinatesUtil;


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
			Date ut, int nProjects) {
		ranks.clear();
		for(SchedBlock sb: sbs){
			SBRank rank = new SBRank();
			rank.setDetails(this.rankerName);
			rank.setUid(sb.getUid());
			double ra = sb.getSchedulingConstraints().getRepresentativeTarget()
					.getSource().getCoordinates().getRA() / 15.0;
			//HA in rads
			double ha = CoordinatesUtil.getHourAngle(ut, ra, CHAJNANTOR_LONGITUDE)
					* Math.PI / 12.0;
			//Dec in rads
			double delta = Math.toRadians(sb.getSchedulingConstraints().getRepresentativeTarget()
					.getSource().getCoordinates().getDec());
			//Chajnantor longitude in rads
			double phi = Math.toRadians(CHAJNANTOR_LATITUDE);
			System.out.println("ha=" + ha + "(" + CoordinatesUtil.getHourAngle(ut, ra, CHAJNANTOR_LONGITUDE) +")" + "; delta=" + delta +"; phi=" + phi);
			double score = (Math.cos(ha) + Math.tan(delta) * Math.tan(phi)) /
					(1 + Math.tan(delta) * Math.tan(phi));
			System.out.println("up=" + (Math.cos(ha) + Math.tan(delta) * Math.tan(phi))
					+ "; down=" + (1 + Math.tan(delta) * Math.tan(phi)) + "; score=" + score);
			rank.setRank(score);
			ranks.add(rank);
		}
		printVerboseInfo(ranks, arrConf.getId(), ut);
		return ranks;
	}

}
