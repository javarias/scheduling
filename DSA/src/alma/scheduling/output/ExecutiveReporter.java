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
package alma.scheduling.output;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alma.scheduling.datamodel.executive.ExecutiveTimeSpent;
import alma.scheduling.datamodel.executive.dao.ExecutiveDAO;
import alma.scheduling.datamodel.obsproject.SchedBlock;

public class ExecutiveReporter implements Reporter {

    private static Logger logger = LoggerFactory.getLogger(ExecutiveReporter.class);
    
    private ExecutiveDAO execDao;
    
    public ExecutiveDAO getExecDao() {
        return execDao;
    }

    public void setExecDao(ExecutiveDAO execDao) {
        this.execDao = execDao;
    }

    @Override
    public void generateXMLOutput() {
        // TODO Auto-generated method stub
        // Not implemented?
    }

    @Override
    public void report(SchedBlock schedBlock) {
        ExecutiveTimeSpent ets = new ExecutiveTimeSpent();
        ets.setExecutive(execDao.getExecutive(schedBlock.getPiName()));
        ets.setObservingSeason(execDao.getCurrentSeason());
        ets.setSbUid(schedBlock.getUid());
        ets.setTimeSpent(schedBlock.getObsUnitControl().getEstimatedExecutionTime().floatValue());
        execDao.saveOrUpdate(ets);
    }

}
