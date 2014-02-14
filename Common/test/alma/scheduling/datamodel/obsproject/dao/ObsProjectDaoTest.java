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
package alma.scheduling.datamodel.obsproject.dao;

import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import alma.scheduling.datamodel.obsproject.ObsProject;
import alma.scheduling.datamodel.obsproject.ObsUnit;
import alma.scheduling.datamodel.obsproject.ObsUnitSet;
import alma.scheduling.datamodel.obsproject.SchedBlock;

public class ObsProjectDaoTest extends TestCase {

    private static Logger logger = LoggerFactory.getLogger(ObsProjectDaoTest.class);
    
    public ObsProjectDaoTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
 
    public void notestXmlObsProjectDao() throws Exception {
        ApplicationContext ctx =
            new ClassPathXmlApplicationContext("alma/scheduling/datamodel/obsproject/dao/context.xml");
        XmlObsProjectDao xmlDao = (XmlObsProjectDao) ctx.getBean("xmlObsProjectDao");
        ObsProjectDao dao = (ObsProjectDao) ctx.getBean("obsProjectDao");
        List<ObsProject> projects = xmlDao.getAllObsProjects();
        assertNotNull(projects);
        assertEquals(1, projects.size());
        ObsProject obsProject = projects.get(0);
        logger.info("principal investigator: " + obsProject.getPrincipalInvestigator());
        ObsUnitSet obsUnitSet = (ObsUnitSet) obsProject.getObsUnit();
        logger.info("# obsunits: " + obsUnitSet.getObsUnits().size());
        for (Iterator<ObsUnit> iter = obsUnitSet.getObsUnits().iterator(); iter.hasNext();) {
            ObsUnit unit = iter.next();
            if (unit instanceof SchedBlock) {
                logger.info("ObsUnit is a SchedBlock");
            } else if (unit instanceof ObsUnitSet) {
                logger.info("ObsUnit is a ObsUnitSet");
            }
        }
        dao.saveOrUpdate(obsProject);
    }
    
    public void testRoundTrip() throws Exception {
        ApplicationContext ctx =
            new ClassPathXmlApplicationContext("alma/scheduling/datamodel/obsproject/dao/context.xml");
        XmlObsProjectDao xmlDao = (XmlObsProjectDao) ctx.getBean("xmlObsProjectDao");
        ObsProjectDao dao = (ObsProjectDao) ctx.getBean("obsProjectDao");
        List<ObsProject> projects = xmlDao.getAllObsProjects();
        for (ObsProject prj : projects) {
            ObsUnit ou = prj.getObsUnit();
            if (ou instanceof ObsUnitSet) {
                ObsUnitSet ous = (ObsUnitSet) ou;
                for (ObsUnit sou : ous.getObsUnits()) {
                    if (sou instanceof SchedBlock) {
                        SchedBlock sb = (SchedBlock) sou;
                        logger.debug("# of observing parameters: " +
                                sb.getObservingParameters().size());                        
                    }
                }
            }
        }
        dao.saveOrUpdate(projects);
        for (ObsProject prj : projects) {
            xmlDao.saveObsProject(prj);
        }
    }
    
}
