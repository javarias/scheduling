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
 * "@(#) $Id: ExecutiveSelector.java,v 1.7 2010/07/20 23:07:00 javarias Exp $"
 */
package alma.scheduling.algorithm.executive;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alma.scheduling.algorithm.sbselection.AbstractBaseSelector;
import alma.scheduling.algorithm.sbselection.NoSbSelectedException;
import alma.scheduling.datamodel.executive.Executive;
import alma.scheduling.datamodel.executive.ObservingSeason;
import alma.scheduling.datamodel.executive.dao.ExecutiveDAO;
import alma.scheduling.datamodel.observatory.ArrayConfiguration;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.dao.SchedBlockDao;

public class ExecutiveSelector extends AbstractBaseSelector {
    private static Logger logger = LoggerFactory
            .getLogger(ExecutiveSelector.class);

    private ExecutiveDAO execDao;
    private SchedBlockDao sbDao;

    public ExecutiveSelector(String selectorName) {
        super(selectorName);
    }

    public ExecutiveDAO getExecDao() {
        return execDao;
    }

    public void setExecDao(ExecutiveDAO execDao) {
        this.execDao = execDao;
    }

    public SchedBlockDao getSbDao() {
        return sbDao;
    }

    public void setSbDao(SchedBlockDao sbDao) {
        this.sbDao = sbDao;
    }

    @Override
    public Collection<SchedBlock> select(Date ut, ArrayConfiguration arrConf)
            throws NoSbSelectedException {
        // ut and arrConf are ignored
        logger.trace("entering");
        List<SchedBlock> acceptedSbs = new ArrayList<SchedBlock>();
        Collection<Executive> execs = execDao.getAllExecutive();
        for (Executive e : execs) {
            List<SchedBlock> sbs = sbDao
                    .findSchedBlocksWithEnoughTimeInExecutive(e, execDao
                            .getCurrentSeason());
            acceptedSbs.addAll(sbs);
        }
        logger.info("# SchedBlocks selected: " + acceptedSbs.size());
        printVerboseInfo(acceptedSbs, arrConf.getId(), ut);
        return acceptedSbs;
    }


//    @Override
//    public Criterion getCriterion(Date ut, ArrayConfiguration arrConf) {
//        /*        query = getSession()
//                .createQuery(
//                        "select sb from SchedBlock sb, PI pi join pi.PIMembership pim, "
//                                + "Executive e join e.executivePercentage ep "
//                                + "where sb.piName = pi.email and "
//                                + "pim.executive = ? and "
//                                + "ep.executive = ? and "
//                                + "ep.season = ? and "
//                                + "ep.totalObsTimeForSeason - sb.obsUnitControl.estimatedExecutionTime >= "
//                                + timeSpent.toString());
//        query.setParameter(0, exec);
//        query.setParameter(1, exec);
//        query.setParameter(2, os);*/
//        //List<Executive> execs = execDao.getAllExecutive();
//        ObservingSeason os = execDao.getCurrentSeason();
//        Conjunction conj = Restrictions.conjunction(); 
//        conj.add(Restrictions.eq("ep.season", os));
//        conj.add(Restrictions.leProperty("obsUnitControl.estimatedExecutionTime", "ep.remainingObsTime"));
//        return conj;
//    }

}
