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
package alma.scheduling.algorithm.observatory;

import java.util.Collection;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alma.scheduling.algorithm.executive.ExecutiveSelector;
import alma.scheduling.algorithm.sbselection.AbstractBaseSelector;
import alma.scheduling.datamodel.observatory.ArrayConfiguration;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.dao.SchedBlockDao;

public class ArrayConfigurationSelector extends AbstractBaseSelector{
    
    public ArrayConfigurationSelector(String selectorName) {
        super(selectorName);
    }

    private static Logger logger = LoggerFactory.getLogger(ExecutiveSelector.class);
    
    private SchedBlockDao sbDao;
    
    public SchedBlockDao getSbDao() {
        return sbDao;
    }

    public void setSbDao(SchedBlockDao sbDao) {
        this.sbDao = sbDao;
    }

    @Override
    public Collection<SchedBlock> select(Date ut, ArrayConfiguration arrConf) {
        if (arrConf == null){
            logger.info("ArrayConfiguration is null, selecting all the SchedBlocks");
            return sbDao.findAll();
        }
        double remaningTime = (arrConf.getEndTime().getTime() - ut.getTime()) / (1000.0 * 60.0 * 60.0);
        Collection<SchedBlock> sbs = sbDao.findSchedBlocksByEstimatedExecutionTime(remaningTime);
        printVerboseInfo(sbs, arrConf.getId(), ut);
        return sbs;
    }
    
    @Override
    public String toString() {
        return getClass().toString();
    }

//    @Override
//    public Criterion getCriterion(Date ut, ArrayConfiguration arrConf) {
//        double remaningTime = (arrConf.getEndTime().getTime() - ut.getTime()) / (1000.0 * 60.0 * 60.0);
//        Criterion crit = Restrictions.le("obsUnitControl.estimatedExecutionTime", new Double(remaningTime));
//        return crit;
//    }
    

}
