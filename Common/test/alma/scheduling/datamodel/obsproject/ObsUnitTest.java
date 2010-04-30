package alma.scheduling.datamodel.obsproject;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alma.scheduling.persistence.HibernateUtil;

public class ObsUnitTest extends TestCase {

    private static Logger logger = LoggerFactory.getLogger(ObsProjectTest.class);
    private Session session;
    
    public ObsUnitTest(String name) {
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
 
    public void testObsUnit() throws Exception {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            // a simple SchedBlock
            SchedBlock sb = new SchedBlock();
            sb.setPiName("me");
            sb.setWeatherConstraints(new WeatherConstraints(0.0, 0.0, 0.0, 0.0));
            session.save(sb);
            // a ObsUnitSet containing several SchedBlock
            // children are saved by cascading
            ObsUnitSet ous = new ObsUnitSet();
            SchedBlock sb2 = new SchedBlock();
            sb2.setWeatherConstraints(new WeatherConstraints(1.0, 1.0, 1.0, 1.0));
            SchedBlock sb3 = new SchedBlock();
            sb3.setWeatherConstraints(new WeatherConstraints(2.0, 2.0, 2.0, 2.0));
            ous.addObsUnit(sb2);
            ous.addObsUnit(sb3);
            session.save(ous);
            tx.commit();
        } catch(Exception ex) {
            tx.rollback();
            throw ex;
        }
    }
    
    public void testObservingParameters() throws Exception {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            SchedBlock sb = new SchedBlock();
            sb.setPiName("me");
            sb.setWeatherConstraints(new WeatherConstraints(0.0, 0.0, 0.0, 0.0));
            ScienceParameters params = new ScienceParameters();
            params.setRepresentativeBandwidth(0.0);
            params.setRepresentativeFrequency(0.0);
            params.setSensitivityGoal(0.0);
            sb.addObservingParameters(params);
            session.save(sb);
            tx.commit();
        } catch(Exception ex) {
            tx.rollback();
            throw ex;
        }        
    }

    public void testPreconditions() throws Exception {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            SchedBlock sb = new SchedBlock();
            sb.setPiName("me");
            Preconditions preCond = new Preconditions(0.0, 0.0);
            sb.setPreConditions(preCond);
            session.save(sb);
            tx.commit();
        } catch(Exception ex) {
            tx.rollback();
            throw ex;
        }        
    }

    public void testSchedulingConstraints() throws Exception {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            SchedBlock sb = new SchedBlock();
            sb.setPiName("me");
            SchedulingConstraints constraints = new SchedulingConstraints(0.0, 0.0, null);
            sb.setSchedulingConstraints(constraints);
            session.save(sb);
            tx.commit();
        } catch(Exception ex) {
            tx.rollback();
            throw ex;
        }        
    }    
    
    public void testTargets() throws Exception {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            SchedBlock sb = new SchedBlock();
            sb.setPiName("me");
            Set<ObservingParameters> paramSet = new HashSet<ObservingParameters>();
            for (double i = 0.0; i < 4; i += 1.01) {
            	ScienceParameters params = new ScienceParameters();
            	params.setRepresentativeBandwidth(i);
            	params.setRepresentativeFrequency(i);
            	params.setSensitivityGoal(i);
            	paramSet.add(params);
            	sb.addObservingParameters(params);
            }
            session.save(sb);
            FieldSource src = new FieldSource("0000+000", new SkyCoordinates(0.0, 0.0), 0.0, 0.0);
            session.save(src);
            Target target = new Target(paramSet, src);
            sb.addTarget(target);
            session.saveOrUpdate(sb);
            tx.commit();
        } catch(Exception ex) {
            tx.rollback();
            throw ex;
        }        
    }
    
    public void testObsUnitControl() throws Exception {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            SchedBlock sb = new SchedBlock();
            sb.setPiName("me");
            ObsUnitControl ouc = new ObsUnitControl();
            ouc.setLastUpdate(new Date());
            ouc.setValidUntil(null);
            ouc.setMaximumTime(1.0);
            ouc.setEstimatedExecutionTime(0.5);
            ouc.setArrayRequested(ArrayType.TWELVE_M);
            sb.setObsUnitControl(ouc);
            SchedBlockControl sbc = new SchedBlockControl();
            sbc.setLastUpdate(new Date());
            sbc.setValidUntil(null);
            sbc.setExecutionCount(1);
            sbc.setIndefiniteRepeat(false);
            sbc.setState(SchedBlockState.FULLY_OBSERVED);
            sbc.setAchievedSensitivity(0.0);
            sb.setSchedBlockControl(sbc);
            session.save(sb);
            tx.commit();
        } catch(Exception ex) {
            tx.rollback();
            throw ex;
        }        
    }
    
    public void testObsProject() throws Exception {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            ObsProject prj = new ObsProject();
            prj.setScienceScore(3.14f);
            prj.setScienceRank(10);
            prj.setPrincipalInvestigator("me");
            prj.setStatus("ready");
            // a ObsUnitSet containing several SchedBlock
            // children are saved by cascading
            ObsUnitSet ous = new ObsUnitSet();
            SchedBlock sb2 = new SchedBlock();
            sb2.setWeatherConstraints(new WeatherConstraints(1.0, 1.0, 1.0, 1.0));
            SchedBlock sb3 = new SchedBlock();
            sb3.setWeatherConstraints(new WeatherConstraints(2.0, 2.0, 2.0, 2.0));
            ous.addObsUnit(sb2);
            ous.addObsUnit(sb3);
            prj.setObsUnit(ous);
            ous.setProject(prj);
            session.save(prj);
            logger.info("project status = " + ous.getProject().getStatus());
            tx.commit();
        } catch(Exception ex) {
            tx.rollback();
            throw ex;
        }
        try {
            tx = session.beginTransaction();
            Query query = session.createQuery("from ObsProject");
            ObsProject prj = (ObsProject) query.list().get(0);
            ObsUnit ous = prj.getObsUnit();
            assertNotNull(ous.getProject());
            tx.commit();
        } catch (Exception ex) {
            tx.rollback();
            throw ex;
        }
        
    }
}
