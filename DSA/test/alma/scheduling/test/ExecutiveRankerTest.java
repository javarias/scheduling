package alma.scheduling.test;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alma.scheduling.algorithm.DynamicSchedulingAlgorithm;
import alma.scheduling.datamodel.BeanFactory;
import alma.scheduling.datamodel.executive.Executive;
import alma.scheduling.datamodel.executive.ExecutivePercentage;
import alma.scheduling.datamodel.executive.ObservingSeason;
import alma.scheduling.datamodel.executive.PI;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.WeatherConstraints;
import alma.scheduling.input.executive.generated.ExecutiveData;
import alma.scheduling.persistence.HibernateUtil;
import alma.scheduling.persistence.XmlUtil;
import junit.framework.TestCase;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.xml.sax.SAXException;

public class ExecutiveRankerTest extends TestCase {

    private static Logger logger = LoggerFactory.getLogger(ExecutiveRankerTest.class);
    private Session session;
    
    public ExecutiveRankerTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
        session = HibernateUtil.getSessionFactory().openSession();
    }

    protected void tearDown() throws Exception {
        session.close();
        HibernateUtil.shutdown();        
        super.tearDown();
    }
 
    public void testExecutiveRanker() throws Exception {
    	
    	//Loading SchedBlocks
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            // a simple SchedBlock
            SchedBlock sb1 = new SchedBlock();
            sb1.setPiName("Astronomer from Salsacia");
            sb1.setWeatherConstraints(new WeatherConstraints(0.0, 0.0, 0.0, 0.0));
            session.save(sb1);
            // a ObsUnitSet containing several SchedBlock
            // children are saved by cascading
            SchedBlock sb2 = new SchedBlock();
            sb2.setPiName("Astronomer from Conservia");
            sb2.setWeatherConstraints(new WeatherConstraints(1.0, 1.0, 1.0, 1.0));
            session.save(sb2);
            tx.commit();
        } catch(Exception ex) {
            tx.rollback();
            throw ex;
        }
        
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
        
        try {
            tx = session.beginTransaction();
            for(ObservingSeason tmp: osOut)
                session.save(tmp);
            for(Executive tmp: execOut)
                session.save(tmp);
            for(PI tmp: piOut)
                session.save(tmp);
            tx.commit();
        } catch(Exception ex) {
            ex.printStackTrace();
            tx.rollback();
            throw ex;
        }
        
        //Starting DSA
		ApplicationContext ctx = new ClassPathXmlApplicationContext("context.xml");
		
		DynamicSchedulingAlgorithm dsa = (DynamicSchedulingAlgorithm) ctx.getBean("dsa");
		dsa.selectCandidateSB();
    }
    

}
