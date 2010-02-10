package alma.scheduling.test;


import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import alma.scheduling.algorithm.DynamicSchedulingAlgorithm;
import alma.scheduling.algorithm.sbselection.NoSbSelectedExecption;

public class DsaTest {
	
	
	public static void main (String args[]) throws NoSbSelectedExecption{
		
		ApplicationContext ctx = new ClassPathXmlApplicationContext("context.xml");
		
		DynamicSchedulingAlgorithm dsa = (DynamicSchedulingAlgorithm) ctx.getBean("dsa");
		dsa.selectCandidateSB();
	}
}
