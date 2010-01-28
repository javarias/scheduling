package alma.scheduling.datamodel.obsproject;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alma.scheduling.persistence.HibernateUtil;

import junit.framework.TestCase;

public class ObsProjectTest extends TestCase {

    private static Logger logger = LoggerFactory.getLogger(ObsProjectTest.class);
    private Session session;
    
    public ObsProjectTest(String name) {
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
 
    public void testObsProject() {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            ObsProject prj = new ObsProject();
            prj.setAssignedPriority(1);
            prj.setPrincipalInvestigator("me");
            prj.setStatus("ready");
            session.save(prj);
            tx.commit();
        } catch(Exception ex) {
            tx.rollback();
        }
    }
    
}
