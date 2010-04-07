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
 * "@(#) $Id: ExecutiveSelector.java,v 1.1 2010/04/07 21:41:58 javarias Exp $"
 */
package alma.scheduling.algorithm.executive;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import alma.scheduling.algorithm.VerboseLevel;
import alma.scheduling.algorithm.sbselection.AbstractBaseSelector;
import alma.scheduling.algorithm.sbselection.NoSbSelectedException;
import alma.scheduling.datamodel.GenericDao;
import alma.scheduling.datamodel.executive.Executive;
import alma.scheduling.datamodel.executive.ExecutivePercentage;
import alma.scheduling.datamodel.executive.ExecutiveTimeSpent;
import alma.scheduling.datamodel.executive.ObservingSeason;
import alma.scheduling.datamodel.executive.PI;
import alma.scheduling.datamodel.executive.dao.ExecutiveDAO;
import alma.scheduling.datamodel.observatory.ArrayConfiguration;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.dao.SchedBlockDao;

public class ExecutiveSelector extends AbstractBaseSelector {
    private static Logger logger = LoggerFactory.getLogger(ExecutiveSelector.class);
    
    private ExecutiveDAO execDao;
    private SchedBlockDao sbDao;
    private HashMap<String, Double> availableTime;
    
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
    public Collection<SchedBlock> select(Date ut) throws NoSbSelectedException {
        // ut time is ignored
        return select();
    }

    @Override
    public Collection<SchedBlock> select(ArrayConfiguration arrConf) throws NoSbSelectedException {
        // ut time is ignored
        return select();
    }

    @Override
    public Collection<SchedBlock> select(Date ut, ArrayConfiguration arrConf) throws NoSbSelectedException {
        // ut time is ignored
        Collection<SchedBlock> sbs = select();
        if (verboseLvl != VerboseLevel.NONE)
            System.out.println("[" + ut.toString() + "]"
                    + getVerboseLine(sbs, arrConf.getId()));
        return sbs;
    }
    
    /*
     *
     * (non-Javadoc)
     * @see alma.scheduling.algorithm.sbselection.SchedBlockSelector#select()
     */
    @Override
    @Transactional(readOnly=true)
    public Collection<SchedBlock> select() throws NoSbSelectedException{
        logger.trace("entering");
        calculateRemainingTime();
        List<SchedBlock> acceptedSbs =  new ArrayList<SchedBlock>();
        List<SchedBlock> sbs =  sbDao.findAll(SchedBlock.class);
        for(SchedBlock sb: sbs){
            //TODO: replace the next line with a new method defined in ExecutiveDAO
            PI pi = ((GenericDao)execDao).findById(PI.class, sb.getPiName());
            Double avTime = availableTime.get(execDao.getExecutive(pi.getName()).getName());
            if (avTime.doubleValue() >= sb.getObsUnitControl().getEstimatedExecutionTime().doubleValue())
                acceptedSbs.add(sb);
        }
        if(acceptedSbs.size() == 0){
            String strCause = "Cannot get any SB valid to be ranked using " + this.toString();
            throw new NoSbSelectedException(strCause);
        }
        logger.info("# SchedBlocks selected: " + acceptedSbs.size());
        return acceptedSbs;
    }
    
    private void calculateRemainingTime(){
        if (availableTime == null)
            availableTime = new HashMap<String, Double>();
        availableTime.clear();
        List<Executive> execs = execDao.getAllExecutive();
        ObservingSeason currOs = execDao.getCurrentSeason();
        for(Executive exec: execs){
            ExecutivePercentage ep = execDao.getExecutivePercentage(exec, currOs);
            List<ExecutiveTimeSpent> etss = 
                execDao.getExecutiveTimeSpent(exec, currOs);
            double spentTime = 0;
            for(ExecutiveTimeSpent ets: etss){
                spentTime += ets.getTimeSpent();
            }
            availableTime.put(exec.getName(), 
                    new Double(ep.getTotalObsTimeForSeason() - spentTime));
        }
        
    }

    @Override
    public String toString() {
        return "ExecutiveSelector";
    }
    
}
