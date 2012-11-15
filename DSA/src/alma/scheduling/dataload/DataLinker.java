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
package alma.scheduling.dataload;

import java.util.HashSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import alma.scheduling.datamodel.config.Configuration;
import alma.scheduling.datamodel.config.ScienceGradeConfig.InvalidScienceGradeConfig;
import alma.scheduling.datamodel.config.dao.ConfigurationDao;
import alma.scheduling.datamodel.executive.PI;
import alma.scheduling.datamodel.executive.PIMembership;
import alma.scheduling.datamodel.executive.dao.ExecutiveDAO;
import alma.scheduling.datamodel.obsproject.ObsProject;
import alma.scheduling.datamodel.obsproject.ObsUnit;
import alma.scheduling.datamodel.obsproject.ObsUnitSet;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.SchedBlockState;
import alma.scheduling.datamodel.obsproject.ScienceGrade;
import alma.scheduling.datamodel.obsproject.dao.ObsProjectDao;
import alma.scheduling.datamodel.obsproject.dao.SchedBlockDao;

public class DataLinker implements DataLoader {

	private static Logger logger = LoggerFactory.getLogger(DataLinker.class);
	
    private SchedBlockDao sbDao;
    private ExecutiveDAO execDao;
    private ConfigurationDao configDao;
    private ObsProjectDao obsPrjDao;
    
    public ConfigurationDao getConfigDao() {
        return configDao;
    }

    public void setConfigDao(ConfigurationDao configDao) {
        this.configDao = configDao;
    }

    public SchedBlockDao getSbDao() {
        return sbDao;
    }

    public void setSbDao(SchedBlockDao sbDao) {
        this.sbDao = sbDao;
    }

    public ExecutiveDAO getExecDao() {
        return execDao;
    }

    public void setExecDao(ExecutiveDAO execDao) {
        this.execDao = execDao;
    }

    public ObsProjectDao getObsPrjDao() {
        return obsPrjDao;
    }

    public void setObsPrjDao(ObsProjectDao obsPrjDao) {
        this.obsPrjDao = obsPrjDao;
    }

    @Override
    public void clear() {
        //Nothing to do with clear method
    }

    @Override
    @Transactional( readOnly=false, propagation=Propagation.MANDATORY, isolation=Isolation.SERIALIZABLE)
    public void load() throws InvalidScienceGradeConfig {
        List<ObsProject>prjs = obsPrjDao.getObsProjectsOrderBySciRank();
        long i = 0;
        for(ObsProject p: prjs){
            if (p.getLetterGrade() != null)
                continue;
            if(i < configDao.getConfiguration().getScienceGradeConfig().getnGradeAPrj())
                p.setLetterGrade(ScienceGrade.A);
            else if (i >=  configDao.getConfiguration().getScienceGradeConfig().getnGradeAPrj() && 
                    i < configDao.getConfiguration().getScienceGradeConfig().getnGradeAPrj() + configDao.getConfiguration().getScienceGradeConfig().getnGradeBPrj())
                p.setLetterGrade(ScienceGrade.B);
            else if (i >= configDao.getConfiguration().getScienceGradeConfig().getnGradeBPrj() && 
                    i < configDao.getConfiguration().getScienceGradeConfig().getnGradeAPrj() + configDao.getConfiguration().getScienceGradeConfig().getnGradeBPrj() + configDao.getConfiguration().getScienceGradeConfig().getnGradeCPrj())
                p.setLetterGrade(ScienceGrade.C);
            else
                p.setLetterGrade(ScienceGrade.D);
            i++;
        }
        obsPrjDao.saveOrUpdate(prjs);
        
        
        List<SchedBlock>sbs = sbDao.findAll();
        for (SchedBlock sb : sbs) {
//            if (sb.getPiName() == null) {
//              System.out.println("sb.getPiName() = " + sb.getPiName());
                PI pi = execDao.getPIFromEmail(sb.getPiName());
                sb.setExecutive(pi.getPIMembership().iterator().next()
                        .getExecutive());
//            }
            ObsProject p = obsPrjDao.getObsProject(sb);
            sb.setScienceScore(p.getScienceScore());
            sb.setLetterGrade(p.getLetterGrade());
            sb.setScienceRank(p.getScienceRank());
            if(p.getStatus().compareTo("CANCELLED") == 0)
                sb.getSchedBlockControl().setState(SchedBlockState.CANCELED);
        }
        sbDao.saveOrUpdate(sbs);
        
//        Configuration config = configDao.getConfiguration();
//        config.getScienceGradeConfig().setTotalPrj(sbs.size());
//        config.getScienceGradeConfig()
//                .setnGradeDPrj(
//                        config.getScienceGradeConfig().getTotalPrj()
//                                - (config.getScienceGradeConfig().getnGradeAPrj()
//                                   + config.getScienceGradeConfig().getnGradeBPrj() 
//                                   + config.getScienceGradeConfig().getnGradeCPrj()));
//        config.getScienceGradeConfig().testValues();
//        configDao.updateConfig();
    }
}
