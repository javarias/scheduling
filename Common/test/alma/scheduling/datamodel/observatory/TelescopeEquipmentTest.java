package alma.scheduling.datamodel.observatory;

import java.util.Date;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alma.scheduling.persistence.HibernateUtil;

import junit.framework.TestCase;

public class TelescopeEquipmentTest extends TestCase {

    private static Logger logger = LoggerFactory.getLogger(TelescopeEquipmentTest.class);
    private Session session;
    
    public TelescopeEquipmentTest(String name) {
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
 
    public void testTelescopeEquipmentCreation() throws Exception {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            // Add an antenna with its subequipment
            Antenna a = new Antenna("DV01", new Date());
            a.setAntennaType("Vertex");
            a.setDiameter(4.0);
            a.setEffectiveCollectingArea(3.14);
            a.setSystemTemperature(15.0);
            AssemblyContainer ber = new AssemblyContainer(AssemblyGroupType.BACKEND_ANALOG_RACK, new Date());
            a.addAssemblyGroup(ber);
            TelescopeEquipment fe = new TelescopeEquipment("FE1", AssemblyGroupType.FRONTEND, new Date());
            AssemblyContainer r = new AssemblyContainer(AssemblyGroupType.RECEIVER, new Date());
            r.getOperations().add(new AssemblyContainerOperation(new Date(), AssemblyOperation.ADD_ALL));
            fe.addAssemblyGroup(r);
            a.addAssemblyGroup(fe);
            a.getOperations().add(new AssemblyContainerOperation(new Date(), AssemblyOperation.ADD_ALL));
            session.save(a);
            // Add a pad
            Pad p = new Pad("TFX01", new Date(), 10.0, 11.0, 12.0);
            session.save(p);
            // Define an array configuration
            ArrayConfiguration ac = new ArrayConfiguration();
            ac.setStartTime(new Date());
            ac.setEndTime(new Date());
            ac.setResolution(0.0);
            ac.setUvCoverage(0.0);
            AntennaInstallation ai = new AntennaInstallation(a, p, new Date(), new Date());
            ac.getAntennaInstallations().add(ai);
            session.save(ac);
            // Update the state of the antenna
            AssemblyContainerState es = new AssemblyContainerState();
            es.getAssemblies().add(AssemblyType.LFRD);
            es.getAssemblies().add(AssemblyType.IFProc);
            es.getAssemblies().add(AssemblyType.ColdCart);
            a.setState(es);
            session.saveOrUpdate(a);
            tx.commit();
        } catch(Exception ex) {
            tx.rollback();
            throw ex;
        }
    }
    
}
