package alma.scheduling.datamodel.weather;

import junit.framework.TestCase;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alma.scheduling.persistence.HibernateUtil;

public class AtmParametersTest extends TestCase {

    private static Logger logger = LoggerFactory.getLogger(AtmParametersTest.class);
    private Session session;
    
    public AtmParametersTest(String name) {
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
 
    public void testSimpleRecordCreation() throws Exception {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            AtmParameters p1 = new AtmParameters();
            p1.setPWV(0.0);
            p1.setFreq(0.0);
            p1.setOpacity(0.0);
            p1.setAtmBrightnessTemp(0.0);
            session.save(p1);
            tx.commit();
        } catch(Exception ex) {
            tx.rollback();
            throw ex;
        }
        try {
            tx = session.beginTransaction();
            session.createQuery("DELETE FROM AtmParameters").executeUpdate();
            tx.commit();            
        } catch(Exception ex) {
            tx.rollback();
            throw ex;
        }
    }    
}
