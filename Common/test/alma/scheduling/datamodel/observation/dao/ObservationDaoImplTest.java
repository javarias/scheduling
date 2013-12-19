package alma.scheduling.datamodel.observation.dao;

import java.util.UUID;

import org.springframework.context.ApplicationContext;

import alma.scheduling.datamodel.observation.ExecBlock;
import alma.scheduling.utils.CommonContextFactory;
import junit.framework.TestCase;
import static alma.scheduling.utils.CommonContextFactory.SCHEDULING_OBSERVATION_DAO_BEAN;

public class ObservationDaoImplTest extends TestCase {

	private ApplicationContext ctx;
	private ObservationDao obsDao;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		ctx = CommonContextFactory.getContext();
		obsDao = ctx.getBean(SCHEDULING_OBSERVATION_DAO_BEAN, ObservationDao.class); 
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testGetNumberOfExecutionsForSb() throws Exception {
		final String sbuid = "uid://A000/X0/X0";
		final String sbuid2 = "uid://A000/X1/X0";
		assertEquals(0, obsDao.getNumberOfExecutionsForSb(sbuid));
		int n = 50;
		for (int i = 0; i < n; i++) {
			ExecBlock eb = new ExecBlock();
			eb.setExecBlockUid(UUID.randomUUID().toString());
			eb.setSchedBlockUid(sbuid);
			obsDao.save(eb);
		}
		for (int i = 0; i < n; i++) {
			ExecBlock eb = new ExecBlock();
			eb.setExecBlockUid(UUID.randomUUID().toString());
			eb.setSchedBlockUid(sbuid2);
			obsDao.save(eb);
		}
		
		assertEquals(n, obsDao.getNumberOfExecutionsForSb(sbuid));
	}
	
	
	public void testGetAccumulatedObservingTimeForSb() throws Exception {
		double obsTime = 2D;
		final String sbuid = "uid://A001/X0/X0";
		final String sbuid2 = "uid://A001/X1/X0";
		assertEquals(0.0, obsDao.getAccumulatedObservingTimeForSb(sbuid2));
		int n = 50;
		for (int i = 0; i < n; i++) {
			ExecBlock eb = new ExecBlock();
			eb.setExecBlockUid(UUID.randomUUID().toString());
			eb.setSchedBlockUid(sbuid);
			eb.setTimeOnSource(obsTime);
			obsDao.save(eb);
		}
		for (int i = 0; i < n; i++) {
			ExecBlock eb = new ExecBlock();
			eb.setExecBlockUid(UUID.randomUUID().toString());
			eb.setSchedBlockUid(sbuid2);
			eb.setTimeOnSource(obsTime);
			obsDao.save(eb);
		}
		
		assertEquals(obsTime * n, obsDao.getAccumulatedObservingTimeForSb(sbuid2));
	}
}
