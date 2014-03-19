package alma.scheduling.datamodel.observation;

import java.util.Date;

import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.classic.Session;

import junit.framework.TestCase;

public class MappingTest extends TestCase {
	
	private Configuration getConfiguration() {
		Configuration config = new Configuration();
		config.setProperty("hibernate.connection.url", "jdbc:hsqldb:mem:test");
		config.setProperty("hibernate.connection.username", "sa");
		config.setProperty("hibernate.connection.password", "");
		config.setProperty("hibernate.connection.driver_class", "org.hsqldb.jdbcDriver");
		config.setProperty("hibernate.cache.provider_class", "org.hibernate.cache.NoCacheProvider");
		config.setProperty("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
		config.setProperty("hibernate.hbm2ddl.auto", "update");
		config.setProperty("hibernate.show_sql", "true");
		config.setProperty("hibernate.current_session_context_class", "thread");
		config.setProperty("hibernate.connection.pool_size", "3");
		//c3p0 stuff
		config.setProperty("c3p0.min_size", "3");
		config.setProperty("c3p0.max_size", "10");
		config.setProperty("c3p0.timeout", "60");
		//mapping
		config.addResource("alma/scheduling/datamodel/observation/Observation.hbm.xml");
		return config;
	}
	
	public void testMapping() throws Exception {
		Configuration config = getConfiguration();
		SessionFactory sf = config.buildSessionFactory();
		
		ExecBlock eb = new ExecBlock();
		eb.setExecBlockUid("uid://A001/X0000/X0000");
		eb.setSchedBlockUid("uid://A001/X0000/X0001");
		eb.setStatus(ExecStatus.SUCCESS);
		eb.setTimeOnCalibration(1.0);
		eb.setStartTime(new Date());
		eb.setSensitivityAchieved(0.01);
		eb.setEndTime(new Date());
		
		Session s = sf.openSession();
		
		Transaction tx = s.beginTransaction();
		s.saveOrUpdate(eb);
		tx.commit();
		s.close();
		sf.close();
		
		sf = config.buildSessionFactory();
		s = sf.openSession();
		System.out.println(s.createQuery("select eb from ExecBlock as eb").list().size());
		eb = (ExecBlock) s.createQuery("select eb from ExecBlock as eb").uniqueResult();
		assertEquals("uid://A001/X0000/X0000", eb.getExecBlockUid());
	}
	
	
}
