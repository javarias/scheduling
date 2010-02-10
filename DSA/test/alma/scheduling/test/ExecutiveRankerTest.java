package alma.scheduling.test;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alma.scheduling.algorithm.DynamicSchedulingAlgorithm;
import alma.scheduling.datamodel.BeanFactory;
import alma.scheduling.datamodel.executive.Executive;
import alma.scheduling.datamodel.executive.ExecutivePercentage;
import alma.scheduling.datamodel.executive.ObservingSeason;
import alma.scheduling.datamodel.executive.PI;
import alma.scheduling.datamodel.executive.dao.ExecutiveDAO;
import alma.scheduling.datamodel.obsproject.ObsProject;
import alma.scheduling.datamodel.obsproject.ObsUnitSet;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.WeatherConstraints;
import alma.scheduling.datamodel.obsproject.dao.SchedBlockDao;
import alma.scheduling.input.executive.generated.ExecutiveData;
import junit.framework.TestCase;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

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
 
    public void testExecutiveRanker() throws Exception {
    	
        // Loading SchedBlocks
        ApplicationContext ctx = new ClassPathXmlApplicationContext(
                "context.xml");
        ExecutiveDAO execDao = (ExecutiveDAO) ctx.getBean("execDao");
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
        ExecutiveData execData = null;
        try {
            execData = ExecutiveData.unmarshalExecutiveData(
                    new FileReader("./projects/SimpleDSATest01/executive/executive.xml"));
        } catch (MarshalException e) {
            e.printStackTrace();
        } catch (ValidationException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        
        //Loading Executive data from castor objects to hibernate
        ArrayList<Executive> execOut =  new ArrayList<Executive>();
        ArrayList<PI> piOut =  new ArrayList<PI>();
        ArrayList<ExecutivePercentage> epOut =  new ArrayList<ExecutivePercentage>();
        ArrayList<ObservingSeason> osOut = new ArrayList<ObservingSeason>();
        
        BeanFactory.copyExecutiveFromXMLGenerated(execData, execOut, piOut, epOut, osOut);
        
        execDao.PopulateDB(piOut, execOut, osOut);
		
		DynamicSchedulingAlgorithm dsa = (DynamicSchedulingAlgorithm) ctx.getBean("dsa");
		dsa.selectCandidateSB();
    }
    

}
