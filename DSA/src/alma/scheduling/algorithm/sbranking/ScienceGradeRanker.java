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
 * "@(#) $Id: ScienceGradeRanker.java,v 1.11 2010/05/25 23:40:15 javarias Exp $"
 */
package alma.scheduling.algorithm.sbranking;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alma.scheduling.datamodel.observatory.ArrayConfiguration;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.ScienceGrade;

public class ScienceGradeRanker extends AbstractBaseRanker {

    private static Logger logger = LoggerFactory.getLogger(ScienceGradeRanker.class);
    
    private double factor;
    public void setFactor(double factor) {
        this.factor = factor;
    }
    
    private ArrayList<SBRank> ranks;
    
    /**
     * Create a new Science Grade Ranker
     * 
     */
    public ScienceGradeRanker(String rankerName){
        super(rankerName);
        ranks = new ArrayList<SBRank>();
    }

    @Override
    public SchedBlock getBestSB(List<SBRank> ranks) {
        return null;
    }

    @Override
    public List<SBRank> rank(List<SchedBlock> sbs, ArrayConfiguration arrConf, Date ut, int nProjects){
        ranks.clear();
        double score;
        for(SchedBlock sb: sbs){
            score = ((double)nProjects - (sb.getProject().getScienceRank() - 1.0))/((double) nProjects);
            if(sb.getProject().getLetterGrade() == ScienceGrade.A)
                score += 4.0;
            else if (sb.getProject().getLetterGrade() == ScienceGrade.B)
                score += 2.0;
            else if (sb.getProject().getLetterGrade() == ScienceGrade.C)
                score += 1.0;
            else
                score += 0;
            SBRank rank = new SBRank();
            rank.setId(sb.getId());
            rank.setRank(score);
            ranks.add(rank);
            logger.debug("rank: " + rank);
        }
        printVerboseInfo(ranks, arrConf.getId(), ut);
        return ranks;
    }

    @Override
    public String toString() {
        return "Science Grade Ranker";
    }


}
