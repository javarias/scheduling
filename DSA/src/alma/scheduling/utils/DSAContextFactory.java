package alma.scheduling.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.context.support.AbstractApplicationContext;

/**
 * 
 * Handle Spring context singleton for the Scheduling Dynamic Algorithm names. The application
 * context xml configuration file must include the <b>alma.scheduling.algorithm.DSAContext.xml</b> and
 * define the Scheduling DSA configuration.
 * 
 * @since ALMA 8.1.0
 * @author javarias
 * $Id: DSAContextFactory.java,v 1.9 2011/07/20 17:06:56 javarias Exp $
 */
public class DSAContextFactory extends CommonContextFactory {

	protected static final String SCHEDULING_DSA_DEFAULT_SPRING_CONFIG = "classpath:alma/scheduling/algorithm/DSAContext.xml";
	//protected static final String SCHEDULING_DSA_DEFAULT_SPRING_CONFIG = "file:/export/home/griffin/javarias/scm/ALMA/HEAD/SCHEDULING/DSA/test/testPolicy.xml.context.xml";
	public static final String SCHEDULING_DSA_RESULTS_DAO_BEAN="DSAResultDAO";
	
	private static AbstractApplicationContext context = null;
	private static ArrayList<String> availablePolicies = null;
	
	/**
	 * It will return the default ApplicationContext for the DSA.
	 * @return the context defined in SCHEDULING_DSA_DEFAULT_SPRING_CONFIG
	 */
	public static synchronized AbstractApplicationContext getContext() {
		System.out.println(DSAContextFactory.class);
		if (context == null) {
			context = SchedulingContextFactory.getContext(SCHEDULING_DSA_DEFAULT_SPRING_CONFIG);
		}
		return context;
	}
	
	public static synchronized AbstractApplicationContext getContext(String contextPath) {
		System.out.println(DSAContextFactory.class);
		if (context == null) {
			context = SchedulingContextFactory.getContext(contextPath);
		}
		return context;
		
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static List<String> getPolicyNames() {
		if (context == null) {
			DSAContextFactory.getContext();
		}
		if (availablePolicies == null) {
			Map policies = context.getBeansOfType(alma.scheduling.algorithm.DynamicSchedulingAlgorithmImpl.class);
			availablePolicies = new ArrayList<String>(policies.keySet());
		}
		return availablePolicies;
	}
	
}
