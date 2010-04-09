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
 * "@(#) $Id: ExecutiveDaoTest.java,v 1.1 2010/04/09 01:26:15 rhiriart Exp $"
 */
package alma.scheduling.datamodel.executive.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.transaction.annotation.Transactional;

import alma.scheduling.datamodel.executive.Executive;
import alma.scheduling.datamodel.executive.ExecutivePercentage;
import alma.scheduling.datamodel.executive.ObservingSeason;
import alma.scheduling.datamodel.executive.PI;

/**
 * Test the Executive DAOs.
 * <P>
 * This test uses Spring on top of Hibernate.
 */
public class ExecutiveDaoTest extends TestCase {

    private static Logger logger = LoggerFactory.getLogger(ExecutiveDaoTest.class);
    
    public ExecutiveDaoTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
 
    @Transactional
    public void testXmlObsProjectDao() throws Exception {
        ApplicationContext ctx =
            new ClassPathXmlApplicationContext("alma/scheduling/datamodel/executive/dao/context.xml");
        logger.info("getting DAOs");
        XmlExecutiveDAO xmldao = (XmlExecutiveDAO) ctx.getBean("xmlExecDao");
        List<Executive> executives = xmldao.getAllExecutive();
        logger.info("# of executives: " + executives.size());
        assertEquals(3, executives.size());
        List<ObservingSeason> seasons = xmldao.getAllObservingSeason();
        logger.info("# of observing seasons: " + seasons.size());
        assertEquals(1, seasons.size());
        List<PI> pis = xmldao.getAllPi();
        logger.info("# of PIs: " + pis.size());
        assertEquals(3, pis.size());
        for (Executive exec : executives) {
            logger.info("executive name: " + exec.getName());
            for (ExecutivePercentage execPercent : exec.getExecutivePercentage()) {
                logger.info("exective percent total obs time: " + execPercent.getTotalObsTimeForSeason());
            }
        }
        
        ExecutiveDAO dao = (ExecutiveDAO) ctx.getBean("execDao");
        
        dao.saveObservingSeasons(seasons);
        
        dao.saveOrUpdate(pis);
    }
    
}
