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
 * "@(#) $Id: SchedBlockExecutorImpl.java,v 1.1 2010/03/13 02:56:15 rhiriart Exp $"
 */
package alma.scheduling.algorithm;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alma.scheduling.datamodel.GenericDao;
import alma.scheduling.datamodel.executive.ExecutiveTimeSpent;
import alma.scheduling.datamodel.executive.dao.ExecutiveDAO;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.dao.SchedBlockDao;

public class SchedBlockExecutorImpl implements SchedBlockExecutor {

    private static Logger logger = LoggerFactory.getLogger(SchedBlockExecutorImpl.class);

    private ExecutiveDAO execDao;
    public void setExecDao(ExecutiveDAO execDao) {
        this.execDao = execDao;
    }
    
    private SchedBlockDao schedBlockDao;
    public void setSchedBlockDao(SchedBlockDao schedBlockDao) {
        this.schedBlockDao = schedBlockDao;
    }
    
    @Override
    public Date execute(SchedBlock schedBlock, Date ut) {
        ExecutiveTimeSpent ets = new ExecutiveTimeSpent();
        ets.setExecutive(execDao.getExecutive(schedBlock.getPiName()));
        ets.setObservingSeason(execDao.getCurrentSeason());
        ets.setSbId(schedBlock.getId());
        ets.setTimeSpent(schedBlock.getObsUnitControl().getEstimatedExecutionTime().floatValue());
        ((GenericDao) execDao).saveOrUpdate(ets);
        
        // TODO calculate achieved sensitivity
        schedBlock.getSchedBlockControl().setAchievedSensitivity(3.14);
        schedBlockDao.saveOrUpdate(schedBlock);
        
        long executionTime = (long) schedBlock.getObsUnitControl().getEstimatedExecutionTime().floatValue()
            * 3600 * 1000;
        Date nextExecutionTime = new Date(ut.getTime() + executionTime);
        return nextExecutionTime;
    }
}
