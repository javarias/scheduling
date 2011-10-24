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
package alma.scheduling.test;

import java.util.ArrayList;

import junit.framework.TestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import alma.scheduling.algorithm.DynamicSchedulingAlgorithm;
import alma.scheduling.algorithm.sbselection.NoSbSelectedException;
import alma.scheduling.datamodel.GenericDao;
import alma.scheduling.datamodel.executive.dao.ExecutiveDAO;
import alma.scheduling.datamodel.obsproject.ObsProject;
import alma.scheduling.datamodel.obsproject.ObsUnitSet;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.WeatherConstraints;
import alma.scheduling.datamodel.obsproject.dao.SchedBlockDao;
import alma.scheduling.output.Reporter;

public class ExecutiveRankerTest extends TestCase {

    private static Logger logger = LoggerFactory.getLogger(ExecutiveRankerTest.class);
    
    public ExecutiveRankerTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        
    }
 
    private void populateDB(ExecutiveDAO xmlExecDao, GenericDao genDao){
        logger.info("Populating the DB with Exec data");
        ArrayList<Object> objs = new ArrayList<Object>();
        objs.addAll(xmlExecDao.getAllExecutive());
        objs.addAll(xmlExecDao.getAllObservingSeason());
        objs.addAll(xmlExecDao.getAllPi());
        genDao.saveOrUpdate(objs);
    }
    
    public void testExecutiveRanker(){
    	
        // Loading SchedBlocks
        ApplicationContext ctx = new ClassPathXmlApplicationContext(
                "context.xml");
        ExecutiveDAO execDao = (ExecutiveDAO) ctx.getBean("execDao");
        ExecutiveDAO xmlExecDao = (ExecutiveDAO) ctx.getBean("xmlExecDao");
        SchedBlockDao sbDao = (SchedBlockDao) ctx.getBean("sbDao");

        ObsProject prj = new ObsProject();
        prj.setScienceRank(1);
        prj.setScienceScore(new Float(1.0));
        prj.setPrincipalInvestigator("me");
        prj.setStatus("ready");
        
        sbDao.saveOrUpdate(prj);
        
        // a simple SchedBlock
        ObsUnitSet ous = new ObsUnitSet();
        SchedBlock sb1 = new SchedBlock();
        sb1.setPiName("1");
        sb1.setWeatherConstraints(new WeatherConstraints(0.0, 0.0, 0.0, 0.0));
        // a ObsUnitSet containing several SchedBlock
        // children are saved by cascading
        SchedBlock sb2 = new SchedBlock();
        sb2.setPiName("2");
        sb2.setWeatherConstraints(new WeatherConstraints(1.0, 1.0, 1.0, 1.0));
        //sbDao.saveOrUpdate(sb1);
       // sbDao.saveOrUpdate(sb2);
        ous.addObsUnit(sb1);
        ous.addObsUnit(sb2);
        sbDao.saveOrUpdate(ous);
        //Loading Executive data from XML
        
        populateDB(xmlExecDao, (GenericDao) execDao);
		
		DynamicSchedulingAlgorithm dsa = (DynamicSchedulingAlgorithm) ctx.getBean("dsa");
		Reporter rep = (Reporter) ctx.getBean("reporter");
		while(true){
		    try{
		        dsa.selectCandidateSB();
		        dsa.rankSchedBlocks();
		        SchedBlock sb = dsa.getSelectedSchedBlock();
		        logger.info("Selected an SB by DSA");
		        //Here SB should be executed
		        rep.report(sb);
		    }
		    catch(NoSbSelectedException ex){
		        //finish the execution of the DSA, we cannot get more SBs
		        break;
		    }
		}
    }
    

}
