package alma.scheduling.datamodel.obsproject;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alma.scheduling.persistence.HibernateUtil;

import junit.framework.TestCase;

public class FieldSourceTest extends TestCase {

    private static Logger logger = LoggerFactory.getLogger(FieldSourceTest.class);
    private Session session;
    
    public FieldSourceTest(String name) {
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
 
    public void testSimpleFieldSourceCreation() throws Exception {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            FieldSource src = new FieldSource("0000+000", new SkyCoordinates(0.0, 0.0), 0.0, 0.0);
            session.save(src);
            tx.commit();
        } catch(Exception ex) {
            tx.rollback();
            throw ex;
        }
    }
    
}
