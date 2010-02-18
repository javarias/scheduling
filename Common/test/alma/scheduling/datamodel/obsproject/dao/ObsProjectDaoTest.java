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
import alma.scheduling.datamodel.obsproject.WeatherConstraints;

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
 
    public void testXmlObsProjectDao() throws Exception {
        ApplicationContext ctx =
            new ClassPathXmlApplicationContext("alma/scheduling/datamodel/obsproject/dao/context.xml");
        ObsProjectDao dao = (ObsProjectDao) ctx.getBean("obsProjectDao");
        List<ObsProject> projects = dao.getAllObsProjects();
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
    }
    
}
