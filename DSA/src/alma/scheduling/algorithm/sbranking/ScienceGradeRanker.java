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
 * "@(#) $Id: ScienceGradeRanker.java,v 1.6 2010/04/14 17:22:12 javarias Exp $"
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

public class ScienceGradeRanker extends AbstractBaseRanker {

    private static Logger logger = LoggerFactory.getLogger(ScienceGradeRanker.class);
    
    private double factor;
    public void setFactor(double factor) {
        this.factor = factor;
    }
    
    private HashMap<SBRank,SchedBlock> ranks;
    
    /**
     * Create a new Science Grade Ranker
     * 
     */
    public ScienceGradeRanker(String rankerName){
        super(rankerName);
        ranks = new HashMap<SBRank, SchedBlock>();
    }

    @Override
    public SchedBlock getBestSB(List<SBRank> ranks) {
        return this.ranks.get(Collections.max(ranks));
    }

    @Override
    public List<SBRank> rank(List<SchedBlock> sbs, ArrayConfiguration arrConf, Date ut){
        ranks.clear();
        for(SchedBlock sb: sbs){
            SBRank rank = new SBRank();
            rank.setId(sb.getId());
            //sb.getParent();
            rank.setRank(sb.getProject().getScienceScore());
            ranks.put(rank, sb);
            logger.debug("rank: " + rank);
        }
        printVerboseInfo(ranks.keySet(), arrConf.getId(), ut);
        return new ArrayList<SBRank>(ranks.keySet());
    }

    @Override
    public String toString() {
        return "Science Grade Ranker";
    }


}
