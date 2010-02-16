package alma.scheduling.test;


import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import alma.scheduling.algorithm.DynamicSchedulingAlgorithm;
import alma.scheduling.algorithm.sbselection.NoSbSelectedException;

public class DsaTest {
	
	
	public static void main (String args[]) throws NoSbSelectedException{
		
		ApplicationContext ctx = new ClassPathXmlApplicationContext("context.xml");
		
		DynamicSchedulingAlgorithm dsa = (DynamicSchedulingAlgorithm) ctx.getBean("dsa");
		dsa.selectCandidateSB();
	}
}
