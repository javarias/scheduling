package alma.scheduling.test;

import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import alma.scheduling.algorithm.MyLittleScoreAlg;
import alma.scheduling.datamodel.Entity;
import alma.scheduling.datamodel.dao.EntityDao;
import alma.scheduling.util.DbUtils;

public class DsaTest {
	
	
	public static void main (String args[]){
		
		try {
			DbUtils.createTables();
		} catch (Exception ex) {
			//Table already created
		}
		
		ApplicationContext ctx = new ClassPathXmlApplicationContext("context.xml");
		
		MyLittleScoreAlg alg = (MyLittleScoreAlg) ctx.getBean("myAlgorithm");
		EntityDao entityDao = (EntityDao) ctx.getBean("entityDao"); 
		
		List<Entity> eList = entityDao.findAll();
		
		if (eList.size() < 1000) {
			for (int i = 0; i < 1000; i++) {
				Entity newEntity = new Entity(i, 0);
				entityDao.saveOrUpdate(newEntity);
			}
		}
		
		eList = entityDao.findAll();
		for (Entity e: eList){
			alg.calculateScore(e);
			entityDao.saveOrUpdate(e);
		}
		
		eList = entityDao.findAll();
		
		for(Entity e: eList){
			System.out.print(e.toString() + " ");
		}
		
		System.out.println("");
		
		
		
	}
}
