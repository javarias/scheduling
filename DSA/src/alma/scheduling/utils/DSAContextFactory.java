package alma.scheduling.utils;

import org.springframework.context.support.AbstractApplicationContext;

/**
 * 
 * Handle Spring context singleton for the Scheduling Dynamic Algorithm names. The application
 * context xml configuration file must include the <b>alma.scheduling.algorithm.DSAContext.xml</b> and
 * define the Scheduling DSA configuration.
 * 
 * @since ALMA 8.1.0
 * @author javarias
 *
 */
public class DSAContextFactory extends CommonContextFactory {

	public static final String SCHEDULING_DSA_RESULTS_DAO_BEAN="DSAResultDAO";
	
	private static AbstractApplicationContext context = null;
	
	/**
	 * It will return the default ApplicationContext for the DSA, which is null
	 * @return null
	 */
	public static synchronized AbstractApplicationContext getContext() {
		return null;
		
	}
	
	public static synchronized AbstractApplicationContext getContext(String contextPath) {
		if (context == null) {
			context = SchedulingContextFactory.getContext(contextPath);
		}
		return context;
		
	}
	
}
