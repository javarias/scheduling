package alma.scheduling.test;

import java.util.ArrayList;

import junit.framework.TestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.transaction.annotation.Transactional;

import alma.scheduling.algorithm.DynamicSchedulingAlgorithm;
import alma.scheduling.datamodel.GenericDao;
import alma.scheduling.datamodel.executive.dao.ExecutiveDAO;
import alma.scheduling.datamodel.obsproject.ObsProject;
import alma.scheduling.datamodel.obsproject.ObsUnitSet;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.WeatherConstraints;
import alma.scheduling.datamodel.obsproject.dao.SchedBlockDao;

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
    
    public void testExecutiveRanker() throws Exception {
    	
        // Loading SchedBlocks
        ApplicationContext ctx = new ClassPathXmlApplicationContext(
                "context.xml");
        ExecutiveDAO execDao = (ExecutiveDAO) ctx.getBean("execDao");
        ExecutiveDAO xmlExecDao = (ExecutiveDAO) ctx.getBean("xmlExecDao");
        SchedBlockDao sbDao = (SchedBlockDao) ctx.getBean("sbDao");

        ObsProject prj = new ObsProject();
        prj.setAssignedPriority(1);
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
		dsa.selectCandidateSB();
		dsa.rankSchedBlocks();
		SchedBlock sb = dsa.getSelectedSchedBlock();
		System.out.println(sb);
    }
    

}
