package alma.scheduling.test;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.WeatherConstraints;
import alma.scheduling.persistence.HibernateUtil;
import junit.framework.TestCase;

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
    }
    

}
